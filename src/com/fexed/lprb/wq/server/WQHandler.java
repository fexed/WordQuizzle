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
    private String username;
    private boolean online;

    public WQHandler(WQServer server, SocketChannel skt) {
        this.server = server;
        this.skt = skt;
        this.online = true;
    }

    @Override
    public void run() {
        ByteBuffer bBuff = ByteBuffer.allocate(128);
        String str;
        SelectionKey keyR, keyW;

        try {
            skt.configureBlocking(false);
            Selector selector = Selector.open();
            keyR = skt.register(selector, SelectionKey.OP_READ);
            keyW = skt.register(selector, SelectionKey.OP_WRITE);
            do {
                int n;
                do {
                    bBuff.clear();
                    n = ((SocketChannel) keyR.channel()).read(bBuff);
                } while (n == 0);
                if (n == -1) online = false;
                else {
                    do {
                        n = ((SocketChannel) keyR.channel()).read(bBuff);
                    } while (n > 0);
                    bBuff.flip();
                    String received = StandardCharsets.UTF_8.decode(bBuff).toString();
                    System.out.println("Ricevo " + received);
                    String command = received.split(":")[0];
                    if (command.equals("login")) {
                        String name = received.split(":")[1].split(" ")[0];
                        this.username = name;
                        String pwd = received.split(":")[1].split(" ")[1];
                        System.out.println("Verifica " + name + " " + pwd);
                        n = WQServerController.server.login(name, pwd);
                        if (n == 0) {
                            str = "answer:OK";
                            ByteBuffer buff = ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8));
                            do { n = ((SocketChannel) keyW.channel()).write(buff); } while (n > 0);
                            WQServerController.gui.updateStatsText(name + " si è connesso.");
                        }
                        else {
                            str = "answer:ERR";
                            ByteBuffer buff = ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8));
                            do { n = ((SocketChannel) keyW.channel()).write(buff); } while (n > 0);
                            online = false;
                        }
                    } else {
                        WQServerController.gui.updateStatsText("(" + username + "): " + received);
                    }
                }
            } while (online);
        } catch (IOException ex) { ex.printStackTrace(); }
        WQServerController.gui.updateStatsText(this.username + " è andato offline.");
        WQServerController.server.logout(username);
    }
}
