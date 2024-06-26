/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2016 wcm.io
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

import static io.wcm.caconfig.editor.EditorProperties.PROPERTY_CATEGORY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.caconfig.management.ConfigurationCollectionData;
import org.apache.sling.caconfig.management.ConfigurationData;
import org.apache.sling.caconfig.management.ConfigurationManager;
import org.apache.sling.caconfig.resource.ConfigurationResourceResolver;
import org.apache.sling.caconfig.spi.metadata.ConfigurationMetadata;
import org.apache.sling.caconfig.spi.metadata.PropertyMetadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONAssert;

import io.wcm.caconfig.editor.ConfigurationCategory;
import io.wcm.caconfig.editor.ConfigurationCategoryProvider;
import io.wcm.caconfig.editor.ConfigurationEditorFilter;
import io.wcm.sling.commons.caservice.impl.ContextAwareServiceResolverImpl;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class ConfigNamesServletTest {

  private final AemContext context = new AemContext();

  @Mock
  private ConfigurationManager configManager;
  @Mock
  private ConfigurationResourceResolver configurationResourceResolver;
  @Mock
  private ConfigurationData configData;

  @BeforeEach
  void setUp() {
    context.currentResource(context.create().resource("/content/test"));

    ConfigurationMetadata metadata1 = new ConfigurationMetadata("name1", List.<PropertyMetadata<?>>of(), false)
        .label("B-label1")
        .description("desc1")
        .properties(Map.of(PROPERTY_CATEGORY, "category1"));
    ConfigurationMetadata metadata2 = new ConfigurationMetadata("name2", List.<PropertyMetadata<?>>of(), true)
        .label("A-label2");
    ConfigurationMetadata metadata3 = new ConfigurationMetadata("name3", List.<PropertyMetadata<?>>of(), false)
        .label("C-label3");
    ConfigurationMetadata metadata4 = new ConfigurationMetadata("name4", List.<PropertyMetadata<?>>of(), false);

    when(configManager.getConfigurationNames()).thenReturn(new TreeSet<>(Set.of("name1", "name2", "name3", "name4")));
    when(configManager.getConfigurationMetadata("name1")).thenReturn(metadata1);
    when(configManager.getConfigurationMetadata("name2")).thenReturn(metadata2);
    when(configManager.getConfigurationMetadata("name3")).thenReturn(metadata3);
    when(configManager.getConfigurationMetadata("name4")).thenReturn(metadata4);

    when(configManager.getConfiguration(context.currentResource(), "name1")).thenReturn(configData);
    ConfigurationCollectionData configCollectionData = mock(ConfigurationCollectionData.class);
    when(configCollectionData.getItems()).thenReturn(List.of(configData));
    when(configManager.getConfigurationCollection(context.currentResource(), "name2")).thenReturn(configCollectionData);

    when(configurationResourceResolver.getContextPath(context.currentResource())).thenReturn("/context/path");

    context.registerService(ConfigurationManager.class, configManager);
    context.registerService(ConfigurationResourceResolver.class, configurationResourceResolver);
    context.registerInjectActivateService(new EditorConfig());
  }

  @Test
  void testResponse() throws Exception {
    ConfigNamesServlet underTest = context.registerInjectActivateService(ConfigNamesServlet.class);
    underTest.doGet(context.request(), context.response());

    assertEquals(HttpServletResponse.SC_OK, context.response().getStatus());

    String expectedJson = "{contextPath:'/context/path',configNames:["
        + "{configName:'name2',label:'A-label2',collection:true,exists:true,inherited:false,overridden:false,allowAdd:true},"
        + "{configName:'name1',label:'B-label1',description:'desc1',category:'category1',collection:false,exists:false,inherited:false,overridden:false,allowAdd:true},"
        + "{configName:'name3',label:'C-label3',collection:false,exists:false,inherited:false,overridden:false,allowAdd:true},"
        + "{configName:'name4',collection:false,exists:false,inherited:false,overridden:false,allowAdd:true}"
        + "],configCategories:[{category:'category1',label:'category1'}]}";
    JSONAssert.assertEquals(expectedJson, context.response().getOutputAsString(), true);
  }

  @Test
  void testResponseWithInheritedOverriddenExists() throws Exception {

    when(configData.getResourcePath()).thenReturn("/path");
    when(configData.isInherited()).thenReturn(true);
    when(configData.isOverridden()).thenReturn(true);

    ConfigNamesServlet underTest = context.registerInjectActivateService(ConfigNamesServlet.class);
    underTest.doGet(context.request(), context.response());

    assertEquals(HttpServletResponse.SC_OK, context.response().getStatus());

    String expectedJson = "{contextPath:'/context/path',configNames:["
        + "{configName:'name2',label:'A-label2',collection:true,exists:true,inherited:true,overridden:true,allowAdd:true},"
        + "{configName:'name1',label:'B-label1',description:'desc1',category:'category1',collection:false,exists:true,inherited:true,overridden:true,allowAdd:true},"
        + "{configName:'name3',label:'C-label3',collection:false,exists:false,inherited:false,overridden:false,allowAdd:true},"
        + "{configName:'name4',collection:false,exists:false,inherited:false,overridden:false,allowAdd:true}"
        + "],configCategories:[{category:'category1',label:'category1'}]}";
    JSONAssert.assertEquals(expectedJson, context.response().getOutputAsString(), true);
  }

  @Test
  void testResponseWithOverriddenOnly() throws Exception {

    when(configData.getResourcePath()).thenReturn(null);
    when(configData.isInherited()).thenReturn(false);
    when(configData.isOverridden()).thenReturn(true);

    ConfigNamesServlet underTest = context.registerInjectActivateService(ConfigNamesServlet.class);
    underTest.doGet(context.request(), context.response());

    assertEquals(HttpServletResponse.SC_OK, context.response().getStatus());

    String expectedJson = "{contextPath:'/context/path',configNames:["
        + "{configName:'name2',label:'A-label2',collection:true,exists:true,inherited:false,overridden:true,allowAdd:true},"
        + "{configName:'name1',label:'B-label1',description:'desc1',category:'category1',collection:false,exists:true,inherited:false,overridden:true,allowAdd:true},"
        + "{configName:'name3',label:'C-label3',collection:false,exists:false,inherited:false,overridden:false,allowAdd:true},"
        + "{configName:'name4',collection:false,exists:false,inherited:false,overridden:false,allowAdd:true}"
        + "],configCategories:[{category:'category1',label:'category1'}]}";
    JSONAssert.assertEquals(expectedJson, context.response().getOutputAsString(), true);
  }

  @Test
  void testResponseWithFiltering() throws Exception {
    context.registerService(ConfigurationEditorFilter.class, new ConfigurationEditorFilter() {
      @Override
      public boolean allowAdd(@NotNull String configName) {
        return !StringUtils.equals(configName, "name3");
      }
    });
    context.registerInjectActivateService(ContextAwareServiceResolverImpl.class);
    context.registerInjectActivateService(ConfigurationEditorFilterService.class);
    ConfigNamesServlet underTest = context.registerInjectActivateService(ConfigNamesServlet.class);

    underTest.doGet(context.request(), context.response());

    assertEquals(HttpServletResponse.SC_OK, context.response().getStatus());

    String expectedJson = "{contextPath:'/context/path',configNames:["
        + "{configName:'name2',label:'A-label2',collection:true,exists:true,inherited:false,overridden:false,allowAdd:true},"
        + "{configName:'name1',label:'B-label1',description:'desc1',category:'category1',collection:false,exists:false,inherited:false,overridden:false,allowAdd:true},"
        + "{configName:'name3',label:'C-label3',collection:false,exists:false,inherited:false,overridden:false,allowAdd:false},"
        + "{configName:'name4',collection:false,exists:false,inherited:false,overridden:false,allowAdd:true}"
        + "],configCategories:[{category:'category1',label:'category1'}]}";
    JSONAssert.assertEquals(expectedJson, context.response().getOutputAsString(), true);
  }

  @Test
  void testResponseWithCategories() throws Exception {
    context.registerInjectActivateService(ContextAwareServiceResolverImpl.class);
    context.registerInjectActivateService(ConfigurationCategoryProviderService.class);

    ConfigNamesServlet underTest = context.registerInjectActivateService(ConfigNamesServlet.class);
    underTest.doGet(context.request(), context.response());

    assertEquals(HttpServletResponse.SC_OK, context.response().getStatus());

    String expectedJson = "{contextPath:'/context/path',configNames:["
        + "{configName:'name2',label:'A-label2',collection:true,exists:true,inherited:false,overridden:false,allowAdd:true},"
        + "{configName:'name1',label:'B-label1',description:'desc1',category:'category1',collection:false,exists:false,inherited:false,overridden:false,allowAdd:true},"
        + "{configName:'name3',label:'C-label3',collection:false,exists:false,inherited:false,overridden:false,allowAdd:true}"
        + ",{configName:'name4',collection:false,exists:false,inherited:false,overridden:false,allowAdd:true}"
        + "],configCategories:[{category:'category1',label:'category1'}]}";
    JSONAssert.assertEquals(expectedJson, context.response().getOutputAsString(), true);
  }

  @Test
  void testResponseWithCategoriesAndConfigurationCategoryProvider() throws Exception {
    context.registerService(ConfigurationCategoryProvider.class, new ConfigurationCategoryProvider() {
      @Override
      public @Nullable ConfigurationCategory getCategoryMetadata(@NotNull String category) {
        return new ConfigurationCategory(category).label(StringUtils.capitalize(category) + "!");
      }
      @Override
      public @Nullable String getCategory(@NotNull ConfigurationMetadata configurationMetadata) {
        return "defaultCategory";
      }
    });

    context.registerInjectActivateService(ContextAwareServiceResolverImpl.class);
    context.registerInjectActivateService(ConfigurationCategoryProviderService.class);

    ConfigNamesServlet underTest = context.registerInjectActivateService(ConfigNamesServlet.class);
    underTest.doGet(context.request(), context.response());

    assertEquals(HttpServletResponse.SC_OK, context.response().getStatus());

    String expectedJson = "{contextPath:'/context/path',configNames:["
        + "{configName:'name2',label:'A-label2',category:'defaultCategory',collection:true,exists:true,inherited:false,overridden:false,allowAdd:true},"
        + "{configName:'name1',label:'B-label1',description:'desc1',category:'category1',collection:false,exists:false,inherited:false,overridden:false,allowAdd:true},"
        + "{configName:'name3',label:'C-label3',category:'defaultCategory',collection:false,exists:false,inherited:false,overridden:false,allowAdd:true},"
        + "{configName:'name4',category:'defaultCategory',collection:false,exists:false,inherited:false,overridden:false,allowAdd:true}"
        + "],configCategories:[{category:'category1',label:'Category1!'},{category:'defaultCategory',label:'DefaultCategory!'}]}";
    JSONAssert.assertEquals(expectedJson, context.response().getOutputAsString(), true);
  }

}
