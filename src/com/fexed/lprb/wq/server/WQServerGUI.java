package com.fexed.lprb.wq.server;

import com.fexed.lprb.wq.client.WQClientController;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.concurrent.Flow;

/**
 * @author Federico Matteoni
 */
public class WQServerGUI {
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

    //COMPONENTS
    private JTextArea statsTxt;
    public void updateStatsText(String txt){ statsTxt.setText(statsTxt.getText() + "\n" + txt); }
    private JLabel titleLabel;
    private JLabel infoLabel;
    private JButton startBtn;
    private JList<String> onlineList;
    private DefaultListModel<String> onlineListModel;
    private JList<String> registeredList;
    private DefaultListModel<String> registeredListModel;
    public void serverIsOnline(int port) {
        titleLabel.setForeground(green);
        startBtn.setEnabled(false);
        startBtn.setText("    Server online    ");
        infoLabel.setText("Online su porta " + port);
    }
    public void serverIsOffline() {
        titleLabel.setForeground(red);
        startBtn.setEnabled(true);
        startBtn.setText("    Avvia server    ");
        infoLabel.setText("Offline");
    }
    public void addOnline(String user) {
        onlineListModel.addElement(user);
    }
    public void removeOnline(String user) {
        onlineListModel.removeElement(user);
    }
    public void addRegistered(String user) {
        registeredListModel.addElement(user);
    }
    public void addAllRegistered(Collection<? extends String> users ) {
        registeredListModel.addAll(users);
    }

    public WQServerGUI() {
        WQServerController.gui = this;

        //FRAME INIT
        JFrame w = new JFrame("WordQuizzle Server");
        w.setSize(500, 600);
        w.setLocation(150, 150);

        //PANELS AND CONTAINER INIT
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

        //NORTH PANE
        titleLabel = new JLabel("WordQuizzle!", JLabel.CENTER);
        titleLabel.setForeground(txtColor);
        titleLabel.setFont(stdFontBig);
        infoLabel = new JLabel("Offline", JLabel.CENTER);
        infoLabel.setForeground(txtColor);
        infoLabel.setFont(stdFont);
        northPane.add(titleLabel);
        northPane.add(Box.createHorizontalGlue());
        northPane.add(infoLabel);

        //CENTER PANE
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
        JButton dumpBtn = initThemedButton("Dump infos");
        dumpBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateStatsText(WQServerController.server.getInfos());
            }
        });
        JButton otherBtn = initThemedButton("Altro...");
        otherBtn.setEnabled(false);
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
        w.setMinimumSize(w.getSize());
        w.setSize(500, 600);
        w.setVisible(true);
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
