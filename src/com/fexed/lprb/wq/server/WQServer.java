package com.fexed.lprb.wq.server;

import com.fexed.lprb.wq.client.WQClient;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

/**
 * @author Federico Matteoni
 */
public class WQServer extends RemoteServer implements WQInterface {
    private HashMap<String, String> userBase;

    @Override
    public int registraUtente(String nickUtente, String password) throws RemoteException {
        //TODO controlli vari
        if (userBase.containsKey(nickUtente)) {
            WQServerController.gui.updateStatsText("Utente \"" + nickUtente + "\" già registrato.");
            return -1;
        } else {
            userBase.put(nickUtente, password);
            WQServerController.gui.updateStatsText("Utente \"" + nickUtente + "\" registrato!");
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

    private void loadServer(int porta) throws RemoteException {
        //RMI
        WQInterface stub = (WQInterface) UnicastRemoteObject.exportObject(this, porta);
        LocateRegistry.createRegistry(porta);
        Registry r = LocateRegistry.getRegistry(porta);
        r.rebind("WordQuizzle_530527", stub);

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

    public WQServer(int porta) {
        try {
            WQServerController.server = this;
            loadServer(porta);
            WQServerController.gui.updateStatsText("Online!");
            WQServerController.gui.serverIsOnline();
        } catch (RemoteException e) {WQServerController.gui.updateStatsText(e.getMessage()); WQServerController.gui.serverIsOffline();}
    }

}
