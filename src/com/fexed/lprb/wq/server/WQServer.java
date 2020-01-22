package com.fexed.lprb.wq.server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.UnicastRemoteObject;

/**
 * @author Federico Matteoni
 */
public class WQServer extends RemoteServer implements WQInterface {
    @Override
    public int registraUtente(String nickUtente, String password) throws RemoteException {
        return -1;
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

    public WQServer(int porta) {
        try {
            WQServerController.server = this;
            WQInterface stub = (WQInterface) UnicastRemoteObject.exportObject(this, porta);
            LocateRegistry.createRegistry(porta);
            Registry r = LocateRegistry.getRegistry(porta);
            r.rebind("WordQuizzle_530527", stub);
            WQServerController.gui.updateStatsText("Online!");
        } catch (RemoteException e) {e.printStackTrace();}
    }

}
