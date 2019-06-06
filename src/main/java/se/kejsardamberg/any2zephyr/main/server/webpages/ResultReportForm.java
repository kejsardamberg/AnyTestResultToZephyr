package se.kejsardamberg.any2zephyr.main.server.webpages;

import se.kejsardamberg.any2zephyr.main.Settings;

public class ResultReportForm {


    public static String toHtml(){
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>").append(System.lineSeparator());
        sb.append("<html lang=\"en\">").append(System.lineSeparator());
        sb.append("   <head>").append(System.lineSeparator());
        sb.append(CommonSections.headSection("", "", "")).append(System.lineSeparator());
        sb.append("      <script>").append(System.lineSeparator());
        sb.append("         function postResult(){").append(System.lineSeparator());
        sb.append("             var projectValue = document.getElementById(\"project\").value;").append(System.lineSeparator());
        sb.append("             var issueValue = document.getElementById(\"issue\").value;").append(System.lineSeparator());
        sb.append("             var statusElement = document.getElementById(\"status\");").append(System.lineSeparator());
        sb.append("             var statusValue = statusElement.options[statusElement.selectedIndex].value;").append(System.lineSeparator());
        sb.append("             var commentString = document.getElementById(\"comment\").value;").append(System.lineSeparator());
        sb.append("             var xhttp = new XMLHttpRequest();").append(System.lineSeparator());
        sb.append("             xhttp.open(\"POST\", \"any2zephyr/report\", true);");
        sb.append("             xhttp.setRequestHeader(\"Content-type\", \"application/x-www-form-urlencoded\");").append(System.lineSeparator());
        sb.append("             xhttp.send('{\"project\": \"' + projectValue + '\", \"issue\": \"' + issueValue + '\", \"status\": \"' + statusValue + '\", \"comment\": \"' + commentString + '\" }');").append(System.lineSeparator());
        sb.append("         }").append(System.lineSeparator()).append(System.lineSeparator());
        sb.append("         function getProjects(){").append(System.lineSeparator());
        sb.append("             let dropdown = document.getElementById(\"project\");").append(System.lineSeparator());
        sb.append("             dropdown.length = 0;").append(System.lineSeparator());
        sb.append("             let defaultOption = document.createElement('option');").append(System.lineSeparator());
        sb.append("             defaultOption.text = 'Select Jira project';").append(System.lineSeparator());
        sb.append("             dropdown.add(defaultOption);").append(System.lineSeparator());
        sb.append("             dropdown.selectedIndex = 0;").append(System.lineSeparator());
        sb.append("             var xhttp = new XMLHttpRequest();").append(System.lineSeparator());
        sb.append("             xhttp.onreadystatechange = function() {").append(System.lineSeparator());
        sb.append("                 if (this.readyState == 4 && this.status == 200) {").append(System.lineSeparator());
        sb.append("                     const data = JSON.parse(this.responseText);").append(System.lineSeparator());
        sb.append("                     let option;").append(System.lineSeparator());
        sb.append("                     for (let i = 0; i < data.options.length; i++) {").append(System.lineSeparator());
        sb.append("                         option = document.createElement('option');").append(System.lineSeparator());
        sb.append("                         option.text = data.options[i].label;").append(System.lineSeparator());
        sb.append("                         option.value = data.options[i].value;").append(System.lineSeparator());
        sb.append("                         dropdown.add(option);").append(System.lineSeparator());
        sb.append("                     }").append(System.lineSeparator());
        sb.append("                 }").append(System.lineSeparator());
        sb.append("             };").append(System.lineSeparator()).append(System.lineSeparator());
        sb.append("             xhttp.open(\"GET\", \"any2zephyr/jiraprojects\", true);");
        //sb.append("             xhttp.setRequestHeader(\"Content-type\", \"application/json\");").append(System.lineSeparator());
        sb.append("             xhttp.send();").append(System.lineSeparator());
        sb.append("         }").append(System.lineSeparator()).append(System.lineSeparator());
        sb.append("      </script>").append(System.lineSeparator());
        sb.append("   </head>").append(System.lineSeparator());
        sb.append("   <body onload='getProjects()'>").append(System.lineSeparator());
        sb.append("    <table id=\"CONTENT\">").append(System.lineSeparator()).append(System.lineSeparator());
        sb.append("      <tr>").append(System.lineSeparator());
        sb.append("        <td>").append(System.lineSeparator()).append(System.lineSeparator());
        sb.append(CommonSections.pageHeader()).append(System.lineSeparator());
        sb.append("           <br>").append(System.lineSeparator());
        sb.append("           <h1>Report test result to Zephyr</h1>").append(System.lineSeparator());
        sb.append("           <p>Current Jira URL: " + Settings.jiraAddress + "</p>").append(System.lineSeparator());
        sb.append("           <form action=\"any2zephyr/result\" method=\"post\" id=\"form1\">").append(System.lineSeparator());
        sb.append("              <span class=\"fieldname\" width=\"160 px\">Jira project:</span><select id=\"project\"></select><br>").append(System.lineSeparator());
        sb.append("              <span class=\"fieldname\" width=\"160 px\">Jira issue key:</span><input type=\"text\" id=\"issue\" name=\"issue\"><br>").append(System.lineSeparator());
        sb.append("              <span class=\"fieldname\" width=\"160 px\">Status:</span><select id=\"status\"><option value=\"PASSED\">PASSED</option><option value=\"FAILED\">FAILED</option></select><br>").append(System.lineSeparator());
        sb.append("              <span class=\"fieldname\" width=\"160 px\">Test execution comment:</span><input type=\"text\" id=\"comment\" name=\"comment\"><br>").append(System.lineSeparator());
        sb.append("           </form>").append(System.lineSeparator());
        sb.append("           <button type=\"button\" form=\"form1\" value=\"Submit\" onclick=\"postResult()\">Submit</button>").append(System.lineSeparator());
        sb.append(     CommonSections.pageFooter());
        sb.append("        </td>").append(System.lineSeparator());
        sb.append("      </tr>").append(System.lineSeparator()).append(System.lineSeparator());
        sb.append("    </table>").append(System.lineSeparator()).append(System.lineSeparator());
        sb.append("</html>").append(System.lineSeparator());
        return sb.toString();
    }
}
