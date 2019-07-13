package shogun.task;

import org.junit.jupiter.api.Test;
import shogun.sdk.SDK;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TaskTrayTest {
    @Test
    void showTray() throws InterruptedException {
        TaskTray taskTray = new TaskTray();
        taskTray.skipConfirmation = true;
        taskTray.show();
        taskTray.waitForActionToFinish();
        SDK sdk = new SDK();
        List<String> installedCandidates = sdk.getInstalledCandidates();
        // number of installed candidates + Other candidates + SDKMAN version + Quit
        int menuCount = 3;
        assertEquals(installedCandidates.size() + menuCount, taskTray.popup.getItemCount(), "showing all installed candidates");
        List<String> strings = sdk.listCandidates();
        int numberOfAvailableCandidates = strings.size() - installedCandidates.size();
        assertEquals(numberOfAvailableCandidates, taskTray.availableCandidatesMenu.getItemCount(), "showing all available candidates");
        System.out.println("Initialization finished");
        dumpLabel(taskTray.popup);

        int numberOfAvailableVersions = ((Menu) taskTray.popup.getItem(0)).getItemCount();

        // click refresh menu
        taskTray.versionMenu.getItem(0).getActionListeners()[0].actionPerformed(new ActionEvent(TaskTrayTest.class, 0, "dummy"));
        taskTray.waitForActionToFinish();
        assertEquals(installedCandidates.size() + menuCount, taskTray.popup.getItemCount(), "showing all installed candidates");
        assertEquals(numberOfAvailableCandidates, taskTray.availableCandidatesMenu.getItemCount(), "showing all available candidates");
        assertEquals(numberOfAvailableVersions, ((Menu) taskTray.popup.getItem(0)).getItemCount());

        int popupItemCount = taskTray.popup.getItemCount();
        int availableItemCount = taskTray.availableCandidatesMenu.getItemCount();
        // install 1 available candidate
        Menu availableCandidateMenu = (Menu) taskTray.availableCandidatesMenu.getItem(0);
        String candidateStr = availableCandidateMenu.getLabel();
        Menu availableCandidateVersionMenu = (Menu) availableCandidateMenu.getItem(0);
        String candidateVersion = availableCandidateVersionMenu.getLabel();
        try {
            MenuItem installMenu = availableCandidateVersionMenu.getItem(availableCandidateVersionMenu.getItemCount() - 1);
            // install the version
            assertEquals(taskTray.getMessage(Messages.install), installMenu.getLabel());
            System.out.printf("Installing %s:%s%n", candidateStr, candidateVersion);
            dumpLabel(taskTray.popup);

            installMenu.getActionListeners()[0].actionPerformed(new ActionEvent(TaskTrayTest.class, 0, "dummy"));
            taskTray.waitForActionToFinish();

            System.out.printf("Finished %s:%s installation%n", candidateStr, candidateVersion);
            dumpLabel(taskTray.popup);
            assertEquals(popupItemCount + 1, taskTray.popup.getItemCount());
            assertEquals(availableItemCount - 1, taskTray.availableCandidatesMenu.getItemCount());

            // uninstall the version
            Menu candidateTobeUninstalledMenu = null;
            for (int i = 0; i < taskTray.popup.getItemCount(); i++) {
                Menu menu = (Menu) taskTray.popup.getItem(i);
                if (menu.getLabel().contains(candidateStr)) {
                    candidateTobeUninstalledMenu = menu;
                    break;
                }
            }
            assertNotNull(candidateTobeUninstalledMenu);
            Menu candidateVersionToBeUninstalledMenu = (Menu) candidateTobeUninstalledMenu.getItem(0);
            MenuItem uninstallMenu = candidateVersionToBeUninstalledMenu.getItem(candidateVersionToBeUninstalledMenu.getItemCount() - 1);
            assertEquals(taskTray.getMessage(Messages.uninstall), uninstallMenu.getLabel());

            System.out.printf("Uninstalling %s%n", candidateVersionToBeUninstalledMenu.getLabel());
            dumpLabel(taskTray.popup);
            uninstallMenu.getActionListeners()[0].actionPerformed(new ActionEvent(TaskTrayTest.class, 0, "dummy"));
            taskTray.waitForActionToFinish();

            // wait for EventQueue to update menu items
            System.out.printf("Uninstalled %s%n", candidateVersionToBeUninstalledMenu.getLabel());
            dumpLabel(taskTray.popup);
            assertEquals(popupItemCount, taskTray.popup.getItemCount());
            assertEquals(availableItemCount, taskTray.availableCandidatesMenu.getItemCount());

            // uninstalled version is archived
            Menu uninstalledVersionMenu = (Menu) ((Menu) taskTray.availableCandidatesMenu.getItem(0)).getItem(0);
            // menu has install  menu item
            assertEquals(1, uninstalledVersionMenu.getItemCount());

        } finally {
            System.out.printf("Uninstalling %s:%s in finally block%n", candidateStr, candidateVersion);
            // ensure the version is uninstalled
            SDK.runSDK("uninstall " + candidateStr + " " + candidateVersion);
        }
    }

    void dumpLabel(Menu menu) {
        for (int i = 0; i < menu.getItemCount(); i++) {
            System.out.println(i + ":" + menu.getItem(i).getLabel());
        }
    }
}