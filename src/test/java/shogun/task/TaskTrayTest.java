package shogun.task;

import org.junit.jupiter.api.Test;
import shogun.sdk.SDK;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TaskTrayTest {
    private TaskTray taskTray;

    @Test
    void showTray() throws InterruptedException {
        taskTray = new TaskTray();
        taskTray.skipConfirmation = true;
        taskTray.show();
        taskTray.waitForActionToFinish();
        Element rootMenu = $(taskTray.popup);
        Element availableCandidates = $(taskTray.availableCandidatesMenu);

        SDK sdk = new SDK();
        List<String> installedCandidates = sdk.getInstalledCandidates();
        // number of installed candidates + Other candidates + SDKMAN version + Quit
        int menuCount = 3;

        assertEquals(installedCandidates.size() + menuCount, rootMenu.getItemCount(), "showing all installed candidates");
        List<String> strings = sdk.listCandidates();
        int numberOfAvailableCandidates = strings.size() - installedCandidates.size();
        assertEquals(numberOfAvailableCandidates, availableCandidates.getItemCount(), "showing all available candidates");
        System.out.println("Initialization finished");
        rootMenu.dumpLabels();

        int numberOfAvailableVersions = rootMenu.getItem(0).getItemCount();

        // click refresh menu
        $(taskTray.versionMenu).findMenu(Messages.refresh).click();
        taskTray.waitForActionToFinish();
        assertEquals(installedCandidates.size() + menuCount, rootMenu.getItemCount(), "showing all installed candidates");
        assertEquals(numberOfAvailableCandidates, availableCandidates.getItemCount(), "showing all available candidates");
        assertEquals(numberOfAvailableVersions, rootMenu.getItem(0).getItemCount());

        int popupItemCount = rootMenu.getItemCount();
        int availableItemCount = availableCandidates.getItemCount();
        // install 1 available candidate
        Element firstAvailable = availableCandidates.getItem(0);
        String candidateStr = firstAvailable.getLabel();
        Element firstAvailableVersionMenu = firstAvailable.getItem(0);
        String candidateVersion = firstAvailableVersionMenu.getLabel();
        try {
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

            // uninstall the version
            Element candidateTobeUninstalledMenu = rootMenu.findMenuContains(candidateStr);

            assertNotNull(candidateTobeUninstalledMenu);
            Element candidateVersionToBeUninstalledMenu = candidateTobeUninstalledMenu.getItem(0);
            Element uninstallMenu = candidateVersionToBeUninstalledMenu.getLast();
            assertEquals(taskTray.getMessage(Messages.uninstall), uninstallMenu.getLabel());

            System.out.printf("Uninstalling %s%n", candidateVersionToBeUninstalledMenu.getLabel());
            rootMenu.dumpLabels();
            uninstallMenu.click();
            taskTray.waitForActionToFinish();

            // wait for EventQueue to update menu items
            System.out.printf("Uninstalled %s%n", candidateVersionToBeUninstalledMenu.getLabel());
            rootMenu.dumpLabels();
            assertEquals(popupItemCount, rootMenu.getItemCount());
            assertEquals(availableItemCount, availableCandidates.getItemCount());

            // uninstalled version is archived
            Element uninstalledVersionMenu = availableCandidates.getItem(0).getItem(0);
            // menu has install  menu item
            assertNotNull(uninstalledVersionMenu.findMenu(Messages.install));

        } finally {
            System.out.printf("Uninstalling %s:%s in finally block%n", candidateStr, candidateVersion);
            // ensure the version is uninstalled
            SDK.runSDK("uninstall " + candidateStr + " " + candidateVersion);
        }
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

        Element findMenu(Messages message) {
            String menuStr = taskTray.getMessage(message).replaceAll("\\{[0-9]+}", "");
            for (int i = 0; i < getItemCount(); i++) {
                Element item = getItem(i);
                if (item.getLabel().contains(menuStr)) {
                    return item;
                }
            }
            return null;
        }

        Element(MenuItem item) {
            this.menu = item;
        }

        int getItemCount() {
            return getMenu().getItemCount();
        }

        Element getItem(int i) {
            return new Element(getMenu().getItem(i));
        }

        Element getLast() {
            return new Element(getMenu().getItem(getMenu().getItemCount() - 1));
        }

        private Menu getMenu() {
            return (Menu) menu;
        }

        Element findMenuContains(String label) {
            for (int i = 0; i < getMenu().getItemCount(); i++) {
                MenuItem menu = getMenu().getItem(i);
                if (menu.getLabel().contains(label)) {
                    return new Element(menu);
                }
            }
            return null;
        }

        String getLabel() {
            return menu.getLabel();
        }

        void click() {
            for (ActionListener actionListener : menu.getActionListeners()) {
                actionListener.actionPerformed(new ActionEvent(TaskTrayTest.class, 0, "dummy"));
            }
        }
    }
}