package com.fexed.lprb.wq.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author Federico Matteoni
 */
public interface WQInterface extends Remote {
    int registraUtente(String nickUtente, String password) throws RemoteException;
}
