package com.fexed.lprb.wq.client;

import java.util.Collection;

/**
 * Interfaccia per la GUI del client
 * @author Federico Matteoni
 */
public interface WQClientGUIInterface {
    /**
     * Aggiorna il log di comunicazione della GUI
     * @param txt Il testo da aggiungere al log
     */
    void updateCommText(String txt);

    /**
     * Pulisce il log di comunicazione della GUI
     * @param txt Il testo da scrivere sul log
     */
    void clearCommText(String txt);

    /**
     * Segnala alla GUI l'avvenuta procedura di login
     * @param username L'username con cui ci si è collegati
     * @param points Il punteggio attuale dell'utente collegato
     */
    void loggedIn(String username, int points);

    /**
     * Segnala alla GUI che il client è offline
     */
    void loggedOut();

    /**
     * Aggiunge un amico alla GUI
     * @param friend Il nickname dell'amico da aggiungere
     */
    void addFriend(String friend);

    /**
     * Aggiunge una serie di amici alla GUI
     * @param friends La collezione di nickname degli utenti da aggiungere
     */
    void addAllFriends(Collection<? extends String> friends);

    /**
     * Mostra una finestra di dialogo con del testo
     * @param text Il testo da mostrare
     */
    void showTextDialog(String text);

    /**
     * Mostra una finestra di dialogo riguardante la sfida proveniente da {@code nickSfidante}
     * @param nickSfidante Il testo da mostrare
     */
    int showChallengeDialog(String nickSfidante);

    /**
     * Aggiorna il punteggio visibile nella GUI
     * @param point Il nuovo punteggio
     */
    void updatePoints(int point);

    /**
     * Disabilita i commandi della GUI
     */
    void disableCommands();

    /**
     * Abilita i commandi della GUI
     */
    void enableCommands();
}
