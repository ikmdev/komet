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
import org.junit.jupiter.api.Disabled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static dev.ikm.komet.kview.state.ConceptDetailsState.BF_NO_FQN;
import static dev.ikm.komet.kview.state.ConceptDetailsState.EDITING_OTN_NO_FQN;
import static org.carlfx.axonic.tools.DiagramHelper.toPlantUml;

@DisplayName("StateBuilder Test")
public class ConceptDetailsStateMachineTest {
    private static final Logger LOG = LoggerFactory.getLogger(ConceptDetailsStateMachineTest.class);

    @Test
    @Disabled("Java 23")
    @DisplayName("Test Concept Details state machine UI flow happy path.")
    void conceptDetailsStateMachineFlowTest() {
        StateMachine sm = StateMachine.create(new ConceptDetailsPattern());

        String expected = """
                name: initial from: INITIAL to: BF_NO_FQN
                name: toggle press from: BF_NO_FQN to: BF_NO_FQN
                name: edit Otn from: BF_NO_FQN to: EDITING_OTN_NO_FQN
                name: add Fqn from: BF_NO_FQN to: ADDING_FQN
                name: add Otn from: BF_NO_FQN to: ADDING_OTN_NO_FQN
                name: done from: ADDING_OTN_NO_FQN to: BF_NO_FQN
                name: cancel from: ADDING_OTN_NO_FQN to: BF_NO_FQN
                name: cancel from: ADDING_FQN to: BF_NO_FQN
                name: done from: ADDING_FQN to: BF_WITH_FQN
                name: add Otn from: BF_WITH_FQN to: ADDING_OTN_WITH_FQN
                name: cancel from: ADDING_OTN_WITH_FQN to: BF_WITH_FQN
                name: done from: ADDING_OTN_WITH_FQN to: BF_WITH_FQN
                name: cancel from: EDITING_FQN to: BF_WITH_FQN
                name: done from: EDITING_FQN to: BF_WITH_FQN
                name: edit fqn from: BF_WITH_FQN to: EDITING_FQN
                name: done from: EDITING_OTN_NO_FQN to: BF_NO_FQN
                name: cancel from: EDITING_OTN_NO_FQN to: BF_NO_FQN
                name: edit Otn from: BF_WITH_FQN to: EDITING_OTN_WITH_FQN
                name: done from: EDITING_OTN_WITH_FQN to: BF_WITH_FQN
                name: cancel from: EDITING_OTN_WITH_FQN to: BF_WITH_FQN
                """;
        StringBuilder sb = new StringBuilder();
        sm.getStatePattern().transitions().forEach(transition -> {
            sb.append("name: %s from: %s to: %s\n".formatted(transition.name(), transition.fromState(), transition.toState()));
        });
        System.out.println(sb);
        Assertions.assertEquals(expected, sb.toString(), "error transitions don't match.");
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
