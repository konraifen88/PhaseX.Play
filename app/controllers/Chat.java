package controllers;

import components.ChatRoom;
import components.Players;
import controller.UIController;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import securesocial.core.java.SecureSocial;
import securesocial.core.java.SecuredAction;
import service.DemoUser;
import views.html.chatIndex;
import views.html.chatRoom;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * If everything works right this class was
 * created by Konraifen88 on 08.01.2016.
 * If it doesn't work I don't know who the hell wrote it.
 */
public class Chat extends Controller {


    public static Map<String,WUIController> gameControllerMap;
    public static Map<String,Players> roomPlayerMap;

    public static Semaphore createGameSem;

    public Chat(){
        gameControllerMap = new HashMap<>();
        roomPlayerMap = new HashMap<>();
        createGameSem = new Semaphore(1);
    }
    /**
     * Display the home page.
     */
    public Result chatIndex() {
        return ok(chatIndex.render());
    }

    /**
     * Display the chat room.
     */
    @SecuredAction
    public synchronized Result chatRoom(DemoUser user, String roomName) {
        try {
            try {
                createGameSem.acquire();
                String username = user.main.fullName().get();
                if (username.isEmpty())
                    username = user.main.userId();
                if (username == null || username.trim().equals("")) {
                    flash("error", "Please choose a valid username.");
                    return redirect(controllers.routes.Chat.chatIndex());
                }
                if (roomPlayerMap.containsKey(roomName)){
                    roomPlayerMap.get(roomName).addPlayer2(user);
                    return ok(chatRoom.render(username, roomName, 1));
                } else {
                    Players players = new Players(user);
                    roomPlayerMap.put(roomName, players);
                    UIController controller = new controller.impl.Controller(2);
                    WUIController wuiController = new WUIController(controller, user);
                    wuiController.start();
                    System.out.println("Mapping Room and Players");
                    gameControllerMap.put(roomName, wuiController);
                    return ok(chatRoom.render(username, roomName, 0));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } finally {
            createGameSem.release();
        }
        //Add failure page
        return ok();
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

    @SecuredAction
    public Result getJsonUpdate() {
        DemoUser player = (DemoUser) ctx().args.get(SecureSocial.USER_KEY);
        return ok(gameControllerMap.get(getRoomNameOfPlayer(player)).getJsonUpdate());
    }

    @SecuredAction
    public String getRoomNameOfPlayer(DemoUser player) {
        String roomName = "";
        System.out.println(roomPlayerMap.toString());
        for(String room : roomPlayerMap.keySet()) {
            if(roomPlayerMap.get(room).getPlayer1().equals(player) || roomPlayerMap.get(room).getPlayer2().equals(player)) {
                roomName = room;
                break;
            }
        }
        System.out.println("got the room: " + roomName);
        return roomName;
    }

    @SecuredAction
    public Result getDrawHidden() {
        DemoUser player = (DemoUser) ctx().args.get(SecureSocial.USER_KEY);

        return ok(gameControllerMap.get(getRoomNameOfPlayer(player)).getDrawHidden(player));
    }

    @SecuredAction
    public Result getDrawOpen() {
        DemoUser player = (DemoUser) ctx().args.get(SecureSocial.USER_KEY);

        return ok(gameControllerMap.get(getRoomNameOfPlayer(player)).getDrawOpen(player));
    }

    @SecuredAction
    public Result discard(int index) {
        DemoUser player = (DemoUser) ctx().args.get(SecureSocial.USER_KEY);

        return ok(gameControllerMap.get(getRoomNameOfPlayer(player)).discard(index,player));
    }

    @SecuredAction
    public Result playPhase(String cards) {
        DemoUser player = (DemoUser) ctx().args.get(SecureSocial.USER_KEY);

        return ok(gameControllerMap.get(getRoomNameOfPlayer(player)).playPhase(cards,player));
    }

    @SecuredAction
    public Result addToPhase(int cardindex, int stackIndex) {
        DemoUser player = (DemoUser) ctx().args.get(SecureSocial.USER_KEY);

        return ok(gameControllerMap.get(getRoomNameOfPlayer(player)).addToPhase(cardindex,stackIndex,player));
    }

}
