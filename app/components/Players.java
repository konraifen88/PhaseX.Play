package components;

import service.DemoUser;

/**
 * Created by tabuechn on 09.01.2016. Be grateful for this code! ^(°.°)^
 */
public class Players {
    private DemoUser player1;
    private DemoUser player2;

    public Players(DemoUser player1) {
        this.player1 = player1;
        this.player2 = null;
    }

    public DemoUser getPlayer1() {
        return player1;
    }

    public DemoUser getPlayer2() {
        return player2;
    }

    public void addPlayer2(DemoUser player2) {
        this.player2 = player2;
    }

}
