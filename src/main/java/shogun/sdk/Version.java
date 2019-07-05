package shogun.sdk;

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

    public String getVendor() {
        return vendor;
    }

    public boolean isUse() {
        return use;
    }

    public String getVersion() {
        return version;
    }

    public String getDist() {
        return dist;
    }

    public String getStatus() {
        return status;
    }

    public String getIdentifier() {
        return identifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Version version = (Version) o;

        if (use != version.use) return false;
        if (!vendor.equals(version.vendor)) return false;
        if (!this.version.equals(version.version)) return false;
        if (!dist.equals(version.dist)) return false;
        if (status != null ? !status.equals(version.status) : version.status != null) return false;
        return identifier.equals(version.identifier);
    }

    @Override
    public int hashCode() {
        int result = vendor.hashCode();
        result = 31 * result + (use ? 1 : 0);
        result = 31 * result + version.hashCode();
        result = 31 * result + dist.hashCode();
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + identifier.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Candidate{" +
                "vendor='" + vendor + '\'' +
                ", use=" + use +
                ", version='" + version + '\'' +
                ", dist='" + dist + '\'' +
                ", status='" + status + '\'' +
                ", identifier='" + identifier + '\'' +
                '}';
    }
}
