package se.kejsardamberg.any2zephyr.zephyr;

import okhttp3.*;
import org.json.JSONObject;
import se.kejsardamberg.any2zephyr.main.App;
import se.kejsardamberg.any2zephyr.main.Settings;

import javax.net.ssl.*;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

public class JiraSession {

    OkHttpClient client = null;
    private String url;
    private String userName;
    private String password;
    private String sessionName = "dummy";
    private String sessionValue = "dummy";
    public static boolean isConnectedToJira = false;

    public JiraSession(String url, String userName, String password){
        this.url = url;
        this.userName = userName;
        this.password = password;
        client = getUnsafeOkHttpClient();
        login();
        client = getUnsafeOkHttpClient().newBuilder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        final Request original = chain.request();

                        final Request authorized = original.newBuilder()
                                .addHeader("Cookie", sessionName + "=" + sessionValue)
                                .build();

                        try{
                            return chain.proceed(authorized);
                        } catch (Exception e){
                            App.log.log(e.toString());
                        }
                        return null;
                    }
                })
                .connectTimeout(Settings.connectionTimeout, TimeUnit.SECONDS)
                .readTimeout(Settings.connectionTimeout, TimeUnit.SECONDS)
                .writeTimeout(Settings.connectionTimeout, TimeUnit.SECONDS)
                .build();
    }

    static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    public Response getRequest(String url){
        App.log.logDebug("Performing HTTP GET request to URL: '" + url + "'.");
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Cache-Control", "no-cache")
                .addHeader(sessionName, sessionValue)
                .build();
        Response response = null;
        if(client == null) return null;
        try {
            return client.newCall(request).execute();
        } catch (Exception e) {
            App.log.log(e.toString());
        }
        return response;
    }

    public Response putRequest(String url, String body) {
        App.log.logDebug("Performing HTTP PUT request to URL: '" + url + "' with data '" + body + "'.");
        RequestBody requestBody = RequestBody.create(JSON, body);
        Request request = new Request.Builder()
                .url(url)
                .put(requestBody)
                .build();
        try {
            Response response = client.newCall(request).execute();
            return response;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Response postRequestNoLogging(String url, String body){
        return postRequestExecution(url, body, false);
    }

    public Response postRequest(String url, String body) {
        return postRequestExecution(url, body, true);
    }

    private Response postRequestExecution(String url, String body, boolean performLogging){
        if(performLogging) App.log.logDebug("Performing HTTP POST request to URL: '" + url + "' with data '" + body + "'.");
        RequestBody requestBody = RequestBody.create(JSON, body);
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        try {
            Response response = client.newCall(request).execute();
            return response;
        } catch (IOException e) {
            App.log.log(e.toString());
        }
        return null;
    }

    private void login(){ //Performs login call to get cookie data
        App.log.log("Logging in to Jira with jiraUserName '" + userName + "'.");
        String responseBody = null;
        try {
            responseBody = postRequestNoLogging(url + "/rest/auth/latest/session", "{\"username\": \"" + userName + "\", \"password\": \"" + password + "\"}").body().string();
        } catch (Exception e) {
            App.log.log(e.toString());
        }
        if(responseBody == null) return;
        App.log.logDebug(responseBody);
        JSONObject jsonObject = null;
        jsonObject = new JSONObject(responseBody);
        final JSONObject finalJsonObject = jsonObject;
        sessionName = finalJsonObject.getJSONObject("session").getString("name");
        sessionValue = finalJsonObject.getJSONObject("session").getString("value");
        if(sessionValue != "dummy") isConnectedToJira = true;
    }

    private static OkHttpClient getUnsafeOkHttpClient() { //Bypassing SSL security check
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();


            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager)trustAllCerts[0]);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            OkHttpClient okHttpClient = builder
                    .connectTimeout(Settings.connectionTimeout, TimeUnit.SECONDS)
                    .readTimeout(Settings.connectionTimeout, TimeUnit.SECONDS)
                    .writeTimeout(Settings.connectionTimeout, TimeUnit.SECONDS)
                    .build();
            return okHttpClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
