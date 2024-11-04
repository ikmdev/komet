package dev.ikm.komet.kview.mvvm.viewmodel;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.common.util.text.NaturalOrder;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class PatternNavViewModel extends FormViewModel {

    private static final Logger LOG = LoggerFactory.getLogger(PatternNavViewModel.class);

    public static String PATTERN_COLLECTION = "pattern";

    private final int maxChildrenInPatternViewer = 150;

    public PatternNavViewModel() {
        super();
        addProperty(VIEW_PROPERTIES, (ViewProperties) null)
        .addProperty(PATTERN_COLLECTION, new ArrayList<EntityFacade>());
    }

    public List<EntityFacade> loadAllPatterns() {
        ViewProperties viewProperties = getPropertyValue(VIEW_PROPERTIES);
        if (viewProperties == null) {
            return Collections.EMPTY_LIST;
        }
//        ImmutableList<String> navigationPatternDescriptions = this.viewProperties.nodeView().calculator().
//                getPreferredDescriptionTextListForComponents(this.observableView.navigationCoordinate().navigationPatternNids());

        ArrayList<EntityFacade> patternItems = new ArrayList<>();
        Map<Integer, List<EntityFacade>> patternCollection = new HashMap<>();
        TinkExecutor.threadPool().execute(() -> {
            PrimitiveData.get().forEachPatternNid(patternNid -> {
                Latest<PatternEntityVersion> latestPattern = viewProperties.calculator().latest(patternNid);
                latestPattern.ifPresent(patternEntityVersion -> {
                    if (EntityService.get().getEntity(patternNid).isPresent()) {
                        patternItems.add(EntityService.get().getEntity(patternNid).get());
                    }
                });
            });

            //FIXME figure out sorting later
            patternItems.sort((o1, o2) -> {
                if ((Integer)o1.nid() instanceof Integer nid1 && (Integer)o2.nid() instanceof Integer nid2) {
                    return NaturalOrder.compareStrings(viewProperties.calculator().getDescriptionTextOrNid(nid1),
                            viewProperties.calculator().getDescriptionTextOrNid(nid2));
                } else {
                    return NaturalOrder.compareStrings(o1.toString(), o2.toString());
                }
            });
            // FIXME don't use rootTreeItem use a plain collection...

            for (EntityFacade patternItem : patternItems) {

                int patternNid = patternItem.nid();
                AtomicInteger childCount = new AtomicInteger();
                PrimitiveData.get().forEachSemanticNidOfPattern(patternNid, semanticNid -> {
                    if (childCount.incrementAndGet() < maxChildrenInPatternViewer) {
                        patternCollection.put(patternNid, EntityService.get().getEntityFast(semanticNid));
                        //patternChildren.add(new TreeItem<>(semanticNid));
                    }
                });
//                if (childCount.get() >= maxChildrenInPatternViewer) {
//                    NumberFormat numberFormat = NumberFormat.getInstance();
//                    patternChildren.add(new TreeItem<>(numberFormat.format(childCount.get() - maxChildrenInPatternViewer) + " additional semantics suppressed..."));
//                }
//                Platform.runLater(() -> patternItem.getChildren().setAll(patternChildren));
            }
        });
        LOG.info(patternCollection.toString());
        return patternItems;
    }
}
