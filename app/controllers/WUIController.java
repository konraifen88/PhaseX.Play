package controllers;


import controller.UIController;
import model.card.ICard;
import model.card.impl.Card;
import model.deckOfCards.IDeckOfCards;
import model.deckOfCards.impl.DeckOfCards;
import model.stack.ICardStack;
import models.Message;
import phasex.Init;
import play.libs.F;
import play.mvc.WebSocket;
import play.mvc.WebSocket.Out;
import play.twirl.api.Html;
import securesocial.core.java.SecuredAction;
import service.DemoUser;
import util.Event;
import util.IObserver;
import view.tui.TUI;
import views.html.gamefield;

import java.util.HashMap;
import java.util.LinkedList;

@SecuredAction
public class WUIController implements IObserver {


    private UIController controller = Init.getInstance().getIn().getInstance(UIController.class);
    private TUI tui = Init.getInstance().getTui();
    private DemoUser player1;
    private DemoUser player2;

    private WebSocket<String> socketPlayer1;
    private Out<String> outPlayer1;
    private WebSocket<String> socketPlayer2;
    private Out<String> outPlayer2;


    public WUIController(UIController controller, DemoUser player1) {
        this.controller = controller;
        this.player1 = player1;

        socketPlayer1 = new WebSocket<String>() {
            @Override
            public void onReady(In<String> in, Out<String> out) {
                System.out.println("Init Socket for Player1");
                outPlayer1 = out;

                in.onClose(new F.Callback0() {
                    @Override
                    public void invoke() throws Throwable {
                        System.out.println("Player1 has quit the game");
                        quitEvent(outPlayer2);
                    }
                });


            }
        };

        socketPlayer2 = new WebSocket<String>() {
            @Override
            public void onReady(In<String> in, Out<String> out) {
                System.out.println("Init Socket for Player2");
                outPlayer2 = out;

                in.onClose(new F.Callback0() {
                    @Override
                    public void invoke() throws Throwable {
                        System.out.println("Player2 has quit the game");
                        quitEvent(outPlayer1);
                    }
                });

            }
        };

        System.out.println("Adding Observer");
        controller.addObserver(this);
    }

    private void quitEvent(Out otherPlayer) {
        try {
            otherPlayer.write("playerLeft");
        } catch (NullPointerException npe) {
        }
    }

    public DemoUser getPlayer1() {
        return player1;
    }

    public DemoUser getPlayer2() {
        return player2;
    }

    public void setPlayer2(DemoUser user) {
        this.player2 = user;
    }

    public WebSocket<String> getSocket(DemoUser du) {
        if (du.equals(player1)) {
            return socketPlayer1;
        }
        return socketPlayer2;
    }

    public String getUI() {
        String ui = tui.getSb().toString();
        ui = ui.replaceAll("\n", "<br>");
        ui = ui.replaceAll(" ", "&nbsp;");
        return ui;
    }

    public Html play(String command) {
        System.out.println(command);
        tui.processInputLine(command);
        return gamefield.render(getUI());
    }

    public Html start() {
        controller.startGame();
        return gamefield.render(getUI());
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
            m.put("currentPlayerName", player1.main.fullName().get());
        } else if (player2 != null) {
            m.put("currentPlayerName", player2.main.fullName().get());
        } else {
            m.put("currentPlayerName", "player2");
        }
        Message message = new Message(m);
        return message;
    }

//    public void updateAll() {
//        socketPlayer1.Out.write("update");
//        socketPlayer2.write("update");
//    }

    public String getJsonUpdate() {

        Message message = getCurrentMessage();

        return message.toJson();
    }


    public String discard() {
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
}
