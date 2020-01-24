package com.fexed.lprb.wq.client;

import com.fexed.lprb.wq.WQInterface;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * @author Federico Matteoni
 */
public class WQClient {
    private int port;
    private SocketChannel skt;
    private SelectionKey keyW;
    private SelectionKey keyR;

    public int login(String name, String password) {
        //INPUT FIX
        name = name.replaceAll(" ", "");
        name = name.replaceAll(":", "");
        password = password.replaceAll(" ", "");
        password = password.replaceAll(":", "");
        try {
            if (register(name, password) > -2) {
                skt = SocketChannel.open();
                WQClientController.gui.updateCommText("Connessione in corso su porta " + port);
                skt.connect(new InetSocketAddress("127.0.0.1", port));
                skt.configureBlocking(false);
                Selector selector = Selector.open();
                keyW = skt.register(selector, SelectionKey.OP_WRITE);
                keyR = skt.register(selector, SelectionKey.OP_READ);

                String str = "login:" + name + " " + password;
                ByteBuffer buff = ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8));
                int n;
                do { n = ((SocketChannel) keyW.channel()).write(buff); } while (n > 0);

                buff = ByteBuffer.allocate(128);
                do { buff.clear(); n = ((SocketChannel) keyR.channel()).read(buff); } while (n == 0);
                do { n = ((SocketChannel) keyR.channel()).read(buff); } while (n > 0);
                buff.flip();
                String received = StandardCharsets.UTF_8.decode(buff).toString();
                System.out.println(received);
                String command = received.split(":")[0];
                if (command.equals("answer")) {
                    if (received.split(":")[1].equals("OK")) {
                        WQClientController.gui.loggedIn(name);
                        return 0;
                    }
                    else return -1;
                }
            } else return -1;
        } catch (IOException e) { WQClientController.gui.updateCommText(e.getMessage()); e.printStackTrace(); }
        return -1;
    }

    public int send(String txt) {
        try {
            ByteBuffer buff = ByteBuffer.wrap(txt.getBytes(StandardCharsets.UTF_8));
            int n;
            do { n = ((SocketChannel) keyW.channel()).write(buff); } while (n > 0);
            WQClientController.gui.updateCommText("(Io): " + txt);
            buff = ByteBuffer.allocate(128);
            do { buff.clear(); n = ((SocketChannel) keyR.channel()).read(buff); } while (n == 0);
            do { n = ((SocketChannel) keyR.channel()).read(buff); } while (n > 0);
            buff.flip();
            String received = StandardCharsets.UTF_8.decode(buff).toString();
            String command = received.split(":")[0];
            if (command.equals("answer")) {
                String str = "";
                for (int i = 1; i < received.split(":").length; i++) {
                    str = str.concat(received.split(":")[i]);
                }
                WQClientController.gui.updateCommText(str);
                return 0;
            } else return -1;
        } catch (IOException ex) { WQClientController.gui.updateCommText(ex.getMessage()); ex.printStackTrace(); }
        return -1;
    }

    public int register(String name, String password) {
        int n;
        WQInterface wq;
        try {
            Registry r = LocateRegistry.getRegistry(port+1);
            wq = (WQInterface) r.lookup("WordQuizzle_530527");
            n = wq.registraUtente(name, password);
            if (n == 0) WQClientController.gui.updateCommText("Utente \"" + name + "\" registrato con successo.");
            else if (n == -2) WQClientController.gui.updateCommText("Errore, password vuota per \"" + name + "\"");
            return n;
        } catch (RemoteException | NotBoundException e) { WQClientController.gui.updateCommText(e.getMessage()); }
        return -3;
    }

    public WQClient(int port) {
        WQClientController.client = this;
        this.port = port;
    }
}
