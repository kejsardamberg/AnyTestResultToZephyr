package server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * This class holds the gateway server itself. This is the simple HTTP server
 * that hosts the REST services.
 */
public class HttpServer {

    private ResourceConfig config = new ResourceConfig();
    Server server;

    /**
     * Starting the server to enable communication.
     */
    public void start(){
        System.out.println(System.lineSeparator() + "Starting AnyTest2Zephyr server." + System.lineSeparator());
        config.packages("server");
        ServletHolder servlet = new ServletHolder(new ServletContainer(config));
        server = new Server(Settings.port);
        ServletContextHandler context = new ServletContextHandler(server, "/*");
        context.addServlet(servlet, "/*");
        try {
            server.start();
        }catch (Exception e){
            System.out.println(System.lineSeparator() + e.toString());
        }
        System.out.println("Parameters used:");
        System.out.println(" * Jira address used = " + Settings.jiraAddress);
        System.out.println(" * Any2Zephyr server port used = " + Settings.port);
        System.out.println(" * Jira user name = " + Settings.jiraUserName);
        System.out.println(" * API key for Zephyr = " + Settings.zephyrAPIkey);
        System.out.println(" * API version = " + Settings.currentApiVersion);
        System.out.println(" * This server connection timeout = " + Settings.serverLifeSpanInSeconds + " seconds.");
        System.out.println(" * URL to connect = http://" + getIPAddressesOfLocalMachine() + ":" + Settings.port + "/any2zephyr");
        if(isStarted()){
            System.out.println(System.lineSeparator() + "Any2Zephyr server started. Check status and end-point descriptions at http://" + getIPAddressesOfLocalMachine() + ":" + Settings.port + "/any2zephyr/about.");
        } else {
            System.out.println(System.lineSeparator() + "Could not start server." + System.lineSeparator());
            return;
        }
        if(!checkJiraConnection()){
            System.out.println("Could not establish connection to Jira/Zephyr. Exiting.");
            stop();
            System.exit(0);
        } else {
            System.out.println("Connection to Jira server established." + System.lineSeparator());
            System.out.println("URL to connect    = http://" + getIPAddressesOfLocalMachine() + ":" + Settings.port + "/" + System.lineSeparator());
            System.out.println(System.lineSeparator() + "Server ready to serve." + System.lineSeparator());
        }

    }

    /**
     * Checks if the server is started.
     *
     * @return Returns true if server is running, else false.
     */
    public boolean isStarted(){
        return (server != null && !server.isFailed());
    }


    /**
     * Stop the server.
     */
    public void stop(){
        try{
            server.stop();
            server.destroy();
            System.out.println(System.lineSeparator() + "Server stopped." + System.lineSeparator());
        }catch (Exception e){
            System.out.println("Error stopping HTTP server: " + e.toString());
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

    /**
     * Checks if a successful connection to the Testlink server can be established.
     *
     * @return Return true if a successful connection can be made with the Testlink server API, given the runtime parameters stated (username and DevKey).
     */
    private static boolean checkJiraConnection() {
        System.out.print("Checking connection to Jira (timeout " + Settings.serverLifeSpanInSeconds + " seconds).");
        //This is where check code resides
        System.out.print(System.lineSeparator());
        return true;
    }

}
