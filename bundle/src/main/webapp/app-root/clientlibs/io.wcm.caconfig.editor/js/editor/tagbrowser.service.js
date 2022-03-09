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
   * Tag browser service module
   */
  angular.module("io.wcm.caconfig.editor")
    .provider("tagbrowserService", TagbrowserServiceProvider);

  function TagbrowserServiceProvider() {
    var configUrls = {};

    function TagbrowserService(configUrls) {
      var that = this;

      that.getPickerSrc = function(rootPath, mode) {
        var url = configUrls.pickerSrc;
        url = that.replacePlaceholder(url, "rootPath", rootPath);

        // guess nodeTypes filter from root path
        var damPath = /^\/content\/dam(\/.*)?$/;
        var nodeTypes = "cq:Page";
        if (damPath.test(rootPath)) {
          nodeTypes = "dam:Asset";
        }
        url = that.replacePlaceholder(url, "nodeTypes", nodeTypes);

        url = that.replacePlaceholder(url, "mode", mode);

        return url;
      };

      that.getSuggestionSrc = function(rootPath) {
        var url = configUrls.suggestionSrc;
        url = that.replacePlaceholder(url, "rootPath", rootPath);
        return url;
      };

      that.replacePlaceholder = function(url, placeholderKey, value) {
        return url.replace("{" + placeholderKey + "}", encodeURIComponent(value));
      }
    }

    this.setUrls = function (urls) {
      configUrls = urls;
    };

    this.$get = function () {
      return new TagbrowserService(configUrls);
    };
  }

}(angular));
