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
package io.wcm.caconfig.editor.model;

import javax.annotation.PostConstruct;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.security.AccessControlManager;
import javax.jcr.security.Privilege;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.caconfig.resource.ConfigurationResourceResolver;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.osgi.annotation.versioning.ProviderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.replication.Replicator;
import com.day.cq.wcm.api.components.ComponentManager;

import io.wcm.caconfig.editor.impl.ConfigDataServlet;
import io.wcm.caconfig.editor.impl.ConfigNamesServlet;
import io.wcm.caconfig.editor.impl.ConfigPersistServlet;
import io.wcm.caconfig.editor.impl.EditorConfig;
import io.wcm.caconfig.editor.impl.UnifiedShellDetector;

/**
 * Provides editor configuration options
 */
@Model(adaptables = HttpServletRequest.class)
@ProviderType
public class EditorConfiguration {

  static final String RT_WCMIO_PATHFIELD = "wcm-io/wcm/ui/granite/components/form/pathfield";
  static final String PATH_PATHFIELD_STANDARD = "/mnt/overlay/granite/ui/content/coral/foundation/form/pathfield";
  static final String PATH_PATHFIELD_WCMIO = "/mnt/overlay/wcm-io/wcm/ui/granite/content/form/pathfield";
  static final String PATH_TAGFIELD_CQ = "/mnt/overlay/cq/gui/content/coral/common/form/tagfield";

  private static final Logger log = LoggerFactory.getLogger(EditorConfiguration.class);

  @SlingObject
  private Resource currentResource;
  @SlingObject
  private SlingHttpServletRequest request;
  @OSGiService
  private ConfigurationResourceResolver configResourceResolver;
  @OSGiService
  private EditorConfig editorConfig;
  @OSGiService
  private UnifiedShellDetector unifiedShellDetector;

  private String servletContextPathPrefix;
  private String configNamesUrl;
  private String configDataUrl;
  private String configPersistUrl;
  private String contextPath;
  private String pathfieldContentPath;
  private String tagfieldContentPath;
  private String language;
  private boolean enabled;
  private boolean canReplicate;
  private boolean unifiedShellAvailable;

  @PostConstruct
  private void activate() {
    this.servletContextPathPrefix = StringUtils.defaultString(request.getContextPath());
    if (StringUtils.equals(this.servletContextPathPrefix, "/")) {
      this.servletContextPathPrefix = "";
    }
    this.configNamesUrl = buildServletPath(ConfigNamesServlet.SELECTOR);
    this.configDataUrl = buildServletPath(ConfigDataServlet.SELECTOR);
    this.configPersistUrl = buildServletPath(ConfigPersistServlet.SELECTOR);
    this.contextPath = configResourceResolver.getContextPath(currentResource);
    this.pathfieldContentPath = buildPathfieldContentPath();
    this.tagfieldContentPath = buildTagfieldContentPath();
    this.language = request.getLocale().getLanguage();
    this.enabled = editorConfig.isEnabled();
    this.canReplicate = hasPermission(Replicator.REPLICATE_PRIVILEGE);
    this.unifiedShellAvailable = unifiedShellDetector.isAvailable();
  }

  private String buildServletPath(String selector) {
    return servletContextPathPrefix + currentResource.getPath() + "." + selector + ".json";
  }

  /**
   * Build path to pathfield content. If the module io.wcm.ui.granite is installed, the optimized
   * pathfield from that module is used, otherwise the default pathfield.
   * @return Pathfield content path
   */
  private String buildPathfieldContentPath() {
    ComponentManager componentManager = request.getResourceResolver().adaptTo(ComponentManager.class);
    if (componentManager != null && componentManager.getComponent(RT_WCMIO_PATHFIELD) != null) {
      return servletContextPathPrefix + PATH_PATHFIELD_WCMIO;
    }
    else {
      return servletContextPathPrefix + PATH_PATHFIELD_STANDARD;
    }
  }

  private boolean hasPermission(String privilegeName) {
    Session session = request.getResourceResolver().adaptTo(Session.class);
    if (session != null) {
      try {
        AccessControlManager accessControlManager = session.getAccessControlManager();
        Privilege privilege = accessControlManager.privilegeFromName(privilegeName);
        return accessControlManager.hasPrivileges(currentResource.getPath(), new Privilege[] { privilege });
      }
      catch (RepositoryException ex) {
        log.warn("Failed to check permission for {}", currentResource.getPath(), ex);
      }
    }
    return false;
  }

  private String buildTagfieldContentPath() {
    return servletContextPathPrefix + PATH_TAGFIELD_CQ;
  }

  public String getServletContextPathPrefix() {
    return this.servletContextPathPrefix;
  }

  public String getConfigNamesUrl() {
    return configNamesUrl;
  }

  public String getConfigDataUrl() {
    return configDataUrl;
  }

  public String getConfigPersistUrl() {
    return configPersistUrl;
  }

  public String getContextPath() {
    return this.contextPath;
  }

  public String getPathfieldContentPath() {
    return this.pathfieldContentPath;
  }

  public String getTagfieldContentPath() {
    return this.tagfieldContentPath;
  }

  public String getLanguage() {
    return this.language;
  }

  public boolean isEnabled() {
    return this.enabled;
  }

  public boolean isCanReplicate() {
    return this.canReplicate;
  }

  public boolean isUnifiedShellAvailable() {
    return this.unifiedShellAvailable;
  }

}
