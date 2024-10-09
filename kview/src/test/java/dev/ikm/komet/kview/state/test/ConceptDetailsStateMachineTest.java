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
package dev.ikm.komet.kview.state.test;

import dev.ikm.komet.kview.state.pattern.ConceptDetailsPattern;
import org.carlfx.axonic.StateMachine;
import org.carlfx.axonic.tools.StateMachineCLI;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static dev.ikm.komet.kview.state.ConceptDetailsState.BF_NO_FQN;
import static dev.ikm.komet.kview.state.ConceptDetailsState.EDITING_OTN_NO_FQN;
import static org.carlfx.axonic.tools.DiagramHelper.toPlantUml;

@DisplayName("StateBuilder Test")
public class ConceptDetailsStateMachineTest {
    private static final Logger LOG = LoggerFactory.getLogger(ConceptDetailsStateMachineTest.class);

    @Test
    @DisplayName("Test Concept Details state machine UI flow happy path.")
    void conceptDetailsStateMachineFlowTest() {
        StateMachine sm = StateMachine.create(new ConceptDetailsPattern());

        // test 1. see current state info
        debugInfo(sm);
        Assertions.assertEquals(BF_NO_FQN, sm.currentState());

        // test 2. transition with to edit Otn
        sm.t("edit Otn");
        debugInfo(sm);
        Assertions.assertEquals(EDITING_OTN_NO_FQN, sm.currentState());

        System.out.println(toPlantUml(sm));
    }

    private void debugInfo(StateMachine stateMachine) {
        LOG.info(" Chose transition: " + stateMachine.currentTransition());
        LOG.info("    Current state: " + stateMachine.currentState());
        LOG.info("       Prev state: " + stateMachine.previousState());
        LOG.info("Avail transitions: " + stateMachine.outgoingTransitions());
        LOG.info("-----------------------------------------------------");
    }

    /**
     * An interactive CLI to test your UI flow (state pattern)
     * @param args
     */
    public static void main(String[] args) {
        StateMachine sm = StateMachine.create(new ConceptDetailsPattern());
        StateMachineCLI.beginConsoleSession(sm);
    }
}
