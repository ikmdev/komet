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
package dev.ikm.komet.framework.context;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.common.service.PluggableService;
import dev.ikm.tinkar.terms.EntityFacade;

import java.util.ArrayList;
import java.util.List;

/**
 * See also: MenuSupplierForFocusedEntity TODO:
 */
public interface AddToContextMenu {
    void addToContextMenu(Control controlWithContext, ContextMenu contextMenu, ViewProperties viewProperties,
                          ObservableValue<EntityFacade> conceptFocusProperty,
                          SimpleIntegerProperty selectionIndexProperty,
                          Runnable unlink);

    /**
     * The rule-driven and plugin-contributed context-menu providers: the Evrete
     * rule engine (as {@link EvreteRulesMenuProvider}) plus any discovered via
     * {@code PluggableService.load(AddToContextMenu.class)}. A component menu —
     * for example an identicon menu — invokes each so rule- and plugin-contributed
     * items appear without that menu knowing about them. The always-on copy
     * built-ins ({@link AddToContextMenuSimple}) are added separately by callers
     * that want them.
     *
     * @return the providers to invoke when building a component context menu
     */
    static AddToContextMenu[] providers() {
        List<AddToContextMenu> providers = new ArrayList<>();
        providers.add(new EvreteRulesMenuProvider());
        PluggableService.load(AddToContextMenu.class).forEach(providers::add);
        return providers.toArray(new AddToContextMenu[0]);
    }
}
