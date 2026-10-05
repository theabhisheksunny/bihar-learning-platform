package com.biharlearning.storage;

import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Optional;

/**
 * The default, safest provider: it NEVER re-hosts a binary. It keeps metadata + the official
 * source URL only, so unverified or no-redistribution content is linked, not mirrored
 * (docs sections 5, 36). Active when STORAGE_PROVIDER=EXTERNAL (the default).
 */
@Component
public class ExternalUrlProvider implements FileStorageProvider {

    @Override
    public String key() { return "EXTERNAL"; }

    @Override
    public boolean canHost() { return false; }

    @Override
    public Optional<StoredObject> store(String path, InputStream data, long sizeBytes,
                                        String contentType, String checksum) {
        // Does not host. Caller falls back to EXTERNAL_REFERENCE using the official source URL.
        return Optional.empty();
    }

    @Override
    public Optional<String> urlFor(String path, boolean signed, int expirySeconds) {
        return Optional.empty();
    }

    @Override
    public Optional<InputStream> open(String path) {
        return Optional.empty();
    }

    @Override
    public boolean delete(String path) {
        return true; // nothing hosted to delete
    }

    @Override
    public Optional<Long> usedBytes() {
        return Optional.of(0L); // hosts nothing
    }
}
