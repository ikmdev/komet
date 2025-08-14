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
package dev.ikm.komet.artifact;

import dev.ikm.komet.framework.KometNode;
import dev.ikm.komet.framework.KometNodeFactory;
import dev.ikm.komet.framework.activity.ActivityStream;
import dev.ikm.komet.framework.activity.ActivityStreamOption;
import dev.ikm.komet.framework.preferences.Reconstructor;
import dev.ikm.komet.framework.view.ObservableViewNoOverride;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.id.PublicIdStringKey;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

public class ArtifactImportNodeFactory implements KometNodeFactory {
    protected static final String STYLE_ID = ArtifactImportNode.STYLE_ID;
    protected static final String TITLE = ArtifactImportNode.TITLE;
    
    public static ArtifactImportNodeFactory provider() {
		return new ArtifactImportNodeFactory();
	}

	private ArtifactImportNodeFactory() {
		super();
	}

    @Override
    public void addDefaultNodePreferences(KometPreferences nodePreferences) {

    }

    @Override
    public ImmutableList<PublicIdStringKey<ActivityStream>> defaultActivityStreamChoices() {
        return Lists.immutable.empty();
    }
    @Override
    public ImmutableList<PublicIdStringKey<ActivityStreamOption>> defaultOptionsForActivityStream(PublicIdStringKey<ActivityStream> streamKey) {
        return Lists.immutable.empty();
    }
    @Reconstructor
    public static ArtifactImportNode reconstructor(ObservableViewNoOverride windowView, KometPreferences nodePreferences) {
        return new ArtifactImportNode(windowView.makeOverridableViewProperties("ArtifactImportNodeFactory.reconstructor"), nodePreferences);
    }
    @Override
    public KometNode create(ObservableViewNoOverride windowView, KometPreferences nodePreferences) {
        return reconstructor(windowView, nodePreferences);
    }

    @Override
    public String getMenuText() {
        return TITLE;
    }

    @Override
    public String getStyleId() {
        return STYLE_ID;
    }

    @Override
    public Class<? extends KometNode> kometNodeClass() {
        return ArtifactImportNode.class;
    }
}

