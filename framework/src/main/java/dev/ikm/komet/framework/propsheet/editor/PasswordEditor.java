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
package dev.ikm.komet.framework.propsheet.editor;

import javafx.beans.property.StringProperty;
import javafx.scene.control.PasswordField;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.editor.AbstractPropertyEditor;

public class PasswordEditor extends AbstractPropertyEditor<String, PasswordField> {
    public PasswordEditor(PropertySheet.Item property) {
        super(property, new PasswordField());
    }

    public PasswordEditor(PropertySheet.Item property, PasswordField control, boolean readonly) {
        super(property, new PasswordField(), readonly);
    }

    @Override
    protected StringProperty getObservableValue() {
        return getEditor().textProperty();
    }

    @Override
    public void setValue(String value) {
        getEditor().setText(value);
    }

}
