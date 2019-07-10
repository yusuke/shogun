package shogun.sdk;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SDK {


    public boolean isInstalled() {
        return Files.exists(Paths.get(getSDK_MAN_DIR() + File.separator + "bin" + File.separator + "sdkman-init.sh"));
    }

    public String install() {
        if (isInstalled()) {
            throw new IllegalStateException("SDKMAN! already installed.");
        }
        return SDKLauncher.exec("bash", "-c", "curl -s \"https://get.sdkman.io\" | bash").trim();
    }

    public String getVersion() {
        return parseSDKVersion(runSDK("version").split("\n"));
    }

    static String parseSDKVersion(String[] versionString) {
        return versionString[versionString.length - 1];
    }

    public Optional<Version> getJDKinUse() {
        return list("java").stream().filter(Version::isUse).findFirst();
    }

    public List<Version> list(String candidate) {
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

    public void install(String candidate, Version version) {
        install(candidate, version.getIdentifier());
    }

    private void install(String candidate, String identifier) {
        runSDK(String.format("install %s %s", candidate, identifier));
    }

    /**
     * @param candidate  candidate
     * @param identifier identifier
     * @param path       local path
     * @return true if the
     */
    boolean installLocal(String candidate, String identifier, String path) {
        if (identifier.contains(" ")) {
            throw new IllegalArgumentException("identifier should not contain white space(s).");
        }
        String result = runSDK(String.format("install %s %s %s", candidate, escape(identifier), escape(path)));
        return !result.contains("Invalid path!") && !result.contains("already installed.");
    }

    private String escape(String string) {
        return string.replaceAll(" ", "\\\\ ");
    }

    public void uninstall(String candidate, Version version) {
        uninstall(candidate, version.getIdentifier());
    }

    void uninstall(String candidate, String identifier) {
        runSDK(String.format("uninstall %s %s", candidate, escape(identifier)));
    }

    public void makeDefault(String candidate, Version version) {
        runSDK(String.format("default %s %s", candidate, version.getIdentifier()));
    }

    private static String runSDK(String command) {
        return SDKLauncher.exec("bash", "-c", String.format("source %s/bin/sdkman-init.sh;sdk %s", getSDK_MAN_DIR(), command)).trim();
    }

    static String getSDK_MAN_DIR() {
        return SDKLauncher.exec("bash", "-c", "echo $SDKMAN_DIR").trim();
    }
}
