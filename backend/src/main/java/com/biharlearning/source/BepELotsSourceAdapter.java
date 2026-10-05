package com.biharlearning.source;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for BEP e-LOTS (bepclots.bihar.gov.in), the Bihar Education Department portal.
 * Research confirmed per-subject pages such as /class-ix-maths/ with chapter PDF links under
 * WordPress upload paths. This adapter reads a known subject page and extracts chapter PDF
 * anchors. It performs NO authentication bypass and relies on PoliteHttpClient throttling.
 *
 * NOTE: run only after an operator confirms robots.txt/terms and enables the source.
 */
@Component("bepELotsSourceAdapter")
public class BepELotsSourceAdapter implements SourceAdapter {

    private static final Logger log = LoggerFactory.getLogger(BepELotsSourceAdapter.class);
    private static final String BASE = "https://bepclots.bihar.gov.in";

    private final PoliteHttpClient http;

    public BepELotsSourceAdapter(PoliteHttpClient http) {
        this.http = http;
    }

    @Override
    public String sourceKey() { return "BEP_ELOTS"; }

    @Override
    public List<SourceContentDescriptor> discover(String classCode) {
        List<SourceContentDescriptor> out = new ArrayList<>();
        if (!"9".equals(classCode)) {
            // Only Class IX wired for MVP. Other classes return empty rather than guessing URLs.
            return out;
        }
        // Known, confirmed Class IX subject page.
        String mathsPage = BASE + "/class-ix-maths/";
        out.add(new SourceContentDescriptor("BOOK", mathsPage, "Mathematics", "mathematics",
                null, mathsPage, null, null, null, null));

        Document doc = http.get(mathsPage);
        if (doc == null) {
            log.warn("BEP e-LOTS maths page unreachable (gov portal may be blocking automated fetch).");
            return out; // graceful: book descriptor still returned with the real page URL
        }
        Elements links = doc.select("a[href$=.pdf]");
        int n = 1;
        for (Element a : links) {
            String href = a.absUrl("href");
            String title = a.text().isBlank() ? ("Chapter " + n) : a.text().trim();
            out.add(new SourceContentDescriptor("CHAPTER", href, title, "mathematics",
                    n, mathsPage, href, null, null, null));
            n++;
        }
        log.info("BEP e-LOTS discovered {} chapter PDFs for Class IX Mathematics", n - 1);
        return out;
    }
}
