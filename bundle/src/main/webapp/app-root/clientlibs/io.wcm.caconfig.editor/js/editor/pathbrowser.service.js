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
(function (angular) {
  "use strict";

  /**
   * Path browser service module
   */
  angular.module("io.wcm.caconfig.editor")
    .provider("pathbrowserService", PathbrowserServiceProvider);

  function PathbrowserServiceProvider() {
    var configUrls = {};

    function PathbrowserService(configUrls) {
      var that = this;

      that.getPickerSrc = function(rootPath) {
        return configUrls.pickerSrc.replace("{rootPath}", encodeURIComponent(rootPath));
      };

      that.getSuggestionSrc = function(rootPath) {
        return configUrls.suggestionSrc.replace("{rootPath}", encodeURIComponent(rootPath));
      };
    }

    this.setUrls = function (urls) {
      configUrls = urls;
    };

    this.$get = function () {
      return new PathbrowserService(configUrls);
    };
  }

}(angular));
