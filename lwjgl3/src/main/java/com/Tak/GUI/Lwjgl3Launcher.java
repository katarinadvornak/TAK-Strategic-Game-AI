package com.Tak.GUI;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

/** Launches the desktop (LWJGL3) application. */
public class Lwjgl3Launcher {
    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired()) return; // This handles macOS support and helps on Windows.
        createApplication();
    }

    private static Lwjgl3Application createApplication() {
        return new Lwjgl3Application(new TakGameMain(), getDefaultConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("Tak");

        // **Set HDPI Mode for Retina Displays on macOS**
        configuration.setHdpiMode(Lwjgl3ApplicationConfiguration.HdpiMode.Pixels);

        // **Fetch the Primary Monitor's Display Mode**
        com.badlogic.gdx.Graphics.DisplayMode currentDisplayMode = Lwjgl3ApplicationConfiguration.getDisplayMode();
        int screenWidth = currentDisplayMode.width;
        int screenHeight = currentDisplayMode.height;

        // **Set Windowed Mode to Screen Resolution**
        configuration.setWindowedMode(screenWidth, screenHeight);

        // **Maximize the Window Without Entering Exclusive Fullscreen**
        configuration.setMaximized(true);

        // **Optional: Prevent Window Resizing**
        configuration.setResizable(false); // Set to true if you want the window to be resizable

        // **Optional: Ensure Window Decorations (Title Bar, Borders) are Visible**
        configuration.setDecorated(true); // Set to false to remove window decorations

        // **Enable VSync to Limit FPS and Prevent Screen Tearing**
        configuration.useVsync(true);

        // **Set Foreground FPS to Match Refresh Rate (+1 as a Safeguard)**
        int refreshRate = currentDisplayMode.refreshRate > 0 ? currentDisplayMode.refreshRate : 60;
        configuration.setForegroundFPS(refreshRate + 1);

        // **Set Window Icons (Optional)**
        configuration.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png");

        return configuration;
    }
}
