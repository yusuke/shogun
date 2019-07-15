package shogun.jdk;

import org.jetbrains.annotations.NotNull;
import shogun.sdk.SDKLauncher;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JDKScanner {
    public static Map<File, String> scan() {
        List<File> result = new ArrayList<>();
        Platform.isMac(() -> {
            result.addAll(searchJDKs(new File("/Library/Java/JavaVirtualMachines")));
            result.addAll(searchJDKs(new File(System.getProperty("user.home"))));
            result.addAll(searchJDKs(new File(System.getProperty("user.home") + File.separator + "Downloads")));
        });
        Map<File, String> versionFileMap = new HashMap<>();
        result.forEach(file -> {
            File javaCommand = new File(file.getAbsoluteFile() + File.separator + "bin" + File.separator + "java");
            if (javaCommand.isFile() && javaCommand.exists()) {
                String versionString = SDKLauncher.exec("bash", "-c", javaCommand.getAbsolutePath() + " -version");
                versionFileMap.put(file, stringToVersion(versionString));
            }
        });
        return versionFileMap;
    }

    private static final Pattern buildNumberPattern = Pattern.compile("\\(build ([._+\\-0-9a-zA-Z]+)\\)");

    static String stringToVersion(String string) {
        Matcher matcher = buildNumberPattern.matcher(string);
        String buildNumber = null;
        if (matcher.find()) {
            buildNumber = matcher.group(1).replaceAll(" ", "");
        }
        String[] vendorCandidates = {"LibericaJDK", "Zulu", "Corretto", "AdoptOpenJDK", "OpenJDK"};
        String vendor = "";
        for (String vendorCandidate : vendorCandidates) {
            if (string.contains(vendorCandidate)) {
                vendor = vendorCandidate;
                break;
            }
        }
        return vendor + buildNumber;
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
