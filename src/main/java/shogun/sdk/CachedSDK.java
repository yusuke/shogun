package shogun.sdk;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import shogun.logging.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;


public class CachedSDK {
    private static Logger logger = LoggerFactory.getLogger();

    public static void main(String[] args) throws IOException {
        SDK sdk = new SDK();
        Properties prop = new Properties();
        File file = new File("src/main/resources/default.properties");
        if (file.exists()) {
            prop.load(new InputStreamReader(new FileInputStream(file)));
        }
        String elements = listCandidates(sdk, prop, null);
        for (String element : elements.split(",")) {
            list(sdk, element, prop, null);
        }
        prop.store(new PrintWriter(new FileOutputStream(file)), "");
    }

    private static String list(SDK sdk, String candidate, @Nullable Properties props, @Nullable Preferences prefs) {
        String list = sdk.list(candidate).stream().map(Version::getIdentifier).collect(Collectors.joining(","));
        if (props != null) {
            props.setProperty(candidate + getPlatformSuffix(), list);
        }
        if (prefs != null) {
            prefs.put(candidate + getPlatformSuffix(), list);
        }
        return list;
    }

    private static String listCandidates(SDK sdk, @Nullable Properties props, @Nullable Preferences prefs) {
        String elements = String.join(",", sdk.listCandidates());
        if (props != null) {
            props.setProperty("candidates" + getPlatformSuffix(), elements);
        }
        if (prefs != null) {
            prefs.put("candidates" + getPlatformSuffix(), elements);
        }
        return elements;
    }

    private static String getPlatformSuffix() {
        switch (Platform.platform) {
            case LINUX:
                String architecture = SDKLauncher.exec("uname -m");
                if (architecture.equals("x86_64")) {
                    return ".Linux64";
                } else {
                    // i686
                    return ".Linux32";
                }
            case MACOS:
                return ".Darwin";
            case WINDOWS:
                return ".windows";
        }
        throw new IllegalStateException("unsupported os");
    }


    private final SDK sdk = new SDK();
    private Preferences pref = Preferences.userNodeForPackage(CachedSDK.class);

    public CachedSDK() {
        try {
            Properties cache = new Properties();
            cache.load(CachedSDK.class.getResourceAsStream("/default.properties"));
            String platformSuffix = getPlatformSuffix();
            String key = "candidates" + platformSuffix;
            if (pref.get(key, "").isEmpty()) {
                String candidates = cache.getProperty(key);
                pref.put(key, candidates);
                for (String candidate : candidates.split(",")) {
                    pref.put(candidate + platformSuffix, cache.getProperty(candidate + platformSuffix));
                }
            }
        } catch (IOException e) {
            logger.warn("failed to load default.properties", e);
        }
    }

    private void ensureSDKinstalled() {
        if (!sdk.isInstalled()) {
            throw new IllegalStateException("SDK not installed.");
        }
    }


    public List<String> listCandidates() {
        ensureSDKinstalled();
        String candidates = pref.get("candidates" + getPlatformSuffix(), "");
        if (candidates.isEmpty()) {
            candidates = listCandidates(sdk, null, pref);
        }
        return Arrays.asList(candidates.split(","));
    }



    public List<Version> list(String candidate) {
        ensureSDKinstalled();
        String candidatesInPref = pref.get(candidate + getPlatformSuffix(), "");
        if (candidatesInPref.isEmpty()) {
            candidatesInPref = list(sdk, candidate, null, pref);
        }

        List<Version> versions = new ArrayList<>();
        File[] installed = new File(SDK.getSDK_MAN_DIR() + File.separator + "candidates" + File.separator + candidate).listFiles();
        if (installed != null) {
            for (File file : installed) {
                if (!file.getName().startsWith(".") && !file.getName().equals("current")) {
                    if (candidate.equals("java")) {
                        JDKScanner.getJava(file).ifPresent(versions::add);
                    } else {
                        versions.add(new Version(candidate, false, file.getName(), ""));
                    }
                }
            }
        }
        versions.sort(Comparator.comparing(Version::toString));

        for (String versionStr : candidatesInPref.split(",")) {
            if (versions.stream().filter(e -> e.getIdentifier().equals(versionStr)).findFirst().isEmpty()) {
                versions.add(new Version(candidate, false, versionStr, ""));
            }
        }
        return versions;
    }
}
