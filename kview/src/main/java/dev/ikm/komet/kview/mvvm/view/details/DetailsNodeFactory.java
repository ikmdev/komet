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
package dev.ikm.komet.kview.mvvm.view.details;

import dev.ikm.komet.framework.KometNode;
import dev.ikm.komet.framework.KometNodeFactory;
import dev.ikm.komet.framework.activity.ActivityStream;
import dev.ikm.komet.framework.activity.ActivityStreamOption;
import dev.ikm.komet.framework.activity.ActivityStreams;
import dev.ikm.komet.framework.annotations.KometNodeFactoryDisplay;
import dev.ikm.komet.framework.preferences.Reconstructor;
import dev.ikm.komet.framework.view.ObservableViewNoOverride;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.id.PublicIdStringKey;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@KometNodeFactoryDisplay(journalView = false, dockFXView = false)
public class DetailsNodeFactory implements KometNodeFactory {
    private static final Logger LOG = LoggerFactory.getLogger(DetailsNodeFactory.class);

    protected static final String STYLE_ID = DetailsNode.STYLE_ID;
    protected static final String TITLE = DetailsNode.TITLE;
    
	public static DetailsNodeFactory provider() {
		return new DetailsNodeFactory();
	}

	public DetailsNodeFactory() {
		super();
	}

    @Override
    public void addDefaultNodePreferences(KometPreferences nodePreferences) {

    }

    @Override
    public ImmutableList<PublicIdStringKey<ActivityStream>> defaultActivityStreamChoices() {
        return Lists.immutable.of(ActivityStreams.SEARCH, ActivityStreams.NAVIGATION,
                ActivityStreams.UNLINKED, ActivityStreams.BUILDER);
    }

    @Override
    public ImmutableList<PublicIdStringKey<ActivityStreamOption>> defaultOptionsForActivityStream(PublicIdStringKey<ActivityStream> streamKey) {
        if (defaultActivityStreamChoices().contains(streamKey)) {
            return Lists.immutable.of(ActivityStreamOption.SUBSCRIBE.keyForOption());
        }
        return Lists.immutable.empty();
    }

    @Reconstructor
    public static DetailsNode reconstructor(ObservableViewNoOverride windowView, KometPreferences nodePreferences) {
        return new DetailsNode(windowView.makeOverridableViewProperties("DetailsNodeFactory.reconstructor"), nodePreferences);
    }
    @Override
    public KometNode create(ObservableViewNoOverride windowView, KometPreferences nodePreferences) {
        return reconstructor(windowView, nodePreferences);
    }

    @Override
    public KometNode create(ObservableViewNoOverride windowView, KometPreferences nodePreferences, boolean displayOnJournalView) {
        return new DetailsNode(windowView.makeOverridableViewProperties("DetailsNodeFactory.create"), nodePreferences, displayOnJournalView);
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
        return DetailsNode.class;
    }
}

