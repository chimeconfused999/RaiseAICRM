import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Web-facing natural-language spreadsheet agent.
 *
 * Runs the same OpenAI tool-call loop the CLI used, but bound to the logged-in
 * user's spreadsheet (from the session) instead of the hardcoded SheetRegistry,
 * and returns a readable transcript instead of printing to the console.
 */
public class WebSheetAgent
{
    private static final int MAX_STEPS0 = 6;

    public static String run(SessionContext context0, String userPrompt0) throws Exception
    {
        if (context0 == null || context0.config == null)
        {
            return "ERROR: Missing session context.";
        }

        if (userPrompt0 == null || userPrompt0.trim().length() == 0)
        {
            return "ERROR: Empty request. Type what you want changed in the sheet.";
        }

        String spreadsheetId0 = context0.config.spreadsheetId;
        String mainTab0 = context0.config.mainTabName;
        String intakeTab0 = context0.config.intakeTabName;

        StringBuilder transcript0 = new StringBuilder();

        String runningPrompt0 =
            "You are a spreadsheet agent operating on ONE Google Spreadsheet for a fundraising CRM.\n"
            + "Tabs you may act on:\n"
            + "  - \"" + mainTab0 + "\": the CRM of investors (one investor per row; headers in row 1).\n"
            + (isBlank(intakeTab0) ? ""
                : "  - \"" + intakeTab0 + "\": incoming/processed investor emails (headers in row 1).\n")
            + "For EVERY function call you MUST set both \"sheetName\" and \"tabName\" to the EXACT tab name "
            + "you want to act on (one of the tab names listed above).\n"
            + "Headers are in the first row. Read the header row first (e.g. range A1:Z1) so you use the right columns.\n"
            + "For any conditional request (if / only if / unless), read the relevant value BEFORE updating.\n"
            + "When the request is fully satisfied, reply with a short plain-text confirmation and DO NOT call a function.\n\n"
            + "USER REQUEST:\n" + userPrompt0;

        String lastFingerprint0 = null;

        for (int step0 = 1; step0 <= MAX_STEPS0; step0++)
        {
            JSONObject response0 = OpenAIClient.getToolCall(runningPrompt0);

            if (response0.optJSONObject("error") != null)
            {
                transcript0.append("ERROR from OpenAI: ")
                    .append(response0.getJSONObject("error").optString("message", "unknown"))
                    .append("\n");
                return transcript0.toString();
            }

            if (!response0.has("output"))
            {
                transcript0.append("ERROR: No output returned by the model.\n");
                return transcript0.toString();
            }

            JSONArray outputArray0 = response0.getJSONArray("output");

            boolean functionCallFound0 = false;
            String assistantText0 = null;

            for (int i = 0; i < outputArray0.length(); i++)
            {
                JSONObject item0 = outputArray0.getJSONObject(i);
                String type0 = item0.optString("type", "");

                if (type0.equals("function_call"))
                {
                    functionCallFound0 = true;

                    String toolName0 = item0.getString("name");
                    String argsString0 = item0.getString("arguments");
                    JSONObject args0 = new JSONObject(argsString0);

                    ToolSpec toolSpec0 = ToolRegistry.getToolByName(toolName0);

                    if (toolSpec0 == null)
                    {
                        transcript0.append("Step ").append(step0)
                            .append(": model requested unknown tool '").append(toolName0).append("'.\n");
                        return transcript0.toString();
                    }

                    String resolvedTab0 = resolveTab(args0.optString("tabName", ""), mainTab0, intakeTab0);

                    String fingerprint0 = toolName0 + "|" + resolvedTab0 + "|" + argsString0;

                    if (fingerprint0.equals(lastFingerprint0))
                    {
                        transcript0.append("Stopping: the model repeated the same action.\n");
                        return transcript0.toString();
                    }

                    lastFingerprint0 = fingerprint0;

                    ToolCall toolCall0 = new ToolCall();
                    toolCall0.toolName = toolName0;
                    mapArguments(toolCall0, toolSpec0, args0);

                    SheetCall sheetCall0 = new SheetCall();
                    sheetCall0.sheetName0 = resolvedTab0;
                    sheetCall0.tabName0 = resolvedTab0;
                    sheetCall0.spreadsheetId0 = spreadsheetId0;

                    String result0;

                    try
                    {
                        result0 = ToolDispatcher.dispatch(toolCall0, sheetCall0);
                    }
                    catch (Exception exception0)
                    {
                        result0 = "ERROR: " + exception0.getMessage();
                    }

                    transcript0.append("Step ").append(step0).append(": ")
                        .append(toolName0).append(" on \"").append(resolvedTab0).append("\"\n")
                        .append("  -> ").append(truncate(result0, 400)).append("\n");

                    runningPrompt0 = runningPrompt0
                        + "\n\nPrevious tool call: " + toolName0
                        + " on tab \"" + resolvedTab0 + "\" with arguments " + args0.toString()
                        + ".\nResult: " + result0
                        + "\nIf the request is now fully satisfied, reply with a short confirmation and do NOT call a function. "
                        + "Otherwise call the next function.";

                    break;
                }
                else if (type0.equals("message"))
                {
                    JSONArray content0 = item0.optJSONArray("content");

                    if (content0 != null)
                    {
                        for (int c = 0; c < content0.length(); c++)
                        {
                            JSONObject contentItem0 = content0.getJSONObject(c);

                            if (contentItem0.has("text"))
                            {
                                assistantText0 = contentItem0.getString("text");
                            }
                        }
                    }
                }
            }

            if (!functionCallFound0)
            {
                if (!isBlank(assistantText0))
                {
                    transcript0.append("Done: ").append(assistantText0.trim()).append("\n");
                }
                else
                {
                    transcript0.append("Done.\n");
                }

                return transcript0.toString();
            }
        }

        transcript0.append("Stopped after ").append(MAX_STEPS0).append(" steps (step limit reached).\n");
        return transcript0.toString();
    }

    private static String resolveTab(String requested0, String mainTab0, String intakeTab0)
    {
        if (requested0 != null)
        {
            if (!isBlank(intakeTab0) && requested0.equalsIgnoreCase(intakeTab0))
            {
                return intakeTab0;
            }

            if (!isBlank(mainTab0) && requested0.equalsIgnoreCase(mainTab0))
            {
                return mainTab0;
            }
        }

        return mainTab0;
    }

    private static String truncate(String value0, int max0)
    {
        if (value0 == null)
        {
            return "";
        }

        if (value0.length() <= max0)
        {
            return value0;
        }

        return value0.substring(0, max0) + "...";
    }

    private static boolean isBlank(String value0)
    {
        return value0 == null || value0.trim().length() == 0;
    }

    private static void mapArguments(ToolCall toolCall0, ToolSpec toolSpec0, JSONObject args0)
    {
        int strIndex0 = 1;
        int intIndex0 = 11;
        int doubIndex0 = 21;

        for (ToolArgSpec argSpec0 : toolSpec0.args)
        {
            Object raw0 = args0.has(argSpec0.name) && !args0.isNull(argSpec0.name)
                ? args0.get(argSpec0.name)
                : null;

            if (argSpec0.type.equals("string"))
            {
                String value0 = raw0 == null ? null : raw0.toString();

                if (strIndex0 == 1) toolCall0.arg1 = value0;
                else if (strIndex0 == 2) toolCall0.arg2 = value0;
                else if (strIndex0 == 3) toolCall0.arg3 = value0;
                else if (strIndex0 == 4) toolCall0.arg4 = value0;
                else if (strIndex0 == 5) toolCall0.arg5 = value0;
                else if (strIndex0 == 6) toolCall0.arg6 = value0;
                else if (strIndex0 == 7) toolCall0.arg7 = value0;
                else if (strIndex0 == 8) toolCall0.arg8 = value0;
                else if (strIndex0 == 9) toolCall0.arg9 = value0;
                else if (strIndex0 == 10) toolCall0.arg10 = value0;

                strIndex0++;
            }
            else if (argSpec0.type.equals("integer"))
            {
                Integer value0 = raw0 == null ? null : ((Number) raw0).intValue();

                if (intIndex0 == 11) toolCall0.arg11 = value0;
                else if (intIndex0 == 12) toolCall0.arg12 = value0;
                else if (intIndex0 == 13) toolCall0.arg13 = value0;
                else if (intIndex0 == 14) toolCall0.arg14 = value0;
                else if (intIndex0 == 15) toolCall0.arg15 = value0;
                else if (intIndex0 == 16) toolCall0.arg16 = value0;
                else if (intIndex0 == 17) toolCall0.arg17 = value0;
                else if (intIndex0 == 18) toolCall0.arg18 = value0;
                else if (intIndex0 == 19) toolCall0.arg19 = value0;
                else if (intIndex0 == 20) toolCall0.arg20 = value0;

                intIndex0++;
            }
            else if (argSpec0.type.equals("double"))
            {
                Double value0 = raw0 == null ? null : ((Number) raw0).doubleValue();

                if (doubIndex0 == 21) toolCall0.arg21 = value0;
                else if (doubIndex0 == 22) toolCall0.arg22 = value0;
                else if (doubIndex0 == 23) toolCall0.arg23 = value0;
                else if (doubIndex0 == 24) toolCall0.arg24 = value0;
                else if (doubIndex0 == 25) toolCall0.arg25 = value0;

                doubIndex0++;
            }
        }
    }
}
