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
        WQClientController.client = this;
        WQInterface wq;
        try {
            Registry r = LocateRegistry.getRegistry(port);
            wq = (WQInterface) r.lookup("WordQuizzle_530527");
            wq.registraUtente("Fexed", "1324");
        } catch (RemoteException | NotBoundException e) {}
    }
}
