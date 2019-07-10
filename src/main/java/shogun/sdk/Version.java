package shogun.sdk;

import java.io.File;
import java.util.Objects;

public class Version {
    private String candidate;
    private boolean use;
    private String version;
    private String status;

    public Version(String candidate, boolean use, String version, String status) {
        this.candidate = candidate;
        this.use = use;
        this.version = version;
        this.status = status;
    }

    public boolean isUse() {
        return use;
    }

    public void setUse(boolean use) {
        this.use = use;
    }

    String getVersion() {
        return version;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String newStatus) {
        this.status = newStatus;
    }

    public boolean isInstalled() {
        return status.equals("installed");
    }

    public boolean isLocallyInstalled() {
        return status.equals("local only");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Version version1 = (Version) o;
        return Objects.equals(version, version1.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version);
    }

    @Override
    public String toString() {
        return version;
    }

    public String getPath() {
        return new File(SDK.getSDK_MAN_DIR() + File.separator + "candidates" + File.separator + candidate + File.separator + getIdentifier()).getAbsolutePath();
    }

    public String getIdentifier() {
        return version;
    }
}
