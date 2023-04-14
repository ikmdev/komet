package dev.ikm.komet.framework.performance.impl;

import dev.ikm.komet.framework.performance.Request;
import dev.ikm.komet.framework.performance.Topic;

public record RequestRecord(Topic topic, Object subject) implements Request {
    public static RequestRecord make(Topic topic, Object subject) {
        return new RequestRecord(topic, subject);
    }
}
