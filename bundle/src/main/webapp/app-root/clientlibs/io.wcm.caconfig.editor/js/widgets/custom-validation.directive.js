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
   * Directive for custom valiation on text fields.
   */
  angular.module("io.wcm.caconfig.widgets")
    .directive("caconfigCustomValidation", customValidation);

  function customValidation() {
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

        // create the regex obj.
        var regex = new RegExp(attr.caconfigCustomValidation);

        // add a parser that will process each time the value is 
        // parsed into the model when the user updates it.
        ctrl.$parsers.unshift(function(value) {
          // test and set the validity after update.
          var valid = regex.test(value);
          ctrl.$setValidity('caconfigCustomValidation', valid);

          // if it's valid, return the value to the model, 
          // otherwise return undefined.
          return valid ? value : undefined;
        });

        // add a formatter that will process each time the value 
        // is updated on the DOM element.
        ctrl.$formatters.unshift(function(value) {
          // validate.
          ctrl.$setValidity('caconfigCustomValidation', regex.test(value));

          // return the value or nothing will be written to the DOM.
          return value;
        });
      }
    };
  }
}(angular));
