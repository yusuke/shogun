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
        boolean sdkAssumedTobeInstalled = Files.exists(Paths.get(SDK.SDK_MAN_DIR));
        assertEquals(sdkAssumedTobeInstalled, new SDK().isInstalled());
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
    void list() {
        SDK sdk = new SDK();
        assumeTrue(sdk.isInstalled());
        List<Version> versions = sdk.list("java");
        assertTrue(30 < versions.size());
//        List<Version> groovyVersions = sdk.list("groovy");
//        assertTrue(30 < groovyVersions.size());
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

//    @Test
//    void parseVersionsGroovy() throws URISyntaxException, IOException {
//        List<String> groovyVersions = Files.readAllLines(Paths.get(SDKTest.class.getResource("/shogun/list-groovy.txt").toURI()));
//        List<Version> versions = SDK.parseVersions(groovyVersions);
//        assertEquals(110, versions.size());
//    }

    @Test
    void parseVersions() throws URISyntaxException, IOException {
        URL resource = SDKTest.class.getResource("/shogun/list-java.txt");
        Path path = Paths.get(resource.toURI());
        List<String> javaVersions = Files.readAllLines(path);
        List<Version> versions = SDK.parseVersions(javaVersions);
        assertEquals(34, versions.size());
        Version adoptOpenJDK = versions.get(0);
        assertEquals("AdoptOpenJDK", adoptOpenJDK.getVendor());
        assertTrue(adoptOpenJDK.isUse());
        assertEquals("12.0.1.j9", adoptOpenJDK.getVersion());
        assertEquals("adpt", adoptOpenJDK.getDist());
        assertEquals("installed", adoptOpenJDK.getStatus());
        assertEquals("12.0.1.j9-adpt", adoptOpenJDK.getIdentifier());

        Version adoptOpenJDK2 = versions.get(1);
        assertEquals("AdoptOpenJDK", adoptOpenJDK2.getVendor());
        assertFalse(adoptOpenJDK2.isUse());
        assertEquals("12.0.1.hs", adoptOpenJDK2.getVersion());
        assertEquals("adpt", adoptOpenJDK2.getDist());
        assertEquals("", adoptOpenJDK2.getStatus());
        assertEquals("12.0.1.hs-adpt", adoptOpenJDK2.getIdentifier());

        Version sap2 = versions.get(versions.size() - 1);

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
        List<String> lines = Files.readAllLines(Paths.get(SDK.SDK_MAN_DIR + File.separator + "var" + File.separator + "version"));
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