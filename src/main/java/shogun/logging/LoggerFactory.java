package shogun.logging;

import org.slf4j.Logger;

public final class LoggerFactory {
    private LoggerFactory() {
    }

    public static Logger getLogger() {
        return org.slf4j.LoggerFactory.getLogger(new Throwable().getStackTrace()[1].getClassName());
    }
}