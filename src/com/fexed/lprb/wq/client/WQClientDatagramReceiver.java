package com.fexed.lprb.wq.client;

import javax.swing.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Thread che ascolta i pacchetti UDP in arrivo dal server
 * @author Federico Matteoni
 */
public class WQClientDatagramReceiver implements Runnable {
    private DatagramSocket datagramSocket;

    public WQClientDatagramReceiver(DatagramSocket datagramSocket) {
        this.datagramSocket = datagramSocket;
    }

    @Override
    public void run() {
        byte[] buff = new byte[128];
        DatagramPacket datagramPacket = new DatagramPacket(buff, buff.length);

        do {
            try {
                datagramSocket.receive(datagramPacket);
                String received = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(buff)).toString();
                String command = received.split(":")[0];
                if (command.equals("challengeRequest")) {
                    WQClientController.gui.updateCommText("Sfida ricevuta...");
                    int n = WQClientController.gui.showChallengeDialog(received.split(":")[1]);
                    if (n == JOptionPane.OK_OPTION) {
                        WQClientController.gui.updateCommText("Sfida accettata!");
                        buff = "challengeResponse:OK".getBytes(StandardCharsets.UTF_8);
                        datagramPacket = new DatagramPacket(buff, buff.length);
                        datagramSocket.send(datagramPacket);
                    } else {
                        WQClientController.gui.updateCommText("Sfida rifiutata.");
                        buff = "challengeResponse:NO".getBytes(StandardCharsets.UTF_8);
                        datagramPacket = new DatagramPacket(buff, buff.length);
                        datagramSocket.send(datagramPacket);
                    }
                }
            } catch (IOException e) {
                try { Thread.sleep(500);}
                catch (InterruptedException ignored) {}
            }
        } while (true);
    }
}
