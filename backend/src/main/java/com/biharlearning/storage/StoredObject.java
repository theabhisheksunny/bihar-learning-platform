package com.biharlearning.storage;

/**
 * Result of a successful store operation. Holds only references/metadata - never the binary.
 * The binary lives in the object store; PostgreSQL keeps this reference (see docs section 44).
 */
public record StoredObject(
        String provider,     // e.g. SUPABASE
        String bucket,
        String path,         // storage path / object key (canonical identity is the checksum, not this)
        long sizeBytes,
        String checksum,     // SHA-256 hex
        String contentType
) {}
