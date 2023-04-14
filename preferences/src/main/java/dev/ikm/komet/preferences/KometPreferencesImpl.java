package dev.ikm.komet.preferences;

import dev.ikm.tinkar.common.service.ServiceKeys;
import dev.ikm.tinkar.common.service.ServiceProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

//~--- classes ----------------------------------------------------------------

/**
 * @author kec
 */
public class KometPreferencesImpl
        extends AbstractPreferences {
    private static final Logger LOG = LoggerFactory.getLogger(KometPreferencesImpl.class);

    public static final String DB_PREFERENCES_FOLDER = "preferences";
    public static final KometPreferencesImpl preferencesImpl = new KometPreferencesImpl();
    public static final KometPreferencesWrapper preferencesWrapper = new KometPreferencesWrapper(preferencesImpl);
    //~--- fieldValues --------------------------------------------------------------
    private final ConcurrentSkipListMap<String, String> preferencesTree = new ConcurrentSkipListMap<>();
    private final File directory;
    private final File preferencesFile;
    private final File temporaryFile;

    //~--- constructors --------------------------------------------------------

    private KometPreferencesImpl() {
        super(null, "");
        File configuredRoot = ServiceProperties.get(ServiceKeys.DATA_STORE_ROOT, new File("target/IsaacPreferencesDefault"));
        this.directory = new File(configuredRoot, DB_PREFERENCES_FOLDER);
        LOG.info("Opening configuration preferences from location: " + this.directory.getAbsolutePath());
        this.preferencesFile = new File(this.directory, "preferences.xml");
        this.temporaryFile = new File(this.directory, "preferences-tmp.xml");
        init();
    }

    private void init() {
        preferencesTree.clear();
        if (preferencesFile.exists()) {
            try (FileInputStream fis = new FileInputStream(preferencesFile)) {
                importMap(fis, preferencesTree);
            } catch (Exception ex) {
                LOG.error(ex.getLocalizedMessage(), ex);
            }
        }
    }

    static void importMap(InputStream is, Map<String, String> map)
            throws Exception {
        XmlForKometPreferences.importMap(is, map);
    }

    //We only enforce singleton for the root preferences.  Its up to the AbstractPreferences to keep track of
    //child references properly.
    private KometPreferencesImpl(KometPreferencesImpl parent, String name) {
        super(parent, name);

        if (!isValidPath(name)) {
            throw new IllegalStateException("Name is not a valid file name or path: " + name);
        }

        this.directory = new File(parent.directory, name);
        this.preferencesFile = new File(this.directory, "preferences.xml");
        this.temporaryFile = new File(this.directory, "preferences-tmp.xml");
        init();
    }

    public static boolean isValidPath(String path) {
        try {
            Paths.get(path);
        } catch (InvalidPathException | NullPointerException ex) {
            return false;
        }
        return true;
    }

    /**
     * The public mechanism to get a handle to a preferences store that stores its data inside the datastore folder.
     *
     * @return This class, wrapped by a {@link KometPreferencesWrapper}
     */
    public static KometPreferences getConfigurationRootPreferences() {
        return preferencesWrapper;
    }
    //~--- methods -------------------------------------------------------------

    public static void reloadConfigurationPreferences() {
        recursiveInit(preferencesImpl);
    }

    private static void recursiveInit(KometPreferencesImpl preferences) {
        preferences.init();
        for (AbstractPreferences childPreferences : preferences.cachedChildren()) {
            recursiveInit((KometPreferencesImpl) childPreferences);
        }
    }

    public Object getLock() {
        return lock;
    }

    @Override
    protected void putSpi(String key, String value) {
        preferencesTree.put(key, value);
    }

    @Override
    protected String getSpi(String key) {
        return preferencesTree.get(key);
    }

    @Override
    protected void removeSpi(String key) {
        preferencesTree.remove(key);
    }

    @Override
    protected void removeNodeSpi()
            throws BackingStoreException {
        if (this.preferencesFile.exists()) {
            this.preferencesFile.delete();
        }

        if (this.temporaryFile.exists()) {
            this.temporaryFile.delete();
        }

        File[] extras = directory.listFiles();

        if (extras != null && extras.length != 0) {
            LOG.warn("Found extraneous files when removing node: " + Arrays.asList(extras));

            for (File extra : extras) {
                extra.delete();
            }
        }

        if (!directory.delete()) {
            throw new BackingStoreException("Couldn't delete: " + directory);
        }
    }

    @Override
    protected String[] keysSpi()
            throws BackingStoreException {
        return preferencesTree.keySet()
                .toArray(new String[preferencesTree.size()]);
    }

    @Override
    protected String[] childrenNamesSpi()
            throws BackingStoreException {
        List<String> result = new ArrayList<>();
        File[] dirContents = directory.listFiles();

        if (dirContents != null) {
            for (File dirContent : dirContents) {
                if (dirContent.isDirectory()) {
                    result.add(dirContent.getName());
                }
            }
        }

        return result.toArray(new String[result.size()]);
    }

    @Override
    protected KometPreferencesImpl childSpi(String name) {
        return new KometPreferencesImpl(this, name);
    }

    @Override
    public String toString() {
        return "Configuration Preference Node: " + this.absolutePath();
    }

    @Override
    protected void syncSpi()
            throws BackingStoreException {
        writeToDisk();
    }

    private void writeToDisk() throws BackingStoreException {
        try {
            if (!directory.exists() && !directory.mkdirs()) {
                throw new BackingStoreException(directory + " create failed.");
            }

            try (FileOutputStream fos = new FileOutputStream(temporaryFile)) {
                exportMap(fos, preferencesTree);
            }
            Files.move(temporaryFile.toPath(), preferencesFile.toPath(), REPLACE_EXISTING);
        } catch (Exception e) {
            if (e instanceof BackingStoreException) {
                throw (BackingStoreException) e;
            }

            throw new BackingStoreException(e);
        }
    }

    static void exportMap(OutputStream os, Map<String, String> map)
            throws Exception {
        XmlForKometPreferences.exportMap(os, map);
    }

    //~--- get methods ---------------------------------------------------------

    @Override
    protected void flushSpi()
            throws BackingStoreException {
        writeToDisk();
    }

    @Override
    public boolean isRemoved() {
        return super.isRemoved();
    }
}