package com.biharlearning.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * PostgreSQL full-text search across books, chapters and questions using the
 * 'simple' config + unaccent (so Hindi/Urdu scripts and diacritics behave reasonably).
 * Architected to be swappable for OpenSearch later (see architecture.md).
 */
@Repository
public class SearchRepository {

    @PersistenceContext
    private EntityManager em;

    public record SearchHit(String type, String id, String title, String snippet, double rank) {}

    @SuppressWarnings("unchecked")
    public List<SearchHit> search(String q, String typeFilter, int limit, int offset) {
        List<SearchHit> hits = new ArrayList<>();
        if (q == null || q.isBlank()) return hits;

        boolean wantBooks = typeFilter == null || typeFilter.isBlank() || typeFilter.equalsIgnoreCase("book");
        boolean wantChapters = typeFilter == null || typeFilter.isBlank() || typeFilter.equalsIgnoreCase("chapter");
        boolean wantQuestions = typeFilter == null || typeFilter.isBlank() || typeFilter.equalsIgnoreCase("question");

        if (wantBooks) {
            Query query = em.createNativeQuery("""
                SELECT 'book' AS type, b.id::text, b.title,
                       left(coalesce(b.description,''), 180) AS snippet,
                       ts_rank(to_tsvector('simple', f_unaccent(coalesce(b.title,'')||' '||coalesce(b.description,''))),
                               websearch_to_tsquery('simple', f_unaccent(:q))) AS rank
                FROM books b
                WHERE to_tsvector('simple', f_unaccent(coalesce(b.title,'')||' '||coalesce(b.description,'')))
                      @@ websearch_to_tsquery('simple', f_unaccent(:q))
                ORDER BY rank DESC
                LIMIT :lim OFFSET :off
                """);
            query.setParameter("q", q);
            query.setParameter("lim", limit);
            query.setParameter("off", offset);
            addRows(hits, query.getResultList());
        }
        if (wantChapters) {
            Query query = em.createNativeQuery("""
                SELECT 'chapter' AS type, c.id::text, c.title,
                       left(coalesce(c.description,''), 180) AS snippet,
                       ts_rank(to_tsvector('simple', f_unaccent(coalesce(c.title,'')||' '||coalesce(c.description,''))),
                               websearch_to_tsquery('simple', f_unaccent(:q))) AS rank
                FROM chapters c
                WHERE to_tsvector('simple', f_unaccent(coalesce(c.title,'')||' '||coalesce(c.description,'')))
                      @@ websearch_to_tsquery('simple', f_unaccent(:q))
                ORDER BY rank DESC
                LIMIT :lim OFFSET :off
                """);
            query.setParameter("q", q);
            query.setParameter("lim", limit);
            query.setParameter("off", offset);
            addRows(hits, query.getResultList());
        }
        if (wantQuestions) {
            Query query = em.createNativeQuery("""
                SELECT 'question' AS type, qn.id::text, left(qn.text, 80) AS title,
                       left(qn.text, 180) AS snippet,
                       ts_rank(to_tsvector('simple', f_unaccent(coalesce(qn.text,''))),
                               websearch_to_tsquery('simple', f_unaccent(:q))) AS rank
                FROM questions qn
                WHERE to_tsvector('simple', f_unaccent(coalesce(qn.text,'')))
                      @@ websearch_to_tsquery('simple', f_unaccent(:q))
                ORDER BY rank DESC
                LIMIT :lim OFFSET :off
                """);
            query.setParameter("q", q);
            query.setParameter("lim", limit);
            query.setParameter("off", offset);
            addRows(hits, query.getResultList());
        }
        hits.sort((a, b) -> Double.compare(b.rank(), a.rank()));
        return hits;
    }

    private void addRows(List<SearchHit> hits, List<Object[]> rows) {
        for (Object[] r : rows) {
            double rank = r[4] == null ? 0d : ((Number) r[4]).doubleValue();
            hits.add(new SearchHit((String) r[0], (String) r[1], (String) r[2], (String) r[3], rank));
        }
    }
}
