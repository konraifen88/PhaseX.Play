package components;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.cache.Cache;
import play.libs.Akka;
import play.libs.Json;
import play.mvc.WebSocket;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;

import java.util.HashMap;
import java.util.Map;

import static akka.pattern.Patterns.ask;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Main-Sources from
 * @author paullabis
 * @source https://github.com/paullabis/play-websockets-chat.git
 *
 * If everything works right this class was
 * adapted by Konraifen88 on 08.01.2016.
 * If it doesn't work I don't know who the hell wrote it.
 */
public class ChatRoom extends UntypedActor {

    Map<String, WebSocket.Out<JsonNode>> members = new HashMap<>();

    public static void join(final String roomName, final String username, WebSocket.In<JsonNode> in,
                            WebSocket.Out<JsonNode> out) throws Exception {

        String chatRoomName = roomName == null || roomName.isEmpty() ? "open" : roomName;

        ActorRef actorRef;
        Object chatRoomCached = Cache.get(chatRoomName);
        if (chatRoomCached == null) {
            actorRef = Akka.system().actorOf(Props.create(ChatRoom.class));
            Cache.set(chatRoomName, actorRef);
        } else {
            actorRef = (ActorRef) chatRoomCached;
        }

        final ActorRef defaultRoom = actorRef;

        // Send the Join message to the room
        String result = (String) Await
                .result(ask(defaultRoom, new Join(username, out), 1000), Duration.create(1, SECONDS));

        if ("OK".equals(result)) {

            // For each event received on the socket,
            in.onMessage(event -> {

                // Send a Talk message to the room.
                defaultRoom.tell(new Talk(username, event.get("text").asText()), null);

            });

            // When the socket is closed.
            in.onClose(() -> {

                // Send a Quit message to the room.
                defaultRoom.tell(new Quit(username), null);

            });

        } else {

            // Cannot connect, create a Json error.
            ObjectNode error = Json.newObject();
            error.put("error", result);

            // Send the error to the socket.
            out.write(error);

        }

    }

    // Members of this room.

    public void onReceive(Object message) throws Exception {
        System.out.println(message);
        if (message instanceof Join) {

            // Received a Join message
            Join join = (Join) message;

            // Check if this username is free.
            if (members.containsKey(join.username)) {
                getSender().tell("This username is already used", self());
            } else if (members.size() < 2) {
                //Join Room and start game
                members.put(join.username, join.channel);
                notifyAll("join", join.username, "has entered the room");
                getSender().tell("OK", self());
            } else {
                getSender().tell("Game already full! Please try another Lobby.", self());
            }

        } else if (message instanceof Talk) {

            // Received a Talk message
            Talk talk = (Talk) message;

            notifyAll("talk", talk.username, talk.text);

        } else if (message instanceof Quit) {

            // Received a Quit message
            Quit quit = (Quit) message;

            members.remove(quit.username);

            notifyAll("quit", quit.username, "has left the room");

        } else {
            System.out.println("ERROR!");
            unhandled(message);
        }

    }

    // Send a Json event to all members
    public void notifyAll(String kind, String user, String text) {
        for (WebSocket.Out<JsonNode> channel : members.values()) {
            ObjectNode event = Json.newObject();
            event.put("kind", kind);
            event.put("user", user);
            event.put("message", text);

            ArrayNode m = event.putArray("members");
            members.keySet().forEach(m::add);

            channel.write(event);
        }
    }

    // -- Messages

    public static class Join {

        final String username;
        final WebSocket.Out<JsonNode> channel;

        public Join(String username, WebSocket.Out<JsonNode> channel) {
            this.username = username;
            this.channel = channel;
        }
    }

    public static class Talk {

        final String username;
        final String text;

        public Talk(String username, String text) {
            this.username = username;
            this.text = text;
        }

    }

    public static class Quit {

        final String username;

        public Quit(String username) {
            this.username = username;
        }

    }
}
