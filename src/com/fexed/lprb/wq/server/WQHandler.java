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

    /**
     * Riferimento al server
     */
    private WQServer server;

    /**
     * Lista delle parole da tradurre per la sfida, null finché non inizia la sfida
     */
    public HashMap<String, String> randomWords;

    /**
     * Indica se l'utente è nel corso di una sfida
     */
    public boolean isChallenging = false;

    /**
     * Punteggio ottenuto durante la singola sfida
     */
    public int pointsMade;

    /**
     * Canale di comunicazione
     */
    private SocketChannel skt;

    /**
     * Il nickname dell'utente che viene gestito da questo handler
     */
    public String username;

    /**
     * Indica se l'handler deve lavorare o disconnettersi
     */
    private boolean online;

    /**
     * Chiave per la comunicazione
     */
    private SelectionKey key;

    /**
     * Porta UDP su cui inoltrare le richieste di sfida
     */
    public int challengePort;

    /**
     * Costruttore che prepara gli attributi principali
     * @param server Riferimento al server
     * @param skt Socket di comunicazione
     */
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
     * Avvia la sfida con il singolo client. Questo metodo si preoccupa di inviare le parole da tradurre, attendere
     * la traduzione ricevuta e calcolare il punteggio ottenuto dal singolo client.
     */
    public void startChallenge() {
        isChallenging = true; //Segnala che l'utente è nel mezzo di una sfida
        this.pointsMade = 0; //Inizializza il punteggio
        ByteBuffer bBuff = ByteBuffer.allocate(128);
        int n;
        try {
            for (String word : randomWords.keySet()) {
                send("challengeRound:" + word); //Manda la parola

                //Attende e legge la risposta
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
                String command = received.split(":")[0]; //Per verifica che il client risponda correttamente
                String translatedWord = received.split(":")[1]; //Ricevo "challengeAnswer:<qualcosa>"
                String wordToGet = randomWords.get(word).toLowerCase().replaceAll("!", "");
                WQServerController.gui.updateStatsText("(" + this.username + ", " + word + "): " + translatedWord + " - " + wordToGet);

                if (command.equals("challengeAnswer")) {
                    if (translatedWord.equals("-1")) pointsMade += 0; //Timeout, non ho risposto, 0 punti
                    else if (translatedWord.toLowerCase().contains(wordToGet)) pointsMade += 2; //Ho risposto bene, +X
                    else pointsMade -= 1; //Ho risposto sbagliando, -Y punti
                }
            }
        } catch (IOException ex) { send("ERR: " + ex.getMessage()); }
        send("challengeRound:-3");

        isChallenging = false; //Segnala che l'utente non è più all'interno della sfida
        randomWords = null;
    }

    /**
     * Inoltra via UPD la richiesta di sfida proveniente da {@code nickSfidante}
     * @param nickSfidante Il nickname dell'utente che ha richiesto la sfida
     * @return il socket se la richiesta viene accettata, {@code null} altrimenti
     */
    public DatagramSocket challenge(String nickSfidante, int port) {
        WQServerController.gui.updateStatsText("(" + this.username + "): " + nickSfidante + " vuole sfidarmi!");
        try {
            //Invio della richiesta di sfida "challengeRequest:<nickSfidante>"
            DatagramSocket datagramSocket = new DatagramSocket();
            datagramSocket.connect(InetAddress.getByName("127.0.0.1"), this.challengePort);
            datagramSocket.setSoTimeout(10000); //10s per accettare la sfida
            byte[] buffer = ("challengeRequest:" + nickSfidante).getBytes(StandardCharsets.UTF_8);
            DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
            datagramSocket.send(datagramPacket);

            //Attesa della risposta o dello scadere del timeout
            try {
                buffer = new byte[128];
                datagramPacket = new DatagramPacket(buffer, buffer.length);
                datagramSocket.receive(datagramPacket);
                String received = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(datagramPacket.getData())).toString();
                //Ricevo "challengeResponse:OK" oppure "challengeResponse:NO"
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
        //Inizializzazione dati di comunicazione
        ByteBuffer bBuff = ByteBuffer.allocate(128);
        String str;

        try {
            skt.configureBlocking(false);
            Selector selector = Selector.open();
            key = skt.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            do {
                if (randomWords == null) { //Se non ho una sfida in attesa
                    int n;
                    do {
                        Thread.sleep(100);
                        bBuff.clear();
                        n = ((SocketChannel) key.channel()).read(bBuff);
                    } while (n == 0 && randomWords == null);
                    if (n == -1) online = false; //Disconnessione
                    else if (randomWords == null) {
                        // /Ricontrollo perché durante l'attesa+lettura potrei aver ricevuto una richiesta di sfida
                        do {
                            n = ((SocketChannel) key.channel()).read(bBuff);
                        } while (n > 0);
                        bBuff.flip();

                        //Parsing della stringa ricevuta
                        String received = StandardCharsets.UTF_8.decode(bBuff).toString();
                        String command = received.split(":")[0];
                        switch (command) {
                            case "login": //"login:<nomeutente> <password>"
                                if (this.username == null) {
                                    String name = received.split(":")[1].split(" ")[0];
                                    String pwd = received.split(":")[1].split(" ")[1];
                                    System.out.println("Verifica " + name + " " + pwd);
                                    n = this.server.login(name, pwd, this);
                                    if (n == 0) { //Login a buon fine
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
                                        received = StandardCharsets.UTF_8.decode(buff).toString(); //challengePort
                                        challengePort = Integer.parseInt(received.split(":")[1]);
                                    } else if (n == -1) { //Segnalazione degli errori
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
                                } else { //Questo handler sta già gestendo un utente
                                    str = "answer:ERR";
                                    ByteBuffer buff = ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8));
                                    do {
                                        n = ((SocketChannel) key.channel()).write(buff);
                                    } while (n > 0);
                                }
                                break;
                            case "showonlinelist": { //Richiesta della lista amici
                                String json = this.server.mostraOnline();
                                str = "onlinelist:".concat(json);
                                ByteBuffer buff = ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8));
                                do {
                                    n = ((SocketChannel) key.channel()).write(buff);
                                } while (n > 0);
                                break;
                            }
                            case "addfriend": { //Aggiunta di un amico
                                try {
                                    String name = received.split(":")[1];
                                    n = this.server.aggiungiAmico(this.username, name);
                                    if (n == 0) { //L'amicizia viene creata con successo
                                        str = "answer:OKFREN";
                                        ByteBuffer buff = ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8));
                                        do {
                                            n = ((SocketChannel) key.channel()).write(buff);
                                        } while (n > 0);
                                    } else { //Segnalazione degli errori
                                        str = "answer:ERR ";
                                        if (n == -1) str = str.concat(name + " non esistente.");
                                        else if (n == -2) str = str.concat("Sei già amico con " + name);
                                        else if (n == -3)
                                            str = str.concat(this.username + " non esistente."); //non dovrebbe mai succedere
                                        else if (n == -4) str = str.concat("Non puoi aggiungere te stesso come amico");
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
                            case "showfriendlist": { //Richiesta della lista amici
                                String json = this.server.listaAmici(this.username);
                                str = "friendlist:".concat(json);
                                ByteBuffer buff = ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8));
                                do {
                                    n = ((SocketChannel) key.channel()).write(buff);
                                } while (n > 0);
                                break;
                            }
                            case "showpoints": { //Richiesta del punteggio utente
                                int points = this.server.mostraPunteggio(this.username);
                                str = "userpoints:".concat(points + "");
                                ByteBuffer buff = ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8));
                                do {
                                    n = ((SocketChannel) key.channel()).write(buff);
                                } while (n > 0);
                                break;
                            }
                            case "showranking": { //Richiesta della classifica
                                String json = this.server.mostraClassifica(this.username);
                                str = "ranking:".concat(json);
                                ByteBuffer buff = ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8));
                                do {
                                    n = ((SocketChannel) key.channel()).write(buff);
                                } while (n > 0);
                                break;
                            }
                            case "challenge": { //Richiesta di sfida
                                String name = received.split(":")[1];
                                this.server.sfida(this.username, name);
                                break;
                            }
                            default: //Messaggio di testo generico
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
