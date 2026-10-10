/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2026 wcm.io
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
   * Directive for password field input, used for encrypted string properties.
   */
  angular.module("io.wcm.caconfig.widgets")
    .directive("caconfigPropertyInputPassword", propertyInputPassword);

  propertyInputPassword.$inject = ["$rootScope", "templateUrlList"];

  function propertyInputPassword($rootScope, templateList) {

    var directive = {
      templateUrl: templateList.propertyInputPassword,
      scope: {
        property: "="
      },
      replace: true,
      link: link
    };

    return directive;

    function link(scope) {
      scope.i18n = $rootScope.i18n;

      // Validation settings
      var props = scope.property.metadata.properties || {};
      scope.validation = props.validation;
      scope.validationMessage = props.validationMessage ? Granite.I18n.get(props.validationMessage) : undefined;
    }
  }
}(angular));
