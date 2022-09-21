/**
 * Register custom validator to form fields inside caconfig editor
 * to effectively suppress the default validation handling (e.g. for required fields)
 * which otherwise would conflict with our AngularJS-based validation handling.
 */
(function(window, $) {
  var registry = $(window).adaptTo("foundation-registry");
  registry.register("foundation.validation.validator", {
    selector: ".caconfig-detail input",
    show: function() {
      // ignore
    },
    clear: function() {
      // ignore
    }
  });
    
})(window, Granite.$);
