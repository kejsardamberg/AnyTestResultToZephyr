package se.kejsardamberg.any2zephyr.zephyr;

import org.json.JSONObject;
import se.kejsardamberg.any2zephyr.main.Settings;

import java.io.IOException;

public class TestCycle {
    String json;
    String name;
    String description;
    String projectId;
    String createdCycleUrl;
    String cycleId;

    public TestCycle(String name, String description, String projectId){
        this.name = name;
        this.description = description;
        this.projectId = projectId;
        json = "{\n" +
                "    \"name\": \"" + name + "\",\n" +
                "    \"description\": \"" + description + "\",\n" +
                "    \"projectId\": \"" + projectId + "\"" +
                "}";

    }

    public void createInZephyr(){
        String createCycleURL = Settings.jiraAddress + "/rest/zapi/latest/cycle";
        String responseBody = null;
        try {
            responseBody = JiraConnector.getJiraSessionSingleton().postRequest(createCycleURL, json).body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Test cycle creation response: " + responseBody);
        JSONObject cycle = new JSONObject(responseBody);
        cycleId = cycle.getString("id");
        createCycleURL = cycle.getString("self");
    }

    public void addTestCase(String issueId){
        String addTestsToCycleURL = Settings.jiraAddress + "/rest/zapi/latest/execution/addTestsToCycle/";
        String testJson = "{\n" +
                "    \"issues\": \"" + issueId + "\",\n" +
                "    \"cycleId\": \"" + cycleId + "\",\n" +
                "    \"projectId\": \"" + projectId + "\",\n" +
                "    \"method\": \"1\"    \n" +
                "}";

        String responseBody = null;
        try {
            responseBody = JiraConnector.getJiraSessionSingleton().postRequest(createdCycleUrl, json).body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Test case adding response: " + responseBody);
    }

    public void linkToIssue(String issueId){

    }
}
