package com.fexed.lprb.wq.server;

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
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Federico Matteoni
 */
public class WQServer extends RemoteServer implements WQInterface {
    private boolean running;
    private HashMap<String, String> userBase;
    private ArrayList<String> loggedIn;
    private int port;

    @Override
    public int registraUtente(String nickUtente, String password) throws RemoteException {
        //TODO controlli vari
        if (userBase.containsKey(nickUtente)) {
            return -1;
        } else if (password.equals("")) {
            WQServerController.gui.updateStatsText("Tentativo di registrare l'utente \"" + nickUtente + "\" con password vuota.");
            return -2;
        } else {
            userBase.put(nickUtente, password);
            Gson gson = new Gson();
            int n = saveToFile("userBase", gson.toJson(userBase));
            WQServerController.gui.updateStatsText("Utente \"" + nickUtente + "\" registrato con successo!");
            return 0;
        }
    }

    public int login(String nickUtente, String password){
        if (userBase.containsKey(nickUtente)) {
            if (userBase.get(nickUtente).equals(password)) {
                loggedIn.add(nickUtente);
                return 0;
            }
            else return -1;
        } else return -1;
    }

    public void logout(String nickUtente){
        loggedIn.remove(nickUtente);
    }

    public int aggiungiAmico(String nickUtente, String nickAmico){
        return -1;
    }

    public String listaAmici(String nickUtente) {
        return "";
    }

    public void sfida(String nickUtente, String nickAmico){

    }

    public int mostraPunteggio(String nickUtente) {
        return -1;
    }

    public String mostraClassifica(String nickUtente) {
        return "";
    }

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
            Type type = new TypeToken<HashMap<String, String>>(){}.getType();
            userBase = gson.fromJson(reader, type);
        } catch (IOException e) {
            userBase = new HashMap<>();
        }
        loggedIn = new ArrayList<>();
    }

    public String getInfos() {
        String str = "*Utenti registrati:\n";
        for(String key : userBase.keySet()) {
            str = str.concat("*-  " + key + ", " + userBase.get(key) + "\n");
        }

        str = str.concat("*\n*Utenti online:\n");
        for(String key : loggedIn) {
            str = str = str.concat("*-  " + key + "\n");
        }

        return str;
    }

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
