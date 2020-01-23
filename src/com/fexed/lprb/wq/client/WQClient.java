package com.fexed.lprb.wq.client;

import com.fexed.lprb.wq.server.WQInterface;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * @author Federico Matteoni
 */
public class WQClient {
    private int port;

    public int login(String name, String password) {
        int n;
        WQInterface wq;
        try {
            Registry r = LocateRegistry.getRegistry(port+1);
            wq = (WQInterface) r.lookup("WordQuizzle_530527");
            n = wq.registraUtente(name, password);
            if (n == 0) WQClientController.gui.updateCommText("Utente \"" + name + "\" registrato con successo.");
            else if (n == -1) WQClientController.gui.updateCommText("Utente \"" + name + "\" già registrato.");
            else if (n == -2) WQClientController.gui.updateCommText("Errore, password vuota per \"" + name + "\"");
            return n;
        } catch (RemoteException | NotBoundException e) { WQClientController.gui.updateCommText(e.getMessage()); }
        return -3;
    }

    public WQClient(int port) {
        WQClientController.client = this;
        this.port = port;
        try {
            SocketChannel skt = SocketChannel.open();
            WQClientController.gui.updateCommText("Connessione in corso su porta " + port);
            skt.connect(new InetSocketAddress("127.0.0.1", port));
            skt.configureBlocking(false);
        } catch (IOException e) { WQClientController.gui.updateCommText(e.getMessage()); e.printStackTrace(); }
    }
}
