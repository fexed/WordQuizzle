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

    /**
     * Il socket su cui ascoltare i pacchetti UDP
     */
    private DatagramSocket datagramSocket;

    /**
     * Segnala se sta già gestendo una sfida o no
     */
    public static boolean isChallenging = false;

    public WQClientDatagramReceiver(DatagramSocket datagramSocket) {
        this.datagramSocket = datagramSocket;
    }

    @Override
    public void run() {
        byte[] buff;
        DatagramPacket datagramPacket;

        do {
            try {
                //Ricezione del datagramma
                buff = new byte[128];
                datagramPacket = new DatagramPacket(buff, buff.length);
                datagramSocket.receive(datagramPacket);
                String received = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(buff)).toString();
                String command = received.split(":")[0];
                if (command.equals("challengeRequest")) { //Se è richiesta di sfida "challengeRequest:<nickname>"
                    if (isChallenging) { //Se sta già sfidando rifiuta automaticamente
                        buff = "challengeResponse:NO".getBytes(StandardCharsets.UTF_8);
                        datagramPacket = new DatagramPacket(buff, buff.length, datagramPacket.getAddress(), datagramPacket.getPort());
                        datagramSocket.send(datagramPacket);
                    } else { //Altrimenti chiede all'utente
                        WQClientController.gui.updateCommText("Sfida ricevuta...");
                        int n = WQClientController.gui.showChallengeDialog(received.split(":")[1]);
                        buff = new byte[128];
                        //In base alla scelta dell'utente accetta o rifiuta
                        if (n == JOptionPane.OK_OPTION) {
                            WQClientController.gui.updateCommText("Sfida accettata!");
                            buff = "challengeResponse:OK".getBytes(StandardCharsets.UTF_8);
                            datagramPacket = new DatagramPacket(buff, buff.length, datagramPacket.getAddress(), datagramPacket.getPort());
                            datagramSocket.send(datagramPacket);
                        } else {
                            WQClientController.gui.updateCommText("Sfida rifiutata.");
                            buff = "challengeResponse:NO".getBytes(StandardCharsets.UTF_8);
                            datagramPacket = new DatagramPacket(buff, buff.length, datagramPacket.getAddress(), datagramPacket.getPort());
                            datagramSocket.send(datagramPacket);
                        }
                    }
                }
            } catch (IOException e) {
                try { Thread.sleep(500);}
                catch (InterruptedException ignored) {}
            }
        } while (true);
    }
}
