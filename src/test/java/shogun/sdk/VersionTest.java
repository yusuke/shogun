package shogun.sdk;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class VersionTest {
    @Test
    void toSizeStr() {
        long eightKb = 8 * 1000;

        assertAll(
                () -> assertEquals("282.7 MB", Version.toSizeStr(282664017)),
                () -> assertEquals("8.0 KB", Version.toSizeStr(eightKb)),
                () -> assertEquals("80.0 MB", Version.toSizeStr(eightKb * 1000 * 10)),
                () -> assertEquals("8.0 GB", Version.toSizeStr(eightKb * 1000 * 1000))
        );
    }

}