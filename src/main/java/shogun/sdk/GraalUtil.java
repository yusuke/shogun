package shogun.sdk;

import java.io.File;

public class GraalUtil {
    public static boolean isGraal(Version version) {
        return (version.isInstalled() || version.isLocallyInstalled() || version.isDetected()) && version.getIdentifier().endsWith("grl");
    }

    public static boolean isNativeImageCommandInstalled(Version version) {
        String commandPath = version.getPath() + File.separator + "bin" + File.separator + "native-image";
        if (Platform.isWindows) {
            commandPath += ".exe";
        }
        return new File(commandPath).exists();
    }

    public static void installNativeImageCommand(Version version) {
        if (isNativeImageCommandInstalled(version)) {
            throw new IllegalStateException("native-image already installed");
        }
        String gu = version.getPath() + File.separator + "bin" + File.separator + "gu";
        SDKLauncher.exec(gu + " install native-image");
    }
}
