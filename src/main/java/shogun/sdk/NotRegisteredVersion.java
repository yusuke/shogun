package shogun.sdk;

import java.io.File;

public class NotRegisteredVersion extends JavaVersion {
    private final File file;

    NotRegisteredVersion(String vendor, String version, String dist, String identifier, File file) {
        super("java", vendor, false, version, dist, "not registered", identifier);
        this.file = file;
    }

    @Override
    public boolean isUse() {
        return false;
    }

    @Override
    public String getPath() {
        return file.getAbsolutePath();
    }
}
