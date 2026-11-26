package com.wanhui.security.crypto;

import javax.crypto.Mac;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class FieldHmacService {
    private final KeyVersionProvider keyProvider;

    public FieldHmacService(KeyVersionProvider keyProvider) {
        this.keyProvider = keyProvider;
    }

    public byte[] hmac(String value) {
        if (value == null) {
            throw new IllegalArgumentException("value must not be null");
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(keyProvider.hmacKey(keyProvider.currentVersion()));
            return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
        } catch (CryptoException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new CryptoException("Unable to calculate HMAC", exception);
        }
    }

    public String normalizeUsername(String username) {
        return username == null ? null : username.trim().toLowerCase(Locale.ROOT);
    }

    public String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    public int currentVersion() {
        return keyProvider.currentVersion();
    }
}
