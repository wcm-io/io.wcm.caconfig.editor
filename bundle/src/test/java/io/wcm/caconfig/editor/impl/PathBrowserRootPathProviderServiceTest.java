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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mock.Strictness;
import org.mockito.junit.jupiter.MockitoExtension;

import io.wcm.caconfig.editor.PathBrowserRootPathProvider;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
@ExtendWith(MockitoExtension.class)
class PathBrowserRootPathProviderServiceTest {

  private final AemContext context = new AemContext();

  @Mock
  private Resource resource;
  @Mock(strictness = Strictness.LENIENT)
  private PathBrowserRootPathProvider provider1;
  @Mock(strictness = Strictness.LENIENT)
  private PathBrowserRootPathProvider provider2;

  private PathBrowserRootPathProviderService underTest;

  @BeforeEach
  void setUp() throws Exception {
    when(provider1.getRootPath(resource)).thenReturn("/content/root1");
    context.registerService(PathBrowserRootPathProvider.class, provider1, PathBrowserRootPathProvider.PROPERTY_SELECTOR, "provider1");
    when(provider2.getRootPath(resource)).thenReturn("/content/root2");
    context.registerService(PathBrowserRootPathProvider.class, provider2, PathBrowserRootPathProvider.PROPERTY_SELECTOR, "provider2");

    underTest = context.registerInjectActivateService(PathBrowserRootPathProviderService.class);
  }

  @Test
  void testProvider1() {
    assertEquals("/content/root1", underTest.getRootPath("provider1", resource));
  }

  @Test
  void testProvider2() {
    assertEquals("/content/root2", underTest.getRootPath("provider2", resource));
  }

  @Test
  void testNonExistingProvider() {
    assertNull(underTest.getRootPath("provider_unknown", resource));
  }

}