package com.fexed.lprb.wq;

import java.util.ArrayList;

/**
 * L'utente di WordQuizzle
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
        this.username = username;
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
        this.username = username;
        this.password = password;
        this.points = points;
        this.friends = friends;
    }

    @Override
    public String toString() {
        return this.username + " [" + this.points  + " punt" + (this.points == 1 ? "o" : "i") + " - " + this.friends.size() + " amic" + (this.friends.size() == 1 ? "o" : "i") + "]";
    }

    /**
     * Ritorna una breve descrizione dell'utente su più linee
     * @return La descrizione con username, password, punti e amici.
     */
    public String description() {
        String descr ="Nome utente: " + this.username + "\nPassword: " + this.password + "\n" + this.points  + " punt" + (this.points == 1 ? "o" : "i") + ", " + this.friends.size() + " amic" + (this.friends.size() == 1 ? "o" : "i");
        if (this.friends.size() > 0) {
            descr += ":";
            for (String username : this.friends) {
                descr = descr.concat("\n- " + username);
            }
        }

        return descr;
    }
}
