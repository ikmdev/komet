/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ikm.komet.framework.comment;

import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.list.ImmutableList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Reads the commit-comment thread attached to a STAMP (or any component).
 * <p>
 * A commit comment is modeled as a {@link TinkarTerm#COMMENT_PATTERN} semantic whose referenced
 * component is the STAMP's nid, with the comment text in field 0. Because a stamp may be
 * referenced by many such semantics — each authored independently, each carrying its own
 * stamp — the comments form a thread. This is the read side of that model: it replaces the
 * never-implemented {@code Get.commitService().getComment(stamp)} the display once expected.
 */
public final class CommentReader {

    /**
     * One comment in a thread: the comment text plus the author and time of the comment
     * semantic's own latest version.
     *
     * @param text        the comment text (field 0 of the comment semantic)
     * @param authorNid   the nid of the comment's author (from the comment semantic's stamp)
     * @param time        the comment's commit time, epoch millis (from the comment semantic's stamp)
     * @param semanticNid the nid of the comment semantic itself
     */
    public record CommentEntry(String text, int authorNid, long time, int semanticNid) {}

    private CommentReader() {}

    /**
     * Returns every {@link TinkarTerm#COMMENT_PATTERN} semantic whose referenced component is
     * {@code componentNid} (e.g. a STAMP nid), each taken from its latest version under the
     * supplied view, ordered oldest comment first.
     *
     * @param componentNid the referenced component nid (e.g. a STAMP nid)
     * @param view         the view used to resolve each comment semantic's latest version
     * @return the comment thread, oldest first (empty if there are none)
     */
    public static List<CommentEntry> getComments(int componentNid, ViewCalculator view) {
        List<CommentEntry> comments = new ArrayList<>();
        int[] commentSemanticNids = EntityService.get()
                .semanticNidsForComponentOfPattern(componentNid, TinkarTerm.COMMENT_PATTERN.nid());
        for (int semanticNid : commentSemanticNids) {
            Latest<SemanticEntityVersion> latest = view.stampCalculator().latest(semanticNid);
            if (latest.isPresent()) {
                SemanticEntityVersion version = latest.get();
                ImmutableList<Object> fields = version.fieldValues();
                if (!fields.isEmpty() && fields.get(0) != null) {
                    StampEntity<?> stamp = version.stamp();
                    comments.add(new CommentEntry(String.valueOf(fields.get(0)),
                            stamp.authorNid(), stamp.time(), semanticNid));
                }
            }
        }
        comments.sort(Comparator.comparingLong(CommentEntry::time));
        return comments;
    }
}
