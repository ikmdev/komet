package dev.ikm.tinkar.common.service;


/**
 * Represents the various activities that can be performed on data 
 * within the application so that database writes can be properly classified.
 */
public enum DataActivity {
    /**
     * Represents the initialization activity typically performed before 
     * any data manipulation or editing operations take place within the application.
     */
    INITIALIZE,

    /**
     * Represents the activity of loading a set of changes into the application.
     * This activity typically involves reading and applying modifications from a predefined change set,
     * allowing the application to update its state based on those changes. It is crucial for scenarios
     * where the application needs to synchronize its data with updates that have been aggregated or prepared beforehand.
     */
    LOADING_CHANGE_SET,

    /**
     * Represents an edit operation which is persistent to the database and are also
     * written to—and sharable via—changes sets.
     */
    SYNCHRONIZABLE_EDIT,

    /**
     * Represents an edit operation where changes to the data
     * are local and won't be saved to change sets for later exchange or recovery.
     */
    LOCAL_EDIT,

    /**
     * Represents an activity where data is repaired, which usually involves
     * fixing corrupted or inconsistent data entries within the application.
     * These activities are typically not recorded in change sets, and would
     * require a full export of the database, and subsequent import to get the repaired data.
     */
    DATA_REPAIR
}
