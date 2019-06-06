package se.kejsardamberg.any2zephyr.zephyr;

import okhttp3.*;
import se.kejsardamberg.any2zephyr.main.Settings;

import java.io.*;
import java.net.URLConnection;

import static se.kejsardamberg.any2zephyr.zephyr.JiraSession.JSON;

public class Attachment {

    public EntityType entityType;
    public long entityId = 0;
    public String filePath;
    private String json = "";
    private TestResult testResult;
    private String url;

    public Attachment(String path, TestResult testResult){
        this.filePath = path;
        this.entityType = EntityType.EXECUTION;
        this.testResult = testResult;
        if(entityType == null)return;
        this.url = Settings.jiraAddress + "/rest/zapi/latest/attachment?entityId=" + testResult.executionId + "&entityType=" + entityType.toString();
        File file = new File(path);
        if(!file.exists())return;
        RandomAccessFile f = null;
        try {
            f = new RandomAccessFile(file, "r");
            byte[] b = new byte[0];
            b = new byte[(int)f.length()];
            f.read(b);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Attachment(String path, TestResult testResult, String executionOrTestStep){
        this.filePath = path;
        this.testResult = testResult;
        this.entityType = EntityType.valueOf(executionOrTestStep.toUpperCase());
        if(entityType == null)return;
        this.url = Settings.jiraAddress + "/rest/zapi/latest/attachment?entityId=" + testResult.executionId + "&entityType=" + entityType.toString();
    }

    public enum EntityType{
        EXECUTION,
        TESTSTEPRESULT
    }

    public void uploadFile() {
        File file = new File(filePath);
        String url = Settings.jiraAddress + "/attachment?entityId=" + testResult.executionId + "&entityType=EXECUTION";
        RequestBody formBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(),
                        RequestBody.create(MediaType.parse("text/plain"), file))
                .addFormDataPart("other_field", "other_field_value")
                .build();
        Request request = new Request.Builder().url(url).post(formBody).build();
        try {
            Response response = JiraConnector.getJiraSessionSingleton().client.newCall(request).execute();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
    public void upload(){
        System.out.println("-------------------------------------------------------");
        System.out.println("Attempting to upload attachment '" + filePath + "'.");
        File file = new File(filePath);
        if(!file.exists())return;
        RandomAccessFile f = null;
        try {
            f = new RandomAccessFile(file, "r");
            byte[] b = new byte[0];
            b = new byte[(int)f.length()];
            f.read(b);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String mimeType = null;
        try {
            InputStream is = new BufferedInputStream(new FileInputStream(file));
            mimeType = URLConnection.guessContentTypeFromStream(is);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.json = "{'file':'" + filePath + "', " + f + ", 'application/octet-stream')}";
        String responseBody = null;
        try {

            RequestBody requestBody = RequestBody.create(JSON, json);
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Content-Type", "application/octet-stream")
                    .post(requestBody)
                    .build();
            Response response = JiraConnector.getJiraSessionSingleton().client.newCall(request).execute();
            //Response response = getJiraSessionSingleton().postRequest(url, json);
            responseBody = response.body().string();
            System.out.println("Mime: '" + mimeType + "', response code:" + response.code());
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Output from attachment upload: " + responseBody);

    }
}
