package com.fexed.lprb.wq.server;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.util.concurrent.Flow;

/**
 * @author Federico Matteoni
 */
public class WQServerGUI {
    private static Color primary = Color.decode("#0F4C81");
    private static Color primaryLight = Color.decode("#1774C6");
    private static Color primaryDark = Color.decode("#07243C");
    private static Color accent = Color.decode("#C61774");
    private static Color txtColor = Color.decode("#F4F5F0");
    private static Font stdFontBig = new Font("Sans-Serif", Font.BOLD, 20);
    private static Font stdFont = new Font("Sans-Serif", Font.PLAIN, 12);
    private static Font stdFontMsg = new Font("Monospaced", Font.PLAIN, 12);
    private static Font stdFontSmall = new Font("Sans-Serif", Font.BOLD, 8);

    public static void main(String args[]) {
        //FRAME INIT
        JFrame w = new JFrame("WordQuizzle Server");
        w.setSize(800, 600);
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
        JLabel titleLabel = new JLabel("WordQuizzle!", JLabel.CENTER);
        titleLabel.setForeground(txtColor);
        titleLabel.setFont(stdFontBig);
        JLabel titleBLabel = new JLabel("Server", JLabel.CENTER);
        titleBLabel.setForeground(txtColor);
        titleBLabel.setFont(stdFont);
        northPane.add(Box.createHorizontalGlue());
        northPane.add(titleLabel);
        northPane.add(Box.createRigidArea(new Dimension(10, 0)));
        northPane.add(titleBLabel);
        northPane.add(Box.createHorizontalGlue());

        //CENTER PANE
        JTextArea statsTxt = new JTextArea("Stringa di\ninformazioni attualmente\ncompletamente inutili ma\nche\nin\nfuturo\n\n\nsaranno abbastanza utili\nad esempio per:\ndebug\nstatistiche\naltro");
        statsTxt.setForeground(txtColor);
        statsTxt.setBackground(primaryDark);
        statsTxt.setFont(stdFontMsg);
        statsTxt.setEditable(false);
        statsTxt.setBorder(BorderFactory.createEmptyBorder(5, 7, 5, 5));
        JPanel controlPane = new JPanel();
        controlPane.setLayout(new BoxLayout(controlPane, BoxLayout.PAGE_AXIS));
        controlPane.setBackground(primary);
        controlPane.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JButton startBtn = new JButton("    Avvia server    ");
        startBtn.setBackground(primaryDark);
        startBtn.setForeground(txtColor);
        startBtn.setFont(stdFont);
        startBtn.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, primaryLight));
        JButton dumpBtn = new JButton("    Dump infos    ");
        dumpBtn.setBackground(primaryDark);
        dumpBtn.setForeground(txtColor);
        dumpBtn.setFont(stdFont);
        dumpBtn.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, primaryLight));
        JButton otherBtn = new JButton("    Altro...    ");
        otherBtn.setBackground(primaryDark);
        otherBtn.setForeground(txtColor);
        otherBtn.setFont(stdFont);
        otherBtn.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, primaryLight));
        otherBtn.setEnabled(false);
        controlPane.add(startBtn);
        controlPane.add(Box.createRigidArea(new Dimension(0, 10)));
        controlPane.add(dumpBtn);
        controlPane.add(Box.createRigidArea(new Dimension(0, 10)));
        controlPane.add(otherBtn);
        controlPane.add(Box.createVerticalGlue());
        centerPane.add(statsTxt);
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
        w.setVisible(true);
    }
}
