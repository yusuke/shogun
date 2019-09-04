package shogun.sdk;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CachedSDKTest {

    @Test
    void listCandidates() {
        List<String> candidates = new CachedSDK().listCandidates();
        assertEquals(29, candidates.size());
    }

    @Test
    void list() {
        SDK sdk = new SDK();
        CachedSDK cachedSDK = new CachedSDK();
        List<Version> versionsCached = cachedSDK.list("java");

        List<Version> versionsSDK = sdk.list("java");
        assertTrue(10 < versionsCached.size());

        Optional<Version> foundBySDK = versionsSDK.stream().filter(Version::isUse).findFirst();
        Optional<Version> foundByCachedSDK = versionsCached.stream().filter(Version::isUse).findFirst();
        assertEquals(foundBySDK, foundByCachedSDK);

        assertEquals(versionsSDK.size(), versionsCached.size());

    }
}