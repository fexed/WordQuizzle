package com.fexed.lprb.wq;

import javax.swing.*;
import java.awt.*;

/**
 * Semplice classe che include colori, tema e inizializzazione di componenti comuni alle GUI di WordQuizzle
 * @author Federico Matteoni
 */
public abstract class WQGUI {
    //THEME
    /**
     * Colore primario delle finestre e dei componenti
     */
    public Color primary = Color.decode("#0F4C81");

    /**
     * Colore primario chiaro di finestre e componenti
     */
    public Color primaryLight = Color.decode("#1774C6");

    /**
     * Colore primario scuro di finestre e componenti
     */
    public Color primaryDark = Color.decode("#07243C");

    /**
     * Colore di accento per finestre o componenti che richiedono attenzione
     */
    public Color accent = Color.decode("#C61774");

    /**
     * Colore del testo
     */
    public Color txtColor = Color.decode("#F4F5F0");

    /**
     * Colore verde adatto al tema
     */
    public Color green = Color.decode("#6F9A3E");

    /**
     * Colore rosso adatto al tema
     */
    public Color red = Color.decode("#9A3E42");

    /**
     * Font standard da 20pt
     */
    public Font stdFontBig = new Font("Sans-Serif", Font.BOLD, 20);

    /**
     * Font standard da 12pt
     */
    public Font stdFont = new Font("Sans-Serif", Font.PLAIN, 12);

    /**
     * Font per i log e messaggi, da 12pt
     */
    public Font stdFontMsg = new Font("Monospaced", Font.PLAIN, 12);

    /**
     * Font standard da 8pt
     */
    public Font stdFontSmall = new Font("Sans-Serif", Font.BOLD, 8);

    //COMPONENTS
    /**
     * Inizializza un pulsante in linea con il tema
     * @param text Il testo da visualizzare sul pulsante
     * @return Il {@code JButton} inizializzato
     */
    public JButton initThemedButton(String text) {
        JButton btn = new JButton("    " + text + "    ");
        btn.setBackground(primaryDark);
        btn.setForeground(txtColor);
        btn.setFont(stdFont);
        btn.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, primaryLight));
        return btn;
    }

    /**
     * Inizializza un campo di testo in linea con il tema
     * @param columns La lunghezza del campo di testo (vedi {@code JTextField})
     * @return La {@code JTextField} inizializzata
     */
    public JTextField initThemedTextField(int columns) {
        JTextField fld = new JTextField(columns);
        fld.setBackground(txtColor);
        fld.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, primaryDark));
        fld.setFont(stdFont);
        return fld;
    }

    /**
     * Inizializza una etichetta in linea con il tema
     * @param text Il testo da visualizzare
     * @param align L'allineamento del testo (vedi {@code JLabel})
     * @return La {@code JLabel} inizializzata
     */
    public JLabel initThemedLabel(String text, int align) {
        JLabel label = new JLabel(text, align);
        label.setForeground(txtColor);
        label.setFont(stdFont);
        return label;
    }

    /**
     * Inizializza una etichetta in linea con il tema con testo grande (vedi {@code stdFontBig})
     * @param text Il testo da visualizzare
     * @param align L'allineamento del testo (vedi {@code JLabel})
     * @return La {@code JLabel} inizializzata
     */
    public JLabel initThemedLabelBig(String text, int align) {
        JLabel label = new JLabel(text, align);
        label.setForeground(txtColor);
        label.setFont(stdFontBig);
        return label;
    }
}
