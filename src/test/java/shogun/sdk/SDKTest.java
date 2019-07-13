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
        List<Version> java = sdk.list("ant");
        Optional<Version> notInstalled = java.stream().filter(e -> !e.getStatus().equals("installed")).findFirst();
        notInstalled.ifPresent(e -> {
                    // install the sdk
            sdk.install(e);
            assertTrue(sdk.isArchiveExists());
            List<Version> newSDKs = sdk.list("ant");
                    Optional<Version> installed = newSDKs.stream().filter(e2 -> e2.getIdentifier().equals(e.getIdentifier())).findFirst();
                    assertTrue(installed.isPresent());
            assertTrue(installed.get().isInstalled());
            sdk.makeDefault("ant", installed.get());
            assertTrue(installed.get().isUse());
                    // uninstall the sdk
            sdk.uninstall(e);
            List<Version> uninstalledSDK = sdk.list("ant");
                    Optional<Version> uninstalled = uninstalledSDK.stream().filter(e2 -> e2.getIdentifier().equals(e.getIdentifier())).findFirst();
                    assertTrue(uninstalled.isPresent());
            assertFalse(uninstalled.get().isInstalled());
            assertTrue(uninstalled.get().isArchived());
            assertFalse(installed.get().isUse());
                }
        );
    }

    @Test
    void parseOfflineModeJavaList() throws IOException, URISyntaxException {
        Path path = Paths.get(SDKTest.class.getResource("/shogun/list-java-offline-mode.txt").toURI());
        String javaVersions = Files.readString(path);
        SDK sdk = new SDK();
        List<Version> versions = sdk.parseVersions("java", javaVersions);
        assumeTrue(sdk.isOffline());
        assertEquals(5, versions.size());
        assertFalse(versions.get(0) instanceof JavaVersion);
        Version jdk14 = versions.get(0);
        assertEquals("jdk-14", jdk14.getVersion());
        assertFalse(jdk14.use);
        assertEquals("installed", jdk14.getStatus());

        Version hsadpt80212 = versions.get(1);
        assertEquals("8.0.212.hs-adpt", hsadpt80212.getVersion());
        assertFalse(hsadpt80212.use);
        assertEquals("installed", hsadpt80212.getStatus());

        Version hsadpt1201 = versions.get(2);
        assertEquals("12.0.1.hs-adpt", hsadpt1201.getVersion());
        assertFalse(hsadpt1201.use);
        assertEquals("installed", hsadpt1201.getStatus());

        Version hsadpt1103 = versions.get(3);
        assertEquals("11.0.3.hs-adpt", hsadpt1103.getVersion());
        assertTrue(hsadpt1103.use);
        assertEquals("installed", hsadpt1103.getStatus());

        Version librca1103 = versions.get(4);
        assertEquals("11.0.3-librca", librca1103.getVersion());
        assertFalse(librca1103.use);
        assertEquals("installed", librca1103.getStatus());
    }

    @Test
    void parseJavaInternetUnreachableList() throws IOException, URISyntaxException {
        Path path = Paths.get(SDKTest.class.getResource("/shogun/list-java-internet-unreachable.txt").toURI());
        String javaVersions = Files.readString(path);
        SDK sdk = new SDK();
        List<Version> versions = sdk.parseVersions("java", javaVersions);
        assumeTrue(sdk.isOffline());
        assertEquals(5, versions.size());
        assertFalse(versions.get(0) instanceof JavaVersion);
        Version jdk14 = versions.get(0);
        assertEquals("jdk-14", jdk14.getVersion());
        assertFalse(jdk14.use);
        assertEquals("installed", jdk14.getStatus());

        Version hsadpt80212 = versions.get(1);
        assertEquals("8.0.212.hs-adpt", hsadpt80212.getVersion());
        assertFalse(hsadpt80212.isUse());
        assertEquals("installed", hsadpt80212.getStatus());

        Version hsadpt1201 = versions.get(2);
        assertEquals("12.0.1.hs-adpt", hsadpt1201.getVersion());
        assertEquals("installed", hsadpt1201.getStatus());

        Version hsadpt1103 = versions.get(3);
        assertEquals("11.0.3.hs-adpt", hsadpt1103.getVersion());
        assertFalse(hsadpt1103.isUse());
        assertTrue(hsadpt1103.isInstalled());

        Version librca1103 = versions.get(4);
        assertEquals("11.0.3-librca", librca1103.getVersion());
        assertEquals("installed", hsadpt1201.getStatus());
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
                sdk.uninstall(e);
                        List<Version> javaList3 = sdk.list("java");
                        assertEquals(javaList, javaList3);
                    }
            );
        } finally {
            sdk.uninstall(new Version("java", false, localDummuyJava, "local only"));
        }

    }

    @Test
    void candidates() {
        SDK sdk = new SDK();
        assumeTrue(sdk.isInstalled());
        List<String> list = sdk.listCandidates();
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
        String groovyVersions = Files.readString(Paths.get(SDKTest.class.getResource("/shogun/list-groovy.txt").toURI()));
        List<Version> versions = new SDK().parseVersions("groovy", groovyVersions);
        assertEquals(110, versions.size());
        assertEquals("3.0.0-beta-1", versions.get(0).getVersion());
        assertEquals("3.0.0-alpha-4", versions.get(1).getVersion());
        assertEquals("2.4.6", versions.get(28).getVersion());
        for (Version version : versions) {
            assertFalse(version.isUse());
            assertFalse(version.isLocallyInstalled());
            assertFalse(version.isInstalled());
        }
    }

    @Test
    void parseVersionsMaven() throws URISyntaxException, IOException {
        String mavenVersions = Files.readString(Paths.get(SDKTest.class.getResource("/shogun/list-maven.txt").toURI()));
        List<Version> versions = new SDK().parseVersions("maven", mavenVersions);
        assertEquals(8, versions.size());

        assertEquals("3.6.1", versions.get(0).getVersion());
        assertEquals("3.6.1", versions.get(0).getIdentifier());
        assertTrue(versions.get(0).use);
        assertEquals("installed", versions.get(0).getStatus());

        assertEquals("3.6.0", versions.get(1).getVersion());
        assertEquals("3.6.0", versions.get(1).getIdentifier());
        assertFalse(versions.get(1).use);
        assertFalse(versions.get(1).isInstalled());

        assertEquals("3.5.4", versions.get(2).getVersion());
        assertEquals("3.5.4", versions.get(2).getIdentifier());
        assertFalse(versions.get(2).use);
        assertEquals("installed", versions.get(2).getStatus());
        assertFalse(versions.get(2).isLocallyInstalled());

        assertEquals("2.2.1", versions.get(7).getVersion());
        assertEquals("2.2.1", versions.get(7).getIdentifier());
        assertFalse(versions.get(7).use);
        assertEquals("local only", versions.get(7).getStatus());
    }


    @Test
    void parseVersions() throws URISyntaxException, IOException {
        URL resource = SDKTest.class.getResource("/shogun/list-java.txt");
        Path path = Paths.get(resource.toURI());
        String javaVersions = Files.readString(path);
        List<Version> versions = new SDK().parseVersions("java", javaVersions);
        assertEquals(34, versions.size());
        JavaVersion adoptOpenJDK = (JavaVersion) versions.get(0);
        assertEquals("AdoptOpenJDK", adoptOpenJDK.getVendor());
        assertTrue(adoptOpenJDK.use);
        assertEquals("12.0.1.j9", adoptOpenJDK.getVersion());
        assertEquals("adpt", adoptOpenJDK.getDist());
        assertEquals("installed", adoptOpenJDK.getStatus());
        assertEquals("12.0.1.j9-adpt", adoptOpenJDK.getIdentifier());

        JavaVersion adoptOpenJDK2 = (JavaVersion) versions.get(1);
        assertEquals("AdoptOpenJDK", adoptOpenJDK2.getVendor());
        assertFalse(adoptOpenJDK2.use);
        assertEquals("12.0.1.hs", adoptOpenJDK2.getVersion());
        assertEquals("adpt", adoptOpenJDK2.getDist());
        assertEquals("", adoptOpenJDK2.getStatus());
        assertEquals("12.0.1.hs-adpt", adoptOpenJDK2.getIdentifier());

        JavaVersion sap2 = (JavaVersion) versions.get(versions.size() - 1);

        assertEquals("SAP", sap2.getVendor());
        assertFalse(sap2.use);
        assertEquals("11.0.3", sap2.getVersion());
        assertEquals("sapmchn", sap2.getDist());
        assertEquals("", sap2.getStatus());
        assertEquals("11.0.3-sapmchn", sap2.getIdentifier());
    }

    @Test
    void isOffline() throws URISyntaxException, IOException {
        String mavenVersions = Files.readString(Paths.get(SDKTest.class.getResource("/shogun/list-maven.txt").toURI()));
        SDK sdk = new SDK();
        assertFalse(sdk.isOffline(mavenVersions));
        assertFalse(sdk.isOffline());
        String offlineResponse = Files.readString(Paths.get(SDKTest.class.getResource("/shogun/offline.txt").toURI()));

        SDK sdk1 = new SDK();
        assertTrue(sdk1.isOffline(offlineResponse));
        assertTrue(sdk1.isOffline());
        String offlineModeResponse = Files.readString((Paths.get(SDKTest.class.getResource("/shogun/offline-mode.txt").toURI())));

        SDK sdk2 = new SDK();
        assertTrue(sdk2.isOffline(offlineModeResponse));
        assertTrue(sdk2.isOffline());
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
        String parsedVersion = Files.readString(Paths.get(SDKTest.class.getResource("/shogun/version-first.txt").toURI()));
        String version = new SDK().parseSDKVersion(parsedVersion);
        assertEquals("SDKMAN 5.7.3+337", version);
    }
}