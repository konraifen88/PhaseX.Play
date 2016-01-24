package controllers;


import controller.UIController;
import model.card.ICard;
import model.card.impl.Card;
import model.deckOfCards.IDeckOfCards;
import model.deckOfCards.impl.DeckOfCards;
import model.stack.ICardStack;
import models.Message;
import phasex.Init;
import play.api.mvc.WebSocket$;
import play.libs.F;
import play.mvc.Results;
import play.mvc.WebSocket;
import play.mvc.WebSocket.Out;
import play.twirl.api.Html;
import securesocial.core.java.SecuredAction;
import service.DemoUser;
import util.Event;
import util.IObserver;
import view.tui.TUI;
import views.html.gamefield.gamefield;

import java.util.HashMap;
import java.util.LinkedList;

@SecuredAction
public class WUIController implements IObserver {


    private UIController controller = Init.getInstance().getIn().getInstance(UIController.class);
    private TUI tui = Init.getInstance().getTui();
    private DemoUser player1;
    private DemoUser player2;

    private Application homeApplication;

    private WebSocket<String> socketPlayer1;
    private Out<String> outPlayer1;
    private WebSocket<String> socketPlayer2;
    private Out<String> outPlayer2;

    private boolean running;
    private String roomName;

    private Thread quitter;


    public WUIController(UIController controller, DemoUser player1, String roomName, Application app) {
        this.homeApplication = app;
        this.controller = controller;
        this.player1 = player1;
        player1.isInGameOrLobby = true;
        this.running = true;
        this.roomName = roomName;

        socketPlayer1 = createSocket(true);

        socketPlayer2 = createSocket(false);

        System.out.println("Adding Observer");
        controller.addObserver(this);
    }

    private WebSocket<String> getSocket(boolean isPlayer1) {
        if (isPlayer1) {
            if (this.socketPlayer1 == null) {
                System.out.println("Player 1 rejoined the game");
                this.quitter.interrupt();
                this.socketPlayer1 = createSocket(true);
            }
            return this.socketPlayer1;
        } else  {
            if (this.socketPlayer2 == null) {
                System.out.println("Player 2 rejoined the game");
                this.quitter.interrupt();
                this.socketPlayer2 = createSocket(false);
            }
            return this.socketPlayer2;
        }
    }

    private WebSocket<String> createSocket(boolean isPlayer1) {
        return new WebSocket<String>() {
            private Thread t;

            @Override
            public void onReady(In<String> in, Out<String> out) {
                System.out.println("Init Socket for Player1");
                if(isPlayer1) {
                    outPlayer1 = out;
                } else {
                    outPlayer2 = out;
                }


                in.onClose(() -> {
                    System.out.println("Player1 has quit the game");
                    running = false;
                    if(isPlayer1) {
                        player1.isInGameOrLobby = false;
                    } else {
                        player2.isInGameOrLobby = false;
                    }
                    t.interrupt();

                    quitEvent(isPlayer1);
                });

                in.onMessage((message) -> {
                    try {
                        outPlayer1.write(message);
                        outPlayer2.write(message);
                    } catch (NullPointerException e) {
                        System.out.println("player2 not in game yet");
                    }
                });

                this.t = new Thread(() -> {
                    while (running) {
                        try {
                            Thread.sleep(30000);
                            out.write("stayingAlive");
                        } catch (InterruptedException ignored) {
                        }
                    }

                }, "HEART_BEATER");
                t.start();
            }
        };
    }

    private void quitEvent(boolean isPlayer1) {
        /*
        if(player2 == null) {
            this.homeApplication.quitGame(roomName);
            return;
        }*/

        WebSocket.Out otherPlayer;
        if (isPlayer1) {
            otherPlayer = outPlayer2;
            this.outPlayer1 = null;
            this.socketPlayer1 = null;
        } else {
            otherPlayer = outPlayer1;
            this.outPlayer2 = null;
            this.socketPlayer2 = null;
        }

        Application.deleteFromAvailableSockets(roomName);
        this.quitter = new Thread(() -> {
            try {
                Thread.sleep(10000);
                otherPlayer.write("playerLeft");
            } catch (NullPointerException | InterruptedException ignored) {
                //other player is not in the game
            }
        }, "QUITTER");
        this.quitter.start();
    }

    public String getRoomName() {
        return this.roomName;
    }

    public DemoUser getPlayer1() {
        return player1;
    }

    public DemoUser getPlayer2() {
        return player2;
    }

    public void setPlayer2(DemoUser user) {
        this.player2 = user;
        this.player2.isInGameOrLobby = true;
    }

    public WebSocket<String> getSocket(DemoUser du) {
        return this.getSocket(du.equals(player1));
        /*if (du.equals(player1)) {
            return socketPlayer1;
        }
        return socketPlayer2;*/
    }

    public String getUI() {
        String ui = tui.getSb().toString();
        ui = ui.replaceAll("\n", "<br>");
        ui = ui.replaceAll(" ", "&nbsp;");
        return ui;
    }

    public void start() {
        controller.startGame();
    }

    public String getDrawOpen(DemoUser user) {
        System.out.println("Attempt to drawOpen");
        if (isCurrentPlayer(user)) {
            System.out.println("Got Drawing Permission");
            controller.drawOpen();
        }
        Message message = getCurrentMessage();
        return message.toJson();
    }

    public String getDrawHidden(DemoUser user) {
        System.out.println("Attempt to DrawHidden");
        if (isCurrentPlayer(user)) {
            System.out.println("Got Drawing Permission");
            controller.drawHidden();
        }
        Message message = getCurrentMessage();
        return message.toJson();
    }

    public String discard(int index, DemoUser user) {
        System.out.println("Attempt to discard");
        if (isCurrentPlayer(user)) {
            System.out.println("discarding at index" + index);
            ICard card = new Card(controller.getCurrentPlayersHand().get(index).getNumber(),
                    controller.getCurrentPlayersHand().get(index).getColor());
            controller.discard(card);
        }

        Message message = getCurrentMessage();
        return message.toJson();
    }

    public String playPhase(String cards, DemoUser user) {
        System.out.println("Attempt to play Phase");
        if (isCurrentPlayer(user)) {
            System.out.println("playing Phase");
            cards = cards.substring(0, cards.length() - 1);
            IDeckOfCards phases = new DeckOfCards();
            for (String card : cards.split(";")) {
                int index = Integer.parseInt(card);
                ICard cardObject = controller.getCurrentPlayersHand().get(index);
                phases.add(cardObject);
            }
            controller.playPhase(phases);
        }
        Message message = getCurrentMessage();
        return message.toJson();
    }

    public String addToPhase(int cardIndex, int stackIndex, DemoUser user) {
        System.out.println("Attempt to add to Phase");
        if (isCurrentPlayer(user)) {
            System.out.println("adding to Phase");
            controller.addToFinishedPhase(controller.getCurrentPlayersHand().get(cardIndex),
                    controller.getAllStacks().get(stackIndex));
        }
        Message message = getCurrentMessage();
        return message.toJson();
    }

    private IDeckOfCards getFirstAndLast(ICardStack stack) {
        IDeckOfCards list = stack.getList();
        if (list.size() > 4) {
            IDeckOfCards retList = new DeckOfCards();
            retList.add(list.get(0));
            retList.add(list.get(1));
            retList.add(list.get(list.size() - 2));
            retList.add(list.get(list.size() - 1));
            return retList;
        } else {
            return list;
        }
    }

    private boolean isCurrentPlayer(DemoUser user) {

        if (controller.getCurrentPlayer().getPlayerNumber() == 0 && user.equals(player1)) {
            return true;
        }
        if (controller.getCurrentPlayer().getPlayerNumber() == 1 && user.equals(player2)) {
            return true;
        }
        return false;
    }

    private Message getCurrentMessage() {
        HashMap<String, Object> m = new HashMap<>();
        IDeckOfCards playerHand = controller.getCurrentPlayersHand();
        //Collections.sort(playerHand, new CardValueComparator());
        m.put("playerHand", playerHand);

        m.put("opponent", controller.getOpponentPlayer().getDeckOfCards());
        if (controller.getCurrentPlayer().getPlayerNumber() == 0) {
            m.put("player1Cards", controller.getCurrentPlayersHand());
            m.put("player2Cards", controller.getOpponentPlayer().getDeckOfCards());
        } else {
            m.put("player2Cards", controller.getCurrentPlayersHand());
            m.put("player1Cards", controller.getOpponentPlayer().getDeckOfCards());
        }


        m.put("stack", controller.getAllStacks());
        int numberOfStacks = controller.getAllStacks().size();
        m.put("stack1", new LinkedList<>());
        m.put("stack2", new LinkedList<>());
        m.put("stack3", new LinkedList<>());
        m.put("stack4", new LinkedList<>());
        for (int i = 0; i <= numberOfStacks; i++) {
            if (i == 1) {
                m.put("stack1", getFirstAndLast(controller.getAllStacks().get(0)));
            }
            if (i == 2) {
                m.put("stack2", getFirstAndLast(controller.getAllStacks().get(1)));
            }
            if (i == 3) {
                m.put("stack3", getFirstAndLast(controller.getAllStacks().get(2)));
            }
            if (i == 4) {
                m.put("stack4", getFirstAndLast(controller.getAllStacks().get(3)));
            }
        }

        m.put("discardIsEmpty", controller.getDiscardPile().size() == 0);
        m.put("discard", controller.getDiscardPile());
        m.put("state", controller.getRoundState().toString());
        m.put("currentPlayerStats", controller.getCurrentPlayer());
        m.put("currentPlayerPhase", controller.getCurrentPlayer().getPhase().getDescription());
        m.put("roundState", controller.getRoundState().toString());
        if (controller.getCurrentPlayer().getPlayerNumber() == 0) {
            m.put("currentPlayerName", getUserName(player1));
        } else if (player2 != null) {
            m.put("currentPlayerName", getUserName(player2));
        } else {
            m.put("currentPlayerName", "player2");
        }
        Message message = new Message(m);
        return message;
    }

    public String getJsonUpdate() {

        Message message = getCurrentMessage();

        return message.toJson();
    }

    public void updateAll() {
        if (outPlayer1 != null) {
            outPlayer1.write("update");
        }
        if (outPlayer2 != null) {
            outPlayer2.write("update");
        }
    }

    @Override
    public void update(Event event) {
        updateAll();
    }

    private String getUserName(DemoUser usr){
        if (usr.main.fullName().isDefined()) {
            return usr.main.fullName().get();
        } else {
            return usr.main.userId();
        }
    }
}
