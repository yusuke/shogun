package shogun.sdk;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import shogun.logging.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraalUtilTest {
    private final static Logger logger = LoggerFactory.getLogger();

    @Test
    void isGraal() throws IOException {
        String baseDir = System.getProperty("user.home") + File.separator + "Downloads";
        Path dummyJDK = createDummyGraal(new File(baseDir));
        NotRegisteredVersion notRegisteredVersion = new NotRegisteredVersion("GraalVM", "19.1.0",
                "grl", "19.1.0-grl", new File(dummyJDK.toAbsolutePath() + "/Contents/Home"));
        assertFalse(GraalUtil.isNativeImageCommandInstalled(notRegisteredVersion));
        assertTrue(GraalUtil.isGraal(notRegisteredVersion));
        GraalUtil.installNativeImageCommand(notRegisteredVersion);
        assertTrue(GraalUtil.isNativeImageCommandInstalled(notRegisteredVersion));

        //noinspection ResultOfMethodCallIgnored
        Files.walk(dummyJDK)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    @NotNull
    private static Path createDummyGraal(File file) throws IOException {
        long time = System.currentTimeMillis();
        Path dummyJDKHome = Path.of(file.getAbsolutePath() + "/shogun_dummyJDK" + time);
        Path bin = Path.of(file.getAbsolutePath() + "/shogun_dummyJDK" + time + "/Contents/Home/bin");
        Path dummyJava = Path.of(file.getAbsolutePath() + "/shogun_dummyJDK" + time + "/Contents/Home/bin/java");
        List<String> strings = Arrays.asList("#!/bin/sh",
                "echo openjdk version \\\"1.8.0_212\\\"",
                "echo OpenJDK Runtime Environment \\(build 1.8.0_212-20190523183630.graal2.jdk8u-src-tar-gz-b03\\)",
                "echo OpenJDK 64-Bit GraalVM CE 19.1.0 (build 25.212-b03-jvmci-20-b04, mixed mode)");
        if (bin.toFile().mkdirs()) {
            Files.write(dummyJava, strings);
            //noinspection ResultOfMethodCallIgnored
            dummyJava.toFile().setExecutable(true);

            Path dummyGu = Path.of(file.getAbsolutePath() + "/shogun_dummyJDK" + time + "/Contents/Home/bin/gu");
            Path dummyNativeImage = Path.of(file.getAbsolutePath() + "/shogun_dummyJDK" + time + "/Contents/Home/bin/native-image");
            List<String> gu = Arrays.asList("#!/bin/sh",
                    "echo echo Downloading: Component catalog from www.graalvm.org",
                    "echo Processing component archive: Native Image",
                    "echo Downloading: Component native-image: Native Image  from github.com",
                    "echo Installing new component: Native Image \\(org.graalvm.native-image, version 19.1.0\\)",
                    "touch " + dummyNativeImage.toAbsolutePath()
            );
            Files.write(dummyGu, gu);
            //noinspection ResultOfMethodCallIgnored
            dummyGu.toFile().setExecutable(true);

        } else {
            logger.debug("failed" + bin.toFile());
        }

        return dummyJDKHome;
    }
}