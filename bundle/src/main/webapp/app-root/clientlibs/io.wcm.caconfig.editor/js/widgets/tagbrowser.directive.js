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
      var multiValue = scope.property.metadata.multivalue;

      if(multiValue) {
        scope.effectiveValues = [];
        scope.values = [];

        setValueArray(scope.property.effectiveValue, scope.effectiveValues);
        setValueArray(scope.property.value, scope.values);
      }

      var prefix = directivePropertyPrefixes.tagbrowser;
      var props = scope.property.metadata.properties;

      var tagfieldName = "tags-" + Math.floor(Math.random() * 100000);

      var options = {};
      var tagfieldWidget;
      var suggestionOverlay;
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
        tagfieldWidget = element.find("foundation-autocomplete")[0];
        suggestionOverlay = element.find("coral-overlay[foundation-autocomplete-suggestion]")[0];
        var taglist = element.find("coral-taglist")[0];
        Coral.commons.ready(tagfieldWidget, function() {
          var selectionCount = "single"
          if(multiValue) {
            selectionCount = "multiple";
          }
          tagfieldWidget.setAttribute("pickersrc", tagbrowserService.getPickerSrc(options.rootPath, selectionCount));
          tagfieldWidget.setAttribute("name", tagfieldName);
          taglist.setAttribute("name", tagfieldName);
          suggestionOverlay.setAttribute("data-foundation-picker-buttonlist-src", tagbrowserService.getSuggestionSrc(options.rootPath));

          let changedOffset = angular.isArray(scope.property.effectiveValue) ? scope.property.effectiveValue.length : 1;
          let changedCount = 0;
          // Add change event listen
          $(taglist).on("coral-collection:add", function onAdd(event) {
            scope.property.value = taglist.items.getAll().map(item => item.value);
            if(!multiValue) {
              if(!scope.property || !scope.property.value || !angular.isArray(scope.property.value)) {
                scope.property.value = null;
              } else if(scope.property.value.length > 0) {
                if(scope.property.value.length > 1) {
                  var tagNode = element.find("coral-tag")[0];
                  tagNode.parentElement.removeChild(tagNode);
                }
                scope.property.value = taglist.items.getAll().map(item => item.value)[0];
              }
            }
            changedCount++;

            let hasChanged = (changedCount > changedOffset) && !compare(scope.property.effectiveValue, scope.property.value);

            if ($rootScope.configForm.$pristine && hasChanged) {
              $rootScope.configForm.$setDirty();
              scope.$digest();
            }
          });
          
          $(taglist).on("coral-collection:remove", function onAdd(event) {
            if(multiValue) {
              scope.property.value = taglist.items.getAll().map(item => item.value);
            } else {
              scope.property.value = null;
            }

            let hasChanged = !compare(scope.property.effectiveValue, scope.property.value);

            if ($rootScope.configForm.$pristine && hasChanged) {
              $rootScope.configForm.$setDirty();
              scope.$digest();
            }
          });

          if(multiValue) {
            for (let i = 0; i < scope.values.length; i++) {
              addTagToList(scope.values[i].value, options.rootPath, taglist)
            }
          } else {
            if(scope.property.value) addTagToList(scope.property.value, options.rootPath, taglist)
          }

        });
      });
    }

    function compare(base, other) {
      if(!base && !other) return true;
      if(base && !other) return false;
      if(!base && other) return false;
      if(angular.isArray(base) && !angular.isArray(other)) return false;
      if(angular.isArray(other) && !angular.isArray(base)) return false;
      if(angular.isArray(other) && angular.isArray(base)) {
        return base.length === other.length && base.every(function(value, index) { return value === other[index]});
      } else {
        return base === other;
      }
    }

    //there might be a better way to fetch the tag labels
    function addTagToList(tagName, rootPath, tagList) {
      let fullTagLabel = "";
      try {
        const jcrTitleJSONSuffix = "/jcr:title.json";

        let tagPath = tagName.replace(":", "/"); //tag-name can look like this: Main:sub/tag
        let tagUrl = rootPath;
        tagPath.split("/").filter(part => Boolean(part)).forEach(function (tagPart) {
          tagUrl += "/" + tagPart;
          let partUrl = tagUrl + jcrTitleJSONSuffix;
          $.ajax({
            url: partUrl,
            type: "GET",
            async: false, //force async=false to create the entire tag-path in order
          })
              .done(function (data) {
                if (fullTagLabel) {
                  fullTagLabel += " / ";
                }
                fullTagLabel += data["jcr:title"];
              });
        });
        if(fullTagLabel) {
          fullTagLabel = fullTagLabel.replace(" / ", " : ");
        } else {
          fullTagLabel = tagName.replace(":", "")
        }
      } catch(error) {
        fullTagLabel = tagName.replace(":", "")
      } finally {
        let coralTag = new Coral.Tag().set({
          value: tagName,
          label: {
            innerHTML: fullTagLabel
          }
        });
        tagList.items.add(coralTag);
      }
    }

    function setValueArray(src, target) {
      var i,
          tempArray;
      if (src && src.length > 0) {
        tempArray = src;
        for (i = 0; i < tempArray.length; i++) {
          target.push({value: tempArray[i]});
        }
      }
      else {
        target = [];
      }
    }

  }
}(angular, Coral, Granite.$));
