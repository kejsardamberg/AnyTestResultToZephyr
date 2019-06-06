package webpages;

import server.HttpServer;
import server.Settings;

/**
 * The about page displaying information about this server
 */
public class AboutPage {

    /**
     * The page HTML generator.
     *
     * @return Returns the HTML code for the page.
     */
    public static String toHtml(){
        return InfoPage.toHtml("<h1>Any test result to Zephyr server</h1>" +
                "<p>This server helps propagate results from tests performed in any test automation to Zephyr for Jira.</p>" +
                "<h2>Run status</h2>" +
                "<table>" +
                "<tr><td>Address to API:</td><td>" + Settings.jiraAddress + "</td></tr>" +
                "<tr><td>Listening for incoming TAF test run data on port:</td><td>" + Settings.port + "</td></tr>" +
                "<tr><td>Current IP address of this TAF Testlink Adapter Server:</td><td>" + HttpServer.getIPAddressesOfLocalMachine() + "</td></tr>" +
                "</table>" +
                "<p><a href=\"taftestlinkadapter/version\">Software version " + Settings.serverVersion+ "</a>" +
                "</p><p><a href=\"taftestlinkadapter/apiversion\">Implemented API version</a></p>" +
                "</p>" +
                "<p><h2>Current available API End-points</h2>" +
                "<table border=\"1\">" +
                "<tr><th>End-point</th><th>Method</th><th>Media-Type</th><th>Description</th></tr>" + System.lineSeparator() +
                endpointTableRow("version", "GET", "text/plain", "Gives the current server version in plain text.") +
                endpointTableRow("version", "GET", "text/html", "Gives the current server version in HTML.") +
                endpointTableRow("version", "GET", "application/json", "Gives the current server version in JSON.") +
                endpointTableRow("about", "GET", "text/html", "Help and description page.") +
                endpointTableRow("", "GET", "text/html", "Help and description page.") +
                endpointTableRow("kill", "GET", "text/plain", "Shuts down the server.") +
                endpointTableRow("kill", "GET", "text/html", "Shuts down the server.") +
                endpointTableRow("logo", "GET", "image/png", "Gets the logo.") +
                endpointTableRow("report", "POST", "text/plain", "Posts test results to Zephyr. JSON body exemple below.") +
                "</table>" +
                "<p>JSON body example for test result report posting:</p><p>" + System.lineSeparator() +
                "{<br>   \"project\": \"MyJiraProject\",<br>   \"jirakey\": \"ABC-12456\",<br>   \"status\": \"pass\"<br>}</p>");
    }

    private static String endpointTableRow(String endpoint, String method, String mediatype, String description){
        return "<tr><td><a href=\"http://" + Settings.localIPAddress + ":" + Settings.port + "/any2zephyr/" + endpoint + "\">" +
                "http://" + Settings.localIPAddress + ":" + Settings.port + "/any2zephyr/" + endpoint + "</a></td>" +
                "<td>" + method + "</td>" +
                "<td>" + mediatype + "</td>" +
                "<td>" + description + "</td></tr>" + System.lineSeparator();
    }
}
