package shogun.sdk;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SDKLauncherTest {
    @Test
    void trimANSIEscapeCodes() {
        // 8 Colors
        assertEquals("Black", SDKLauncher.trimANSIEscapeCodes("\u001b[30mBlack\u001B[0m"));
        assertEquals("Red", SDKLauncher.trimANSIEscapeCodes("\u001b[31mRed\u001B[0m"));
        assertEquals("Green", SDKLauncher.trimANSIEscapeCodes("\u001b[32mGreen\u001B[0m"));
        assertEquals("Yellow", SDKLauncher.trimANSIEscapeCodes("\u001b[33mYellow\u001B[0m"));
        assertEquals("Blue", SDKLauncher.trimANSIEscapeCodes("\u001b[34mBlue\u001B[0m"));
        assertEquals("Magenta", SDKLauncher.trimANSIEscapeCodes("\u001b[35mMagenta\u001B[0m"));
        assertEquals("Cyan", SDKLauncher.trimANSIEscapeCodes("\u001b[36mCyan\u001B[0m"));
        assertEquals("White", SDKLauncher.trimANSIEscapeCodes("\u001b[37mWhite\u001B[0m"));

        // 16 colors
        assertEquals("Bright black", SDKLauncher.trimANSIEscapeCodes("\u001b[30mBright black\u001B[0m"));
        assertEquals("Bright red", SDKLauncher.trimANSIEscapeCodes("\u001b[31mBright red\u001B[0m"));
        assertEquals("Bright green", SDKLauncher.trimANSIEscapeCodes("\u001b[32mBright green\u001B[0m"));
        assertEquals("Bright yellow", SDKLauncher.trimANSIEscapeCodes("\u001b[33mBright yellow\u001B[0m"));
        assertEquals("Bright blue", SDKLauncher.trimANSIEscapeCodes("\u001b[34mBright blue\u001B[0m"));
        assertEquals("Bright magenta", SDKLauncher.trimANSIEscapeCodes("\u001b[35mBright magenta\u001B[0m"));
        assertEquals("Bright cyan", SDKLauncher.trimANSIEscapeCodes("\u001b[36mBright cyan\u001B[0m"));
        assertEquals("Bright white", SDKLauncher.trimANSIEscapeCodes("\u001b[37mBright white\u001B[0m"));

        // 256 colors
        assertEquals("1", SDKLauncher.trimANSIEscapeCodes("\u001b[38;5;1m1\u001B[0m"));
        assertEquals("2", SDKLauncher.trimANSIEscapeCodes("\u001b[38;5;2m2\u001B[0m"));
        assertEquals("3", SDKLauncher.trimANSIEscapeCodes("\u001b[38;5;3m3\u001B[0m"));
        assertEquals("35", SDKLauncher.trimANSIEscapeCodes("\u001b[38;5;35m35\u001B[0m"));
        assertEquals("70", SDKLauncher.trimANSIEscapeCodes("\u001b[38;5;70m70\u001B[0m"));
        assertEquals("162", SDKLauncher.trimANSIEscapeCodes("\u001b[38;5;162m162\u001B[0m"));

        // background colors

        assertEquals("Background black", SDKLauncher.trimANSIEscapeCodes("\u001b[40mBackground black\u001B[0m"));
        assertEquals("Background red", SDKLauncher.trimANSIEscapeCodes("\u001b[41mBackground red\u001B[0m"));
        assertEquals("Background green", SDKLauncher.trimANSIEscapeCodes("\u001b[42mBackground green\u001B[0m"));
        assertEquals("Background yellow", SDKLauncher.trimANSIEscapeCodes("\u001b[43mBackground yellow\u001B[0m"));
        assertEquals("Background blue", SDKLauncher.trimANSIEscapeCodes("\u001b[44mBackground blue\u001B[0m"));
        assertEquals("Background magenta", SDKLauncher.trimANSIEscapeCodes("\u001b[45mBackground magenta\u001B[0m"));
        assertEquals("Background cyan", SDKLauncher.trimANSIEscapeCodes("\u001b[46mBackground cyan\u001B[0m"));
        assertEquals("Background white", SDKLauncher.trimANSIEscapeCodes("\u001b[47mBackground white\u001B[0m"));

        assertEquals("Bright Background black", SDKLauncher.trimANSIEscapeCodes("\u001b[40;1mBright Background black\u001B[0m"));
        assertEquals("Bright Background red", SDKLauncher.trimANSIEscapeCodes("\u001b[41;1mBright Background red\u001B[0m"));
        assertEquals("Bright Background green", SDKLauncher.trimANSIEscapeCodes("\u001b[42;1mBright Background green\u001B[0m"));
        assertEquals("Bright Background yellow", SDKLauncher.trimANSIEscapeCodes("\u001b[43;1mBright Background yellow\u001B[0m"));
        assertEquals("Bright Background blue", SDKLauncher.trimANSIEscapeCodes("\u001b[44;1mBright Background blue\u001B[0m"));
        assertEquals("Bright Background magenta", SDKLauncher.trimANSIEscapeCodes("\u001b[45;1mBright Background magenta\u001B[0m"));
        assertEquals("Bright Background cyan", SDKLauncher.trimANSIEscapeCodes("\u001b[46;1mBright Background cyan\u001B[0m"));
        assertEquals("Bright Background white", SDKLauncher.trimANSIEscapeCodes("\u001b[47;1mBright Background white\u001B[0m"));

        // decorations
        assertEquals("Bold", SDKLauncher.trimANSIEscapeCodes("\u001b[1mBold\u001B[0m"));
        assertEquals("Underline", SDKLauncher.trimANSIEscapeCodes("\u001b[4mUnderline\u001B[0m"));
        assertEquals("Reversed", SDKLauncher.trimANSIEscapeCodes("\u001b[7mReversed\u001B[0m"));

//        // cursor navigations
//        assertEquals("Up", SDKLauncher.trimANSIEscapeCodes("\u001b[10AUp\u001B[0m"));
//        assertEquals("Down", SDKLauncher.trimANSIEscapeCodes("\u001b[20BDown\u001B[0m"));
//        assertEquals("Right", SDKLauncher.trimANSIEscapeCodes("\u001b[30CRight\u001B[0m"));
//        assertEquals("Left", SDKLauncher.trimANSIEscapeCodes("\u001b[40DLeft\u001B[0m"));

        //combination
        assertEquals(" A  B  C  D ", SDKLauncher.trimANSIEscapeCodes("\u001b[40m A \u001b[41m B \u001b[42m C \u001b[43m D \u001b[0m"));


        assertEquals("SDKMAN 5.7.3+337", SDKLauncher.trimANSIEscapeCodes("\u001B[1;33mSDKMAN 5.7.3+337\u001B[0m"));
        assertEquals("hoge", SDKLauncher.trimANSIEscapeCodes("\u001b[30;1mhoge\u001B[0m"));
        assertEquals("hoge", SDKLauncher.trimANSIEscapeCodes("\u001b[38;5;37mhoge\u001B[0m"));
        assertEquals("[1;33mSDKMAN 5.7.3+337[0m", SDKLauncher.trimANSIEscapeCodes("[1;33mSDKMAN 5.7.3+337[0m"));

    }


}