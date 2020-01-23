package com.fexed.lprb.wq.server;

import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.UnicastRemoteObject;
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
    private int port;

    @Override
    public int registraUtente(String nickUtente, String password) throws RemoteException {
        //TODO controlli vari
        if (userBase.containsKey(nickUtente)) {
            WQServerController.gui.updateStatsText("Tentativo di registrare l'utente \"" + nickUtente + "\" già registrato.");
            return -1;
        } else if (password.equals("")) {
            WQServerController.gui.updateStatsText("Tentativo di registrare l'utente \"" + nickUtente + "\" con password vuota.");
            return -2;
        } else {
            userBase.put(nickUtente, password);
            WQServerController.gui.updateStatsText("Utente \"" + nickUtente + "\" registrato con successo!");
            return 0;
        }
    }

    private int login(String nickUtente, String password){
        return -1;
    }

    private void logout(String nickUtente){

    }

    private int aggiungiAmico(String nickUtente, String nickAmico){
        return -1;
    }

    private String listaAmici(String nickUtente) {
        return "";
    }

    private void sfida(String nickUtente, String nickAmico){

    }

    private int mostraPunteggio(String nickUtente) {
        return -1;
    }

    private String mostraClassifica(String nickUtente) {
        return "";
    }

    private void loadServer() throws RemoteException {
        //RMI
        WQInterface stub = (WQInterface) UnicastRemoteObject.exportObject(this, port+1);
        LocateRegistry.createRegistry(port+1);
        Registry r = LocateRegistry.getRegistry(port+1);
        r.rebind("WordQuizzle_530527", stub);
        WQServerController.gui.updateStatsText("Registrazioni aperte su porta " + (port+1));

        //TODO load from file
        userBase = new HashMap<>();
    }

    public String getInfos() {
        String str = "Utenti registrati:\n";
        for(String key : userBase.keySet()) {
            str = str.concat("-  " + key + ", " + userBase.get(key));
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
