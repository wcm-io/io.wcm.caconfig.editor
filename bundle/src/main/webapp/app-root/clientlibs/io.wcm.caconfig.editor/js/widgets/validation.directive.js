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
   * Directive for custom validation (synchronous or asynchronous).
   */
  angular.module("io.wcm.caconfig.widgets")
    .directive("caconfigValidation", validation);

  validation.$inject = ["$q", "configService"];

  function validation($q, configService) {
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

        var options = buildOptions(configService);

        // observe attribute to get result from interpolations
        attr.$observe('caconfigValidation', function(validatorName) {
          applyValidator(ctrl, validatorName, options, registry, $q);
        });
      }
    };
  }

  /**
   * Builds options to pass additional context information to the validators.
   */
  function buildOptions(configService) {
    return {
      contextPath: configService.getState().contextPath
    }
  }

  /**
   * Apply validators to input control.
   */
  function applyValidator(ctrl, validatorName, options, registry, $q) {
    var validator = getValidator(validatorName, registry);
    if (!validator) {
      return;
    }

    // apply validator synchronously or asynchronously
    if (validator.async) {
      applyAsyncValidator(ctrl, validator, options, $q);
    }
    else {
      applySyncValidator(ctrl, validator, options);
    }
  }

  function applySyncValidator(ctrl, validator, options) {
    ctrl.$validators.caconfigValidation = function(modelValue, viewValue) {
      var value = modelValue || viewValue;
      if (value) {
        return validator.validate(value, options);
      }
      return true
    }
  }

  function applyAsyncValidator(ctrl, validator, options, $q) {
    ctrl.$asyncValidators.caconfigValidation = function(modelValue, viewValue) {
      var deferred = $q.defer();
      var value = modelValue || viewValue;
      if (value) {
        var promise = validator.validate(value, options);
        promise.then(function (valid) {
          if (valid) {
            deferred.resolve();
          }
          else {
            deferred.reject();
          }
        }, deferred.reject);
      }
      else {
        deferred.resolve();
      }
      return deferred.promise;
    }
  }

  /**
   * Gets validator with the given name.
   */
  function getValidator(validatorName, registry) {
    if (!validatorName || validatorName == "") {
      return;
    }
    var validator = getGraniteUIFoundationValidator(validatorName, registry);
    if (!validator) {
      validator = getCustomValidator(validatorName, registry); 
    }
    if (!validator) {
      console.log(`caconfigValidation: Validator '${validatorName}' not found.`)
    }
    return validator;
  }

  /**
   * Get a Granite UI foundation validator. They are built to operate directly on DOM elements, so we
   * have to mock that a bit to just validate actual values. The validator implementat that is
   * returned is a wrapper that implements the same interface as custom validation methods.
   */
  function getGraniteUIFoundationValidator(validatorName, registry) {
    // build mocked DOM element with data-foundation-validation property to match selectors against
    var mockElement = document.createElement("div")
    mockElement.setAttribute("data-foundation-validation", validatorName);

    // find matching validator from registry
    var validator = registry.get("foundation.validation.validator")
        .filter(item => item.selector != undefined && item.validate != undefined)
        .find(item => mockElement.matches(item.selector));
    if (validator) {
      // return a wrapper around the GraniteUI validator that behaves like a custom validation method
      return {
        name: validatorName,
        async: false,
        validate: function (value) {
          var mockInputElement = document.createElement("input");
          mockInputElement.value = value
          // GraniteUI validators return a validation message in case of failure
          // but we cannot pass it over to AngularJS here, so just return true if no message was returned
          return validator.validate(mockInputElement) == undefined
        }
      };
    }
  }

  /**
   * Returns registered custom validator.
   */
  function getCustomValidator(validatorName, registry) {
    return registry.get("io.wcm.caconfig.editor.validator")
        .find(item => item.name == validatorName && item.validate != undefined);
  }

}(angular));
