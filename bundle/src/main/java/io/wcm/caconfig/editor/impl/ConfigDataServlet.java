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

import static io.wcm.caconfig.editor.impl.JsonMapper.OBJECT_MAPPER;
import static io.wcm.caconfig.editor.impl.NameConstants.RP_COLLECTION;
import static io.wcm.caconfig.editor.impl.NameConstants.RP_CONFIGNAME;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.caconfig.management.ConfigurationManager;
import org.apache.sling.caconfig.management.multiplexer.ConfigurationPersistenceStrategyMultiplexer;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.jetbrains.annotations.NotNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Read configuration data.
 */
@Component(service = Servlet.class)
@SlingServletResourceTypes(
    resourceTypes = "/apps/wcm-io/caconfig/editor/components/page/editor",
    selectors = ConfigDataServlet.SELECTOR,
    extensions = "json",
    methods = "GET")
public class ConfigDataServlet extends SlingSafeMethodsServlet {
  private static final long serialVersionUID = 1L;

  /**
   * Selector
   */
  public static final String SELECTOR = "configData";

  @Reference
  private ConfigurationManager configManager;
  @Reference
  private ConfigurationPersistenceStrategyMultiplexer configurationPersistenceStrategy;
  @Reference
  private EditorConfig editorConfig;
  @Reference
  private DropdownOptionProviderService dropdownOptionProviderService;
  @Reference
  private PathBrowserRootPathProviderService pathBrowserRootPathProviderService;
  @Reference
  private TagBrowserRootPathProviderService tagBrowserRootPathProviderService;

  private static Logger log = LoggerFactory.getLogger(ConfigDataServlet.class);

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  protected void doGet(@NotNull SlingHttpServletRequest request, @NotNull SlingHttpServletResponse response) throws ServletException, IOException {
    if (!editorConfig.isEnabled()) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    // get parameters
    String configName = request.getParameter(RP_CONFIGNAME);
    if (StringUtils.isBlank(configName)) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }
    boolean collection = BooleanUtils.toBoolean(request.getParameter(RP_COLLECTION));

    // output configuration
    try {
      ConfigDataResponseGenerator generator = new ConfigDataResponseGenerator(
          configManager, configurationPersistenceStrategy,
          dropdownOptionProviderService, pathBrowserRootPathProviderService, tagBrowserRootPathProviderService);
      Object result = generator.getConfiguration(request.getResource(), configName, collection);
      if (result == null) {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
      }
      else {
        response.setContentType("application/json;charset=" + StandardCharsets.UTF_8.name());
        response.getWriter().write(OBJECT_MAPPER.writeValueAsString(result));
      }
    }
    /*CHECKSTYLE:OFF*/ catch (Exception ex) { /*CHECKSTYLE:ON*/
      log.error("Error getting configuration for " + configName + (collection ? "[col]" : ""), ex);
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
    }
  }

}
