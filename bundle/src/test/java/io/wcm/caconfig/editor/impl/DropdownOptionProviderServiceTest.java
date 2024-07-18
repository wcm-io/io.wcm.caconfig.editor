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
package io.wcm.caconfig.editor.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mock.Strictness;
import org.mockito.junit.jupiter.MockitoExtension;

import io.wcm.caconfig.editor.DropdownOptionItem;
import io.wcm.caconfig.editor.DropdownOptionProvider;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
@ExtendWith(MockitoExtension.class)
class DropdownOptionProviderServiceTest {

  private final AemContext context = new AemContext();

  @Mock
  private Resource resource;
  @Mock(strictness = Strictness.LENIENT)
  private DropdownOptionProvider provider1;
  @Mock(strictness = Strictness.LENIENT)
  private DropdownOptionProvider provider2;

  private DropdownOptionProviderService underTest;

  @BeforeEach
  @SuppressWarnings("null")
  void setUp() {
    when(provider1.getDropdownOptions(resource)).thenReturn(List.of(
        new DropdownOptionItem("v1", "desc1"),
        new DropdownOptionItem("v2", "desc2")));
    context.registerService(DropdownOptionProvider.class, provider1, DropdownOptionProvider.PROPERTY_SELECTOR, "provider1");
    when(provider2.getDropdownOptions(resource)).thenReturn(List.of(
        new DropdownOptionItem("v3", "desc3")));
    context.registerService(DropdownOptionProvider.class, provider2, DropdownOptionProvider.PROPERTY_SELECTOR, "provider2");

    underTest = context.registerInjectActivateService(DropdownOptionProviderService.class);
  }

  @Test
  void testProvider1() {
    assertEquals(List.of(
        Map.of("value", "v1", "description", "desc1"),
        Map.of("value", "v2", "description", "desc2")),
        underTest.getDropdownOptions("provider1", resource));
  }

  @Test
  void testProvider2() {
    assertEquals(List.of(
        Map.of("value", "v3", "description", "desc3")),
        underTest.getDropdownOptions("provider2", resource));
  }

  @Test
  void testNonExistingProvider() {
    assertEquals(Collections.emptyList(), underTest.getDropdownOptions("provider_unknown", resource));
  }

}
