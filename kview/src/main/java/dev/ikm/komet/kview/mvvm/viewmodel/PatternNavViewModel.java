package dev.ikm.komet.kview.mvvm.viewmodel;

import dev.ikm.komet.framework.concurrent.TaskWrapper;
import dev.ikm.komet.framework.observable.*;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.common.service.TrackingCallable;
import dev.ikm.tinkar.common.util.text.NaturalOrder;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.PatternFacade;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javafx.application.Platform;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

public class PatternNavViewModel extends FormViewModel {

    private static final Logger LOG = LoggerFactory.getLogger(PatternNavViewModel.class);

    public static String PATTERN_COLLECTION = "pattern";
    public static String MAX_CHILDREN_COUNT = "maxChildrenInPatternViewer";

    private Consumer<Stream<EntityFacade>> streamConsumer;

    public PatternNavViewModel() {
        super();
        this.addProperty(VIEW_PROPERTIES, (ViewProperties) null)
        .addProperty(MAX_CHILDREN_COUNT, 150)
        .addProperty(PATTERN_COLLECTION, new ArrayList<EntityFacade>(), true);
    }



    public void reload() {
        TinkExecutor.threadPool().execute(() -> {
            ViewProperties viewProperties = getPropertyValue(VIEW_PROPERTIES);
            if (viewProperties == null) {
                LOG.warn("PatternNavViewModel's ViewProperties is null. Unable to reload.");
                return;
            }

            LOG.info("Loading patterns...");

            // Step 1: Collect all patterns with valid versions
            List<PatternFacade> patterns = new ArrayList<>();
            PrimitiveData.get().forEachPatternNid(patternNid -> {
                EntityHandle entityHandle = EntityHandle.get(patternNid);
                if (entityHandle.isPresent()) {
                    switch (entityHandle.expectEntity()) {
                        case PatternEntity patternEntity -> {
                            Latest<ObservablePatternVersion> latest = viewProperties.calculator().latest(patternEntity);
                            if (latest.isPresent()) {
                                patterns.add(patternEntity);
                            }
                        }
                        case ConceptEntity conceptEntity -> LOG.warn(
                                "Unexpected concept {} {} found in pattern collection. Skipping...",
                                conceptEntity.publicId(), viewProperties.calculator().getDescriptionTextOrNid(conceptEntity)
                        );
                        case StampEntity stampEntity -> LOG.warn(
                                "Unexpected stamp {} {} found in pattern collection. Skipping...",
                                stampEntity.publicId(), viewProperties.calculator().languageCalculator().getTextForStamp(stampEntity)
                        );
                        case SemanticEntity semanticEntity -> LOG.warn(
                                "Unexpected semantic {} {} found in pattern collection. Skipping...",
                                semanticEntity.publicId(), viewProperties.calculator().getDescriptionTextOrNid(semanticEntity)
                        );
                        default -> throw new IllegalStateException("Unexpected value: " + entityHandle.expectEntity());
                    }
                }
            });

            LOG.info("Loaded {} patterns, now sorting...", patterns.size());

            // Step 2: Sort by name (trivial for 54 items)
            patterns.sort((o1, o2) -> NaturalOrder.compareStrings(
                    viewProperties.calculator().getDescriptionTextOrNid(o1.nid()),
                    viewProperties.calculator().getDescriptionTextOrNid(o2.nid())
            ));

            LOG.info("Sorted {} patterns, updating UI...", patterns.size());

            // Step 3: Update UI on FX thread
            Platform.runLater(() -> {
                ObservableList<EntityFacade> observableList = getObservableList(PATTERN_COLLECTION);
                observableList.clear();
                observableList.addAll(patterns);

                if (streamConsumer != null) {
                    streamConsumer.accept(observableList.stream());
                }

                LOG.info("Reload complete: {} patterns displayed", patterns.size());
            });
        });
    }
    public void setOnReload(Consumer<Stream<EntityFacade>> streamConsumer) {
        this.streamConsumer = streamConsumer;
    }
}
