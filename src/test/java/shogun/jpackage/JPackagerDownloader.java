package shogun.jpackage;

import org.junit.jupiter.api.Test;
import shogun.sdk.Platform;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

class JPackagerDownloader {
    @Test
    void ensureJpackagerExists() throws IOException {
        // https://mail.openjdk.java.net/pipermail/openjfx-dev/2018-September/022500.html
        Path packagerRoot = Paths.get("jdk.packager");
        if (!Files.exists(packagerRoot)) {
            String fileName = "jdk.packager-";
            String os = System.getProperty("os.name").toLowerCase();
            boolean isWindows = false;
            if (Platform.isLinux) {
                fileName += "linux";
            } else if (Platform.isWindows) {
                fileName += "windows";
                isWindows = true;
            } else if (Platform.isMac) {
                fileName += "osx";
            } else {
                throw new IllegalStateException("Unsupported os:" + os);
            }
            fileName += ".zip";
            Files.createDirectory(packagerRoot);
            String downloadURL = "http://download2.gluonhq.com/jpackager/11/" + fileName;
            URL url = new URL(downloadURL);
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) url.openConnection();
                int code = conn.getResponseCode();
                if (code == 200) {
                    Files.copy(conn.getInputStream(), packagerRoot.resolve(fileName));
                } else {
                    throw new IOException("URL[" + url + "] returns code [" + code + "].");
                }
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
            unZip(packagerRoot, Paths.get("jdk.packager" + File.separator + fileName));
            Path bin = packagerRoot.resolve("jpackager" + (isWindows ? ".exe" : ""));
            //noinspection ResultOfMethodCallIgnored
            bin.toFile().setExecutable(true);
            if (isWindows) {
                // jpackager.exe needs to be located in %JAVA_HOME%\bin
                Files.move(bin, Path.of(System.getProperty("java.home") + File.separator + "bin" + File.separator + "jpackager.exe"));
                // jdk.packager.jar needs to be located in %JAVA_HOME%\bin
                Files.move(packagerRoot.resolve("jdk.packager.jar"), Path.of(System.getProperty("java.home") + File.separator + "bin" + File.separator + "jdk.packager.jar"));
            }
        }
    }


    private static void unZip(Path root, Path archiveFile) throws IOException {
        ZipFile zip = new ZipFile(archiveFile.toFile());
        Enumeration<? extends ZipEntry> entries = zip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.isDirectory()) {
                Files.createDirectories(root.resolve(entry.getName()));
            } else {
                try (InputStream is = new BufferedInputStream(zip.getInputStream(entry))) {
                    Files.copy(is, root.resolve(entry.getName()));
                }
            }

        }
    }

}

