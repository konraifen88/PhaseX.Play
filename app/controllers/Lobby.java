package controllers;

import securesocial.core.java.SecuredAction;
import service.DemoUser;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by tabuechn on 07.01.2016. Be grateful for this code! ^(°.°)^
 */
public class Lobby {
    private static List<DemoUser> userList = new LinkedList<>();


    @SecuredAction
    public void addUser(DemoUser user) {

        userList.add(user);
        System.out.println(userList);
    }

    @SecuredAction
    public List<DemoUser> getUserList() {
        System.out.println("returning List:");
        System.out.println(userList);
        return userList;
    }
}
