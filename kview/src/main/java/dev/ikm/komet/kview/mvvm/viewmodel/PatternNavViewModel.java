package dev.ikm.komet.kview.mvvm.viewmodel;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.common.util.text.NaturalOrder;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.stream.Stream;

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
            ObservableList<EntityFacade> observableList = getObservableList(PATTERN_COLLECTION);
            PrimitiveData.get().forEachPatternNid(patternNid -> {
                Latest<PatternEntityVersion> latestPattern = viewProperties.calculator().latest(patternNid);
                latestPattern.ifPresent(patternEntityVersion -> {
                    if (EntityService.get().getEntity(patternNid).isPresent()) {
                        observableList.add(EntityService.get().getEntity(patternNid).get());
                    }
                });
            });
            observableList.sort((o1, o2) -> {
                if ((Integer) o1.nid() instanceof Integer nid1 && (Integer) o2.nid() instanceof Integer nid2) {
                    return NaturalOrder.compareStrings(viewProperties.calculator().getDescriptionTextOrNid(nid1),
                            viewProperties.calculator().getDescriptionTextOrNid(nid2));
                } else {
                    return NaturalOrder.compareStrings(o1.toString(), o2.toString());
                }
            });
            if (streamConsumer != null) {
                streamConsumer.accept(observableList.stream());
            }
        });
    }

    public void setOnReload(Consumer<Stream<EntityFacade>> streamConsumer) {
        this.streamConsumer = streamConsumer;
    }
}
