package com.biharlearning.source;

import java.util.List;

/**
 * A pluggable adapter for one external content source. Implementations MUST:
 *  - respect robots.txt / terms (the caller only runs enabled + robots-confirmed sources),
 *  - send a descriptive User-Agent and rate-limit requests,
 *  - never bypass authentication, CAPTCHAs, paywalls, or DRM,
 *  - return normalized descriptors with null for anything not verified.
 */
public interface SourceAdapter {

    /** Stable key matching content_sources.source_key (e.g. "NCERT"). */
    String sourceKey();

    /**
     * Discover available content for a class.
     * @param classCode e.g. "9"
     * @return normalized descriptors (possibly empty). Must not throw for an empty source.
     */
    List<SourceContentDescriptor> discover(String classCode);
}
