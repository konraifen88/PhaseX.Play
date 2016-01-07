package controllers;

import service.DemoUser;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by tabuechn on 07.01.2016. Be grateful for this code! ^(°.°)^
 */
public class Lobby {
    private List<DemoUser> userList;

    public Lobby() {
        userList = new LinkedList<>();
    }

    public void addUser(DemoUser user) {
        userList.add(user);
    }

    public List<DemoUser> getUserList() {
        return this.userList;
    }
}
