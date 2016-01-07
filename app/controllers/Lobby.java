package controllers;

import controller.UIController;
import models.WUIObserver;
import play.api.mvc.Controller;
import play.mvc.WebSocket;
import securesocial.core.java.SecureSocial;
import securesocial.core.java.SecuredAction;
import service.DemoUser;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by tabuechn on 07.01.2016. Be grateful for this code! ^(°.°)^
 */
public class Lobby {
    private static List<DemoUser> userList = new LinkedList<>();
    private static List<Game> gameList = new LinkedList<>();
    private static int numberOfGames = 0;



    @SecuredAction
    public void addUser(DemoUser user) {
        userList.add(user);
    }

    @SecuredAction
    public void addGame() {
        UIController controller = new controller.impl.Controller(2);
        Game newGame = new Game(controller,numberOfGames);
        numberOfGames++;
        gameList.add(newGame);

    }

    @SecuredAction
    public void addPlayerToGame(DemoUser user,int gameNumber) {
        gameList.get(gameNumber).addPlayer(user);
    }

    @SecuredAction
    public List<DemoUser> getUserList() {
        return userList;
    }

    @SecuredAction
    public List<Game> getGames() {
        return gameList;
    }


}
