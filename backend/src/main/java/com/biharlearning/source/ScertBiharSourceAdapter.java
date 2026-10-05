package com.biharlearning.source;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adapter for SCERT Bihar (scert.bihar.gov.in). Research confirmed a Class 9 e-resource PDF at
 * /public/uploads/eresources/CLASS_9_WORLD_OF_HISTORY.pdf. A full crawlable catalogue was not
 * verified, so this adapter returns only the confirmed document and leaves broader discovery for
 * when an operator confirms a listing page. No fabricated URLs.
 */
@Component("scertBiharSourceAdapter")
public class ScertBiharSourceAdapter implements SourceAdapter {

    private static final String DOC =
            "https://scert.bihar.gov.in/public/uploads/eresources/CLASS_9_WORLD_OF_HISTORY.pdf";

    @Override
    public String sourceKey() { return "SCERT"; }

    @Override
    public List<SourceContentDescriptor> discover(String classCode) {
        if (!"9".equals(classCode)) return List.of();
        return List.of(new SourceContentDescriptor(
                "BOOK", DOC, "Social Science: World of History", "social-science",
                null, "https://scert.bihar.gov.in", DOC, null, null, null));
    }
}
