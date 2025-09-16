package dev.ikm.komet.kview.mvvm.viewmodel;

import static dev.ikm.komet.kview.mvvm.viewmodel.OtherNameViewModel.OtherNameProperties.DESCRIPTION_CASE_SIGNIFICANCE;
import static dev.ikm.komet.kview.mvvm.viewmodel.OtherNameViewModel.OtherNameProperties.DESCRIPTION_LANGUAGE;
import static dev.ikm.komet.kview.mvvm.viewmodel.OtherNameViewModel.OtherNameProperties.HAS_OTHER_NAME;
import dev.ikm.tinkar.entity.ConceptEntity;

public class OtherNameViewModel extends DescrNameViewModel {

    public enum OtherNameProperties {
        DESCRIPTION_CASE_SIGNIFICANCE,                  // The FQN Case Significance
        DESCRIPTION_LANGUAGE,                           // The FQN Language
        HAS_OTHER_NAME,                         // Whether the concept already has any other name
    }

    public OtherNameViewModel() {
        addProperty(DESCRIPTION_CASE_SIGNIFICANCE, (ConceptEntity) null)
        .addProperty(DESCRIPTION_LANGUAGE, (ConceptEntity) null)
        .addProperty(HAS_OTHER_NAME, false);
    }
}