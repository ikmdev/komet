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
package dev.ikm.tinkar.provider.search;

import dev.ikm.tinkar.common.util.time.Stopwatch;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class Indexer {
    public static final String NID_POINT = "nidPoint";
    public static final String NID = "nid";
    public static final String RC_NID = "rcNid";
    public static final String PATTERN_NID = "patternNid";
    public static final String FIELD_INDEX = "fieldIndex";
    public static final String TEXT_FIELD_NAME = "text";
    private static final Logger LOG = LoggerFactory.getLogger(Indexer.class);
    private static final File defaultDataDirectory = new File("target/lucene/");
    private static DirectoryReader indexReader;
    private static Directory indexDirectory;
    private static Analyzer analyzer;
    private static IndexWriter indexWriter;
    private final Path indexPath;

    public Indexer() throws IOException {
        Indexer.indexDirectory = new ByteBuffersDirectory();
        Indexer.analyzer = new StandardAnalyzer();
        Indexer.indexWriter = Indexer.getIndexWriter();
        Indexer.indexReader = DirectoryReader.open(Indexer.indexWriter, true, false);
        this.indexPath = null;
    }

    private static IndexWriter getIndexWriter() throws IOException {
        //Create the indexer
        IndexWriterConfig config = new IndexWriterConfig(analyzer());
        config.setCommitOnClose(true);
        return new IndexWriter(indexDirectory(), config);
    }

    public static Analyzer analyzer() {
        return analyzer;
    }
    public static IndexWriter indexWriter() {
        return indexWriter;
    }
    public static Directory indexDirectory() {
        return indexDirectory;
    }
    public static DirectoryReader indexReader() {
        return indexReader;
    }

    public Indexer(Path indexPath) throws IOException {
        Stopwatch stopwatch = new Stopwatch();
        LOG.info("Opening lucene indexer");
        this.indexPath = indexPath;
        Indexer.indexDirectory = FSDirectory.open(this.indexPath);
        Indexer.analyzer = new StandardAnalyzer();
        Indexer.indexWriter = Indexer.getIndexWriter();
        Indexer.indexReader = DirectoryReader.open(Indexer.indexWriter);
        stopwatch.stop();
        LOG.info("Opened lucene index in: " + stopwatch.durationString());
    }

    public void commit() throws IOException {
        Stopwatch stopwatch = new Stopwatch();
        LOG.info("Committing lucene index");
        indexWriter.commit();
        stopwatch.stop();
        LOG.info("Committed lucene index in: {}", stopwatch.durationString());
    }

    public void close() throws IOException {
        Stopwatch stopwatch = new Stopwatch();
        LOG.info("Closing lucene index");
        Indexer.indexReader.close();
        Indexer.indexWriter.close();
        TypeAheadSearch.get().close();
        stopwatch.stop();
        LOG.info("Closed lucene index in: " + stopwatch.durationString());
    }

    public void index(Object object) {
        if (object instanceof SemanticEntity semanticEntity) {
            IntPoint nidPoint = new IntPoint(NID_POINT, 0);
            // The IntPoint field does not store the value,
            // so we also need a stored field to retrieve the nid from a document.
            StoredField nidField = new StoredField(NID, 0);
            StoredField rcNidField = new StoredField(RC_NID, 0);
            StoredField patternNidField = new StoredField(PATTERN_NID, 0);
            StoredField fieldIndexField = new StoredField(FIELD_INDEX, 0);

            // KEC: Deliberately commented out. See explanation on method for reason.
            // deleteDocumentIfExists(semanticEntity);


            Document document = new Document();
            nidPoint.setIntValue(semanticEntity.nid());
            nidField.setIntValue(semanticEntity.nid());
            rcNidField.setIntValue(semanticEntity.referencedComponentNid());
            patternNidField.setIntValue(semanticEntity.patternNid());

            document.add(nidPoint);
            document.add(nidField);
            document.add(rcNidField);
            document.add(patternNidField);
            for (SemanticEntityVersion version : ((SemanticEntity<SemanticEntityVersion>) semanticEntity).versions()) {
                ImmutableList<Object> fields = version.fieldValues();
                for (int i = 0; i < fields.size(); i++) {
                    Object field = fields.get(i);
                    if (field instanceof String text) {
                        text = text.strip();
                        if (i == 0) {
                            document.add(new TextField(TEXT_FIELD_NAME, text, Field.Store.YES));
                            fieldIndexField.setIntValue(i);
                            document.add(fieldIndexField);
                        } else {
                            // Check to make sure identical text is not already in the document,
                            // to prevent unnecessary document/index bloat.
                            boolean alreadyAdded = false;
                            for (String value: document.getValues(TEXT_FIELD_NAME)) {
                                if (text.equals(value)) {
                                    alreadyAdded = true;
                                    break;
                                }
                            }
                            if (!alreadyAdded) {
                                document.add(new TextField(TEXT_FIELD_NAME, text, Field.Store.YES));
                                fieldIndexField.setIntValue(i);
                                document.add(fieldIndexField);
                            }
                        }
                    }
                }
            }
            try {
                long addSequence = indexWriter.addDocument(document);
                if (!EntityService.get().isLoadPhase()) {
                    TypeAheadSearch.get().buildSuggester();
                }
            } catch (IOException e) {
                LOG.error("Exception writing: " + object);
            }
        }

    }

    /**
     * This method would delete any existing document for the semantic. This is a costly operation,
     * and unnecessary for standard use cases. Since the semantic chronologies are append only,
     * the same historic versions will still be written to the index. Determination of current
     * content is done by the StampComputer, not by the lucene index, so there is no need to remove
     * historic versions.
     *
     * @param semanticEntity
     */
    private static void deleteDocumentIfExists(SemanticEntity semanticEntity) {
        try {
            Query nidQuery = IntPoint.newExactQuery(NID_POINT, semanticEntity.nid());
            long deleteSequence = indexWriter.deleteDocuments(nidQuery);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
