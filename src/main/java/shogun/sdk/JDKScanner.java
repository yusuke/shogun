package shogun.sdk;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JDKScanner {
    public static List<NotRegisteredVersion> scan() {
        List<File> result = new ArrayList<>();
        Platform.isMac(() -> {
            result.addAll(searchJDKs(new File("/Library/Java/JavaVirtualMachines")));
            result.addAll(searchJDKs(new File(System.getProperty("user.home"))));
            result.addAll(searchJDKs(new File(System.getProperty("user.home") + File.separator + "Library/Java/JavaVirtualMachines")));
            result.addAll(searchJDKs(new File(System.getProperty("user.home") + File.separator + "Downloads")));
        });
        List<NotRegisteredVersion> versionList = new ArrayList<>();
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
                ).forEach(file -> {
            File javaCommand = new File(file.getAbsoluteFile() + File.separator + "bin" + File.separator + "java");
            if (javaCommand.isFile() && javaCommand.exists()) {
                String versionString = SDKLauncher.exec("bash", "-c", javaCommand.getAbsolutePath() + " -version");
                versionList.add(stringToVersion(versionString, file));
            }
        });
        return versionList;
    }

    private final static Pattern buildNumberPattern = Pattern.compile("\\(build ([._0-9a-zA-Z]+)([._+\\-0-9a-zA-Z]+)?\\)");
    private final static Pattern graalPattern = Pattern.compile("GraalVM[ A-Z]+([0-9.]+)");
    private final static String[] vendorCandidates = {"LibericaJDK", "Zulu", "Corretto", "AdoptOpenJDK", "sapmachine", "GraalVM", "OpenJDK"};
    private final static String[] vendorLabel = {"librca", "zulu", "amzn", "adpt", "sapmchn", "grl", "open"};

    static NotRegisteredVersion stringToVersion(String string, File file) {
        String vendor = "Unclassified";
        String dist = "";
        for (int i = 0; i < vendorCandidates.length; i++) {
            String vendorCandidate = vendorCandidates[i];
            if (string.contains(vendorCandidate)) {
                vendor = vendorCandidate;
                dist = vendorLabel[i];
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
