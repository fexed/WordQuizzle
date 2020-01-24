package com.fexed.lprb.wq.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
    public void updateCommText(String txt) { commText.setText(commText.getText() + "\n" + txt); }
    private JLabel loginNameLbl;
    private JButton loginBtn;
    public void loggedIn(String username) { loginNameLbl.setText("Username: " + username); loginBtn.setEnabled(false); }

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
        JPanel centerPane = new JPanel();
        centerPane.setBackground(primary);
        centerPane.setLayout(new BoxLayout(centerPane, BoxLayout.PAGE_AXIS));
        JPanel southPane = new JPanel();
        southPane.setBackground(primary);
        southPane.setLayout(new BoxLayout(southPane, BoxLayout.LINE_AXIS));
        southPane.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        //NORTH PANE
        JLabel titleLabel = initThemedLabelBig("WordQuizzle!", JLabel.CENTER);
        northPane.add(titleLabel);

        //CENTER PANE
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
        loginNameLbl = initThemedLabel("<login non eseguito>", JLabel.LEFT);
        loginPane.add(loginNameLbl);
        loginPane.add(loginBtn);

        JPanel commPane = new JPanel();
        commPane.setBackground(primary);
        commPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10 ,10));
        commPane.setLayout(new BoxLayout(commPane, BoxLayout.PAGE_AXIS));
        commText = new JTextArea("Client online");
        commText.setForeground(txtColor);
        commText.setBackground(primaryDark);
        commText.setFont(stdFontMsg);
        commText.setBorder(BorderFactory.createEmptyBorder(5, 7, 5, 5));
        JPanel commMsgPane = new JPanel();
        commMsgPane.setBackground(primary);
        commMsgPane.setLayout(new FlowLayout());
        JTextField inputFld = initThemedTextField(15);
        JButton sendBtn = initThemedButton("Invia");
        sendBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                WQClientController.client.send(inputFld.getText());
            }
        });
        commMsgPane.add(inputFld);
        commMsgPane.add(sendBtn);
        commPane.add(commText);
        commPane.add(Box.createRigidArea(new Dimension(0, 5)));
        commPane.add(commMsgPane);
        centerPane.add(loginPane);
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
        w.setVisible(true);
        showLoginDialog();
    }

    private void showTextDialog(String text) {
        JFrame f = new JFrame();
        JDialog d = new JDialog(f, "Word Quizzle! Info", true);
        d.getContentPane().setLayout(new BoxLayout(d.getContentPane(), BoxLayout.PAGE_AXIS));
        d.getContentPane().setBackground(primaryLight);
        JLabel textLbl = initThemedLabel(text, JLabel.CENTER);
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
                    showTextDialog("Errore nella procedura di login: password vuota.");
                    showLoginDialog();
                }
            }
        });
        d.getContentPane().add(dLoginBtn);
        d.setSize(200, 200);
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
