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
package dev.ikm.komet.preferences;

import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.common.service.SimpleIndeterminateTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


//~--- classes ----------------------------------------------------------------

/**
 * 
 */
public class PreferencesProvider
        implements PreferencesService {

    public static final PreferencesProvider singleton = new PreferencesProvider();
    private static final Logger LOG = LoggerFactory.getLogger(PreferencesProvider.class);
    ;

    //~--- methods -------------------------------------------------------------

    /**
     * Start me.
     */
    public void start() {
        SimpleIndeterminateTracker progressTask = new SimpleIndeterminateTracker("Preference provider startup");
        TinkExecutor.threadPool().submit(progressTask);
        try {
            //Just doing this to make sure it starts without errors
            KometPreferencesImpl.getConfigurationRootPreferences();
        } catch (Throwable ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
        } finally {
            progressTask.finished();
        }
    }

    /**
     * Stop me.
     */
    public void stop() {
        SimpleIndeterminateTracker progressTask = new SimpleIndeterminateTracker("Preference provider save");
        TinkExecutor.threadPool().submit(progressTask);
        try {
            KometPreferencesImpl.getConfigurationRootPreferences().sync();
        } catch (Throwable ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
        } finally {
            progressTask.finished();
        }
    }

    //~--- get methods ---------------------------------------------------------

    @Override
    public void reloadConfigurationPreferences() {
        KometPreferencesImpl.reloadConfigurationPreferences();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KometPreferences getConfigurationPreferences() {
        return KometPreferencesImpl.getConfigurationRootPreferences();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KometPreferences getSystemPreferences() {
        return new PreferencesWrapper(Preferences.systemRoot());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KometPreferences getUserPreferences() {
        return new PreferencesWrapper(Preferences.userRoot());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearConfigurationPreferences() {
        try {
            getConfigurationPreferences().removeNode();
            getConfigurationPreferences().flush();
        } catch (BackingStoreException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearSystemPreferences() {
        try {
            Preferences.systemRoot().removeNode();
            Preferences.systemRoot().flush();
        } catch (BackingStoreException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearUserPreferences() {
        try {
            Preferences.userRoot().removeNode();
            Preferences.userRoot().flush();
        } catch (BackingStoreException e) {
            throw new RuntimeException(e);
        }
    }
}
