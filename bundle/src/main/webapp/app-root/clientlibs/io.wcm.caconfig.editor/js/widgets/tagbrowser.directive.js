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
    .directive("caconfigTagbrowser", tagbrowser);

  tagbrowser.$inject = ["templateUrlList", "directivePropertyPrefixes", "$timeout", "$rootScope", "configService", "tagbrowserService"];

  function tagbrowser(templateList, directivePropertyPrefixes, $timeout, $rootScope, configService, tagbrowserService) {
    var directive = {
      replace: true,
      templateUrl: templateList.tagbrowser,
      scope: {
        property: "="
      },
      link: link
    };

    return directive;

    function link(scope, element) {
      var prefix = directivePropertyPrefixes.tagbrowser;
      var props = scope.property.metadata.properties;
      console.log(scope.property);
      var options = {};
      var pathfieldWidget;
      var suggestionOverlay;
      var allValues = [];
      angular.forEach(props, function (value, prop) {
        var propName;
        // if the property starts with the prefix "tagbrowser" followed by a tagbrowser property name
        // remove the "tagbrowser" prefix and use the remaining part as option name
        if (prop && prop.length > prefix.length && prop.substring(0, prefix.length) !== -1) {
          propName = prop.substring(prefix.length);
          options[propName.charAt(0).toLowerCase() + propName.slice(1)] = props[prop];
        }
      });

      // get root path from config or take /content/cq:tags as default
      options.rootPath = options.rootPath || "/content/cq:tags";
      // if rootPathContext is set root path to current context path
      if (options.rootPathContext === "true") {
        options.rootPath = configService.getState().contextPath || options.rootPath;
        delete options.rootPathContext;
      }

      $timeout(function () {
        pathfieldWidget = element.find("foundation-autocomplete")[0];
        suggestionOverlay = element.find("coral-overlay[foundation-autocomplete-suggestion]")[0];

        Coral.commons.ready(pathfieldWidget, function() {
          pathfieldWidget.setAttribute("pickersrc", tagbrowserService.getPickerSrc(options.rootPath));
          suggestionOverlay.setAttribute("data-foundation-picker-buttonlist-src", tagbrowserService.getSuggestionSrc(options.rootPath));

          pathfieldWidget.value = scope.property.effectiveValue;

          // Add change event listen
          //why, I'm not able to trigger this at all
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
