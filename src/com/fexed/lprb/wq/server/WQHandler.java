package com.fexed.lprb.wq.server;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * Handler della connessione con un singolo client
 * @author Federico Matteoni
 */
public class WQHandler implements Runnable {
    private WQServer server;
    public HashMap<String, String> randomWords;
    public boolean isChallenging = false;
    public int pointsMade;
    private SocketChannel skt;
    public String username;
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
        System.out.println(str + " to " + username);
        try {
            ByteBuffer buff = ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8));
            int n;
            do {
                n = ((SocketChannel) key.channel()).write(buff);
            } while (n > 0);
        } catch(Exception ignored) {}
    }

    public void startChallenge() {
        isChallenging = true;
        this.pointsMade = 0;
        ByteBuffer bBuff = ByteBuffer.allocate(128);
        String str;
        int n;
        try {
            for (String word : randomWords.keySet()) {
                send("challengeRound:" + word);

                do {
                    try { Thread.sleep(100); }
                    catch (InterruptedException ignored) {}
                    bBuff.clear();
                    n = ((SocketChannel) key.channel()).read(bBuff);
                } while (n == 0);
                do {
                    n = ((SocketChannel) key.channel()).read(bBuff);
                } while (n > 0);
                bBuff.flip();
                String received = StandardCharsets.UTF_8.decode(bBuff).toString();
                String command = received.split(":")[0];
                String translatedWord = received.split(":")[1];
                String wordToGet = randomWords.get(word).toLowerCase().replaceAll("!", "");
                WQServerController.gui.updateStatsText("(" + this.username + ", " + word + "): " + translatedWord + " - " + wordToGet);

                if (command.equals("challengeAnswer")) {
                    if (translatedWord.equals("-1")) pointsMade += 0;
                    else if (translatedWord.toLowerCase().contains(wordToGet)) pointsMade += 2;
                    else pointsMade -= 1;
                }
            }
        } catch (IOException ex) { send("ERR: " + ex.getMessage()); }
        send("challengeRound:-3");

        isChallenging = false;
        randomWords = null;
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
            datagramSocket.connect(InetAddress.getByName("127.0.0.1"), this.challengePort);
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
                if (randomWords == null) {
                    int n;
                    do {
                        Thread.sleep(100);
                        bBuff.clear();
                        n = ((SocketChannel) key.channel()).read(bBuff);
                    } while (n == 0 && randomWords == null);
                    if (n == -1) online = false;
                    else if (randomWords == null) {
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
                                            try {
                                                Thread.sleep(100);
                                            } catch (InterruptedException ignored) {
                                            }
                                            buff.clear();
                                            n = ((SocketChannel) key.channel()).read(buff);
                                        } while (n == 0);
                                        do {
                                            n = ((SocketChannel) key.channel()).read(buff);
                                        } while (n > 0);
                                        buff.flip();
                                        received = StandardCharsets.UTF_8.decode(buff).toString(); //challengePort;
                                        challengePort = Integer.parseInt(received.split(":")[1]);
                                    } else if (n == -1) {
                                        str = "answer:ERR1";
                                        ByteBuffer buff = ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8));
                                        do {
                                            n = ((SocketChannel) key.channel()).write(buff);
                                        } while (n > 0);
                                        online = false;
                                    } else if (n == -2) {
                                        str = "answer:ERR2";
                                        ByteBuffer buff = ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8));
                                        do {
                                            n = ((SocketChannel) key.channel()).write(buff);
                                        } while (n > 0);
                                        online = false;
                                    } else if (n == -3) {
                                        str = "answer:ERR3";
                                        ByteBuffer buff = ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8));
                                        do {
                                            n = ((SocketChannel) key.channel()).write(buff);
                                        } while (n > 0);
                                        online = false;
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
                            case "showonlinelist": {
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
                            case "showfriendlist": {
                                String json = this.server.listaAmici(this.username);
                                str = "friendlist:".concat(json);
                                ByteBuffer buff = ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8));
                                do {
                                    n = ((SocketChannel) key.channel()).write(buff);
                                } while (n > 0);
                                break;
                            }
                            case "showpoints": {
                                int points = this.server.mostraPunteggio(this.username);
                                str = "userpoints:".concat(points + "");
                                ByteBuffer buff = ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8));
                                do {
                                    n = ((SocketChannel) key.channel()).write(buff);
                                } while (n > 0);
                                break;
                            }
                            case "showranking": {
                                String json = this.server.mostraClassifica(this.username);
                                str = "ranking:".concat(json);
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
                } else {
                    startChallenge();
                }
            } while (online);
        } catch (Exception ex) { ex.printStackTrace(); }
        if (this.username != null) {
            WQServerController.gui.updateStatsText(this.username + " è andato offline.");
            this.server.logout(username);
        }WQServerController.gui.subThreadsText();
    }
}
