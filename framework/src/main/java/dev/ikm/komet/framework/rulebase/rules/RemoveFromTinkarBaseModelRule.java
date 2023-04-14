package dev.ikm.komet.framework.rulebase.rules;

import dev.ikm.komet.framework.performance.Observation;
import dev.ikm.komet.framework.performance.Statement;
import dev.ikm.komet.framework.performance.Topic;
import dev.ikm.komet.framework.rulebase.RuntimeRule;
import dev.ikm.komet.framework.rulebase.actions.AbstractActionGenerated;
import dev.ikm.komet.framework.rulebase.actions.RemoveFromTinkarBaseModelActionGenerated;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.edit.EditCoordinate;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.ConceptEntityVersion;
import dev.ikm.tinkar.entity.EntityVersion;

import java.util.Optional;

import static dev.ikm.tinkar.terms.TinkarTerm.TINKAR_BASE_MODEL_COMPONENT_PATTERN;

@RuntimeRule
public class RemoveFromTinkarBaseModelRule extends AbstractComponentRule {
    @Override
    public String name() {
        return "Remove from Tinkar base model";
    }

    @Override
    public String description() {
        return "Remove concept as a mandatory part of the Tinkar base model. ";
    }

    @Override
    public Topic topicToProcess() {
        return Topic.COMPONENT_FOCUSED;
    }

    @Override
    boolean conditionsMet(Statement statement, ViewCalculator viewCalculator) {
        if (statement.subject() instanceof ConceptEntityVersion conceptVersion) {
            int[] semanticNidsForComponent = PrimitiveData.get().semanticNidsForComponentOfPattern(conceptVersion.nid(), TINKAR_BASE_MODEL_COMPONENT_PATTERN.nid());
            // case 1: never a member
            if (semanticNidsForComponent.length == 0) {
                return false;
            }
            // case 2: a member, and maybe active.
            Latest<EntityVersion> latestSemanticVersion = viewCalculator.latest(semanticNidsForComponent[0]);
            if (latestSemanticVersion.isPresent() && latestSemanticVersion.get().active()) {
                return true;
            }
        }
        return false;
    }

    @Override
    Optional<AbstractActionGenerated> makeAction(Statement statement, ViewCalculator viewCalculator, EditCoordinate editCoordinate) {
        if (statement instanceof Observation observation && observation.isPresent() && statement.subject() instanceof ConceptEntityVersion conceptEntityVersion) {
            return Optional.of(new RemoveFromTinkarBaseModelActionGenerated(conceptEntityVersion, viewCalculator, editCoordinate));
        }
        return Optional.empty();
    }
}