package dev.ikm.komet.search;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import org.eclipse.collections.api.list.ImmutableList;
import dev.ikm.komet.framework.ExplorationNodeAbstract;
import dev.ikm.komet.framework.search.SearchControllerAndNode;
import dev.ikm.komet.framework.search.SearchPanelController;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.terms.EntityFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

public class SearchNode extends ExplorationNodeAbstract {
    private static final Logger LOG = LoggerFactory.getLogger(SearchNode.class);
    protected static final String STYLE_ID = "search-node";
    protected static final String TITLE = "Search";
    // TODO link entityFocus with list selection
    final SimpleObjectProperty<EntityFacade> entityFocusProperty = new SimpleObjectProperty<>();
    final SearchPanelController controller;
    private final BorderPane searchPane = new BorderPane();

    private ReadOnlyDoubleProperty widthProperty;

    // TODO add option to send semantic or enclosing top component to activity stream...
    public SearchNode(ViewProperties viewProperties, KometPreferences nodePreferences) {
        super(viewProperties, nodePreferences);
        // TODO makeOverridableViewProperties should accept node preferences, and accept saved overrides
        try {
            SearchControllerAndNode searchControllerAndNode = new SearchControllerAndNode();
            this.searchPane.setCenter(searchControllerAndNode.searchPanelPane());
            this.controller = searchControllerAndNode.controller();

            Platform.runLater(() -> {
                findTabPaneParent().ifPresent(tabPane -> {
                    widthProperty = tabPane.widthProperty();
                    tabPane.widthProperty().addListener((observable, oldValue, newValue) -> {
                        this.searchPane.setMinWidth(newValue.doubleValue());
                        this.searchPane.setPrefWidth(newValue.doubleValue());
                        this.searchPane.setMaxWidth(newValue.doubleValue());
                    });
                });
                this.controller.setProperties(this.searchPane, this.activityStreamKeyProperty(), viewProperties, nodePreferences);
                this.searchPane.setTop(null);
            });
            revertPreferences();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    Optional<TabPane> findTabPaneParent() {
        Parent parent = this.searchPane.getParent();
        while (parent != null) {
            if (parent instanceof TabPane tabPane) {
                return Optional.of(tabPane);
            }
            parent = parent.getParent();
        }
        LOG.info("Tab pane not found...");
        return Optional.empty();
    }

    protected ReadOnlyDoubleProperty widthProperty() {
        return widthProperty;
    }

    @Override
    protected boolean showActivityStreamIcon() {
        return false;
    }

    @Override
    public String getDefaultTitle() {
        return TITLE;
    }

    @Override
    public void handleActivity(ImmutableList<EntityFacade> entities) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void revertAdditionalPreferences() {
        this.controller.setQueryString(getNodePreferences().get(SearchKeys.QUERY_TEXT, ""));
        getNodePreferences().get(SearchKeys.RESULT_LAYOUT_OPTION).ifPresent(optionName -> {
            this.controller.getResultsLayoutCombo().setValue(SearchPanelController.RESULT_LAYOUT_OPTIONS.valueOf(optionName));
        });
        getNodePreferences().getBoolean(SearchKeys.SHOW_RESULTS).ifPresent(showResults -> {
            if (showResults) {
                Platform.runLater(() -> this.controller.doSearch());
            }
        });
    }

    @Override
    public String getStyleId() {
        return STYLE_ID;
    }

    @Override
    protected void saveAdditionalPreferences() {
        getNodePreferences().put(SearchKeys.QUERY_TEXT, this.controller.getQueryString());
        getNodePreferences().put(SearchKeys.RESULT_LAYOUT_OPTION, this.controller.getResultsLayoutCombo().getValue().name());
        getNodePreferences().putBoolean(SearchKeys.SHOW_RESULTS, this.controller.hasResults());
    }

    @Override
    public Node getNode() {
        return searchPane;
    }

    @Override
    public void close() {

    }

    @Override
    public boolean canClose() {
        return false;
    }

    @Override
    public Class factoryClass() {
        return SearchNodeFactory.class;
    }

    enum SearchKeys {
        QUERY_TEXT,
        SHOW_RESULTS,
        RESULT_LAYOUT_OPTION,
    }
}