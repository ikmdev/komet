package dev.ikm.komet.artifact;

import javafx.scene.Node;
import javafx.scene.control.Label;
import org.eclipse.collections.api.list.ImmutableList;
import dev.ikm.komet.framework.ExplorationNodeAbstract;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.terms.EntityFacade;

public class ArtifactExportNode extends ExplorationNodeAbstract {
    protected static final String STYLE_ID = "export-node";
    protected static final String TITLE = "Export Artifact";

    public ArtifactExportNode(ViewProperties viewProperties, KometPreferences nodePreferences) {
        super(viewProperties, nodePreferences);
    }

    @Override
    public Node getNode() {
        return new Label(titleProperty.getValue());
    }

    @Override
    public void close() {

    }

    @Override
    public boolean canClose() {
        return false;
    }

    @Override
    public String getDefaultTitle() {
        return TITLE;
    }

    @Override
    public void handleActivity(ImmutableList<EntityFacade> entities) {
        // Nothing to do...
    }

    @Override
    public void revertAdditionalPreferences() {

    }

    @Override
    public String getStyleId() {
        return STYLE_ID;
    }

    @Override
    protected void saveAdditionalPreferences() {
        // No additional fields.
    }

    @Override
    public Class<ArtifactExportNodeFactory> factoryClass() {
        return ArtifactExportNodeFactory.class;
    }
}
