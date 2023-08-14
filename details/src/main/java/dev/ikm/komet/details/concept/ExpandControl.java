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
package dev.ikm.komet.details.concept;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.Border;
import dev.ikm.komet.framework.graphics.Icon;

/**
 * 
 */
public final class ExpandControl
        extends Button {

    DisclosureState disclosureState = DisclosureState.CLICK_TO_OPEN;
    SimpleObjectProperty<ExpandAction> expandActionProperty = new SimpleObjectProperty<>(ExpandAction.HIDE_CHILDREN);

    //~--- constructors --------------------------------------------------------

    public ExpandControl() {
        super("", Icon.TAXONOMY_CLICK_TO_OPEN.makeIcon());
        setBorder(Border.EMPTY);
        setCenterShape(true);
        setAlignment(Pos.CENTER);
        setMinSize(25, 25);
        setMaxSize(25, 25);
        setPrefSize(25, 25);
        getStyleClass().setAll("expand-control");
    }

    //~--- methods -------------------------------------------------------------

    @Override
    public void fire() {
        if (isArmed()) {
            switch (disclosureState) {
                case CLICK_TO_CLOSE:
                    disclosureState = DisclosureState.CLICK_TO_OPEN;
                    setGraphic(Icon.TAXONOMY_CLICK_TO_OPEN.makeIcon());
                    expandActionProperty.set(ExpandAction.HIDE_CHILDREN);
                    break;

                case CLICK_TO_OPEN:
                    disclosureState = DisclosureState.CLICK_TO_CLOSE;
                    setGraphic(Icon.TAXONOMY_CLICK_TO_CLOSE.makeIcon());
                    expandActionProperty.set(ExpandAction.SHOW_CHILDREN);
                    break;

                default:
            }
        }

        super.fire();
    }

    public ReadOnlyObjectProperty<ExpandAction> expandActionProperty() {
        return this.expandActionProperty;
    }

    //~--- get methods ---------------------------------------------------------

    public ExpandAction getExpandAction() {
        return expandActionProperty.get();
    }

    //~--- set methods ---------------------------------------------------------

    public void setExpandAction(ExpandAction expandAction) {
        this.expandActionProperty.set(expandAction);

        switch (expandAction) {
            case HIDE_CHILDREN:
                disclosureState = DisclosureState.CLICK_TO_OPEN;
                setGraphic(Icon.TAXONOMY_CLICK_TO_OPEN.makeIcon());
                break;

            case SHOW_CHILDREN:
                disclosureState = DisclosureState.CLICK_TO_CLOSE;
                setGraphic(Icon.TAXONOMY_CLICK_TO_CLOSE.makeIcon());
                break;

            default:
                throw new UnsupportedOperationException("can't handle action: " + expandAction);
        }
    }
}

