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

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Describes a configuration category.
 */
@ProviderType
public final class ConfigurationCategory implements Comparable<ConfigurationCategory> {

  private final String category;
  private String label;

  /**
   * @param category Category name
   */
  public ConfigurationCategory(@NotNull String category) {
    this.category = category;
  }

  /**
   * @return Category name
   */
  public @NotNull String getCategory() {
    return this.category;
  }

  /**
   * @return Category label (translatable via i18n). Falls back to category name if not defined.
   */
  public @NotNull String getLabel() {
    return StringUtils.defaultString(this.label, this.category);
  }

  /**
   * @param value Category label (translatable via i18n)
   * @return this
   */
  public ConfigurationCategory label(@Nullable String value) {
    this.label = value;
    return this;
  }

  @Override
  public int hashCode() {
    return this.category.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ConfigurationCategory) {
      return this.category.equals(((ConfigurationCategory)obj).category);
    }
    return false;
  }

  @Override
  public int compareTo(ConfigurationCategory other) {
    return this.category.compareTo(other.category);
  }

}
