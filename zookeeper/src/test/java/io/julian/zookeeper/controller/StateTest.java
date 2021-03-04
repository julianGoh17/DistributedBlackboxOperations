package io.julian.zookeeper.controller;

import io.julian.zookeeper.models.Proposal;
import io.julian.zookeeper.models.ProposalTest;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class StateTest {
    private Vertx vertx;

    @Before
    public void before() {
        this.vertx = Vertx.vertx();
    }

    @Test
    public void TestAddProposalCompletesSuccessfully(final TestContext context) {
        State state = new State(vertx);

        Assert.assertEquals(0, state.getHistory().size());
        Async async = context.async();
        state.addProposal(Proposal.mapFrom(ProposalTest.JSON))
            .onComplete(context.asyncAssertSuccess(res -> {
                Assert.assertEquals(1, state.getHistory().size());
                Assert.assertEquals(ProposalTest.JSON.encodePrettily(), state.getHistory().get(0).toJson().encodePrettily());
                async.complete();
            }));
        async.awaitSuccess();
    }

    @Test
    public void TestSetLastAcceptedIndex() {
        State state = new State(vertx);
        Assert.assertEquals(0, state.getLastAcceptedIndex());

        int higher = 2;
        int lower = 1;
        state.setLastAcceptedIndex(higher);
        Assert.assertEquals(higher, state.getLastAcceptedIndex());

        state.setLastAcceptedIndex(lower);
        Assert.assertEquals(higher, state.getLastAcceptedIndex());
    }

    @Test
    public void TestDoesExistOutstandingTransaction(final TestContext context) {
        State state = new State(vertx);
        Proposal smaller = Proposal.mapFrom(ProposalTest.JSON);
        Proposal bigger = Proposal.mapFrom(ProposalTest.JSON);

        int offset = 100;
        bigger.getTransactionId().setCounter(ProposalTest.COUNTER + offset);

        Async async = context.async();
        state.addProposal(smaller)
            .compose(v -> state.addProposal(bigger))
            .onComplete(context.asyncAssertSuccess(v -> {
                async.complete();
            }));

        async.awaitSuccess();

        Assert.assertFalse(state.doesExistOutstandingTransaction(ProposalTest.COUNTER));
        Assert.assertTrue(state.doesExistOutstandingTransaction(ProposalTest.COUNTER + offset));

        state.setLastAcceptedIndex(2);
        Assert.assertFalse(state.doesExistOutstandingTransaction(ProposalTest.COUNTER + offset));
    }

    @After
    public void after() {
        this.vertx.close();
    }
}
