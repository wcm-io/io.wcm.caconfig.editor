/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2022 wcm.io
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

import io.wcm.caconfig.editor.TagBrowserRootPathProvider;
import org.apache.sling.api.resource.Resource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

/**
 * Get dynamic rooth paths for tag browser.
 */
@Component(service = TagBrowserRootPathProviderService.class)
public class TagBrowserRootPathProviderService {

  private BundleContext bundleContext;

  @Activate
  private void activated(BundleContext bc) {
    this.bundleContext = bc;
  }

  /**
   * Get root path from service implementations.
   * @param contextResource Context resource
   * @return Root path or null
   */
  @SuppressWarnings({ "null", "java:S112" })
  public @Nullable String getRootPath(@NotNull String selector, @NotNull Resource contextResource) {
    final String filter = "(" + TagBrowserRootPathProvider.PROPERTY_SELECTOR + "=" + selector + ")";
    try {
      ServiceReference<TagBrowserRootPathProvider> ref = bundleContext.getServiceReferences(TagBrowserRootPathProvider.class, filter)
          .stream().findFirst().orElse(null);
      if (ref != null) {
        TagBrowserRootPathProvider provider = bundleContext.getService(ref);
        try {
          return provider.getRootPath(contextResource);
        }
        finally {
          bundleContext.ungetService(ref);
        }
      }
    }
    catch (InvalidSyntaxException ex) {
      throw new RuntimeException("Invalid filter syntax: " + filter, ex);
    }
    return null;
  }

}
