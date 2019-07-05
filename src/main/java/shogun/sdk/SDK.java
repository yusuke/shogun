package shogun.sdk;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SDK {

    static final String SDK_MAN_DIR = System.getProperty("user.home") + File.separator + ".sdkman";

    boolean isInstalled() {
        return Files.exists(Paths.get(SDK_MAN_DIR + File.separator + "bin" + File.separator + "sdkman-init.sh"));
    }

    public String getVersion() {
        return runSDK("version");
    }

    List<Version> list(String candidate) {
        return parseVersions(Arrays.asList(runSDK("list " + candidate).split("\n")));
    }

    List<String> list() {
        return parseList(Arrays.asList(runSDK("list").split("\n")));
    }

    static List<String> parseList(List<String> list) {
        return list.stream().filter(e -> e.contains("$ sdk install ")).map(e -> e.split("sdk install ")[1]).collect(Collectors.toList());
    }

    static List<Version> parseVersions(List<String> versions) {
        String lastVendor = "";
        List<Version> versionList = new ArrayList<>(50);
        for (String line : versions) {
            if (line.contains("|") && !(line.contains("Vendor") && line.contains("Use") && line.contains("Dist"))) {
                // line contains version
                String[] split = line.split("\\|");
                String vendor = split[0].trim();
                if (vendor.length() == 0) {
                    vendor = lastVendor;
                } else {
                    lastVendor = vendor;
                }
                boolean use = ">>>".equals(split[1].trim());
                String versionStr = split[2].trim();
                String dist = split[3].trim();
                String status = split[4].trim();
                String identifier = split[5].trim();
                versionList.add(new Version(vendor, use, versionStr, dist, status, identifier));
            }
        }
        return versionList;
    }

    void install(Version version) {


    }

    void uninstall(Version version) {

    }

    void use(Version version) {
    }

    private static String runSDK(String command) {
        return SDKLauncher.exec("bash", "-c", String.format("source %s/bin/sdkman-init.sh;sdk %s", SDK_MAN_DIR, command)).trim();
    }
}
