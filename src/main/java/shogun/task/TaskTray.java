package shogun.task;

import shogun.sdk.SDK;
import shogun.sdk.SDKLauncher;
import shogun.sdk.Version;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static javax.swing.JOptionPane.QUESTION_MESSAGE;

public class TaskTray {
    private ResourceBundle bundle = ResourceBundle.getBundle("message", Locale.getDefault());

    private boolean blinking = false;
    private SDK sdk = new SDK();
    private SystemTray tray;
    private TrayIcon icon;
    private PopupMenu popup = new PopupMenu();
    private List<Version> jdkList;
    private final JFrame thisFrameMakesDialogsAlwaysOnTop = new JFrame();

    private ImageIcon dialogIcon = new ImageIcon(Toolkit.getDefaultToolkit().createImage(ClassLoader.getSystemResource("images/duke-128x128.png")));
    private Version lastDefaultJDK;

    public void show() {
        thisFrameMakesDialogsAlwaysOnTop.setAlwaysOnTop(true);

        List<Image> animatedDuke = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            Image image = Toolkit.getDefaultToolkit().createImage(ClassLoader.getSystemResource("images/duke-64x64-anim" + i + ".png"));
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
                        icon.setImage(animatedDuke.get(0));
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

    private synchronized void refreshItems() {
        blinking = true;
        popup.removeAll();
        if (sdk.isInstalled()) {
            String version = sdk.getVersion();
            List<Version> original = sdk.list("java");
            jdkList = new ArrayList<>();
            jdkList.addAll(original.stream().filter(e -> e.isLocallyInstalled() || e.isInstalled()).collect(Collectors.toList()));
            jdkList.addAll(original.stream().filter(e -> !(e.isLocallyInstalled() || e.isInstalled())).collect(Collectors.toList()));
            for (Version jdk : jdkList) {
                if (jdk.isUse()) {
                    lastDefaultJDK = jdk;
                }
                Menu jdkMenuItem = new Menu(toLabel(jdk));
                updateMenu(jdkMenuItem, jdk);
                popup.add(jdkMenuItem);
            }

            MenuItem versionLabel = new MenuItem(version);
            versionLabel.addActionListener(e -> refreshItems());
            popup.add(versionLabel);
        } else {
            MenuItem installMenu = new MenuItem(bundle.getString("installSDKMan"));
            installMenu.addActionListener(e -> installSDKMAN());
            popup.add(installMenu);
        }


        MenuItem quitMenu = new MenuItem(bundle.getString("quit"));
        quitMenu.addActionListener(e -> {
            tray.remove(icon);
            System.exit(0);
        });
        popup.add(quitMenu);
        blinking = false;
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
        if (jdk.isInstalled() || jdk.isLocallyInstalled()) {
            if (!jdk.isUse()) {
                MenuItem menuItem = new MenuItem(bundle.getString("makeDefault"));
                menuItem.addActionListener(e -> setDefault(jdk));
                menu.add(menuItem);
            }

            MenuItem openInTerminalMenu = new MenuItem(getMessage("openInTerminal", jdk.getIdentifier()));
            openInTerminalMenu.addActionListener(e -> openInTerminal(jdk));
            menu.add(openInTerminalMenu);

            MenuItem copyPathMenu = new MenuItem(bundle.getString("copyPath"));
            copyPathMenu.addActionListener(e -> copyPathToClipboard(jdk));
            menu.add(copyPathMenu);

            MenuItem revealInFinderMenu = new MenuItem(bundle.getString("revealInFinder"));
            revealInFinderMenu.addActionListener(e -> revealInFinder(jdk));
            menu.add(revealInFinderMenu);

            MenuItem uninstallItem = new MenuItem(bundle.getString("uninstall"));
            uninstallItem.addActionListener(e -> uninstall(jdk));
            menu.add(uninstallItem);
        }

        if (!jdk.isInstalled() && !jdk.isLocallyInstalled()) {
            MenuItem menuItem = new MenuItem(bundle.getString("install"));
            menuItem.addActionListener(e -> install(jdk));
            menu.add(menuItem);
        }

    }

    private void openInTerminal(Version jdk) {
        SDKLauncher.exec("bash", "-c", String.format("osascript -e 'tell application \"Terminal\" to do script \"sdk use java %s\"';osascript -e 'tell application \"Terminal\" to activate'", jdk.getIdentifier()));
    }

    private void copyPathToClipboard(Version jdk) {
        Toolkit kit = Toolkit.getDefaultToolkit();
        Clipboard clip = kit.getSystemClipboard();

        StringSelection ss = new StringSelection(jdk.getPath());
        clip.setContents(ss, ss);
    }
    private void revealInFinder(Version jdk) {
        ProcessBuilder pb = new ProcessBuilder("open", jdk.getPath());
        try {
            Process process = pb.start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static String toLabel(Version version) {
        String label = version.isUse() ? ">" : "  ";
        label += version.toString();
        if (version.getStatus().length() != 0) {
            label += " (" + version.getStatus() + ")";
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

    private String getMessage(String pattern, String... values) {
        MessageFormat formatter = new MessageFormat(bundle.getString(pattern));
        return formatter.format(values);
    }

    private void install(Version newJDK) {
        blinking = true;
        int response = JOptionPane.showConfirmDialog(thisFrameMakesDialogsAlwaysOnTop,
                getMessage("confirmInstallMessage", newJDK.toString()),
                getMessage("confirmInstallTitle", newJDK.toString()), JOptionPane.OK_CANCEL_OPTION,
                QUESTION_MESSAGE, dialogIcon);
        if (response == JOptionPane.OK_OPTION) {
            sdk.install("java", newJDK);
            newJDK.setStatus("installed");
        }

        blinking = false;
    }


    private void uninstall(Version jdkToBeUninstalled) {
        blinking = true;
        int response = JOptionPane.showConfirmDialog(thisFrameMakesDialogsAlwaysOnTop,
                getMessage("confirmUninstallMessage", jdkToBeUninstalled.toString()),
                getMessage("confirmUninstallTitle", jdkToBeUninstalled.toString()), JOptionPane.OK_CANCEL_OPTION,
                QUESTION_MESSAGE, dialogIcon);
        if (response == JOptionPane.OK_OPTION) {
            sdk.uninstall("java", jdkToBeUninstalled);
            if (lastDefaultJDK != null && lastDefaultJDK.equals(jdkToBeUninstalled)) {
                lastDefaultJDK.setUse(false);
                lastDefaultJDK = null;
            }

            // set new jdk installed
            jdkToBeUninstalled.setStatus("");
            updateMenu((Menu) popup.getItem(jdkList.indexOf(jdkToBeUninstalled)), jdkToBeUninstalled);
        }
        blinking = false;
    }

}