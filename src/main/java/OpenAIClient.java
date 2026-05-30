import java.util.concurrent.TimeUnit;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class OpenAIClient
{
    private static final String API_KEY = System.getenv("OPENAI_API_KEY");

    public static JSONObject getToolCall(String prompt) throws Exception
    {
        OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(240, TimeUnit.SECONDS)
            .writeTimeout(240, TimeUnit.SECONDS)
            .callTimeout(240, TimeUnit.SECONDS)
            .build();

        JSONObject body = new JSONObject();
        body.put("model", "gpt-4.1-mini");
        body.put("input", prompt);
        body.put("tool_choice", "auto");

        JSONArray tools = new JSONArray();

        for (ToolSpec toolSpec : ToolRegistry.TOOLS)
        {
            JSONObject tool = new JSONObject();
            tool.put("type", "function");
            tool.put("name", toolSpec.name);
            tool.put("description", toolSpec.purpose);
            tool.put("strict", true);

            JSONObject parameters = new JSONObject();
            parameters.put("type", "object");

            JSONObject properties = new JSONObject();
            JSONArray required = new JSONArray();

            // -----------------------------
            // ADD SHEET ARGUMENTS FIRST
            // -----------------------------

            JSONObject sheetNameArg0 = new JSONObject();
            sheetNameArg0.put("type", "string");
            sheetNameArg0.put(
                "description",
                "The configured sheet name. Must be one of the allowed sheet names listed in the prompt (e.g., INTAKE, CRM)."
            );
            properties.put("sheetName", sheetNameArg0);
            required.put("sheetName");

            JSONObject tabNameArg0 = new JSONObject();
            tabNameArg0.put("type", "string");
            tabNameArg0.put(
                "description",
                "The tab name inside the selected sheet. Must match one of the allowed tabs for that sheet."
            );
            properties.put("tabName", tabNameArg0);
            required.put("tabName");

            // -----------------------------
            // ADD TOOL-SPECIFIC ARGUMENTS
            // -----------------------------

            for (ToolArgSpec arg : toolSpec.args)
            {
                JSONObject argObject = new JSONObject();
                argObject.put("type", arg.type);
                argObject.put("description", arg.description);

                properties.put(arg.name, argObject);
                required.put(arg.name);
            }

            parameters.put("properties", properties);
            parameters.put("required", required);
            parameters.put("additionalProperties", false);

            tool.put("parameters", parameters);
            tools.put(tool);
        }

        body.put("tools", tools);

        Request request = new Request.Builder()
            .url("https://api.openai.com/v1/responses")
            .addHeader("Authorization", "Bearer " + API_KEY)
            .addHeader("Content-Type", "application/json")
            .post(RequestBody.create(body.toString(), MediaType.get("application/json")))
            .build();

        Response response = client.newCall(request).execute();
        String responseBody = response.body().string();

        return new JSONObject(responseBody);
    }

    public static String getTextResponse(String prompt0) throws Exception
    {
        OkHttpClient client0 = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .callTimeout(180, TimeUnit.SECONDS)
            .build();

        JSONObject body0 = new JSONObject();
        body0.put("model", "gpt-4.1-mini");
        body0.put("input", prompt0);

        Request request0 = new Request.Builder()
            .url("https://api.openai.com/v1/responses")
            .addHeader("Authorization", "Bearer " + API_KEY)
            .addHeader("Content-Type", "application/json")
            .post(RequestBody.create(body0.toString(), MediaType.get("application/json")))
            .build();

        try (Response response0 = client0.newCall(request0).execute())
        {
            String responseBody0 = response0.body().string();

            JSONObject json0 = new JSONObject(responseBody0);

            if (!response0.isSuccessful())
            {
                System.out.println("OpenAI API Error:");
                System.out.println(json0.toString(2));
                throw new Exception("OpenAI API request failed with status: " + response0.code());
            }

            if (json0.has("error") && !json0.isNull("error"))
            {
                System.out.println("OpenAI API Error:");
                System.out.println(json0.toString(2));
                throw new Exception("OpenAI API returned an error.");
            }

            if (json0.has("output_text"))
            {
                return json0.getString("output_text");
            }

            if (!json0.has("output"))
            {
                System.out.println("Unexpected OpenAI response:");
                System.out.println(json0.toString(2));
                throw new Exception("OpenAI response missing output.");
            }

            JSONArray outputArray0 = json0.getJSONArray("output");

            for (int outputIndex0 = 0; outputIndex0 < outputArray0.length(); outputIndex0++)
            {
                JSONObject outputItem0 = outputArray0.getJSONObject(outputIndex0);

                if (outputItem0.has("content"))
                {
                    JSONArray contentArray0 = outputItem0.getJSONArray("content");

                    for (int contentIndex0 = 0; contentIndex0 < contentArray0.length(); contentIndex0++)
                    {
                        JSONObject contentItem0 = contentArray0.getJSONObject(contentIndex0);

                        if (contentItem0.has("text"))
                        {
                            return contentItem0.getString("text");
                        }
                    }
                }
            }

            System.out.println("Could not extract text from OpenAI response:");
            System.out.println(json0.toString(2));

            throw new Exception("Could not extract text from OpenAI response.");
        }
    }
}