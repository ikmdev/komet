package dev.ikm.komet.kview.mvvm.view.changeset.exchange.test;

import dev.ikm.komet.kview.mvvm.view.changeset.exchange.GitSyncFileSorter;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GitSyncFileSorterTest {

    private final MutableList<String> expectedFileOrder = Lists.mutable.of(
            "AUser (SOLOR) 20250822T123305EDT bSb ike-cs.zip",
            "DUser (SOLOR) 20250822T123305EDT bSb ike-cs.zip",
            "User (SOLOR) 20250822T123305EDT bSb ike-cs.zip",
            "BUser (SOLOR) 20250822T113322CDT Wct ike-cs.zip",
            "User (SOLOR) 20250822T113322CDT Wct ike-cs.zip",
            "CUser (SOLOR) 20250822T123420EDT NCu ike-cs.zip",
            "User (SOLOR) 20250822T123420EDT NCu ike-cs.zip",
            "User (SOLOR) 20250828T094448EDT hDM ike-cs.zip",
            "User (SOLOR) 20250828T095249EDT iq7 ike-cs.zip",
            "User (SOLOR) 20250828T100324EDT ONZ ike-cs.zip",
            "User (SOLOR) 20250828T104221EDT cwP ike-cs.zip",
            "User (SOLOR) 20250828T114447EDT wZa ike-cs.zip",
            "User (SOLOR) 20250828T114805EDT y3C ike-cs.zip",
            "User (SOLOR) 20250828T171321EDT LvZ ike-cs.zip",
            "User (SOLOR) 20250828T172446EDT lS2 ike-cs.zip",
            "User (SOLOR) 20250828T204426EDT SWO ike-cs.zip",
            "User (SOLOR) 20250828T213329EDT JrA ike-cs.zip",
            "Afilename-without-datetime-ike-cs.zip",
            "filename-without-datetime-ike-cs.zip");

    @Test
    public void test() {
        GitSyncFileSorter sorter = new GitSyncFileSorter();
        // Given a list of files in the expected sort order
        // When comparing a file to a SUBSEQUENT file in the list
        // Then the comparison result should always be NEGATIVE
        for (int i=0; i<expectedFileOrder.size()-2; i++) {
            for (int z=i+1; z < expectedFileOrder.size()-1; z++) {
                int comparisonResult = sorter.compare(expectedFileOrder.get(i), expectedFileOrder.get(z));
                assertTrue(comparisonResult < 0,
                        "Expected NEGATIVE comparison result:\n" + expectedFileOrder.get(i) + "\n" + expectedFileOrder.get(z));
            }
        }
        // Given a list of files in the expected sort order
        // When comparing a file to a PRIOR file in the list
        // Then the comparison result should always be POSITIVE
        for (int i=0; i<expectedFileOrder.size()-2; i++) {
            for (int z=i+1; z < expectedFileOrder.size()-1; z++) {
                int comparisonResult = sorter.compare(expectedFileOrder.get(z), expectedFileOrder.get(i));
                assertTrue(comparisonResult > 0,
                        "Expected POSITIVE comparison result:\n" + expectedFileOrder.get(z) + "\n" + expectedFileOrder.get(i));
            }
        }
        // Given a list of files in the expected sort order
        // When comparing a file to an EQUIVALENT file in the list
        // Then the comparison result should always be ZERO
        for (int i=0; i<expectedFileOrder.size()-1; i++) {
            int comparisonResult = sorter.compare(expectedFileOrder.get(i), expectedFileOrder.get(i));
            assertTrue(comparisonResult == 0,
                    "Expected EQUIVALENT comparison result:\n" + expectedFileOrder.get(i) + "\n" + expectedFileOrder.get(i));
        }
    }

    @Test
    public void compareEntireListOfFiles() {
        MutableList<String> filesToSort = Lists.mutable.of(
                "filename-without-datetime-ike-cs.zip",
                "Afilename-without-datetime-ike-cs.zip",
                "AUser (SOLOR) 20250822T123305EDT bSb ike-cs.zip",
                "User (SOLOR) 20250828T094448EDT hDM ike-cs.zip",
                "User (SOLOR) 20250828T114447EDT wZa ike-cs.zip",
                "User (SOLOR) 20250828T204426EDT SWO ike-cs.zip",
                "BUser (SOLOR) 20250822T113322CDT Wct ike-cs.zip",
                "User (SOLOR) 20250822T113322CDT Wct ike-cs.zip",
                "User (SOLOR) 20250828T095249EDT iq7 ike-cs.zip",
                "User (SOLOR) 20250828T114805EDT y3C ike-cs.zip",
                "User (SOLOR) 20250828T213329EDT JrA ike-cs.zip",
                "CUser (SOLOR) 20250822T123420EDT NCu ike-cs.zip",
                "User (SOLOR) 20250822T123305EDT bSb ike-cs.zip",
                "User (SOLOR) 20250828T100324EDT ONZ ike-cs.zip",
                "User (SOLOR) 20250828T171321EDT LvZ ike-cs.zip",
                "DUser (SOLOR) 20250822T123305EDT bSb ike-cs.zip",
                "User (SOLOR) 20250822T123420EDT NCu ike-cs.zip",
                "User (SOLOR) 20250828T104221EDT cwP ike-cs.zip",
                "User (SOLOR) 20250828T172446EDT lS2 ike-cs.zip");

        filesToSort.sort(new GitSyncFileSorter());

        assertEquals(expectedFileOrder, filesToSort);
    }
}
