package com.fexed.lprb.wq.server;

import java.util.HashMap;

/**
 * @author Federico Matteoni
 */
public class WQServerChallenge implements Runnable {

    /**
     * Il primo sfidante
     */
    private WQHandler sfidante1;

    /**
     * Il secondo sfidante
     */
    private WQHandler sfidante2;

    /**
     * Riferimento al server
     */
    private WQServer server;

    /**
     * Le parole da passare agli sfidanti
     */
    private HashMap<String, String> randomWords;

    /**
     * Costruttore, prepara gli attributi della classe
     * @param s1 Il primo sfidante
     * @param s2 Il secondo sfidante
     * @param words La lista delle parole
     * @param server Riferimento al server
     */
    public WQServerChallenge(WQHandler s1, WQHandler s2, HashMap<String, String> words, WQServer server) {
        this.sfidante1 = s1;
        this.sfidante2 = s2;
        this.randomWords = words;
        this.server = server;
    }

    @Override
    public void run() {
        WQServerController.gui.addThreadsText();
        sfidante1.randomWords = new HashMap<>(randomWords); //Avvia il primo sfidante
        sfidante2.randomWords = new HashMap<>(randomWords); //Avvia il secondo sfidante

        try { Thread.sleep(3000); }
        catch (InterruptedException ignored) {} //Aspetta tre secondi per consentire l'inizio della sfida

        //Aspetta che entrambi gli sfidanti finiscano
        do {
            try { Thread.sleep(500); }
            catch (InterruptedException ignored) {}
        } while (sfidante1.isChallenging);

        do {
            try { Thread.sleep(500); }
            catch (InterruptedException ignored) {}
        } while (sfidante2.isChallenging);

        //Segnala al server la fine della sfida
        server.fineSfida(sfidante1, sfidante1.pointsMade, sfidante2, sfidante2.pointsMade);
        WQServerController.gui.subThreadsText();
    }
}
