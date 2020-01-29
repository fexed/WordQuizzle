package com.fexed.lprb.wq.client;

import com.fexed.lprb.wq.WQGUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Collection;

/**
 * Implementazione della GUI del client
 * @author Federico Matteoni
 */
public class WQClientGUI extends WQGUI implements WQClientGUIInterface {
    //Dati del client
    /**
     * L'username dell'utente connesso
     */
    private String username;

    //Componenti della GUI
    /**
     * La finestra principale
     */
    private JFrame w;

    /**
     * Il log di comunicazione
     */
    private JTextArea commText;

    /**
     * Etichetta con le info dell'utente connesso (username e punti)
     */
    private JLabel loginNameLbl;

    /**
     * Pulsante di login
     */
    private JButton loginBtn;

    /**
     * Pulsante d'invio
     */
    private JButton sendBtn;

    /**
     * Pulsante per aggiungere un amico
     */
    private JButton addFriendBtn;

    /**
     * Pulsante per richiedere i punti al server
     */
    private JButton pointsBtn;

    /**
     * Pulsante per richiedere la lista online al server
     */
    private JButton onlineBtn;

    /**
     * Pulsante per richiedere la classifica della lista amici al server
     */
    private JButton rankingBtn;

    /**
     * Lista degli amici
     */
    private JList<String> friendList;

    /**
     * Modello per la lista degli amici
     */
    private DefaultListModel<String> friendListModel;
    public void updateCommText(String txt) { commText.setText(commText.getText() + "\n" + txt); }
    public void clearCommText(String txt) { commText.setText(txt); }
    public void loggedIn(String username, int points) {
        this.username = username;
        loginNameLbl.setText(username + " (" + points + " punti)");
        loginNameLbl.setForeground(green);
        loginBtn.setEnabled(false);
        loginBtn.setVisible(false);
        loginBtn.setText("    Loggato    ");
        enableCommands();
    }
    public void loggedOut() {
        this.username = null;
        loginNameLbl.setText("<non eseguito>");
        loginNameLbl.setForeground(red);
        loginBtn.setEnabled(true);
        loginBtn.setVisible(true);
        loginBtn.setText("    Login    ");
        friendListModel.removeAllElements();
        disableCommands();
    }
    @Override
    public void updatePoints(int point) {
        loginNameLbl.setText(username + " (" + point + " punti)");
    }
    public void addFriend(String friend) {
        friendListModel.addElement(friend);
    }
    public void addAllFriends(Collection<? extends String> friends) {
        friendListModel.removeAllElements();
        friendListModel.addAll(friends);
    }
    public void disableCommands() {
        addFriendBtn.setEnabled(false);
        pointsBtn.setEnabled(false);
        onlineBtn.setEnabled(false);
        rankingBtn.setEnabled(false);
    }
    public void enableCommands() {
        addFriendBtn.setEnabled(true);
        pointsBtn.setEnabled(true);
        onlineBtn.setEnabled(true);
        rankingBtn.setEnabled(true);
    }

    public WQClientGUI() {
        WQClientController.gui = this;

        //Inizializzazione della finestra principale
        w = new JFrame("WordQuizzle!");
        w.setSize(800, 600);
        w.setLocation(550, 150);

        //Inizializzazione del container principale e dei pannelli
        Container p = new JPanel();
        p.setBackground(primary);
        p.setLayout(new BorderLayout(5, 5));
        JPanel northPane = new JPanel(); //Pannello superiore
        northPane.setBackground(primary);
        northPane.setBorder(BorderFactory.createEmptyBorder(0, 15, 5, 15));
        northPane.setLayout(new BoxLayout(northPane, BoxLayout.LINE_AXIS));
        JPanel centerPane = new JPanel(); //Pannello centrale
        centerPane.setBackground(primary);
        centerPane.setLayout(new BoxLayout(centerPane, BoxLayout.LINE_AXIS));
        JPanel southPane = new JPanel(); //Pannello inferiore
        southPane.setBackground(primary);
        southPane.setLayout(new BoxLayout(southPane, BoxLayout.LINE_AXIS));
        southPane.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        //Pannello superiore
        JLabel titleLabel = initThemedLabelBig("WordQuizzle!", JLabel.CENTER); //Etichetta del titolo
        northPane.add(titleLabel);

        //Pannello con le operazioni e informazioni di login
        JPanel loginPane = new JPanel();
        loginPane.setBackground(primary);
        loginPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        loginPane.setLayout(new FlowLayout());
        loginBtn = initThemedButton("Login");
        loginBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showLoginDialog();
            }
        });
        JLabel loginStatusLbl = initThemedLabel("Login: ", JLabel.LEFT);
        loginNameLbl = initThemedLabel("<non eseguito>", JLabel.LEFT);
        loginNameLbl.setForeground(red);
        loginPane.add(loginStatusLbl);
        loginPane.add(loginNameLbl);
        loginPane.add(loginBtn);
        northPane.add(loginPane);

        //Pannello centrale
        friendListModel = new DefaultListModel<>(); //Inizializzazione della lista amicizia
        friendList = new JList<>(friendListModel);
        friendList.setBackground(primaryLight);
        friendList.setPreferredSize(new Dimension(200, 200));
        friendList.setMaximumSize(new Dimension(200, 1024));
        friendList.setMinimumSize(new Dimension(200, 50));
        friendList.setForeground(txtColor);
        friendList.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        JPanel friendsPanel = new JPanel();
        friendsPanel.setLayout(new BoxLayout(friendsPanel, BoxLayout.PAGE_AXIS));
        friendsPanel.setBackground(primary);
        JLabel friendLbl = initThemedLabel("Lista amici", JLabel.LEFT);
        friendsPanel.add(friendLbl);
        friendLbl.add(Box.createRigidArea(new Dimension(0, 2)));
        friendsPanel.add(friendList);
        centerPane.add(friendsPanel);
        centerPane.add(Box.createRigidArea(new Dimension(10, 0)));
        friendList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getClickCount() == 2) { //Doppio click per lanciare una sfida
                    int index = friendList.getSelectedIndex();
                    String nick = friendListModel.get(index);
                    WQClientController.client.send("challenge:" + nick);
                }
            }
        });

        //Pannello delle comunicazioni
        JPanel commPane = new JPanel();
        commPane.setBackground(primary);
        commPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10 ,10));
        commPane.setLayout(new BoxLayout(commPane, BoxLayout.PAGE_AXIS));
        commText = new JTextArea(); //Inizializzazione log di comunicazione
        commText.setForeground(txtColor);
        commText.setBackground(primaryDark);
        commText.setFont(stdFontMsg);
        commText.setEditable(false);
        commText.setLineWrap(true);
        commText.setWrapStyleWord(true);
        commText.setBorder(BorderFactory.createEmptyBorder(5, 7, 5, 5));
        JScrollPane scrollTextArea = new JScrollPane(commText, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollTextArea.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        scrollTextArea.setPreferredSize(new Dimension(0, 300));

        JPanel commMsgPane = new JPanel();
        commMsgPane.setBackground(primary);
        commMsgPane.setLayout(new FlowLayout());

        JPanel commCommandsPane = new JPanel();
        commCommandsPane.setBackground(primary);
        commCommandsPane.setLayout(new GridLayout());
        JTextField inputFld = initThemedTextField(15);
        sendBtn = initThemedButton("Invia"); //Inizializzazione pulsante invia
        sendBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Invio e pulizia della casella di testo
                String txt = inputFld.getText();
                inputFld.setText("");
                int n = WQClientController.client.send(txt);
                if (n == -1) showTextDialog("Errore");
            }
        });
        addFriendBtn = initThemedButton("Aggiungi amico"); //Inizializzazione pulsante aggiungi amicizia
        addFriendBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Creazione della finestra di dialogo modale (blocca input nella finestra principale
                JDialog d = new JDialog(w, "WordQuizzle! Aggiungi un amico", true);
                JPanel panel = new JPanel(); //Container principale
                panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
                panel.setBackground(primaryLight);
                panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                //Etichetta con messaggio, casella di testo e pulsanti
                JLabel infoLabel = initThemedLabel("Inserisci il nickname dell'utente che vuoi aggiungere come amico.", JLabel.CENTER);
                JTextField inputFld = initThemedTextField(10);
                JButton confirmBtn = initThemedButton("Aggiungi");
                JButton cancelBtn = initThemedButton("Annulla");
                panel.add(infoLabel);
                panel.add(inputFld);
                JPanel btnPanel = new JPanel();
                btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.LINE_AXIS));
                btnPanel.add(confirmBtn);
                btnPanel.add(cancelBtn);
                panel.add(btnPanel);

                //Pulsante di conferma, invia comando al server tramite il client
                confirmBtn.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        d.setVisible(false);
                        String nickAmico = inputFld.getText();
                        int n = WQClientController.client.send("addfriend:" + nickAmico);
                        if (n == -1) showTextDialog("Errore");
                    }
                });

                //Pulsante annulla, chiude la finestra
                cancelBtn.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        d.setVisible(false);
                    }
                });
                d.getRootPane().setDefaultButton(confirmBtn);
                d.setContentPane(panel);
                d.pack();
                d.setLocation(w.getX() + w.getWidth()/2 - d.getWidth()/2, w.getY() + w.getHeight()/2 - d.getHeight()/2);
                d.setVisible(true);
            }
        });
        pointsBtn = initThemedButton("Punteggio"); //Inizializzazione pulsante di richieta del punteggio
        pointsBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Invio del comando al server tramite il client
                int n = WQClientController.client.send("showpoints");
                if (n == -1) showTextDialog("Errore");
            }
        });
        rankingBtn = initThemedButton("Classifica"); //Inizializzazione pulsante di richiesta della classifica
        rankingBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Invio comando al server
                int n = WQClientController.client.send("showranking");
                if (n == -1) showTextDialog("Errore");
            }
        });
        onlineBtn = initThemedButton("Utenti online"); //Inizializzazione pulsante di richiesta degli utenti online
        onlineBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int n = WQClientController.client.send("showonlinelist");
                if (n == -1) showTextDialog("Errore");
            }
        });
        JButton clearBtn = initThemedButton("Pulisci"); //Inizializzazione pulsante di pulizia del log
        clearBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                commText.setText("");
            }
        });
        commMsgPane.add(inputFld);
        commMsgPane.add(sendBtn);
        commCommandsPane.add(addFriendBtn);
        commCommandsPane.add(pointsBtn);
        commCommandsPane.add(rankingBtn);
        commCommandsPane.add(onlineBtn);
        commCommandsPane.add(clearBtn);
        commPane.add(scrollTextArea);
        commPane.add(Box.createRigidArea(new Dimension(0, 5)));
        commPane.add(commMsgPane);
        commPane.add(Box.createRigidArea(new Dimension(0, 5)));
        commPane.add(commCommandsPane);
        centerPane.add(commPane);

        //Pannello inferiore con semplice footer
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
                WQClientController.client.logout();
                w.dispose();
                System.exit(0);
            }
        });
        w.getRootPane().setDefaultButton(sendBtn); //Enter sulla tastiera = pulsante invio sulla GUI, per praticità
        w.setVisible(true);
        w.pack();
        w.setMinimumSize(w.getSize());
        disableCommands();
        showLoginDialog();
    }

    public void showTextDialog(String text) {
        //Inizializzazione della finestra di dialogo modale con titolo
        JDialog d = new JDialog(w, "Word Quizzle! Info", true);
        d.getContentPane().setLayout(new BoxLayout(d.getContentPane(), BoxLayout.PAGE_AXIS));
        d.getContentPane().setBackground(primaryLight);
        JLabel textLbl = initThemedLabel(text, JLabel.CENTER);
        textLbl.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JButton okBtn = initThemedButton("Ok"); //Pulsante di conferma che semplicemente chiude la finestra
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
        d.setLocation(w.getX() + w.getWidth()/2 - d.getWidth()/2, w.getY() + w.getHeight()/2 - d.getHeight()/2);
        d.setVisible(true);
    }

    @Override
    public int showChallengeDialog(String nickSfidante) {
        //Mostra la finestra di dialogo per la sfida
        //Ho voluto renderla in tema con il resto del programma, motivo per cui non sono riuscito ad usare il
        //JOptionPane in maniera soddisfacente e ho dovuto trovare una soluzione
        final int[] n = new int[1]; //Per poter modificare la variabile da un metodo interno
        n[0] = JOptionPane.CANCEL_OPTION; //Comportamento di default
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.setBackground(primaryLight);
        JLabel textLbl = initThemedLabelBig(nickSfidante + " ti sta sfidando!", JLabel.CENTER);
        textLbl.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.add(textLbl);
        JPanel buttonsPnl = new JPanel();
        buttonsPnl.setLayout(new BoxLayout(buttonsPnl, BoxLayout.LINE_AXIS));
        buttonsPnl.setBackground(primaryLight);
        JButton okBtn = initThemedButton("Accetta"); //Accetta la sfida
        JDialog d = new JDialog(w, "WordQuizzle! Richiesta di sfida", true);
        okBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                n[0] = JOptionPane.OK_OPTION; //Valore di sfida accettata
                d.setVisible(false);
            }
        });
        JButton noBtn = initThemedButton("Rifiuta"); //Rifiuta la sfida
        noBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                n[0] = JOptionPane.CANCEL_OPTION; //Valore di sfida rifiutata (superfluo)
                d.setVisible(false);
            }
        });
        buttonsPnl.add(Box.createHorizontalGlue());
        buttonsPnl.add(okBtn);
        buttonsPnl.add(Box.createRigidArea(new Dimension(5, 0)));
        buttonsPnl.add(noBtn);
        buttonsPnl.add(Box.createHorizontalGlue());
        panel.add(buttonsPnl);
        d.setContentPane(panel);
        d.pack();
        d.setResizable(false);
        d.setLocation(w.getX() + w.getWidth()/2 - d.getWidth()/2, w.getY() + w.getHeight()/2 - d.getHeight()/2);

        Timer timer = new Timer(10000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                d.setVisible(false);
                d.dispose();
            }
        });
        timer.setRepeats(false);
        timer.start();
        d.setVisible(true);
        //Soluzione brutta, ma non riesco a trovare di meglio per avere la finestra in tema
        while(d.isVisible()) try{Thread.sleep(50);} catch(InterruptedException ignored){}
        timer.stop();
        return n[0];
    }

    private void showLoginDialog() {
        //Mostra la finestra di dialogo per il login
        JDialog d = new JDialog(w, "Word Quizzle! Login", true);
        d.getContentPane().setLayout(new BoxLayout(d.getContentPane(), BoxLayout.PAGE_AXIS));
        d.getContentPane().setBackground(primaryLight);
        JTextField dUserFld = initThemedTextField(15);
        JPasswordField dPwdFld = new JPasswordField(15);
        dPwdFld.setBackground(txtColor);
        dPwdFld.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, primaryDark));
        dPwdFld.setFont(stdFont);
        JButton dLoginBtn = initThemedButton("Login");

        JPanel username = new JPanel();
        username.setBackground(primaryLight);
        username.setBorder(BorderFactory.createEmptyBorder(10, 5, 0, 5));
        username.setLayout(new FlowLayout());
        username.add(initThemedLabel("Nome utente", JLabel.CENTER));
        username.add(dUserFld);
        JPanel password = new JPanel();
        password.setBackground(primaryLight);
        password.setBorder(BorderFactory.createEmptyBorder(10, 5, 0, 5));
        password.setLayout(new FlowLayout());
        password.add(initThemedLabel("Password", JLabel.CENTER));
        password.add(dPwdFld);
        d.getContentPane().add(username);
        d.getContentPane().add(Box.createRigidArea(new Dimension(10, 10)));
        d.getContentPane().add(password);
        dLoginBtn.addActionListener(new ActionListener() { //Avvia la procedura di login
            @Override
            public void actionPerformed(ActionEvent e) {
                if (dUserFld.getText().length() > 0 && dPwdFld.getPassword().length > 0) { //Se i campi sono riempiti
                    String name = dUserFld.getText();
                    String pwd = String.copyValueOf(dPwdFld.getPassword());
                    int n = WQClientController.client.login(name, pwd); //Procedura di login e codice di ritorno
                    d.setVisible(false);
                    if (n == -1) {
                        showTextDialog("Errore nella procedura di login: l'utente non esiste");
                        showLoginDialog();
                    } else if (n == -2) {
                        showTextDialog("Errore nella procedura di login: password sbagliata");
                        showLoginDialog();
                    } else if (n == -3) {
                        showTextDialog("Errore nella procedura di login: l'utente è già collegato");
                        showLoginDialog();
                    } else if (n == 0) {
                    } else {
                        showTextDialog("Errore nella procedura di login");
                        showLoginDialog();
                    }
                } else {
                    showTextDialog("Prego riempire tutti i campi.");
                }
            }
        });
        d.getContentPane().add(dLoginBtn);
        d.setSize(200, 200);
        d.setLocation(w.getX() + w.getWidth()/2 - d.getWidth()/2, w.getY() + w.getHeight()/2 - d.getHeight()/2);
        d.getRootPane().setDefaultButton(dLoginBtn);
        d.setResizable(false);
        d.setVisible(true);
    }

    public static void main(String[] args) {
        if (args.length != 1) System.err.println("Usage: server <porta>");
        else {
            try {
                int port = Integer.parseInt(args[0]);
                if (port < 1024) throw new NumberFormatException();
                WQClient client = new WQClient(port);
                WQClientGUI gui = new WQClientGUI();
            } catch (NumberFormatException ex) { System.err.println("Il parametro inserito non è una porta valida"); }
        }
    }
}
