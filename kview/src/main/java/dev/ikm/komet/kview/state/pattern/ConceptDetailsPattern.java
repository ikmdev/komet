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
package dev.ikm.komet.kview.state.pattern;

import org.carlfx.axonic.StatePattern;

import static dev.ikm.komet.kview.state.ConceptDetailsState.*;

public class ConceptDetailsPattern extends StatePattern {
    public ConceptDetailsPattern() {
        this.initial(BF_NO_FQN)
            .t("toggle press")
            .t("edit Otn", BF_NO_FQN, EDITING_OTN_NO_FQN)
            .t("add Fqn", BF_NO_FQN, ADDING_FQN)
            .t("add Otn", BF_NO_FQN, ADDING_OTN_NO_FQN)
            .t("done", ADDING_OTN_NO_FQN, BF_NO_FQN)
            .t("cancel", ADDING_OTN_NO_FQN, BF_NO_FQN)
            .t("cancel", ADDING_FQN, BF_NO_FQN)
            .t("done", ADDING_FQN, BF_WITH_FQN)
            .t("add Otn", ADDING_OTN_WITH_FQN)
            .t("cancel", ADDING_OTN_WITH_FQN, BF_WITH_FQN)
            .t("done", ADDING_OTN_WITH_FQN, BF_WITH_FQN)
            .t("cancel", EDITING_FQN, BF_WITH_FQN)
            .t("done", EDITING_FQN, BF_WITH_FQN)
            .t("edit fqn", BF_WITH_FQN, EDITING_FQN)
            .t("done", EDITING_OTN_NO_FQN, BF_NO_FQN)
            .t("cancel", EDITING_OTN_NO_FQN, BF_NO_FQN)
            .t("edit Otn", BF_WITH_FQN, EDITING_OTN_WITH_FQN)
            .t("done", EDITING_OTN_WITH_FQN, BF_WITH_FQN)
            .t("cancel", EDITING_OTN_WITH_FQN, BF_WITH_FQN)
        ;
    }
}
