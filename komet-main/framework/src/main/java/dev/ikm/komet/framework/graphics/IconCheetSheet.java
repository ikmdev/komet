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
package dev.ikm.komet.framework.graphics;

public enum IconCheetSheet {
    Fontawesome("https://kordamp.org/ikonli/cheat-sheet-fontawesome.html"),
    Fontawesome5("https://kordamp.org/ikonli/cheat-sheet-fontawesome5.html"),
    RunestroIcons("https://kordamp.org/ikonli/cheat-sheet-runestroicons.html"),
    OctIcons("https://kordamp.org/ikonli/cheat-sheet-octicons.html"),
    MaterialDesign2("https://kordamp.org/ikonli/cheat-sheet-materialdesign2.html"),
    ControlsFx("-fx-graphic: url(\"/org/controlsfx/dialog/dialog-confirm.png\"); in style sheet"),
    
    ;
    String url;

    IconCheetSheet(String url) {
        this.url = url;
    }
}
