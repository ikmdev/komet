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
package dev.ikm.komet.kview.mvvm.view;

import dev.ikm.komet.framework.view.ViewProperties;
import org.carlfx.cognitive.viewmodel.ViewModel;

import java.util.function.BiConsumer;

public abstract class AbstractBasicController implements BasicController {
    private ViewProperties viewProperties;

    public ViewProperties getViewProperties() {
        return viewProperties;
    }
    public void updateModel(ViewProperties viewProperties) {
        this.viewProperties = viewProperties;
    }
    public <T extends ViewModel, U extends BasicController> void updateModel(ViewProperties viewProperties, BiConsumer<T, U> viewModelControllerConsumer) {
        this.viewProperties = viewProperties;
        if (viewModelControllerConsumer != null) {
            viewModelControllerConsumer.accept(getViewModel(), (U) this);
        }
    }
    public <T extends ViewModel, U extends BasicController> void updateViewModel(BiConsumer<T, U> viewModelControllerConsumer) {
        if (viewModelControllerConsumer != null) {
            viewModelControllerConsumer.accept(getViewModel(), (U) this);
        }
    }

    public abstract <T extends ViewModel> T getViewModel();
}
