package com.fexed.lprb.wq.server;

import com.fexed.lprb.wq.WQUtente;
import java.util.Collection;

/**
 * Interfaccia per la GUI del server
 * @author Federico Matteoni
 */
public interface WQServerGUIInterface {
    /**
     * Aggiorna il log di comunicazione della GUI
     * @param txt Il testo da aggiungere
     */
    void updateStatsText(String txt);

    /**
     * Segnala alla GUI che il server è online
     * @param port La porta di ascolto del server
     */
    void serverIsOnline(int port);

    /**
     * Segnala alla GUI che il server è andato offline
     */
    void serverIsOffline();

    /**
     * Aggiunge un utente alla lista degli utenti online
     * @param user Il nickname dell'utente da aggiungere
     */
    void addOnline(String user);

    /**
     * Rimuove un utente dalla lista degli utenti online
     * @param user Il nickname dell'utente da rimuovere
     */
    void removeOnline(String user);

    /**
     * Aggiunge un utente alla lista degli utenti registrati
     * @param user L'utente da aggiungere
     */
    void addRegistered(WQUtente user);

    /**
     * Aggiunge un insieme di utenti alla lista degli utenti registrati
     * @param users La collezione di utenti da aggiungere
     */
    void addAllRegistered(Collection<? extends WQUtente> users );
}
