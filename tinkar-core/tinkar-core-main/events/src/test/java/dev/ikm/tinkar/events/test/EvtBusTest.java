package dev.ikm.tinkar.events.test;

import dev.ikm.tinkar.events.Evt;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.tinkar.events.EvtType;
import dev.ikm.tinkar.events.Subscriber;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EvtBusTest {

    private static final Logger LOG = LoggerFactory.getLogger(EvtBusTest.class);

    @Test
    public void testPubSub() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        Subscriber<MyEvent> subscriber = evt -> {
            LOG.info("Event received " + evt.getEventType());
            latch.countDown();
        };

        EvtBusFactory.getDefaultEvtBus().subscribe("topic", MyEvent.class, subscriber);
        EvtBusFactory.getDefaultEvtBus().publish("topic", new MyEvent(this, Evt.ANY));

        latch.await(5, TimeUnit.SECONDS);
        assertEquals(0, latch.getCount());
    }

    static class MyEvent extends Evt {
        MyEvent(Object source, EvtType<?> eventType) {
            super(source, eventType);
        }
    }

}
