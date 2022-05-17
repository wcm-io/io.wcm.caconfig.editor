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
   * Directive for synchronous validation via a validator registered in Granite UI Foundation Registry.
   */
  angular.module("io.wcm.caconfig.widgets")
    .directive("caconfigValidationGraniteFoundation", validate);

  function validate() {
    return {
      // restrict to an attribute type.
      restrict: 'A',

      // element must have ng-model attribute.
      require: 'ngModel',

      // scope = the parent scope
      // elem = the element the directive is on
      // attr = a dictionary of attributes on the element
      // ctrl = the controller for ngModel.
      link: function(scope, elem, attr, ctrl) {
        var registry = $(window).adaptTo("foundation-registry");
        if (!registry) {
          return;
        }

        // build mocked DOM element with data-foundation-validation property to match selectors against
        var validatorName = attr.caconfigValidationGraniteFoundation
        var mockElement = document.createElement("div")
        mockElement.setAttribute("data-foundation-validation", validatorName);

        // find all matching validatiors from registry
        var validators = registry.get("foundation.validation.validator")
            .filter(item => item.selector != undefined && item.validate != undefined)
            .filter(item => mockElement.matches(item.selector));
        if (validators.length == 0) {
          return;
        }
        var validator = validators[0];

        // validate against matching validators
        ctrl.$validators.caconfigValidationGraniteFoundation = function(modelValue, viewValue) {
          var value = modelValue || viewValue;
          var mockInputElement = document.createElement("input");
          mockInputElement.value = value
          // GraniteUI validators return a validation message in case of failure
          // but we cannot pass it over to AngularJS here, so just return true if no messages was returned
          return validator.validate(mockInputElement) == undefined
        };

      }
    };
  }
}(angular));
