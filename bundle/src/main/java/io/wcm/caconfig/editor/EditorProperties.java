/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2017 wcm.io
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

import org.osgi.annotation.versioning.ProviderType;

/**
 * Properties that can be used for configuration property definitions to customize the edit widget within the editor.
 */
@ProviderType
public final class EditorProperties {

  private EditorProperties() {
    // constants only
  }

  /**
   * Property name for defining a category for configurations. The editor allows to filter by category.
   * This property has to be applied to a configuration definition, not to a property definition.
   */
  public static final String PROPERTY_CATEGORY = "category";

  /**
   * Property name for defining the widget type. Values should be one of the WIDGET_TYPE_* properties.
   */
  public static final String PROPERTY_WIDGET_TYPE = "widgetType";

  /**
   * Widget type to add a pathbrowser selection widget to a string parameter.
   */
  public static final String WIDGET_TYPE_PATHBROWSER = "pathbrowser";

  /**
   * With this additional property the root path for the path browser widget can be set:
   * The root path is passed as string value of the property.
   */
  public static final String PROPERTY_PATHBROWSER_ROOT_PATH = "pathbrowserRootPath";

  /**
   * With this additional property the root path for the path browser widget can be set:
   * If set to true, the current configuration context path is used as root path.
   */
  public static final String PROPERTY_PATHBROWSER_ROOT_PATH_CONTEXT = "pathbrowserRootPathContext";

  /**
   * Defines the OSGi service property of a {@link PathBrowserRootPathProvider} implementation
   * that should be used to dynamically define the root path, instead of providing
   * a fixed one via {@link #PROPERTY_PATHBROWSER_ROOT_PATH}.
   */
  public static final String PROPERTY_PATHBROWSER_ROOT_PATH_PROVIDER = "pathbrowserRootPathProvider";

  /**
   * Widget type to add a dropdown list selection widget to a string or number parameter.
   */
  public static final String WIDGET_TYPE_DROPDOWN = "dropdown";

  /**
   * Defines the list of dropdown options as JSON array with the list options.
   * Each list option item is a JSON object with two properties <code>value</code> and <code>description</code>.
   */
  public static final String PROPERTY_DROPDOWN_OPTIONS = "dropdownOptions";

  /**
   * Defines the OSGi service property of a {@link io.wcm.caconfig.editor.DropdownOptionProvider} implementation
   * that should be used to dynamically fetch a list of dropdown options, instead of providing
   * a fixed set of dropdown options via {@link #PROPERTY_DROPDOWN_OPTIONS}.
   */
  public static final String PROPERTY_DROPDOWN_OPTIONS_PROVIDER = "dropdownOptionsProvider";

  /**
   * Widget type that allows to enter multiple lines of text for a string parameter.
   */
  public static final String WIDGET_TYPE_TEXTAREA = "textarea";

  /**
   * Widget type to add a tagbrowser selection widget to a string parameter.
   */
  public static final String WIDGET_TYPE_TAGBROWSER = "tagbrowser";

  /**
   * With this additional property the root path for the tag browser widget can be set:
   * The root path is passed as string value of the property.
   */
  public static final String PROPERTY_TAGBROWSER_ROOT_PATH = "tagbrowserRootPath";

  /**
   * Defines the OSGi service property of a {@link PathBrowserRootPathProvider} implementation
   * that should be used to dynamically define the root path, instead of providing
   * a fixed one via {@link #PROPERTY_TAGBROWSER_ROOT_PATH}.
   */
  public static final String PROPERTY_TAGBROWSER_ROOT_PATH_PROVIDER = "tagbrowserRootPathProvider";

  /**
   * Define this property as required/mandatory. Configuration cannot be saved if no value is given.
   */
  public static final String PROPERTY_REQUIRED = "required";

  /**
   * Property name for defining a custom validation for a string or numeric parameter.
   * The value is either a name of a Granite UI Foundation Validator, or a registered custom validation method.
   * See <a href="https://wcm.io/caconfig/editor/validation.html">documentation</a> for details.
   */
  public static final String PROPERTY_VALIDATION = "validation";

  /**
   * Property name for defining a custom validation message.
   * Set to a message (or i18n key) to be displayed if any of the configured validations fails.
   */
  public static final String PROPERTY_VALIDATION_MESSAGE = "validationMessage";

  /**
   * Property name for defining the property "rows" of Textarea.
   */
  public static final String PROPERTY_TEXTAREA_ROWS = "textareaRows";

}
