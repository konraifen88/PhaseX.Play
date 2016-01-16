package actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;

/**
 * Main-Sources from
 * @author paullabis
 * @source https://github.com/paullabis/play-websockets-chat.git
 *
 * If everything works right this class was
 * adapted by Konraifen88 on 08.01.2016.
 * If it doesn't work I don't know who the hell wrote it.
 */

public class MyWebSocketActor extends UntypedActor {

    private final ActorRef out;

    public MyWebSocketActor(ActorRef out) {
        this.out = out;
    }

    public static Props props(ActorRef out) {
        return Props.create(MyWebSocketActor.class, out);
    }

    public void onReceive(Object message) throws Exception {
        if (message instanceof String) {
            out.tell("I received your message: " + message, self());
        }
    }

}
