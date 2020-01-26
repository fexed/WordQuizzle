package com.fexed.lprb.wq.client;

import java.util.TimerTask;

/**
 * @author Federico Matteoni
 */
public class WQClientTimerTask extends TimerTask {
    private WQClient client;

    public WQClientTimerTask(WQClient client) {
        this.client = client;
    }

    @Override
    public void run() {
        client.send("-1");
    }
}
