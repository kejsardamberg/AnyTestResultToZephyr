Any2Zephyr
==================
This tool is used to propagate test results from any test tool to tests in Zephyr.

Requirements:
1). Java JRE (version >= 1.8).
2). Jira installation with Zephyr and ZAPI.
3). An active Jira user account
4). Network access to the Jira installation from where this program is executed

Basic usage
----------------------------------------------------------
The tool report results to Zephyr from any test tool by giving a Jira project, a Jira issue to report results to, and the status of the test (passed or failed).
If the issue stated for reporting is another issue type than Zephyr test test results are reported to a Zephyr test called 'Automated regression.' that is a referenced issue to the given issue.
Each test result reporting creates a new test execution.

java -jar any2zephyr.jar -help

Execution modes
----------------------------------------------------------
There are two basic modes of this tool. For single test case reporting the command line interface will do fine. For many test result reportings the program and JVM initialization takes a lot of time, hence a REST service has been produced.

Command line interface for single test case reporting
----------------------------------------------------------
The CLI interface is used by using the syntax below, where valid statuses are pass, fail, passed, and failed. Status values are not sensitive to case.

java -jar any2zephyr.jar jiraaddress=http://jira.myorganization.org:80/ jirauser=myjirausername jirapassword=myjirapassword project=MyJiraProject issue=TheIssueKey status=pass

Saving settings
----------------------------------------------------------
For ease of use the Jira address, user name, and password can be saved as a JSON object in the same folder as the any2zephyr.jar resides in. One way of doing this is using the switch 'savesettings'. This produces the file. E.g;

java -jar any2zephyr.jar jiraaddress=http://jira.myorganization.org:80/ jirauser=myjirausername jirapassword=myjirapassword savesettings

Another way of producing this file is from the 'Save settings'-button in the web GUI when this utility is ran in the server mode. A sample file content can be seen on the row below:
{"jiraBaseUrl":"https://jira.company.org:8080","jiraUserName":"admin","jiraUserPassword":"SoooooSecret123"}

Server mode
----------------------------------------------------------
The server mode is entered giving the switch 'mode=server'. This starts a small web server that hosts a few end-points. Of course Jira connection information must be provided, either at the command line, like below, or through the properties file as stated above.

java -jar any2zephyr.jar jiraaddress=http://jira.myorganization.org:80/ jirauser=myjirausername jirapassword=myjirapassword mode=server

A few small web pages can be displayed for this server. They root of these can be found at http://localhost:80/any2zephyr/. This page also contains a few admin actions and endpoint descriptions for the REST services provided.

