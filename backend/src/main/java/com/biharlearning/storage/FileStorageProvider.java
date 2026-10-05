package com.biharlearning.storage;

import java.io.InputStream;
import java.util.Optional;

/**
 * Storage provider SPI. The business logic depends on {@link FileStorageService}, never on a
 * concrete provider, so migrating to S3 / R2 / GCS later is a configuration change
 * (STORAGE_PROVIDER=...) rather than an application rewrite (docs sections 2, 34, 44).
 *
 * Implementations must stream rather than buffer whole files into memory (docs section 11).
 */
public interface FileStorageProvider {

    /** Stable key, e.g. "SUPABASE" or "EXTERNAL". Matches STORAGE_PROVIDER config. */
    String key();

    /** Whether this provider can actually host binaries (EXTERNAL cannot). */
    boolean canHost();

    /**
     * Store a stream at the given logical path. The checksum is computed by the caller and passed
     * for integrity + dedup; providers should persist it (e.g. as object metadata) where possible.
     * @return the stored object reference, or empty if this provider does not host.
     */
    Optional<StoredObject> store(String path, InputStream data, long sizeBytes,
                                 String contentType, String checksum);

    /**
     * A URL the browser can use to fetch the object directly (CDN/public or time-limited signed),
     * so Spring Boot does not have to proxy distributable PDFs (docs section 12). Empty when the
     * provider cannot produce one (e.g. EXTERNAL references, which use the official source URL).
     */
    Optional<String> urlFor(String path, boolean signed, int expirySeconds);

    /** Open a stream to the object for backend proxying when a direct URL is not appropriate. */
    Optional<InputStream> open(String path);

    /** Delete an object. Returns true if the provider performed (or no-oped) the deletion safely. */
    boolean delete(String path);

    /** Best-effort total bytes used by this provider, or empty if not programmatically known. */
    Optional<Long> usedBytes();
}
