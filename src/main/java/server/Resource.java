package server;

import webpages.AboutPage;
import webpages.InfoPage;

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
        System.out.println("Got a request for version.");
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
        System.out.println("Got a request for version.");
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
        System.out.println("Got a request for version.");
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
        System.out.println("Shutting down server due to kill request to API.");
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
        System.out.println("Shutting down server due to kill request to API.");
        ShutdownRegistration shutdownRegistration = new ShutdownRegistration(2);
        return InfoPage.toHtml("<h1>AnyTest2Zephyr server shutdown in two seconds.</h1>");
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
     * API endpoint for posting test results to Zephyr. The response is status information.
     *
     * @param testResult The JSON with test registration data.
     * @return Returns a report from the information transfer. If errors are encountered more information is displayed.
     */
    @POST
    @Path("report")
    @Produces(MediaType.TEXT_PLAIN)
    public String postTestRun(String testResult) {
        System.out.println("Received POST request to /report with content: '" + testResult + "'." + System.lineSeparator());
        try{
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
            System.out.println(error.toString());
            return error.toString();
        }
    }
}