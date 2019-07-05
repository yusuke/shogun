package shogun.sdk;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class SDKLauncher {

    /**
     * Run specified command
     *
     * @param command Command to run
     * @return output
     */
    static String exec(String... command) {
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            Process process = pb.start();
            process.waitFor();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            redirectStream(process.getInputStream(), baos);
            redirectStream(process.getErrorStream(), baos);
            return trimANSIEscapeCodes(baos.toString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void redirectStream(InputStream from, OutputStream to) throws IOException {
        int c;
        try (InputStream is = from) {
            while ((c = is.read()) != -1) {
                to.write(c);
            }
        }
    }

    /**
     * trim ANSI escape codes to decorate terminal characters
     *
     * @param escaped string with ANSI escape sequences
     * @return string without ANSI escape sequences
     */
    static String trimANSIEscapeCodes(String escaped) {
        return escaped.replaceAll("\u001B\\[[0-9;]*m", "");
    }

}
