package shogun.sdk;

import java.io.File;

public final class Version {
    private String vendor;
    private boolean use;
    private String version;
    private String dist;
    private String status;
    private String identifier;

    public Version(String vendor, boolean use, String version, String dist, String status, String identifier) {
        this.vendor = vendor;
        this.use = use;
        this.version = version;
        this.dist = dist;
        this.status = status;
        this.identifier = identifier;
    }

    String getVendor() {
        return vendor;
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

    String getDist() {
        return dist;
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

    public String getIdentifier() {
        return identifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Version version = (Version) o;

        if (!vendor.equals(version.vendor)) return false;
        if (!this.version.equals(version.version)) return false;
        if (!dist.equals(version.dist)) return false;
        return identifier.equals(version.identifier);
    }

    @Override
    public int hashCode() {
        int result = vendor.hashCode();
        result = 31 * result + version.hashCode();
        result = 31 * result + dist.hashCode();
        result = 31 * result + identifier.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return isLocallyInstalled() ? identifier : vendor + " " + version;
    }

    public String getPath() {
        return new File(SDK.getSDK_MAN_DIR() + File.separator + "candidates" + File.separator + "java" + File.separator + identifier).getAbsolutePath();
    }

}
