package shogun.sdk;

import java.util.Map;

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

    private final static Map<String, String> vendorMap = Map.of(
            "librca", "BellSoft",
            "zulu", "Azul Zulu",
            "zulufx", "Azul ZuluFX",
            "amzn", "Amazon",
            "adpt", "AdoptOpenJDK",
            "sapmchn", "SAP",
            "grl", "GraalVM",
            "open", "Java.net");

    JavaVersion(String version, String status) {
        super("java", false, version.contains("-") ? version.split("-")[0] : version, status);

        this.identifier = version;
        String[] split = version.split("-");
        if (split.length == 2) {
            this.vendor = vendorMap.get(split[1]);
            this.dist = split[1];
        } else {
            this.vendor = "Unclassified";
            this.dist = "none";
        }
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
