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
package io.wcm.caconfig.editor.impl;

import java.util.Collection;

import org.apache.sling.api.resource.Resource;
import org.jetbrains.annotations.NotNull;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.FieldOption;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import io.wcm.caconfig.editor.ConfigurationEditorFilter;
import io.wcm.sling.commons.caservice.ContextAwareServiceCollectionResolver;
import io.wcm.sling.commons.caservice.ContextAwareServiceResolver;

/**
 * Aggregates configuration filters via Context-Aware services.
 */
@Component(service = ConfigurationEditorFilterService.class)
public class ConfigurationEditorFilterService {

  @Reference(cardinality = ReferenceCardinality.MULTIPLE, fieldOption = FieldOption.UPDATE,
      policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
  private Collection<ServiceReference<ConfigurationEditorFilter>> filters;

  @Reference
  private ContextAwareServiceResolver contextAwareServiceResolver;
  private ContextAwareServiceCollectionResolver<ConfigurationEditorFilter, Void> contextAwareServiceCollectionResolver;

  @Activate
  private void activate() {
    this.contextAwareServiceCollectionResolver = contextAwareServiceResolver.getCollectionResolver(this.filters);
  }

  /**
   * Allow to add configurations with this name in the configuration editor.
   * @param contextResource Content resource
   * @param configName Configuration name
   * @return if true, the configuration is offered in the "add configuration" dialog
   */
  public boolean allowAdd(@NotNull Resource contextResource, @NotNull String configName) {
    return contextAwareServiceCollectionResolver.resolveAll(contextResource)
        .filter(filter -> !filter.allowAdd(configName))
        .count() == 0;
  }

}
