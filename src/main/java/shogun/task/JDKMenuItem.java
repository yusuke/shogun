package shogun.task;

import shogun.sdk.SDK;
import shogun.sdk.Version;

import java.awt.*;

class JDKMenuItem extends MenuItem {
    JDKMenuItem(TaskTray taskTray, SDK sdk, Version version) {

        addActionListener(e -> {
            taskTray.blinking = true;
            sdk.makeDefault("java", version);
            taskTray.refreshItems();
            taskTray.blinking = false;

        });
        setLabel(toLabel(version));
    }

    private static String toLabel(Version version) {
        String label = version.isUse() ? "> " : "  ";
        label += version.getIdentifier();
        if (version.getStatus().length() != 0) {
            label += "(" + version.getStatus() + ")";
        }
        return label;
    }
}
