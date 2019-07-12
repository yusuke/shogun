package shogun.task;

import org.junit.jupiter.api.Test;
import shogun.sdk.SDK;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskTrayTest {
    @Test
    void showTray() throws InterruptedException {
        TaskTray taskTray = new TaskTray();
        taskTray.show();
        synchronized (taskTray.lock) {
            taskTray.lock.wait(180000);
        }
        SDK sdk = new SDK();
        List<String> installedCandidates = sdk.getInstalledCandidates();
        // number of installed candidates + Other candidates + SDKMAN version + Quit
        int menuCount = 3;
        assertEquals(installedCandidates.size() + menuCount, taskTray.popup.getItemCount(), "showing all installed candidates");
        List<String> strings = sdk.listCandidates();
        int numberOfAvailableCandidates = strings.size() - installedCandidates.size();
        Menu availableCandidatesMenu = taskTray.candidatesMenu;
        assertEquals(numberOfAvailableCandidates, availableCandidatesMenu.getItemCount(), "showing all available candidates");

        // click version menu
        taskTray.versionMenu.getActionListeners()[0].actionPerformed(new ActionEvent(TaskTrayTest.class, 0, "dummy"));

        // wait for menus to be initialized
        synchronized (taskTray.lock) {
            taskTray.lock.wait(180000);
        }
        assertEquals(installedCandidates.size() + menuCount, taskTray.popup.getItemCount(), "showing all installed candidates");
        assertEquals(numberOfAvailableCandidates, availableCandidatesMenu.getItemCount(), "showing all available candidates");

    }
}