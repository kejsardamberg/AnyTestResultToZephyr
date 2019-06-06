package se.kejsardamberg.any2zephyr.main.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import se.kejsardamberg.any2zephyr.main.App;
import se.kejsardamberg.any2zephyr.main.Settings;
import se.kejsardamberg.any2zephyr.zephyr.JiraConnector;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * This class holds the gateway server itself. This is the simple HTTP server
 * that hosts the REST services.
 */
public class HttpServer {

    private ResourceConfig config = new ResourceConfig();
    public Server server;

    /**
     * Starting the server to enable communication.
     */
    public void start(){
        App.log.log("Starting AnyTest2Zephyr server.");
        config.packages("se/kejsardamberg/any2zephyr/main/server");
        ServletHolder servlet = new ServletHolder(new ServletContainer(config));
        server = new Server(Settings.port);
        ServletContextHandler context = new ServletContextHandler(server, "/*");
        context.addServlet(servlet, "/*");
        try {
            server.start();
        }catch (Exception e){
            App.log.log(e.toString());
        }
        App.log.log("Parameters used for web server:" + System.lineSeparator() +
                " * Jira address used = " + Settings.jiraAddress + System.lineSeparator() +
                " * Any2Zephyr server port used = " + Settings.port + System.lineSeparator() +
                " * Jira jiraUserName name = " + Settings.jiraUserName + System.lineSeparator() +
                " * API key for Zephyr = " + Settings.zephyrAPIkey + System.lineSeparator() +
                " * API version = " + Settings.currentApiVersion + System.lineSeparator() +
                " * This server connection timeout = " + Settings.serverLifeSpanInSeconds + " seconds." + System.lineSeparator() +
                " * URL to connect = http://" + getIPAddressesOfLocalMachine() + ":" + Settings.port + "/any2zephyr");
        if(isStarted()){
            App.log.log("Any2Zephyr server started. Check status and end-point descriptions at http://" + getIPAddressesOfLocalMachine() + ":" + Settings.port + "/any2zephyr/about.");
        } else {
            App.log.log("Could not start server.");
            return;
        }
        if(!checkJiraConnection()){
            App.log.log("Could not establish connection to Jira/Zephyr. Exiting.");
            stop();
            System.exit(0);
        } else {
            App.log.log("Connection to Jira server established.");
            App.log.log("URL to connect = http://" + getIPAddressesOfLocalMachine() + ":" + Settings.port + "/any2zephyr");
            App.log.log("Server ready to serve.");
        }

    }

    /**
     * Checks if the server is started.
     *
     * @return Returns true if server is running, else false.
     */
    public boolean isStarted(){
        if(App.supressConnectionStatusChecking)return true;
        return (server != null && !server.isFailed());
    }


    /**
     * Stop the server.
     */
    public void stop(){
        try{
            server.stop();
            server.destroy();
            App.log.log("Server stopped.");
        }catch (Exception e){
            App.log.log("Error stopping HTTP server: " + e.toString());
        }
    }

    /**
     * Identifies local IP-address of the machine this server is executed on. Used to display connect help information.
     *
     * @return Returns IP address of the machine the server is executed upon.
     */
    public static String getIPAddressesOfLocalMachine(){
        String ip = "Could not identify local IP address.";
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return ip;
    }

    public static String connectionStatus(){
        if(App.supressConnectionStatusChecking) return "Suppressed due to 'config' mode";
        if(checkJiraConnection()) return "Connected";
        return "Not connected";
    }

    /**
     * Checks if a successful connection to the Testlink server can be established.
     *
     * @return Return true if a successful connection can be made with the Testlink server API, given the runtime parameters stated (username and DevKey).
     */
    private static boolean checkJiraConnection() {
        if(App.supressConnectionStatusChecking)return true;
        App.log.log("Checking connection to Jira (timeout " + Settings.serverLifeSpanInSeconds + " seconds).");
        return JiraConnector.getLoggedInUser() != null;
    }

}
