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
package dev.ikm.komet.layout.controls;

import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.collections.FXCollections;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Locks in the override-detection semantics of {@link FilterOptions.Option#equals} (ike-issues#710).
 *
 * <p>The View Options popup decides whether a coordinate dimension is overridden by comparing the
 * nodeView's projected {@code Option} to the inherited-parent baseline {@code Option}. The comparison must
 * be <b>order-sensitive for the ordered preference lists</b> (description-type and dialect — "first match
 * wins") and order-<b>in</b>sensitive for the set-like options (status, module, …). If description-type
 * were compared order-insensitively, an order-only override (FQN-first vs Regular-first) would be invisible:
 * no override dot, REVERT disabled, even though the card renders the reordered preference.
 *
 * <p><b>What this guards:</b> {@link FilterOptions} compares ordered preference lists (description-type,
 * dialect, the language-coordinate list) with {@code equalInOrder}, and the set-like dimensions (status,
 * module, …) with {@code equalIgnoringOrder}. Those two helpers replaced a pair whose names were inverted
 * relative to their behavior — the cause of #710. These tests fail if description-type ever silently
 * becomes order-insensitive again (or a set-like dimension order-sensitive).
 */
class FilterOptionsOrderSensitivityTest {

    private static final EnumSet<FilterOptions.Option.BUTTON> NONE =
            EnumSet.of(FilterOptions.Option.BUTTON.NONE);

    private static FilterOptions.Option<EntityFacade> descriptionType(EntityFacade... preferenceOrder) {
        List<EntityFacade> available = List.of(TinkarTerm.REGULAR_NAME_DESCRIPTION_TYPE,
                TinkarTerm.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE);
        return new FilterOptions.Option<>(FilterOptions.OPTION_ITEM.DESCRIPTION_TYPE, "description.option.title",
                new ArrayList<>(available), FXCollections.observableArrayList(preferenceOrder), null,
                true, false, NONE, false);
    }

    private static FilterOptions.Option<State> status(State... selection) {
        List<State> available = List.of(State.ACTIVE, State.INACTIVE);
        return new FilterOptions.Option<>(FilterOptions.OPTION_ITEM.STATUS, "status.title",
                new ArrayList<>(available), FXCollections.observableArrayList(selection),
                FXCollections.observableArrayList(), true, false, NONE, false);
    }

    @Test
    void descriptionTypeReorderIsNotEqual() {
        FilterOptions.Option<EntityFacade> fqnFirst = descriptionType(
                TinkarTerm.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE, TinkarTerm.REGULAR_NAME_DESCRIPTION_TYPE);
        FilterOptions.Option<EntityFacade> regularFirst = descriptionType(
                TinkarTerm.REGULAR_NAME_DESCRIPTION_TYPE, TinkarTerm.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE);
        assertNotEquals(fqnFirst, regularFirst,
                "description-type is an ordered preference list; a reorder must register as a different Option "
                        + "(else the override dot/REVERT never appear — ike-issues#710)");
    }

    @Test
    void descriptionTypeSameOrderIsEqual() {
        FilterOptions.Option<EntityFacade> a = descriptionType(
                TinkarTerm.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE, TinkarTerm.REGULAR_NAME_DESCRIPTION_TYPE);
        FilterOptions.Option<EntityFacade> b = descriptionType(
                TinkarTerm.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE, TinkarTerm.REGULAR_NAME_DESCRIPTION_TYPE);
        assertEquals(a, b, "identical description-type preference orders must compare equal "
                + "(so an unchanged dimension shows no spurious override)");
    }

    @Test
    void setLikeStatusIsOrderInsensitive() {
        FilterOptions.Option<State> activeInactive = status(State.ACTIVE, State.INACTIVE);
        FilterOptions.Option<State> inactiveActive = status(State.INACTIVE, State.ACTIVE);
        assertEquals(activeInactive, inactiveActive,
                "status is a set, not an ordered list; its membership comparison must ignore order");
    }
}
