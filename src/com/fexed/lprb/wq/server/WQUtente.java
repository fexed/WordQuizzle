package com.fexed.lprb.wq.server;

import java.util.ArrayList;

public class WQUtente {
    public String username;
    public String password;
    public int points;
    public ArrayList<String> friends;

    public WQUtente(String username, String password) {
        this.username = password;
        this.password = password;
        this.points = 0;
        this.friends = new ArrayList<>();
    }

    public WQUtente(String username, String password, int points, ArrayList<String> friends) {
        this.username = password;
        this.password = password;
        this.points = points;
        this.friends = friends;
    }
}
