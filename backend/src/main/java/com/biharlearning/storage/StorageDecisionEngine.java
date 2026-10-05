package com.biharlearning.storage;

import com.biharlearning.entity.ContentSource;
import org.springframework.stereotype.Component;

/**
 * Decides what to do with a discovered PDF (docs section 5). The default is conservative:
 * if hosting permission is not clearly GRANTED, we keep metadata + the official URL and do NOT
 * re-host (EXTERNAL_REFERENCE). We never mirror on the assumption that "public == re-hostable"
 * (docs section 36).
 */
@Component
public class StorageDecisionEngine {

    public enum Decision { HOST, EXTERNAL_REFERENCE, REJECT, PENDING_REVIEW }

    public record Outcome(Decision decision, String reason) {}

    /**
     * @param source       the content source (may be null)
     * @param canHost      whether the active storage provider can host at all
     * @param licenseKnown whether we have a concrete license/permission for this document
     * @param permission   GRANTED / DENIED / UNKNOWN
     */
    public Outcome decide(ContentSource source, boolean canHost, boolean licenseKnown, String permission) {
        if ("DENIED".equalsIgnoreCase(permission)) {
            return new Outcome(Decision.EXTERNAL_REFERENCE, "Redistribution denied - link to official source only");
        }
        if (!canHost) {
            return new Outcome(Decision.EXTERNAL_REFERENCE, "Active storage provider cannot host - external reference");
        }
        if ("GRANTED".equalsIgnoreCase(permission)) {
            return new Outcome(Decision.HOST, "Hosting permission granted for this document");
        }
        // UNKNOWN permission: do not auto-mirror. Hold for admin review (docs 36).
        return new Outcome(Decision.PENDING_REVIEW,
                "Hosting permission UNKNOWN - keep metadata + official URL pending operator verification");
    }
}
