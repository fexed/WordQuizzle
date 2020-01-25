package com.fexed.lprb.wq;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interfaccia di registrazione a WordQuizzle
 * @author Federico Matteoni
 */
public interface WQInterface extends Remote {
    /**
     * Operazione per inserire un nuovo utente.
     * @param nickUtente Il nickname dell'utente da aggiungere
     * @param password La password dell'utente da aggiungere
     * @return 0 se l'aggiunta va a buon fine, -1 se l'utente è già esistente, -2 se la password è vuota
     * @throws RemoteException
     */
    int registraUtente(String nickUtente, String password) throws RemoteException;
}
