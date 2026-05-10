package com.simulador.util;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.ColorUIResource;

import java.awt.*;

/**
 * Tema visual oscuro para el simulador.
 * Define colores, fuentes y utilidades de estilización.
 */
public class Theme {

    // --- Colores principales ---
    public static final Color BG_DARK = new Color(15, 15, 22);
    public static final Color BG_PANEL = new Color(24, 24, 36);
    public static final Color BG_PANEL_LIGHT = new Color(32, 32, 48);
    public static final Color BG_INPUT = new Color(40, 40, 56);
    public static final Color BG_HOVER = new Color(50, 50, 70);

    // --- Acentos ---
    public static final Color ACCENT = new Color(99, 102, 241);       // Indigo
    public static final Color ACCENT_LIGHT = new Color(129, 140, 248);
    public static final Color SUCCESS = new Color(34, 197, 94);        // Verde
    public static final Color WARNING = new Color(245, 158, 11);       // Ámbar
    public static final Color DANGER = new Color(239, 68, 68);         // Rojo
    public static final Color INFO = new Color(56, 189, 248);          // Cyan

    // --- Texto ---
    public static final Color TEXT_PRIMARY = new Color(229, 231, 235);
    public static final Color TEXT_SECONDARY = new Color(156, 163, 175);
    public static final Color TEXT_MUTED = new Color(107, 114, 128);

    // --- Bordes ---
    public static final Color BORDER_COLOR = new Color(55, 55, 75);
    public static final Color BORDER_LIGHT = new Color(75, 75, 95);

    // --- Fuentes ---
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO = new Font("Consolas", Font.PLAIN, 12);
    public static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 12);

    /**
     * Aplica el tema oscuro global a todos los componentes Swing.
     */
    public static void applyGlobalTheme() {
        UIManager.put("Panel.background", new ColorUIResource(BG_DARK));
        UIManager.put("Label.foreground", new ColorUIResource(TEXT_PRIMARY));
        UIManager.put("Label.font", FONT_BODY);

        UIManager.put("Table.background", new ColorUIResource(BG_PANEL));
        UIManager.put("Table.foreground", new ColorUIResource(TEXT_PRIMARY));
        UIManager.put("Table.gridColor", new ColorUIResource(BORDER_COLOR));
        UIManager.put("Table.selectionBackground", new ColorUIResource(ACCENT));
        UIManager.put("Table.selectionForeground", new ColorUIResource(Color.WHITE));
        UIManager.put("Table.font", FONT_BODY);
        UIManager.put("TableHeader.background", new ColorUIResource(BG_PANEL_LIGHT));
        UIManager.put("TableHeader.foreground", new ColorUIResource(TEXT_PRIMARY));
        UIManager.put("TableHeader.font", FONT_BUTTON);

        UIManager.put("ScrollPane.background", new ColorUIResource(BG_PANEL));
        UIManager.put("ScrollBar.thumb", new ColorUIResource(BG_HOVER));
        UIManager.put("ScrollBar.track", new ColorUIResource(BG_PANEL));

        UIManager.put("ComboBox.background", new ColorUIResource(BG_INPUT));
        UIManager.put("ComboBox.foreground", new ColorUIResource(TEXT_PRIMARY));
        UIManager.put("ComboBox.selectionBackground", new ColorUIResource(ACCENT));
        UIManager.put("ComboBox.font", FONT_BODY);

        UIManager.put("Spinner.background", new ColorUIResource(BG_INPUT));
        UIManager.put("Spinner.foreground", new ColorUIResource(TEXT_PRIMARY));

        UIManager.put("TextField.background", new ColorUIResource(BG_INPUT));
        UIManager.put("TextField.foreground", new ColorUIResource(TEXT_PRIMARY));
        UIManager.put("TextField.caretForeground", new ColorUIResource(TEXT_PRIMARY));
        UIManager.put("TextField.font", FONT_BODY);

        UIManager.put("TextArea.background", new ColorUIResource(BG_PANEL));
        UIManager.put("TextArea.foreground", new ColorUIResource(TEXT_PRIMARY));
        UIManager.put("TextArea.font", FONT_MONO);

        UIManager.put("OptionPane.background", new ColorUIResource(BG_PANEL));
        UIManager.put("OptionPane.messageForeground", new ColorUIResource(TEXT_PRIMARY));
        UIManager.put("OptionPane.font", FONT_BODY);

        UIManager.put("ToolTip.background", new ColorUIResource(BG_PANEL_LIGHT));
        UIManager.put("ToolTip.foreground", new ColorUIResource(TEXT_PRIMARY));
        UIManager.put("ToolTip.font", FONT_SMALL);

        // Botones: forzar colores para que Nimbus no los sobrescriba
        UIManager.put("Button.background", new ColorUIResource(BG_INPUT));
        UIManager.put("Button.foreground", new ColorUIResource(TEXT_PRIMARY));
        UIManager.put("Button.font", FONT_BUTTON);

        // TableHeader: forzar opacidad en Nimbus
        UIManager.put("TableHeader.opaque", true);
    }

    /**
     * Crea un botón estilizado con el color de acento.
     */
    public static JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(FONT_BUTTON);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(button.getPreferredSize().width + 20, 34));

        // Forzar colores sobre Nimbus para que respete el bgColor
        applyNimbusOverrides(button, bgColor, Color.WHITE);

        Color hoverColor = bgColor.brighter();
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(hoverColor);
                applyNimbusOverrides(button, hoverColor, Color.WHITE);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(bgColor);
                applyNimbusOverrides(button, bgColor, Color.WHITE);
            }
        });

        return button;
    }

    /**
     * Aplica overrides de Nimbus a un componente para forzar colores de fondo y texto.
     * Esto es necesario porque Nimbus ignora setBackground/setForeground en muchos componentes.
     */
    public static void applyNimbusOverrides(JComponent comp, Color bg, Color fg) {
        UIDefaults overrides = new UIDefaults();
        overrides.put("Button.background", bg);
        overrides.put("Button[Enabled].backgroundPainter", new NimbusBgPainter(bg));
        overrides.put("Button[MouseOver].backgroundPainter", new NimbusBgPainter(bg.brighter()));
        overrides.put("Button[Pressed].backgroundPainter", new NimbusBgPainter(bg.darker()));
        overrides.put("Button[Focused].backgroundPainter", new NimbusBgPainter(bg));
        overrides.put("Button[Focused+MouseOver].backgroundPainter", new NimbusBgPainter(bg.brighter()));
        overrides.put("Button[Focused+Pressed].backgroundPainter", new NimbusBgPainter(bg.darker()));
        comp.putClientProperty("Nimbus.Overrides", overrides);
        comp.putClientProperty("Nimbus.Overrides.InheritDefaults", false);
    }

    /**
     * Painter simple que rellena el fondo de un componente con un color sólido.
     * Usado para sobrescribir los painters de Nimbus.
     */
    private static class NimbusBgPainter implements javax.swing.Painter<JComponent> {
        private final Color color;
        NimbusBgPainter(Color color) { this.color = color; }
        @Override
        public void paint(Graphics2D g, JComponent object, int width, int height) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(color);
            g.fillRoundRect(0, 0, width, height, 8, 8);
        }
    }

    /**
     * Crea un panel con título y borde estilizado.
     */
    public static JPanel createTitledPanel(String title) {
        JPanel panel = new JPanel();
        panel.setBackground(BG_PANEL);
        panel.setBorder(createPanelBorder(title));
        return panel;
    }

    /**
     * Crea un borde con título para paneles.
     */
    public static Border createPanelBorder(String title) {
        LineBorder line = new LineBorder(BORDER_COLOR, 1);
        EmptyBorder padding = new EmptyBorder(8, 10, 8, 10);

        javax.swing.border.TitledBorder titled = BorderFactory.createTitledBorder(
                line, " " + title + " ");
        titled.setTitleColor(ACCENT_LIGHT);
        titled.setTitleFont(FONT_SUBTITLE);

        return new CompoundBorder(titled, padding);
    }

    /**
     * Estiliza un JScrollPane para el tema oscuro.
     */
    public static JScrollPane styledScrollPane(Component view) {
        JScrollPane sp = new JScrollPane(view);
        sp.setBackground(BG_PANEL);
        sp.getViewport().setBackground(BG_PANEL);
        sp.setBorder(new LineBorder(BORDER_COLOR, 1));
        return sp;
    }
}
