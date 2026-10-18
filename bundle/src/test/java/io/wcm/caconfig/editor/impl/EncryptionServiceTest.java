/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2026 wcm.io
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.adobe.granite.crypto.CryptoSupport;
import org.apache.sling.commons.crypto.CryptoService;

@ExtendWith(MockitoExtension.class)
class EncryptionServiceTest {

  @Mock
  private CryptoSupport adobeCryptoSupport;

  @Mock
  private CryptoService slingCryptoService;

  private EncryptionService underTest;

  @BeforeEach
  void setUp() {
    underTest = new EncryptionService();
  }

  @Test
  void testEncryptionWithAdobeCryptoSupport() throws Exception {
    underTest.adobeCryptoSupport = adobeCryptoSupport;

    String plaintext = "myPassword";
    String encrypted = "encrypted_value_12345";

    when(adobeCryptoSupport.protect(plaintext)).thenReturn(encrypted);

    assertEquals(encrypted, underTest.encrypt(plaintext));
    assertTrue(underTest.isEncryptionAvailable());
  }

  @Test
  void testDecryptionWithAdobeCryptoSupport() throws Exception {
    underTest.adobeCryptoSupport = adobeCryptoSupport;

    String encrypted = "encrypted_value_12345";
    String plaintext = "myPassword";

    when(adobeCryptoSupport.unprotect(encrypted)).thenReturn(plaintext);

    assertEquals(plaintext, underTest.decrypt(encrypted));
  }

  @Test
  void testEncryptionWithSlingCryptoService() throws Exception {
    underTest.slingCryptoService = slingCryptoService;

    String plaintext = "myPassword";
    String encrypted = "encrypted_sling_12345";

    when(slingCryptoService.encrypt(plaintext)).thenReturn(encrypted);

    assertEquals(encrypted, underTest.encrypt(plaintext));
    assertTrue(underTest.isEncryptionAvailable());
  }

  @Test
  void testDecryptionWithSlingCryptoService() throws Exception {
    underTest.slingCryptoService = slingCryptoService;

    String encrypted = "encrypted_sling_12345";
    String plaintext = "myPassword";

    when(slingCryptoService.decrypt(encrypted)).thenReturn(plaintext);

    assertEquals(plaintext, underTest.decrypt(encrypted));
  }

  @Test
  void testEncryptionWithBothImplementations() throws Exception {
    // Adobe should be preferred
    underTest.adobeCryptoSupport = adobeCryptoSupport;
    underTest.slingCryptoService = slingCryptoService;

    String plaintext = "myPassword";
    String encrypted = "encrypted_adobe_12345";

    when(adobeCryptoSupport.protect(plaintext)).thenReturn(encrypted);

    assertEquals(encrypted, underTest.encrypt(plaintext));
  }

  @Test
  void testEncryptionWithoutCryptoSupport() {
    underTest.adobeCryptoSupport = null;
    underTest.slingCryptoService = null;

    String value = "myPassword";
    assertFalse(underTest.isEncryptionAvailable());

    assertThrows(IllegalStateException.class, () -> underTest.encrypt(value),
        "Should throw exception when no CryptoSupport is available");
  }

  @Test
  void testDecryptionWithoutCryptoSupport() {
    underTest.adobeCryptoSupport = null;
    underTest.slingCryptoService = null;

    String value = "myPassword";

    assertThrows(IllegalStateException.class, () -> underTest.decrypt(value),
        "Should throw exception when no CryptoSupport is available");
  }

  @Test
  void testEncryptionHandlesException() throws Exception {
    underTest.adobeCryptoSupport = adobeCryptoSupport;

    String plaintext = "myPassword";

    when(adobeCryptoSupport.protect(plaintext)).thenThrow(new RuntimeException("Encryption failed"));

    assertThrows(IllegalStateException.class, () -> underTest.encrypt(plaintext),
        "Should throw exception on encryption failure");
  }

  @Test
  void testDecryptionHandlesException() throws Exception {
    underTest.adobeCryptoSupport = adobeCryptoSupport;

    String encrypted = "encrypted_value_12345";

    when(adobeCryptoSupport.unprotect(encrypted)).thenThrow(new RuntimeException("Decryption failed"));

    assertThrows(IllegalStateException.class, () -> underTest.decrypt(encrypted),
        "Should throw exception on decryption failure");
  }

}

