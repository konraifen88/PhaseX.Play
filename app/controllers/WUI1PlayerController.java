package controllers;


import controller.UIController;
import model.card.ICard;
import model.card.impl.Card;
import model.deck.IDeckOfCards;
import model.deck.impl.DeckOfCards;
import model.stack.ICardStack;
import models.Message;
import persistence.DBEnum;
import persistence.DatabaseAccess;
import phasex.Init;
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
public class WUI1PlayerController implements IObserver {


    private UIController controller = Init.getInstance().getIn().getInstance(UIController.class);
    private TUI tui = Init.getInstance().getTui();
    private DemoUser player1;

    private Application homeApplication;

    private WebSocket<String> socketPlayer1;
    private Out<String> outPlayer1;

    private boolean running;
    private String roomName;
    private DatabaseAccess saveDAO;

    public WUI1PlayerController(UIController controller, DemoUser player1, String roomName, Application app) {
        this.homeApplication = app;
        this.controller = controller;
        this.player1 = player1;
        this.player1.isInGameOrLobby = true;
        this.running = true;
        this.roomName = roomName;

        socketPlayer1 = createSocket();
        saveDAO = new DatabaseAccess(DBEnum.COUCHEDB);



        System.out.println("Adding Observer");
        controller.addObserver(this);
    }

    public WebSocket<String> getSocket() {
        return this.socketPlayer1;
    }

    private WebSocket<String> createSocket() {
        return new WebSocket<String>() {
            private Thread t;

            @Override
            public void onReady(In<String> in, Out<String> out) {
                System.out.println("Init Socket for Player1");
                outPlayer1 = out;

                in.onClose(() -> {
                    System.out.println("Player1 has quit the game");
                    running = false;
                    t.interrupt();
                    quitEvent();
                });

                in.onMessage((mesg) -> {
                    System.out.println("SinglePlayer Message:");
                    System.out.println(mesg);
                    if(mesg.equals("drawOpen")) {
                        controller.drawOpen();
                    }
                    if(mesg.equals("drawHidden")) {
                        controller.drawHidden();
                    }
                    if(mesg.startsWith("discard")) {
                        System.out.println("discarding event");
                        discard(Integer.parseInt(mesg.split(" ")[1]),player1);
                    }
                    if(mesg.startsWith("playPhase")) {
                        System.out.println("playing Phase");
                        String cardString = "";
                        String[] cardArray = mesg.split(" ");
                        for(int i = 1; i < cardArray.length; i++) {
                            cardString += cardArray[i] + ";";
                        }
                        System.out.println("playing the Cards " + cardString);
                        playPhase(cardString,player1);
                    }
                    if(mesg.startsWith("addToPhase")) {
                        int stackNumber = Integer.parseInt(mesg.split(" ")[1]);
                        int cardNumber = Integer.parseInt(mesg.split(" ")[2]);
                        addToPhase(cardNumber,stackNumber,player1);
                    }
                    if(mesg.startsWith("save")) {
                        System.out.println("Saving the game of: " + controller.getPlayers()[0].getPlayerName());
                        saveDAO.saveGame(controller);
                        System.out.println("Saved the game of: " + controller.getPlayers()[0].getPlayerName());
                        out.write("saved");
                    }
                    if(mesg.startsWith("load")) {

                        UIController tmpController = saveDAO.loadGame(controller.getPlayers()[0]);

                        if(tmpController == null) {
                            System.out.println("No saved game for player " + controller.getPlayers()[0].getPlayerName() + " found");
                            out.write("not loaded");
                        } else {
                            System.out.println("Loaded the game of: " + controller.getPlayers()[0].getPlayerName());
                            controller = tmpController;
                            out.write("loaded");
                        }

                    }
                    out.write(getJsonUpdate());
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

    private void quitEvent() {
        this.homeApplication.quitSinglePlayer(player1);
    }

    public String getRoomName() {
        return this.roomName;
    }

    public DemoUser getPlayer1() {
        return player1;
    }


    private ICard getCardOfIndex(int index) {
        return null;
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
        return gamefield.render(getUI(), homeApplication.getEnv());
    }

    public Html start() {
        String provider = player1.main.providerId();
        String userID = player1.main.userId();
        controller.startGame(provider + userID);
        return gamefield.render(getUI(), homeApplication.getEnv());
    }


    public String getDrawHidden(DemoUser user) {
        controller.drawHidden();
        Message message = getCurrentMessage();
        return message.toJson();
    }

    public String discard(int index, DemoUser user) {
        ICard card = new Card(controller.getCurrentPlayersHand().get(index).getNumber(), controller.getCurrentPlayersHand().get(index).getColor());
        controller.discard(card);
        Message message = getCurrentMessage();
        return message.toJson();
    }

    public String playPhase(String cards, DemoUser user) {
        cards = cards.substring(0, cards.length() - 1);
        IDeckOfCards phases = new DeckOfCards();
        for (String card : cards.split(";")) {
            int index = Integer.parseInt(card);
            ICard cardObject = controller.getCurrentPlayersHand().get(index);
            phases.add(cardObject);
        }
        System.out.println(phases.toString());
        controller.playPhase(phases);
        Message message = getCurrentMessage();
        return message.toJson();
    }

    public String addToPhase(int cardIndex, int stackIndex, DemoUser user) {
        System.out.println("adding to Phase");
        controller.addToFinishedPhase(controller.getCurrentPlayersHand().get(cardIndex), controller.getAllStacks().get(stackIndex));
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
        if(controller.getCurrentPlayer().getPlayerNumber() == 0) {
            m.put("currentPlayerName","Player1");
        } else {
            m.put("currentPlayerName","Player2");
        }
        Message message = new Message(m);
        return message;
    }

    public String getJsonUpdate() {

        Message message = getCurrentMessage();

        return message.toJson();
    }



    @Override
    public void update(Event event) {
        try {
            outPlayer1.write("update");
        } catch (NullPointerException npe) {
            //play doesn't have the Socket at initialization
        }
    }
}
