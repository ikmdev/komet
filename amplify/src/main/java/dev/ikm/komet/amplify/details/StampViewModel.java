package dev.ikm.komet.amplify.details;

import dev.ikm.komet.amplify.mvvm.SimpleViewModel;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.framework.window.WindowSettings;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.KometPreferencesImpl;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.terms.TinkarTerm;

import java.util.ArrayList;
import java.util.List;

import static dev.ikm.komet.preferences.JournalWindowPreferences.MAIN_KOMET_WINDOW;

public class StampViewModel extends SimpleViewModel {

    public List<ConceptEntity> findAllModules() {
        KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();
        KometPreferences windowPreferences = appPreferences.node(MAIN_KOMET_WINDOW);

        WindowSettings windowSettings = new WindowSettings(windowPreferences);
        ViewProperties viewProperties = windowSettings.getView().makeOverridableViewProperties();
        Entity<? extends EntityVersion> moduleEntity = EntityService.get().getEntityFast(TinkarTerm.MODULE);
        IntIdSet moduleDescendents = viewProperties.calculator().descendentsOf(moduleEntity.nid());


        // get all descendant modules
        List<ConceptEntity> allModules =
                moduleDescendents.intStream()
                        .mapToObj(moduleNid -> (ConceptEntity) Entity.getFast(moduleNid))
                        .toList();
        return allModules;

    }
    public List<ConceptEntity<ConceptEntityVersion>> findAllPaths() {
        //List of Concepts that represent available Paths in the data
        List<ConceptEntity<ConceptEntityVersion>> paths = new ArrayList<>();
        //Get all Path semantics from the Paths Pattern
        int[] pathSemanticNids = EntityService.get().semanticNidsOfPattern(TinkarTerm.PATHS_PATTERN.nid());
        //For each Path semantic get the concept that the semantic is referencing
        for (int pathSemanticNid : pathSemanticNids) {
            SemanticEntity<SemanticEntityVersion> semanticEntity = Entity.getFast(pathSemanticNid);
            int pathConceptNid = semanticEntity.referencedComponentNid();
            paths.add(EntityService.get().getEntityFast(pathConceptNid));
        }
        return paths;
    }
}
