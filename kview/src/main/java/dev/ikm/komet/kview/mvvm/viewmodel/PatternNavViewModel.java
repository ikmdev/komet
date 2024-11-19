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

}
