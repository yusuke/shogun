package shogun.jdk;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JDKScannerTest {
    @Test
    void scan() {
        Map<File, String> scan = JDKScanner.scan();
        assertNotNull(scan);
    }

    @Test
    void stringToVersion() {
        assertAll(() -> {
                    String v = "java version \"1.6.0_65\"\n" +
                            "Java(TM) SE Runtime Environment (build 1.6.0_65-b14-468)\n" +
                            "Java HotSpot(TM) 64-Bit Server VM (build 20.65-b04-468, mixed mode)\n";
                    assertEquals("1.6.0_65-b14-468", JDKScanner.stringToVersion(v));
                },
                () -> {
                    // OpenJDK 14 - jpackage
                    String v = "openjdk version \"14-jpackage\" 2020-03-17\n" +
                            "OpenJDK Runtime Environment (build 14-jpackage+1-8)\n" +
                            "OpenJDK 64-Bit Server VM (build 14-jpackage+1-8, mixed mode, sharing)\n";
                    assertEquals("OpenJDK14-jpackage+1-8", JDKScanner.stringToVersion(v));
                },
                () -> {
                    // Liberica
                    String v = "openjdk version \"11.0.3-BellSoft\" 2019-04-16\n" +
                            "LibericaJDK Runtime Environment (build 11.0.3-BellSoft+12)\n" +
                            "LibericaJDK 64-Bit Server VM (build 11.0.3-BellSoft+12, mixed mode)";
                    assertEquals("LibericaJDK11.0.3-BellSoft+12", JDKScanner.stringToVersion(v));
                },
                () -> {
                    // AdoptOpenJDK
                    String v = "openjdk version \"11.0.3\" 2019-04-16\n" +
                            "OpenJDK Runtime Environment AdoptOpenJDK (build 11.0.3+7)\n" +
                            "OpenJDK 64-Bit Server VM AdoptOpenJDK (build 11.0.3+7, mixed mode)";
                    assertEquals("AdoptOpenJDK11.0.3+7", JDKScanner.stringToVersion(v));
                },
                () -> {
                    // Zulu 11
                    String v = "openjdk version \"11.0.3\" 2019-04-16 LTS\n" +
                            "OpenJDK Runtime Environment Zulu11.31+11-CA (build 11.0.3+7-LTS)\n" +
                            "OpenJDK 64-Bit Server VM Zulu11.31+11-CA (build 11.0.3+7-LTS, mixed mode)";
                    assertEquals("Zulu11.0.3+7-LTS", JDKScanner.stringToVersion(v));
                },
                () -> {
                    // SAP 12
                    String v = "openjdk version \"12.0.1\" 2019-04-17\n" +
                            "OpenJDK Runtime Environment (build 12.0.1+12-sapmachine)\n" +
                            "OpenJDK 64-Bit Server VM (build 12.0.1+12-sapmachine, mixed mode, sharing)";
                    assertEquals("OpenJDK12.0.1+12-sapmachine", JDKScanner.stringToVersion(v));
                },
                () -> {
                    // Corretto 8
                    String v = "openjdk version \"1.8.0_212\"\n" +
                            "OpenJDK Runtime Environment Corretto-8.212.04.1 (build 1.8.0_212-b04)\n" +
                            "OpenJDK 64-Bit Server VM Corretto-8.212.04.1 (build 25.212-b04, mixed mode)";
                    assertEquals("Corretto1.8.0_212-b04", JDKScanner.stringToVersion(v));
                },
                () -> {
                    // OTN 1.8
                    String v = "java version \"1.8.0_211\"\n" +
                            "Java(TM) SE Runtime Environment (build 1.8.0_211-b12)\n" +
                            "Java HotSpot(TM) 64-Bit Server VM (build 25.211-b12, mixed mode)";
                    assertEquals("1.8.0_211-b12", JDKScanner.stringToVersion(v));
                },
                () -> {
                    // OpenJDK 14 ea
                    String v = "openjdk version \"14-ea\" 2020-03-17\n" +
                            "OpenJDK Runtime Environment (build 14-ea+5-129)\n" +
                            "OpenJDK 64-Bit Server VM (build 14-ea+5-129, mixed mode, sharing)";
                    assertEquals("OpenJDK14-ea+5-129", JDKScanner.stringToVersion(v));
                }
        );

    }
}