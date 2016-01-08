/**
 * Copyright 2012-214 Jorge Aliss (jaliss at gmail dot com) - twitter: @jaliss
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package controllers;

import com.google.inject.Inject;
import controller.UIController;
import models.Message;
import play.Logger;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import securesocial.core.BasicProfile;
import securesocial.core.RuntimeEnvironment;
import securesocial.core.java.SecureSocial;
import securesocial.core.java.SecuredAction;
import securesocial.core.java.UserAwareAction;
import service.DemoUser;
import views.html.homepage;
import views.html.linkResult;
import views.html.lobbyPage;

import java.util.HashMap;
import java.util.Map;


/**
 * A sample controller
 */
public class Application extends Controller {
    public static Logger.ALogger logger = Logger.of("application.controllers.Application");
    private RuntimeEnvironment env;
    private Lobby lobby;
    private Chat chat;
    private int gameLobbyNumber = 0;
    private Map<Integer,WUIController> gameControllerMap = new HashMap<>();

    /**
     * A constructor needed to get a hold of the environment instance.
     * This could be injected using a DI framework instead too.
     *
     * @param env
     */
    @Inject()
    public Application(RuntimeEnvironment env) {
        this.env = env;
        lobby = new Lobby();
        chat = new Chat();
    }

    /**
     * This action only gets called if the user is logged in.
     *
     * @return
     */

    @SecuredAction
    public Result index() {
        if (logger.isDebugEnabled()) {
            logger.debug("access granted to index");
        }
        DemoUser user = (DemoUser) ctx().args.get(SecureSocial.USER_KEY);
        lobby.addUser(user);
//        return chat.chatRoom(user.main.fullName().get(), "teest");
        //return ok(index.render(user, SecureSocial.env()));
        return ok(homepage.render());
    }

    @SecuredAction
    public Result goToChatRoom(String roomName){
        DemoUser user = (DemoUser) ctx().args.get(SecureSocial.USER_KEY);
        return chat.chatRoom(user.main.fullName().get(), roomName);
    }

    @SecuredAction
    public Result getLobbyUpdate() {
        Message message = lobbyUpdateMessage();
        return ok(message.toJson());
    }

    @SecuredAction
    public Message lobbyUpdateMessage() {
        HashMap<String, Object> m = new HashMap<>();
        m.put("allUsers",lobby.getUserList());
        m.put("games",lobby.getGames());
        Message message = new Message(m);
        return message;
    }

    @SecuredAction
    public Result addGame() {
        lobby.addGame();
        Message message = lobbyUpdateMessage();
        return ok(message.toJson());
    }

    @SecuredAction
    public Result joinGame(int gameNumber) {

        DemoUser demoUser = (DemoUser) ctx().args.get(SecureSocial.USER_KEY);
        lobby.addPlayerToGame(demoUser,gameNumber);
        Message message = lobbyUpdateMessage();
        return ok(message.toJson());
    }

    @SecuredAction
    public Result startGameInstance(UIController ctrl, DemoUser player1, DemoUser player2, int lobbynumber) {
        WUIController wuictrl = new WUIController(ctrl);
        ctrl.startGame();
        gameControllerMap.put(lobbynumber,wuictrl);
        return ok();
    }

    @SecuredAction
    public Result startGame(int gameNumber) {
        Game game = lobby.getGames().get(gameNumber);
        startGameInstance(game.getController(),game.getPlayer1(),game.getPlayer2(),game.lobbyNumber);
        return ok();
    }

    @SecuredAction
    public Result getLobby() {
        return ok(lobbyPage.render());
    }

    @SecuredAction
    public Result getJsonUpdate() {
        //return gameControllerMap.get(lobbyNumber).getJsonUpdate();
        return ok();
    }

    @SecuredAction
    public Result ngGame() {
        //return ok(ngGamefield.render(gameControllerMap.get(lobbyNumber).getUI()));
        return ok();
    }

    @SecuredAction
    public Result getDrawHidden() {
        return ok();
    }

    @SecuredAction
    public Result getDrawOpen() {
        return ok();
    }

    @SecuredAction
    public Result discard(int index) {
        return ok();
    }

    @SecuredAction
    public Result playPhase(String cards) {
        return ok();
    }

    @SecuredAction
    public Result addToPhase(int cardindex, int stackIndex) {
        return ok();
    }



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
    public WebSocket<String> getLobbySocket() {
        return new WebSocket<String>() {

            public void onReady(WebSocket.In<String> in, WebSocket.Out<String> out) {
                System.out.println("Socket initialized");

                in.onMessage((event) -> {
                    System.out.println(event);
                    if(event.toString().equals("addGame")) {
                        lobby.addGame();
                        out.write(lobbyUpdateMessage().toJson());
                    }
                    if(event.toString().startsWith("joinGame")) {
                        System.out.println("ist join da?");

                    }

                });

                in.onClose(() -> {
                    System.out.println("Socket closed");
                });

            }
        };
    }

    @SecuredAction(authorization = WithProvider.class, params = {"twitter"})
    public Result onlyTwitter() {
        return ok("You are seeing this because you logged in using Twitter");
    }

    @SecuredAction
    public Result linkResult() {
        DemoUser current = (DemoUser) ctx().args.get(SecureSocial.USER_KEY);
        return ok(linkResult.render(current, current.identities));
    }

    /**
     * Sample use of SecureSocial.currentUser. Access the /current-user to test it
     */
    public F.Promise<Result> currentUser() {
        return SecureSocial.currentUser(env).map(new F.Function<Object, Result>() {
            @Override
            public Result apply(Object maybeUser) throws Throwable {
                String id;

                if (maybeUser != null) {
                    DemoUser user = (DemoUser) maybeUser;
                    id = user.main.userId();
                } else {
                    id = "not available. Please log in.";
                }
                return ok("your id is " + id);
            }
        });
    }


}
