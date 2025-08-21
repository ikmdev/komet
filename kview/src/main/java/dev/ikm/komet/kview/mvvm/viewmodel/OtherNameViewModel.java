package dev.ikm.komet.kview.mvvm.viewmodel;

import dev.ikm.tinkar.entity.ConceptEntity;

import static dev.ikm.komet.kview.mvvm.viewmodel.OtherNameViewModel.OtherNameProperties.FQN_CASE_SIGNIFICANCE;
import static dev.ikm.komet.kview.mvvm.viewmodel.OtherNameViewModel.OtherNameProperties.FQN_LANGUAGE;
import static dev.ikm.komet.kview.mvvm.viewmodel.OtherNameViewModel.OtherNameProperties.HAS_OTHER_NAME;

public class OtherNameViewModel extends DescrNameViewModel {

    public enum OtherNameProperties {
        FQN_CASE_SIGNIFICANCE,                  // The FQN Case Significance
        FQN_LANGUAGE,                           // The FQN Language
        HAS_OTHER_NAME,                         // Whether the concept already has any other name
    }

    public OtherNameViewModel() {
        addProperty(FQN_CASE_SIGNIFICANCE, (ConceptEntity) null)
        .addProperty(FQN_LANGUAGE, (ConceptEntity) null)
        .addProperty(HAS_OTHER_NAME, false);
    }
}