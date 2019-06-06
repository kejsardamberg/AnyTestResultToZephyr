package se.kejsardamberg.any2zephyr.main;

import se.kejsardamberg.any2zephyr.eventlogging.TransactionLog;
import se.kejsardamberg.any2zephyr.zephyr.TestResult;
import se.kejsardamberg.any2zephyr.main.server.HttpServer;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Command line enabled start class for 'Any test result to Zephyr'
 * server start up a gateway listening for posted test run results from a
 * test run - and reporting the test results to Zephyr.
 *
 * Created by jordam on 2017-03-18.
 */
public class App {
    public static TransactionLog log = new TransactionLog();
    private static boolean serverMode = false;
    public static boolean supressConnectionStatusChecking = false;
    private static ProgramArgumentManager pam;

    /**
     * This is the main program executor
     *
     * @param args Runtime arguments
     */
    public static void main(String[] args){
        pam = new ProgramArgumentManager(args);
        if(args.length == 0){
            System.out.println(helpText());
            System.exit(0);
        }
        printHelpTextIfSwitchIsFound();
        log.log("Processing " + pam.numberOfArguments() + " runtime arguments.");
        Settings.attemptToReadFromFile();
        applyConfigIfStated();

        setAddress();
        setUserName();
        setPassword();
        setMode();
        setPortIfStatedAsParameter();
        setConnectionTimeout();
        setServerLifeSpanIfStatedAsAParameter();
        saveSettingsIfWanted();
        if(!serverMode){
            cliUsage();
            return;
        }
        HttpServer server = new HttpServer();
        server.start();
        if(!server.isStarted()) {
            try {
                server.server.stop();
            } catch (Exception ignored) {
            }
            server.server.destroy();
            return;
        }
        if(Settings.serverLifeSpanInSeconds != null && Integer.valueOf(Settings.serverLifeSpanInSeconds) > 0){
            ServerKiller serverKiller = new ServerKiller(server);
            Thread killer = new Thread(serverKiller);
            killer.start();
        }
        if(supressConnectionStatusChecking && Desktop.isDesktopSupported()){//Config - try to open browser
            try {
                Desktop.getDesktop().browse(new URI("http://" + Settings.localIPAddress + ":" + Settings.port + "/any2zephyr"));
            } catch (IOException e) {
                log.log("Direct your web browser to http://" + Settings.localIPAddress + ":" + Settings.port + "/any2zephyr to configure the server, or check the help options. Error: " + e.getMessage());
            } catch (URISyntaxException e) {
                log.log("Direct your web browser to http://" + Settings.localIPAddress + ":" + Settings.port + "/any2zephyr to configure the server, or check the help options. Error: " + e.getMessage());
            }
        }
        try {
            server.server.join();
        } catch (InterruptedException e) {
            log.log(e.toString());
        } finally {
            server.stop();
        }
    }

    static class ServerKiller implements Runnable{

        HttpServer server;
        Integer lifetimeInMilliseconds;
        public ServerKiller(HttpServer server){
            lifetimeInMilliseconds = Integer.valueOf(Settings.serverLifeSpanInSeconds) * 1000;
            this.server = server;
        }
        @Override
        public void run() {
            log.log("Registering server shutdown in " + lifetimeInMilliseconds/1000 + " seconds.");
            try {
                Thread.sleep(lifetimeInMilliseconds);
            } catch (InterruptedException e) {
                log.log("Stopping server due to lifespan parameter used (at " + lifetimeInMilliseconds/1000 + " seconds).");
                server.stop();
            }
            server.stop();
        }
    }

    /**
     * Help text for command line.
     *
     * @return The text
     */
    private static String helpText(){
        return System.lineSeparator() +
                "Any test result to Zephyr server" + System.lineSeparator() +
                "===============================" + System.lineSeparator() +
                "This utility could both be used as s microservice or as a command line utility. Usage as a web server:" +
                "Usage example for server:" + System.lineSeparator() + System.lineSeparator() +
                "   java -jar any2zephyr.jar jiraaddress=http://jira.mycompany.com:80 username=taftestlinkuser password=secret [ port=2221 ][ timeout=120 ]" + System.lineSeparator() + System.lineSeparator() +
                "where port number is the AnyTest2Zephyr web server port to use." + System.lineSeparator() +
                "Default TCP port is 2221 - chosen not to collide with known services. The important part is that it should not be a port in use already." + System.lineSeparator() +
                System.lineSeparator() +
                "Usage example as a CLI utility:" + System.lineSeparator() +
                "   java -jar any2zephyr.jar MyJiraProjectName MyIssueKey PASSED|FAILED" + System.lineSeparator() + System.lineSeparator() +
                "If you want to understand how AnyTest2Zephyr server works, and should be applied, and what other switches exist, use the switch:" + System.lineSeparator() + System.lineSeparator() +
                "   java -jar any2zephyr.jar -help" + System.lineSeparator();
    }

    /**
     * Printing information text describing the workings of this program if the argument 'info' is found.
     */
    private static void printHelpTextIfSwitchIsFound(){
        if(pam.hasAnyOfTheArguments("info", "help", "man", "h", "?")){
            pam.use("info", "man", "help", "h", "?");
            System.out.println(System.lineSeparator() + "This server is used as a proxy between any test automation and Zephyr. It produces test run results to Zephyr. " + System.lineSeparator() + System.lineSeparator() +
                    "The easiest way of learning the use of it and setting it up might be to use it as:" + System.lineSeparator() + System.lineSeparator() +
                    "   java -jar any2zephyr.jar config" + System.lineSeparator() + System.lineSeparator() +
                    "This will start a local web server and the address to it will be printed so you can use your browser to reach the configuration pages of it." + System.lineSeparator() + System.lineSeparator() +
                    "COMMAND LINE PARAMETERS" + System.lineSeparator() +
                    "=======================" + System.lineSeparator() + System.lineSeparator() +
                    "Jira connection parameters" + System.lineSeparator() +
                    "--------------------------" + System.lineSeparator() +
                    "A few different command line parameters can be used at startup:" + System.lineSeparator() + System.lineSeparator() +
                    "Required parameters (unless saved to properties file):" + System.lineSeparator() +
                    "     jiraaddress=http://jira.mycompany.com:80/" + System.lineSeparator() +
                    "                                              Make sure the full adress to the Jira" + System.lineSeparator() +
                    "                                              is there." + System.lineSeparator() + System.lineSeparator() +
                    "     jirauser=jirausername                    Used to interact with Jira, so" + System.lineSeparator() +
                    "                                              make sure you use a valid jiraUserName name for" + System.lineSeparator() +
                    "                                              Jira." + System.lineSeparator() + System.lineSeparator() +
                    "     jirapassword=yourpassword                Used to set the password to interact with Jira." + System.lineSeparator() + System.lineSeparator() +
                    "Optional parameters:" + System.lineSeparator() +
                    "     timeout=45                               Sets the Jira server connection timeout." + System.lineSeparator() +
                    "                                              Default is " + Settings.connectionTimeout + " seconds." + System.lineSeparator() + System.lineSeparator() +
                    "     savesettings                             Saves Jira URL, user name, and password to a JSON formatted" + System.lineSeparator() +
                    "                                              file called 'any2zephyr.properties' in the program folder." + System.lineSeparator() + System.lineSeparator() +
                    "Server parameters (not using the CLI for registering single test result)" + System.lineSeparator() +
                    "------------------------------------------------------------------------" + System.lineSeparator() +
                    "Optional parameters that change behaviour of this server:" + System.lineSeparator() +
                    "     mode=server|cli                          Set mode to server if you want to start" + System.lineSeparator() +
                    "                                              the REST server. Default is CLI usage." + System.lineSeparator() + System.lineSeparator() +
                    "     config                                   Supresses Jira connection checking, for testing or configuration." + System.lineSeparator() + System.lineSeparator() +
                    "     port=2221                                Port number is of your own choice for " + System.lineSeparator() +
                    "                                              activated. Make sure " + System.lineSeparator() +
                    "                                              the REST server, if server mode is" + System.lineSeparator() +
                    "                                              it is not the same as the Jira server uses." + System.lineSeparator() + System.lineSeparator() +
                    "     lifespan=600                             Makes the server automatically shut down after" + System.lineSeparator() +
                    "                                              a set number of seconds. Default it never" + System.lineSeparator() +
                    "                                              shuts down (=0)." + System.lineSeparator() + System.lineSeparator() +
                    "Test case result registration from command line" + System.lineSeparator() +
                    "-----------------------------------------------" + System.lineSeparator() +
                    "     project=JiraProjectName                  Sets the Jira project name for search scope for issues." + System.lineSeparator() + System.lineSeparator() +
                    "     issue=JRA-12345                          Identifies what Jira issue (by its key) to" + System.lineSeparator() +
                    "                                              write test results to. If the issue is a" + System.lineSeparator() +
                    "                                              Zephyr test the results will be written as" + System.lineSeparator() +
                    "                                              a new Execution to it, othervise a test called" + System.lineSeparator() +
                    "                                              '" + Settings.testSummaryForCreatedTests + "' will be created" + System.lineSeparator() +
                    "                                              and linked to the issue." + System.lineSeparator() + System.lineSeparator() +
                    "     status=pass|fail                         States if the test result is passed or failed." + System.lineSeparator() + System.lineSeparator() +
                    "     comment='This went well'                 Comment added to the test execution in Zephyr." + System.lineSeparator() +
                    "                                              If stated last in the command line all trailing" + System.lineSeparator() +
                    "                                              words will be included in the comment." + System.lineSeparator() + System.lineSeparator() +
                    "Other useful parameters" + System.lineSeparator() +
                    "-----------------------" + System.lineSeparator() +
                    "     debug                                    Achieves more verbose output." + System.lineSeparator() + System.lineSeparator() +
                    "                                              to the given value interpreted as seconds." + System.lineSeparator() + System.lineSeparator() +
                    "All of these run time parameters are case insensitive and the order of them are irrelevant." + System.lineSeparator() + System.lineSeparator()+
                    "If the server mode is activated an output of interfaces will be printed. A HTML help page" + System.lineSeparator() +
                    "is available from running server. The link to this page is displayed when the server is starting up." + System.lineSeparator());
            System.exit(0);
        }
    }

    private static void setConnectionTimeout(){
        if(!pam.hasArgument("timeout"))return;
        String timeoutString = pam.use("timeout");
        if(timeoutString == null || timeoutString.length() == 0 || Integer.valueOf(timeoutString) == null) {
            log.log("Timeout parameter cannot be interpreted. Sticking with default " + Settings.connectionTimeout + " seconds.");
            return;
        }
        Settings.connectionTimeout =  Integer.valueOf(timeoutString);
        log.log("Setting Jira connection timeout to " + Settings.connectionTimeout + " seconds.");
    }

    /**
     * Setting the mode of this server.
     */
    private static void setMode(){
        if(!pam.hasArgument("mode"))return;
        String mode = pam.use("mode");
        if(mode.toLowerCase().equals("server")){
            serverMode = true;
            log.log("Setting mode to " + mode + ".");
        } else {
            serverMode = false;
            log.logDebug("Mode=" + mode + ". Not identified as server mode.");
        }
    }

    /**
     * Setting the address to the Jira installation.
     */
    private static void applyConfigIfStated(){
        if(pam.hasArgument("config")){
            pam.use("config");
            serverMode = true;
            supressConnectionStatusChecking = true;
            log.log("Supressing Jira connection status checking due to command line switch 'config' found.");
        }
    }


    /**
     * Setting the address to the Jira installation.
     */
    private static void setAddress(){
        if(pam.hasArgument("jiraaddress")){
            log.log("Setting jiraAddress to " + pam.getValue("jiraaddress") + ".");
            Settings.jiraAddress = pam.use("jiraaddress");
        }
    }

    /**
     *  Sets the jiraUserName name used for Jira interaction.
    */
    private static void setUserName(){
        if(pam.hasArgument("jirauser")){
            log.log("Setting Jira jiraUserName name to " + pam.getValue("jirauser") + ".");
            Settings.jiraUserName = pam.use("jirauser");
        }
    }

    private static void setPortIfStatedAsParameter(){
        if(pam.hasArgument("port")){
            Integer port = Integer.valueOf(pam.use("port"));
            log.log("Setting server port to " + port + ".");
            Settings.port = port;
        }
    }


    /**
     * Testlink connection attempts has a timeout for establishing a successful connection.
     * This method sets the timeout from runtime parameters.
     */
    private static void setServerLifeSpanIfStatedAsAParameter(){
        if(!pam.hasArgument("lifespan"))return;
        log.log("Setting server life span to " + pam.getValue("lifespan") + " seconds.");
        Settings.serverLifeSpanInSeconds = pam.use("lifespan");
    }

    private static void cliUsage(){
        log.log("Assuming command line interface utility usage.");
        String project = pam.use("project");
        String issue = pam.use("issue");
        String status = pam.use("status");
        TestResult testResult = new TestResult();
        if(project == null || issue == null || status == null){
            log.log("Need arguments for project, issue, and status.");
            return;
        }
        testResult.project = project;
        testResult.issue = issue;
        if(status.toLowerCase().equals("pass")) {
            testResult.status = TestResult.TestStatus.PASSED;
        } else if(status.toLowerCase().equals("failed")) {
            testResult.status = TestResult.TestStatus.FAILED;
        } else {
            testResult.status = TestResult.TestStatus.valueOf(status.toLowerCase());
        }
        log.log("Registering new test result from CLI usage: " + testResult.toString());
        testResult.report();
    }

    private static void setPassword() {
        if(!pam.hasArgument("jirapassword")) return;
        StringBuilder sb = new StringBuilder();
        for(Byte letter : pam.getValue("jirapassword").getBytes()){
            sb.append("*");
        }
        log.log("Setting Jira password to '" + sb.toString() + "'.");
        Settings.jiraPassword = pam.use("jirapassword");
    }

    private static void saveSettingsIfWanted() {
        if(!pam.hasArgument("savesettings"))return;
        pam.use("savesettings");
        Settings.attemptToSaveSettingsToFile();
        log.log("Saving current settings to 'any2zephyr.properties' file.");
    }
}
