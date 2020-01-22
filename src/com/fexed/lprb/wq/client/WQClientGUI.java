package com.fexed.lprb.wq.client;

import javax.swing.*;
import java.awt.*;

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

    public WQClientGUI() {
        WQClientController.gui = this;

        //FRAME INIT
        JFrame w = new JFrame("WordQuizzle!");
        w.setSize(800, 600);
        w.setLocation(150, 150);

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
        JLabel titleLabel = new JLabel("WordQuizzle!", JLabel.CENTER);
        titleLabel.setForeground(txtColor);
        titleLabel.setFont(stdFontBig);
        northPane.add(titleLabel);

        //CENTER PANE
        JPanel loginPane = new JPanel();
        loginPane.setBackground(primary);
        loginPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        loginPane.setLayout(new FlowLayout());
        JButton loginBtn = initThemedButton("Login");
        JTextField loginNameFld = initThemedTextField(15);
        loginPane.add(loginNameFld);
        loginPane.add(loginBtn);

        JPanel commPane = new JPanel();
        commPane.setBackground(primary);
        commPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10 ,10));
        commPane.setLayout(new BoxLayout(commPane, BoxLayout.PAGE_AXIS));
        JTextArea commText = new JTextArea("Server: online\nIo: GIOCA CON ASD (ho cliccato pulsante)\n...\ngioco\nin\ncorso\n...\nServer: fine! brv gg wp");
        commText.setForeground(txtColor);
        commText.setBackground(primaryDark);
        commText.setFont(stdFontMsg);
        commText.setBorder(BorderFactory.createEmptyBorder(5, 7, 5, 5));
        JPanel commMsgPane = new JPanel();
        commMsgPane.setBackground(primary);
        commMsgPane.setLayout(new FlowLayout());
        JTextField inputFld = initThemedTextField(15);
        JButton sendBtn = initThemedButton("Invia");
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
        w.pack();
        w.setVisible(true);
    }

    public static void main(String[] args) {
        if (args.length != 1) System.err.println("Usage: server <porta>");
        else {
            try {
                int port = Integer.parseInt(args[0]);
                if (port < 1024) throw new NumberFormatException();
                WQClientGUI gui = new WQClientGUI();
                WQClient client = new WQClient(port);
            } catch (NumberFormatException ex) { System.err.println("Il parametro inserito non è una porta valida"); }
        }
    }
}
