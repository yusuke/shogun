package shogun.task;

import org.junit.jupiter.api.Test;
import shogun.sdk.SDK;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskTrayTest {
    @Test
    void showTray() throws InterruptedException {
        TaskTray taskTray = new TaskTray();
        taskTray.skipConfirmation = true;
        taskTray.lock = new CountDownLatch(1);
        taskTray.show();
        taskTray.lock.await();
        SDK sdk = new SDK();
        List<String> installedCandidates = sdk.getInstalledCandidates();
        // number of installed candidates + Other candidates + SDKMAN version + Quit
        int menuCount = 3;
        assertEquals(installedCandidates.size() + menuCount, taskTray.popup.getItemCount(), "showing all installed candidates");
        List<String> strings = sdk.listCandidates();
        int numberOfAvailableCandidates = strings.size() - installedCandidates.size();
        assertEquals(numberOfAvailableCandidates, taskTray.availableCandidatesMenu.getItemCount(), "showing all available candidates");

        int numberOfAvailableVersions = ((Menu) taskTray.popup.getItem(0)).getItemCount();

        // click version menu
        taskTray.lock = new CountDownLatch(1);
        taskTray.versionMenu.getActionListeners()[0].actionPerformed(new ActionEvent(TaskTrayTest.class, 0, "dummy"));

        // wait for menus to be initialized
        taskTray.lock.await();
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
            MenuItem installMenu = availableCandidateVersionMenu.getItem(0);
            // install the version
            taskTray.lock = new CountDownLatch(1);
            assertEquals(taskTray.getMessage(Messages.install), installMenu.getLabel());
            installMenu.getActionListeners()[0].actionPerformed(new ActionEvent(TaskTrayTest.class, 0, "dummy"));
            taskTray.lock.await();
            // wait for EventQueue to update menu items
            Thread.sleep(1000);
            assertEquals(popupItemCount + 1, taskTray.popup.getItemCount());
            assertEquals(availableItemCount - 1, taskTray.availableCandidatesMenu.getItemCount());

            // uninstall the version
            Menu candidateVersionToBeUninstalledMenu = (Menu) availableCandidateMenu.getItem(0);
            MenuItem uninstallMenu = candidateVersionToBeUninstalledMenu.getItem(candidateVersionToBeUninstalledMenu.getItemCount() - 1);
            assertEquals(taskTray.getMessage(Messages.uninstall), uninstallMenu.getLabel());

            taskTray.lock = new CountDownLatch(1);
            uninstallMenu.getActionListeners()[0].actionPerformed(new ActionEvent(TaskTrayTest.class, 0, "dummy"));
            taskTray.lock.await();
            // wait for EventQueue to update menu items
            Thread.sleep(1000);

            assertEquals(popupItemCount, taskTray.popup.getItemCount());
            assertEquals(availableItemCount, taskTray.availableCandidatesMenu.getItemCount());
        } finally {
            // ensure the version is uninstalled
            SDK.runSDK("uninstall " + candidateStr + " " + candidateVersion);
        }
    }
}