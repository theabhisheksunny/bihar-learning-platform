package com.biharlearning.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

/**
 * The single entry point the rest of the application uses for object storage. It selects the
 * active {@link FileStorageProvider} by configuration (STORAGE_PROVIDER) and exposes a provider-
 * agnostic API. Business logic (PdfService, DocumentService, sync) depends on THIS, never on a
 * concrete provider (docs sections 2, 34, 44).
 */
@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    private final FileStorageProvider provider;
    private final String bucket;

    public FileStorageService(List<FileStorageProvider> providers,
                              @Value("${app.storage.provider:EXTERNAL}") String providerKey,
                              @Value("${app.storage.bucket:documents}") String bucket) {
        this.bucket = bucket;
        FileStorageProvider selected = providers.stream()
                .filter(p -> p.key().equalsIgnoreCase(providerKey))
                .findFirst()
                .orElseGet(() -> providers.stream()
                        .filter(p -> p.key().equals("EXTERNAL")).findFirst()
                        .orElseThrow(() -> new IllegalStateException("No storage provider available")));
        this.provider = selected;
        log.info("FileStorageService using provider '{}' (canHost={})", selected.key(), selected.canHost());
    }

    public String activeProvider() { return provider.key(); }

    public boolean canHost() { return provider.canHost(); }

    public String bucket() { return bucket; }

    public Optional<StoredObject> store(String path, InputStream data, long sizeBytes,
                                        String contentType, String checksum) {
        return provider.store(path, data, sizeBytes, contentType, checksum);
    }

    public Optional<String> publicUrl(String path) {
        return provider.urlFor(path, false, 0);
    }

    public Optional<String> signedUrl(String path, int expirySeconds) {
        return provider.urlFor(path, true, expirySeconds);
    }

    public Optional<InputStream> open(String path) {
        return provider.open(path);
    }

    public boolean delete(String path) {
        return provider.delete(path);
    }

    public Optional<Long> providerUsedBytes() {
        return provider.usedBytes();
    }
}
