package shogun.task;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import shogun.sdk.SDK;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskTrayTest {
    private TaskTray taskTray;
    private Path dummyJDK;
    private List<String> toBeUninstalled = new ArrayList<>();
    private String candidateStr;

    @Test
    void showTray() throws InterruptedException, IOException {
        String dummyJDKVersion = "15.1";
        String dummyJDKVendor = "AdoptOpenJDK";
        dummyJDK = createDummyJDK(dummyJDKVendor, dummyJDKVersion);

        taskTray = new TaskTray();
        taskTray.skipConfirmation = true;
        taskTray.show();
        taskTray.waitForActionToFinish();
        Element rootMenu = $(taskTray.popup);
        Element availableCandidates = $(taskTray.availableCandidatesMenu);

        SDK sdk = new SDK();
        List<String> installedCandidates = sdk.getInstalledCandidates();
        // number of installed candidates + Other candidates + SDKMAN version + Shogun version + Quit
        int menuCount = 4;

        assertEquals(installedCandidates.size() + menuCount, rootMenu.getItemCount(), "showing all installed candidates");
        List<String> strings = sdk.listCandidates();
        int numberOfAvailableCandidates = strings.size() - installedCandidates.size();
        assertEquals(numberOfAvailableCandidates, availableCandidates.getItemCount(), "showing all available candidates");
        System.out.println("Initialization finished");
        rootMenu.dumpLabels();

        int numberOfAvailableVersions = rootMenu.getItem(0).getItemCount();

        Element javaMenu;
        javaMenu = rootMenu.findMenuContains("java");
        if (javaMenu == null) {
            availableCandidates.findMenuContains("java");
        }
        assertNotNull(javaMenu);
        long numberOfUnregisteredJDKs = javaMenu.countMenuContains(taskTray.getMessage(Messages.notRegistered));
        if (0 < numberOfUnregisteredJDKs) {
            Element dummyJDKMenu = javaMenu.findMenuContains(dummyJDKVersion);
            // not registered java menu has "copy to clipboard", "reveal in finder", "register" menuItems
            assertEquals(3, dummyJDKMenu.getItemCount());
            // register the JDK
            dummyJDKMenu.getLast().click();
            taskTray.waitForActionToFinish();

            Element javaMenu2;
            javaMenu2 = rootMenu.findMenuContains("java");
            if (javaMenu2 == null) {
                availableCandidates.findMenuContains("java");
            }
            assertNotNull(javaMenu2);
            // number of unregistered java decreased
            assertEquals(numberOfUnregisteredJDKs - 1, javaMenu.countMenuContains(taskTray.getMessage(Messages.notRegistered)));
            Element dummyJDKMenu2 = javaMenu.findMenuContains(dummyJDKVersion);
            // uninstall the jdk
            dummyJDKMenu2.getLast().click();
            taskTray.waitForActionToFinish();
        }

        int popupItemCount = rootMenu.getItemCount();
        int availableItemCount = availableCandidates.getItemCount();
        // install 1 available candidate
        Element firstAvailable = availableCandidates.getItem(0);
        candidateStr = firstAvailable.getLabel();
        Element firstAvailableVersionMenu = firstAvailable.getItem(0);
        String candidateVersion = firstAvailableVersionMenu.getLabel().trim();
        toBeUninstalled.add(candidateVersion);
        Element installMenu = firstAvailableVersionMenu.getLast();
        // install the version
        assertEquals(taskTray.getMessage(Messages.install), installMenu.getLabel());
        System.out.printf("Installing %s:%s%n", candidateStr, candidateVersion);
        rootMenu.dumpLabels();

        installMenu.click();
        taskTray.waitForActionToFinish();

        System.out.printf("Finished %s:%s installation%n", candidateStr, candidateVersion);
        rootMenu.dumpLabels();
        assertEquals(popupItemCount + 1, rootMenu.getItemCount());
        assertEquals(availableItemCount - 1, availableCandidates.getItemCount());

        Element installedCandidate = rootMenu.findMenuContains(candidateStr);
        // ensure that the installed version is marked as default
        Element makeDefaultMenu = rootMenu.findMenuContains(candidateStr).findMenuContains(candidateVersion).findMenu(Messages.makeDefault);
        if (makeDefaultMenu != null) {
            makeDefaultMenu.click();
            taskTray.waitForActionToFinish();
        }

        Element secondVersion = installedCandidate.getLast();
        assertNotNull(installedCandidate);

        String thirdVersionLabel = secondVersion.getLabel().trim();
        toBeUninstalled.add(thirdVersionLabel);
        Element thirdVersionInstallMenu = secondVersion.findMenu(Messages.install);
        thirdVersionInstallMenu.click();
        taskTray.waitForActionToFinish();
        // make the third version default
        rootMenu.findMenuContains(candidateStr).findMenuContains(thirdVersionLabel).findMenu(Messages.makeDefault).click();
        taskTray.waitForActionToFinish();

        List<String> labels = rootMenu.findMenuContains(candidateStr).labels();
        // only 1 version is marked as default
        assertEquals(1, labels.stream().filter(e -> e.contains(">")).count());

        // test all installed versions are listed on top
        boolean installed = true;
        for (String label : labels) {
            System.out.println(label);
            if (installed && label.contains("installed")) {
                continue;
            }
            if (installed && !label.contains("installed")) {
                installed = false;
            }
            if (!installed && label.contains("installed")) {
                fail("version that is marked as installed it not listed on top");
            }
        }

        Element candidateVersionToBeUninstalledMenu = installedCandidate.getItem(0);
        Element uninstallMenu = candidateVersionToBeUninstalledMenu.getLast();
        assertEquals(taskTray.getMessage(Messages.uninstall), uninstallMenu.getLabel());

        System.out.printf("Uninstalling %s%n", candidateVersionToBeUninstalledMenu.getLabel());
        rootMenu.dumpLabels();
        uninstallMenu.click();
        taskTray.waitForActionToFinish();

        // wait for EventQueue to update menu items
        System.out.printf("Uninstalled %s%n", candidateVersionToBeUninstalledMenu.getLabel());
        rootMenu.dumpLabels();
        assertEquals(popupItemCount + 1, rootMenu.getItemCount());
        assertEquals(availableItemCount - 1, availableCandidates.getItemCount());

        rootMenu.findMenuContains(candidateStr).findMenuContains(thirdVersionLabel).findMenu(Messages.uninstall).click();
        taskTray.waitForActionToFinish();
        assertEquals(popupItemCount, rootMenu.getItemCount());
        assertEquals(availableItemCount, availableCandidates.getItemCount());

        // uninstalled version is archived
        Element uninstalledVersionMenu = availableCandidates.getItem(0).getItem(0);
        // menu has install  menu item
        assertNotNull(uninstalledVersionMenu.findMenu(Messages.install));

        // click refresh menu
        $(taskTray.versionMenu).findMenu(Messages.refresh).click();
        taskTray.waitForActionToFinish();
        assertEquals(installedCandidates.size() + menuCount, rootMenu.getItemCount(), "showing all installed candidates");
        assertEquals(numberOfAvailableCandidates, availableCandidates.getItemCount(), "showing all available candidates");
        assertEquals(numberOfAvailableVersions, rootMenu.getItem(0).getItemCount());


    }

    @AfterEach
    void finish() throws IOException {
        for (String versionStr : toBeUninstalled) {
            System.out.printf("Uninstalling %s:%s in finally block%n", candidateStr, versionStr);
            // ensure the version is uninstalled
            SDK.runSDK("uninstall " + candidateStr + " " + versionStr);
        }
        //noinspection ResultOfMethodCallIgnored
        Files.walk(dummyJDK)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    Element $(Menu menu) {
        return new Element(menu);
    }

    class Element {
        private MenuItem menu;

        Element(Menu menu) {
            this.menu = menu;
        }

        void dumpLabels() {
            for (int i = 0; i < getMenu().getItemCount(); i++) {
                System.out.println(i + ":" + getMenu().getItem(i).getLabel());
            }
        }

        long countMenuContains(String label) {
            return labels().stream().filter(e -> e.contains(label)).count();

        }

        Element(MenuItem item) {
            this.menu = item;
        }

        int getItemCount() {
            return getMenu().getItemCount();
        }

        Element getItem(@SuppressWarnings("SameParameterValue") int i) {
            try {
                return new Element(getMenu().getItem(i));
            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println("index:" + i + " not found.");
                labels().forEach(System.out::println);
                throw e;
            }
        }

        Element getLast() {
            return new Element(getMenu().getItem(getMenu().getItemCount() - 1));
        }

        private Menu getMenu() {
            return (Menu) menu;
        }

        Element findMenuContains(String label) {
            for (int i = 0; i < getMenu().getItemCount(); i++) {
                MenuItem menuItem = getMenu().getItem(i);
                if (menuItem.getLabel().contains(label)) {
                    return new Element(menuItem);
                }
            }
            System.out.println("label:" + label + " not found.");
            labels().forEach(System.out::println);
            return null;
        }

        Element findMenu(Messages message) {
            return findMenuContains(taskTray.getMessage(message).replaceAll("\\{[0-9]+}", ""));
        }

        String getLabel() {
            return menu.getLabel();
        }

        void click() {
            for (ActionListener actionListener : menu.getActionListeners()) {
                actionListener.actionPerformed(new ActionEvent(TaskTrayTest.class, 0, "dummy"));
            }
        }

        List<String> labels() {
            List<String> labels = new ArrayList<>();
            for (int i = 0; i < getMenu().getItemCount(); i++) {
                labels.add(getMenu().getItem(i).getLabel());
            }
            return labels;
        }
    }


    @NotNull
    private static Path createDummyJDK(String dummyVendor, String version) throws IOException {
        String home = System.getProperty("user.home");
        long time = System.currentTimeMillis();
        Path dummyJDKHome = Path.of(home + "/Downloads/shogun_dummyJDK" + time);
        Path bin = Path.of(home + "/Downloads/shogun_dummyJDK" + time + "/Contents/Home/bin");
        Path dummyJava = Path.of(home + "/Downloads/shogun_dummyJDK" + time + "/Contents/Home/bin/java");
        List<String> strings = Arrays.asList("#!/bin/sh",
                "echo openjdk version \\\"11.0.3\\\" 2019-04-16",
                String.format("echo OpenJDK Runtime Environment %s \\(build %s\\)", dummyVendor, version),
                String.format("echo OpenJDK 64-Bit Server VM %s \\(build %s, mixed mode\\)", dummyVendor, version));
        if (bin.toFile().mkdirs()) {
            Files.write(dummyJava, strings);
        } else {
            System.out.println("failed" + bin.toFile());
        }
        //noinspection ResultOfMethodCallIgnored
        dummyJava.toFile().setExecutable(true);
        return dummyJDKHome;
    }

}