package se.kejsardamberg.any2zephyr.main;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import se.kejsardamberg.any2zephyr.main.server.HttpServer;

import java.io.File;
import java.io.IOException;

/**
 * Static test run settings with default values for important runtime parameters.
 */
public class Settings {

    /**
     * For Jira interaction
     */
    public static String jiraAddress;
    public static String jiraUserName;
    public static String jiraPassword;

    /**
     * The IP port where this server sets up it endpoint at.
     */
    public static int port = 2221;

    /**
     * Current Testlink Adapter Server version
     */
    public static String serverVersion = "1.0";

    /**
     * The API version need to be updated when relevant structural changes to TAF occurs.
     */
    public static String currentApiVersion = "v1";


    /**
     * The zephyr developer key generated for a jiraUserName in the Testlink GUI (User Account Page).
     */
    public static String zephyrAPIkey = null;

    /**
     * Log file name for transaction log.
     */
    public static String transactionLogFileName = "transactionLog.log";

    public static String testSummaryForCreatedTests = "Automated regression";

    public static boolean debug = false;

    /**
     * Log file time stamp format.
     */
    public static String dateFormatForLogFileLogEntries = "yyyy-MM-dd HH:mm:ss";

    public static String serverLifeSpanInSeconds = null;
    public static Integer connectionTimeout = 60;
    public static String localIPAddress = HttpServer.getIPAddressesOfLocalMachine();

    private static boolean settingsFileExist(){
        File file = new File("any2zephyr.properties");
        return file.exists();
    }

    public static void attemptToReadFromFile(){
        if(!settingsFileExist())return;
        ObjectMapper mapper = new ObjectMapper();
        JiraSettings jiraSettings = null;
        try {
            jiraSettings = mapper.readValue(new File("any2zephyr.properties"), JiraSettings.class);
        } catch (IOException ignored) {
        }
        if(jiraSettings == null)return;
        jiraAddress = jiraSettings.jiraBaseUrl;
        jiraUserName = jiraSettings.jiraUserName;
        jiraPassword = jiraSettings.jiraUserPassword;
    }

    public static void attemptToSaveSettingsToFile(){
        File file = new File("any2zephyr.properties");
        if(settingsFileExist()){
            file.delete();
        }
        ObjectMapper mapper = new ObjectMapper();
        JiraSettings jiraSettings = new JiraSettings();
        jiraSettings.jiraBaseUrl = jiraAddress;
        jiraSettings.jiraUserName = jiraUserName;
        jiraSettings.jiraUserPassword = jiraPassword;
        try {
            mapper.writeValue(file, jiraSettings);
        } catch (IOException ignored) {
        }

    }

    public static class JiraSettings{
        @JsonProperty public String jiraBaseUrl;
        @JsonProperty public String jiraUserName;
        @JsonProperty public String jiraUserPassword;

        public JiraSettings(){}
    }

}
