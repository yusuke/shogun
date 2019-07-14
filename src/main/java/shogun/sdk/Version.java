package shogun.sdk;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Objects;

public class Version {
    private final String candidate;
    /**
     * true if the version is marked as default. This property won't be used because isUse() checks actual symbolic link
     */
    boolean use;
    private final String version;
    private final String status;

    Version(String candidate, boolean use, String version, String status) {
        this.candidate = candidate;
        this.use = use;
        this.version = version;
        this.status = status;
    }

    public boolean isUse() {
        try {
            return Files.readSymbolicLink(getCurrentDir()).equals(getInstallationDir());
        } catch (IOException e) {
            return false;
        }
    }

    public void setUse(boolean use) {
        this.use = use;
    }

    String getVersion() {
        return version;
    }


    String getStatus() {
        return status;
    }

    public boolean isInstalled() {
        return getInstallationDir().toFile().exists() && !Files.isSymbolicLink(getInstallationDir());
    }

    public boolean isLocallyInstalled() {
        return getInstallationDir().toFile().exists() && Files.isSymbolicLink(getInstallationDir());
    }

    boolean isArchived() {
        return getArchiveFile().exists();
    }

    @SuppressWarnings("unused")
    public String getArchiveSize() {
        return toSizeStr(getArchiveFile().length());
    }

    static String toSizeStr(long length) {
        double kiloBytes = length / 1000;
        DecimalFormat decimalFormat = new DecimalFormat("###,##0.0");
        if (kiloBytes < 1000) {
            return decimalFormat.format(kiloBytes) + " KB";
        }
        double megaBytes = kiloBytes / 1000;
        if (megaBytes < 1000) {
            return decimalFormat.format(megaBytes) + " MB";
        }

        double gigaBytes = megaBytes / 1000;
        return decimalFormat.format(gigaBytes) + " GB";
    }

    @NotNull
    private File getArchiveFile() {
        return new File(SDK.getSDK_MAN_DIR() + File.separator + "archives" + File.separator + candidate + "-" + getIdentifier() + ".zip");
    }

    @NotNull
    private Path getInstallationDir() {
        return Paths.get(SDK.getSDK_MAN_DIR() + File.separator + "candidates" + File.separator + candidate + File.separator + getIdentifier());
    }

    @NotNull
    private Path getCurrentDir() {
        return Paths.get(SDK.getSDK_MAN_DIR() + File.separator + "candidates" + File.separator + candidate + File.separator + "current");
    }

    @SuppressWarnings("unused")
    public void removeArchive() {
        if (!isArchived()) {
            throw new IllegalStateException("Version not archived:" + this.toString());
        }
        //noinspection ResultOfMethodCallIgnored
        getArchiveFile().delete();
    }

    public String getCandidate() {
        return candidate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Version version1 = (Version) o;
        return candidate.equals(version1.candidate) &&
                version.equals(version1.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(candidate, version);
    }

    @Override
    public String toString() {
        return version;
    }

    public String getPath() {
        return new File(SDK.getSDK_MAN_DIR() + File.separator + "candidates" + File.separator + candidate + File.separator + getIdentifier()).getAbsolutePath();
    }

    public String getIdentifier() {
        return version;
    }
}
