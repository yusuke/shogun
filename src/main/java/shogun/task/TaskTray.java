package shogun.task;

import shogun.sdk.SDK;
import shogun.sdk.Version;

import java.awt.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class TaskTray {

    private boolean blinking = false;
    private List<Version> java;

    public void show() {
        System.setProperty("apple.awt.UIElement", "true");
        Image duke64x64 = Toolkit.getDefaultToolkit().createImage(ClassLoader.getSystemResource("duke-64x64.png"));
        List<Image> animations = new ArrayList<>();
        animations.add(duke64x64);
        for (int i = 1; i < 12; i++) {
            URL systemResource = ClassLoader.getSystemResource("duke-64x64-anim" + i + ".png");
            System.out.println(i + ":" + systemResource);
            Image image = Toolkit.getDefaultToolkit().createImage(systemResource);
            animations.add(image);
        }



        SDK sdk = new SDK();
        String version = sdk.getVersion();
        try {
            SystemTray tray = SystemTray.getSystemTray();
            PopupMenu popup = new PopupMenu();
            TrayIcon icon = new TrayIcon(duke64x64, "Shogun", popup);
            icon.setImageAutoSize(true);

            java = sdk.list("java");

            List<MenuItem> jdkMenuItems = new ArrayList<>();
            for (Version jdk : java) {
                MenuItem jdkItem = new MenuItem(toLabel(jdk));
                jdkItem.addActionListener(e -> {
                    blinking = true;
                    sdk.makeDefault("java", jdk);
                    java = sdk.list("java");
                    for (int i = 0; i < jdkMenuItems.size(); i++) {
                        MenuItem jdkMenuItem = jdkMenuItems.get(i);
                        jdkMenuItem.setLabel(toLabel(java.get(i)));
                    }
                    blinking = false;

                });
                jdkMenuItems.add(jdkItem);
                popup.add(jdkItem);
            }

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

    private static String toLabel(Version version) {
        String label = version.isUse() ? "> " : "  ";
        label += version.getIdentifier();
        if (version.getStatus().length() != 0) {
            label += "(" + version.getStatus() + ")";
        }
        return label;
    }
}