package models;

import com.google.gson.Gson;

import java.util.HashMap;

/**
 * Created by tabuechn on 02.12.2015. Be grateful for this code! ^(°.°)^
 */
public class Message {
    private final HashMap<String, Object> map;

    public Message(HashMap<String, Object> map) {
        this.map = map;
    }

    public String toJson() {
        return (new Gson()).toJson(this);
    }
}
