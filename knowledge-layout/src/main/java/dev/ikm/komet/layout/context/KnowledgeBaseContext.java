package dev.ikm.komet.layout.context;

import dev.ikm.komet.layout.KlKnowledgeBaseContext;
import dev.ikm.komet.layout.KlObject;
import dev.ikm.komet.layout.preferences.KlProfiles;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.id.PublicIdStringKey;
import dev.ikm.tinkar.common.service.ServiceKeys;
import dev.ikm.tinkar.common.service.ServiceProperties;
import dev.ikm.tinkar.coordinate.view.ViewCoordinateRecord;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

import java.io.File;
import java.util.Optional;

import static dev.ikm.komet.layout.KlObject.PreferenceKeys.INITIALIZED;

/**
 * The KnowledgeBaseContext class serves as a singleton implementation of KlKnowledgeBaseContext.
 * It provides a centralized context and preferences management for a knowledge base.
 * This class ensures that a single, shared instance is used throughout the application.
 *
 * It includes mechanisms to handle properties, preferences, and associated context operations.
 * Properties are stored in a thread-safe observable map, and preferences are managed
 * using a shared knowledge base preferences component. The internal context is initialized
 * based on preference-based configurations or with default values as required.
 */
public class KnowledgeBaseContext implements KlKnowledgeBaseContext {

    public static final KnowledgeBaseContext INSTANCE = new KnowledgeBaseContext();

    private final ObservableMap<Object, Object> properties = FXCollections.synchronizedObservableMap(FXCollections.observableHashMap());
    private final KometPreferences preferences;
    private final KlContext context;

    // create
    private KnowledgeBaseContext() {
        this.preferences = KlProfiles.sharedKnowledgeBasePreferences();
        if (preferences.getBoolean(INITIALIZED, false)) {
            ViewCoordinateRecord viewCoordinateRecord = (ViewCoordinateRecord) KlContext.PreferenceKeys.VIEW_COORDINATE.defaultValue();
            Optional<File> dataStoreRoot = ServiceProperties.get(ServiceKeys.DATA_STORE_ROOT);
            StringBuilder sb = new StringBuilder("Knowledge Base context for: ");
            if (dataStoreRoot.isPresent()) {
                sb.append(dataStoreRoot.get().getAbsolutePath());
            } else {
                sb.append(" Knowledge base without data store root configured");
            }
            PublicIdStringKey publicIdStringKey = PublicIdStringKey.make(sb.toString());
            this.context = new KbContext(this, viewCoordinateRecord, publicIdStringKey);
        } else {
            this.context = new KbContext(preferences, this);
        }
    }


    @Override
    public KlContext context() {
        return this.context;
    }

    @Override
    public ImmutableList<KlContext> contexts() {
        return Lists.immutable.of(context);
    }

    @Override
    public KlObject klObject() {
        return this;
    }

    @Override
    public KometPreferences preferences() {
        return KlProfiles.sharedKnowledgeBasePreferences();
    }

    @Override
    public ObservableMap<Object, Object> properties() {
        return properties;
    }

    @Override
    public boolean hasProperties() {
        return true;
    }

    @Override
    public boolean hasProperty(Object key) {
        return properties.containsKey(key);
    }

    private class KbContext extends ContextBlueprint {
        public KbContext(KlContextProvider contextProvider, ViewCoordinateRecord viewCoordinateRecord, PublicIdStringKey publicIdStringKey) {
            super(contextProvider, viewCoordinateRecord, publicIdStringKey);
        }

        public KbContext(KometPreferences preferences, KlContextProvider contextProvider) {
            super(preferences, contextProvider);
        }

        @Override
        public KlObject klPeer() {
            return KnowledgeBaseContext.this;
        }

        @Override
        public void unsubscribeDependentContexts() {
            // TODO: Decide if we propagate to all windows from here.
        }

        @Override
        public void subscribeDependentContexts() {
            // TODO: Decide if we propagate to all windows from here.
        }
    }
}
