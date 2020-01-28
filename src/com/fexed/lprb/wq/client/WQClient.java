package com.fexed.lprb.wq.client;

import com.fexed.lprb.wq.WQInterface;
import com.fexed.lprb.wq.WQUtente;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Timer;

/**
 * Il client di WordQuizzle
 * @author Federico Matteoni
 */
public class WQClient {

    /**
     * La porta con la quale si connette al server WordQuizzle
     */
    private int port;

    /**
     * Il socket di comunicazione
     */
    private SocketChannel skt;

    /**
     * Chiave lettura/scrittura per la comunicazione
     */
    private SelectionKey key;

    /**
     * Suffisso per i messaggi da mandare
     */
    private String suffix = "";

    /**
     * Timer per il tempo di risposta durante la sfida
     */
    private Timer answerTimer;

    /**
     * Esegue la registrazione dell'utente e, se è andata a buon fine o se l'utente era già registrato, il login a
     * WordQuizzle.
     * @param name Il nickname con cui eseguire il login
     * @param password La password con cui eseguire il login
     * @return 0 se il login è andato a buon fine, -1 se ci sono stati problemi
     */
    public int login(String name, String password) {
        //Fix dell'input per evitare problemi di parsing
        name = name.replaceAll(" ", "");
        name = name.replaceAll(":", "");
        name = name.toLowerCase();
        password = password.replaceAll(" ", "");
        password = password.replaceAll(":", "");
        try {
            //Registra l'utente se non è già registrato
            if (register(name, password) > -2) {
                //Apre la connessione TCP verso il server
                skt = SocketChannel.open();
                WQClientController.gui.updateCommText("Connessione in corso su porta " + port);
                skt.connect(new InetSocketAddress("127.0.0.1", port));
                skt.configureBlocking(false);
                Selector selector = Selector.open();
                key = skt.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ);

                //Esegue la procedura di login
                String str = "login:" + name + " " + password;
                ByteBuffer buff = ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8));
                int n;
                do { n = ((SocketChannel) key.channel()).write(buff); } while (n > 0);

                buff = ByteBuffer.allocate(128);
                do { buff.clear(); n = ((SocketChannel) key.channel()).read(buff); } while (n == 0);
                do { n = ((SocketChannel) key.channel()).read(buff); } while (n > 0);
                buff.flip();
                String received = StandardCharsets.UTF_8.decode(buff).toString();
                String command = received.split(":")[0];
                if (command.equals("answer")) {
                    //Parsing della risposta:  "answer:OK" oppure "answer:ERRn"
                    if (received.split(":")[1].equals("OK")) {
                        WQUtente myUser = null; //Dati dell'utente loggato

                        //Ricezione e parsing del json
                        Gson gson = new Gson();
                        buff = ByteBuffer.allocate(128);
                        do { buff.clear(); n = ((SocketChannel) key.channel()).read(buff); } while (n == 0);
                        do { n = ((SocketChannel) key.channel()).read(buff); } while (n > 0);
                        buff.flip();
                        received = StandardCharsets.UTF_8.decode(buff).toString();
                        myUser = gson.fromJson(received, WQUtente.class);
                        if (myUser != null) { //Per evitare NullPointerEx
                            System.out.println(myUser.description());
                            WQClientController.gui.addAllFriends(myUser.friends);
                            WQClientController.gui.loggedIn(myUser.username, myUser.points);
                        }
                        //Avvio il hread ricevitore del client
                        new Thread(new WQClientReceiver(skt, key)).start();
                        try {
                            DatagramSocket datagramSocket = new DatagramSocket();
                            new Thread(new WQClientDatagramReceiver(datagramSocket)).start();
                            str = "challengePort:" + datagramSocket.getLocalPort();
                            buff = ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8));
                            do { n = ((SocketChannel) key.channel()).write(buff); } while (n > 0);
                            WQClientController.gui.updateCommText("DatagramReceiver su porta: " + datagramSocket.getLocalPort());
                        } catch (Exception ex) {
                            str = "challengePort:-1";
                            buff = ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8));
                            do { n = ((SocketChannel) key.channel()).write(buff); } while (n > 0);
                            WQClientController.gui.updateCommText("Impossibile avviare il datagramReceiver: " + ex.getMessage());
                        }
                        return 0;
                    } else if (received.split(":")[1].equals("ERR1")) { //L'utente non esiste
                        //Non dovrebbe mai accadere perché se l'utente non esiste si esegue la procedura di registrazione
                        return -1;
                    } else if (received.split(":")[1].equals("ERR2")) { //La password è sbagliata
                        return -2;
                    } else if (received.split(":")[1].equals("ERR3")) { //L'utente risulta già collegato
                        return -3;
                    } else return -4;
                }
            } else return -4; //Errore generico
        } catch (IOException e) { WQClientController.gui.updateCommText(e.getMessage()); e.printStackTrace(); }
        return -4;
    }

    /**
     * Elabora la stringa ricevuta dal server
     * @param received La stringa ricevuta
     * @return 0 se la stringa è stata elaborata correttamente, -1 se la stringa non è valida
     */
    public int receive(String received) {
        //Il server invia sempre stringhe del tipo "comando:dati", per cui controllo il comando per prima cosa
        try {
            String command = received.split(":")[0];
            switch (command) {
                case "answer": //Usato per risposte e messaggi generici
                    String response = received.split(":")[1];
                    if (response.contains("OKFREN")) { //Risposta all'aggiunta di un amico "addfriend:nickAmico"
                        WQClientController.gui.updateCommText("Amico aggiunto con successso!");
                        //Richiedo automaticamente la lista amici, per aggiornala nella GUI
                        send("showfriendlist");
                    } else if (response.contains("challenge")) { //Messaggi riguardanti inizio/fine della sfida di traduzione
                        int points = Integer.parseInt(response.split(" ")[1]);
                        if (response.split(" ")[0].equals("challengeWin")) { //Vincitore della sfida
                            WQClientController.gui.updateCommText("Hai vinto! Sei a " + points + " punti.");
                        } else if (response.split(" ")[0].equals("challengeLose")) { //Sfida persa
                            WQClientController.gui.updateCommText("Hai perso... sei a " + points + " punti.");
                        } else { //Pareggio
                            WQClientController.gui.updateCommText("Sei a " + points + " punti.");
                        }
                        WQClientController.gui.updatePoints(points); //Aggiorno i punti visibili sulla GUI
                    } else { //Tutti gli altri messaggi generici vengono mostrati semplicemente sul log
                        String str = received.substring(command.length() + 1);
                        WQClientController.gui.updateCommText(str);
                    }
                    return 0;
                case "userpoints": //Risposta alla richiesta del punteggio utente attuale
                    try {
                        int n = Integer.parseInt(received.split(":")[1]);
                        WQClientController.gui.updateCommText("Attualmente sei a " + n + " punti.");
                        WQClientController.gui.updatePoints(n);
                    } catch (NumberFormatException ex) { WQClientController.gui.showTextDialog(ex.getMessage()); }
                    return 0;
                case "onlinelist": //Risposta alla richiesta della lista di utenti online
                    String json = received.substring(command.length() + 1);
                    Gson gson = new Gson();
                    Type type = new TypeToken<ArrayList<WQUtente>>() {}.getType();
                    JsonReader reader = new JsonReader(new StringReader(json));
                    reader.setLenient(true);
                    ArrayList<WQUtente> utentiOnline = gson.fromJson(reader, type);
                    WQClientController.gui.updateCommText("Utenti attualmente collegati: ");
                    for (WQUtente user : utentiOnline) {
                        WQClientController.gui.updateCommText("- " + user.username + " (" + user.points + " punti)");
                    }
                    WQClientController.gui.updateCommText("");
                    return 0;
                case "ranking": //Risposta alla richiesta della classifica
                    json = received.substring(command.length() + 1);
                    gson = new Gson();
                    type = new TypeToken<ArrayList<WQUtente>>() {}.getType();
                    reader = new JsonReader(new StringReader(json));
                    reader.setLenient(true);
                    ArrayList<WQUtente> classifica = gson.fromJson(reader, type);
                    WQClientController.gui.updateCommText("Classifica: ");
                    for (WQUtente user : classifica) {
                        WQClientController.gui.updateCommText(user.points + " punti: " + user.username);
                    }
                    WQClientController.gui.updateCommText("");
                    return 0;
                case "friendlist": //Risposta alla richiesta della lista amici
                    json = received.substring(command.length() + 1);
                    gson = new Gson();
                    WQUtente[] friends = gson.fromJson(json, WQUtente[].class);
                    ArrayList<String> friendsNames = new ArrayList<>();
                    WQClientController.gui.updateCommText("Amici:");
                    for (WQUtente friend : friends) {
                        WQClientController.gui.updateCommText("- " + friend.username);
                        friendsNames.add(friend.username);
                    }
                    WQClientController.gui.addAllFriends(friendsNames);
                    return 0;
                case "challengeRound": //Messaggi che arrivato durante la sfida
                    String word = received.split(":")[1];
                    if (word.equals("1")) { //Inizio della sfida, pulisco il log per poter leggere senza distrazioni
                        WQClientController.gui.clearCommText("**** La sfida ha inizio! Preparati.");
                        WQClientDatagramReceiver.isChallenging = true;
                        WQClientController.gui.disableCommands();
                    }
                    else if (word.equals("-1")) { //Fine anomala della sfida
                        WQClientController.gui.updateCommText("***** La sfida è terminata!");
                        WQClientDatagramReceiver.isChallenging = false;
                        WQClientController.gui.enableCommands();
                    }
                    else if (word.equals("-2")) WQClientController.gui.updateCommText("La sfida è stata rifiutata!");
                    else if (word.equals("-3")) { //Completamento della sfida e attesa
                        WQClientController.gui.updateCommText("Fine! Attendi i risultati.");
                        WQClientDatagramReceiver.isChallenging = false;
                        WQClientController.gui.enableCommands();
                    }
                    else { //Parola da tradurre durante la sfida
                        WQClientController.gui.updateCommText("Parola da tradurre: " + word);
                        suffix = "challengeAnswer:"; //Predisposizione per la risposta da mandare
                        answerTimer = new Timer(); //Avvio il timer per la risposta, 5s
                        answerTimer.schedule(new WQClientTimerTask(this), 5000);
                    }
                    return 0;
                default:
                    WQClientController.gui.showTextDialog(received);
                    return -1;
            }
        } catch (NullPointerException ex) {
            WQClientController.gui.showTextDialog(received);
            return -1;
        }
    }

    /**
     * Invia una stringa di testo al server
     * @param txt Il testo da spedire
     * @return 0 se la comunicazione è avvenuta correttamente, -1 in caso contrario
     */
    public int send(String txt) {
        try {
            String sent = suffix.concat(txt); //Preparazione della stringa da spedire
            ByteBuffer buff = ByteBuffer.wrap(sent.getBytes(StandardCharsets.UTF_8));
            if (suffix.equals("challengeAnswer:")) {
                answerTimer.cancel();
                answerTimer = null;
            }
            suffix = "";
            int n;
            do { n = ((SocketChannel) key.channel()).write(buff); } while (n > 0); //Spedizione

            //Abbellimento dell'output
            if (sent.equals("challengeAnswer:-1")) WQClientController.gui.updateCommText("(Io): ...");
            else WQClientController.gui.updateCommText("(Io): " + txt);
            return 0;
        } catch (IOException ex) { WQClientController.gui.updateCommText(ex.getMessage()); ex.printStackTrace(); }
        return -1;
    }

    /**
     * Avvia la procedura di registrazione a WordQuizzle
     * @param name Il nickname con cui registrarsi
     * @param password La password con cui registrarsi
     * @return 0 se la registrazione è avvenuta con successo, -1 se l'utente è già registrato, -2 se la password è vuota, -3 se ci sono errori di comunicazione
     */
    public int register(String name, String password) {
        int n;
        WQInterface wq;
        try {
            Registry r = LocateRegistry.getRegistry(port+1); //Connessione all'RMI
            wq = (WQInterface) r.lookup("WordQuizzle_530527");
            n = wq.registraUtente(name, password);
            if (n == 0) WQClientController.gui.updateCommText("Utente \"" + name + "\" registrato con successo.");
            else if (n == -2) WQClientController.gui.updateCommText("Errore, password vuota per \"" + name + "\"");
            return n;
        } catch (RemoteException | NotBoundException e) { WQClientController.gui.updateCommText(e.getMessage()); }
        return -3;
    }

    /**
     * Costruttore del client
     * @param port La porta alla quale connettersi
     */
    public WQClient(int port) {
        WQClientController.client = this; //Registrazione al controller
        this.port = port;
    }
}
