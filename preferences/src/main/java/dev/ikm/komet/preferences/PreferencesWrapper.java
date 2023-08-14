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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.prefs.BackingStoreException;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

/**
 * An system or user preferences wrapper.
 *
 * 
 */
public class PreferencesWrapper implements KometPreferences {
    private static final Logger LOG = LoggerFactory.getLogger(PreferencesWrapper.class);
    final Preferences delegate;

    public PreferencesWrapper(Preferences delegate) {
        this.delegate = delegate;
    }

    @Override
    public void put(String key, String value) {
        delegate.put(key, value);
    }

    @Override
    public String get(String key, String defaultValue) {
        return delegate.get(key, defaultValue);
    }

    @Override
    public void remove(String key) {
        delegate.remove(key);
    }

    @Override
    public void clear() throws BackingStoreException {
        delegate.clear();
    }

    @Override
    public void putInt(String key, int value) {
        delegate.putInt(key, value);
    }

    @Override
    public int getInt(String key, int defaultValue) {
        return delegate.getInt(key, defaultValue);
    }

    @Override
    public void putLong(String key, long value) {
        delegate.putLong(key, value);
    }

    @Override
    public long getLong(String key, long defaultValue) {
        return delegate.getLong(key, defaultValue);
    }

    @Override
    public void putBoolean(String key, boolean value) {
        delegate.putBoolean(key, value);
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        return delegate.getBoolean(key, defaultValue);
    }

    @Override
    public void putDouble(String key, double value) {
        delegate.putDouble(key, value);
    }

    @Override
    public double getDouble(String key, double defaultValue) {
        return delegate.getDouble(key, defaultValue);
    }

    @Override
    public void putByteArray(String key, byte[] value) {
        if (key.equals("navigation")) {
            LOG.info("navigation 1: " + java.util.Base64.getEncoder().encodeToString(value));
        }
        delegate.putByteArray(key, value);
    }

    @Override
    public byte[] getByteArray(String key, byte[] defaultValue) {
        if (defaultValue == null) {
            throw new NullPointerException("Default values cannot be null. Use equivalent Optional method instead.");
        }
        return delegate.getByteArray(key, defaultValue);
    }

    @Override
    public String[] keys() throws BackingStoreException {
        return delegate.keys();
    }

    @Override
    public String[] childrenNames() throws BackingStoreException {
        return delegate.childrenNames();
    }

    @Override
    public PreferencesWrapper parent() {
        return new PreferencesWrapper(delegate.parent());
    }

    @Override
    public PreferencesWrapper node(String pathName) {
        return new PreferencesWrapper(delegate.node(pathName));
    }

    @Override
    public boolean nodeExists(String pathName) throws BackingStoreException {
        return delegate.nodeExists(pathName);
    }

    @Override
    public void removeNode() throws BackingStoreException {
        try {
            delegate.removeNode();
        } catch (IllegalStateException ex) {
            // if already removed, ignore.
        }
    }

    @Override
    public String name() {
        return delegate.name();
    }

    @Override
    public String absolutePath() {
        return delegate.absolutePath();
    }

    @Override
    public PreferenceNodeType getNodeType() {
        if (delegate.isUserNode()) {
            return PreferenceNodeType.USER;
        }
        return PreferenceNodeType.SYSTEM;
    }

    @Override
    public void flush() throws BackingStoreException {
        delegate.flush();
    }

    @Override
    public void sync() throws BackingStoreException {
        delegate.sync();
    }

    @Override
    public void addPreferenceChangeListener(PreferenceChangeListener pcl) {
        delegate.addPreferenceChangeListener(pcl);
    }

    @Override
    public void removePreferenceChangeListener(PreferenceChangeListener pcl) {
        delegate.removePreferenceChangeListener(pcl);
    }

    @Override
    public void addNodeChangeListener(NodeChangeListener ncl) {
        delegate.addNodeChangeListener(ncl);
    }

    @Override
    public void removeNodeChangeListener(NodeChangeListener ncl) {
        delegate.removeNodeChangeListener(ncl);
    }

    @Override
    public void exportNode(OutputStream os) throws IOException, BackingStoreException {
        delegate.exportNode(os);
    }

    @Override
    public void exportSubtree(OutputStream os) throws IOException, BackingStoreException {
        delegate.exportSubtree(os);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

}
