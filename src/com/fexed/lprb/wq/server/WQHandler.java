package com.fexed.lprb.wq.server;

import com.fexed.lprb.wq.WQUtente;
import com.google.gson.Gson;

import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Random;

/**
 * Handler della connessione con un singolo client
 * @author Federico Matteoni
 */
public class WQHandler implements Runnable {
    private WQServer server;
    private SocketChannel skt;
    private String username;
    private boolean online;
    private SelectionKey key;
    public int challengePort;

    public WQHandler(WQServer server, SocketChannel skt) {
        this.server = server;
        this.skt = skt;
        this.online = true;
    }

    /**
     * Invia {@code str} al client
     * @param str Il testo da spedire
     */
    public void send(String str) {
        try {
            ByteBuffer buff = ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8));
            int n;
            do {
                n = ((SocketChannel) key.channel()).write(buff);
            } while (n > 0);
        } catch(Exception ignored) {}
    }

    /**
     * Inoltra via UPD la richiesta di sfida proveniente da {@code nickSfidante}
     * @param nickSfidante Il nickname dell'utente che ha richiesto la sfida
     * @return il socket se la richiesta viene accettata, null altrimenti
     */
    public DatagramSocket challenge(String nickSfidante, int port) {
        WQServerController.gui.updateStatsText("(" + this.username + "): " + nickSfidante + " vuole sfidarmi!");
        try {
            DatagramSocket datagramSocket = new DatagramSocket();
            datagramSocket.connect(InetAddress.getLocalHost(), this.challengePort);
            datagramSocket.setSoTimeout(10000); //10s per accettare la sfida
            byte[] buffer = ("challengeRequest:" + nickSfidante).getBytes(StandardCharsets.UTF_8);
            DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
            datagramSocket.send(datagramPacket);

            try {
                buffer = new byte[128];
                datagramPacket = new DatagramPacket(buffer, buffer.length);
                datagramSocket.receive(datagramPacket);
                String received = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(datagramPacket.getData())).toString();
                String response = received.split(":")[1].trim();
                if ("OK".equals(response)) {
                    WQServerController.gui.updateStatsText(this.username + " ha accettato!");
                    return datagramSocket;
                } else {
                    WQServerController.gui.updateStatsText(this.username + " ha rifiutato!");
                    return null;
                }
            } catch (SocketTimeoutException ex) {
                WQServerController.gui.updateStatsText("Timeout!");
                return null;
            }
        } catch (Exception e) { WQServerController.gui.updateStatsText(e.getMessage()); e.printStackTrace(); return null; }
    }

    @Override
    public void run() {
        ByteBuffer bBuff = ByteBuffer.allocate(128);
        String str;

        try {
            skt.configureBlocking(false);
            Selector selector = Selector.open();
            key = skt.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            do {
                int n;
                do {
                    Thread.sleep(100);
                    bBuff.clear();
                    n = ((SocketChannel) key.channel()).read(bBuff);
                } while (n == 0);
                if (n == -1) online = false;
                else {
                    do {
                        n = ((SocketChannel) key.channel()).read(bBuff);
                    } while (n > 0);
                    bBuff.flip();
                    String received = StandardCharsets.UTF_8.decode(bBuff).toString();
                    String command = received.split(":")[0];
                    switch (command) {
                        case "login":
                            if (this.username == null) {
                                String name = received.split(":")[1].split(" ")[0];
                                String pwd = received.split(":")[1].split(" ")[1];
                                System.out.println("Verifica " + name + " " + pwd);
                                n = this.server.login(name, pwd, this);
                                if (n == 0) {
                                    this.username = name;
                                    str = "answer:OK";
                                    ByteBuffer buff = ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8));
                                    do {
                                        n = ((SocketChannel) key.channel()).write(buff);
                                    } while (n > 0);
                                    WQServerController.gui.updateStatsText(name + " si è connesso.");
                                    Gson gson = new Gson();
                                    str = gson.toJson(this.server.ottieniUtente(this.username));
                                    buff = ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8));
                                    do {
                                        n = ((SocketChannel) key.channel()).write(buff);
                                    } while (n > 0);
                                    buff = ByteBuffer.allocate(128);
                                    do {
                                        try { Thread.sleep(100); }
                                        catch (InterruptedException ignored) {}
                                        buff.clear();
                                        n = ((SocketChannel) key.channel()).read(buff);
                                    } while (n == 0);
                                    do {
                                        n = ((SocketChannel) key.channel()).read(buff);
                                    } while (n > 0);
                                    buff.flip();
                                    received = StandardCharsets.UTF_8.decode(buff).toString(); //challengePort;
                                    challengePort = Integer.parseInt(received.split(":")[1]);
                                } else {
                                    str = "answer:ERR";
                                    ByteBuffer buff = ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8));
                                    do {
                                        n = ((SocketChannel) key.channel()).write(buff);
                                    } while (n > 0);
                                    online = false;
                                }
                            } else {
                                str = "answer:ERR";
                                ByteBuffer buff = ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8));
                                do {
                                    n = ((SocketChannel) key.channel()).write(buff);
                                } while (n > 0);
                            }
                            break;
                        case "showonline": {
                            WQServerController.gui.updateStatsText(this.username + " richiede lista online.");
                            String json = this.server.mostraOnline();
                            str = "onlinelist:".concat(json);
                            ByteBuffer buff = ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8));
                            do {
                                n = ((SocketChannel) key.channel()).write(buff);
                            } while (n > 0);
                            break;
                        }
                        case "addfriend": {
                            try {
                                String name = received.split(":")[1];
                                n = this.server.aggiungiAmico(this.username, name);
                                if (n == 0) { //amicizia creata
                                    str = "answer:OKFREN";
                                    ByteBuffer buff = ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8));
                                    do {
                                        n = ((SocketChannel) key.channel()).write(buff);
                                    } while (n > 0);
                                    Gson gson = new Gson();
                                    str = gson.toJson(this.server.ottieniUtente(this.username));
                                    buff = ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8));
                                    do {
                                        n = ((SocketChannel) key.channel()).write(buff);
                                    } while (n > 0);
                                } else {
                                    str = "answer:ERR ";
                                    if (n == -1) str = str.concat(name + " non esistente.");
                                    else if (n == -2) str = str.concat("Sei già amico con " + name);
                                    else if (n == -3)
                                        str = str.concat(this.username + " non esistente."); //non dovrebbe mai succedere
                                    ByteBuffer buff = ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8));
                                    do {
                                        n = ((SocketChannel) key.channel()).write(buff);
                                    } while (n > 0);
                                }
                            } catch (Exception ex) {
                                str = "answer:ERR " + ex.getMessage();
                                ByteBuffer buff = ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8));
                                do {
                                    n = ((SocketChannel) key.channel()).write(buff);
                                } while (n > 0);
                                WQServerController.gui.updateStatsText(ex.getMessage());
                            }
                            break;
                        }
                        case "friendlist": {
                            String json = this.server.listaAmici(this.username);
                            str = "answer:".concat(json);
                            ByteBuffer buff = ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8));
                            do {
                                n = ((SocketChannel) key.channel()).write(buff);
                            } while (n > 0);
                            break;
                        }
                        case "points": {
                            int points = this.server.mostraPunteggio(this.username);
                            str = "answer:".concat(points + "");
                            ByteBuffer buff = ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8));
                            do {
                                n = ((SocketChannel) key.channel()).write(buff);
                            } while (n > 0);
                            break;
                        }
                        case "ranking": {
                            String json = this.server.mostraClassifica(this.username);
                            str = "answer:".concat(json);
                            ByteBuffer buff = ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8));
                            do {
                                n = ((SocketChannel) key.channel()).write(buff);
                            } while (n > 0);
                            break;
                        }
                        case "challenge": {
                            String name = received.split(":")[1];
                            this.server.sfida(this.username, name);
                            break;
                        }
                        default:
                            WQServerController.gui.updateStatsText("(" + username + "): " + received);
                            break;
                    }
                }
            } while (online);
        } catch (Exception ex) { ex.printStackTrace(); }
        if (this.username != null) {
            WQServerController.gui.updateStatsText(this.username + " è andato offline.");
            this.server.logout(username);
        }
    }
}
