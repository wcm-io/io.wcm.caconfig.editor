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
package io.wcm.caconfig.editor;

import org.apache.sling.caconfig.spi.metadata.ConfigurationMetadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ConsumerType;

import io.wcm.sling.commons.caservice.ContextAwareService;

/**
 * Provides metadata for configuration categories used by an application.
 * <p>
 * Applications can set service properties or bundle headers as defined in {@link ContextAwareService} to apply this
 * configuration only for resources that match the relevant resource paths.
 * </p>
 */
@ConsumerType
public interface ConfigurationCategoryProvider extends ContextAwareService {

  /**
   * Get metadata for a given configuration name.
   * @param category Category name
   * @return Metadata or null if category is not known
   */
  @Nullable
  ConfigurationCategory getCategoryMetadata(@NotNull String category);

  /**
   * This method is called for configurations that are in use and do not have a category name assigned.
   * With this method it's possible to assign categories by inspecting the configuration metadata, or
   * to just assign a default category.
   * @param configurationMetadata Metadata of configuration that does not have a category defined
   * @return Category name or null if no category should be assigned
   */
  @Nullable
  String getCategory(@NotNull ConfigurationMetadata configurationMetadata);

}
