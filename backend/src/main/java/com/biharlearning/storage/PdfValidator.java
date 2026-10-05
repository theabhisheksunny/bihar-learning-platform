package com.biharlearning.storage;

import org.springframework.stereotype.Component;

/**
 * Validates a candidate PDF before storage (docs section 26). Rejects HTML error pages disguised
 * as .pdf, wrong content types, and files that don't start with the %PDF- signature.
 */
@Component
public class PdfValidator {

    public record Result(boolean valid, String reason) {
        static Result ok() { return new Result(true, null); }
        static Result bad(String r) { return new Result(false, r); }
    }

    /**
     * @param contentType HTTP Content-Type header
     * @param head        first bytes of the body (enough to see the signature)
     * @param declaredSize Content-Length if known, else -1
     * @param maxBytes    max hostable size
     */
    public Result validate(String contentType, byte[] head, long declaredSize, long maxBytes) {
        if (contentType != null) {
            String ct = contentType.toLowerCase();
            if (ct.contains("text/html")) {
                return Result.bad("Received text/html (likely an error page), expected application/pdf");
            }
        }
        if (head == null || head.length < 5) {
            return Result.bad("Empty or too-small response body");
        }
        // PDF files begin with "%PDF-".
        boolean signature = head[0] == '%' && head[1] == 'P' && head[2] == 'D' && head[3] == 'F' && head[4] == '-';
        if (!signature) {
            return Result.bad("Missing %PDF- signature; not a valid PDF");
        }
        if (declaredSize > 0 && maxBytes > 0 && declaredSize > maxBytes) {
            return Result.bad("File exceeds max hostable size (" + declaredSize + " > " + maxBytes + ")");
        }
        return Result.ok();
    }
}
