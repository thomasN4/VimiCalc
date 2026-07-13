package vimicalc.controller;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Fuzzy matching of COMMAND-mode completion queries against command names.
 *
 * <p>Matches are grouped into three tiers, best first; within a tier,
 * candidates are ordered alphabetically. All comparisons are
 * case-insensitive:</p>
 *
 * <ul>
 *   <li><b>Tier 0 — prefix</b>: the candidate starts with the query
 *       ({@code res} → {@code resizeColumn}). The empty query is a prefix of
 *       everything, so it matches every candidate.</li>
 *   <li><b>Tier 1 — camelHump</b>: every query character lands on a word
 *       start — index 0, an uppercase character, or the position directly
 *       after the previous match ({@code rc} → {@code resizeColumn},
 *       {@code fW} → {@code fontWeight}).</li>
 *   <li><b>Tier 2 — scattered</b>: the query is a plain subsequence of the
 *       candidate ({@code res} → {@code gridlines}).</li>
 * </ul>
 *
 * <p>Matching is greedy left-to-right with no backtracking or per-character
 * scoring — with a candidate pool the size of the command set, tiers plus
 * alphabetical order are ranking enough.</p>
 */
final class FuzzyMatcher {
    private FuzzyMatcher() {}

    /**
     * Filters and ranks {@code candidates} against {@code query}.
     *
     * @param query      the text typed so far
     * @param candidates the names to match against
     * @return the matching candidates, best tier first, alphabetical within
     *         a tier; empty when nothing matches
     */
    static @NotNull List<String> rank(@NotNull String query, @NotNull List<String> candidates) {
        record Scored(String name, int tier) {}
        List<Scored> scored = new ArrayList<>();
        for (String candidate : candidates) {
            int tier = tier(query, candidate);
            if (tier >= 0) scored.add(new Scored(candidate, tier));
        }
        scored.sort(Comparator.comparingInt(Scored::tier).thenComparing(Scored::name));
        return scored.stream().map(Scored::name).toList();
    }

    /**
     * Classifies how well {@code query} matches {@code candidate}.
     *
     * @return {@code 0} (prefix), {@code 1} (camelHump), {@code 2}
     *         (scattered subsequence), or {@code -1} for no match
     */
    static int tier(@NotNull String query, @NotNull String candidate) {
        if (candidate.regionMatches(true, 0, query, 0, query.length())) return 0;
        if (humpMatch(query, candidate)) return 1;
        if (subsequenceMatch(query, candidate)) return 2;
        return -1;
    }

    /** Whether every query char lands at index 0, on an uppercase char, or right after the previous match. */
    private static boolean humpMatch(String query, String candidate) {
        int prev = -1;
        for (int q = 0; q < query.length(); q++) {
            char qc = Character.toLowerCase(query.charAt(q));
            int found = -1;
            for (int i = prev + 1; i < candidate.length(); i++) {
                char cc = candidate.charAt(i);
                if (Character.toLowerCase(cc) != qc) continue;
                if (i == 0 || Character.isUpperCase(cc) || i == prev + 1) {
                    found = i;
                    break;
                }
            }
            if (found < 0) return false;
            prev = found;
        }
        return true;
    }

    /** Whether {@code query} is a case-insensitive subsequence of {@code candidate}. */
    private static boolean subsequenceMatch(String query, String candidate) {
        int pos = 0;
        for (int q = 0; q < query.length(); q++) {
            char qc = Character.toLowerCase(query.charAt(q));
            while (pos < candidate.length()
                && Character.toLowerCase(candidate.charAt(pos)) != qc) pos++;
            if (pos == candidate.length()) return false;
            pos++;
        }
        return true;
    }
}
