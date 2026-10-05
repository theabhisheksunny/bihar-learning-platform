package com.biharlearning.storage;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** SHA-256 helpers for content identity + deduplication (docs section 6). */
public final class Checksums {

    private Checksums() {}

    /** Compute SHA-256 of a byte array (hex). For small payloads/tests. */
    public static String sha256(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return toHex(md.digest(data));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Stream the input through a SHA-256 digest WITHOUT buffering the whole file in memory
     * (docs section 11). Returns the hex digest after fully consuming the stream.
     */
    public static String sha256Streaming(InputStream in) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] buf = new byte[8192];
            try (DigestInputStream dis = new DigestInputStream(in, md)) {
                while (dis.read(buf) != -1) { /* drain */ }
            }
            return toHex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(Character.forDigit((b >> 4) & 0xF, 16))
                               .append(Character.forDigit(b & 0xF, 16));
        return sb.toString();
    }
}
