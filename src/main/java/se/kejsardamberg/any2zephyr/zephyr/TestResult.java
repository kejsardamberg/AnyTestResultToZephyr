package se.kejsardamberg.any2zephyr.zephyr;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.json.JSONArray;
import org.json.JSONObject;
import se.kejsardamberg.any2zephyr.main.App;
import se.kejsardamberg.any2zephyr.main.Settings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestResult {
    @JsonProperty public TestStatus status = TestStatus.NO_RUN;
    @JsonProperty public String issue;
    @JsonProperty public String project;
    @JsonProperty public String attachment;
    @JsonProperty public String comment = null;

    String issueId;
    String projectId;
    String rootIssueKey;
    String executionId;
    List<String> testStepIds = new ArrayList<>();

    public TestResult(){} //For JSON Jackson

    public TestResult(String project, String issueName, TestStatus status, String comment){
        this.issue = issueName;
        this.project = project;
        this.status = status;
        this.comment = comment;
    }

    @Override
    public String toString(){
        return "[TestResult: Project='" + project +
                "', projectId='" + projectId +
                "', status='" + status.toString() +
                "', issue='" + issue +
                "', issueId='" + issueId +
                "', rootIssueKey='" + rootIssueKey +
                "', executionId='" + executionId +
                "', comment='" + comment +
                "', testStepIds='" + String.join("', '", testStepIds) + "']";
    }

    public void report(){
        if(cannotFindProjectId()) {
            App.log.log("Cannot find project '" + project + "' in Jira.");
            return;
        }
        if(cannotFindIssueId()) {
            App.log.log("Cannot find issue id for issue '" + issue + "'.");
            return;
        }
        if(!issueIsTest()){
            if(!issueHasAutomatedRegressionTest()){
                createNewTest();
                linkTestToIssue();
            }
        }
        createNewExecution();
        invokeNewExecution();
        if(cannotFindExecutionId()) return;
        updateStatusForTestSteps();
        //Attachment attachment = new Attachment("C:\\temp\\test.txt", this);
        //attachment.uploadFile();
        updateTestExecutionStatus();
    }

    private boolean issueHasAutomatedRegressionTest(){
        try {
            String responseBody = JiraConnector.getJiraSessionSingleton().getRequest(Settings.jiraAddress + "/rest/api/2/issue/" + rootIssueKey + "?fields=issuelinks").body().string();
            JSONObject issueObject = new JSONObject(responseBody);
            if(issueObject.getJSONObject("fields") == null)return false;
            JSONObject fields = issueObject.getJSONObject("fields");
            JSONArray links = fields.getJSONArray("issuelinks");
            for (int i = 0; i < links.length(); i++) {
                JSONObject link = links.getJSONObject(i);
                if(link.getJSONObject("inwardIssue") == null) continue;
                if(link.getJSONObject("inwardIssue").getJSONObject("fields") == null) continue;
                //if(link.getJSONObject("inwardIssue").getJSONObject("fields").getJSONObject("summary") == null) continue;
                if(link.getJSONObject("inwardIssue").getJSONObject("fields").getString("summary").equals(Settings.testSummaryForCreatedTests)) {
                    App.log.log("Issue '" + rootIssueKey + "' already has a test called '" + Settings.testSummaryForCreatedTests + "'.");
                    issueId = link.getJSONObject("inwardIssue").getString("id");
                    issue = link.getJSONObject("inwardIssue").getString("key");
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean issueIsTest(){
        boolean is = JiraConnector.getIssueType(issue).equals("Test");
        if(is) return is;
        rootIssueKey = issue;
        App.log.log("Issue '" + issue + "' is not a Zephyr test but an issue of type '" + JiraConnector.getIssueType(issue) + "'.");
        return is;
    }

    private void createNewTest() {
        App.log.log("Attempting to create a Zephyr test as a sub-issue of issue '" + issue + "'.");
        rootIssueKey = issue;
        String postJson = "{\n" +
                "    \"fields\": {\n" +
                "       \"project\":\n" +
                "       { \n" +
                "          \"id\": \"" + projectId + "\"\n" +
                "       },\n" +
                "       \"summary\": \"" + Settings.testSummaryForCreatedTests + "\",\n" +
                "       \"description\": \"Automatically created test.\",\n" +
                "       \"issuetype\": {\n" +
                "          \"name\": \"Test\"\n" +
                "       }\n" +
                "   }\n" +
                "}";
        try{
            String responseBody = JiraConnector.getJiraSessionSingleton().postRequest(Settings.jiraAddress + "/rest/api/2/issue/", postJson).body().string();
            App.log.logDebug("Create test response body: " + responseBody);
            JSONObject obj = new JSONObject(responseBody);
            issueId = obj.getString("id");
            issue = obj.getString("key");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void linkTestToIssue(){
        String issuesLinkJson = "{\n" +
                "    \"type\":{\n" +
                "        \"name\":\"Relates\"\n" +
                "    },\n" +
                "    \"inwardIssue\": {\n" +
                "        \"id\": \"" + issueId + "\"\n" +
                "    },\n" +
                "    \"outwardIssue\": {\n" +
                "        \"id\": \"" + JiraConnector.getIssueId(rootIssueKey) + "\"\n" +
                "    },\n" +
                "    \"comment\": {\n" +
                "        \"body\": \"Automatically linked test to issue.\"\n" +
                "    }\n" +
                "}";
        String responseBody2 = null;
        try {
            responseBody2 = JiraConnector.getJiraSessionSingleton().postRequest(Settings.jiraAddress + "/rest/api/2/issueLink", issuesLinkJson).body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        App.log.log("Link issue '" + rootIssueKey + "' to test '" + issue + "'.");
        App.log.logDebug("Link issue response: " + responseBody2);

    }

    private void invokeNewExecution() {
        try {
            String responseBody = JiraConnector.getJiraSessionSingleton().getRequest(Settings.jiraAddress + "/rest/zapi/latest/execution?issueId=" + issueId).body().string();
            App.log.logDebug(responseBody);
            if(!responseBody.contains("\"" + issueId + "\"")) App.log.logDebug("Could not find issue id in response. Continuing.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateTestExecutionStatus(){
        JiraConnector.updateExecutionStatus(executionId, status, comment);
    }

    private void updateStatusForTestSteps() {
        testStepIds = JiraConnector.getTestStepIds(issueId);
        for(String testStepId : testStepIds){
            JiraConnector.createNewStepResult(issueId, testStepId, executionId, TestResult.TestStatus.PASSED);
        }
    }

    private void createNewExecution() {
        executionId = JiraConnector.createNewExecutionGetExecutionId(issueId, projectId);
    }

    private boolean cannotFindIssueId(){
        if(issueId == null) getIssueId(issue);
        return issueId == null;
    }

    private void getIssueId(String issueName) {
        issueId = JiraConnector.getIssueId(issueName);
    }

    private boolean cannotFindProjectId(){
        if(projectId == null) getProjectIdForProject(project);
        return projectId == null;
    }

    private void getProjectIdForProject(String projectName) {
        projectId = JiraConnector.getProjectIdForProject(projectName);
    }

    private boolean cannotFindExecutionId(){
        if(executionId == null)return true;
        return false;
    }

    public enum TestStatus {
        PASSED("1", "Passed"),
        FAILED("2", "Failed"),
        NO_RUN("-1", "Unexecuted"),
        WIP("3", "Work in progress");

        private String statusValue;
        private String friendlyName;

        private TestStatus(String testStepValue, String friendlyName){
            this.statusValue = testStepValue;
            this.friendlyName = friendlyName;
        }

        @Override
        public String toString(){
            return friendlyName;
        }

        public String getValue() {
            return statusValue;
        }
    }
}
