package dev.ikm.komet.framework.performance.impl;

import dev.ikm.komet.framework.performance.Measure;
import dev.ikm.komet.framework.performance.Observation;
import dev.ikm.komet.framework.performance.Topic;

public record ObservationRecord(Topic topic, Object subject, Measure value) implements Observation {
}
