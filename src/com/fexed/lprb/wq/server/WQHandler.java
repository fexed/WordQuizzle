package com.fexed.lprb.wq.server;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class WQHandler implements Runnable {
    private WQServer server;
    private SocketChannel skt;

    public WQHandler(WQServer server, SocketChannel skt) {
        this.server = server;
        this.skt = skt;
    }

    @Override
    public void run() {
        ByteBuffer bBuff = ByteBuffer.allocate(128);
        StringBuilder sBuff = new StringBuilder();
        String str;
        WQServerController.gui.updateStatsText("Connessione accettata.");

        try {
            int n;
            do {
                bBuff.clear();
                n = skt.read(bBuff);
            } while (n == 0);
            do {
                n = skt.read(bBuff);
            } while (n > 0);
            WQServerController.gui.updateStatsText(StandardCharsets.UTF_8.decode(bBuff).toString());
        } catch (IOException ex) { WQServerController.gui.updateStatsText(ex.getMessage()); ex.printStackTrace(); }

    }
}
