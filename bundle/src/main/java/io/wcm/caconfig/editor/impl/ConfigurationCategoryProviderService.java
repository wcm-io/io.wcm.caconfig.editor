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

import static io.wcm.caconfig.editor.EditorProperties.PROPERTY_CATEGORY;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.caconfig.spi.metadata.ConfigurationMetadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.FieldOption;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import io.wcm.caconfig.editor.ConfigurationCategory;
import io.wcm.caconfig.editor.ConfigurationCategoryProvider;
import io.wcm.sling.commons.caservice.ContextAwareServiceCollectionResolver;
import io.wcm.sling.commons.caservice.ContextAwareServiceResolver;

/**
 * Manages getting and assigning configuration categories.
 * This service is only available if the bundle io.wcm.sling.commons is present in the system.
 */
@Component(service = ConfigurationCategoryProviderService.class)
public class ConfigurationCategoryProviderService {

  @Reference(cardinality = ReferenceCardinality.MULTIPLE, fieldOption = FieldOption.UPDATE,
      policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
  private SortedSet<ServiceReference<ConfigurationCategoryProvider>> filters = new ConcurrentSkipListSet<>(
      Collections.reverseOrder());

  @Reference
  private ContextAwareServiceResolver serviceResolver;
  private ContextAwareServiceCollectionResolver<ConfigurationCategoryProvider, Void> serviceCollectionResolver;

  @Activate
  private void activate() {
    // store a map with all categories per name for each provider
    this.serviceCollectionResolver = serviceResolver.getCollectionResolver(this.filters);
  }

  @Deactivate
  private void deactivate() {
    this.serviceCollectionResolver.close();
  }

  /**
   * Gets the category (including metadata) that is assigned to a configuration definition.
   * @param contextResource Content resource
   * @param configurationMetadata Configuration definition
   * @return Assigned/detected category, or null if none is detected.
   */
  public @Nullable ConfigurationCategory getCategory(@NotNull Resource contextResource, @NotNull ConfigurationMetadata configurationMetadata) {

    // check for assigned or detected category name
    String category = getCategoryName(contextResource, configurationMetadata);
    if (category == null) {
      return null;
    }

    // get category metadata
    return getCategoryMetadata(contextResource, category);
  }

  /**
   * Get category name that is in the metadata of the configuration definition.
   * If none is set, check if a {@link ConfigurationCategoryProvider} provides a configuration name.
   * Returns null if no category found.
   */
  @SuppressWarnings("null")
  private @Nullable String getCategoryName(@NotNull Resource contextResource, @NotNull ConfigurationMetadata configurationMetadata) {
    String category = getPropertiesString(configurationMetadata.getProperties(), PROPERTY_CATEGORY);

    if (StringUtils.isEmpty(category)) {
      category = serviceCollectionResolver.resolveAll(contextResource)
          .map(provider -> provider.getCategory(configurationMetadata))
          .filter(Objects::nonNull)
          .findFirst().orElse(null);
    }

    return category;
  }

  /**
   * Try to get configuration metadata from {@link ConfigurationCategoryProvider}.
   * If not present, return metadata only based on the internal category name as fallback.
   */
  @SuppressWarnings("null")
  private @NotNull ConfigurationCategory getCategoryMetadata(@NotNull Resource contextResource, @NotNull String category) {
    return serviceCollectionResolver.resolveAll(contextResource)
        .map(provider -> provider.getCategoryMetadata(category))
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(new ConfigurationCategory(category));
  }

  /**
   * Get directly assigned category bypassing any check for {@link ConfigurationCategoryProvider}.
   * This is used when io.wcm.sling.commons is not present.
   * @param configurationMetadata Configuration metadata
   * @return Category metadata or null if no category assigned
   */
  static final @Nullable ConfigurationCategory getAssignedCategory(@NotNull ConfigurationMetadata configurationMetadata) {
    String category = getPropertiesString(configurationMetadata.getProperties(), PROPERTY_CATEGORY);
    if (StringUtils.isNotEmpty(category)) {
      return new ConfigurationCategory(category);
    }
    return null;
  }

  private static String getPropertiesString(Map<String, String> properties, String key) {
    if (properties == null) {
      return null;
    }
    return properties.get(key);
  }

}
