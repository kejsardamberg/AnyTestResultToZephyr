package server;

/**
 * Static test run settings with default values for important runtime parameters.
 */
public class Settings {

    /**
     * Current Testlink Adapter Server version
     */
    public static String serverVersion = "1.0";

    /**
     * The API version need to be updated when relevant structural changes to TAF occurs.
     */
    public static String currentApiVersion = "v1";

    /**
     * The IP port where this server sets up it endpoint at.
     */
    public static int port = 2221;

    /**
     * The address to the Testlink API endpoint
     */
    public static String jiraAddress = "http://127.0.0.1:80";

    /**
     * The Testlink user account name used for communication with Testlink
     */
    public static String jiraUserName = null;

    /**
     * The Testlink developer key generated for a user in the Testlink GUI (User Account Page).
     */
    public static String zephyrAPIkey = null;

    /**
     * Any connection to Testlink has a timeout for achieving a successful connection.
     * The parameter below sets the timeout, stated in seconds.
     */
    public static String serverLifeSpanInSeconds = "10";

    public static String localIPAddress = HttpServer.getIPAddressesOfLocalMachine();

}
