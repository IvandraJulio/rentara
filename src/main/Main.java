package main;

import com.formdev.flatlaf.FlatDarkLaf;
import dao.DatabaseConnection;
import view.MainFrame;

import javax.swing.*;
import java.awt.*;

/**
 * Entry Point Utama Aplikasi Desktop GUI Rentara Management System
 * Menginisialisasi FlatDarkLaf tema modern dan meluncurkan MainFrame GUI.
 */
public class Main {
    public static void main(String[] args) {
        // Inisialisasi basis data SQLite
        DatabaseConnection.initDatabase();

        // Konfigurasi Look & Feel FlatLaf Dark Modern
        try {
            FlatDarkLaf.setup();
            UIManager.put("Button.arc", 12);
            UIManager.put("Component.arc", 10);
            UIManager.put("CheckBox.arc", 6);
            UIManager.put("ProgressBar.arc", 10);
            UIManager.put("ScrollBar.thumbArc", 8);
            UIManager.put("ScrollBar.width", 10);
            UIManager.put("TitlePane.unifiedBackground", true);
            UIManager.put("RootPane.background", new Color(0x18, 0x19, 0x1D));
        } catch (Exception e) {
            System.err.println("Gagal menerapkan FlatLaf, menggunakan tema sistem default: " + e.getMessage());
        }

        // Jalankan GUI pada Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
