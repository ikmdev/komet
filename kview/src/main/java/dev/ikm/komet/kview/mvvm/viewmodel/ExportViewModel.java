package dev.ikm.komet.kview.mvvm.viewmodel;

import static dev.ikm.tinkar.terms.TinkarTerm.DEVELOPMENT_PATH;
import static dev.ikm.tinkar.terms.TinkarTerm.MASTER_PATH;
import static dev.ikm.tinkar.terms.TinkarTerm.PRIMORDIAL_PATH;
import static dev.ikm.tinkar.terms.TinkarTerm.SANDBOX_PATH;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.terms.EntityFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class ExportViewModel extends FormViewModel {

    private static final Logger LOG = LoggerFactory.getLogger(ExportViewModel.class);

    public ExportViewModel() {
        super();
            addProperty(VIEW_PROPERTIES, (ViewProperties) null);
    }

    public Set<EntityFacade> getPaths() {
        return Set.of(PRIMORDIAL_PATH, SANDBOX_PATH);
    }
}
