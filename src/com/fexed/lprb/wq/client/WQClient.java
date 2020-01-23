package com.fexed.lprb.wq.client;

import com.fexed.lprb.wq.server.WQInterface;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * @author Federico Matteoni
 */
public class WQClient {
    public WQClient(int port) {
        int n;
        WQClientController.client = this;
        WQInterface wq;
        try {
            Registry r = LocateRegistry.getRegistry(port);
            wq = (WQInterface) r.lookup("WordQuizzle_530527");
            n = wq.registraUtente("Fexed", "1324");
            if (n == 0) WQClientController.gui.updateCommText("Utente \"" + "Fexed" + "\" registrato con successo.");
            else WQClientController.gui.updateCommText("Utente \"" + "Fexed" + "\" già registrato.");
        } catch (RemoteException | NotBoundException e) {}
    }
}
