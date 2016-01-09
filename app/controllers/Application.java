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
import components.Players;
import controller.UIController;
import play.Logger;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;
import securesocial.core.BasicProfile;
import securesocial.core.RuntimeEnvironment;
import securesocial.core.java.SecureSocial;
import securesocial.core.java.SecuredAction;
import securesocial.core.java.UserAwareAction;
import service.DemoUser;
import views.html.homepage;
import views.html.linkResult;
import views.html.ngGamefield;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;


/**
 * A sample controller
 */
public class Application extends Controller {
    public static Logger.ALogger logger = Logger.of("application.controllers.Application");
    private RuntimeEnvironment env;
    private Chat chat;
    private Map<String,WUIController> gameControllerMap = new HashMap<>();
    private Map<String,Players> roomPlayerMap = new HashMap<>();
    private Semaphore createGameSem;


    /**
     * A constructor needed to get a hold of the environment instance.
     * This could be injected using a DI framework instead too.
     *
     * @param env
     */
    @Inject()
    public Application(RuntimeEnvironment env) {
        this.env = env;
        chat = new Chat();
        createGameSem = new Semaphore(1);
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
    public Result createGame(String roomName) {
        try {
            try {
                createGameSem.acquire();
                System.out.println("Creating a new Game");
                if(gameControllerMap.containsKey(roomName)) {
                    System.out.println("Adding Player 2 to Game");
                    Players players = roomPlayerMap.get(roomName);
                    DemoUser player2 = (DemoUser) ctx().args.get(SecureSocial.USER_KEY);
                    players.addPlayer2(player2);
                    return ok(ngGamefield.render(gameControllerMap.get(roomName).getUI()));
                }
                System.out.println("Creating a new Game Controller");
                UIController controller = new controller.impl.Controller(2);
                WUIController wuiController = new WUIController(controller);
                wuiController.start();
                System.out.println("Mapping Room and Players");
                gameControllerMap.put(roomName,wuiController);
                DemoUser player1 = (DemoUser) ctx().args.get(SecureSocial.USER_KEY);
                Players players = new Players(player1);
                roomPlayerMap.put(roomName,players);
                System.out.println("init game ready");
                return ok(ngGamefield.render(wuiController.getUI()));
            } finally {
                createGameSem.release();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

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
