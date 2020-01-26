package com.fexed.lprb.wq.server;

import java.util.HashMap;

/**
 * @author Federico Matteoni
 */
public class WQServerChallenge implements Runnable {
    private WQHandler sfidante1;
    private WQHandler sfidante2;
    private WQServer server;
    private HashMap<String, String> randomWords;

    public WQServerChallenge(WQHandler s1, WQHandler s2, HashMap<String, String> words, WQServer server) {
        this.sfidante1 = s1;
        this.sfidante2 = s2;
        this.randomWords = words;
        this.server = server;
    }

    @Override
    public void run() {
        WQServerController.gui.addThreadsText();
        sfidante1.randomWords = new HashMap<>(randomWords);
        sfidante2.randomWords = new HashMap<>(randomWords);

        try { Thread.sleep(3000); }
        catch (InterruptedException ignored) {}

        do {
            try { Thread.sleep(500); }
            catch (InterruptedException ignored) {}
        } while (sfidante1.isChallenging);


        do {
            try { Thread.sleep(500); }
            catch (InterruptedException ignored) {}
        } while (sfidante2.isChallenging);

        server.fineSfida(sfidante1, sfidante1.pointsMade, sfidante2, sfidante2.pointsMade);
        WQServerController.gui.subThreadsText();
    }
}
