package shogun.task;

import shogun.sdk.SDK;

import java.awt.*;

public class TaskTray {

    public void show() {
        System.setProperty("apple.awt.UIElement", "true");
        Image duke64x64 = Toolkit.getDefaultToolkit().createImage(ClassLoader.getSystemResource("duke-64x64.png"));
        Image duke32x32 = Toolkit.getDefaultToolkit().createImage(ClassLoader.getSystemResource("duke-64x64-white.png"));
        SDK sdk = new SDK();
        String version = sdk.getVersion();
        try {
            SystemTray tray = SystemTray.getSystemTray();
            PopupMenu popup = new PopupMenu();
            TrayIcon icon = new TrayIcon(duke64x64, "Shogun", popup);
            icon.setImageAutoSize(true);

            MenuItem versionLabel = new MenuItem(version);
            versionLabel.setEnabled(false);
            popup.add(versionLabel);
            MenuItem item2 = new MenuItem("Exit");
            item2.addActionListener(e -> {
                tray.remove(icon);
                System.exit(0);
            });
            popup.add(item2);

            tray.add(icon);

            Thread thread = new Thread(() -> {
                while (true) {
                    icon.setImage(duke32x32);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    icon.setImage(duke64x64);

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            });
            thread.setDaemon(true);
            thread.start();

        } catch (AWTException e) {
            e.printStackTrace();
        }
    }
}