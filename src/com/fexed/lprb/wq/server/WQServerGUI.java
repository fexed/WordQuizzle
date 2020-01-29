package com.fexed.lprb.wq.server;

import com.fexed.lprb.wq.WQGUI;
import com.fexed.lprb.wq.WQUtente;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Collection;

/**
 * Implementazione della GUI del server
 * @author Federico Matteoni
 */
public class WQServerGUI extends WQGUI implements WQServerGUIInterface {
    //Dati del server
    /**
     * Numero di thread in esecuzione
     */
    private int nThreads = 0;

    /**
     * Indica alla GUI se chiudere il processo o meno
     */
    private boolean shouldClose = false;

    //Componenti della GUI
    /**
     * La finestra principale
     */
    private JFrame w;

    /**
     * Il log di comunicazione
     */
    private JTextArea statsTxt;

    /**
     * Etichetta con il nome del server
     */
    private JLabel titleLabel;

    /**
     * Etichetta con il numero di threads
     */
    private JLabel threadsLabel;

    /**
     * Etichetta con il numero di porta
     */
    private JLabel infoLabel;

    /**
     * Pulsante di avvio del server
     */
    private JButton startBtn;

    /**
     * Lista degli utenti connessi
     */
    private JList<String> onlineList;

    /**
     * Modello della lista degli utenti connessi
     */
    private DefaultListModel<String> onlineListModel;

    /**
     * Lista degli utenti registrati
     */
    private JList<WQUtente> registeredList;

    /**
     * Modello della lista degli utenti registrati
     */
    private DefaultListModel<WQUtente> registeredListModel;
    public void updateStatsText(String txt){ statsTxt.setText(statsTxt.getText() + "\n" + txt); }
    public void clearStatsText(String txt){ statsTxt.setText(txt); }
    public void serverIsOnline(int port) {
        titleLabel.setForeground(green);
        startBtn.setEnabled(false);
        startBtn.setText("    Server online    ");
        infoLabel.setText("Porta " + port);
        threadsLabel.setText(nThreads + " thread" + (nThreads == 1 ? "" : "s" ));
    }
    public void serverIsOffline() {
        titleLabel.setForeground(red);
        startBtn.setEnabled(true);
        startBtn.setText("    Avvia server    ");
        infoLabel.setText("Offline");
        if (shouldClose) { //In caso, chiude il processo
            w.dispose();
            System.exit(0);
        }
    }
    public void addOnline(String user) {
        onlineListModel.addElement(user);
    }
    public void removeOnline(String user) {
        onlineListModel.removeElement(user);
    }
    public void addRegistered(WQUtente user) {
        registeredListModel.addElement(user);
    }
    public void addAllRegistered(Collection<? extends WQUtente> users ) {
        registeredListModel.addAll(users);
    }
    public void addThreadsText() {
        nThreads++;
        threadsLabel.setText(nThreads + " thread" + (nThreads == 1 ? "" : "s" ));
    }
    public void subThreadsText() {
        nThreads--;
        threadsLabel.setText(nThreads + " thread" + (nThreads == 1 ? "" : "s" ));
    }

    public WQServerGUI() {
        WQServerController.gui = this;

        //Inizializzazione della finestra principale
        w = new JFrame("WordQuizzle Server");
        w.setSize(500, 600);
        w.setLocation(150, 150);

        //Inizializzazione del container principale e dei pannelli
        Container p = new JPanel();
        p.setBackground(primary);
        p.setLayout(new BorderLayout(5, 5));
        JPanel northPane = new JPanel();
        northPane.setBackground(primary);
        northPane.setLayout(new BoxLayout(northPane, BoxLayout.LINE_AXIS));
        northPane.setBorder(BorderFactory.createEmptyBorder(0, 15, 5, 15));
        JPanel centerPane = new JPanel();
        centerPane.setBackground(primary);
        centerPane.setLayout(new BoxLayout(centerPane, BoxLayout.LINE_AXIS));
        JPanel southPane = new JPanel();
        southPane.setBackground(primary);
        southPane.setLayout(new BoxLayout(southPane, BoxLayout.LINE_AXIS));
        southPane.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        //Pannello superiore
        titleLabel = new JLabel("WordQuizzle!", JLabel.CENTER);
        titleLabel.setForeground(txtColor);
        titleLabel.setFont(stdFontBig);
        infoLabel = new JLabel("Offline", JLabel.CENTER);
        infoLabel.setForeground(txtColor);
        infoLabel.setFont(stdFont);
        threadsLabel = initThemedLabel("- threads", JLabel.RIGHT);
        northPane.add(titleLabel);
        northPane.add(Box.createHorizontalGlue());
        northPane.add(infoLabel);
        northPane.add(Box.createRigidArea(new Dimension(15, 0)));
        northPane.add(threadsLabel);

        //Pannello centrale
        statsTxt = new JTextArea("");
        statsTxt.setForeground(txtColor);
        statsTxt.setBackground(primaryDark);
        statsTxt.setFont(stdFontMsg);
        statsTxt.setEditable(false);
        statsTxt.setLineWrap(true);
        statsTxt.setWrapStyleWord(true);
        statsTxt.setBorder(BorderFactory.createEmptyBorder(5, 7, 5, 5));
        JScrollPane scrollTextArea = new JScrollPane(statsTxt, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollTextArea.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        JPanel controlPane = new JPanel();
        controlPane.setLayout(new BoxLayout(controlPane, BoxLayout.PAGE_AXIS));
        controlPane.setBackground(primary);
        controlPane.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        startBtn = initThemedButton("Avvia Server");
        startBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new WQServer(1337);
            }
        });
        JButton dumpBtn = initThemedButton("Informazioni");
        dumpBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateStatsText(WQServerController.server.getInfos());
            }
        });
        JButton otherBtn = initThemedButton("Ripulisci il log");
        otherBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearStatsText("");
            }
        });
        JLabel onlineListLbl =  new JLabel("Utenti online", JLabel.LEFT);
        onlineListLbl.setForeground(txtColor);
        onlineListLbl.setFont(stdFont);
        onlineListModel = new DefaultListModel<>();
        onlineList = new JList<>(onlineListModel);
        onlineList.setBackground(primaryLight);
        onlineList.setForeground(txtColor);
        onlineList.setPreferredSize(new Dimension(200, 150));
        onlineList.setMaximumSize(new Dimension(200, 150));
        onlineList.setMinimumSize(new Dimension(200, 50));
        onlineList.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        controlPane.add(onlineListLbl);
        controlPane.add(Box.createRigidArea(new Dimension(0, 5)));
        controlPane.add(onlineList);
        controlPane.add(Box.createRigidArea(new Dimension(0, 10)));
        JLabel registeredListLbl =  new JLabel("Utenti registrati", JLabel.LEFT);
        registeredListLbl.setForeground(txtColor);
        registeredListLbl.setFont(stdFont);
        registeredListModel = new DefaultListModel<>();
        registeredList = new JList<>(registeredListModel);
        registeredList.setBackground(primaryLight);
        registeredList.setForeground(txtColor);
        registeredList.setPreferredSize(new Dimension(200, 150));
        registeredList.setMaximumSize(new Dimension(200, 150));
        registeredList.setMinimumSize(new Dimension(200, 50));
        registeredList.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        registeredList.addMouseListener(new MouseAdapter() { //Doppio click per avere informazioni sull'utente
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getClickCount() == 2) {
                    int index = registeredList.getSelectedIndex();
                    String descr = registeredListModel.get(index).description();
                    showTextDialog(descr);
                }
            }
        });
        controlPane.add(registeredListLbl);
        controlPane.add(Box.createRigidArea(new Dimension(0, 5)));
        controlPane.add(registeredList);
        controlPane.add(Box.createRigidArea(new Dimension(0, 20)));
        controlPane.add(Box.createVerticalGlue());
        controlPane.add(startBtn);
        controlPane.add(Box.createRigidArea(new Dimension(0, 10)));
        controlPane.add(dumpBtn);
        controlPane.add(Box.createRigidArea(new Dimension(0, 10)));
        controlPane.add(otherBtn);
        centerPane.add(scrollTextArea);
        centerPane.add(Box.createRigidArea(new Dimension(5, 0)));
        centerPane.add(controlPane);

        //Pannello inferiore
        JLabel footerLabel = new JLabel("Federico Matteoni - 530257", JLabel.RIGHT);
        footerLabel.setForeground(txtColor);
        footerLabel.setFont(stdFontSmall);
        southPane.add(Box.createHorizontalGlue());
        southPane.add(footerLabel);

        //Finalizzazione della finestra principale
        p.add(northPane, BorderLayout.PAGE_START);
        p.add(centerPane, BorderLayout.CENTER);
        p.add(southPane, BorderLayout.PAGE_END);
        w.setContentPane(p);
        w.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        w.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                WQServerController.server.stopServer();
                shouldClose = true;
            }
        });
        w.pack();
        w.setMinimumSize(w.getSize());
        w.setSize(500, 600);
        w.setVisible(true);
    }

    public void showTextDialog(String text) {
        //Inizializzazione della finestra di dialogo con titolo
        //Usata per mostrare le info degli utenti registrati, quindi non modale per comodità d'uso
        JDialog d = new JDialog(w, "Word Quizzle server info", false);
        d.getContentPane().setLayout(new BoxLayout(d.getContentPane(), BoxLayout.PAGE_AXIS));
        d.getContentPane().setBackground(primaryLight);
        JLabel textLbl = initThemedLabel("<html>" + text.replaceAll("\n", "<br />") + "</html>", JLabel.CENTER);
        textLbl.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JButton okBtn = initThemedButton("Ok"); //Chiude la finestra
        okBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                d.setVisible(false);
            }
        });
        d.getContentPane().add(textLbl);
        d.getContentPane().add(Box.createHorizontalGlue());
        d.getContentPane().add(okBtn);
        d.getContentPane().add(Box.createHorizontalGlue());
        d.pack();
        d.setResizable(false);
        d.setLocation(w.getX() + d.getWidth()/2, w.getY() + d.getHeight()/2);
        d.setVisible(true);
    }

    public static void main(String args[]) {
        if (args.length != 1) System.err.println("Usage: server <porta>");
        else {
            try {
                int port = Integer.parseInt(args[0]);
                if (port < 1024) throw new NumberFormatException();
                WQServerGUI gui = new WQServerGUI();
                WQServer server = new WQServer(port);
            } catch (NumberFormatException ex) { System.err.println("Il parametro inserito non è una porta valida"); }
        }
    }
}
