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

import org.jetbrains.annotations.NotNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.granite.crypto.CryptoSupport;
import org.apache.sling.commons.crypto.CryptoService;

/**
 * Service for encrypting and decrypting configuration values.
 * Uses Adobe Granite's CryptoSupport if available, with fallback to Apache Sling's CryptoService.
 * Throws an exception if neither implementation is available.
 */
@Component(service = EncryptionService.class)
public class EncryptionService {

  @Reference(cardinality = ReferenceCardinality.OPTIONAL)
  CryptoSupport adobeCryptoSupport;

  @Reference(cardinality = ReferenceCardinality.OPTIONAL)
  CryptoService slingCryptoService;

  private static final Logger log = LoggerFactory.getLogger(EncryptionService.class);

  /**
   * Encrypts a value using available CryptoSupport implementation.
   * 
   * @param value the value to encrypt
   * @return encrypted value
   * @throws IllegalStateException if no CryptoSupport implementation is available
   */
  public @NotNull String encrypt(@NotNull String value) {
    if (adobeCryptoSupport != null) {
      try {
        return adobeCryptoSupport.protect(value);
      }
      catch (Exception ex) {
        log.error("Failed to encrypt value using Adobe Granite CryptoSupport", ex);
        throw new IllegalStateException("Encryption failed: " + ex.getMessage(), ex);
      }
    }
    else if (slingCryptoService != null) {
      try {
        return slingCryptoService.encrypt(value);
      }
      catch (Exception ex) {
        log.error("Failed to encrypt value using Sling CryptoService", ex);
        throw new IllegalStateException("Encryption failed: " + ex.getMessage(), ex);
      }
    }
    else {
      throw new IllegalStateException(
          "No CryptoSupport implementation available. Please install either Adobe Granite CryptoSupport or Apache Sling Commons Crypto.");
    }
  }

  /**
   * Decrypts a value using available CryptoSupport implementation.
   * 
   * @param value the encrypted value
   * @return decrypted value
   * @throws IllegalStateException if no CryptoSupport implementation is available
   */
  public @NotNull String decrypt(@NotNull String value) {
    if (adobeCryptoSupport != null) {
      try {
        return adobeCryptoSupport.unprotect(value);
      }
      catch (Exception ex) {
        log.error("Failed to decrypt value using Adobe Granite CryptoSupport", ex);
        throw new IllegalStateException("Decryption failed: " + ex.getMessage(), ex);
      }
    }
    else if (slingCryptoService != null) {
      try {
        return slingCryptoService.decrypt(value);
      }
      catch (Exception ex) {
        log.error("Failed to decrypt value using Sling CryptoService", ex);
        throw new IllegalStateException("Decryption failed: " + ex.getMessage(), ex);
      }
    }
    else {
      throw new IllegalStateException(
          "No CryptoSupport implementation available. Please install either Adobe Granite CryptoSupport or Apache Sling Commons Crypto.");
    }
  }

  /**
   * Checks if any encryption implementation is available.
   * 
   * @return true if either Adobe Granite or Sling CryptoSupport is available
   */
  public boolean isEncryptionAvailable() {
    return adobeCryptoSupport != null || slingCryptoService != null;
  }

}
