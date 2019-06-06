package se.kejsardamberg.any2zephyr.main.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import se.kejsardamberg.any2zephyr.main.App;
import se.kejsardamberg.any2zephyr.main.Settings;
import se.kejsardamberg.any2zephyr.main.server.webpages.AboutPage;
import se.kejsardamberg.any2zephyr.main.server.webpages.InfoPage;
import se.kejsardamberg.any2zephyr.main.server.webpages.ResultReportForm;
import se.kejsardamberg.any2zephyr.zephyr.JiraConnector;
import se.kejsardamberg.any2zephyr.zephyr.JiraSession;
import se.kejsardamberg.any2zephyr.zephyr.TestResult;

import javax.imageio.ImageIO;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * The different end-points of the HTTP server
 *
 * Created by jordam on 2017-03-18.
 */
@Path("any2zephyr")
public class Resource {

    /**
     * Returns version of this server.
     *
     * @return Returns version of this server.
     */
    @GET
    @Path("version")
    @Produces(MediaType.TEXT_HTML)
    public String versionHtml() {
        App.log.log("Got a request for version.");
        return InfoPage.toHtml("<p>Server version: " + Settings.serverVersion + ".</p>");
    }

    /**
     * Returns version of this server.
     *
     * @return Returns version of this server.
     */
    @SuppressWarnings("SameReturnValue")
    @GET
    @Path("version")
    @Produces(MediaType.TEXT_PLAIN)
    public String versionPlainText() {
        App.log.log("Got a request for version.");
        return Settings.serverVersion;
    }

    /**
     * Returns version of this server.
     *
     * @return Returns version of this server.
     */
    @SuppressWarnings("SameReturnValue")
    @GET
    @Path("version")
    @Produces(MediaType.APPLICATION_JSON)
    public String versionJson() {
        App.log.log("Got a request for version.");
        return "{ \"version\": \"" + Settings.serverVersion + "\" }";
    }

    /**
     * Landing page for general identification of this server.
     *
     * @return Return general identification information
     */
    @GET
    @Path("about")
    @Produces(MediaType.TEXT_HTML)
    public String about() {
        App.log.log("Received a request for the about page.");
        return AboutPage.toHtml();
    }

    /**
     * Landing page is simple redirect to About Page.
     *
     * @return Returns the about page.
     */
    @GET
    @Path("")
    @Produces(MediaType.TEXT_HTML)
    public String landingPage() {
        App.log.log("Received a request to web root. Returning the about page.");
        return AboutPage.toHtml();
    }

    /**
     * Shuts down the service.
     *
     * @return Returns a shutdown message
     */
    @GET
    @Path("kill")
    @Produces(MediaType.TEXT_PLAIN)
    public String shutDownServer(){
        App.log.log("Shutting down server due to kill request to API.");
        ShutdownRegistration shutdownRegistration = new ShutdownRegistration(2);
        return "AnyTest2Zephyr server shutdown in two seconds.";
    }

    /**
     * Shuts down the service.
     *
     * @return Returns a shutdown message
     */
    @GET
    @Path("kill")
    @Produces(MediaType.TEXT_HTML)
    public String shutDownServerHtml(){
        App.log.log("Shutting down server due to kill request to API.");
        ShutdownRegistration shutdownRegistration = new ShutdownRegistration(2);
        return InfoPage.toHtml("<h1>AnyTest2Zephyr server shutdown in two seconds.</h1>" +
                "<input type=\"button\" value=\"Refresh Page\" onClick=\"window.location.href=window.location.href\">\n");
    }

    /**
     * Clears the transaction log.
     *
     * @return Returns a message
     */
    @GET
    @Path("clearlog")
    @Produces(MediaType.TEXT_HTML)
    public String clearLog(){
        App.log.log("Clearing the transaction log file due to received request.");
        App.log.clear();
        return InfoPage.toHtml("<i>Cleared the log file.</i>");
    }

    /**
     * Produces a form for manual entry of test details, mainly for testing.
     *
     * @return Returns a message
     */
    @GET
    @Path("form")
    @Produces(MediaType.TEXT_HTML)
    public String resultReportingForm(){
        App.log.log("Producing form for test result reporting.");
        return ResultReportForm.toHtml();
    }

    @GET
    @Path("logo")
    @Produces("image/png")
    public Response getLogo(){
        BufferedImage image = null;
        try {
            image = ImageIO.read(getClass().getClassLoader().getResource("logo.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", baos);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] imageData = baos.toByteArray();

        // uncomment line below to send non-streamed
         return Response.ok(imageData).build();

        // uncomment line below to send streamed
        // return Response.ok(new ByteArrayInputStream(imageData)).build();
    }

    /**
     * Saves current settings
     * @return Returns a message
     */
    @GET
    @Path("savesettings")
    @Produces(MediaType.TEXT_HTML)
    public String saveSettings(){
        Settings.attemptToSaveSettingsToFile();
        return InfoPage.toHtml("<i>Settings saved</i>");
    }

    @GET
    @Path("jiraprojects")
    @Produces(MediaType.APPLICATION_JSON)
    public String jiraProjects(){
        if(!JiraSession.isConnectedToJira)return "{\"options\":[{\"label\":\" <Not-connected-to-Jira> \",\"value\":\"0\"}]}";
        return JiraConnector.getProjects();
    }

    @POST
    @Path("savesettings")
    @Produces(MediaType.TEXT_PLAIN)
    public String saveCurrentSettings(String json){
        App.log.log("Received request to save current connection parameters '" + json + "'.");
        ObjectMapper mapper = new ObjectMapper();
        Settings.JiraSettings jiraSettings = null;
        try {
            jiraSettings = mapper.readValue(json, Settings.JiraSettings.class);
        } catch (IOException e) {
            return InfoPage.toHtml("Cannot interprete parameters. Cannot save settings.");
        }

        //Use new settings
        Settings.jiraAddress = jiraSettings.jiraBaseUrl;
        Settings.jiraPassword = jiraSettings.jiraUserPassword;
        Settings.jiraUserName = jiraSettings.jiraUserName;
        Settings.attemptToSaveSettingsToFile();
        return "Settings saved";
    }

    /**
     * Saves current settings
     * @return Returns a message
     */
    @POST
    @Path("tryconnection")
    @Produces(MediaType.TEXT_PLAIN)
    public String tryConnection(String json){
        App.log.log("Received request to test connection with parameters '" + json + "'.");
        ObjectMapper mapper = new ObjectMapper();
        Settings.JiraSettings jiraSettings = null;
        try {
            jiraSettings = mapper.readValue(json, Settings.JiraSettings.class);
        } catch (IOException e) {
            //Ignored;
        }
        if(jiraSettings == null) return "Cannot interprete parameters";

        //Save initial settings
        boolean originalState = App.supressConnectionStatusChecking;
        String originalJiraAddress = Settings.jiraAddress;
        String originalJiraUser = Settings.jiraUserName;
        String originalJiraPassword = Settings.jiraPassword;

        //Use new settings temporarily
        App.supressConnectionStatusChecking = false;
        Settings.jiraAddress = jiraSettings.jiraBaseUrl;
        Settings.jiraPassword = jiraSettings.jiraUserPassword;
        Settings.jiraUserName = jiraSettings.jiraUserName;
        //Perform check
        String returnString = HttpServer.connectionStatus();

        //Restore settings
        App.supressConnectionStatusChecking = originalState;
        Settings.jiraAddress = originalJiraAddress;
        Settings.jiraUserName = originalJiraUser;
        Settings.jiraPassword = originalJiraPassword;

        App.log.log("Returning connection status '" + returnString + "'.");
        return returnString;
    }

    /**
     * API endpoint for posting test results to Zephyr. The response is status information.
     *
     * @param testResult The JSON with test registration data.
     * @return Returns a report from the information transfer. If errors are encountered more information is displayed.
     */
    @POST
    @Path("report")
    @Produces(MediaType.TEXT_PLAIN)
    public String postTestRun(String testResult) {
        App.log.log("Received POST request to /any2zephyr/report with content: '" + testResult + "'.");
        try{
            TestResult result = new ObjectMapper().readValue(testResult, TestResult.class);
            App.log.log("Received test result: '" + result.toString() + "'.");
            result.report();
            //This is where code goes in
            return "Registering test result";
        } catch (Exception e){
            StringBuilder error = new StringBuilder();
            error.append(System.lineSeparator());
            error.append("Problems encountered in 'AnyTest2Zephyr server' while trying to register test result to Zephyr.");
            error.append(System.lineSeparator());
            error.append("Error: ");
            error.append(e.getMessage());
            error.append(System.lineSeparator());
            error.append("Cause: ");
            error.append(e.getCause());
            error.append(System.lineSeparator());
            try{
                for(StackTraceElement stackTraceElement : e.getStackTrace()){
                    error.append(stackTraceElement.toString()).append(System.lineSeparator());
                }
                error.append(System.lineSeparator());
            }catch (Exception ignored){}
            error.append("Debug log entries for test registration from AnyTest2Zephyr server:");
            error.append(System.lineSeparator());
            App.log.log(error.toString());
            return error.toString();
        }
    }
}