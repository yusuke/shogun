package shogun.sdk;

public class JavaVersion extends Version {
    private final String dist;
    private final String identifier;
    private final String vendor;

    JavaVersion(String candidate, String vendor, boolean use, String version, String dist, String status, String identifier) {
        super(candidate, use, version, status);
        this.vendor = vendor;
        this.dist = dist;
        this.identifier = identifier;

    }

    String getVendor() {
        return vendor;
    }

    String getDist() {
        return dist;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String toString() {
        return getInstallationDir().startsWith(SDK.getSDK_MAN_DIR()) ? (vendor + " " + getVersion()) : identifier;
    }
}
