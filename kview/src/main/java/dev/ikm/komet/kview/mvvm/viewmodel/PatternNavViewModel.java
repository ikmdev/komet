package dev.ikm.komet.kview.mvvm.viewmodel;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.common.util.text.NaturalOrder;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.entity.SemanticRecord;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.text.html.ListView;
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

//    public void loadAllPatterns() {
//        ViewProperties viewProperties = getPropertyValue(VIEW_PROPERTIES);
//        //this.rootTreeItem.getChildren().clear();
//        TinkExecutor.threadPool().execute(() -> {
//            ArrayList<TreeItem<Object>> patternItems = new ArrayList<>();
//            PrimitiveData.get().forEachPatternNid(patternNid -> {
//                Latest<PatternEntityVersion> latestPattern = viewProperties.calculator().latest(patternNid);
//                latestPattern.ifPresent(patternEntityVersion -> {
//                    patternItems.add(new TreeItem<>(patternNid));
//                });
//            });
//            patternItems.sort((o1, o2) -> {
//                if (o1.getValue() instanceof Integer nid1 && o2.getValue() instanceof Integer nid2) {
//                    return NaturalOrder.compareStrings(viewProperties.calculator().getDescriptionTextOrNid(nid1),
//                            viewProperties.calculator().getDescriptionTextOrNid(nid2));
//                } else {
//                    return NaturalOrder.compareStrings(o1.toString(), o2.toString());
//                }
//            });
//            Platform.runLater(() -> this.rootTreeItem.getChildren().setAll(patternItems));
//            for (TreeItem<Object> patternItem : patternItems) {
//                ArrayList<TreeItem<Object>> patternChildren = new ArrayList<>();
//                int patternNid = (Integer) patternItem.getValue();
//                AtomicInteger childCount = new AtomicInteger();
//                PrimitiveData.get().forEachSemanticNidOfPattern(patternNid, semanticNid -> {
//                    if (childCount.incrementAndGet() < maxChildrenInPatternViewer) {
//                        patternChildren.add(new TreeItem<>(semanticNid));
//                    }
//                });
//                if (childCount.get() >= maxChildrenInPatternViewer) {
//                    NumberFormat numberFormat = NumberFormat.getInstance();
//                    patternChildren.add(new TreeItem<>(numberFormat.format(childCount.get() - maxChildrenInPatternViewer) + " additional semantics suppressed..."));
//                }
//                Platform.runLater(() -> patternItem.getChildren().setAll(patternChildren));
//            }
//        });
//    }
}
