package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import components.ChatRoom;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import securesocial.core.RuntimeEnvironment;
import views.html.lobby.chatRoom;

import java.util.Map;

/**
 * Main-Sources from
 *
 * @author paullabis
 * @source https://github.com/paullabis/play-websockets-chat.git
 * <p>
 * If everything works right this class was
 * adapted by Konraifen88 on 08.01.2016.
 * If it doesn't work I don't know who the hell wrote it.
 */
public class Chat extends Controller {

    private static Map<String, Integer> availableLobbies = Application.availableLobbies;

    /**
     * Display the chat room.
     */
    public Result chatRoom(String username, String roomName, RuntimeEnvironment env) {
        if (username == null || username.trim().equals("")) {
            flash("error", "Please choose a valid username.");
            return redirect(routes.Application.getMainPage());
        }
        return ok(chatRoom.render(username, roomName, env));
    }

    public static void playerLeftChat(String roomName) {
        System.out.println("Player in Room " + roomName + " Left");
        Integer i = availableLobbies.get(roomName);
        if (i != null) {
            if (i == 1) {
                availableLobbies.remove(roomName);
            } else {
                availableLobbies.put(roomName, --i);
            }
        } else {
            System.err.println("You tried to get an nonexistent room");
        }

    }

    /*
    public Result chatRoomWithoutEnv(String username, String roomName) {
        return chatRoom(username, roomName, null);
    }*/

    public Result chatRoomJs(String username, final String roomName) {

        return ok(views.js.lobby.chatRoom.render(username, roomName));
    }

    /**
     * Handle the chat websocket.
     */
    public WebSocket<JsonNode> chat(final String username, final String roomName) {
        return new WebSocket<JsonNode>() {
            public void onReady(WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out) {
                try {
                    ChatRoom.join(roomName, username, in, out);
                } catch (Exception ex) {
                    //ex.printStackTrace();
                }

            }
        };
    }

}
