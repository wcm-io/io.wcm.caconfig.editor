/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2016 wcm.io
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

  var STORAGE_OVERVIEW_CATEGORYFILTER = "caconfig-overview-categoryFilter";

  angular.module("io.wcm.caconfig.editor")
    .controller("OverviewController", OverviewController);

  OverviewController.$inject = ["$document", "$window", "$rootScope", "$timeout", "configService", "modalService", "publishService"];

  function OverviewController($document, $window, $rootScope, $timeout, configService, modalService, publishService) {
    var that = this;

    $rootScope.title = $rootScope.i18n("title");
    that.state = configService.getState();
    configService.loadConfigNames()
      .then(function() {
        that.ovReady = true;

        // get last selected category filter
        var lastCategoryFilter = $window.localStorage.getItem(STORAGE_OVERVIEW_CATEGORYFILTER);
        if (!that.state.configCategories.find(item => item.category == lastCategoryFilter)) {
          lastCategoryFilter = undefined;
        }
        $rootScope.categoryFilter = lastCategoryFilter;

        // ugly block of code to register event of coral-select and propagate it's value to AngularJS change
        // the coral-select widget itself lives outside AngularJS' controls
        $timeout(function () {
          var select = $document.find("#caconfig-configurationOverviewFilter").get(0);
          if (select) {
            Coral.commons.ready(select, function(component) {
              $(component).on('change', function() {
                $rootScope.categoryFilter = component.value;
                $window.localStorage.setItem(STORAGE_OVERVIEW_CATEGORYFILTER, component.value);
                $rootScope.$apply();
              });
            });
          }
        });
      });

    that.hasNonExistingConfig = function () {
      var i;

      if (!that.state.configNames) {
        return false;
      }

      for (i = 0; i < that.state.configNames.length; i++) {
        if (!that.state.configNames[i].exists) {
          return true;
        }
      }
      return false;
    };

    that.showNonExistingConfigs = function () {
      modalService.triggerEvent(modalService.modal.ADD_CONFIG, "caconfig-setup");
      modalService.show(modalService.modal.ADD_CONFIG);
    };

    that.quickPublish = function () {
      publishService.quickPublish();
    };

    that.managePublication = function () {
      publishService.managePublication();
    };

    // custom filter for category listing.
    // hide configurations that do not exist, and those not matching the current category filter
    $rootScope.configNamesFilter = function(categoryFilter) {
      return function(configName) {
        if (configName.exists != true) {
          return false;
        }
        if (categoryFilter) {
          return configName.category == categoryFilter;
        }
        return true; 
      }
    };

  }
}(angular, Coral, Granite.$));
