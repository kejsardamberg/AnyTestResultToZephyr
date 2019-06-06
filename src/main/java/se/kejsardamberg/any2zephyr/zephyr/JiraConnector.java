package se.kejsardamberg.any2zephyr.zephyr;

import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import se.kejsardamberg.any2zephyr.main.App;
import se.kejsardamberg.any2zephyr.main.Settings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JiraConnector {
    private static String jiraBaseUrl = Settings.jiraAddress;

    private static JiraSession jira;

    public static JiraSession getJiraSessionSingleton(){
        if(jira == null){
            jira = new JiraSession(Settings.jiraAddress, Settings.jiraUserName, Settings.jiraPassword);
        }
        return jira;
    }


    public JiraConnector(){
        this.jiraBaseUrl = Settings.jiraAddress;
        jira = new JiraSession(Settings.jiraAddress, Settings.jiraUserName, Settings.jiraPassword);
    }

    public JiraConnector(String jiraBaseUrl, String userName, String password){
        this.jiraBaseUrl = jiraBaseUrl;
        jira = new JiraSession(jiraBaseUrl, userName, password);

    }

    public static String getProjects(){
        App.log.logDebug("----------------------------------------------------------------------------------------------");
        App.log.logDebug("Retrieving registered projects in Jira at '" + jiraBaseUrl + "'.");
        String projectJson = null;
        try {
            projectJson = getJiraSessionSingleton().getRequest(jiraBaseUrl + "/rest/zapi/latest/util/project-list").body().string();
        } catch (IOException e) {
            App.log.logDebug(e.toString());
        }
        App.log.logDebug("Projects: " + projectJson);
        return projectJson;
    }

    public static String getLoggedInUser(){
        String userInfoJson = null;
        try {
            userInfoJson = getJiraSessionSingleton().getRequest(jiraBaseUrl + "/rest/zapi/latest/zql/executionFilter/jiraUserName").body().string();
        } catch (Exception e) {
            App.log.log(e.toString());
        }
        App.log.logDebug("Logged in jiraUserName info: " + userInfoJson);
        return userInfoJson;
    }

    public void reportTestResult(String projectName, String issueName, TestResult.TestStatus status, String comment) {
        App.log.logDebug("----------------------------------------------------------------------------------------------");
        App.log.logDebug("Reporting results.");
        String projectId = getProjectIdForProject(projectName);
        String issueId = getIssueId(issueName);
        String executionId = createNewExecutionGetExecutionId(issueId, projectId);
        List<String> testStepIds = getTestStepIds(issueId);
        for(String testStepId : testStepIds){
            createNewStepResult(issueId, testStepId, "452", TestResult.TestStatus.PASSED);
        }
        updateExecutionStatus(executionId, status, comment);
    }

    public static String getProjectIdForProject(String projectName) {
        App.log.logDebug("----------------------------------------------------------------------------------------------");
        App.log.logDebug("Getting project id for project with name '" + projectName + "'.");
        if(projectName == null) return null;
        String id = null;
        JSONObject projectsJson = new JSONObject(getProjects());

        JSONArray c = projectsJson.getJSONArray("options");
        for (int i = 0 ; i < c.length(); i++) {
            JSONObject obj = c.getJSONObject(i);
            String projectLabel = obj.getString("label");
            if(projectLabel == null) continue;
            App.log.logDebug("Found project labeled '" + projectLabel + "'.");
            if(projectLabel.contains(projectName)){
                id = obj.getString("value");
            }
        }
        if(id != null){
            App.log.logDebug("Project id for project called '" + projectName + "' found to be " + id + ".");
        }
        return id;
    }

    public static String updateExecutionStatus(String executionId, TestResult.TestStatus status, String comment){
        App.log.logDebug("----------------------------------------------------------------------------------------------");
        App.log.logDebug("Updating execution status to '" + status.toString() + "' for execution with id=" + executionId + ".");
        String responseBody = null;
        String json = "{\"status\": \"" + status.getValue() + "\"";
        if(comment != null) json += ", \"comment\": \"" + comment + "\"";
        json += "}";
        try {
            responseBody = getJiraSessionSingleton().putRequest(jiraBaseUrl + "/rest/zapi/latest/execution/" + executionId +"/execute",
                    //"{\"lastTestResult\":{ \"executionStatus\": \"" + status.getValue() + "\"}}").body().string();
                    json).body().string();
        } catch (IOException e) {
            App.log.logDebug(e.toString());
        }
        App.log.logDebug("Execution status update response body:" + responseBody);
        return responseBody;
    }

    public static String createNewExecutionGetExecutionId(String issueId, String projectId){
        App.log.logDebug("----------------------------------------------------------------------------------------------");
        App.log.logDebug("Creating new execution for issue with issueId=" + issueId + " and projectId=" + projectId + ".");
        if(issueId == null || projectId == null) return null;
        String responseBody = null;
        try {
            responseBody = getJiraSessionSingleton().postRequest(jiraBaseUrl + "/rest/zapi/latest/execution", "{\n" +
                    "  \"issueId\": \"" + issueId + "\",\n" +
                    "  \"projectId\": \"" + projectId + "\",\n" +
                    "  \"cycleId\": \"-1\"\n" +
                    "}").body().string();
        } catch (IOException e) {
            App.log.logDebug(e.toString());
        }
        App.log.logDebug("New execution response body:" + responseBody);
        JSONObject jsonObj = new JSONObject(responseBody);
        String executionId = jsonObj.keySet().toArray()[0].toString();
        return executionId;
    }

    public static void createNewStepResult(String issueId, String stepId, String executionId, TestResult.TestStatus status){
        App.log.logDebug("----------------------------------------------------------------------------------------------");
        App.log.logDebug("Creating new step result '" + status.toString() + "' for issue with issueId=" + issueId + " and stepId=" + stepId + ".");
        String responseBody = null;
        try {
            responseBody = getJiraSessionSingleton().putRequest(jiraBaseUrl + "/rest/zapi/latest/teststep/" + issueId, "{\"stepId\":" + stepId + ",\"issueId\":\"" + issueId + "\",\"executionId\":" + executionId + ",\"status\":\"" + status.getValue() + "\",}").body().string();
        } catch (IOException e) {
            App.log.logDebug(e.toString());
        }
        App.log.logDebug("New test step result body: " + responseBody);
    }


    public static List<String> getTestStepIds(String issueId){
        App.log.logDebug("----------------------------------------------------------------------------------------------");
        App.log.logDebug("Getting the test steps for issue with issueId=" + issueId + ".");
        Response response = getJiraSessionSingleton().getRequest(jiraBaseUrl + "/rest/zapi/latest/teststep/" + issueId);
        String responseBody = null;
        try {
            responseBody = response.body().string();
        } catch (IOException e) {
            App.log.logDebug(e.toString());
        }
        if(responseBody == null) return new ArrayList<String>();
        ArrayList<String> testStepIds = new ArrayList<>();
        if(response.code() != 200){
            App.log.logDebug("Could not retrieve test steps for issue with id '" + issueId + "'. Response: " + response.code() + System.lineSeparator() + response.body().toString());;
        } else {
            JSONArray testSteps = null;
            try {
                App.log.logDebug("'" + responseBody + "'");
                testSteps = new JSONArray(responseBody);
            } catch (Exception e) {
                App.log.logDebug(e.getMessage());
            }
            if(testSteps != null)
                for (int i = 0; i < testSteps.length(); i++) {
                    JSONObject testStep = testSteps.getJSONObject(i);
                    testStepIds.add(String.valueOf(testStep.getInt("id")));
                }
        }
        App.log.logDebug("Test step ids: " + testStepIds);
        return testStepIds;
    }

    public static String getIssueType(String issueKey){
        App.log.logDebug("----------------------------------------------------------------------------------------------");
        App.log.logDebug("Getting the issue type for issue with key '" + issueKey + "'.");
        if(issueKey == null) return null;
        Response response = getJiraSessionSingleton().getRequest(jiraBaseUrl + "/rest/api/2/issue/" + issueKey);
        String issueType = null;
        if(response.code() != 200){
            App.log.logDebug("Could not retrieve issue '" + issueKey + "'. Response: " + response.code() + System.lineSeparator() + response.body().toString());;
        } else {
            String responseBody = null;
            try {
                responseBody = response.body().string();
            } catch (IOException e) {
                App.log.logDebug(e.toString());
            }
            App.log.logDebug("Issue body: " + responseBody);
            JSONObject obj = null;
            obj = new JSONObject(responseBody);
            JSONObject fields = obj.getJSONObject("fields");
            JSONObject issueTypeJson = fields.getJSONObject("issuetype");
            issueType = issueTypeJson.getString("name");
            App.log.logDebug("Issue type: " + issueType);
        }
        return issueType;
    }

    public static String getIssueId(String issueKey) {
        App.log.logDebug("----------------------------------------------------------------------------------------------");
        App.log.logDebug("Getting the issue id for issue with key '" + issueKey + "'.");
        if(issueKey == null) return null;
        Response response = getJiraSessionSingleton().getRequest(jiraBaseUrl + "/rest/api/2/issue/" + issueKey);
        String issueId = null;
        if(response.code() != 200){
            App.log.logDebug("Could not retrieve issue '" + issueKey + "'. Response: " + response.code() + System.lineSeparator() + response.body().toString());;
        } else {
            String responseBody = null;
            try {
                responseBody = response.body().string();
            } catch (IOException e) {
                App.log.logDebug(e.toString());
            }
            App.log.logDebug("Issue body: " + responseBody);
            JSONObject obj = null;
            obj = new JSONObject(responseBody);
            issueId = obj.getString("id");
            App.log.logDebug("IssueId: " + issueId);
        }
        return issueId;
    }

}
