/*
 * FXSkins,
 * Copyright (C) 2021 PixelDuke (Pedro Duque Vieira - www.pixelduke.com)
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.ikm.komet.kview.controls.skin;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.scene.control.TextField;

public class FXTextFieldSkin extends TextFieldWithButtonSkin {
    public FXTextFieldSkin(TextField textField) {
        super(textField);

        textField.skinProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                textField.applyCss();
                textField.skinProperty().removeListener(this);
            }
        });
    }

    @Override
    protected void onRightButtonReleased()
    {
        getSkinnable().setText("");
    }
}