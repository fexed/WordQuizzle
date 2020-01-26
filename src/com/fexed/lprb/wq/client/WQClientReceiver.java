package com.fexed.lprb.wq.client;

import com.google.gson.JsonParseException;

import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

/**
 * Thread che ascolta le comunicazioni TCP in arrivo dal server
 * @author Federico Matteoni
 */
public class WQClientReceiver implements Runnable {
    private SocketChannel skt;
    private SelectionKey key;

    public WQClientReceiver(SocketChannel skt, SelectionKey key) {
        this.skt = skt;
        this.key = key;
    }

    @Override
    public void run() {
        try {
            do {
                ByteBuffer buff = ByteBuffer.allocate(128);
                int n;
                do {
                    try { Thread.sleep(100); }
                    catch (InterruptedException ignored) {}
                    buff.clear();
                    n = ((SocketChannel) key.channel()).read(buff);
                } while (n == 0);
                do {
                    n = ((SocketChannel) key.channel()).read(buff);
                } while (n > 0);
                buff.flip();
                String received = StandardCharsets.UTF_8.decode(buff).toString();
                System.out.println(received);

                try {
                    WQClientController.client.receive(received);
                } catch (JsonParseException ex) { WQClientController.client.receive(ex.getMessage()); ex.printStackTrace();}
            } while (true); //TODO fix
        } catch (Exception ex) { WQClientController.client.receive(ex.getMessage()); ex.printStackTrace();}
    }
}
