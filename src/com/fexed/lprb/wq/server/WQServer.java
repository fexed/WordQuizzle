package com.fexed.lprb.wq.server;

import com.fexed.lprb.wq.WQInterface;
import com.fexed.lprb.wq.WQUtente;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.lang.reflect.Type;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Federico Matteoni
 */
public class WQServer extends RemoteServer implements WQInterface {
    private boolean running;
    private HashMap<String, WQUtente> userBase;
    private ArrayList<String> loggedIn;
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
        if (userBase.containsKey(nickUtente)) {
            return -1;
        } else if (password.equals("")) {
            WQServerController.gui.updateStatsText("Tentativo di registrare l'utente \"" + nickUtente + "\" con password vuota.");
            return -2;
        } else {
            WQUtente user = new WQUtente(nickUtente, password);
            userBase.put(nickUtente, user);
            Gson gson = new Gson();
            int n = saveToFile("userBase", gson.toJson(userBase));
            WQServerController.gui.updateStatsText("Utente \"" + nickUtente + "\" registrato con successo!");
            return 0;
        }
    }

    /**
     * Procedura di login per un utente già registrato
     * @param nickUtente Il nickname dell'utente
     * @param password La password dell'utente
     * @return 0 se il login va a buon fine, -1 se ci sono errori
     */
    public int login(String nickUtente, String password){
        if (userBase.containsKey(nickUtente)) {
            if (userBase.get(nickUtente).password.equals(password)) {
                if (!loggedIn.contains(nickUtente)) {
                    loggedIn.add(nickUtente);
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
     * @return JSON rappresentante la lista degli utenti online ({@code ArraList<WQUtente>})
     */
    public String mostraOnline(){
        ArrayList<String> online = new ArrayList<>(loggedIn);
        Gson gson = new Gson();
        return gson.toJson(online);
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
        Gson gson = new Gson();
        return gson.toJson(listaOrdinata);
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
        } catch (IOException e) {
            userBase = new HashMap<>();
        }
        loggedIn = new ArrayList<>();
    }

    /**
     * Restituisce una stringa con varie info sullo stato attuale del server
     * @return Le info
     */
    public String getInfos() {
        //TODO migliorare getInfos()
        String str = "*Utenti registrati:\n";
        for(String key : userBase.keySet()) {
            str = str.concat("*-  " + key + ", " + userBase.get(key).points + "\n");
        }

        str = str.concat("*\n*Utenti online:\n");
        for(String key : loggedIn) {
            str = str = str.concat("*-  " + key + "\n");
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
            loadServer();
            srvSkt = ServerSocketChannel.open();
            srvSkt.socket().bind(new InetSocketAddress(porta));
            srvSkt.configureBlocking(false);
            WQServerController.gui.updateStatsText("Online!");
            WQServerController.gui.serverIsOnline();

            WQServerController.gui.updateStatsText("In ascolto su " + this.port);
            do {
                skt = srvSkt.accept();
                if (skt != null) threadPool.execute(new WQHandler(this, skt));
                else Thread.sleep(100);
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
