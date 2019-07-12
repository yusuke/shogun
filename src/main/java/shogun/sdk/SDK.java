package shogun.sdk;

import org.slf4j.Logger;
import shogun.logging.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class SDK {
    private final static Logger logger = LoggerFactory.getLogger();
    private final static String SHOGUN_VERSION;

    static {
        Properties p = new Properties();
        try {
            p.load(SDK.class.getResourceAsStream("/version.properties"));
        } catch (IOException e) {
            logger.debug("exception while loading version.properties", e);
        }
        SHOGUN_VERSION = p.getProperty("version", "unknown");
        logger.info("SHOGUN_VERSION: {}", SHOGUN_VERSION);
    }

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
        return parseSDKVersion(runSDK("version"));
    }

    String parseSDKVersion(String versionString) {
        wasOfflineLastTime = isOffline(versionString);
        String[] split = versionString.split("\n");
        return split[split.length - 1];
    }

    public List<Version> list(String candidate) {
        return parseVersions(candidate, runSDK("list " + candidate));
    }

    private boolean wasOfflineLastTime = false;

    public boolean isOffline() {
        return wasOfflineLastTime;
    }

    boolean isOffline(String status) {
        return wasOfflineLastTime = status.contains("INTERNET NOT REACHABLE!") || status.contains("Offline:");
    }

    List<Version> parseVersions(String candidate, String response) {
        isOffline(response);
        if (!isOffline() && candidate.equals("java")) {
            return parseJavaVersions(response);
        }
        List<List<Version>> versionListList = new ArrayList<>();
        for (String line : response.split("\n")) {
            if ((isOffline() && line.matches("^ [*>].*$")) || (!isOffline() && (line.startsWith(" ") && !line.trim().isEmpty()))) {
                // line contains version
                String status = "";
                boolean currentlyInUse = false;
                String version;

                // versions are ordered as follows:
                // 1 4 7  <- versionListInOneLine
                // 2 5
                // 3 6
                List<Version> versionListInOneLine = new ArrayList<>();
                for (String element : line.replaceAll(" +", " ").trim().split(" ")) {
                    // + - local version
                    // * - installed
                    // > - currently in use
                    if (element.matches("[+*>]")) {
                        switch (element) {
                            case "+":
                                status = "local only";
                                break;
                            case ">":
                                currentlyInUse = true;
                                status = "installed";
                                break;
                            case "*":
                                status = "installed";
                        }
                    } else {
                        version = element;
                        versionListInOneLine.add(new Version(candidate, currentlyInUse, version, status));
                        status = "";
                        currentlyInUse = false;
                    }
                }
                versionListList.add(versionListInOneLine);
            }
        }
        List<Version> versionList = new ArrayList<>();
        if (0 < versionListList.size()) {
            int numberOfColumns = versionListList.get(0).size();
            for (int i = 0; i < numberOfColumns; i++) {
                for (List<Version> versions : versionListList) {
                    if (i < versions.size()) {
                        versionList.add(versions.get(i));
                    }
                }
            }
        }
        return versionList;
    }

    public List<String> listCandidates() {
        return parseList(Arrays.asList(runSDK("list").split("\n")));
    }

    static List<String> parseList(List<String> list) {
        return list.stream().filter(e -> e.contains("$ sdk install ")).map(e -> e.split("sdk install ")[1]).collect(Collectors.toList());
    }

    private List<Version> parseJavaVersions(String string) {
        List<Version> versionList = new ArrayList<>(50);
        String[] versions = string.split("\n");
        String lastVendor = "";
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
                versionList.add(new JavaVersion("java", vendor, use, versionStr, dist, status, identifier));
            }
        }

        return versionList;
    }

    public void install(Version version) {
        runSDK(String.format("install %s %s", version.getCandidate(), version.getIdentifier()));
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

    public void uninstall(Version version) {
        runSDK(String.format("uninstall %s %s", version.getCandidate(), escape(version.getIdentifier())));
    }

    public List<String> getInstalledCandidates() {
        if (!isInstalled()) {
            throw new IllegalStateException("SDKMAN! not installed!");
        }
        var candidates = new ArrayList<String>();
        File[] candidateDirs = new File(getSDK_MAN_DIR() + File.separator + "candidates").listFiles();
        if (candidateDirs != null) {
            for (File file : candidateDirs) {
                if (file.isDirectory()) {
                    File[] files = file.listFiles();
                    if (files != null && 0 < files.length) {
                        candidates.add(file.getName());
                    }
                }
            }
        }
        return candidates;
    }

    public void makeDefault(String candidate, Version version) {
        runSDK(String.format("default %s %s", candidate, version.getIdentifier()));
    }

    private static String runSDK(String command) {
        return SDKLauncher.exec("bash", "-c", String.format("source %s/bin/sdkman-init.sh;sdk %s", getSDK_MAN_DIR(), command)).trim();
    }

    private static String sdkManDir = null;

    static String getSDK_MAN_DIR() {
        if (sdkManDir == null) {
            sdkManDir = SDKLauncher.exec("bash", "-c", "source ~/.bash_profile;echo $SDKMAN_DIR").trim();
            logger.debug("SDKMAN_DIR: {}", sdkManDir);
        }
        return sdkManDir;
    }
}
