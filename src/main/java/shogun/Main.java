package shogun;

import shogun.task.TaskTray;


public class Main {
    public static void main(String[] args) {
        System.setProperty("apple.awt.UIElement", "true");
        new TaskTray().show();
    }
}
