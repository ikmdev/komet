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
package dev.ikm.komet.kview.mvvm.viewmodel;

import javafx.concurrent.Task;
import org.carlfx.cognitive.viewmodel.ValidationViewModel;

public class ProgressViewModel extends ValidationViewModel {
    public static String TASK_PROPERTY = "taskProperty";
    public static String IS_CANCELLED_PROP = "cancelState";
    public static String CANCEL_BUTTON_TEXT_PROP = "cancelButtonText";
    public static String SHOW_CANCEL_BUTTON_PROP = "showCancelButton";



    public ProgressViewModel(){
        super();
        addProperty(TASK_PROPERTY, (Task<Void>) null);
        addProperty(IS_CANCELLED_PROP, false);
        addProperty(CANCEL_BUTTON_TEXT_PROP, "Cancel");
        addProperty(SHOW_CANCEL_BUTTON_PROP, true);
    }
}
