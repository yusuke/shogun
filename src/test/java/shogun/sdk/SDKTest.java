package shogun.sdk;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class SDKTest {

    @Test
    void isInstalled() {
        boolean sdkAssumedTobeInstalled = Files.exists(Paths.get(SDK.getSDK_MAN_DIR()));
        assertEquals(sdkAssumedTobeInstalled, new SDK().isInstalled());
    }

    @Test
    void sdkManDir() {

        SDK sdk = new SDK();
        if (sdk.isInstalled()) {
            assertEquals(new File(System.getProperty("user.home") + File.separator + ".sdkman").getAbsolutePath(), SDK.getSDK_MAN_DIR());
        }
    }

    @Test
    void install() {
        SDK sdk = new SDK();
        if (!sdk.isInstalled()) {
            String result = sdk.install();
            assertTrue(result.contains("Enjoy!!!"));
            assertTrue(sdk.isInstalled());
            try {
                sdk.install();
                fail("should throw IllegalStateException");
            } catch (IllegalStateException ignore) {
            }
        }
    }

    @Test
    void getInstalledCandidates() {
    }

    @Test
    void list() {
        SDK sdk = new SDK();
        assumeTrue(sdk.isInstalled());
        List<Version> versions = sdk.list("java");
        assertTrue(30 < versions.size());

        List<String> installedCandidates = sdk.getInstalledCandidates();
        assertTrue(installedCandidates.contains("java"));
    }

    @Test
    void installUninstall() {
        SDK sdk = new SDK();
        List<Version> java = sdk.list("java");
        Optional<Version> notInstalled = java.stream().filter(e -> !e.getStatus().equals("installed")).findFirst();
        notInstalled.ifPresent(e -> {
                    // install the sdk
                    sdk.install("java", e);
                    List<Version> newSDKs = sdk.list("java");
                    Optional<Version> installed = newSDKs.stream().filter(e2 -> e2.getIdentifier().equals(e.getIdentifier())).findFirst();
                    assertTrue(installed.isPresent());
            assertTrue(installed.get().isInstalled());
                    // uninstall the sdk
                    sdk.uninstall("java", e);
                    List<Version> uninstalledSDK = sdk.list("java");
                    Optional<Version> uninstalled = uninstalledSDK.stream().filter(e2 -> e2.getIdentifier().equals(e.getIdentifier())).findFirst();
                    assertTrue(uninstalled.isPresent());
            assertFalse(uninstalled.get().isInstalled());
                }
        );
    }

    @Test
    void installLocal() throws IOException {
        SDK sdk = new SDK();
        String localDummuyJava = "localJava";
        try {
            List<Version> javaList = sdk.list("java");
            String dummyJDKPath = Files.createTempDirectory("dummyJDK").toAbsolutePath().toString();
            try {
                sdk.installLocal("java", "local java", dummyJDKPath);
                fail("expecting IllegalArgumentException");
            } catch (IllegalArgumentException e) {
                assertTrue(e.getMessage().contains("white space"));
            }

            boolean result = sdk.installLocal("java", localDummuyJava, dummyJDKPath);
            assertTrue(result);
            boolean result2 = sdk.installLocal("java", localDummuyJava, dummyJDKPath);
            // already exists
            assertFalse(result2);

            List<Version> javaList2 = sdk.list("java");
            Optional<Version> locallyInstalled = javaList2.stream().filter(e -> e.getIdentifier().equals(localDummuyJava)).findFirst();
            assertTrue(locallyInstalled.isPresent());
            locallyInstalled.ifPresent(e -> {
                        assertEquals(localDummuyJava, e.getIdentifier());
                        assertEquals("local only", e.getStatus());
                        assertTrue(e.isLocallyInstalled());
                        assertEquals(javaList.size() + 1, javaList2.size());
                        sdk.uninstall("java", localDummuyJava);
                        List<Version> javaList3 = sdk.list("java");
                        assertEquals(javaList, javaList3);
                    }
            );
        } finally {
            sdk.uninstall("java", localDummuyJava);
        }

    }

    @Test
    void candidates() {
        SDK sdk = new SDK();
        assumeTrue(sdk.isInstalled());
        List<String> list = sdk.list();
        assertTrue(10 < list.size());
        assertEquals("ant", list.get(0));
        assertEquals("asciidoctorj", list.get(1));
    }


    @Test
    void parseList() throws URISyntaxException, IOException {
        List<String> javaVersions = Files.readAllLines(Paths.get(SDKTest.class.getResource("/shogun/list.txt").toURI()));
        List<String> candidates = SDK.parseList(javaVersions);
        assertTrue(10 < candidates.size());
        assertEquals("ant", candidates.get(0));
        assertEquals("asciidoctorj", candidates.get(1));
    }

    @Test
    void parseVersionsGroovy() throws URISyntaxException, IOException {
        List<String> groovyVersions = Files.readAllLines(Paths.get(SDKTest.class.getResource("/shogun/list-groovy.txt").toURI()));
        List<Version> versions = new SDK().parseVersions("groovy", groovyVersions);
        assertEquals(110, versions.size());
        for (Version version : versions) {
            assertFalse(version.isUse());
            assertFalse(version.isLocallyInstalled());
            assertFalse(version.isInstalled());
        }
    }

    @Test
    void parseVersionsMaven() throws URISyntaxException, IOException {
        List<String> mavenVersions = Files.readAllLines(Paths.get(SDKTest.class.getResource("/shogun/list-maven.txt").toURI()));
        List<Version> versions = new SDK().parseVersions("maven", mavenVersions);
        assertEquals(8, versions.size());

        assertEquals("3.6.1", versions.get(0).getVersion());
        assertEquals("3.6.1", versions.get(0).getIdentifier());
        assertTrue(versions.get(0).isUse());
        assertTrue(versions.get(0).isInstalled());
        assertFalse(versions.get(0).isLocallyInstalled());

        assertEquals("3.6.0", versions.get(1).getVersion());
        assertEquals("3.6.0", versions.get(1).getIdentifier());
        assertFalse(versions.get(1).isUse());
        assertFalse(versions.get(1).isInstalled());
        assertFalse(versions.get(1).isLocallyInstalled());

        assertEquals("3.5.4", versions.get(2).getVersion());
        assertEquals("3.5.4", versions.get(2).getIdentifier());
        assertFalse(versions.get(2).isUse());
        assertTrue(versions.get(2).isInstalled());
        assertFalse(versions.get(2).isLocallyInstalled());

        assertEquals("2.2.1", versions.get(7).getVersion());
        assertEquals("2.2.1", versions.get(7).getIdentifier());
        assertFalse(versions.get(7).isUse());
        assertFalse(versions.get(7).isInstalled());
        assertTrue(versions.get(7).isLocallyInstalled());
    }


    @Test
    void parseVersions() throws URISyntaxException, IOException {
        URL resource = SDKTest.class.getResource("/shogun/list-java.txt");
        Path path = Paths.get(resource.toURI());
        List<String> javaVersions = Files.readAllLines(path);
        List<JavaVersion> versions = SDK.parseJavaVersions(javaVersions);
        assertEquals(34, versions.size());
        JavaVersion adoptOpenJDK = versions.get(0);
        assertEquals("AdoptOpenJDK", adoptOpenJDK.getVendor());
        assertTrue(adoptOpenJDK.isUse());
        assertEquals("12.0.1.j9", adoptOpenJDK.getVersion());
        assertEquals("adpt", adoptOpenJDK.getDist());
        assertEquals("installed", adoptOpenJDK.getStatus());
        assertEquals("12.0.1.j9-adpt", adoptOpenJDK.getIdentifier());

        JavaVersion adoptOpenJDK2 = versions.get(1);
        assertEquals("AdoptOpenJDK", adoptOpenJDK2.getVendor());
        assertFalse(adoptOpenJDK2.isUse());
        assertEquals("12.0.1.hs", adoptOpenJDK2.getVersion());
        assertEquals("adpt", adoptOpenJDK2.getDist());
        assertEquals("", adoptOpenJDK2.getStatus());
        assertEquals("12.0.1.hs-adpt", adoptOpenJDK2.getIdentifier());

        JavaVersion sap2 = versions.get(versions.size() - 1);

        assertEquals("SAP", sap2.getVendor());
        assertFalse(sap2.isUse());
        assertEquals("11.0.3", sap2.getVersion());
        assertEquals("sapmchn", sap2.getDist());
        assertEquals("", sap2.getStatus());
        assertEquals("11.0.3-sapmchn", sap2.getIdentifier());
    }


    @Test
    void version() throws IOException {

        SDK sdk = new SDK();
        assumeTrue(sdk.isInstalled());

        // version is stored in ~/.sdkman/var/version
        List<String> lines = Files.readAllLines(Paths.get(SDK.getSDK_MAN_DIR() + File.separator + "var" + File.separator + "version"));
        // X.Y.Z+nnn
        String versionFullString = lines.get(0);

        // SDKMAN X.Y.Z+nnn
        String version = sdk.getVersion();
        assertTrue(version.endsWith(versionFullString));
    }

    @Test
    void versionFirst() throws URISyntaxException, IOException {
        // test version description with broadcast message can be parsed.
        List<String> parsedVersion = Files.readAllLines(Paths.get(SDKTest.class.getResource("/shogun/version-first.txt").toURI()));
        String version = SDK.parseSDKVersion(parsedVersion.toArray(new String[]{}));
        assertEquals("SDKMAN 5.7.3+337", version);
    }
}