package controllers;


import controller.UIController;
import model.card.ICard;
import model.card.impl.Card;
import model.card.impl.CardValueComparator;
import model.deckOfCards.IDeckOfCards;
import model.deckOfCards.impl.DeckOfCards;
import model.stack.ICardStack;
import models.Message;
import models.WUIObserver;
import phasex.Init;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import play.twirl.api.Html;
import securesocial.core.java.SecuredAction;
import service.DemoUser;
import view.tui.TUI;
import views.html.gamefield;
import views.html.newGamefield;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

@SecuredAction
public class WUIController {


    private UIController controller = Init.getInstance().getIn().getInstance(UIController.class);
    private TUI tui = Init.getInstance().getTui();
    private DemoUser player1;
    private DemoUser player2;
    private DemoUser currentPlayer;

    public WUIController(UIController controller, DemoUser player1) {
        this.controller = controller;
        this.player1 = player1;
        this.currentPlayer = player1;
    }


    public void setPlayer2(DemoUser user) {
        this.player1 = user;
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

    /*
    public Html ngApp() {
        controller.startGame();
        return newGamefield.render();
    }

    public Html restart() {
        controller = new controller.impl.Controller(2);
        controller.startGame();
        return newGamefield.render();
    }*/

    public String getDrawOpen(DemoUser user) {
        if(isCurrentPlayer(user)) {
            controller.drawOpen();
        }
        Message message = getCurrentMessage();
        return message.toJson();
    }

    public String getDrawHidden(DemoUser user) {
        if(isCurrentPlayer(user)) {
            controller.drawHidden();
        }
        Message message = getCurrentMessage();
        return message.toJson();
    }

    public String discard(int index, DemoUser user) {
        if(isCurrentPlayer(user)) {
            ICard card = new Card(controller.getCurrentPlayersHand().get(index).getNumber(),
                    controller.getCurrentPlayersHand().get(index).getColor());
            controller.discard(card);
        }

        Message message = getCurrentMessage();
        return message.toJson();
    }

    public String playPhase(String cards, DemoUser user) {
        if(isCurrentPlayer(user)) {
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
        if(isCurrentPlayer(user)) {
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
        if(controller.getCurrentPlayer().getPlayerNumber() == 0 && user.equals(player1)) {
            return true;
        }
        if(controller.getCurrentPlayer().getPlayerNumber() == 1 && user.equals(player2)) {
            return true;
        }
        return false;
    }

    private Message getCurrentMessage() {
        HashMap<String, Object> m = new HashMap<>();
        IDeckOfCards playerHand = controller.getCurrentPlayersHand();
        Collections.sort(playerHand, new CardValueComparator());
        m.put("playerHand", playerHand);

        m.put("opponent", controller.getOpponentPlayer().getDeckOfCards());
        if(controller.getCurrentPlayer().getPlayerNumber() == 0) {
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
        if(controller.getCurrentPlayer().getPlayerNumber() == 0) {
            m.put("currentPlayerName",player1.main.fullName().get());
        } else if(player2 != null) {
            m.put("currentPlayerName",player2.main.fullName().get());
        } else {
            m.put("currentPlayerName","player2");
        }
        Message message = new Message(m);
        return message;
    }

    public String getJsonUpdate() {

        Message message = getCurrentMessage();

        return message.toJson();
    }

//    public static WebSocket<String> getSocket() {
//        return new WebSocket<String>() {
//            public void onReady(WebSocket.In<String> in, WebSocket.Out<String> out) {
//                System.out.println("we start a socket");
//                in.onMessage((event) -> {
//                    System.out.println(event);
//                    WUIObserver wuiObserver = new WUIObserver(controller, out);
//                    wuiObserver.analyzeMessage(event);
//                         /*)
//                         System.out.println(event + " came in");
//                         switch (event.toString()) {
//                             case "GET":
//                                 System.out.println("GET");
//                                 out.write(Application.getCurrentStateAsJSon());
//                                 break;
//                             default:
//                                 System.out.println("CRAP");
//                                 out.write("YOU FUCKED UP");
//                                 break;
//                         }
//                         System.out.println(event + " gone");
//                        */
//                });
//                in.onClose(() -> {
//                    System.out.println("Socket geschlossen");
//                });
//
//            }
//        };
//    }

    public String discard() {
        Message message = getCurrentMessage();
        return message.toJson();
    }
}
