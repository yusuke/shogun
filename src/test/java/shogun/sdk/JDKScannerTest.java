package shogun.sdk;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class JDKScannerTest {
    @Test
    void scan() throws IOException {
        String vendor = "AdoptOpenJDK";
        String version = "15.0.1";
        Path dummyJDK = createDummyJDK(vendor, version);
        List<NotRegisteredVersion> scan = JDKScanner.scan();
        SDK sdk = new SDK();
        sdk.uninstall("java", version);
        assertNotNull(scan);

        assertTrue(0 < scan.size());
        Optional<NotRegisteredVersion> first = scan.stream().filter(e -> e.getVersion().equals(version)).findFirst();
        assertTrue(first.isPresent());
        NotRegisteredVersion jdkToRegister = first.get();
        try {
            sdk.install(jdkToRegister);
            List<NotRegisteredVersion> scan2 = JDKScanner.scan();
            assertEquals(scan.size() - 1, scan2.size());
        } finally {
            sdk.uninstall(jdkToRegister);
            //noinspection ResultOfMethodCallIgnored
            Files.walk(dummyJDK)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    @NotNull
    private static Path createDummyJDK(String dummyVendor, String version) throws IOException {
        String home = System.getProperty("user.home");
        long time = System.currentTimeMillis();
        Path dummyJDKHome = Path.of(home + "/Downloads/shogun_dummyJDK" + time);
        Path bin = Path.of(home + "/Downloads/shogun_dummyJDK" + time + "/Contents/Home/bin");
        Path dummyJava = Path.of(home + "/Downloads/shogun_dummyJDK" + time + "/Contents/Home/bin/java");
        List<String> strings = Arrays.asList("#!/bin/sh",
                "echo openjdk version \\\"11.0.3\\\" 2019-04-16",
                String.format("echo OpenJDK Runtime Environment %s \\(build %s\\)", dummyVendor, version),
                String.format("echo OpenJDK 64-Bit Server VM %s \\(build %s, mixed mode\\)", dummyVendor, version));
        if (bin.toFile().mkdirs()) {
            Files.write(dummyJava, strings);
        } else {
            System.out.println("failed" + bin.toFile());
        }
        //noinspection ResultOfMethodCallIgnored
        dummyJava.toFile().setExecutable(true);
        return dummyJDKHome;
    }


    @Test
    void stringToVersion() {
        assertAll(() -> {
                    String v = "java version \"1.6.0_65\"\n" +
                            "Java(TM) SE Runtime Environment (build 1.6.0_65-b14-468)\n" +
                            "Java HotSpot(TM) 64-Bit Server VM (build 20.65-b04-468, mixed mode)\n";
                    assertEquals("1.6.0_65", JDKScanner.stringToVersion(v, new File("dumy")).getIdentifier());
                },
                () -> {
                    // OpenJDK 14 - jpackage
                    String v = "openjdk version \"14-jpackage\" 2020-03-17\n" +
                            "OpenJDK Runtime Environment (build 14-jpackage+1-8)\n" +
                            "OpenJDK 64-Bit Server VM (build 14-jpackage+1-8, mixed mode, sharing)\n";
                    assertEquals("14-open", JDKScanner.stringToVersion(v, new File("dumy")).getIdentifier());
                },
                () -> {
                    // Liberica
                    String v = "openjdk version \"11.0.3-BellSoft\" 2019-04-16\n" +
                            "LibericaJDK Runtime Environment (build 11.0.3-BellSoft+12)\n" +
                            "LibericaJDK 64-Bit Server VM (build 11.0.3-BellSoft+12, mixed mode)";
                    assertEquals("11.0.3-librca", JDKScanner.stringToVersion(v, new File("dumy")).getIdentifier());
                },
                () -> {
                    // AdoptOpenJDK
                    String v = "openjdk version \"11.0.3\" 2019-04-16\n" +
                            "OpenJDK Runtime Environment AdoptOpenJDK (build 11.0.3+7)\n" +
                            "OpenJDK 64-Bit Server VM AdoptOpenJDK (build 11.0.3+7, mixed mode)";
                    assertEquals("11.0.3-adpt", JDKScanner.stringToVersion(v, new File("dumy")).getIdentifier());
                },
                () -> {
                    // Zulu 11
                    String v = "openjdk version \"11.0.3\" 2019-04-16 LTS\n" +
                            "OpenJDK Runtime Environment Zulu11.31+11-CA (build 11.0.3+7-LTS)\n" +
                            "OpenJDK 64-Bit Server VM Zulu11.31+11-CA (build 11.0.3+7-LTS, mixed mode)";
                    assertEquals("11.0.3-zulu", JDKScanner.stringToVersion(v, new File("dumy")).getIdentifier());
                },
                () -> {
                    // SAP 12
                    String v = "openjdk version \"12.0.1\" 2019-04-17\n" +
                            "OpenJDK Runtime Environment (build 12.0.1+12-sapmachine)\n" +
                            "OpenJDK 64-Bit Server VM (build 12.0.1+12-sapmachine, mixed mode, sharing)";
                    assertEquals("12.0.1-sapmchn", JDKScanner.stringToVersion(v, new File("dumy")).getIdentifier());
                },
                () -> {
                    // Corretto 8
                    String v = "openjdk version \"1.8.0_212\"\n" +
                            "OpenJDK Runtime Environment Corretto-8.212.04.1 (build 1.8.0_212-b04)\n" +
                            "OpenJDK 64-Bit Server VM Corretto-8.212.04.1 (build 25.212-b04, mixed mode)";
                    assertEquals("1.8.0_212-amzn", JDKScanner.stringToVersion(v, new File("dumy")).getIdentifier());
                },
                () -> {
                    // OTN 1.8
                    String v = "java version \"1.8.0_211\"\n" +
                            "Java(TM) SE Runtime Environment (build 1.8.0_211-b12)\n" +
                            "Java HotSpot(TM) 64-Bit Server VM (build 25.211-b12, mixed mode)";
                    assertEquals("1.8.0_211", JDKScanner.stringToVersion(v, new File("dumy")).getIdentifier());
                },
                () -> {
                    // Graal CE 19.1.0
                    String v = "openjdk version \"1.8.0_212\"\n" +
                            "OpenJDK Runtime Environment (build 1.8.0_212-20190523183630.graal2.jdk8u-src-tar-gz-b03)\n" +
                            "OpenJDK 64-Bit GraalVM CE 19.1.0 (build 25.212-b03-jvmci-20-b04, mixed mode)";
                    assertEquals("19.1.0-grl", JDKScanner.stringToVersion(v, new File("dumy")).getIdentifier());
                },
                () -> {
                    // Graal EE 19.1.0
                    String v = "java version \"1.8.0_212\"\n" +
                            "Java(TM) SE Runtime Environment (build 1.8.0_212-b31)\n" +
                            "Java HotSpot(TM) 64-Bit GraalVM EE 19.1.0 (build 25.212-b31-jvmci-20-b04, mixed mode)";
                    assertEquals("19.1.0-grl", JDKScanner.stringToVersion(v, new File("dumy")).getIdentifier());
                },
                () -> {
                    // OpenJDK 14 ea
                    String v = "openjdk version \"14-ea\" 2020-03-17\n" +
                            "OpenJDK Runtime Environment (build 14-ea+5-129)\n" +
                            "OpenJDK 64-Bit Server VM (build 14-ea+5-129, mixed mode, sharing)";
                    assertEquals("14-open", JDKScanner.stringToVersion(v, new File("dumy")).getIdentifier());
                }
        );

    }
}