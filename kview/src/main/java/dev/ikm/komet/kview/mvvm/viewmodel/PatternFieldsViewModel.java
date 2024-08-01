package dev.ikm.komet.kview.mvvm.viewmodel;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.terms.EntityFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PatternFieldsViewModel extends FormViewModel {

    private static final Logger LOG = LoggerFactory.getLogger(PatternFieldsViewModel.class);

    public static String PURPOSE_ENTITY = "purposeEntity";

    public static String MEANING_ENTITY = "meaningEntity";

    public PatternFieldsViewModel() {
        super();
        addProperty(VIEW_PROPERTIES, (ViewProperties) null)
                .addProperty(PURPOSE_ENTITY, (EntityFacade) null) // this is/will be the 'purpose' concept entity
                .addProperty(MEANING_ENTITY, (EntityFacade) null); // this is/will be the 'purpose' concept entity
    }
}
