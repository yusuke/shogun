package shogun.sdk;

import org.slf4j.Logger;
import shogun.logging.LoggerFactory;

public final class Platform {
    private final static Logger logger = LoggerFactory.getLogger();

    private static final OS platform;

    enum OS {
        WINDOWS, LINUX, MACOS
    }

    static {
        String os = System.getProperty("os.name").toLowerCase();
        boolean isWindows = false;
        if (os.contains("nux")) {
            platform = OS.LINUX;
        } else if (os.startsWith("windows")) {
            platform = OS.WINDOWS;
        } else if (os.contains("mac") || os.contains("darwin")) {
            platform = OS.MACOS;
        } else {
            throw new IllegalStateException("Unsupported os:" + os);
        }
    }

    public static void isMac(Runnable runnable) {
        if (platform == OS.MACOS) {
            try {
                runnable.run();
            } catch (Exception e) {
                logger.warn("exception:", e);
            }

        }
    }

    static void isWindows(Runnable runnable) {
        if (platform == OS.WINDOWS) {
            try {
                runnable.run();
            } catch (Exception e) {
                logger.warn("exception:", e);
            }

        }
    }

    static void isLinux(Runnable runnable) {
        if (platform == OS.LINUX) {
            try {
                runnable.run();
            } catch (Exception e) {
                logger.warn("exception:", e);
            }

        }
    }

}
