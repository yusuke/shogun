package shogun.sdk;

import java.io.*;

public class SDKLauncher {

    /**
     * Run specified command
     *
     * @param command Command to run
     * @return output
     */
    public static String exec(String... command) {

        try {
            File tempFile = File.createTempFile("sdk", "log");
            command[command.length - 1] = command[command.length - 1] + " >" + tempFile.getAbsolutePath() + " 2>&1";
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
