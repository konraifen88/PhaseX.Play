package controllers;

import components.ChatRoom;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import views.html.chatIndex;
import views.html.chatRoom;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * If everything works right this class was
 * created by Konraifen88 on 08.01.2016.
 * If it doesn't work I don't know who the hell wrote it.
 */
public class Chat extends Controller {

    /**
     * Display the home page.
     */
    public Result chatIndex() {
        return ok(chatIndex.render());
    }

    /**
     * Display the chat room.
     */
    public Result chatRoom(String username, String roomName) {
        if (username == null || username.trim().equals("")) {
            flash("error", "Please choose a valid username.");
            return redirect(controllers.routes.Chat.chatIndex());
        }

        return ok(chatRoom.render(username, roomName));
    }

    public Result chatRoomJs(String username, final String roomName) {

        return ok(views.js.chatRoom.render(username, roomName));
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
                    ex.printStackTrace();
                }
            }
        };
    }

}
