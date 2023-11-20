/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2023 wcm.io
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

import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

/**
 * Detects Unified Shell (on AEMaaCS).
 */
@Component(service = UnifiedShellDetector.class, immediate = true)
public class UnifiedShellDetector {

  private static final String BUNDLE_UNIFIED_SHELL_INTEGRATION = "com.adobe.cq.unifiedshell.unified-shell-integration";

  private boolean available;

  @Activate
  private void activate(BundleContext bundleContext) {
    this.available = Stream.of(bundleContext.getBundles())
        .anyMatch(bundle -> StringUtils.equals(bundle.getSymbolicName(), BUNDLE_UNIFIED_SHELL_INTEGRATION));
  }

  /**
   * @return True if Unified Shell Bundle is available
   */
  public boolean isAvailable() {
    return this.available;
  }

}
