package shogun.task;

import shogun.sdk.SDK;
import shogun.sdk.Version;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TaskTray {

    private boolean blinking = false;
    private SDK sdk = new SDK();
    private SystemTray tray;
    private TrayIcon icon;
    private PopupMenu popup = new PopupMenu();
    private List<Version> jdkList;

    private Version lastDefaultJDK;

    public void show() {
        List<Image> animatedDuke = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            Image image = Toolkit.getDefaultToolkit().createImage(ClassLoader.getSystemResource("duke-64x64-anim" + i + ".png"));
            animatedDuke.add(image);
        }

        try {
            tray = SystemTray.getSystemTray();
            icon = new TrayIcon(animatedDuke.get(0), "Shogun", popup);
            icon.setImageAutoSize(true);

            refreshItems();

            tray.add(icon);

            Thread thread = new Thread(() -> {
                while (true) {
                    while (blinking) {
                        for (Image animation : animatedDuke) {
                            EventQueue.invokeLater(() -> icon.setImage(animation));
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
                        EventQueue.invokeLater(() -> icon.setImage(animatedDuke.get(0)));
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

    private void refreshItems() {
        popup.removeAll();
        if (sdk.isInstalled()) {
            String version = sdk.getVersion();
            jdkList = sdk.list("java");
            for (Version jdk : jdkList) {
                if (jdk.isUse()) {
                    lastDefaultJDK = jdk;
                }
                Menu jdkMenuItem = new Menu(toLabel(jdk));
                updateMenu(jdkMenuItem, jdk);
                popup.add(jdkMenuItem);
            }

            MenuItem versionLabel = new MenuItem(version);
            versionLabel.setEnabled(false);
            popup.add(versionLabel);
        } else {
            MenuItem installMenu = new MenuItem("Install SDKMAN!");
            installMenu.addActionListener(e -> installSDKMAN());
            popup.add(installMenu);
        }


        MenuItem quitMenu = new MenuItem("Quit");
        quitMenu.addActionListener(e -> {
            tray.remove(icon);
            System.exit(0);
        });
        popup.add(quitMenu);
    }

    private void installSDKMAN() {
        blinking = true;
        sdk.install();
        refreshItems();
        blinking = false;
    }

    private void updateMenu(Menu menu, Version jdk) {
        menu.setLabel(toLabel(jdk));
        menu.removeAll();
        if (jdk.isInstalled() && !jdk.isUse()) {
            MenuItem menuItem = new MenuItem("Make default");
            menuItem.addActionListener(e -> setDefault(jdk));
            menu.add(menuItem);
        }
        if (jdk.isInstalled()) {
            MenuItem menuItem = new MenuItem("Uninstall");
            menuItem.addActionListener(e -> uninstall(jdk));
            menu.add(menuItem);
        }
        if (jdk.isInstalled()) {
            MenuItem menuItem = new MenuItem("Reveal in Finder");
            menuItem.addActionListener(e -> revealInFinder(jdk));
            menu.add(menuItem);
        }
        if (!jdk.isInstalled()) {
            MenuItem menuItem = new MenuItem("Install");
            menuItem.addActionListener(e -> install(jdk));
            menu.add(menuItem);
        }
    }

    private void revealInFinder(Version jdk) {
        jdk.revealInFinder();

    }

    private static String toLabel(Version version) {
        String label = version.isUse() ? ">" : "  ";
        label += version.getVendor() + " " + version.getVersion();
        if (version.getStatus().length() != 0) {
            label += "(" + version.getStatus() + ")";
        }
        return label;
    }

    private void setDefault(Version newJDK) {
        blinking = true;
        sdk.makeDefault("java", newJDK);

        // set last default jdk inactive
        if (lastDefaultJDK != null) {
            lastDefaultJDK.setUse(false);
            updateMenu((Menu) popup.getItem(jdkList.indexOf(lastDefaultJDK)), lastDefaultJDK);
        }

        // set new jdk active
        newJDK.setUse(true);
        updateMenu((Menu) popup.getItem(jdkList.indexOf(newJDK)), newJDK);

        lastDefaultJDK = newJDK;
        blinking = false;
    }

    private void install(Version newJDK) {
        blinking = true;
        sdk.install("java", newJDK);

        // set new jdk installed
        newJDK.setStatus("installed");
        updateMenu((Menu) popup.getItem(jdkList.indexOf(newJDK)), newJDK);

        blinking = false;
    }

    private void uninstall(Version newJDK) {
        blinking = true;
        sdk.uninstall("java", newJDK);
        if (lastDefaultJDK != null && lastDefaultJDK.equals(newJDK)) {
            lastDefaultJDK.setUse(false);
            lastDefaultJDK = null;
        }

        // set new jdk installed
        newJDK.setStatus("");
        updateMenu((Menu) popup.getItem(jdkList.indexOf(newJDK)), newJDK);

        blinking = false;
    }

}