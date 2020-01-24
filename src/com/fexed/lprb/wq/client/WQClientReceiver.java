package com.fexed.lprb.wq.client;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class WQClientReceiver implements Runnable {
    private SocketChannel skt;
    private SelectionKey keyR;

    public WQClientReceiver(SocketChannel skt, SelectionKey keyR) {
        this.skt = skt;
        this.keyR = keyR;
    }

    @Override
    public void run() {
        try {
            do {
                ByteBuffer buff = ByteBuffer.allocate(128);
                int n;
                do {
                    buff.clear();
                    n = ((SocketChannel) keyR.channel()).read(buff);
                } while (n == 0);
                do {
                    n = ((SocketChannel) keyR.channel()).read(buff);
                } while (n > 0);
                buff.flip();
                String received = StandardCharsets.UTF_8.decode(buff).toString();
                WQClientController.client.receive(received);
            } while (true); //TODO fix
        } catch (Exception ex) { WQClientController.client.receive(ex.getMessage()); }
    }
}