/**
 * Copyright 2012-214 Jorge Aliss (jaliss at gmail dot com) - twitter: @jaliss
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package controllers;

import com.google.gson.Gson;
import com.google.inject.Inject;
import components.Players;
import controller.UIController;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import securesocial.core.BasicProfile;
import securesocial.core.RuntimeEnvironment;
import securesocial.core.java.SecureSocial;
import securesocial.core.java.SecuredAction;
import securesocial.core.java.UserAwareAction;
import service.DemoUser;
import views.html.common.homePage;
import views.html.common.instruction;
import views.html.gamefield.gamefield;
import views.html.gamefield.newGamefield;
import views.html.login.linkResult;

import java.util.*;
import java.util.concurrent.Semaphore;

public class Application extends Controller {
    public static Logger.ALogger logger = Logger.of("application.controllers.Application");
    public static Map<String, WUIController> gameControllerMap = new HashMap<>();
    public static Map<String, Players> roomPlayerMap = new HashMap<>();
    public static Map<String, Integer> availableLobbies = Collections.synchronizedMap(new HashMap<>());
    public static Map<DemoUser, WUI1PlayerController> singlePlayerMap = new HashMap<>();
    public static Semaphore createGameSem = new Semaphore(1);
    public static Semaphore socketSem = new Semaphore(1);
    public static Semaphore updateSem = new Semaphore(1);
    public static Semaphore lobbySem = new Semaphore(1);


    public static List<WebSocket.Out<String>> lobbySockets = new LinkedList<>();

    RuntimeEnvironment env;
    private Chat chat;


    @Inject()
    public Application(RuntimeEnvironment env) {
        this.env = env;
        chat = new Chat();
    }

    public static synchronized void addToAvailableLobbies(String roomName){
        try {
            lobbySem.acquire();
            System.out.println("adding to room" + roomName);
            if(availableLobbies.containsKey(roomName)) {
                int numberInRoom = availableLobbies.get(roomName);
                availableLobbies.put(roomName,++numberInRoom);
            } else {
                availableLobbies.put(roomName,1);
            }

            notifiyAllSocketLobbys();
        } catch (InterruptedException itre) {

        } finally {
            lobbySem.release();
        }

    }

    public static synchronized void deleteFromAvailableSockets(String roomName) {
        try {
            lobbySem.acquire();
            System.out.println("deleting from room" + roomName);
            int numberInRoom = availableLobbies.get(roomName);
            if(numberInRoom > 1) {
                availableLobbies.put(roomName, --numberInRoom);
            } else {
                availableLobbies.remove(roomName);
            }
            notifiyAllSocketLobbys();
        } catch (InterruptedException itre) {

        } finally {
            lobbySem.release();
        }

    }

    public static synchronized void deleteRoom(String roomName) {
        gameControllerMap.remove(roomName);
        roomPlayerMap.remove(roomName);
        deleteFromAvailableSockets(roomName);
    }

    public static void notifiyAllSocketLobbys() {
        Gson gson = new Gson();
        String lobbies = gson.toJson(availableLobbies);
        for (WebSocket.Out<String> out : lobbySockets) {
            try {
                out.write(lobbies);
            } catch (NullPointerException npe) {
                System.out.println("Socket no longer exists");
            }

        }
    }

    public static String getCurrentPlayerName() {
        DemoUser user = (DemoUser) ctx().args.get(SecureSocial.USER_KEY);
        if (user == null) {
            return null;
        }
        try {
            if (user.main.fullName().isDefined()) {
                return user.main.fullName().get();
            } else if (user.main.firstName().isDefined()) {
                return user.main.fullName().get();
            } else if (!user.main.userId().isEmpty()) {
                return user.main.userId();
            }
            return null;
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    @UserAwareAction
    public Result getMainPage() {
        return ok(homePage.render(getCurrentPlayerName(), env));
    }

    @UserAwareAction
    public Result getInstruction() {
        return ok(instruction.render(getCurrentPlayerName(), env));
    }

    @SecuredAction
    public Result goToChatRoom(String roomName) {
        DemoUser player = (DemoUser) ctx().args.get(SecureSocial.USER_KEY);
        if (player.isInGameOrLobby) {
            flash("error", "WTF? Why do you want to play 2 games at the same time?");
            return redirect("/");
        }
        addToAvailableLobbies(roomName);
        return chat.chatRoom(getCurrentPlayerName(), roomName, env);
    }

    public WebSocket<String> createLobbySocket() {
        return new WebSocket<String>() {
            @Override
            public void onReady(In<String> in, Out<String> out) {
                lobbySockets.add(out);
                in.onMessage((data)-> {
                    Gson gson = new Gson();
                    String lobbies = gson.toJson(availableLobbies);
                    out.write(lobbies);
                });

                in.onClose(()-> {
                    lobbySockets.remove(out);
                });
            }
        };
    }

    @SecuredAction
    public Result quitGame(String roomName) {
        System.out.println("Player left the game");
        availableLobbies.remove(roomName);
        notifiyAllSocketLobbys();
        gameControllerMap.remove(roomName);
        roomPlayerMap.remove(roomName);
        return ok();
    }

    public WebSocket<String> getLobbySocket() {
        WebSocket<String> socket = createLobbySocket();
        return socket;
    }

    @SecuredAction
    public Result getJsonUpdate() throws InterruptedException {
        System.out.println("Update called");
        try {
            updateSem.acquire();
            System.out.println("got Update Mutex");
            DemoUser player = (DemoUser) ctx().args.get(SecureSocial.USER_KEY);
            return ok(gameControllerMap.get(getRoomNameOfPlayer(player)).getJsonUpdate());
        } finally {
            System.out.println("release Update Mutex");
            updateSem.release();
        }
    }

    @SecuredAction
    public String getRoomNameOfPlayer(DemoUser player) {
        String roomName = "";
        System.out.println(roomPlayerMap.toString());
        for (String room : roomPlayerMap.keySet()) {
            if (roomPlayerMap.get(room).getPlayer1().equals(player) || roomPlayerMap.get(room).getPlayer2()
                    .equals(player)) {
                roomName = room;
                break;
            }
        }
        return roomName;
    }

    @SecuredAction
    public Result singlePlayer() {
        DemoUser player = (DemoUser) ctx().args.get(SecureSocial.USER_KEY);
        System.out.println("is defined)" + player);
        WUI1PlayerController wctrl = new WUI1PlayerController(new controller.impl.ActorController(),player, getCurrentPlayerName(),this);
        wctrl.start();
        singlePlayerMap.put(player,wctrl);
        return ok(gamefield.render(getCurrentPlayerName(), env));
    }

    @SecuredAction
    public synchronized WebSocket<String> getSinglePlayerSocket(String userID) {
        WUI1PlayerController ctrl = getSinglePlayerController(userID);
        return ctrl.getSocket();
    }

    private WUI1PlayerController getSinglePlayerController(String userID) {
        WUI1PlayerController ctrl = null;
        for(DemoUser user : singlePlayerMap.keySet()) {
            System.out.println("containing Name:" + user.main.userId());
            System.out.println("searching Name:" + userID);

            if (user.main.fullName().isDefined()) {
                if (userID.equals(user.main.fullName().get())) {
                    ctrl = singlePlayerMap.get(user);
                }
            } else {
                if (userID.equals(user.main.userId())) {
                    ctrl = singlePlayerMap.get(user);
                }

            }
        }
        return ctrl;
    }

    public void quitSinglePlayer(DemoUser player) {
        player.isInGameOrLobby = false;
    }

    @SecuredAction
    public synchronized Result createGame(String roomName) throws InterruptedException {
        try {
            System.out.println("Creating a new Game");
            createGameSem.acquire();
            System.out.println("Got createGame Mutex");

            if (gameControllerMap.containsKey(roomName)) {
                //System.out.println("Adding Player 2 to Game");
                Players players = roomPlayerMap.get(roomName);
                DemoUser newPlayer = (DemoUser) ctx().args.get(SecureSocial.USER_KEY);
                if (players.getPlayer1().equals(newPlayer)) {
                    System.out.println("redirecting Player1");
                    return ok(newGamefield.render(0, roomName, getCurrentPlayerName(), env));
                }
                try {
                    if (players.getPlayer2().equals(newPlayer)) {
                        return ok(newGamefield.render(1, roomName, getCurrentPlayerName(), env));
                    }
                    if (!players.getPlayer2().equals(newPlayer) && !players.getPlayer1().equals(newPlayer)) {
                        System.out.println("redirect third player to the homepage");
                        flash("error","You are not part of this game");
                        return redirect("/");
                    }
                } catch (NullPointerException npe) {
                    //doNothing
                }
                players.addPlayer2(newPlayer);
                System.out.println("Player 2 is: " + getCurrentPlayerName());
                gameControllerMap.get(roomName).setPlayer2(newPlayer, getCurrentPlayerName());
                addToAvailableLobbies(roomName);
                return ok(newGamefield.render(1, roomName, getCurrentPlayerName(), env));
            } else {

                System.out.println("Creating a new Game Controller");
                DemoUser player1 = (DemoUser) ctx().args.get(SecureSocial.USER_KEY);
                System.out.println("Player 1 is: " + getCurrentPlayerName());
                UIController controller = new controller.impl.ActorController();
                WUIController wuiController = new WUIController(controller, player1, roomName,this);
                wuiController.start(getCurrentPlayerName());
                System.out.println("Mapping Room and Players");
                gameControllerMap.put(roomName, wuiController);
                Players players = new Players(player1);
                roomPlayerMap.put(roomName, players);
                addToAvailableLobbies(roomName);
                System.out.println(roomPlayerMap.toString());
                System.out.println(gameControllerMap.toString());
                System.out.println("init game ready");

                return ok(newGamefield.render(0, roomName, getCurrentPlayerName(), env));
            }
        } finally {
            System.out.println("release create Game Mutex");
            createGameSem.release();
        }


    }

    @SecuredAction
    public Result getUserID() {
        DemoUser player = (DemoUser) ctx().args.get(SecureSocial.USER_KEY);
        return ok(player.main.userId());
    }

    @SecuredAction
    public synchronized WebSocket<String> getSocket(String userID) throws InterruptedException {
        try {
            System.out.println("Get Socket Called");
            socketSem.acquire();
            System.out.println("Got Socket Mutex");
            System.out.println(gameControllerMap);
            WUIController wuictrl = null;
            DemoUser player = null;
            for (WUIController wui : gameControllerMap.values()) {
                System.out.println("is Player1:" + wui.getPlayer1().main.userId().equals(userID));
                if (wui.getPlayer1().main.userId().equals(userID)) {
                    wuictrl = wui;
                    player = wuictrl.getPlayer1();
                    break;
                }
                try {
                    System.out.println("is Player2: " + wui.getPlayer2().main.userId().equals(userID));
                    if (wui.getPlayer2().main.userId().equals(userID)) {
                        wuictrl = wui;
                        player = wuictrl.getPlayer2();
                        break;
                    }
                } catch (NullPointerException npe) {
                    //player 2 is not in the game yet
                }
            }
            System.out.println(wuictrl.toString());
            return wuictrl.getSocket(player);
        } finally {
            System.out.println("Release Socket Mutex");
            socketSem.release();
        }
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

        return ok(gameControllerMap.get(getRoomNameOfPlayer(player)).discard(index, player));
    }

    @SecuredAction
    public Result playPhase(String cards) {
        DemoUser player = (DemoUser) ctx().args.get(SecureSocial.USER_KEY);

        return ok(gameControllerMap.get(getRoomNameOfPlayer(player)).playPhase(cards, player));
    }

    @SecuredAction
    public Result addToPhase(int cardindex, int stackIndex) {
        DemoUser player = (DemoUser) ctx().args.get(SecureSocial.USER_KEY);

        return ok(gameControllerMap.get(getRoomNameOfPlayer(player)).addToPhase(cardindex, stackIndex, player));
    }

    //TODO: Remove if no more needed
    @UserAwareAction
    public Result userAware() {
        DemoUser demoUser = (DemoUser) ctx().args.get(SecureSocial.USER_KEY);
        String userName;
        if (demoUser != null) {
            BasicProfile user = demoUser.main;
            if (user.firstName().isDefined()) {
                userName = user.firstName().get();
            } else if (user.fullName().isDefined()) {
                userName = user.fullName().get();
            } else {
                userName = "authenticated user";
            }
        } else {
            userName = "guest";
        }
        return ok("Hello " + userName + ", you are seeing a public page");
    }

    @SecuredAction
    public Result linkResult() {
        DemoUser current = (DemoUser) ctx().args.get(SecureSocial.USER_KEY);
        return ok(linkResult.render(current, current.identities, env));
    }

    public RuntimeEnvironment getEnv() {
        return env;
    }
}
