/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2024 wcm.io
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.wcm.caconfig.editor.impl;

import static io.wcm.caconfig.editor.impl.NameConstants.RP_COLLECTION;
import static io.wcm.caconfig.editor.impl.NameConstants.RP_CONFIGNAME;
import static io.wcm.caconfig.extensions.persistence.testcontext.PersistenceTestUtils.writeConfiguration;
import static io.wcm.caconfig.extensions.persistence.testcontext.PersistenceTestUtils.writeConfigurationCollection;
import static org.apache.sling.api.resource.ResourceResolver.PROPERTY_RESOURCE_TYPE;
import static org.apache.sling.testing.mock.caconfig.ContextPlugins.CACONFIG;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.sling.testing.mock.caconfig.MockContextAwareConfig;
import org.apache.sling.testing.mock.osgi.MockOsgi;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.skyscreamer.jsonassert.JSONAssert;

import com.day.cq.wcm.api.Page;

import io.wcm.caconfig.extensions.persistence.example.ListConfig;
import io.wcm.caconfig.extensions.persistence.example.SimpleConfig;
import io.wcm.caconfig.extensions.persistence.impl.PagePersistenceStrategy;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextBuilder;
import io.wcm.testing.mock.aem.junit5.AemContextCallback;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class ConfigDataServletPagePersistenceAccessManagerTest {

  private final AemContext context = new AemContextBuilder()
      .resourceResolverType(ResourceResolverType.JCR_OAK)
      .beforeSetUp(new AemContextCallback() {
        @Override
        public void execute(@NotNull AemContext ctx) {
          // also find sling:configRef props in cq:Page/jcr:content nodes
          MockOsgi.setConfigForPid(ctx.bundleContext(), "org.apache.sling.caconfig.resource.impl.def.DefaultContextPathStrategy",
              "configRefResourceNames", new String[] { "jcr:content", "." });
          // AEM-specific configuration management settings
          MockOsgi.setConfigForPid(ctx.bundleContext(), "org.apache.sling.caconfig.management.impl.ConfigurationManagementSettingsImpl",
              "ignorePropertyNameRegex", new String[] { "^(jcr|cq):.+", "^" + PROPERTY_RESOURCE_TYPE + "$" },
              "configCollectionPropertiesResourceNames", new String[] { "jcr:content", "." });
        }
      })
      .plugin(CACONFIG)
      .build();

  private ConfigDataServlet underTest;

  private Page contentPage;

  @BeforeEach
  void setUp() {
    context.registerInjectActivateService(DropdownOptionProviderService.class);
    context.registerInjectActivateService(PathBrowserRootPathProviderService.class);
    context.registerInjectActivateService(TagBrowserRootPathProviderService.class);
    context.registerInjectActivateService(EditorConfig.class);
    underTest = context.registerInjectActivateService(ConfigDataServlet.class);

    // enable AEM page persistence strategy for this test
    context.registerInjectActivateService(PagePersistenceStrategy.class, "enabled", true);

    // create sample content page with config reference
    contentPage = context.create().page("/content/mypage", "/apps/app1/templates/template1",
        "sling:configRef", "/conf/myconf");
    context.currentPage(contentPage);
  }

  @Test
  void testSimpleConfig() throws Exception {
    MockContextAwareConfig.registerAnnotationClasses(context, SimpleConfig.class);

    // write config
    writeConfiguration(context, contentPage.getPath(), SimpleConfig.class.getName(),
        "stringParam", "value1",
        "intParam", 123);

    context.request().setParameterMap(Map.of(
        RP_CONFIGNAME, SimpleConfig.class.getName()));
    underTest.doGet(context.request(), context.response());

    assertEquals(HttpServletResponse.SC_OK, context.response().getStatus());

    String expectedJson = "{'configName':'io.wcm.caconfig.extensions.persistence.example.SimpleConfig',"
        + "'overridden':false,'inherited':false,'readOnly':false,"
        + "'configSourcePath':'/conf/myconf/sling:configs/io.wcm.caconfig.extensions.persistence.example.SimpleConfig/jcr:content',"
        + "'properties':["
        + "{'name':'boolParam','default':false,'inherited':false,'overridden':false,'metadata':{'type':'Boolean'}},"
        + "{'name':'intParam','value':123,'effectiveValue':123,"
        + "'configSourcePath':'/conf/myconf/sling:configs/io.wcm.caconfig.extensions.persistence.example.SimpleConfig/jcr:content',"
        + "'default':false,'inherited':false,'overridden':false,'metadata':{'type':'Integer','defaultValue':5}},"
        + "{'name':'stringParam','value':'value1','effectiveValue':'value1',"
        + "'configSourcePath':'/conf/myconf/sling:configs/io.wcm.caconfig.extensions.persistence.example.SimpleConfig/jcr:content',"
        + "'default':false,'inherited':false,'overridden':false,'metadata':{'type':'String'}}]}";
    JSONAssert.assertEquals(expectedJson, context.response().getOutputAsString(), true);
  }

  @Test
  void testListConfig() throws Exception {
    MockContextAwareConfig.registerAnnotationClasses(context, ListConfig.class);

    // write config
    writeConfigurationCollection(context, contentPage.getPath(), ListConfig.class.getName(), List.of(
        Map.of("stringParam", "value1", "intParam", 123)),
        Map.of("sling:configCollectionInherit", true));

    context.request().setParameterMap(Map.of(
        RP_CONFIGNAME, ListConfig.class.getName(),
        RP_COLLECTION, true));
    underTest.doGet(context.request(), context.response());

    assertEquals(HttpServletResponse.SC_OK, context.response().getStatus());

    String expectedJson = "{'configName':'io.wcm.caconfig.extensions.persistence.example.ListConfig','readOnly':false,"
        + "'configSourcePath':'/conf/myconf/sling:configs/io.wcm.caconfig.extensions.persistence.example.ListConfig',"
        + "'properties':{'sling:configCollectionInherit':true},"
        + "'items':["
        + "{'configName':'io.wcm.caconfig.extensions.persistence.example.ListConfig','collectionItemName':'item0',"
        + "'overridden':false,'inherited':false,'readOnly':false,"
        + "'configSourcePath':'/conf/myconf/sling:configs/io.wcm.caconfig.extensions.persistence.example.ListConfig/item0/jcr:content',"
        + "'properties':["
        + "{'name':'intParam','value':123,'effectiveValue':123,"
        + "'configSourcePath':'/conf/myconf/sling:configs/io.wcm.caconfig.extensions.persistence.example.ListConfig/item0/jcr:content',"
        + "'default':false,'inherited':false,'overridden':false,'metadata':{'type':'Integer'}},"
        + "{'name':'stringParam','value':'value1','effectiveValue':'value1',"
        + "'configSourcePath':'/conf/myconf/sling:configs/io.wcm.caconfig.extensions.persistence.example.ListConfig/item0/jcr:content',"
        + "'default':false,'inherited':false,'overridden':false,'metadata':{'type':'String'}}]}],"
        + "'newItem':{'configName':'io.wcm.caconfig.extensions.persistence.example.ListConfig','overridden':false,'properties':["
        + "{'name':'intParam','default':false,'inherited':false,'overridden':false,'metadata':{'type':'Integer'}},"
        + "{'name':'stringParam','default':false,'inherited':false,'overridden':false,'metadata':{'type':'String'}}]}}";
    JSONAssert.assertEquals(expectedJson, context.response().getOutputAsString(), true);
  }

}
