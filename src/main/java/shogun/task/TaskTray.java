package shogun.task;

import shogun.sdk.SDK;
import shogun.sdk.Version;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TaskTray {

    boolean blinking = false;
    private SDK sdk = new SDK();
    private SystemTray tray;
    private TrayIcon icon;
    private PopupMenu popup = new PopupMenu();

    public void show() {
        Image duke64x64 = Toolkit.getDefaultToolkit().createImage(ClassLoader.getSystemResource("duke-64x64.png"));
        List<Image> animations = new ArrayList<>();
        animations.add(duke64x64);
        for (int i = 1; i < 12; i++) {
            Image image = Toolkit.getDefaultToolkit().createImage(ClassLoader.getSystemResource("duke-64x64-anim" + i + ".png"));
            animations.add(image);
        }

        try {
            tray = SystemTray.getSystemTray();
            icon = new TrayIcon(duke64x64, "Shogun", popup);
            icon.setImageAutoSize(true);

            refreshItems();

            tray.add(icon);

            Thread thread = new Thread(() -> {
                while (true) {
                    while (blinking) {
                        for (Image animation : animations) {
                            icon.setImage(animation);
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (!blinking) {
                                break;
                            }
                        }
                    }
                    while (!blinking) {
                        icon.setImage(duke64x64);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            thread.setDaemon(true);
            thread.start();

        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    void refreshItems() {
        popup.removeAll();
        String version = sdk.getVersion();
        List<Version> jdkList = sdk.list("java");

        for (Version jdk : jdkList) {
            MenuItem jdkMenuItem = new JDKMenuItem(this, sdk, jdk);
            popup.add(jdkMenuItem);
        }

        MenuItem versionLabel = new MenuItem(version);
        versionLabel.setEnabled(false);
        popup.add(versionLabel);
        MenuItem exitMenu = new MenuItem("Exit");
        exitMenu.addActionListener(e -> {
            tray.remove(icon);
            System.exit(0);
        });
        popup.add(exitMenu);
    }

}