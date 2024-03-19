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

import static io.wcm.caconfig.editor.EditorProperties.PROPERTY_DROPDOWN_OPTIONS;
import static io.wcm.caconfig.editor.EditorProperties.PROPERTY_DROPDOWN_OPTIONS_PROVIDER;
import static io.wcm.caconfig.editor.EditorProperties.PROPERTY_PATHBROWSER_ROOT_PATH;
import static io.wcm.caconfig.editor.EditorProperties.PROPERTY_PATHBROWSER_ROOT_PATH_PROVIDER;
import static io.wcm.caconfig.editor.EditorProperties.PROPERTY_TAGBROWSER_ROOT_PATH;
import static io.wcm.caconfig.editor.EditorProperties.PROPERTY_TAGBROWSER_ROOT_PATH_PROVIDER;
import static io.wcm.caconfig.editor.EditorProperties.PROPERTY_WIDGET_TYPE;
import static io.wcm.caconfig.editor.EditorProperties.WIDGET_TYPE_DROPDOWN;
import static io.wcm.caconfig.editor.EditorProperties.WIDGET_TYPE_PATHBROWSER;
import static io.wcm.caconfig.editor.EditorProperties.WIDGET_TYPE_TAGBROWSER;
import static io.wcm.caconfig.editor.impl.JsonMapper.OBJECT_MAPPER;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.caconfig.management.ConfigurationCollectionData;
import org.apache.sling.caconfig.management.ConfigurationData;
import org.apache.sling.caconfig.management.ConfigurationManager;
import org.apache.sling.caconfig.management.ValueInfo;
import org.apache.sling.caconfig.management.multiplexer.ConfigurationPersistenceStrategyMultiplexer;
import org.apache.sling.caconfig.spi.ConfigurationPersistenceException;
import org.apache.sling.caconfig.spi.metadata.PropertyMetadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.wcm.caconfig.editor.impl.data.configdata.ConfigCollectionItem;
import io.wcm.caconfig.editor.impl.data.configdata.ConfigItem;
import io.wcm.caconfig.editor.impl.data.configdata.PropertyItem;
import io.wcm.caconfig.editor.impl.data.configdata.PropertyItemMetadata;

/**
 * Generates JSON response for {@link ConfigDataServlet}
 */
class ConfigDataResponseGenerator {

  private static final Pattern JSON_STRING_ARRAY_PATTERN = Pattern.compile("^\\[.*\\]$");
  private static final Pattern JSON_STRING_OBJECT_PATTERN = Pattern.compile("^\\{.*\\}$");

  private final ConfigurationManager configManager;
  private final ConfigurationPersistenceStrategyMultiplexer configurationPersistenceStrategy;
  private final DropdownOptionProviderService dropdownOptionProviderService;
  private final PathBrowserRootPathProviderService pathBrowserRootPathProviderService;
  private final TagBrowserRootPathProviderService tagBrowserRootPathProviderService;

  private static Logger log = LoggerFactory.getLogger(ConfigDataResponseGenerator.class);

  ConfigDataResponseGenerator(@NotNull ConfigurationManager configManager,
      @NotNull ConfigurationPersistenceStrategyMultiplexer configurationPersistenceStrategy,
      @NotNull DropdownOptionProviderService dropdownOptionProviderService,
      @NotNull PathBrowserRootPathProviderService pathBrowserRootPathProviderService,
      @NotNull TagBrowserRootPathProviderService tagBrowserRootPathProviderService) {
    this.configManager = configManager;
    this.configurationPersistenceStrategy = configurationPersistenceStrategy;
    this.dropdownOptionProviderService = dropdownOptionProviderService;
    this.pathBrowserRootPathProviderService = pathBrowserRootPathProviderService;
    this.tagBrowserRootPathProviderService = tagBrowserRootPathProviderService;
  }

  Object getConfiguration(@NotNull Resource contextResource, String configName, boolean collection) {
    Object result;
    if (collection) {
      ConfigurationData newItem = configManager.newCollectionItem(contextResource, configName);
      if (newItem == null) {
        throw new ConfigurationPersistenceException("Invalid configuration name: " + configName);
      }
      result = fromConfigCollection(contextResource,
          configManager.getConfigurationCollection(contextResource, configName), newItem, configName);
    }
    else {
      ConfigurationData configData = configManager.getConfiguration(contextResource, configName);
      if (configData != null) {
        result = fromConfig(contextResource, configData, configData.isInherited(), configName);
      }
      else {
        result = null;
      }
    }
    return result;
  }

  private ConfigCollectionItem fromConfigCollection(@NotNull Resource contextResource,
      ConfigurationCollectionData configCollection, ConfigurationData newItem, String fullConfigName) {
    ConfigCollectionItem result = new ConfigCollectionItem();
    result.setConfigName(configCollection.getConfigName());

    if (!configCollection.getProperties().isEmpty()) {
      Map<String, Object> properties = new TreeMap<>();
      for (Map.Entry<String, Object> entry : configCollection.getProperties().entrySet()) {
        properties.put(entry.getKey(), entry.getValue());
      }
      result.setProperties(properties);
    }

    List<ConfigItem> items = new ArrayList<>();
    for (ConfigurationData configData : configCollection.getItems()) {
      items.add(fromConfig(contextResource, configData, configData.isInherited(), fullConfigName));
    }
    result.setItems(items);

    result.setNewItem(fromConfig(contextResource, newItem, null, fullConfigName));

    return result;
  }

  @SuppressWarnings("java:S3776")
  private ConfigItem fromConfig(@NotNull Resource contextResource, ConfigurationData config, Boolean inherited, String fullConfigName) {
    ConfigItem result = new ConfigItem();

    result.setConfigName(config.getConfigName());
    result.setCollectionItemName(config.getCollectionItemName());
    result.setOverridden(config.isOverridden());
    result.setInherited(inherited);

    List<PropertyItem> props = new ArrayList<>();
    for (String propertyName : config.getPropertyNames()) {
      ValueInfo<?> item = config.getValueInfo(propertyName);
      if (item == null) {
        continue;
      }
      PropertyMetadata<?> itemMetadata = item.getPropertyMetadata();

      PropertyItem prop = new PropertyItem();
      prop.setName(item.getName());

      // special handling for nested configs and nested config collections
      if (itemMetadata != null && itemMetadata.isNestedConfiguration()) {
        PropertyItemMetadata metadata = new PropertyItemMetadata();
        metadata.setLabel(itemMetadata.getLabel());
        metadata.setDescription(itemMetadata.getDescription());
        metadata.setProperties(toJsonWithValueConversion(itemMetadata.getProperties(), contextResource));
        prop.setMetadata(metadata);

        if (itemMetadata.getType().isArray()) {
          ConfigurationData[] configDatas = (ConfigurationData[])item.getValue();
          if (configDatas != null) {
            ConfigCollectionItem nestedConfigCollection = new ConfigCollectionItem();
            StringBuilder collectionConfigName = new StringBuilder();
            if (config.getCollectionItemName() != null) {
              collectionConfigName.append(configurationPersistenceStrategy.getCollectionItemConfigName(fullConfigName
                  + "/" + config.getCollectionItemName(), config.getResourcePath()));
            }
            else {
              collectionConfigName.append(configurationPersistenceStrategy.getConfigName(fullConfigName, config.getResourcePath()));
            }
            collectionConfigName.append("/").append(itemMetadata.getConfigurationMetadata().getName());
            nestedConfigCollection.setConfigName(collectionConfigName.toString());
            List<ConfigItem> items = new ArrayList<>();
            for (ConfigurationData configData : configDatas) {
              items.add(fromConfig(contextResource, configData, false, collectionConfigName.toString()));
            }
            nestedConfigCollection.setItems(items);
            prop.setNestedConfigCollection(nestedConfigCollection);
          }
        }
        else {
          ConfigurationData configData = (ConfigurationData)item.getValue();
          if (configData != null) {
            prop.setNestedConfig(fromConfig(contextResource, configData, null, fullConfigName
                + "/" + itemMetadata.getConfigurationMetadata().getName()));
          }
        }
      }

      // property data and metadata
      else {
        prop.setValue(item.getValue());
        prop.setEffectiveValue(item.getEffectiveValue());
        prop.setConfigSourcePath(item.getConfigSourcePath());
        prop.setIsDefault(item.isDefault());
        prop.setInherited(item.isInherited());
        prop.setOverridden(item.isOverridden());

        if (itemMetadata != null) {
          PropertyItemMetadata metadata = new PropertyItemMetadata();
          if (itemMetadata.getType().isArray()) {
            metadata.setType(ClassUtils.primitiveToWrapper(itemMetadata.getType().getComponentType()).getSimpleName());
            metadata.setMultivalue(true);
          }
          else {
            metadata.setType(ClassUtils.primitiveToWrapper(itemMetadata.getType()).getSimpleName());
          }
          metadata.setDefaultValue(itemMetadata.getDefaultValue());
          metadata.setLabel(itemMetadata.getLabel());
          metadata.setDescription(itemMetadata.getDescription());
          metadata.setProperties(toJsonWithValueConversion(itemMetadata.getProperties(), contextResource));
          prop.setMetadata(metadata);
        }
      }
      props.add(prop);
    }
    result.setProperties(props);

    return result;
  }

  /**
   * Converts the given map to JSON. Each map value is checked for a valid JSON string - if this is the case it's
   * inserted as JSON objects and not as string.
   * @param properties Map
   * @param contextResource Context resource
   * @return JSON object or null
   */
  @SuppressWarnings({ "PMD.ReturnEmptyCollectionRatherThanNull", "java:S3776" })
  private @Nullable Map<String, Object> toJsonWithValueConversion(@Nullable Map<String, String> properties,
      @NotNull Resource contextResource) {
    if (properties == null || properties.isEmpty()) {
      return null;
    }

    Map<String, Object> metadataProps = new TreeMap<>();
    for (Map.Entry<String, String> entry : properties.entrySet()) {
      metadataProps.put(entry.getKey(), tryConvertJsonString(entry.getValue()));
    }

    // check for dynamic dropdown option injection
    boolean isDropdown = WIDGET_TYPE_DROPDOWN.equals(metadataProps.get(PROPERTY_WIDGET_TYPE));
    if (isDropdown) {
      Optional<String> dynamicProvider = Optional.ofNullable(metadataProps.get(PROPERTY_DROPDOWN_OPTIONS_PROVIDER))
          .filter(Objects::nonNull)
          .map(String::valueOf)
          .filter(StringUtils::isNotBlank);
      if (dynamicProvider.isPresent()) {
        List<Map<String, Object>> items = dropdownOptionProviderService.getDropdownOptions(dynamicProvider.get(), contextResource);
        if (!items.isEmpty()) {
          metadataProps.put(PROPERTY_DROPDOWN_OPTIONS, items);
        }
        metadataProps.remove(PROPERTY_DROPDOWN_OPTIONS_PROVIDER);
      }
    }

    // check for dynamic root path injection
    boolean isPathBrowser = WIDGET_TYPE_PATHBROWSER.equals(metadataProps.get(PROPERTY_WIDGET_TYPE));
    if (isPathBrowser) {
      Optional<String> dynamicProvider = Optional.ofNullable(metadataProps.get(PROPERTY_PATHBROWSER_ROOT_PATH_PROVIDER))
          .filter(Objects::nonNull)
          .map(String::valueOf)
          .filter(StringUtils::isNotBlank);
      if (dynamicProvider.isPresent()) {
        String rootPath = pathBrowserRootPathProviderService.getRootPath(dynamicProvider.get(), contextResource);
        if (rootPath != null) {
          metadataProps.put(PROPERTY_PATHBROWSER_ROOT_PATH, rootPath);
        }
        metadataProps.remove(PROPERTY_PATHBROWSER_ROOT_PATH_PROVIDER);
      }
    }

    boolean isTagBrowser = WIDGET_TYPE_TAGBROWSER.equals(metadataProps.get(PROPERTY_WIDGET_TYPE));
    if (isTagBrowser) {
      Optional<String> dynamicProvider = Optional.ofNullable(metadataProps.get(PROPERTY_TAGBROWSER_ROOT_PATH_PROVIDER))
          .filter(Objects::nonNull)
          .map(String::valueOf)
          .filter(StringUtils::isNotBlank);
      if (dynamicProvider.isPresent()) {
        String rootPath = tagBrowserRootPathProviderService.getRootPath(dynamicProvider.get(), contextResource);
        if (rootPath != null) {
          metadataProps.put(PROPERTY_TAGBROWSER_ROOT_PATH, rootPath);
        }
        metadataProps.remove(PROPERTY_TAGBROWSER_ROOT_PATH_PROVIDER);
      }
    }

    return metadataProps;
  }

  private @Nullable Object tryConvertJsonString(@Nullable String value) {
    if (value == null) {
      return null;
    }
    if (JSON_STRING_ARRAY_PATTERN.matcher(value).matches()) {
      try {
        return OBJECT_MAPPER.readValue(value, List.class);
      }
      catch (IOException ex) {
        // no valid json - ignore
        log.trace("Conversion to JSON array value failed for: {}", value, ex);
      }
    }
    if (JSON_STRING_OBJECT_PATTERN.matcher(value).matches()) {
      try {
        return OBJECT_MAPPER.readValue(value, Map.class);
      }
      catch (IOException ex) {
        // no valid json - ignore
        log.trace("Conversion to JSON object value failed for: {}", value, ex);
      }
    }
    return value;
  }

}
