package dev.ikm.komet.kview.mvvm.view.genpurpose;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The order a section's reference fields get when the layout author set none (an empty
 * {@code EditorSectionModel#getReferenceFieldOrder()}): the pattern's fields ranked by how well
 * their values tell the section's semantics apart. The author's own order, when there is one,
 * bypasses this class.
 *
 * <p>Greedy: the first field is the one whose values split the semantics into the most distinct
 * values; the next is the one that best splits the semantics the first left sharing a value, and
 * so on until every semantic stands alone or no field splits anything further. The fields never
 * picked follow in pattern order. A field that splits as well as an earlier-placed one in the
 * pattern loses to it, so with nothing to tell apart (fewer than two semantics, or identical
 * ones) the result is pattern order.
 */
public final class AutomaticFieldOrder {

    private AutomaticFieldOrder() {
    }

    /**
     * The field indexes {@code 0} to {@code fieldCount - 1}, each once, most distinguishing first.
     *
     * @param valuesPerSemantic one row per semantic, each its field values as display text in
     *                          pattern order; a row shorter than {@code fieldCount}, or a null
     *                          entry, counts as blank for the fields it lacks
     * @param fieldCount        how many fields the pattern has
     */
    public static List<Integer> rank(List<List<String>> valuesPerSemantic, int fieldCount) {
        List<Integer> order = new ArrayList<>(fieldCount);

        // The sets of semantics still sharing every value picked so far: one group of everything
        // to begin with, only groups of two or more being worth splitting.
        List<List<Integer>> tiedGroups = new ArrayList<>();
        List<Integer> everyone = new ArrayList<>();
        for (int i = 0; i < valuesPerSemantic.size(); i++) {
            everyone.add(i);
        }
        if (everyone.size() > 1) {
            tiedGroups.add(everyone);
        }

        while (!tiedGroups.isEmpty()) {
            int bestField = -1;
            int bestSplits = 0;
            for (int field = 0; field < fieldCount; field++) {
                if (order.contains(field)) {
                    continue;
                }
                int splits = 0;
                for (List<Integer> group : tiedGroups) {
                    splits += splitBy(group, field, valuesPerSemantic).size() - 1;
                }
                if (splits > bestSplits) {
                    bestField = field;
                    bestSplits = splits;
                }
            }
            if (bestField < 0) {
                break;
            }
            order.add(bestField);

            List<List<Integer>> stillTied = new ArrayList<>();
            for (List<Integer> group : tiedGroups) {
                for (List<Integer> subgroup : splitBy(group, bestField, valuesPerSemantic).values()) {
                    if (subgroup.size() > 1) {
                        stillTied.add(subgroup);
                    }
                }
            }
            tiedGroups = stillTied;
        }

        for (int field = 0; field < fieldCount; field++) {
            if (!order.contains(field)) {
                order.add(field);
            }
        }
        return order;
    }

    /** The group's semantics by their value of the field. */
    private static Map<String, List<Integer>> splitBy(List<Integer> group, int field, List<List<String>> valuesPerSemantic) {
        Map<String, List<Integer>> byValue = new HashMap<>();
        for (Integer semantic : group) {
            byValue.computeIfAbsent(valueOf(valuesPerSemantic.get(semantic), field), _ -> new ArrayList<>()).add(semantic);
        }
        return byValue;
    }

    private static String valueOf(List<String> values, int field) {
        if (field >= values.size() || values.get(field) == null) {
            return "";
        }
        return values.get(field);
    }
}
