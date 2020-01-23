package com.fexed.lprb.wq.server;

import java.net.Socket;
import java.nio.channels.SocketChannel;

public class WQHandler implements Runnable {
    private WQServer server;
    private SocketChannel skt;

    public WQHandler(WQServer server, SocketChannel skt) {
        this.server = server;
        this.skt = skt;
    }

    @Override
    public void run() {
        WQServerController.gui.updateStatsText("Connessione accettata.");
    }
}
