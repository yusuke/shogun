package shogun.sdk;

import org.slf4j.Logger;
import shogun.logging.LoggerFactory;

import java.io.*;

public class SDKLauncher {
    private final static Logger logger = LoggerFactory.getLogger();

    /**
     * Run specified command
     *
     * @param command Command to run
     * @return output
     */
    public static String exec(String... command) {
        try {
            File tempFile = File.createTempFile("sdk", "log");
            logger.debug("Command to be executed: {}", (Object) command);
            ProcessBuilder pb = new ProcessBuilder(command);
            Process process = pb.start();
            OutputStream outputStream = process.getOutputStream();
            PrintWriter printWriter = new PrintWriter(outputStream);
            // say yes
            printWriter.write("n\n");
            printWriter.flush();
            process.waitFor();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            redirectStream(process.getInputStream(), baos);
            redirectStream(new FileInputStream(tempFile), baos);
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
                System.out.write(c);
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
