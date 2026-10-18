package com.lostandfound;

import com.lostandfound.gui.MainDashboardFrame;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainDashboardFrame frame = new MainDashboardFrame();
            frame.setVisible(true);
        });
    }
}
