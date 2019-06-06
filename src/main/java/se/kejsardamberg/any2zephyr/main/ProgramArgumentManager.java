package se.kejsardamberg.any2zephyr.main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class is meant to ease program argument management.
 * It accepts a wide range of well used argument formats.
 * For example it can accept and interprete argument formats
 * from both the Windows and the Unix/Linux worlds,
 * like the examples below:<br>
 * -myArgumentName myArgumentValue<br>
 * --myArgumentName myArgumentValue<br>
 * myArgumentName:myArgumentValue<br>
 * -myArgumentName:myArgumentValue<br>
 * myArgumentName = myArgumentValue<br>
 * myArgumentName=myArgumentValue<br>
 * -myArgumentName=myArgumentValue<br>
 * myArgumentName = myArgumentValue<br><br>
 * <p>
 * It can also manage switches (stand-alone arguments), as well as parameter values.<br>
 * myStandaloneArgumentSwitch<br>
 * -myStandaloneArgumentSwitch<br>
 * --myStandaloneArgumentSwitch<br><br>
 * <p>
 * Support for long string management is also included<br>
 * out of the box. Examples below:<br>
 * -"My Parameter Name"="My parameter value"<br>
 * 'My parameter name':'My parameter value'<br>
 * myParameterName:'My parameter with escaped \' character'<br>
 * --myParameterName="My \" escaped character string"<br><br>
 * <p>
 * This class also can keep track of what arguments has been used or not.
 * This is meant to be used for for example logging of unrecognized
 * switches and parameters.
 *
 * Separators ('=' and ':') and quotes (' or ") can be escaped.
 */
public class ProgramArgumentManager {

    private ArgumentList programArguments = new ArgumentList();
    private String[] initialArguments;
    private String[] propertySeparators = new String[]{"=", ":"};
    private String[] stringMarkers = new String[]{"\"", "'"};

    /**
     * Enables access to prepared program argument management.
     *
     * @param args Program arguments
     */
    public ProgramArgumentManager(String[] args) {
        initiate(args);
    }

    /**
     * Enables access to prepared program argument management.
     *
     * @param args Program arguments
     */
    public ProgramArgumentManager(String args) {
        initiate(new String[]{args});
    }

    /**
     * Enables access to prepared program argument management.
     *
     * @param args Program arguments
     */
    public ProgramArgumentManager(List<String> args) {
        initiate(args.toArray(new String[0]));
    }

    public HelpSection createHelpSection() {
        return new HelpSection();
    }

    /**
     * Enables access to prepared program argument management.
     *
     * @param args Program arguments
     */
    public ProgramArgumentManager(Object... args) {
        List<String> arguments = new ArrayList<>();
        for (Object o : args) {
            arguments.add(o.toString());
        }
        initiate(arguments.toArray(new String[0]));
    }

    public Argument getArgument(String argumentName) {
        for (Argument argument : programArguments) {
            if (argument.argumentName.toLowerCase().equals(argumentName.trim().toLowerCase())) {
                return argument;
            }
        }
        return null;
    }

    public Argument getArgumentByOrder(int order) {
        if (programArguments.size() < order) return null;
        return programArguments.get(order);
    }

    public String getArgumentByOrderAsString(int order) {
        if (programArguments.size() < order) return null;
        String returnString = programArguments.get(order).argumentName;
        if (programArguments.get(order).argumentValue != null)
            returnString += "=" + programArguments.get(order).argumentValue;
        return returnString;
    }

    private void initiate(String[] args) {
        initialArguments = args;
        for (String word : bringStringsTogether()) {
            programArguments.add(new UnidentifiedArgument(word));
        }
        cleanEmptyEntries();
        identifyPropertiesFromStandaloneSeparators();
        for (String separator : propertySeparators) {
            identifyPropertiesFromSeparator(separator);
        }
        identifyDashedProperties();
        cleanDashedEntries();
        identifyStandaloneArguments();
    }

    private void identifyDashedProperties() {
        List<Integer> indexesForRemoval = new ArrayList<>();
        for (int i = 0; i < programArguments.size(); i++) {
            Argument argument = programArguments.get(i);
            if (!argument.getClass().equals(UnidentifiedArgument.class)) continue;
            if (argument.argumentName.trim().startsWith("-") && i < programArguments.size() &&
                    argument.argumentName.replaceAll("-", "").trim().length() > 0) { //Should contain other characters than dash.
                if (programArguments.get(i + 1).getClass().equals(UnidentifiedArgument.class)) {
                    indexesForRemoval.add(i);
                    indexesForRemoval.add(i + 1);
                    programArguments.add(new PropertyArgument(removeInitialDashes(programArguments.get(i).argumentName), programArguments.get(i + 1).argumentName));
                    i++;
                }
            }
        }
        Collections.reverse(indexesForRemoval);
        for (int i : indexesForRemoval) {
            programArguments.remove(i);
        }
    }

    private void identifyStandaloneArguments() {
        for (int i = 0; i < programArguments.size(); i++) {
            if (programArguments.get(i).getClass().equals(UnidentifiedArgument.class)) {
                StandaloneArgument arg = new StandaloneArgument(programArguments.get(i).argumentName);
                programArguments.set(i, arg);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[ProgramArgumentManager. Arguments:")
                .append(System.lineSeparator());
        if (programArguments.size() > 0) {
            List<String> args = new ArrayList<>();
            sb.append("   Standalone arguments: ");
            for (Argument argument : programArguments) {
                if (argument.argumentValue == null)
                    args.add("'" + argument.argumentName + "' (" + argument.getClass().getSimpleName() + ")");
            }
            sb.append(String.join(", ", args))
                    .append(".")
                    .append(System.lineSeparator());
        }
        if (numberOfArgumentsWithValues() > 0) {
            List<String> descriptions = new ArrayList<>();
            sb.append("   Identified arguments with values: ");
            for (Argument argument : getArgumentsWithValues()) {
                descriptions.add("'" + argument.argumentName + "'='" + argument.argumentValue + "' (" + argument.getClass().getSimpleName() + ")");
            }
            sb.append(String.join(", ", descriptions)).append(".").append(System.lineSeparator());
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Get value of parameter by its name.
     *
     * @param argumentName The name of the argument to retrieve the value for.
     * @return Returns the value if there is a value. If the argument cannot be found, or it doesn't have a value, it returns null.
     */
    public String getValue(String argumentName) {
        for (Argument argument : programArguments) {
            if (argument.argumentName.equals(argumentName)) {
                return argument.argumentValue;
            }
        }
        return null;
    }

    /**
     * Returns a list of the arguments that has not been marked as used.
     *
     * @return Returns a list of the arguments that has not been marked as used.
     */
    public ArgumentList unusedArguments() {
        ArgumentList argumentList = new ArgumentList();
        for (Argument argument : programArguments) {
            if (!argument.isUsed())
                argumentList.add(argument);
        }
        return argumentList;
    }

    /**
     * Returns a list of the arguments that has been marked as used.
     *
     * @return Returns a list of the arguments that has been marked as used.
     */
    public ArgumentList usedArguments() {
        ArgumentList argumentList = new ArgumentList();
        for (Argument argument : programArguments) {
            if (argument.isUsed())
                argumentList.add(argument);
        }
        return argumentList;
    }

    /**
     * Retrieves the value of the argument with the given name, and marks it as used for reference.
     *
     * @param argumentName The name of the argument. Case insensitive.
     * @return Returns the value of the argument if it has a value, and the argument can be found. Othervise it returns null.
     */
    public String use(String argumentName) {
        if (!hasArgument(argumentName)) return null;
        for (Argument argument : programArguments) {
            if (argument.argumentName.toLowerCase().equals(argumentName.trim().toLowerCase())) {
                argument.markAsUsed();
                return argument.argumentValue;
            }
        }
        return null;
    }

    /**
     * Check if the argument is registered.
     *
     * @param argument Argument name, without dashes.
     * @return Returns true if an argument with the given name can be found.
     */
    public boolean hasArgument(String argument) {
        for (Argument programArgument : programArguments) {
            if (removeInitialDashes(programArgument.argumentName.trim()).toLowerCase().equals(removeInitialDashes(argument).toLowerCase()))
                return true;
        }
        return false;
    }

    /**
     * Often alternative parameter names for the same argument is used. This method checks if any of them was given.
     *
     * @param alternativeArguments Alternatives to find.
     * @return Returns true if any of the alternative argument names are found.
     */
    public boolean hasAnyOfTheArguments(String... alternativeArguments) {
        for (String arg : alternativeArguments) {
            for (Argument argument : programArguments) {
                if (argument.argumentName.toLowerCase().equals(arg.trim().toLowerCase())) return true;
            }
        }
        return false;
    }

    /**
     * Often alternative parameter names for the same argument is used.
     * This method checks if any of them was given and return the value
     * of the first one encountered and marks the record as used.
     *
     * @param alternativeArgumentNames Alternatives to find.
     * @return Returns value if the first of the alternative argument
     * names are found. If no found or no value exist it returns null.
     */
    public String use(String... alternativeArgumentNames) {
        for (String arg : alternativeArgumentNames) {
            for (Argument argument : programArguments) {
                if (argument.argumentName.toLowerCase().equals(arg.trim().toLowerCase())) {
                    argument.markAsUsed();
                    return argument.argumentValue;
                }
            }
        }
        return null;
    }

    /**
     * Often alternative parameter names for the same argument is used.
     * This method checks if any of them was given and return the value
     * of the first one encountered.
     *
     * @param alternativeArgumentNames Alternatives to find.
     * @return Returns value if the first of the alternative argument
     * names are found. If no found or no value exist it returns null.
     */
    public String getValue(String... alternativeArgumentNames) {
        for (String arg : alternativeArgumentNames) {
            for (Argument argument : programArguments) {
                if (argument.argumentName.toLowerCase().equals(arg.trim().toLowerCase())) return argument.argumentValue;
            }
        }
        return null;
    }


    /**
     * Returns a count of the number of arguments found.
     *
     * @return Returns a count of the number of arguments found.
     */
    public int numberOfArguments() {
        return programArguments.size();
    }

    /**
     * Returns the unused arguments as strings, for custom output.
     *
     * @return Returns the unused arguments as strings, for custom output.
     */
    public List<String> unusedArgumentsAsStrings() {
        List<String> arguments = new ArrayList<>();
        for (Argument arg : unusedArguments()) {
            arguments.add(arg.toString());
        }
        return arguments;
    }


    public static class Argument {
        public String argumentName;
        public String argumentValue = null;
        public boolean used = false;

        public Argument(String argumentName) {
            this.argumentName = argumentName;
        }

        @Override
        public String toString() {
            String returnString = "[" + getClass().getSimpleName() + ": Argument name: '" + argumentName + "'";
            if (argumentValue != null)
                returnString += ", argument value: '" + argumentValue + "'";
            return returnString + ", used: " + String.valueOf(used) + "]";
        }

        public void markAsUsed() {
            used = true;
        }

        public boolean isUsed() {
            return used;
        }

        public void markAsUnused() {
            used = false;
        }
    }

    private class PropertyArgument extends Argument {
        public PropertyArgument(String propertyName, String propertyValue) {
            super(propertyName);
            this.argumentValue = propertyValue;
        }
    }

    private class UnidentifiedArgument extends Argument {
        public UnidentifiedArgument(String phrase) {
            super(phrase);
        }
    }

    private class StandaloneArgument extends Argument {
        public StandaloneArgument(String phrase) {
            super(phrase);
        }
    }

    private String joinToConsecutiveArgumentString() {
        StringBuilder sb = new StringBuilder();
        for (String part : initialArguments) {
            if (sb.toString().endsWith(" ") || part.startsWith(" ")) {
                sb.append(part);
            } else {
                sb.append(" ").append(part);
            }
        }
        return sb.toString();
    }

    private List<String> bringStringsTogether() {
        List<String> strings = new ArrayList<>();
        //String argumentString = " " + String.join(" ", initialArguments) + " ";
        String argumentString = joinToConsecutiveArgumentString();
        StringBuilder sb = new StringBuilder();
        for (int charNo = 0; charNo < argumentString.length(); charNo++) {

            sb.append(argumentString.charAt(charNo));

            if (String.valueOf(argumentString.charAt(charNo)).equals(" ") || charNo == argumentString.length() - 1) {
                strings.add(sb.toString().trim());
                sb = new StringBuilder();
                continue;
            }

            if (isPropertySeparator(String.valueOf(argumentString.charAt(charNo)))) {
                if (!isEscaped(argumentString, charNo)) {
                    String argumentName = sb.toString();
                    if (argumentName.length() > 0) argumentName = argumentName.substring(0, argumentName.length() - 1);
                    strings.add(argumentName);
                    strings.add(String.valueOf(argumentString.charAt(charNo)));
                    sb = new StringBuilder();
                    continue;
                } else {
                    String word = sb.toString();
                    sb = new StringBuilder().append(word.substring(0, word.length() - 2)).append(word.substring(word.length() - 1));
                }
            }

            //Look for first string marker. Recognize the ones with space before them or property separator next to them.
            if (!isEscaped(argumentString, charNo) && isStringSeparator(String.valueOf(argumentString.charAt(charNo))) &&
                    (
                            charNo == 0 ||
                                    String.valueOf(argumentString.charAt(charNo - 1)).equals(" ") ||
                                    isPropertySeparator(String.valueOf(argumentString.charAt(charNo - 1))) ||
                                    (
                                            argumentString.length() >= charNo + 1) &&
                                            isPropertySeparator(String.valueOf(argumentString.charAt(charNo + 1)
                                                    )
                                            )
                    )
                    ) {

                int startPosition = charNo + 1;

                //Look for second string marker. Recognize the ones followed by a space or with a property separator next to it.
                for (int secondMarkerIndex = startPosition; secondMarkerIndex < argumentString.length(); secondMarkerIndex++) {
                    if (isStringSeparator(String.valueOf(argumentString.charAt(secondMarkerIndex))) && !isEscaped(argumentString, secondMarkerIndex) &&
                            (secondMarkerIndex == argumentString.length() || //If last entry or
                                    (secondMarkerIndex > 0 && isPropertySeparator(String.valueOf(argumentString.charAt(secondMarkerIndex - 1)))) || //...has a property separator just in front of it
                                    (secondMarkerIndex < argumentString.length() &&
                                            (
                                                    isPropertySeparator(String.valueOf(argumentString.charAt(secondMarkerIndex + 1))) || //...or behind it
                                                            String.valueOf(argumentString.charAt(secondMarkerIndex + 1)).equals(" "))))) { //...or is followed by a space
                        int stopPosition = secondMarkerIndex;
                        strings.add(argumentString.substring(startPosition, stopPosition));
                        sb = new StringBuilder();
                        if (secondMarkerIndex < argumentString.length()) charNo = secondMarkerIndex + 1;
                        secondMarkerIndex = argumentString.length();
                    }
                }
            }
        }
        return strings;
    }

    private boolean isEscaped(String instring, int position) {
        if (position == 0) return false;
        if (String.valueOf(instring.charAt(position - 1)).equals("\\")) return true;
        return false;
    }

    private boolean isStringSeparator(String instring) {
        for (String separator : stringMarkers) {
            if (instring.equals(separator)) return true;
        }
        return false;
    }

    private void identifyArgumentsFromLeadingDashes() {
        for (int i = 0; i < programArguments.size(); i++) {
            if (programArguments.get(i).getClass() != UnidentifiedArgument.class) continue;
            if (programArguments.get(i).argumentName.trim().startsWith("-")) {
                programArguments.set(i, new StandaloneArgument(removeInitialDashes(programArguments.get(i).argumentName)));
            }
        }
    }

    private void identifyPropertiesFromSeparator(String separator) {
        List<Integer> indexesForRemoval = new ArrayList<>();
        for (int i = 0; i < programArguments.size(); i++) {
            Argument argument = programArguments.get(i);
            if (argument.getClass() != UnidentifiedArgument.class) continue;
            if ((argument.argumentName.contains(separator) &&
                    argument.argumentName.indexOf(separator) > 0) &&
                    !isEscaped(argument.argumentName, argument.argumentName.indexOf(separator))) {
                String[] parts = argument.argumentName.split(separator);
                String propertyName = parts[0];
                String propertyValue = null;
                if (parts.length > 0) {
                    propertyValue = argument.argumentName.substring(propertyName.length() + separator.length());
                }
                if (propertyName != null && propertyValue != null) {
                    indexesForRemoval.add(i);
                    programArguments.add(new PropertyArgument(removeInitialDashes(propertyName.trim()), propertyValue.trim()));
                }
            }
        }
        Collections.reverse(indexesForRemoval);
        for (int index : indexesForRemoval) {
            programArguments.remove(index);
        }
    }

    private boolean isPropertySeparator(String instring) {
        for (String separator : propertySeparators) {
            if (instring.trim().equals(separator)) return true;
        }
        return false;
    }

    private void identifyPropertiesFromStandaloneSeparators() {
        List<Integer> indexesForRemoval = new ArrayList<Integer>();
        for (int i = 0; i < programArguments.size(); i++) {
            //if(programArguments.get(i).getClass() != UnidentifiedArgument.class)continue;
            if (isPropertySeparator(programArguments.get(i).argumentName)) {
                if (i > 0 && programArguments.size() > i) {
                    indexesForRemoval.add(i - 1);
                    indexesForRemoval.add(i);
                    indexesForRemoval.add(i + 1);
                    programArguments.add(new PropertyArgument(removeInitialDashes(programArguments.get(i - 1).argumentName.trim()), programArguments.get(i + 1).argumentName.trim()));
                }
            }
        }
        Collections.reverse(indexesForRemoval);
        for (int index : indexesForRemoval) {
            programArguments.remove(index);
        }
    }

    private void cleanEmptyEntries() {
        List<Integer> indexesForRemoval = new ArrayList<>();
        for (int i = 0; i < programArguments.size(); i++) {
            if (programArguments.get(i).argumentName.trim().length() == 0) {
                indexesForRemoval.add(i);
            }
        }
        Collections.reverse(indexesForRemoval);
        for (int i : indexesForRemoval) {
            programArguments.remove(i);
        }
    }

    private void cleanDashedEntries() {
        List<Integer> indexesForRemoval = new ArrayList<>();
        for (int i = 0; i < programArguments.size(); i++) {
            if (!programArguments.get(i).getClass().equals(UnidentifiedArgument.class)) continue;
            if (programArguments.get(i).argumentName.trim().equals("-")) {
                indexesForRemoval.add(i);
            }
        }
        Collections.reverse(indexesForRemoval);
        for (int i : indexesForRemoval) {
            programArguments.remove(i);
        }
    }


    int numberOfArgumentsWithoutValues() {
        int count = 0;
        for (Argument argument : programArguments) {
            if (argument.argumentValue == null) {
                count++;
            }
        }
        return count;
    }

    int numberOfArgumentsWithValues() {
        int count = 0;
        for (Argument argument : programArguments) {
            if (argument.argumentValue != null) {
                count++;
            }
        }
        return count;
    }

    private ArgumentList getArgumentsWithValues() {
        ArgumentList argumentsWithValues = new ArgumentList();
        for (Argument argument : programArguments) {
            if (argument.argumentValue != null)
                argumentsWithValues.add(argument);
        }
        return argumentsWithValues;
    }

    private void identifyStringsWithSpacesSurroundedBySeparator(String separator) {
        List<Integer> indexesForRemoval = new ArrayList<>();
        for (int i = 0; i < programArguments.size(); i++) {
            String start = programArguments.get(i).argumentName.trim();
            if (start.startsWith(separator)) {
                if (start.endsWith(separator)) {
                    programArguments.set(i, new UnidentifiedArgument(start.substring(1, start.length() - 1)));
                } else {
                    for (int j = i + 1; j < programArguments.size(); j++) { //Trailing arguments
                        if (programArguments.get(j).argumentName.trim().endsWith(separator)) {
                            List<String> parts = new ArrayList<>();
                            for (int argNr = i; argNr <= j; argNr++) {
                                parts.add(programArguments.get(argNr).argumentName);
                            }
                            String argument = String.join(" ", parts);
                            argument = argument.substring(separator.length(), argument.length() - separator.length());
                            programArguments.set(i, new UnidentifiedArgument(argument));
                            for (int removeIndexes = i + 1; removeIndexes <= j; removeIndexes++) {
                                indexesForRemoval.add(removeIndexes);
                            }
                            if (programArguments.size() > j)
                                i = j + 1;
                        }
                    }
                }
            } else if (start.contains(":" + separator)) { //Property with value spanning multiple argument parts
                String[] parts = start.split(":" + separator);
                String propertyName = parts[0];
                StringBuilder sb = new StringBuilder();
                sb.append(propertyName + ":");
                sb.append(start.substring(propertyName.length() + ":".length() + separator.length()));
                for (int j = i + 1; j < programArguments.size(); j++) { //Trailing arguments
                    sb.append(" ").append(programArguments.get(j));
                    if (!programArguments.get(j).argumentName.trim().endsWith(separator)) continue;
                    String argument = sb.toString().trim();
                    argument = argument.substring(1, argument.length() - 1);
                    programArguments.set(i, new UnidentifiedArgument(argument));
                    for (int removeIndexes = i + 1; removeIndexes < j; removeIndexes++) {
                        indexesForRemoval.add(removeIndexes);
                    }
                    if (programArguments.size() > j) {
                        i = j + 1;
                    }
                }
            } else if (start.contains("=" + separator)) {

            }
        }
        Collections.reverse(indexesForRemoval);
        for (int i : indexesForRemoval) {
            programArguments.remove(i);
        }
    }

    private static String removeInitialDashes(String instring) {
        while (instring.startsWith("-")) {
            instring = instring.substring(1);
        }
        return instring;
    }

    public static class HelpSection {
        ArgumentDescriptionList argumentDescriptionList = new ArgumentDescriptionList();
        int maxNumberOfCharactersWidth = 79;
        String leadingCharacterBeforeArgumentName = "-";
        int numberOfLeadingSpacesBeforeArgumentName = 2;
        int numberOfBufferSpacesBetweenLongestArgumentNameAndDescriptionSection = 3;
        String dividerBetweenArgumentDescriptions = System.lineSeparator();
        boolean addCliHint = false;

        public HelpSection addArgument(String description, String sampleData, boolean mandatory, String... alternativeNames) {
            argumentDescriptionList.add(new ArgumentDescription(description, sampleData, alternativeNames, mandatory));
            return this;
        }

        public HelpSection addCliHint() {
            addCliHint = true;
            return this;
        }

        public HelpSection setNumberOfLeadingSpacesBeforeArgumentName(int numberOfLeadingSpacesBeforeArgumentName) {
            this.numberOfLeadingSpacesBeforeArgumentName = numberOfLeadingSpacesBeforeArgumentName;
            return this;
        }

        public HelpSection setLeadingCharacterBeforeArgumentName(String characterBeforeArgumentName) {
            this.leadingCharacterBeforeArgumentName = characterBeforeArgumentName;
            return this;
        }

        public HelpSection setDividerBetweenArgumentDescriptions(String dividerString) {
            this.dividerBetweenArgumentDescriptions = dividerString;
            return this;
        }

        public HelpSection setNumberOfBufferCharactersBetweenLongestArgumentNameAndDescriptionSection(int numberOfBufferSpacesBetweenLongestArgumentNameAndDescriptionSection) {
            this.numberOfBufferSpacesBetweenLongestArgumentNameAndDescriptionSection = numberOfBufferSpacesBetweenLongestArgumentNameAndDescriptionSection;
            return this;
        }

        public HelpSection setMaxWidthInCharacters(int maxNumberOfCharactersWidth) {
            this.maxNumberOfCharactersWidth = maxNumberOfCharactersWidth;
            return this;
        }

        private static String bufferSpaces(int count) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < count; i++) {
                sb.append(" ");
            }
            return sb.toString();
        }

        public void print() {
            System.out.println(toString());
        }


        @Override
        public String toString() {
            String returnString = "";
            StringBuilder sb = new StringBuilder();

            //CLI sample section
            if (addCliHint) {
                for (ArgumentDescription argumentDescription : argumentDescriptionList.list) {
                    if (argumentDescription.mandatory) {
                        sb.append(leadingCharacterBeforeArgumentName)
                                .append(String.join("|" + leadingCharacterBeforeArgumentName, argumentDescription.alternativeNames));
                        if (argumentDescription.sampleData == null) {
                            sb.append(" ");
                        } else {
                            sb.append("=<").append(argumentDescription.sampleData).append("> ");
                        }
                    } else {
                        sb.append("[")
                                .append(leadingCharacterBeforeArgumentName)
                                .append(String.join("|" + leadingCharacterBeforeArgumentName, argumentDescription.alternativeNames));
                        if (argumentDescription.sampleData == null) {
                            sb.append(" ");
                        } else {
                            sb.append("=<").append(argumentDescription.sampleData).append(">");
                        }
                        sb.append("] ");
                    }
                }
                sb.append(System.lineSeparator()).append(System.lineSeparator());
                returnString += sb.toString();
                sb = new StringBuilder();
            }

            //Descriptions
            List<String> descriptions = new ArrayList<>();
            int nameSectionWidth = numberOfLeadingSpacesBeforeArgumentName +
                    leadingCharacterBeforeArgumentName.length() +
                    argumentDescriptionList.lengthOfLongestArgumentName() +
                    numberOfBufferSpacesBetweenLongestArgumentNameAndDescriptionSection;
            int descriptionSectionWidth = maxNumberOfCharactersWidth - nameSectionWidth;
            for (ArgumentDescription argumentDescription : argumentDescriptionList.list) {
                int argumentNamePointer = 0;
                int descriptionWordPointer = 0;
                String[] descriptionWords = argumentDescription.description.split(" ");
                while (argumentNamePointer <= argumentDescription.alternativeNames.length - 1 ||
                        descriptionWordPointer <= descriptionWords.length) {
                    if (argumentNamePointer <= argumentDescription.alternativeNames.length - 1) {
                        sb.append(bufferSpaces(numberOfLeadingSpacesBeforeArgumentName))
                                .append(leadingCharacterBeforeArgumentName)
                                .append(argumentDescription.alternativeNames[argumentNamePointer])
                                .append(bufferSpaces(nameSectionWidth - argumentDescription.alternativeNames[argumentNamePointer].length() - leadingCharacterBeforeArgumentName.length() - numberOfLeadingSpacesBeforeArgumentName));
                    } else {
                        sb.append(bufferSpaces(nameSectionWidth));
                    }
                    argumentNamePointer++;
                    if (descriptionWordPointer <= descriptionWords.length) {
                        String row = "";
                        String rowWithNextWord = "";
                        while (rowWithNextWord.trim().length() <= descriptionSectionWidth) {
                            if (descriptionWordPointer >= descriptionWords.length) {
                                descriptionWordPointer = descriptionWords.length + 1;
                                break;
                            }
                            row += descriptionWords[descriptionWordPointer] + " ";
                            if (descriptionWordPointer + 1 < descriptionWords.length) {
                                rowWithNextWord = row + descriptionWords[descriptionWordPointer + 1];
                            } else {
                                rowWithNextWord = row;
                            }
                            descriptionWordPointer++;
                        }
                        sb.append(row.trim());
                    } else {
                        sb.append(bufferSpaces(descriptionSectionWidth));
                    }
                    sb.append(System.lineSeparator());
                }
                descriptions.add(sb.toString());
            }
            returnString += String.join(dividerBetweenArgumentDescriptions, descriptions);
            return returnString;
        }

        private class ArgumentDescriptionList {
            public List<ArgumentDescription> list = new ArrayList<>();

            public void add(ArgumentDescription argumentDescription) {
                list.add(argumentDescription);
            }

            public int lengthOfLongestArgumentName() {
                int longest = 0;
                for (ArgumentDescription argumentDescription : list) {
                    for (String alternativeName : argumentDescription.alternativeNames) {
                        if (alternativeName.length() > longest) longest = alternativeName.length();
                    }
                }
                return longest;
            }
        }

        private class ArgumentDescription {
            String description;
            String[] alternativeNames;
            String sampleData;
            boolean mandatory;

            public ArgumentDescription(String description, String sampleData, String[] alternativeNames, boolean mandatory) {
                this.description = description;
                this.sampleData = sampleData;
                this.alternativeNames = alternativeNames;
                this.mandatory = mandatory;
            }
        }
    }

    public class ArgumentList extends ArrayList<Argument>{

        @Override
        public String toString(){
            List<String> arguments = new ArrayList<>();
            for(Argument argument : this){
                arguments.add(argument.toString());
            }
            return "'" + String.join("', '", arguments) + "'";
        }

        public List<String> asListOfStrings(){
            List<String> arguments = new ArrayList<>();
            for(Argument argument : this){
                arguments.add(argument.toString());
            }
            return arguments;
        }
    }
}