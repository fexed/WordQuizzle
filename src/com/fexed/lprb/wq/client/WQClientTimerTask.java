package com.fexed.lprb.wq.client;

import java.util.TimerTask;

/**
 * TimerTask per il timeout di risposta durante la sfida. Invia "-1" al server
 * @author Federico Matteoni
 */
public class WQClientTimerTask extends TimerTask {

    /**
     * Riferimento al client
     */
    private WQClient client;

    public WQClientTimerTask(WQClient client) {
        this.client = client;
    }

    @Override
    public void run() {
        client.send("-1");
    }
}
