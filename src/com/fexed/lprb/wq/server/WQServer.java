package com.fexed.lprb.wq.server;

import com.fexed.lprb.wq.WQInterface;
import com.fexed.lprb.wq.WQUtente;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.lang.reflect.Type;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Il server di WordQuizzle
 * @author Federico Matteoni
 */
public class WQServer extends RemoteServer implements WQInterface {
    /**
     * Flag per tenere il server in esecuzione o avviare la procedura di spegnimento
     */
    private boolean running;

    /**
     * Gli utenti registrati
     */
    private HashMap<String, WQUtente> userBase;

    /**
     * Gli utenti attualmente collegati
     */
    private HashMap<String, WQHandler> loggedIn;

    /**
     * La porta di ascolto del server
     */
    private int port;

    /**
     * Operazione per inserire un nuovo utente.
     * @param nickUtente Il nickname dell'utente da aggiungere
     * @param password La password dell'utente da aggiungere
     * @return 0 se l'aggiunta va a buon fine, -1 se l'utente è già esistente, -2 se la password è vuota
     * @throws RemoteException
     */
    @Override
    public int registraUtente(String nickUtente, String password) throws RemoteException {
        //TODO controlli vari
        if (userBase.containsKey(nickUtente.toLowerCase())) {
            return -1;
        } else if (password.equals("")) {
            WQServerController.gui.updateStatsText("Tentativo di registrare l'utente \"" + nickUtente + "\" con password vuota.");
            return -2;
        } else {
            WQUtente user = new WQUtente(nickUtente, password);
            userBase.put(nickUtente, user);
            WQServerController.gui.addRegistered(user);
            saveServer();
            WQServerController.gui.updateStatsText("Utente \"" + nickUtente + "\" registrato con successo!");
            return 0;
        }
    }

    /**
     * Ritorna l'utente desiderato
     * @param nickUtente L'utente da ottenere
     * @return L'utente
     */
    public WQUtente ottieniUtente(String nickUtente) {
        return userBase.get(nickUtente);
    }

    /**
     * Procedura di login per un utente già registrato
     * @param nickUtente Il nickname dell'utente
     * @param password La password dell'utente
     * @return 0 se il login va a buon fine, -1 se ci sono errori
     */
    public int login(String nickUtente, String password, WQHandler handler){
        if (userBase.containsKey(nickUtente.toLowerCase())) {
            if (userBase.get(nickUtente.toLowerCase()).password.equals(password)) {
                if (!loggedIn.keySet().contains(nickUtente.toLowerCase())) {
                    loggedIn.put(nickUtente, handler);
                    WQServerController.gui.addOnline(nickUtente);
                    return 0;
                } else return -1; //TODO codice di errore login
            }
            else return -1;
        } else return -1;
    }

    /**
     * Effettua il logout dell'utente dal servizio
     * @param nickUtente Il nickname dell'utente che si vuole scollegare
     */
    public void logout(String nickUtente){
        loggedIn.remove(nickUtente);
        WQServerController.gui.removeOnline(nickUtente);
    }

    /**
     * Visualizza la lista degli utenti attualmente online
     * @return JSON rappresentante la lista degli utenti online ({@code ArraList<String>})
     */
    public String mostraOnline(){
        ArrayList<String> online = new ArrayList<>(loggedIn.keySet());
        Gson gson = new Gson();
        String json = gson.toJson(online);
        return json;
    }

    /**
     * Usata da un utente per aggiungerne un altro alla propria lista amici. L'operazione aggiunge {@code nickAmico} alla lista amici di {@code nickUtente} e viceversa. Non è necessario che {@code nickAmico} accetti l'amicizia.
     * @param nickUtente L'utente che vuole aggiungere alla propria lista amici
     * @param nickAmico L'utente da aggiungere alla lista amici
     * @return 0 se l'operazione va a buon fine, -1 se {@code nickAmico} non esiste, -2 se l'amicizia è già esistente, -3 se {@code nickUtente} non esiste
     */
    public int aggiungiAmico(String nickUtente, String nickAmico){
        if (userBase.containsKey(nickUtente)) {
            if (userBase.containsKey(nickAmico)) {
                if (!userBase.get(nickUtente).friends.contains(userBase.get(nickAmico).username)) {
                    userBase.get(nickUtente).friends.add(nickAmico);
                    userBase.get(nickAmico).friends.add(nickUtente);
                    saveServer();
                    return 0;
                } else return -2;
            } else return -1;
        } else return -3;
    }

    /**
     * Usata per visualizzare la propria lista amici
     * @param nickUtente L'utente che vuole visualizzare la propria lista amici
     * @return JSON rappresentante la lista amici ({@code ArraList<WQUtente>})
     */
    public String listaAmici(String nickUtente) {
        ArrayList<WQUtente> friendList = new ArrayList<>();
        for (String name : userBase.get(nickUtente).friends) {
            friendList.add(userBase.get(name));
        }
        Gson gson = new Gson();
        return gson.toJson(friendList.toArray());
    }

    /**
     * {@code nickUtente} sfida {@code nickAmico} se quest'ultimo appartiene alla lista amici.
     * @param nickUtente
     * @param nickAmico
     */
    public void sfida(String nickUtente, String nickAmico){
        //TODO
        WQServerController.gui.updateStatsText("Sfida da " + nickUtente + " a " + nickAmico + "!");
        if (userBase.containsKey(nickAmico)) { //se esiste
            if (userBase.get(nickUtente).friends.contains(nickAmico)) { //se è amico
                if (loggedIn.containsKey(nickAmico)) { //se è online
                    WQServerController.gui.updateStatsText("Richiesta mandata a " + nickAmico);
                    DatagramSocket dtgSkt = loggedIn.get(nickAmico).challenge(nickUtente, this.port);
                    if (dtgSkt != null) {
                        WQServerController.gui.updateStatsText("Che abbia inizio la sfida tra " + nickUtente + " e " + nickAmico + "!");
                        WQHandler sfidanteUtente = loggedIn.get(nickUtente);
                        WQHandler sfidanteAmico = loggedIn.get(nickAmico);
                    } else {
                        WQServerController.gui.updateStatsText("Sfida tra " + nickUtente + " e " + nickAmico + " rifiutata!");
                    }
                }
            }
        }
    }

    /**
     * Restituisce il punteggio di {@code nickUtente}
     * @param nickUtente L'utente di cui si vuole sapere il punteggio
     * @return Il punteggio di {@code nickUtente}
     */
    public int mostraPunteggio(String nickUtente) {
        return userBase.get(nickUtente).points;
    }

    /**
     * Restituisce un JSON rappresentante la classifica degli utenti amici di {@code nickUtente}.
     * @param nickUtente L'utente che vuole conoscere la classifica
     * @return JSON rappresentante la classifica
     */
    public String mostraClassifica(String nickUtente) {
        ArrayList<WQUtente> listaOrdinata = new ArrayList<>();
        for (String name : userBase.get(nickUtente).friends) {
            listaOrdinata.add(userBase.get(name));
        }
        listaOrdinata.add(userBase.get(nickUtente));
        listaOrdinata.sort(new Comparator<WQUtente>() {
            @Override
            public int compare(WQUtente o1, WQUtente o2) {
                return Integer.compare(o1.points, o2.points);
            }
        });
        ArrayList<String> lista = new ArrayList<>();
        for (WQUtente usr : listaOrdinata) {
            lista.add(usr.username);
        }
        Gson gson = new Gson();
        return gson.toJson(lista);
    }

    /**
     * Salva {@code fileData} su {@code fileName}
     * @param filename Il nome del file su cui salvare
     * @param fileData I dati da salvare
     * @return 0 se il salvataggio è andato a buon fine, -1 se il file non può essere creato o è una directory, -2 se c'è IOException
     */
    private int saveToFile(String filename, String fileData) {
        System.out.println("Saving " + fileData + " to " + filename);
        try {
            FileOutputStream fileout = new FileOutputStream(new File(filename));
            fileout.write(fileData.getBytes(StandardCharsets.UTF_8));
            return 0;
        } catch (FileNotFoundException ex) {
            return -1;
        } catch (IOException ex) {
            return -2;
        }
    }

    /**
     * Inizializza il WQServer
     * @throws RemoteException
     */
    private void loadServer() throws RemoteException {
        //RMI
        WQInterface stub = (WQInterface) UnicastRemoteObject.exportObject(this, port+1);
        LocateRegistry.createRegistry(port+1);
        Registry r = LocateRegistry.getRegistry(port+1);
        r.rebind("WordQuizzle_530527", stub);
        WQServerController.gui.updateStatsText("Registrazioni aperte su porta " + (port+1));

        try {
            FileInputStream userBaseFile = new FileInputStream(new File("userBase"));
            String userBaseJson = "";
            byte[] buff = new byte[512];
            ByteBuffer bBuff;
            int n;
            do {
                n = userBaseFile.read(buff);
                bBuff = ByteBuffer.wrap(buff);
                userBaseJson = userBaseJson.concat(StandardCharsets.UTF_8.decode(bBuff).toString());
            } while (n > -1);
            Gson gson = new Gson();
            JsonReader reader = new JsonReader(new StringReader(userBaseJson));
            reader.setLenient(true);
            Type type = new TypeToken<HashMap<String, WQUtente>>(){}.getType();
            userBase = gson.fromJson(reader, type);
            WQServerController.gui.addAllRegistered(userBase.values());
        } catch (IOException e) {
            userBase = new HashMap<>();
        }
        loggedIn = new HashMap<>();
    }

    /**
     * Salva su file i dati degli utenti del server
     */
    public void saveServer() {
        Gson gson = new Gson();
        int n = saveToFile("userBase", gson.toJson(userBase));
    }

    /**
     * Restituisce una stringa con varie info sullo stato attuale del server
     * @return Le info
     */
    public String getInfos() {
        //TODO migliorare getInfos()
        String str = "\n\nWQServer!\n";
        str = str.concat("Con " + loggedIn.size() + " utenti connessi su " + userBase.size() + " registrati\n");
        str = str.concat("Online sulla porta " + this.port + " e " + (this.port+1) + "\n");
        str = str.concat("Utenti registrati:\n");
        for(WQUtente usr : userBase.values()) {
            str = str.concat("-  " + usr.username + "\n   " + usr.points + " punti, " + usr.friends.size() + " amici\n");
        }

        return str;
    }

    /**
     * Ferma l'esecuzione del server
     */
    public void stopServer() {
        this.running = false;
    }

    public WQServer(int porta) {
        ServerSocketChannel srvSkt = null;
        SocketChannel skt = null;
        ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(25);
        this.port = porta;
        this.running = true;
        WQServerController.server = this;

        try {
            WQServerController.gui.updateStatsText(Time.from(Calendar.getInstance().toInstant()).toString());
            loadServer();
            srvSkt = ServerSocketChannel.open();
            srvSkt.socket().bind(new InetSocketAddress(porta));
            srvSkt.configureBlocking(false);
            WQServerController.gui.updateStatsText("Online!");
            WQServerController.gui.serverIsOnline(port);

            WQServerController.gui.updateStatsText("In ascolto su " + this.port);
            do {
                skt = srvSkt.accept();
                if (skt != null) threadPool.execute(new WQHandler(this, skt));
                else Thread.sleep(500);
            } while (running);

            WQServerController.gui.updateStatsText("Spegnimento...");
            threadPool.shutdown();
            try { threadPool.awaitTermination(1, TimeUnit.SECONDS); }
            catch (InterruptedException ignored) {}
            srvSkt.close();
            WQServerController.gui.serverIsOffline();
        } catch (Exception e) {WQServerController.gui.updateStatsText(e.getMessage()); WQServerController.gui.serverIsOffline(); e.printStackTrace();}
    }

}
