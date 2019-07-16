package shogun.task;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import shogun.logging.LoggerFactory;
import shogun.sdk.*;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static javax.swing.JOptionPane.QUESTION_MESSAGE;

public class TaskTray {
    private final static Logger logger = LoggerFactory.getLogger();
    private final ResourceBundle bundle = ResourceBundle.getBundle("message", Locale.getDefault());

    private final SDK sdk = new SDK();
    private SystemTray tray;
    private TrayIcon icon;
    // for the test purpose, set true to skip confirmation dialogs
    boolean skipConfirmation = false;
    final PopupMenu popup = new PopupMenu();
    final Menu availableCandidatesMenu = new Menu(getMessage(Messages.availableCandidates));
    final Menu versionMenu = new Menu();
    private final MenuItem shogunVersionMenu = new MenuItem("Shogun " + SDK.SHOGUN_VERSION);
    private final MenuItem flushArchivesMenu = new MenuItem();

    private final MenuItem quitMenu = new MenuItem(getMessage(Messages.quit));

    private final Frame thisFrameMakesDialogsAlwaysOnTop = new Frame();
    private final List<Image> animatedDuke;
    private final DukeThread duke;

    public TaskTray() {
        animatedDuke = new ArrayList<>();
        logger.debug("Loading Duke images.");
        for (int i = 0; i < 12; i++) {
            Image image = Toolkit.getDefaultToolkit().createImage(ClassLoader.getSystemResource("images/duke-64x64-anim" + i + ".png"));
            animatedDuke.add(image);
        }
        duke = new DukeThread();
        invokeLater(() -> {
            popup.add(availableCandidatesMenu);

            MenuItem refreshMenu = new MenuItem(getMessage(Messages.refresh));
            refreshMenu.addActionListener(e -> versionMenuClicked());
            versionMenu.add(refreshMenu);

            setFlushArchivesMenuLabel();
            flushArchivesMenu.addActionListener(e -> flushArchivesClicked());
            versionMenu.add(flushArchivesMenu);

            popup.add(versionMenu);

            shogunVersionMenu.setEnabled(false);
            popup.add(shogunVersionMenu);
            quitMenu.addActionListener(e -> quit());
            popup.add(quitMenu);
        });
    }

    private void setFlushArchivesMenuLabel() {
        flushArchivesMenu.setLabel(getMessage(Messages.flushArchives, sdk.getArchivesSize()));
    }


    private void invokeLater(Runnable runnable) {
        if (Thread.currentThread().getName().startsWith("AWT-EventQueue")) {
            // already in event queue
            runnable.run();
        } else {
            duke.startRoll();
            EventQueue.invokeLater(() -> {
                        runnable.run();
                        duke.stopRoll();
                    }
            );
        }
    }

    private void execute(Runnable runnable) {
        if (Thread.currentThread().getName().startsWith(EXECUTE_THREAD_NAME)) {
            // already in execute thread
            runnable.run();
        } else {
            duke.startRoll();
            executorService.execute(() -> {
                        runnable.run();
                        duke.stopRoll();
                    }
            );
        }
    }

    private void versionMenuClicked() {
        if (sdk.isInstalled()) {
            execute(this::initializeMenuItems);

        } else {
            installSDKMAN();
        }
    }


    private void flushArchivesClicked() {
        execute(() -> {
            sdk.flushArchives();
            invokeLater(this::setFlushArchivesMenuLabel);
        });
    }

    private CountDownLatch dukeLatch = new CountDownLatch(0);

    void waitForActionToFinish() throws InterruptedException {
        dukeLatch.await(60, TimeUnit.SECONDS);
    }

    private final String DUKE_THREAD_NAME = "Duke roller";
    class DukeThread extends Thread {
        final AtomicInteger integer = new AtomicInteger();


        DukeThread() {
            setName(DUKE_THREAD_NAME);
            setDaemon(true);
        }

        private synchronized void startRoll() {
            if (integer.getAndIncrement() == 0) {
                dukeLatch = new CountDownLatch(1);
                synchronized (this) {
                    this.notify();
                }
            }
        }

        private synchronized void stopRoll() {
            if (integer.decrementAndGet() == 0) {
                synchronized (this) {
                    this.notify();
                }
                dukeLatch.countDown();
            }
        }

        public void run() {
            //noinspection InfiniteLoopStatement
            while (true) {
                while (0 < integer.get()) {
                    for (Image animation : animatedDuke) {
                        EventQueue.invokeLater(() -> icon.setImage(animation));
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ignore) {
                        }
                        if (0 == integer.get()) {
                            break;
                        }
                    }
                }
                EventQueue.invokeLater(() -> icon.setImage(animatedDuke.get(0)));
                try {
                    synchronized (this) {
                        wait();
                    }
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    private final ImageIcon dialogIcon = new ImageIcon(Toolkit.getDefaultToolkit().createImage(ClassLoader.getSystemResource("images/duke-128x128.png")));

    private final String EXECUTE_THREAD_NAME = "Shogun Execute Thread";
    private final ExecutorService executorService = Executors.newFixedThreadPool(1,
            new ThreadFactory() {
                int count = 0;

                @Override
                public Thread newThread(@NotNull Runnable r) {
                    Thread thread = new Thread(r);
                    thread.setName(String.format(EXECUTE_THREAD_NAME + "[%d]", count++));
                    thread.setDaemon(true);
                    return thread;
                }
            }
    );


    public void show() {
        thisFrameMakesDialogsAlwaysOnTop.setAlwaysOnTop(true);
        EventQueue.invokeLater(() -> {
            logger.debug("Preparing task tray.");
            tray = SystemTray.getSystemTray();
            icon = new TrayIcon(animatedDuke.get(0), "Shogun", popup);
            icon.setImageAutoSize(true);
            try {
                tray.add(icon);
            } catch (AWTException e) {
                quit();
            }
        });
        duke.start();
        execute(this::initializeMenuItems);
    }

    private void quit() {
        invokeLater(() -> tray.remove(icon));
        System.exit(0);
    }

    private final Map<String, Candidate> candidateMap = new HashMap<>();

    private synchronized void initializeMenuItems() {
        logger.debug("Initializing menu items.");
        setVersionMenuLabel();

        List<String> installedCandidates = new ArrayList<>();
        if (sdk.isInstalled()) {
            sdk.getInstalledCandidates()
                    .forEach(e -> {
                        logger.debug("Installed candidate: {}", e);
                        installedCandidates.add(e);
                        candidateMap.computeIfAbsent(e, e2 -> new Candidate(e, true));
                    });
        }
        if (!sdk.isOffline()) {
            logger.debug("Offline mode.");
            // list available candidates
            sdk.listCandidates().stream()
                    .filter(e -> !installedCandidates.contains(e))
                    .forEach(e -> {
                        logger.debug("Available candidate: {}", e);
                        installedCandidates.add(e);
                        candidateMap.computeIfAbsent(e, e2 -> new Candidate(e, false));
                    });
        }
        installedCandidates.forEach(e -> candidateMap.get(e).refreshMenus());
    }

    private void setVersionMenuLabel() {
        String label;
        if (sdk.isInstalled()) {
            String version = sdk.getVersion();
            logger.debug("SDKMAN! version {} detected.", version);
            label = version + (sdk.isOffline() ? " (offline)" : "");
        } else {
            logger.debug("SDKMAN! not installed.");
            label = getMessage(Messages.installSDKMan);
        }
        invokeLater(() -> versionMenu.setLabel(label));
    }

    class Candidate {
        private final String candidate;
        private List<Version> versions;
        final Menu candidateMenu;

        Candidate(String candidate, boolean installed) {
            this.candidate = candidate;
            logger.debug("Building menu for : {}", candidate);
            candidateMenu = new Menu(candidate);
            if (installed) {
                addToInstalledCandidatesMenu(candidateMenu);
            } else {
                addToAvailableCandidatesMenu(candidateMenu);
            }
        }

        void refreshMenus() {
            logger.debug("Refreshing menus for: {}", candidate);
            this.versions = sdk.list(candidate);
            List<Version> sortedVersions = new ArrayList<>();
            versions.stream().filter(e -> e.isInstalled() || e.isLocallyInstalled()).forEach(sortedVersions::add);
            if ("java".equals(candidate)) {
                Platform.isMac(() -> {
                    logger.debug("Scanning JDK(s) not managed by SDKMAN!");
                    List<NotRegisteredVersion> jdkList = JDKScanner.scan();
                    logger.debug("Found {} JDK(s)", jdkList.size());
                    sortedVersions.addAll(jdkList);
                });
            }
            versions.stream().filter(e -> !e.isInstalled() && !e.isLocallyInstalled()).forEach(sortedVersions::add);
            this.versions = sortedVersions;
            invokeLater(() -> {
                candidateMenu.removeAll();
                setRootMenuLabel(candidateMenu);

                for (Version version : versions) {
                    Menu menu = new Menu(toLabel(version));
                    updateMenu(menu, version);
                    candidateMenu.add(menu);
                }
            });
            setFlushArchivesMenuLabel();
        }

        private boolean isInstalled() {
            return versions.stream().anyMatch(e -> e.isInstalled() || e.isLocallyInstalled());
        }

        void setRootMenuLabel(Menu menu) {
            // add version string in use
            String label = versions.stream()
                    .filter(Version::isUse).map(e -> candidate + " > " + e.toString())
                    .findFirst().orElse(candidate);
            logger.debug("setting root label to {}", label);
            invokeLater(() -> menu.setLabel(label));
        }

        Menu find(Menu menu, Version version) {
            for (int i = 0; i < menu.getItemCount(); i++) {
                Menu item = (Menu) menu.getItem(i);
                if (item.getLabel().contains(version.toString())) {
                    return item;
                }
            }
            throw new IllegalStateException("menu not found");
        }

        void setDefault(Version version) {
            execute(() -> {
                logger.debug("Set default: {}", version);
                Menu menu = candidateMenu;
                Optional<Version> lastDefault = versions.stream().filter(Version::isUse).findFirst();
                sdk.makeDefault(version.getCandidate(), version);

                Version newDefaultVersion = versions.get(versions.indexOf(version));
                newDefaultVersion.setUse(true);
                Menu newDefaultMenu = find(menu, newDefaultVersion);
                invokeLater(() -> {
                    lastDefault.ifPresent(oldDefaultVersion -> {
                        oldDefaultVersion.setUse(false);
                        Menu oldDefaultMenu = find(menu, oldDefaultVersion);
                        updateMenu(oldDefaultMenu, oldDefaultVersion);
                    });
                    updateMenu(newDefaultMenu, newDefaultVersion);
                });
                setRootMenuLabel(menu);
            });
        }

        void install(Version version) {
            boolean isNotRegisteredJDK = version instanceof NotRegisteredVersion;
            String dialogTitle = isNotRegisteredJDK ? getMessage(Messages.confirmRegisterTitle, version.getCandidate(), version.getIdentifier()) :
                    getMessage(Messages.confirmInstallTitle, version.getCandidate(), version.toString());
            String dialogMessage = isNotRegisteredJDK ? getMessage(Messages.confirmRegisterMessage, version.getCandidate(), version.getIdentifier(), version.getPath()) :
                    getMessage(Messages.confirmInstallMessage, version.getCandidate(), version.toString());
            int response = skipConfirmation ? JOptionPane.OK_OPTION :
                    JOptionPane.showConfirmDialog(thisFrameMakesDialogsAlwaysOnTop,
                            dialogMessage, dialogTitle, JOptionPane.OK_CANCEL_OPTION,
                            QUESTION_MESSAGE, dialogIcon);
            if (response == JOptionPane.OK_OPTION) {
                execute(() -> {
                    logger.debug("Install: {}", version);
                    var wasInstalled = isInstalled();
                    sdk.install(version);
                    refreshMenus();
                    if (!wasInstalled) {
                        // this candidate wasn't installed. move to installed candidates list
                        Menu candidateRootMenu = candidateMenu;
                        invokeLater(() -> availableCandidatesMenu.remove(candidateRootMenu));
                        addToInstalledCandidatesMenu(candidateRootMenu);
                    }
                });
            }
        }

        void uninstall(Version version) {
            int response = skipConfirmation ? JOptionPane.OK_OPTION :
                    JOptionPane.showConfirmDialog(thisFrameMakesDialogsAlwaysOnTop,
                            getMessage(Messages.confirmUninstallMessage, version.getCandidate(), version.toString()),
                            getMessage(Messages.confirmUninstallTitle, version.getCandidate(), version.toString()), JOptionPane.OK_CANCEL_OPTION,
                            QUESTION_MESSAGE, dialogIcon);
            if (response == JOptionPane.OK_OPTION) {
                execute(() -> {
                    logger.debug("Uninstall: {}", version);
                    var wasInstalled = isInstalled();
                    sdk.uninstall(version);
                    refreshMenus();
                    if (wasInstalled && !isInstalled()) {
                        // no version of this candidate is installed anymore. move to available candidates list
                        Menu candidateRootMenu = candidateMenu;
                        invokeLater(() -> popup.remove(candidateRootMenu));
                        addToAvailableCandidatesMenu(candidateRootMenu);
                    }
                });
            }
        }

        void addToInstalledCandidatesMenu(Menu menu) {
            boolean added = false;
            // number of installed candidates + Other candidates + SDKMAN version + Shogun version + Quit
            int menuCount = 4;
            for (int i = 0; i < popup.getItemCount() - menuCount; i++) {
                MenuItem item = popup.getItem(i);
                if (0 < item.getLabel().compareTo(candidate)) {
                    int index = i;
                    invokeLater(() -> popup.insert(menu, index));
                    added = true;
                    break;
                }
            }
            if (!added) {
                // last item in installed candidates items
                invokeLater(() -> popup.insert(menu, popup.getItemCount() - menuCount));
            }
        }

        void addToAvailableCandidatesMenu(Menu menu) {
            boolean added = false;
            for (int i = 0; i < availableCandidatesMenu.getItemCount(); i++) {
                Menu item = (Menu) availableCandidatesMenu.getItem(i);
                if (0 < item.getLabel().compareTo(candidate)) {
                    int index = i;
                    invokeLater(() -> availableCandidatesMenu.insert(menu, index));
                    added = true;
                    break;
                }
            }
            if (!added) {
                invokeLater(() -> availableCandidatesMenu.add(menu));
            }
        }

        // needs to be called inside GUI thread
        private void updateMenu(Menu menu, Version version) {
            menu.setLabel(toLabel(version));
            menu.removeAll();
            boolean isDetectedJDK = version instanceof NotRegisteredVersion;


            if ((version.isInstalled() || version.isLocallyInstalled()) && !version.isUse()) {
                MenuItem menuItem = new MenuItem(getMessage(Messages.makeDefault));
                menuItem.addActionListener(e -> setDefault(version));
                menu.add(menuItem);
            }
            if (version.isInstalled() || version.isLocallyInstalled()) {
                MenuItem openInTerminalMenu = new MenuItem(getMessage(Messages.openInTerminal, version.getIdentifier()));
                openInTerminalMenu.addActionListener(e -> openInTerminal(version));
                menu.add(openInTerminalMenu);
            }
            if (version.isDetected() || version.isInstalled() || version.isLocallyInstalled()) {
                MenuItem copyPathMenu = new MenuItem(getMessage(Messages.copyPath));
                copyPathMenu.addActionListener(e -> copyPathToClipboard(version));
                menu.add(copyPathMenu);

                MenuItem revealInFinderMenu = new MenuItem(getMessage(Messages.revealInFinder));
                revealInFinderMenu.addActionListener(e -> revealInFinder(version));
                menu.add(revealInFinderMenu);
            }

            if (version.isInstalled() || version.isLocallyInstalled()) {
                MenuItem uninstallItem = new MenuItem(getMessage(Messages.uninstall));
                uninstallItem.addActionListener(e -> uninstall(version));
                menu.add(uninstallItem);
            }

            if (!version.isInstalled() && !version.isLocallyInstalled()) {
                MenuItem menuItem = new MenuItem(getMessage(version.isDetected() ? Messages.register : Messages.install));
                menuItem.addActionListener(e -> install(version));
                menu.add(menuItem);
            }
        }

    }

    private void installSDKMAN() {
        execute(() -> {
            sdk.install();
            invokeLater(this::initializeMenuItems);
        });
    }

    private void openInTerminal(Version version) {
        SDKLauncher.exec("bash",
                "-c", String.format("osascript -e 'tell application \"Terminal\" to do script \"sdk use %s %s\"';osascript -e 'tell application \"Terminal\" to activate'",
                        version.getCandidate(), version.getIdentifier()));
    }

    private void copyPathToClipboard(Version version) {
        copyPathToClipboard(version.getPath());
    }

    private void copyPathToClipboard(String content) {
        Toolkit kit = Toolkit.getDefaultToolkit();
        Clipboard clip = kit.getSystemClipboard();
        StringSelection ss = new StringSelection(content);
        clip.setContents(ss, ss);
    }

    private void revealInFinder(Version version) {
        revealInFinder(version.getPath());
    }

    private void revealInFinder(String path) {
        ProcessBuilder pb = new ProcessBuilder("open", path);
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
        if (version.isLocallyInstalled()) {
            label += " (local only)";
        } else if (version.isInstalled()) {
            label += " (installed)";
        } else if (version.isDetected()) {
            label += " (detected)";
        }
        return label;
    }

    String getMessage(Messages message, String... values) {
        MessageFormat formatter = new MessageFormat(bundle.getString(message.name()));
        return formatter.format(values);
    }
}