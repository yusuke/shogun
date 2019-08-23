package shogun.sdk;

import org.slf4j.Logger;
import shogun.logging.LoggerFactory;

import java.io.*;
import java.lang.ProcessBuilder.Redirect;

public class SDKLauncher {
    private final static Logger logger = LoggerFactory.getLogger();

    /**
     * Run specified command
     *
     * @param command Command to run
     * @return output
     */
    public static String exec(String... command) {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("sdk", "log");
            logger.debug("Command to be executed: {}", (Object) command);
            String[] commands = new String[command.length + 2];
            commands[0] = getBash();
            commands[1] = "-c";
            System.arraycopy(command, 0, commands, 2, command.length);
            ProcessBuilder pb = new ProcessBuilder(commands)
                    .directory(new File("."))
                    .redirectErrorStream(true)
                    .redirectOutput(Redirect.to(tempFile));
            Process process = pb.start();
            OutputStream outputStream = process.getOutputStream();
            PrintWriter printWriter = new PrintWriter(outputStream);
            // say yes
            printWriter.write("n\n");
            printWriter.flush();
            process.waitFor();
            try (FileInputStream fis = new FileInputStream(tempFile)) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                fis.transferTo(baos);

                String response = trimANSIEscapeCodes(baos.toString());
                logger.debug("Response: {}", response);
                return response;
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (tempFile != null && tempFile.exists()) {
                if (!tempFile.delete()) {
                    logger.warn("Failed to delete {} even though it exists, try deleteOnExit", tempFile);
                    tempFile.deleteOnExit();
                }
            }
        }
    }

    private static String getBash() {
        if (Platform.isWindows) {
            return System.getProperty("shell.path", "c:/Program Files/Git/bin/bash");
        }
        return "bash";
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
