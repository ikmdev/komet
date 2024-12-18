package dev.ikm.komet.layout.context;

import dev.ikm.tinkar.common.service.PluggableService;
import dev.ikm.tinkar.common.service.PluginServiceLoader;
import javafx.collections.ObservableList;

/**
 * There is one context for a stand-alone application. There may be multiple ApplicationContexts
 * in a multi-user environment.
 */
public interface ApplicationContext {
    /**
     * Returns the singleton instance of the ApplicationContext.
     *
     * @return the ApplicationContext instance.
     */
    static ApplicationContext get() {
        return PluggableService.first(ApplicationContext.class);
    }
    /**
     * Returns a list of orchestration contexts for the application.
     *
     * @return an observable list of {@link KlOrchestrationContext} instances.
     */
    ObservableList<KlOrchestrationContext> orchestrationContexts();
}
