package com.fexed.lprb.wq;

import java.util.ArrayList;

/**
 * @author Federico Matteoni
 */
public class WQUtente {
    /**
     * Il nome utente
     */
    public String username;

    /**
     * La password
     */
    public String password;

    /**
     * Il punteggio
     */
    public int points;

    /**
     * La lista amici
     */
    public ArrayList<String> friends;

    /**
     * Inizializza un nuovo utente
     * @param username Il nickname
     * @param password La password
     */
    public WQUtente(String username, String password) {
        this.username = password;
        this.password = password;
        this.points = 0;
        this.friends = new ArrayList<>();
    }

    /**
     * Inserisce un utente esistente
     * @param username Il nickname
     * @param password La password
     * @param points Il punteggio
     * @param friends La lista amici
     */
    public WQUtente(String username, String password, int points, ArrayList<String> friends) {
        this.username = password;
        this.password = password;
        this.points = points;
        this.friends = friends;
    }
}
