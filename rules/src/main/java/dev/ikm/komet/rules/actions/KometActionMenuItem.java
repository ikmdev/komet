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
package dev.ikm.komet.rules.actions;

import dev.ikm.komet.actions.KometActionOffer;
import dev.ikm.komet.actions.KometActionResult;
import dev.ikm.komet.framework.Dialogs;
import dev.ikm.komet.terms.KometConcept;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.coordinate.edit.EditCoordinate;
import dev.ikm.tinkar.coordinate.edit.EditCoordinateRecord;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.event.ActionEvent;

/**
 * The menu presentation of a {@link KometActionOffer} (ike-lab-documents topic
 * {@code dev-komet-actions}): a menu item named after the offered action's concept that performs
 * the offer off the JavaFX thread. A completed run needs no word; a refusal is reported as
 * information and a failure as an error, both with the result's message. The action itself knows
 * nothing of menus; this adapter is the only part of it that extends ControlsFX's {@code Action}.
 */
public final class KometActionMenuItem extends AbstractActionSuggested {

    private final KometActionOffer offer;

    /**
     * @param offer          the offer this item performs
     * @param viewCalculator the view the subject is read in
     * @param editCoordinate the edit coordinate a change is written under
     */
    public KometActionMenuItem(KometActionOffer offer, ViewCalculator viewCalculator, EditCoordinate editCoordinate) {
        super(viewCalculator.getPreferredDescriptionTextWithFallbackOrNid(offer.action().concept().nid()),
                viewCalculator, editCoordinate);
        this.offer = offer;
        viewCalculator.getDefinitionDescriptionText(offer.action().concept().nid()).ifPresent(this::setLongText);
    }

    /**
     * @return the offer this item performs
     */
    public KometActionOffer offer() {
        return offer;
    }

    @Override
    public void doAction(ActionEvent actionEvent, EditCoordinateRecord editCoordinate) {
        TinkExecutor.threadPool().execute(() -> {
            KometActionResult result = offer.perform(viewCalculator, editCoordinate);
            if (EntityFacade.same(result.outcome(), KometConcept.ACTION_REFUSED)) {
                Dialogs.showInformationDialog(getText(), result.message());
            } else if (EntityFacade.same(result.outcome(), KometConcept.ACTION_FAILED)) {
                Dialogs.showErrorDialog(getText(), result.message(), "");
            }
        });
    }
}
