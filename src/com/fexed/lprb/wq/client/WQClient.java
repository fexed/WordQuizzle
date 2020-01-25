package com.fexed.lprb.wq.client;

import com.fexed.lprb.wq.WQInterface;
import com.fexed.lprb.wq.WQUtente;
import com.google.gson.Gson;

import java.io.IOException;
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

/**
 * Il client di WordQuizzle
 * @author Federico Matteoni
 */
public class WQClient {
    private int port;
    private SocketChannel skt;
    private SelectionKey key;

    /**
     * Esegue la registrazione dell'utente e, se è andata a buon fine o se l'utente era già registrato, il login a WordQuizzle.
     * @param name Il nickname con cui eseguire il login
     * @param password La password con cui eseguire il login
     * @return 0 se il login è andato a buon fine, -1 se ci sono stati problemi
     */
    public int login(String name, String password) {
        //INPUT FIX
        name = name.replaceAll(" ", "");
        name = name.replaceAll(":", "");
        name = name.toLowerCase();
        password = password.replaceAll(" ", "");
        password = password.replaceAll(":", "");
        try {
            if (register(name, password) > -2) {
                skt = SocketChannel.open();
                WQClientController.gui.updateCommText("Connessione in corso su porta " + port);
                skt.connect(new InetSocketAddress("127.0.0.1", port));
                skt.configureBlocking(false);
                Selector selector = Selector.open();
                key = skt.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ);

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
                    if (received.split(":")[1].equals("OK")) {
                        WQUtente myUser = null;
                        Gson gson = new Gson();
                        buff = ByteBuffer.allocate(128);
                        do { buff.clear(); n = ((SocketChannel) key.channel()).read(buff); } while (n == 0);
                        do { n = ((SocketChannel) key.channel()).read(buff); } while (n > 0);
                        buff.flip();
                        received = StandardCharsets.UTF_8.decode(buff).toString();
                        myUser = gson.fromJson(received, WQUtente.class);
                        if (myUser != null) {
                            System.out.println(myUser.toString());
                            WQClientController.gui.addAllFriends(myUser.friends);
                        }
                        WQClientController.gui.loggedIn(myUser.username, myUser.points);
                        new Thread(new WQClientReceiver(skt, key)).start();
                        return 0;
                    }
                    else return -1;
                }
            } else return -1;
        } catch (IOException e) { WQClientController.gui.updateCommText(e.getMessage()); e.printStackTrace(); }
        return -1;
    }

    /**
     * Elabora la stringa ricevuta dal server
     * @param received La stringa ricevuta
     * @return 0 se la stringa è stata elaborata correttamente, -1 se la stringa non è valida
     */
    public int receive(String received) {
        String command = received.split(":")[0];
        if (command.equals("answer")) {
            String response = received.split(":")[1];
            if (response.equals("OKFREN")) {
                try {
                    WQUtente myUser = null;
                    int n;
                    Gson gson = new Gson();
                    ByteBuffer buff = ByteBuffer.allocate(128);
                    do {
                        buff.clear();
                        n = ((SocketChannel) key.channel()).read(buff);
                    } while (n == 0);
                    do {
                        n = ((SocketChannel) key.channel()).read(buff);
                    } while (n > 0);
                    buff.flip();
                    received = StandardCharsets.UTF_8.decode(buff).toString();
                    myUser = gson.fromJson(received, WQUtente.class);
                    if (myUser != null) {
                        System.out.println(myUser.toString());
                        WQClientController.gui.addAllFriends(myUser.friends);
                    }
                } catch (Exception ex) {
                    String str = received.substring(command.length() + 1);
                    WQClientController.gui.updateCommText(str);
                    WQClientController.gui.updateCommText(ex.getMessage());
                }
            } else {
                String str = received.substring(command.length() + 1);
                WQClientController.gui.updateCommText(str);
            }
            return 0;
        } else if (command.equals("notif")) {
            String str = received.substring(command.length()+1);
            WQClientController.gui.showTextDialog(str);
            return 0;
        } else return -1;
    }

    /**
     * Invia una stringa di testo al server
     * @param txt Il testo da spedire
     * @return 0 se la comunicazione è avvenuta correttamente, -1 in caso contrario
     */
    public int send(String txt) {
        try {
            ByteBuffer buff = ByteBuffer.wrap(txt.getBytes(StandardCharsets.UTF_8));
            int n;
            do { n = ((SocketChannel) key.channel()).write(buff); } while (n > 0);
            WQClientController.gui.updateCommText("(Io): " + txt);
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
            Registry r = LocateRegistry.getRegistry(port+1);
            wq = (WQInterface) r.lookup("WordQuizzle_530527");
            n = wq.registraUtente(name, password);
            if (n == 0) WQClientController.gui.updateCommText("Utente \"" + name + "\" registrato con successo.");
            else if (n == -2) WQClientController.gui.updateCommText("Errore, password vuota per \"" + name + "\"");
            return n;
        } catch (RemoteException | NotBoundException e) { WQClientController.gui.updateCommText(e.getMessage()); }
        return -3;
    }

    public WQClient(int port) {
        WQClientController.client = this;
        this.port = port;
    }
}
