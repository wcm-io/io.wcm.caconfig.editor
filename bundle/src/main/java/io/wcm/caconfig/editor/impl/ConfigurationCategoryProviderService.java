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

import org.apache.sling.api.resource.Resource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import io.wcm.caconfig.editor.ConfigurationCategory;
import io.wcm.sling.commons.caservice.ContextAwareServiceResolver;

/**
 * Manages getting and assigning configuration categories.
 */
@Component(service = ConfigurationCategoryProviderService.class)
public class ConfigurationCategoryProviderService {

  @Reference
  private ContextAwareServiceResolver serviceResolver;

  public @Nullable ConfigurationCategory getCategory(@NotNull Resource contextResource, @NotNull String configName) {
    return null;
  }

}
