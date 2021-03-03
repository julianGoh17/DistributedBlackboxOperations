package io.julian.zookeeper.tools;

import io.julian.server.models.HTTPRequest;
import io.julian.server.models.control.ClientMessage;
import io.julian.zookeeper.controller.State;
import io.julian.zookeeper.models.Proposal;
import io.julian.zookeeper.models.Zxid;
import io.vertx.core.AbstractVerticle;

// TODO: Maybe delete?
public class AddVerticle extends AbstractVerticle {
    public final static HTTPRequest REQUEST = HTTPRequest.POST;
    public final static int REPEATS = 5;
    public final static float EPOCH = 12345;

    private final State state;
    private final int postfix;

    public AddVerticle(final State state, final int postfix) {
        this.state = state;
        this.postfix = postfix;
    }

    @Override
    public void start() {
        vertx.eventBus().consumer(formatAddress(), v -> addToState());
    }

    public String formatAddress() {
        return String.format("add-%d", postfix);
    }

    public void addToState() {
        for (int i = 0; i < REPEATS; i++) {
            this.state.addProposal(new Proposal(
                new ClientMessage(REQUEST, new Counter(i).toJson(), Integer.toString(i)),
                new Zxid(EPOCH, i)
            ));
        }
    }
}
