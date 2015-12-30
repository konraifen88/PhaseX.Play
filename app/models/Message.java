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
        /*StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (String key : map.keySet()) {
            sb.append("\"").append(key);
            sb.append("\":\"").append(map.get(key).toString());
            sb.append("\",");
        }
        String result = sb.toString().substring(0, sb.toString().length() - 2);
        result = result + "}";
        System.out.println(result);
        */
        return (new Gson()).toJson(this);
    }
}
