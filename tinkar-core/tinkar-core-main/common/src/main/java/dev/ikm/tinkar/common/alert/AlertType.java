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
package dev.ikm.tinkar.common.alert;

/**
 * A subset of javafx.scene.control.Alert.AlertType
 *
 * 
 */
public enum AlertType {
    /**
     * An information alert.
     */
    INFORMATION(false),

    /**
     * A warning alert.
     */
    WARNING(false),

    /**
     * An error alert.
     */
    ERROR(true),

    /**
     * A confirmation alert. Not sure about this one...
     * confirmation alerts would need some type of time out perhaps...
     */
    CONFIRMATION(false),

    /**
     * Indicate success of an activity such as a commit or another automated process.
     */
    SUCCESS(false);

    private boolean alertPreventsCommit;

    private AlertType(boolean alertPreventsCommit) {
        this.alertPreventsCommit = alertPreventsCommit;
    }

    /**
     * For integration of alerts into the Commit API, we need to know if an alert is fatal to a commit or not.
     *
     * @return
     */
    public boolean preventsCheckerPass() {
        return this.alertPreventsCommit;
    }
}

