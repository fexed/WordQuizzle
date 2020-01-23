package com.fexed.lprb.wq.server;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
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
        SelectionKey keyR, keyW;
        WQServerController.gui.updateStatsText("Connessione accettata.");

        try {
            skt.configureBlocking(false);
            Selector selector = Selector.open();
            keyR = skt.register(selector, SelectionKey.OP_READ);
            keyW = skt.register(selector, SelectionKey.OP_WRITE);
            int n;
            do {
                bBuff.clear();
                n = ((SocketChannel) keyR.channel()).read(bBuff);
            } while (n == 0);
            do {
                n = ((SocketChannel) keyR.channel()).read(bBuff);
            } while (n > 0);
            bBuff.flip();
            WQServerController.gui.updateStatsText("Ricevuto: " + StandardCharsets.UTF_8.decode(bBuff).toString());
        } catch (IOException ex) { WQServerController.gui.updateStatsText(ex.getMessage()); ex.printStackTrace(); }

    }
}
