package com.fexed.lprb.wq.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.NoRouteToHostException;
import java.util.concurrent.Flow;

/**
 * @author Federico Matteoni
 */
public class WQClientGUI {
    //THEME
    private Color primary = Color.decode("#0F4C81");
    private Color primaryLight = Color.decode("#1774C6");
    private Color primaryDark = Color.decode("#07243C");
    private Color accent = Color.decode("#C61774");
    private Color txtColor = Color.decode("#F4F5F0");
    private Color green = Color.decode("#6F9A3E");
    private Color red = Color.decode("#9A3E42");
    private Font stdFontBig = new Font("Sans-Serif", Font.BOLD, 20);
    private Font stdFont = new Font("Sans-Serif", Font.PLAIN, 12);
    private Font stdFontMsg = new Font("Monospaced", Font.PLAIN, 12);
    private Font stdFontSmall = new Font("Sans-Serif", Font.BOLD, 8);
    private JButton initThemedButton(String text) {
        JButton btn = new JButton("    " + text + "    ");
        btn.setBackground(primaryDark);
        btn.setForeground(txtColor);
        btn.setFont(stdFont);
        btn.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, primaryLight));
        return btn;
    }
    private JTextField initThemedTextField(int columns) {
        JTextField fld = new JTextField(columns);
        fld.setBackground(txtColor);
        fld.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, primaryDark));
        fld.setFont(stdFont);
        return fld;
    }
    private JLabel initThemedLabel(String text, int align) {
        JLabel label = new JLabel(text, align);
        label.setForeground(txtColor);
        label.setFont(stdFont);
        return label;
    }
    private JLabel initThemedLabelBig(String text, int align) {
        JLabel label = new JLabel(text, align);
        label.setForeground(txtColor);
        label.setFont(stdFontBig);
        return label;
    }

    //COMPONENTS
    private JTextArea commText;
    private JLabel loginNameLbl;
    private JButton loginBtn;
    private JList<String> friendList;
    private DefaultListModel<String> friendListModel;
    public void updateCommText(String txt) { commText.setText(commText.getText() + "\n" + txt); }
    public void loggedIn(String username) {
        loginNameLbl.setText(username);
        loginNameLbl.setForeground(green);
        loginBtn.setEnabled(false);
        loginBtn.setVisible(false);
        loginBtn.setText("    Loggato    ");
    }
    public void addFriend(String friend) {
        friendListModel.addElement(friend);
    }


    public WQClientGUI() {
        WQClientController.gui = this;

        //FRAME INIT
        JFrame w = new JFrame("WordQuizzle!");
        w.setSize(800, 600);
        w.setLocation(550, 150);

        //PANELS AND CONTAINER INIT
        Container p = new JPanel();
        p.setBackground(primary);
        p.setLayout(new BorderLayout(5, 5));
        JPanel northPane = new JPanel();
        northPane.setBackground(primary);
        northPane.setBorder(BorderFactory.createEmptyBorder(0, 15, 5, 15));
        northPane.setLayout(new BoxLayout(northPane, BoxLayout.LINE_AXIS));
        JPanel centerPane = new JPanel();
        centerPane.setBackground(primary);
        centerPane.setLayout(new BoxLayout(centerPane, BoxLayout.LINE_AXIS));
        JPanel southPane = new JPanel();
        southPane.setBackground(primary);
        southPane.setLayout(new BoxLayout(southPane, BoxLayout.LINE_AXIS));
        southPane.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        //NORTH PANE
        JLabel titleLabel = initThemedLabelBig("WordQuizzle!", JLabel.CENTER);
        northPane.add(titleLabel);

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
        loginPane.add(loginStatusLbl);
        loginPane.add(loginNameLbl);
        loginPane.add(loginBtn);
        northPane.add(loginPane);

        //CENTER PANE
        friendListModel = new DefaultListModel<>();
        friendList = new JList<>(friendListModel);
        friendList.setBackground(primaryLight);
        friendList.setPreferredSize(new Dimension(200, 200));
        friendList.setMaximumSize(new Dimension(200, 1024));
        friendList.setMinimumSize(new Dimension(200, 50));
        friendList.setForeground(txtColor);
        //dimensions
        friendList.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        centerPane.add(friendList);
        centerPane.add(Box.createRigidArea(new Dimension(10, 0)));

        JPanel commPane = new JPanel();
        commPane.setBackground(primary);
        commPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10 ,10));
        commPane.setLayout(new BoxLayout(commPane, BoxLayout.PAGE_AXIS));
        commText = new JTextArea("Client online");
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
        commCommandsPane.setLayout(new FlowLayout());
        JTextField inputFld = initThemedTextField(15);
        JButton sendBtn = initThemedButton("Invia");
        sendBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String txt = inputFld.getText();
                inputFld.setText("");
                int n = WQClientController.client.send(txt);
                if (n == -1) showTextDialog("Errore");
            }
        });
        JButton addFriendBtn = initThemedButton("Aggiungi amico");
        addFriendBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int n = WQClientController.client.send("addfriend");
                if (n == -1) showTextDialog("Errore");
            }
        });
        JButton pointsBtn = initThemedButton("Punteggio");
        pointsBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int n = WQClientController.client.send("points");
                if (n == -1) showTextDialog("Errore");
            }
        });
        JButton rankingBtn = initThemedButton("Classifica");
        rankingBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int n = WQClientController.client.send("ranking");
                if (n == -1) showTextDialog("Errore");
            }
        });
        JButton onlineBtn = initThemedButton("Utenti online");
        onlineBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int n = WQClientController.client.send("showonline");
                if (n == -1) showTextDialog("Errore");
            }
        });
        commMsgPane.add(inputFld);
        commMsgPane.add(sendBtn);
        commCommandsPane.add(addFriendBtn);
        commCommandsPane.add(pointsBtn);
        commCommandsPane.add(rankingBtn);
        commCommandsPane.add(onlineBtn);
        commPane.add(scrollTextArea);
        commPane.add(Box.createRigidArea(new Dimension(0, 5)));
        commPane.add(commMsgPane);
        commPane.add(Box.createRigidArea(new Dimension(0, 5)));
        commPane.add(commCommandsPane);
        centerPane.add(commPane);

        //SOUTH PANE
        JLabel footerLabel = new JLabel("Federico Matteoni - 530257", JLabel.RIGHT);
        footerLabel.setForeground(txtColor);
        footerLabel.setFont(stdFontSmall);
        southPane.add(Box.createHorizontalGlue());
        southPane.add(footerLabel);

        p.add(northPane, BorderLayout.PAGE_START);
        p.add(centerPane, BorderLayout.CENTER);
        p.add(southPane, BorderLayout.PAGE_END);
        w.setContentPane(p);
        w.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        w.getRootPane().setDefaultButton(sendBtn);
        w.setVisible(true);
        w.pack();
        w.setMinimumSize(w.getSize());
        showLoginDialog();
    }

    public void showTextDialog(String text) {
        JFrame f = new JFrame();
        JDialog d = new JDialog(f, "Word Quizzle! Info", true);
        d.getContentPane().setLayout(new BoxLayout(d.getContentPane(), BoxLayout.PAGE_AXIS));
        d.getContentPane().setBackground(primaryLight);
        JLabel textLbl = initThemedLabel(text, JLabel.CENTER);
        textLbl.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JButton okBtn = initThemedButton("Ok");
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
        d.setLocation(550 + d.getWidth()/2, 150 + d.getHeight()/2);
        d.setVisible(true);
    }

    private void showLoginDialog() {
        JFrame f = new JFrame();
        JDialog d = new JDialog(f, "Word Quizzle! Login", true);
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
        dLoginBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = dUserFld.getText();
                String pwd = String.copyValueOf(dPwdFld.getPassword());
                int n = WQClientController.client.login(name, pwd);
                d.setVisible(false);
                if (n == -1) {
                    showTextDialog("Errore nella procedura di login.");
                    showLoginDialog();
                }
            }
        });
        d.getContentPane().add(dLoginBtn);
        d.setSize(200, 200);
        d.setLocation(550 + d.getWidth()/2, 150 + d.getHeight()/2);
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
