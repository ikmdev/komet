package dev.ikm.komet.kview.mvvm.viewmodel;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.mvvm.view.timeline.TimelinePathMap;
import org.carlfx.cognitive.viewmodel.SimpleViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TimelineViewModel extends SimpleViewModel {
    private static final Logger LOG = LoggerFactory.getLogger(TimelineViewModel.class);

    public enum TimelineProperties {
        VIEW_PROPERTIES,

        AVAILABLE_PATH_MOULES_MAP,

        SELECTED_PATH,
        CHECKED_MODULE_IDS,

        FILTER_POP_UP_VISIBLE
    }

    public TimelineViewModel() {
        addProperty(TimelineProperties.AVAILABLE_PATH_MOULES_MAP, new LinkedHashMap<String, List<Integer>>())
                .addProperty(TimelineProperties.CHECKED_MODULE_IDS, List.<Integer>of())
                .addProperty(TimelineProperties.SELECTED_PATH, (String) null)
                .addProperty(TimelineProperties.VIEW_PROPERTIES, (ViewProperties) null)
                .addProperty(TimelineProperties.FILTER_POP_UP_VISIBLE, false);
    }
}
