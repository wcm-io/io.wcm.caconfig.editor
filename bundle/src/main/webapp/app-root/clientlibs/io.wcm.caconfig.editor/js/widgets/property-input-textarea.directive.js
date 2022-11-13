/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2019 wcm.io
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
   * Directive for textarea field input, used for string multiline properties.
   */
  angular.module("io.wcm.caconfig.widgets")
    .directive("caconfigPropertyInputTextarea", propertyInputTextarea);

  propertyInputTextarea.$inject = ["$rootScope", "templateUrlList", "inputMap"];

  function propertyInputTextarea($rootScope, templateList, inputMap) {

    var directive = {
      templateUrl: templateList.propertyInputTextarea,
      scope: {
        property: "="
      },
      replace: true,
      link: link
    };

    return directive;

    function link(scope) {
      var input = inputMap[scope.property.metadata.type];
      scope.pattern = input.pattern;
      scope.i18n = $rootScope.i18n;

      // Validation settings
      var props = scope.property.metadata.properties || {};
      scope.validation = props.validation;
      scope.validationMessage = props.validationMessage ? Granite.I18n.get(props.validationMessage) : undefined;
    }
  }
}(angular));
