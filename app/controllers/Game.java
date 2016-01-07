package controllers;

import controller.UIController;
import service.DemoUser;

/**
 * Created by tabuechn on 07.01.2016. Be grateful for this code! ^(°.°)^
 */
public class Game {
    public static UIController controller;
    public int lobbyNumber;
    public DemoUser player1;
    public DemoUser player2;
    public int numberOfPlayers;

    public Game(UIController pController, int pLobbyNumber) {
        controller = pController;
        lobbyNumber = pLobbyNumber;
        numberOfPlayers = 0;
    }

    public boolean addPlayer(DemoUser user) {
        if(player1 == null) {
            player1 = user;
            numberOfPlayers++;
            return true;
        }
        if (player2 == null) {
            player2 = user;
            numberOfPlayers++;
            return true;
        }
        return false;
    }

    public boolean removePlayer(DemoUser user) {
        if (player1.main.userId().equals(user.main.userId().toString())) {
            player1 = null;
            numberOfPlayers --;
            return true;
        }
        if (player2.main.userId().equals(user.main.userId().toString())) {
            player2 = null;
            numberOfPlayers --;
            return true;
        }
        return false;
    }
}
