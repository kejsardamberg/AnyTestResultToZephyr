package se.kejsardamberg.any2zephyr.eventlogging;

import se.kejsardamberg.any2zephyr.main.Settings;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

    private String fileName;
    private Boolean fileWriteAccess = true;

    public Logger(String fileName){
        this.fileName = fileName;
        log("----------------------------------------------------");
        log("New log session initialized.");
        log("----------------------------------------------------");
    };

    /*
    private static class ThreadSafeSingletonHolder{
        private static final Logger logger = new Logger();
    }

    private static Logger logger(){
        return ThreadSafeSingletonHolder.logger;
    }
    */

    public void log(String message){
        message = new SimpleDateFormat(Settings.dateFormatForLogFileLogEntries).format(new Date()) + " > " + message + System.lineSeparator();
        System.out.print(message);
        if(fileWriteAccess) appendToTransactionLogFile(message);
    }

    private void appendToTransactionLogFile(String message){
        createLogFileIfItDoesNotExist();
        try {
            Files.write(Paths.get(fileName), message.getBytes(), StandardOpenOption.APPEND);
        }catch (IOException e) {
            System.out.println("Could not write to file '" + fileName + "'. No file based logging.");
            fileWriteAccess = false;
        }
    }

    private void createLogFileIfItDoesNotExist(){
        if(Files.exists(Paths.get(fileName)))return;
        File file = new File(fileName);
        try {
            System.out.println("Log file '" + fileName + "' not found. Attempting to create it.");
            file.createNewFile();
            System.out.println("Log file '" + fileName + "' created successfully.");
        } catch (IOException e) {
            fileWriteAccess = false;
            System.out.println("Cannot create file '" + fileName + "'. No file based logging.");
        }
    }

}
