package dev.ikm.komet.kview.mvvm.view.changeset.exchange;

import dev.ikm.tinkar.common.util.time.DateTimeUtil;

import java.time.Instant;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Compares GitSync file path Strings for sorting / ordering. If either or both file paths do not contain a datetime
 * in the expected format, then the fallback defaults to the default lexicographic comparison.
 * <p>
 * GitSync file paths are expected to have a datetime in the filename unless renamed by a user. In the case that the
 * file is renamed by the user, it is assumed the changeset is a more prominent "milestone" containing committed data.
 * Due to this assumption, we load those files last to reduce any side effects of loading uncommitted data from the
 * (likely less prominent) auto-named files. To avoid these assumptions, we should update this class to interrogate
 * the MANIFEST files to compare the package-dates.
 */
// TODO: Update to compare the package-dates from the MANIFEST file.
public class GitSyncFileSorter implements Comparator<String> {

    // Regex and Pattern to find datetime in format yyyyMMdd'T'HHmmssZZZ (e.g., 20250725T153526EDT)
    // Regex finds 8 digits, then the letter T, then 6 digits, then 3 capitol letters
    static final String FILENAME_DATETIME_REGEX = "(\\d{8}T\\d{6}[A-Z]{3})";
    static final Pattern FILENAME_DATETIME_PATTERN = Pattern.compile(FILENAME_DATETIME_REGEX);

    /**
     * Compares two file paths for sorting / ordering.
     *
     * @param s1 the first file path String to be compared.
     * @param s2 the second file path String to be compared.
     * @return a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater than the second.
     */
    @Override
    public int compare(String s1, String s2) {
        Matcher s1Matcher = FILENAME_DATETIME_PATTERN.matcher(s1);
        Matcher s2Matcher = FILENAME_DATETIME_PATTERN.matcher(s2);

        boolean s1Found = s1Matcher.find();
        boolean s2Found = s2Matcher.find();

        if (s1Found && !s2Found) {
            return -1; // Datetime found in s1 but not s2, so s1 comes first
        } else if (!s1Found && s2Found) {
            return 1; // Datetime found in s2 but not s1, so s2 comes first
        } else if (!s1Found && !s2Found) {
            return s1.compareTo(s2); // Datetime not found in either, so use default string comparison
        } else {
            // Datetime found in both, so compare datetime representations
            String s1Match = s1Matcher.group();
            String s2Match = s2Matcher.group();

            try {
                // Convert datetime String regex matches to epochMillis and compare resulting values
                Instant s1Instant = Instant.from(DateTimeUtil.COMPRESSED_DATE_TIME.parse(s1Match));
                Instant s2Instant = Instant.from(DateTimeUtil.COMPRESSED_DATE_TIME.parse(s2Match));
                long s1EpochMillis = s1Instant.toEpochMilli();
                long s2EpochMillis = s2Instant.toEpochMilli();

                int dateTimeCompareResult = Long.compare(s1EpochMillis, s2EpochMillis);

                if (dateTimeCompareResult == 0) {
                    return s1.compareTo(s2); // Datetime representations are equal, so use default string comparison
                } else {
                    return dateTimeCompareResult;
                }
            } catch (Exception ex) {
                // If there is an error converting either datetime to epochMillis, then lexicographically compare
                // the datetime String regex matches since it is extremely likely the first 8 digits (i.e, yyyyMMdd)
                // will determine the appropriate sorting order.
                int matchResult = s1Match.compareTo(s2Match);
                if (matchResult == 0) {
                    return s1.compareTo(s2); // String regex matches are still equal, so use default string comparison
                } else {
                    return matchResult;
                }
            }
        }
    }
}
