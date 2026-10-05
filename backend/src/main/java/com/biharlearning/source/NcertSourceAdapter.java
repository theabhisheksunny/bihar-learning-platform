package com.biharlearning.source;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adapter for NCERT (ncert.nic.in). The public catalogue is a server-rendered page at
 * /textbook.php and books use codes (Class 9 Maths = iemh1, chapters iemh101..). The exact live
 * download handler has changed over time and could NOT be verified in research, so this adapter
 * does NOT fabricate a download URL. It returns the confirmed catalogue page as the official
 * source; an operator confirms the live template before download/proxy is enabled.
 */
@Component("ncertSourceAdapter")
public class NcertSourceAdapter implements SourceAdapter {

    private static final String CATALOGUE = "https://ncert.nic.in/textbook.php";

    @Override
    public String sourceKey() { return "NCERT"; }

    @Override
    public List<SourceContentDescriptor> discover(String classCode) {
        if (!"9".equals(classCode)) return List.of();
        // Confirmed book code for Class 9 Maths. documentUrl left null (live template unverified).
        return List.of(new SourceContentDescriptor(
                "BOOK", "iemh1", "Mathematics", "mathematics",
                null, CATALOGUE, null, null, "CC BY-NC-ND 4.0", null));
    }
}
