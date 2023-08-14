package dev.ikm.komet.artifact;

import dev.ikm.komet.framework.ExplorationNodeAbstract;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import org.eclipse.collections.api.list.ImmutableList;

import java.io.IOException;

public class ArtifactImportNode extends ExplorationNodeAbstract {
    protected static final String STYLE_ID = "import-node";
    protected static final String TITLE = "Import Artifact";

    public ArtifactImportNode(ViewProperties viewProperties, KometPreferences nodePreferences) {
        super(viewProperties, nodePreferences);
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
        // No additional preferences.
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
    public Node getNode() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ArtifactImport.fxml"));
        try {
            Pane pane = loader.load();
            return pane;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
    }

    @Override
    public boolean canClose() {
        return false;
    }

    @Override
    public Class<ArtifactImportNodeFactory> factoryClass() {
        return ArtifactImportNodeFactory.class;
    }
}
