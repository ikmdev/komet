/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ikm.komet.framework.preferences;

import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.ProxyFactory;
import dev.ikm.tinkar.terms.TinkarTerm;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;

import static dev.ikm.komet.framework.preferences.PreferenceGroup.Keys.GROUP_NAME;

/**
 * 
 */
public final class UserPreferencesPanel extends AbstractPreferences implements UserPreferenceItems {
    final SimpleObjectProperty<ConceptFacade> userConceptProperty = new SimpleObjectProperty<>(this, TinkarTerm.KOMET_USER.toXmlFragment(), TinkarTerm.KOMET_USER);
    final SimpleListProperty<ConceptFacade> userConceptOptions = new SimpleListProperty(this, TinkarTerm.KOMET_USER_LIST.toXmlFragment(), FXCollections.observableArrayList());
    final PropertySheetItemConceptWrapper userConceptWrapper;
    final SimpleObjectProperty<ConceptFacade> moduleConceptProperty = new SimpleObjectProperty<>(this, TinkarTerm.MODULE_FOR_USER.toXmlFragment(), TinkarTerm.SOLOR_MODULE);
    final SimpleListProperty<ConceptFacade> moduleConceptOptions = new SimpleListProperty(this, TinkarTerm.MODULE_OPTIONS_FOR_EDIT_COORDINATE.toXmlFragment(), FXCollections.observableArrayList());
    final PropertySheetItemConceptWrapper moduleConceptWrapper;
    final SimpleObjectProperty<ConceptFacade> pathConceptProperty = new SimpleObjectProperty<>(this, TinkarTerm.PATH_FOR_USER.toXmlFragment(), TinkarTerm.DEVELOPMENT_PATH);
    final SimpleListProperty<ConceptFacade> pathConceptOptions = new SimpleListProperty(this, TinkarTerm.PATH_OPTIONS_FOR_EDIT_CORDINATE.toXmlFragment(), FXCollections.observableArrayList());
    final PropertySheetItemConceptWrapper pathConceptWrapper;

    public UserPreferencesPanel(KometPreferences preferencesNode, ViewProperties viewProperties,
                                KometPreferencesController kpc) {
        super(preferencesNode, preferencesNode.get(GROUP_NAME, "User"), viewProperties,
                kpc);
        this.userConceptWrapper = new PropertySheetItemConceptWrapper(viewProperties.nodeView(), userConceptProperty);
        this.userConceptWrapper.setAllowedValues(userConceptOptions);

        this.moduleConceptWrapper = new PropertySheetItemConceptWrapper(viewProperties.nodeView(), moduleConceptProperty);
        this.moduleConceptWrapper.setAllowedValues(moduleConceptOptions);

        this.pathConceptWrapper = new PropertySheetItemConceptWrapper(viewProperties.nodeView(), pathConceptProperty);
        this.pathConceptWrapper.setAllowedValues(pathConceptOptions);
        revertFields();
        save();
        int[] userConceptOptionNids = new int[userConceptOptions.size()];
        for (int i = 0; i < userConceptOptionNids.length; i++) {
            userConceptOptionNids[i] = userConceptOptions.get(i).nid();
        }

        getItemList().add(new PropertySheetItemConceptConstraintWrapper(userConceptWrapper, viewProperties.nodeView(), "User"));
        getItemList().add(new PropertySheetItemConceptConstraintWrapper(moduleConceptWrapper, viewProperties.nodeView(), "Module"));

        //login();
    }

    public static void login() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void saveFields() throws BackingStoreException {

        preferencesNode.put(Keys.USER_CONCEPT, Entity.getFast(userConceptProperty.get()).toXmlFragment());
        List<String> userConceptOptionExternalStrings = new ArrayList<>();
        for (ConceptFacade spec : userConceptOptions) {
            userConceptOptionExternalStrings.add(spec.toXmlFragment());
        }
        preferencesNode.putList(Keys.USER_CONCEPT_OPTIONS, userConceptOptionExternalStrings);

        preferencesNode.put(Keys.PATH_CONCEPT, Entity.getFast(pathConceptProperty.get()).toXmlFragment());
        List<String> pathConceptOptionExternalStrings = new ArrayList<>();
        for (ConceptFacade spec : pathConceptOptions) {
            pathConceptOptionExternalStrings.add(spec.toXmlFragment());
        }
        preferencesNode.putList(Keys.PATH_CONCEPT_OPTIONS, pathConceptOptionExternalStrings);

        preferencesNode.put(Keys.MODULE_CONCEPT, Entity.getFast(moduleConceptProperty.get()).toXmlFragment());
        List<String> moduleConceptOptionExternalStrings = new ArrayList<>();
        for (ConceptFacade spec : moduleConceptOptions) {
            moduleConceptOptionExternalStrings.add(spec.toXmlFragment());
        }
        preferencesNode.putList(Keys.MODULE_CONCEPT_OPTIONS, moduleConceptOptionExternalStrings);
    }

    @Override
    protected void revertFields() {

        List<String> userConceptOptionExternalStrings = preferencesNode.getList(Keys.USER_CONCEPT_OPTIONS);
        if (userConceptOptionExternalStrings.isEmpty()) {
            userConceptOptionExternalStrings.add(TinkarTerm.USER.toXmlFragment());
        }
        userConceptOptions.clear();
        for (String externalString : userConceptOptionExternalStrings) {
            userConceptOptions.add(ProxyFactory.fromXmlFragment(externalString));
        }
        String userConceptSpec = preferencesNode.get(Keys.USER_CONCEPT, TinkarTerm.USER.toXmlFragment());
        userConceptProperty.set(ProxyFactory.fromXmlFragment(userConceptSpec));


        List<String> pathConceptOptionExternalStrings = preferencesNode.getList(Keys.PATH_CONCEPT_OPTIONS);
        if (pathConceptOptionExternalStrings.isEmpty()) {
            pathConceptOptionExternalStrings.add(TinkarTerm.MASTER_PATH.toXmlFragment());
            pathConceptOptionExternalStrings.add(TinkarTerm.DEVELOPMENT_PATH.toXmlFragment());
        }
        pathConceptOptions.clear();
        for (String externalString : pathConceptOptionExternalStrings) {
            pathConceptOptions.add(ProxyFactory.fromXmlFragment(externalString));
        }
        String pathConceptSpec = preferencesNode.get(Keys.PATH_CONCEPT, TinkarTerm.DEVELOPMENT_PATH.toXmlFragment());
        pathConceptProperty.set(ProxyFactory.fromXmlFragment(pathConceptSpec));

        List<String> moduleConceptOptionExternalStrings = preferencesNode.getList(Keys.MODULE_CONCEPT_OPTIONS);
        if (moduleConceptOptionExternalStrings.isEmpty()) {
            moduleConceptOptionExternalStrings.add(TinkarTerm.SOLOR_MODULE.toXmlFragment());
            moduleConceptOptionExternalStrings.add(TinkarTerm.SOLOR_OVERLAY_MODULE.toXmlFragment());
        }
        moduleConceptOptions.clear();
        for (String externalString : moduleConceptOptionExternalStrings) {
            moduleConceptOptions.add(ProxyFactory.fromXmlFragment(externalString));
        }
        String moduleConceptSpec = preferencesNode.get(Keys.MODULE_CONCEPT, TinkarTerm.SOLOR_MODULE.toXmlFragment());
        moduleConceptProperty.set(ProxyFactory.fromXmlFragment(moduleConceptSpec));

    }


    enum Keys {
        USER_CONCEPT,
        USER_CONCEPT_OPTIONS,
        MODULE_CONCEPT,
        MODULE_CONCEPT_OPTIONS,
        PATH_CONCEPT,
        PATH_CONCEPT_OPTIONS,
    }
}

