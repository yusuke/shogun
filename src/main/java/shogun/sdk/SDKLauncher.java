package shogun.sdk;

import org.slf4j.Logger;
import shogun.logging.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;

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
            pb.redirectOutput(ProcessBuilder.Redirect.to(tempFile));
            Process process = pb.start();
            PrintWriter printWriter = new PrintWriter(process.getOutputStream());
            // say no
            printWriter.write("n\n");
            printWriter.flush();
            process.waitFor();
            String response = trimANSIEscapeCodes(Files.readString(tempFile.toPath()));
            //noinspection ResultOfMethodCallIgnored
            tempFile.delete();
            logger.debug("Response:");
            logger.debug(response);
            return response;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
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
