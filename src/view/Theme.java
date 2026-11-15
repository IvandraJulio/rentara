package view;

import java.awt.*;

/**
 * Tema dan Palet Desain Rentara Management System
 * Disesuaikan secara presisi dengan spesifikasi UI Figma (Dark Theme Charcoal & Warm Orange).
 */
public class Theme {
    // Palet Warna Latar
    public static final Color BG_DARK = new Color(0x18, 0x19, 0x1D);
    public static final Color BG_SIDEBAR = new Color(0x11, 0x13, 0x15);
    public static final Color BG_CARD = new Color(0x1F, 0x20, 0x26);
    public static final Color BG_CARD_LIGHT = new Color(0x26, 0x27, 0x2C);
    public static final Color BG_INPUT = new Color(0x18, 0x1C, 0x1E);
    public static final Color BORDER = new Color(0x30, 0x30, 0x3D);
    public static final Color BORDER_LIGHT = new Color(0x3F, 0x40, 0x4F);

    // Palet Warna Aksen
    public static final Color ACCENT_ORANGE = new Color(0xFF, 0xAA, 0x72);
    public static final Color ACCENT_ORANGE_DARK = new Color(0xE1, 0x85, 0x57);
    public static final Color ACCENT_LIME = new Color(0xDB, 0xE8, 0x9A);

    // Tipografi / Teks
    public static final Color TEXT_WHITE = new Color(0xF7, 0xF7, 0xFA);
    public static final Color TEXT_MUTED = new Color(0xBF, 0xC0, 0xCA);
    public static final Color TEXT_DIM = new Color(0x94, 0x95, 0xA4);

    // Status Badges
    public static final Color BADGE_GREEN_BG = new Color(0x16, 0x2C, 0x22);
    public static final Color BADGE_GREEN_TXT = new Color(0xAC, 0xE0, 0xC3);

    public static final Color BADGE_AMBER_BG = new Color(0x35, 0x27, 0x15);
    public static final Color BADGE_AMBER_TXT = new Color(0xFF, 0xD1, 0x8A);

    public static final Color BADGE_RED_BG = new Color(0x38, 0x1A, 0x20);
    public static final Color BADGE_RED_TXT = new Color(0xFF, 0xA5, 0xAB);

    // Font Helpers
    public static final String FONT_NAME = "Segoe UI";

    public static Font font(int style, float size) {
        return new Font(FONT_NAME, style, (int) size);
    }

    public static Font titleLarge() {
        return new Font(FONT_NAME, Font.BOLD, 26);
    }

    public static Font titleMedium() {
        return new Font(FONT_NAME, Font.BOLD, 20);
    }

    public static Font titleSmall() {
        return new Font(FONT_NAME, Font.BOLD, 16);
    }

    public static Font bodyBold() {
        return new Font(FONT_NAME, Font.BOLD, 13);
    }

    public static Font bodyRegular() {
        return new Font(FONT_NAME, Font.PLAIN, 13);
    }

    public static Font caption() {
        return new Font(FONT_NAME, Font.PLAIN, 11);
    }

    public static Font captionBold() {
        return new Font(FONT_NAME, Font.BOLD, 11);
    }

    public static Font micro() {
        return new Font(FONT_NAME, Font.BOLD, 10);
    }
}
