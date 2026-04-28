# Copilot Instructions for io.wcm.caconfig.editor

## Project Purpose

This project provides a context-aware configuration editor for AEM.

- The backend is implemented in Java as OSGi services and Sling servlets.
- The frontend is implemented primarily with AngularJS 1.x.
- The UI is embedded into the AEM author/admin experience and must fit into AEM's CoralUI and Granite styling.
- The frontend is not a pure CoralUI app and not a generic AngularJS app. CoralUI provides visual structure and selected widget behavior, while AngularJS owns most field behavior, data binding, validation, and user interaction.

When generating code, preserve this hybrid architecture instead of trying to rewrite existing functionality toward modern frontend frameworks or pure Coral widget implementations.

## Repository Layout

- `bundle/src/main/java/io/wcm/caconfig/editor`: Java API, models, OSGi services, Sling servlets, and response generation.
- `bundle/src/test/java`: backend unit tests.
- `bundle/src/main/resources/angularjs-partials`: AngularJS HTML partials used as directive templates and compiled into the frontend template module.
- `bundle/src/main/webapp/app-root/clientlibs/io.wcm.caconfig.editor/js`: application-specific AngularJS modules, controllers, services, directives, and modal logic.
- `bundle/src/main/webapp/app-root/clientlibs/io.wcm.caconfig.editor/css`: editor styling and Coral helper CSS.
- `bundle/src/main/webapp/app-root/clientlibs/io.wcm.caconfig.editor.angularjs`: vendored AngularJS runtime libraries.
- `package/jcr_root`: AEM package content.

## Frontend Rules

- Treat AngularJS 1.x as the primary frontend implementation model.
- Keep using the existing AngularJS module, controller, service, and directive patterns already present in the clientlibs.
- Keep templates in `bundle/src/main/resources/angularjs-partials` and the related directive or controller logic in the AEM clientlib JavaScript.
- If a UI change affects a template in `angularjs-partials`, also check whether the corresponding directive or controller in the clientlib must be updated.
- Remember that Grunt compiles the HTML partials into `templates.module.js`. Do not hand-edit generated output unless the repository already expects it.
- Reuse existing clientlib modules and naming conventions such as `*.module.js`, `*.service.js`, `*.controller.js`, and `*.directive.js`.
- Prefer the existing AngularJS dependency injection style using explicit `$inject` arrays.

## CoralUI and AEM Integration Rules

- Preserve AEM and CoralUI compatibility first.
- Reuse existing Coral markup, CSS classes, and widget initialization patterns instead of replacing them with custom HTML controls.
- When changing field widgets, account for both Coral widget state and AngularJS model state.
- Be careful with dynamic Coral widgets that create or wrap DOM elements at runtime. Existing code may need manual event binding, delayed initialization, or `$compile` integration to keep AngularJS validation and model binding working.
- Do not assume Granite or Coral validation is sufficient. Check how AngularJS forms, `ng-model`, `ng-required`, error labels, and dirty/pristine handling are implemented before changing validation behavior.
- Match the existing admin UI look and behavior. Avoid introducing styling or interaction patterns that feel separate from AEM.

## Backend Rules

- Follow existing Java, Sling, and OSGi patterns in `bundle/src/main/java`.
- Keep Sling servlet resource types, selectors, and extension mappings consistent with existing code.
- Prefer extending existing response models and services over adding parallel ad hoc JSON generation.
- Keep logic that derives editor metadata, dropdown options, browser roots, validation metadata, or persisted configuration behavior in the backend where similar functionality already lives.
- Add or update backend tests for behavioral changes whenever practical.

## Change Strategy

- Make minimal, targeted changes that fit the current architecture.
- Do not migrate AngularJS code to React, Vue, or newer Angular.
- Do not replace clientlibs, CoralUI integration, or AEM packaging conventions with modern bundlers unless explicitly requested.
- Avoid broad refactors unless the task requires them.
- For frontend features, check whether the change spans all of these layers:
  - backend property metadata or JSON output
  - AngularJS directive or controller behavior
  - AngularJS partial template markup
  - CSS for Coral-aligned rendering

## Validation and Build Expectations

Before finishing a change, use the smallest relevant validation available:

- Backend changes: run Maven tests relevant to the bundle module when feasible.
- Frontend JavaScript changes: run the Grunt or ESLint tasks in `bundle` when feasible.
- Template changes: regenerate compiled AngularJS templates with the Grunt build.

Useful commands:

- `mvn clean install`
- `mvn -pl bundle test`
- in `bundle`: `npm install`
- in `bundle`: `grunt build`
- in `bundle`: `grunt lint:js`

## Implementation Guidance for Copilot

- When asked to implement a field or validation change, inspect both the backend metadata source and the frontend directive/template pair before editing.
- When asked to change dropdowns, path browsers, tag browsers, or multifields, expect custom integration code that bridges Coral widgets and AngularJS forms.
- When adding validation, ensure the UI state, AngularJS form state, and persisted value semantics stay aligned.
- When generating examples or fixes, prefer patterns already used in this repository over generic AEM or AngularJS examples.
- Keep generated code compatible with the repository's existing Java version, Maven build, AngularJS style, and clientlib packaging.