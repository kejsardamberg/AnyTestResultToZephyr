package server;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Command line enabled start class for 'Any test result to Zephyr'
 * server start up a gateway listening for posted test run results from a
 * test run - and reporting the test results to Zephyr.
 *
 * Created by jordam on 2017-03-18.
 */
public class App {
    public static ObjectMapper mapper = new ObjectMapper();

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
     *
     * @param args The run time argument this jar file is started with
     */
    private static void printHelpTextIfSwitchIsFound(String[] args){
        for(String arg : args){
            if(arg.toLowerCase().equals("info") || arg.equals("-help") || arg.equals("man") || arg.equals("--help") || arg.equals("-h") || arg.equals("-?")){
                System.out.println(System.lineSeparator() + "This server is used as a proxy between any test automation and Zephyr. It produces test run results to Zephyr. " + System.lineSeparator() + System.lineSeparator() +
                        "Starting the server" + System.lineSeparator() +
                        "--------------------------------------------------" + System.lineSeparator() +
                        "A few different command line parameters can be used at startup:" + System.lineSeparator() + System.lineSeparator() +
                        "Required parameters:" + System.lineSeparator() +
                        "     jiraaddress=http://jira.mycompany.com:80/" + System.lineSeparator() +
                        "                                              Make sure the full adress to the Jira" + System.lineSeparator() +
                        "                                              is there." + System.lineSeparator() + System.lineSeparator() +
                        "     username=jirauser                        Used to interact with Jira, so" + System.lineSeparator() +
                        "                                              make sure you use a valid user name for" + System.lineSeparator() +
                        "                                              Jira." + System.lineSeparator() + System.lineSeparator() +
                        "     apikey=2a861343a3dca60b876ca5b6567568de  You can find the Zephyr API key in Zephyr" + System.lineSeparator() + System.lineSeparator() +
                        "Optional parameters that change behaviour of this server:" + System.lineSeparator() +
                        "     port=2221                                Port number is of your own choice. Make sure " + System.lineSeparator() +
                        "                                              it is not the same as the Jira server uses." + System.lineSeparator() + System.lineSeparator() +
                        "     connectionTimeout=15                     Sets the connection timeout for this server" + System.lineSeparator() +
                        "                                              connection to 15 seconds. I omitted the server" + System.lineSeparator() +
                        "                                              will shut down in ten minutes. If set to zero" + System.lineSeparator() +
                        "                                              the server will not shut down at all." + System.lineSeparator() + System.lineSeparator() +
                        "All of these run time parameters are case insensitive and the order of them are irrelevant." + System.lineSeparator() + System.lineSeparator());
                System.exit(0);
            }
        }
    }

    /**
     * Setting the address to the Jira installation.
     *
     * @param args Runtime arguments for this program.
     */
    private static void setAddress(String[] args){
        for(String arg : args){
            if(arg.contains("=")){
                if(arg.split("=")[0].toLowerCase().equals("jiraaddress")){
                    String address = arg.split("=")[1];
                    System.out.println("Setting jiraAddress to " + address + ".");
                    Settings.jiraAddress = address;
                }
            }
        }
    }

    private static void setApiKey(String[] args){
        for(String arg : args){
            if(arg.contains("=")){
                if(arg.split("=")[0].toLowerCase().equals("apikey")){
                    String apiKey = arg.split("=")[1];
                    System.out.println("Setting Zephyr API key to " + apiKey + ".");
                    Settings.zephyrAPIkey = apiKey;
                }
            }
        }
    }

    /**
     *  Sets the user name used Jira. A username is used in
     *  combination with a API key.
     *
     * @param args The runtime arguments for this program
     */
    private static void setUserName(String[] args){
        for(String arg : args){
            if(arg.contains("=")){
                if(arg.split("=")[0].toLowerCase().equals("username")){
                    String testlinkUserName= arg.split("=")[1];
                    System.out.println("Setting Jira user name to " + testlinkUserName + ".");
                    Settings.jiraUserName = testlinkUserName;
                }
            }
        }
    }

    private static void setPortIfStatedAsParameter(String[] args){
        for(String arg : args){
            if(arg.contains("=")){
                if(arg.split("=")[0].toLowerCase().equals("port")){
                    Integer port = Integer.valueOf(arg.split("=")[1]);
                    System.out.println("Setting server port to " + port + ".");
                    Settings.port = port;
                }
            }
        }
    }


    /**
     * Testlink connection attempts has a timeout for establishing a successful connection.
     * This method sets the timeout from runtime parameters.
     *
     * @param args The runtime arguments of this program
     */
    private static void setServerLifeSpanIfStatedAsAParameter(String[] args){
        for(String arg: args){
            if(arg.contains("=")){
                if(arg.split("=")[0].toLowerCase().equals("connectiontimeout") && arg.split("=").length > 1 && arg.split("=")[1].trim().length() > 0){
                    System.out.println("Setting server life span to " + arg.split("=")[1] + " seconds.");
                    Settings.serverLifeSpanInSeconds = arg.split("=")[1];
                }
            }
        }
    }


    /**
     * This is the main program executor
     *
     * @param args Runtime arguments
     */
    public static void main(String[] args){
        //originalOutputChannel = System.out;
        if(args.length == 0){
            System.out.println(helpText());
            System.exit(0);
        }
        printHelpTextIfSwitchIsFound(args);
        System.out.println("Processing " + args.length + " runtime arguments.");
        setAddress(args);
        setApiKey(args);
        setUserName(args);
        setPortIfStatedAsParameter(args);
        setServerLifeSpanIfStatedAsAParameter(args);
        if(Settings.zephyrAPIkey == null || Settings.jiraAddress == null || Settings.jiraUserName == null){
            System.out.println("Cannot start as server. Required parameter missing.");
            if(args.length == 2){
                System.out.println("Assuming command line interface utility usage.");
                System.out.println("Jira project: " + args[0]);
                System.out.println("Jira issue key: " + args[1]);
                System.out.println("Zephyr test status: " + args[2]);
            } else {
                System.out.println("Arguments found: '" + String.join("', '", args) + "'");
                System.out.println("Arguments needed for non-server utilization: Jira project, Jira issue key, test status (pass/fail)");
            }
            //This is where command line single use is stated
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

        try {
            server.server.join();
        } catch (InterruptedException e) {
            System.out.println(e.toString());
        } finally {
            server.stop();
        }
    }
}
