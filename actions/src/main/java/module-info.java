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

/**
 * Komet's actions, free of JavaFX (dev-komet-actions): {@link dev.ikm.komet.actions.KometAction},
 * bound to the action concept it implements, with its input, result and offer, and the actions
 * Komet implements itself. A plugin contributes actions with
 * {@code provides dev.ikm.komet.actions.KometAction with ...}.
 */
module dev.ikm.komet.actions {
    requires transitive dev.ikm.tinkar.entity;
    requires transitive dev.ikm.tinkar.terms;
    requires transitive dev.ikm.komet.terms;
    requires dev.ikm.tinkar.common;
    requires org.eclipse.collections.api;

    exports dev.ikm.komet.actions;

    uses dev.ikm.komet.actions.KometAction;
    provides dev.ikm.komet.actions.KometAction
            with dev.ikm.komet.actions.component.ActivateComponent,
                 dev.ikm.komet.actions.component.InactivateComponent;
}
