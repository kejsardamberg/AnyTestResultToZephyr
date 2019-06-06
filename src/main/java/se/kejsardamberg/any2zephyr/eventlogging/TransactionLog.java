package se.kejsardamberg.any2zephyr.eventlogging;

import se.kejsardamberg.any2zephyr.main.Settings;

import java.io.File;

public class TransactionLog {

    private Logger logger = new Logger(Settings.transactionLogFileName);

    public void log(String message){
        logger.log(message);
    }

    public void logDebug(String message){
        if(Settings.debug) logger.log(message);
    }

    public void clear() {
        File file = new File(Settings.transactionLogFileName);
        file.delete();
    }
}
