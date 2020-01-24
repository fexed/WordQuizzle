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
    private SelectionKey keyR, keyW;

    public WQHandler(WQServer server, SocketChannel skt) {
        this.server = server;
        this.skt = skt;
        this.online = true;
    }

    public void send(String str) {
        try {
            ByteBuffer buff = ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8));
            int n;
            do {
                n = ((SocketChannel) keyW.channel()).write(buff);
            } while (n > 0);
        } catch(Exception ignored) {}
    }

    @Override
    public void run() {
        ByteBuffer bBuff = ByteBuffer.allocate(128);
        String str;

        try {
            skt.configureBlocking(false);
            Selector selector = Selector.open();
            keyR = skt.register(selector, SelectionKey.OP_READ);
            keyW = skt.register(selector, SelectionKey.OP_WRITE);
            do {
                int n;
                do {
                    Thread.sleep(50);
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
                    String command = received.split(":")[0];
                    switch (command) {
                        case "login":
                            if (this.username == null) {
                                String name = received.split(":")[1].split(" ")[0];
                                this.username = name;
                                String pwd = received.split(":")[1].split(" ")[1];
                                System.out.println("Verifica " + name + " " + pwd);
                                n = WQServerController.server.login(name, pwd, this);
                                if (n == 0) {
                                    str = "answer:OK";
                                    ByteBuffer buff = ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8));
                                    do {
                                        n = ((SocketChannel) keyW.channel()).write(buff);
                                    } while (n > 0);
                                    WQServerController.gui.updateStatsText(name + " si è connesso.");
                                } else {
                                    str = "answer:ERR";
                                    ByteBuffer buff = ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8));
                                    do {
                                        n = ((SocketChannel) keyW.channel()).write(buff);
                                    } while (n > 0);
                                    online = false;
                                }
                            } else {
                                str = "answer:ERR";
                                ByteBuffer buff = ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8));
                                do {
                                    n = ((SocketChannel) keyW.channel()).write(buff);
                                } while (n > 0);
                            }
                            break;
                        case "showonline": {
                            String json = WQServerController.server.mostraOnline();
                            str = "answer:".concat(json);
                            ByteBuffer buff = ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8));
                            do {
                                n = ((SocketChannel) keyW.channel()).write(buff);
                            } while (n > 0);
                            break;
                        }
                        case "addfriend": {
                            //TODO addfriend
                            str = "answer:OKaddfriend";
                            ByteBuffer buff = ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8));
                            do {
                                n = ((SocketChannel) keyW.channel()).write(buff);
                            } while (n > 0);
                            break;
                        }
                        case "friendlist": {
                            String json = WQServerController.server.listaAmici(this.username);
                            str = "answer:".concat(json);
                            ByteBuffer buff = ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8));
                            do {
                                n = ((SocketChannel) keyW.channel()).write(buff);
                            } while (n > 0);
                            break;
                        }
                        case "points": {
                            int points = WQServerController.server.mostraPunteggio(this.username);
                            str = "answer:".concat(points + "");
                            ByteBuffer buff = ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8));
                            do {
                                n = ((SocketChannel) keyW.channel()).write(buff);
                            } while (n > 0);
                            break;
                        }
                        case "ranking": {
                            String json = WQServerController.server.mostraClassifica(this.username);
                            str = "answer:".concat(json);
                            ByteBuffer buff = ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8));
                            do {
                                n = ((SocketChannel) keyW.channel()).write(buff);
                            } while (n > 0);
                            break;
                        }
                        case "challenge": {
                            //TODO challenge
                            str = "answer:OKchallenge";
                            ByteBuffer buff = ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8));
                            do {
                                n = ((SocketChannel) keyW.channel()).write(buff);
                            } while (n > 0);
                            String name = received.split(":")[1];
                            WQServerController.server.sfida(this.username, name);
                            break;
                        }
                        default:
                            WQServerController.gui.updateStatsText("(" + username + "): " + received);
                            break;
                    }
                }
            } while (online);
        } catch (Exception ex) { ex.printStackTrace(); }
        WQServerController.gui.updateStatsText(this.username + " è andato offline.");
        WQServerController.server.logout(username);
    }
}
