package dev.ikm.komet.kview.mvvm.view.changeset.exchange;

import dev.ikm.tinkar.common.util.time.DateTimeUtil;

import java.time.Instant;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GitSyncFileSorter implements Comparator<String> {

    @Override
    public int compare(String s1, String s2) {
        // Regex Pattern to find datetime in format yyyyMMdd'T'HHmmssZZZ (e.g., 20250725T153526EDT)
        // Regex finds 8 digits, then the letter T, then 8 digits, then 3 capitol letters
        String regex = "(\\d{8}T\\d{6}[A-Z]{3})";
        Pattern pattern = Pattern.compile(regex);

        Matcher s1Matcher = pattern.matcher(s1);
        Matcher s2Matcher = pattern.matcher(s2);

        boolean s1Found = s1Matcher.find();
        boolean s2Found = s2Matcher.find();

        if (s1Found && !s2Found) {
            return -1; // Datetime found in s1 but not s2, so s1 comes first
        } else if (!s1Found && s2Found) {
            return 1; // Datetime found in s2 but not s1, so s2 comes first
        } else if (!s1Found && !s2Found) {
            return s1.compareTo(s2); // Datetime not found in either, so use default string comparison
        } else {
            // Datetime found in both, so compare datetime strings
            String s1Match = s1Matcher.group();
            String s2Match = s2Matcher.group();
            try {
                Instant s1Instant = Instant.from(DateTimeUtil.COMPRESSED_DATE_TIME.parse(s1Match));
                Instant s2Instant = Instant.from(DateTimeUtil.COMPRESSED_DATE_TIME.parse(s2Match));
                long s1EpochMillis = s1Instant.toEpochMilli();
                long s2EpochMillis = s2Instant.toEpochMilli();

                return Long.compare(s1EpochMillis, s2EpochMillis);
            } catch (Exception ex) {
                return s1Match.compareTo(s2Match);
            }
        }
    }
}
