## Field Validation

### Overview

By default, the Context-Aware Configuration Editor does not apply any validation on the data entered by the user, except making sure the data matches the selected data type (e.g. for integer parameters, only numeric input is allowed).

It is possible to define a validation for text-based input fields validating against predefined or custom rules.

_To use this, you need to deploy the optional dependency `io.wcm.sling.commons`._


### Configuring validation

Example for configuring a validation:


```java
@Property(label = "E-Mail", property = {
    "validation=wcmio.email",
    "validationMessage=Please enter a valid email address."
})
String email();
```

The validator referenced via the `validation` property can be:

* A Granite UI foundation validator registered in AEM that can also be used in edit dialogs
* A custom validator provided by the AEM project to synchronous or asynchronous validation

The `validationMessage` is displayed to the user when the validation detects an invalid value. It can be a text, or an i18n key looked up in the AEM i18n dictionaries.

Validation support is implemented for the following widget types:

* `textfield`
* `textarea`
* `pathbrowser`


### Validating with Granite UI foundation validators

AEM provides very few built-in validators to be used in edit dialogs, but they can be added via 3rd-party libraries like [wcm.io WCM Granite UI Extensions][wcmio-graniteui-extensions] or implemented within the AEM project ([example][graniteui-foundation-validator-example]).

The benefit of implementing and registering a validator in this way is that it can be used in both Context-Aware Configuration Editor and Edit dialog. But it is not possible to implement asynchronous validations, so this is not the best fit if the validation e.g. involves HTTP requests. Additionally the Context-Aware Configuration Editor cannot display the actual validation message returned by the validator, it always displays the validation message provided via the `validationMessage` property.

The client library that contains the validator has to be registered to the category `io.wcm.caconfig.editor.validation`.


### Validating with custom validator

Registering a custom validator provides most flexibility. Example for a simple synchronous validator:

```javascript
var registry = $(window).adaptTo("foundation-registry");

registry.register('io.wcm.caconfig.editor.validator', {
  name: 'my-custom-validator',
  validate: function(value) {
    // return true if validation was successful
    return /^[0-9a-f]+$/.test(value)
  }
});

```

If the validation involves calling AEM or other backends via HTTP, and asynchronous validator should be used. Example:

```javascript
registry.register('io.wcm.caconfig.editor.validator', {
  name: 'my-custom-async-validator',
  async: true,
  validate: function(value, options) {
    return new Promise((resolve, reject) => {
      // execute backend call
      // call resolve() if validation was successful, reject() if not or a communication error occurred
    });
  }
});
```

Two parameters are provided for the `validate` method:

* `value`: The value to validate
* `options`: Further context options. Contains currently only one property `contextPath` which contains the root page path of the context-aware configuration context. This can be used to make request to Sling Servlets bound to resources in context of the site/tenant where the editor is opened.

The client library that contains the validator has to be registered to the category `io.wcm.caconfig.editor.validation`.

See [examples][custom-validator-examples] for custom validators.



[wcmio-graniteui-extensions]: https://wcm.io/wcm/ui/granite/
[graniteui-foundation-validator-example]: https://github.com/wcm-io/io.wcm.wcm.ui.granite/blob/develop/src/main/webapp/app-root/clientlibs/io.wcm.ui.granite.validation/js/validation.js
[custom-validator-examples]: https://github.com/wcm-io/io.wcm.caconfig.sample-app/blob/develop/bundles/core/src/main/webapp/app-root/clientlibs/contextaware-config-sample-validation/js/sample-validation.js
