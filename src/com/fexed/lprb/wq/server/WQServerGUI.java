package com.fexed.lprb.wq.server;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

/**
 * @author Federico Matteoni
 */
public class WQServerGUI {
    private static Color primary = Color.decode("#0F4C81");
    private static Color primaryLight = Color.decode("#1774C6");
    private static Color primaryDark = Color.decode("#07243C");
    private static Color accent = Color.decode("#C61774");
    private static Color txtColor = Color.decode("#F4F5F0");
    private static Font stdFontBig = new Font("Sans-Serif", Font.BOLD, 18);
    private static Font stdFont = new Font("Sans-Serif", Font.PLAIN, 12);

    public static void main(String args[]) {
        JFrame w = new JFrame("WordQuizzle Server");
        w.setSize(800, 600);
        w.setLocation(150, 150);

        Container p = new JPanel();
        p.setBackground(primary);
        p.setLayout(new BorderLayout(5, 5));

        JLabel titleLbl = new JLabel("Statistiche!", JLabel.LEFT);
        titleLbl.setForeground(txtColor);
        titleLbl.setFont(stdFont);
        p.add(titleLbl, BorderLayout.PAGE_START);

        JLabel statsLbl = new JLabel("WordQuizzle!", JLabel.LEFT);
        statsLbl.setForeground(txtColor);
        statsLbl.setFont(stdFont);
        p.add(statsLbl, BorderLayout.PAGE_START);
        JTextPane statTxtPane = new JTextPane();
        statTxtPane.setText("Testo\nincredibilmente\nutile\nma\n\n\n\npoco informativo perché comunque è solo\nun test");
        statTxtPane.setEditable(false);
        statTxtPane.setBackground(primaryDark);
        statTxtPane.setForeground(txtColor);
        statTxtPane.setFont(stdFont);
        p.add(statTxtPane, BorderLayout.CENTER);

        w.setContentPane(p);
        w.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        w.setVisible(true);
        w.pack();
    }
}
