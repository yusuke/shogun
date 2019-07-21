package shogun;

import shogun.sdk.Platform;
import shogun.task.TaskTray;

import java.io.File;

public class Shogun {
    public static void main(String[] args) {
        // Set ${log.path} in logback.xml
        // Use of Platform.isMac should be avoided here because log.path need to be set before logback gets initialized
        if (System.getProperty("os.name").toLowerCase().matches("^.*(mac|darwin).*$")) {
            // macOS
            System.setProperty("log.path", "/Library/Logs/Shogun".replaceAll("/", File.separator));
        } else {
            // Windows or  Linux
            System.setProperty("log.path", "/Shogun/Logs".replaceAll("/", File.separator));
        }

        Platform.isMac(() -> System.setProperty("apple.awt.UIElement", "true"));
        new TaskTray().show();
    }
}
