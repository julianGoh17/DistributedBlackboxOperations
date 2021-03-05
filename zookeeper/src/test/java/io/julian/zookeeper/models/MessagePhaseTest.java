package io.julian.zookeeper.models;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class MessagePhaseTest {
    private final List<String> strings = Arrays.asList("ack", "commit", "error");
    private final List<MessagePhase> phases = Arrays.asList(MessagePhase.ACK, MessagePhase.COMMIT, MessagePhase.ERROR);

    @Test
    public void TestToValue() {
        for (int i = 0; i < strings.size(); i++) {
            Assert.assertEquals(strings.get(i), phases.get(i).toValue());
        }
    }

    @Test
    public void TestFromValue() {
        for (int i = 0; i < strings.size(); i++) {
            Assert.assertEquals(phases.get(i), MessagePhase.fromValue(strings.get(i)));
        }
    }
}
