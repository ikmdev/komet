package dev.ikm.komet.amplifydetails;

import dev.ikm.komet.framework.ExplorationNodeAbstract;
import dev.ikm.komet.framework.TopPanelFactory;
import dev.ikm.komet.framework.activity.ActivityStreamOption;
import dev.ikm.komet.framework.controls.EntityLabelWithDragAndDrop;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.flow.FlowSubscriber;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class AmplifyDetailsNode extends ExplorationNodeAbstract {
    private static final Logger LOG = LoggerFactory.getLogger(AmplifyDetailsNode.class);

    protected final SimpleObjectProperty<EntityFacade> entityFocusProperty = new SimpleObjectProperty<>();
    protected FlowSubscriber<Integer> invalidationSubscriber;
    protected static final String CONCEPT_DETAILS_VIEW_FXML_FILE = "amplify-details.fxml";
    protected static final String AMPLIFY_DETAILS_OPTION_1A_CSS_FILE = "amplify-details-opt-1a.css";
    protected static final String AMPLIFY_DETAILS_OPTION_2_CSS_FILE = "amplify-details-opt-2.css";

    protected static final String STYLE_ID = "amplify-details-node";
    protected static final String TITLE = "Amplify Details";
    private BorderPane detailsViewBorderPane;
    private AmplifyDetailsController detailsViewController;

    public AmplifyDetailsNode(ViewProperties viewProperties, KometPreferences nodePreferences) {
        super(viewProperties, nodePreferences);
        init();
        registerListeners(viewProperties);
        revertPreferences();
    }

    /**
     * Initialization view panel(fxml) and it's controller.
     */
    private void init() {
        try {
            // Load Concept Details View Panel (FXML & Controller)
            FXMLLoader loader = new FXMLLoader(getClass().getResource(CONCEPT_DETAILS_VIEW_FXML_FILE));
            this.detailsViewBorderPane = loader.load();
            this.detailsViewController = loader.getController();

            // Programmatically change CSS Theme
            this.detailsViewBorderPane.getStylesheets().clear();
//            String cssFile = AMPLIFY_DETAILS_OPTION_1A_CSS_FILE;
            String cssFile = AMPLIFY_DETAILS_OPTION_2_CSS_FILE;
            String css =this.getClass().getResource(cssFile).toExternalForm();
            this.detailsViewBorderPane.getStylesheets().add(css);
            LOG.info("AmplifyDetailsNode is using CSS file: %s".formatted(cssFile));

            // Add the menu drop down for coordinates & activity stream options with Blue Title of concept
            this.detailsViewBorderPane.setTop(
                    TopPanelFactory.make(
                            viewProperties,
                            entityFocusProperty,
                            activityStreamKeyProperty,
                            optionForActivityStreamKeyProperty,
                            true));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Wireup listeners(handler code) that will respond on change. E.g. The entityFocusProperty changes when a user selects a concept (in a Navigator tree view).
     * @param viewProperties
     */
    private void registerListeners(ViewProperties viewProperties) {

        // When a new entity is selected populate the view. An entity has been selected upstream (activity stream)
        this.entityFocusProperty.addListener((observable, oldEntityFacade, newEntityFacade) -> {
            if (newEntityFacade != null) {
                titleProperty.set(viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(newEntityFacade));
                toolTipTextProperty.set(viewProperties.calculator().getFullyQualifiedDescriptionTextWithFallbackOrNid(newEntityFacade));

                // Also publish(dispatch) to any subscribers of this view.
                if (ActivityStreamOption.PUBLISH.keyForOption().equals(super.optionForActivityStreamKeyProperty.get()) ||
                        ActivityStreamOption.SYNCHRONIZE.keyForOption().equals(super.optionForActivityStreamKeyProperty.get())) {
                    getActivityStream().dispatch(newEntityFacade);
                }

                // Populate Detail View
                if (getDetailsViewController() != null) {
                    getDetailsViewController().updateView(viewProperties, newEntityFacade);
                }

            } else {
                // Show a blank view (nothing selected)
                titleProperty.set(EntityLabelWithDragAndDrop.EMPTY_TEXT);
                toolTipTextProperty.set(EntityLabelWithDragAndDrop.EMPTY_TEXT);
                getDetailsViewController().clearView();
            }
        });

        // If database updates the underlying entity, this will do a force update of the UI.
        this.invalidationSubscriber = new FlowSubscriber<>(nid -> {
            if (entityFocusProperty.isNotNull().get() && entityFocusProperty.get().nid() == nid) {
                // component has changed, need to update.
                Platform.runLater(() -> entityFocusProperty.set(null));
                Platform.runLater(() -> entityFocusProperty.set(Entity.provider().getEntityFast(nid)));
            }
        });

        // Register to the Entity Service
        Entity.provider().addSubscriberWithWeakReference(this.invalidationSubscriber);

    }

    protected void revertDetailsPreferences() {

    }

    /**
     * Returns the associated controller to update the UI.
     * @return AmplifyDetailsController The attached controller to the Details view (fxml)
     */
    private AmplifyDetailsController getDetailsViewController() {
        return detailsViewController;
    }

    @Override
    public String getDefaultTitle() {
        return TITLE;
    }

    @Override
    public void handleActivity(ImmutableList<EntityFacade> entities) {
        if (entities.isEmpty()) {
            entityFocusProperty.set(null);
        } else {
            EntityFacade entityFacade = entities.get(0);
            // Only display Concept Details.
            if (entityFacade instanceof ConceptFacade) {
                entityFocusProperty.set(entityFacade);
            } else {
                entityFocusProperty.set(null);
            }
        }
    }

    @Override
    public final void revertAdditionalPreferences() {
        if (nodePreferences.hasKey(DetailNodeKey.ENTITY_FOCUS)) {
            nodePreferences.getEntity(DetailNodeKey.ENTITY_FOCUS).ifPresentOrElse(entityFacade -> entityFocusProperty.set(entityFacade),
                    () -> entityFocusProperty.set(null));
        }
        revertDetailsPreferences();
    }

    @Override
    public String getStyleId() {
        return STYLE_ID;
    }

    @Override
    protected void saveAdditionalPreferences() {
        if (entityFocusProperty.get() != null) {
            nodePreferences.putEntity(DetailNodeKey.ENTITY_FOCUS, entityFocusProperty.get());
        } else {
            nodePreferences.remove(DetailNodeKey.ENTITY_FOCUS);
        }
        nodePreferences.putBoolean(DetailNodeKey.REQUEST_FOCUS_ON_ACTIVITY, false);
        saveDetailsPreferences();
    }

    protected void saveDetailsPreferences() {

    }

    @Override
    public Node getNode() {
        return this.detailsViewBorderPane;
    }

    @Override
    public void close() {

    }

    @Override
    public boolean canClose() {
        return false;
    }

    @Override
    public Class<AmplifyDetailsNodeFactory> factoryClass() {
        return AmplifyDetailsNodeFactory.class;
    }
    public enum DetailNodeKey {
        ENTITY_FOCUS,
        REQUEST_FOCUS_ON_ACTIVITY
    }

}
