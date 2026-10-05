package com.biharlearning.entity;

/** Provenance verification state of a content item. */
public enum VerificationStatus {
    OFFICIAL,
    VERIFIED,
    THIRD_PARTY,
    AI_GENERATED,
    PENDING_REVIEW,
    OUTDATED
}
