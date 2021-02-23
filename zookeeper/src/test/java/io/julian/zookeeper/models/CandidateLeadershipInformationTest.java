package io.julian.zookeeper.models;

import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

public class CandidateLeadershipInformationTest {
    public static final String HOST = "random-host@string";
    public static final int PORT = 12345;
    public static final long CANDIDATE_NUMBER = 1231212323L;

    public static final JsonObject JSON = new JsonObject()
        .put(CandidateLeadershipInformation.HOST_KEY, HOST)
        .put(CandidateLeadershipInformation.PORT_KEY, PORT)
        .put(CandidateLeadershipInformation.CANDIDATE_NUMBER_KEY, CANDIDATE_NUMBER);

    @Test
    public void TestCanMapFromJson() {
        CandidateLeadershipInformation info = JSON.mapTo(CandidateLeadershipInformation.class);

        Assert.assertEquals(HOST, info.getHost());
        Assert.assertEquals(PORT, info.getPort());
        Assert.assertEquals(CANDIDATE_NUMBER, info.getCandidateNumber());
    }

    @Test
    public void TestCanMapToJson() {
        JsonObject mapped = new CandidateLeadershipInformation(HOST, PORT, CANDIDATE_NUMBER).toJson();

        Assert.assertEquals(JSON.encodePrettily(), mapped.encodePrettily());
    }
}
