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
(function (angular, Coral, $) {
  "use strict";

  angular.module("io.wcm.caconfig.widgets")
    .directive("caconfigPathbrowser", pathbrowser);

  pathbrowser.$inject = ["templateUrlList", "directivePropertyPrefixes", "$timeout", "$rootScope", "configService", "pathbrowserService"];

  function pathbrowser(templateList, directivePropertyPrefixes, $timeout, $rootScope, configService, pathbrowserService) {
    var directive = {
      replace: true,
      templateUrl: templateList.pathbrowser,
      scope: {
        property: "="
      },
      link: link
    };

    return directive;

    function link(scope, element) {
      var prefix = directivePropertyPrefixes.pathbrowser;
      var props = scope.property.metadata.properties;
      var options = {};
      var pathfieldWidget;
      var suggestionOverlay;

      angular.forEach(props, function (value, prop) {
        var propName;
        // if the property starts with the prefix "pathbrowser" followed by a pathbrowser property name
        // remove the "pathbrowser" prefix and use the remaining part as option name
        if (prop && prop.length > prefix.length && prop.substring(0, prefix.length) !== -1) {
          propName = prop.substring(prefix.length);
          options[propName.charAt(0).toLowerCase() + propName.slice(1)] = props[prop];
        }
      });

      // get root path from config
      options.rootPath = options.rootPath || "/content";
      // if rootPathContext is set set root path to current context path
      if (options.rootPathContext === "true") {
        options.rootPath = configService.getState().contextPath || options.rootPath;
        delete options.rootPathContext;
      }

      $timeout(function () {
        pathfieldWidget = element.find("foundation-autocomplete")[0];
        suggestionOverlay = element.find("coral-overlay[foundation-autocomplete-suggestion]")[0];

        Coral.commons.ready(pathfieldWidget, function() {
          pathfieldWidget.setAttribute("pickersrc", pathbrowserService.getPickerSrc(options.rootPath));
          suggestionOverlay.setAttribute("data-foundation-picker-buttonlist-src", pathbrowserService.getSuggestionSrc(options.rootPath));

          pathfieldWidget.value = scope.property.effectiveValue;

          // Add change event listen
          $(pathfieldWidget).on("change", function onChange() {
            scope.property.value = pathfieldWidget.value;

            if ($rootScope.configForm.$pristine) {
              $rootScope.configForm.$setDirty();
              scope.$digest();
            }
          });
        });
      });
    }

  }
}(angular, Coral, Granite.$));
