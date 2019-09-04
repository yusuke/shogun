package shogun.sdk;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JDKScanner {
    public static List<JavaVersion> scan() {
        List<File> result = new ArrayList<>();
        Platform.isMac(() -> {
            result.addAll(searchJDKs(new File("/Library/Java/JavaVirtualMachines")));
            result.addAll(searchJDKs(new File(System.getProperty("user.home"))));
            result.addAll(searchJDKs(new File(System.getProperty("user.home") + File.separator + "Library/Java/JavaVirtualMachines")));
            result.addAll(searchJDKs(new File(System.getProperty("user.home") + File.separator + "Downloads")));
        });
        List<JavaVersion> versionList = new ArrayList<>();
        List<String> localJDKPaths = SDK.listLocallyInstalledPaths();
        result.stream()
                .filter(e -> {
                            for (String localJDKPath : localJDKPaths) {
                                if (localJDKPath.equals(e.getAbsolutePath())) {
                                    return false;
                                }
                            }
                            return true;
                        }
                ).forEach(file -> getJava(file).ifPresent(versionList::add));
        return versionList;
    }

    static Optional<JavaVersion> getJava(File maybeJavaHome) {
        File javaCommand = new File(maybeJavaHome.getAbsolutePath() + File.separator + "bin" + File.separator + "java");
        if (javaCommand.isFile() && javaCommand.exists()) {
            String versionString = SDKLauncher.exec(javaCommand.getAbsolutePath() + " -version");
            return Optional.of(stringToVersion(versionString, maybeJavaHome));
        }
        return Optional.empty();
    }

    private final static Pattern buildNumberPattern = Pattern.compile("\\(build ([._0-9a-zA-Z]+)([._+\\-0-9a-zA-Z]+)?\\)");
    private final static Pattern graalPattern = Pattern.compile("GraalVM[ A-Z]+([0-9.]+)");
    private final static Pattern javaNetVersionPattern = Pattern.compile("([0-9]+)-ea\\+([0-9]+)");
    private final static Map<String, String> candidateLabelMap = new LinkedHashMap<>();

    static {
        candidateLabelMap.put("LibericaJDK", "librca");
        candidateLabelMap.put("BellSoft", "librca");
        candidateLabelMap.put("Zulu", "zulu");
        candidateLabelMap.put("Corretto", "amzn");
        candidateLabelMap.put("AdoptOpenJDK", "adpt");
        candidateLabelMap.put("sapmachine", "sapmchn");
        candidateLabelMap.put("GraalVM", "grl");
        candidateLabelMap.put("OpenJDK", "open");
    }

    private final static Map<String, String> vendorMap = Map.of(
            "BellSoft", "BellSoft",
            "LibericaJDK", "BellSoft",
            "Zulu", "Azul Zulu",
            "Corretto", "Amazon",
            "AdoptOpenJDK", "AdoptOpenJDK",
            "sapmachine", "SAP",
            "GraalVM", "GraalVM",
            "OpenJDK", "Java.net");
    static NotRegisteredVersion stringToVersion(String string, File file) {
        String vendor = "Unclassified";
        String dist = "";

        for (String vendorCandidate : candidateLabelMap.keySet()) {
            if (string.contains(vendorCandidate)) {
                vendor = vendorMap.get(vendorCandidate);
                dist = candidateLabelMap.get(vendorCandidate);
                break;
            }
        }
        String buildNumber = null;
        Matcher matcher;
        if (dist.equals("grl")) {
            matcher = graalPattern.matcher(string);
        } else {
            matcher = buildNumberPattern.matcher(string);
        }
        if (matcher.find()) {
            buildNumber = matcher.group(1).replaceAll(" ", "");
        }
        String identifier = dist.equals("") ? buildNumber : buildNumber + "-" + dist;
        if (file.getAbsolutePath().startsWith(SDK.getSDK_MAN_DIR())) {
            identifier = file.getName();
//            buildNumber = file.getName();
        }
        if (vendor.equals("Java.net")) {
            Matcher eaVersion = javaNetVersionPattern.matcher(string);
            if (eaVersion.find()) {
                buildNumber = String.format("%s.ea.%s", eaVersion.group(1), eaVersion.group(2));
                identifier = buildNumber + "-open";
            }
        }

        return new NotRegisteredVersion(vendor, buildNumber, dist, identifier, file);
    }

    private static List<File> searchJDKs(@NotNull File file) {
        List<File> result = new ArrayList<>();
        if (file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File candidate : files) {
                    if (!Files.isSymbolicLink(candidate.toPath())) {
                        File javaHome = new File(candidate.getAbsolutePath() + File.separator + "Contents" + File.separator + "Home");
                        if (javaHome.exists() && javaHome.isDirectory()) {
                            result.add(javaHome);
                        } else if (candidate.exists() && candidate.isDirectory()) {
                            File javaCommand = new File(candidate.getAbsolutePath() + File.separator + "bin" + File.separator + "java");
                            if (javaCommand.exists() && javaCommand.isFile()) {
                                result.add(javaHome);
                            }
                        }
                    }
                }
            }
        }
        return result;
    }
}
