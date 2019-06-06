package se.kejsardamberg.any2zephyr.main.server.webpages;

import se.kejsardamberg.any2zephyr.main.server.HttpServer;
import se.kejsardamberg.any2zephyr.main.Settings;

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
                "<h2>Requirements</h2>" +
                "<ol>" +
                "<li>Java JRE (version >= 1.8)</li>" +
                "<li>Jira installation with Zephyr and ZAPI</li>" +
                "<li>An active Jira user account</li>" +
                "<li>Network access to the Jira installation from where this program is executed</li>" +
                "</ol>" +
                "<h2>Basic usage</h2>" +
                "<p>The tool report results to Zephyr from any test tool by giving a Jira project, a Jira issue to report results to, and the status of the test (passed or failed). " +
                "If the issue stated for reporting is another issue type than Zephyr test test results are reported to a Zephyr test called 'Automated regression.' that is a referenced issue to the given issue. " +
                "Each test result reporting creates a new test execution.</p>" +
                "<p><i>java -jar any2zephyr.jar -help</i></p>" +
                "<h3>Execution modes</h3>" +
                "<p>There are two basic modes of this tool. For single test case reporting the command line interface will do fine. For many test result reportings the program and JVM initialization takes a lot of time, hence a REST service has been produced.</p>" +
                "<h4>Command line interface for single test case reporting</h4>" +
                "<p>The CLI interface is used by using the syntax below, where valid statuses are pass, fail, passed, and failed. Status values are not sensitive to case.</p>" +
                "<p><i>java -jar any2zephyr.jar jiraaddress=http://jira.myorganization.org:80/ jirauser=myjirausername jirapassword=myjirapassword project=MyJiraProject issue=TheIssueKey status=pass</i></p>" +
                "<h4>Saving settings</h4>" +
                "<p>For ease of use the Jira address, user name, and password can be saved as a JSON object in the same folder as the any2zephyr.jar resides in. One way of doing this is using the switch 'savesettings'. This produces the file. E.g;</p>" +
                "<p><i>java -jar any2zephyr.jar jiraaddress=http://jira.myorganization.org:80/ jirauser=myjirausername jirapassword=myjirapassword savesettings</i></p>" +
                "<p>Another way of producing this file is from the 'Save settings'-button in the web GUI when this utility is ran in the server mode. A sample file content can be seen on the row below:</p>" +
                "<p><i>{\"jiraBaseUrl\":\"https://jira.company.org:8080\",\"jiraUserName\":\"admin\",\"jiraUserPassword\":\"SoooooSecret123\"}</i></p>" +
                "<h4>Server mode</h4>" +
                "<p>The server mode is entered giving the switch 'mode=server'. This starts a small web server that hosts a few end-points. Of course Jira connection information must be provided, either at the command line, like below, or through the properties file as stated above.</p>" +
                "<p><i>java -jar any2zephyr.jar jiraaddress=http://jira.myorganization.org:80/ jirauser=myjirausername jirapassword=myjirapassword mode=server</i></p>" +
                "<p>A few small web pages can be displayed for this server. They root of these can be found at http://localhost:80/any2zephyr/. This page also contains a few admin actions and endpoint descriptions for the REST services provided.</p>" +
                "<h2>Server administration</h2>" +
                "<form action=\"/any2zephyr/savesettings\"><input type=\"submit\" value=\"Save Jira parameters to properties file for re-use\" /></form>" +
                "<form action=\"/any2zephyr/clearlog\"><input type=\"submit\" value=\"Clear transaction log\" /></form>" +
                "<form action=\"/any2zephyr/form\"><input type=\"submit\" value=\"Manual entry form...\" /></form>" +
                "<form action=\"/any2zephyr/kill\"><input type=\"submit\" value=\"Shut down server\" /></form>" +
                "<h3>Run status</h3>" +
                "<form action=\"/any2zephyr/tryconnection\" method=\"post\" id=\"form1\">" +
                "<table>" +
                "<tr><td>Address to Jira:</td><td><input type=\"text\" id=\"jiraaddress\" name=\"jiraurl\" value=\"" + Settings.jiraAddress + "\"></td></tr>" +
                "<tr><td>IP address of server:</td><td>" + HttpServer.getIPAddressesOfLocalMachine() + "</td></tr>" +
                "<tr><td>Incoming requests port:</td><td>" + Settings.port + "</td></tr>" +
                "<tr><td><a href=\"taftestlinkadapter/version\">Software version:</a></td><td>" + Settings.serverVersion+ "</td></tr>" +
                "<tr><td><a href=\"taftestlinkadapter/apiversion\">Implemented API version:</a></td><td>" + Settings.currentApiVersion + "</td></tr>" +
                "<tr><td>Jira user name:</td><td><input type=\"text\" value=\"" + Settings.jiraUserName + "\" name=\"jirauser\" id=\"jirauser\"></td></tr>" +
                "<tr><td>Jira user password:</td><td><input type=\"text\" value=\"******\" name=\"jirapassword\" id=\"jirapassword\"></td></tr>" +
                "<tr><td>Jira connection status:</td><td id=\"connectionStatus\">" + HttpServer.connectionStatus() + "</td></tr>" +
                "</table>" +
                "</form>" +
                "<button type=\"button\" form=\"form1\" value=\"Submit\" onclick=\"postResult('any2zephyr/tryconnection')\">Try connection parameters</button>" +
                "<button type=\"button\" form=\"form1\" value=\"Submit\" onclick=\"postResult('any2zephyr/savesettings')\">Save parameters as default</button>" +
                "<p><span>Status:</span><span id=\"savestatus\">Ok</span></p>" +
                "<h3>Current available API End-points</h3>" +
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
                endpointTableRow("jiraprojects", "GET", "application/json", "Retrieves the Jira projects.") +                endpointTableRow("form", "GET", "text/html", "Produces a form for manually entering test results.") +
                endpointTableRow("savesettings", "GET", "text/html", "Saves Jira settings (User, password, URL) for re-use.") +
                endpointTableRow("clearlog", "GET", "text/html", "Clears the transaction log.") +
                endpointTableRow("tryconnection", "POST", "text/plain", "Returns connection status for current Jira connection.") +
                endpointTableRow("report", "POST", "text/plain", "Posts test results to Zephyr. JSON body exemple below.") +
                "</table>" +
                "<p>JSON body example for test result report posting:</p><p>" + System.lineSeparator() +
                "{<br>   \"project\": \"MyJiraProject\",<br>   \"jirakey\": \"ABC-12456\",<br>   \"status\": \"pass\"<br>}</p>", javascript());
    }

    private static String endpointTableRow(String endpoint, String method, String mediatype, String description){
        return "<tr><td><a href=\"http://" + Settings.localIPAddress + ":" + Settings.port + "/any2zephyr/" + endpoint + "\">" +
                "http://" + Settings.localIPAddress + ":" + Settings.port + "/any2zephyr/" + endpoint + "</a></td>" +
                "<td>" + method + "</td>" +
                "<td>" + mediatype + "</td>" +
                "<td>" + description + "</td></tr>" + System.lineSeparator();
    }

    private static String javascript(){
        StringBuilder sb = new StringBuilder();
        sb.append("      <script>").append(System.lineSeparator());
        sb.append("         function postResult(endpoint){").append(System.lineSeparator());
        sb.append("             var jiraaddress = document.getElementById(\"jiraaddress\").value;").append(System.lineSeparator());
        sb.append("             var jiraUserName = document.getElementById(\"jirauser\").value;").append(System.lineSeparator());
        sb.append("             var jiraUserPassword = document.getElementById(\"jirapassword\").value;").append(System.lineSeparator());
        sb.append("             var xhttp = new XMLHttpRequest();").append(System.lineSeparator());
        sb.append("             xhttp.onreadystatechange = function() {").append(System.lineSeparator());
        sb.append("                if (xhttp.readyState == XMLHttpRequest.DONE) {").append(System.lineSeparator());
        sb.append("                   document.getElementById(\"savestatus\").innerHTML = xhttp.responseText;").append(System.lineSeparator());
        sb.append("                }").append(System.lineSeparator());
        sb.append("             }").append(System.lineSeparator());
        sb.append("             xhttp.open(\"POST\", endpoint, true);");
        sb.append("             xhttp.setRequestHeader(\"Content-type\", \"application/x-www-form-urlencoded\");").append(System.lineSeparator());
        sb.append("             xhttp.send('{\"jiraBaseUrl\": \"' + jiraaddress + '\", \"jiraUserName\": \"' + jiraUserName + '\", \"jiraUserPassword\": \"' + jiraUserPassword + '\" }');").append(System.lineSeparator());
        sb.append("         }").append(System.lineSeparator());
        sb.append("      </script>").append(System.lineSeparator());
        return sb.toString();
    }
}
