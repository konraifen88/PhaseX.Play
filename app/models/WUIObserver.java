package models;

import controller.UIController;
import play.Logger;
import play.mvc.WebSocket;
import util.Event;
import util.IObserver;

import java.util.HashMap;
import java.util.Map;

/**
 * If everything works right this class was
 * created by Konraifen88 on 25.11.2015.
 * If it doesn't work I don't know who the hell wrote it.
 */
public class WUIObserver implements IObserver {
    private WebSocket.Out<String> out;
    private UIController controller;

    public WUIObserver(UIController controller, WebSocket.Out<String> out) {
        controller.addObserver(this);
        this.controller = controller;
        this.out = out;
    }

    @Override
    public void update(Event event) {
        String currentState = controller.getRoundState().toString();
        out.write(currentState);
        Logger.debug("WUI updated to phase " + currentState);
    }

    public String getCurrentStateAsJSon() {

        Map<String, Object> result = new HashMap<>();

        String state = controller.getRoundState().toString();
        result.put("state", state);
        switch (state) {
            case "StartPhase":
                break;
            case "EndPhase":
                break;
            case "DrawPhase":
                result.put("player", controller.getCurrentPlayersHand());
                result.put("opponent", controller.getNumberOfCardsForNextPlayer());
                result.put("stack1", controller.getAllStacks().get(0));
                result.put("stack2", controller.getAllStacks().get(1));
                result.put("stack3", controller.getAllStacks().get(2));
                result.put("stack4", controller.getAllStacks().get(3));
                result.put("discard", controller.getDiscardPile().get(0));
                break;
            case "PlayerTurnNotFinished":
                break;
            case "PlayerTurnFinished":
                break;
            default:
                break;
        }
        System.out.println(controller.getRoundState().toString() + " round state");
        return controller.getRoundState().toString();
        //return (new Gson()).toJson(result);
    }

    public void analyzeMessage(String msg) {
        System.out.println("analyze " + msg);
        switch (msg) {
            case "DISCARD":
                System.out.println("writing out state");
                System.out.println(controller);
                HashMap<String, Object> m = new HashMap<>();
                System.out.println("writing out state");

                m.put("playerHand", controller.getCurrentPlayersHand());
                System.out.println("writing out state2");
                m.put("opponent", controller.getNumberOfCardsForNextPlayer());

                m.put("stack", controller.getAllStacks());

                m.put("discard", controller.getDiscardPile());
                System.out.println("writing out state4");
                for (String key : m.keySet()) {
                    System.out.println("Key: " + key);
                    System.out.println("Val: " + m.get(key));
                }
                System.out.println("writing out state");
                Message message = new Message(m);
                System.out.println("JSON String: " + message.toJson());


                out.write(message.toJson());
                break;
            default:
                System.out.println("default case");
                out.write("NOT REALLY");
        }
    }
}
