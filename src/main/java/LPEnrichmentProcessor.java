import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

public class LPEnrichmentProcessor
{
    private static final int MAX_CRM_ROWS0 = 500;
    private static final int MAX_COLUMNS0 = 200;
    private static final int MAX_ROWS_TO_ENRICH0 = 4;
    private static final int MAX_TEXT_CHARS0 = 20000;

    private static final String STATUS_QUEUED0 = "QUEUED";
    private static final String STATUS_COMPLETED0 = "COMPLETED";
    private static final String STATUS_FAILED0 = "FAILED";
    private static final String STATUS_FAILED_RETRY0 = "FAILED_RETRY";

    private static final int UPDATE_ALLOCATOR_TYPE0 = 0;
    private static final int UPDATE_SECTOR_TAGS0 = 1;
    private static final int UPDATE_MICROSECTOR_TAGS0 = 2;
    private static final int UPDATE_GEOGRAPHY0 = 3;
    private static final int UPDATE_PRIOR_BACKED_FUNDS0 = 4;
    private static final int UPDATE_INTELLIGENCE_JSON0 = 5;
    private static final int UPDATE_LAST_ENRICHED_AT0 = 6;
    private static final int UPDATE_ENRICHMENT_STATUS0 = 7;
    private static final int UPDATE_FIELD_COUNT0 = 8;

    public static String enrichLpRows(SessionContext context0) throws Exception
    {
        if (context0 == null || context0.config == null)
        {
            return "ERROR: Missing session context or config.";
        }

        String spreadsheetId0 = context0.config.spreadsheetId;
        String crmTabName0 = context0.config.mainTabName;

        HashMap<String, Integer> crmHeaderMap0 = SheetsApp.buildHeaderMap(
            spreadsheetId0,
            crmTabName0,
            context0.config.mainTabHeaderRow,
            MAX_COLUMNS0
        );

        int websiteCol0 = SheetsApp.findColumnInHeaderMap(
            crmHeaderMap0,
            context0.config.mainTabWebsiteCol
        );

        int enrichmentStatusCol0 = SheetsApp.findColumnInHeaderMap(
            crmHeaderMap0,
            context0.config.mainTabEnrichmentStatusCol
        );

        int[] updateCols0 = buildUpdateColumns(context0, crmHeaderMap0);

        if (websiteCol0 == -1 || enrichmentStatusCol0 == -1 || hasMissingColumn(updateCols0))
        {
            return "ERROR: Missing required LP enrichment columns.";
        }

        int startRow0 = context0.config.mainTabDataStartRow;

        String[][] websiteColumn0 = SheetsApp.readRangeMatrix(
            spreadsheetId0,
            crmTabName0,
            startRow0,
            websiteCol0,
            MAX_CRM_ROWS0,
            websiteCol0
        );

        String[][] statusColumn0 = SheetsApp.readRangeMatrix(
            spreadsheetId0,
            crmTabName0,
            startRow0,
            enrichmentStatusCol0,
            MAX_CRM_ROWS0,
            enrichmentStatusCol0
        );

        LinkedHashMap<Integer, String> rowsToProcess0 = selectRowsForProcessing(
            websiteColumn0,
            statusColumn0,
            startRow0
        );

        if (rowsToProcess0.size() == 0)
        {
            return "LP enrichment complete. No eligible rows found.";
        }

        LinkedHashMap<Integer, String[]> rowUpdates0 = new LinkedHashMap<Integer, String[]>();

        int processedAttemptCount0 = 0;
        int completedCount0 = 0;
        int failedCount0 = 0;

        for (Map.Entry<Integer, String> entry0 : rowsToProcess0.entrySet())
        {
            if (processedAttemptCount0 >= MAX_ROWS_TO_ENRICH0)
            {
                break;
            }

            int rowNumber0 = entry0.getKey();
            String website0 = entry0.getValue();

            processedAttemptCount0++;

            try
            {
                System.out.println("Enriching row " + rowNumber0 + ": " + website0);

                String[] updateValues0 = enrichSingleWebsite(website0);

                rowUpdates0.put(rowNumber0, updateValues0);

                completedCount0++;
            }
            catch (Exception exception0)
            {
                String[] failedUpdate0 = buildFailedUpdate(
                    website0,
                    exception0.getMessage()
                );

                rowUpdates0.put(rowNumber0, failedUpdate0);

                failedCount0++;

                System.out.println("Failed row " + rowNumber0 + ": " + exception0.getMessage());
            }
        }

        if (rowUpdates0.size() == 0)
        {
            return "LP enrichment complete. No updates prepared.";
        }

        executeColumnBatchUpdates(
            spreadsheetId0,
            crmTabName0,
            updateCols0,
            rowUpdates0
        );

        return "LP enrichment complete. Completed: "
            + completedCount0
            + ", Failed: "
            + failedCount0
            + ".";
    }

    private static LinkedHashMap<Integer, String> selectRowsForProcessing(
        String[][] websiteColumn0,
        String[][] statusColumn0,
        int startRow0)
    {
        LinkedHashMap<Integer, String> rowsToProcess0 = new LinkedHashMap<Integer, String>();

        int rowCount0 = Math.max(websiteColumn0.length, statusColumn0.length);

        for (int i = 0; i < rowCount0; i++)
        {
            String website0 = getLocalColumnValue(websiteColumn0, i);
            String status0 = getLocalColumnValue(statusColumn0, i);

            int actualSheetRowNumber0 = startRow0 + i;

            boolean hasWebsite0 = !isBlank(website0);
            boolean statusEligible0 = shouldEnrich(status0);
            boolean selected0 = hasWebsite0 && statusEligible0;

            System.out.println(
                "Row " + actualSheetRowNumber0
                + " | website=[" + website0 + "]"
                + " | status=[" + status0 + "]"
                + " | hasWebsite=" + hasWebsite0
                + " | statusEligible=" + statusEligible0
                + " | SELECTED=" + selected0
            );

            if (!selected0)
            {
                continue;
            }

            rowsToProcess0.put(actualSheetRowNumber0, website0);
        }

        return rowsToProcess0;
    }

    private static String[] enrichSingleWebsite(String website0) throws Exception
    {
        String normalizedWebsite0 = WebsiteCrawlerService.normalizeRootUrl(website0);

        LinkedHashMap<String, String> scrapedPages0 =
            WebsiteCrawlerService.crawlWebsite(normalizedWebsite0);

        System.out.println("Website crawl complete. Pages scraped: " + scrapedPages0.size());

        String sourceText0 = buildOpenAiSourceText(scrapedPages0);

        System.out.println(
            "Sending "
            + sourceText0.length()
            + " characters to OpenAI..."
        );


        JSONObject intelligenceJson0 = analyzeWithOpenAI(
            normalizedWebsite0,
            sourceText0
        );

        String now0 = java.time.Instant.now().toString();

        forceMetadata(
            intelligenceJson0,
            now0,
            STATUS_COMPLETED0
        );

        String[] updateValues0 = new String[UPDATE_FIELD_COUNT0];

        updateValues0[UPDATE_ALLOCATOR_TYPE0] = extractAllocatorType(intelligenceJson0);
        updateValues0[UPDATE_SECTOR_TAGS0] = extractSectorTags(intelligenceJson0);
        updateValues0[UPDATE_MICROSECTOR_TAGS0] = extractMicrosectorTags(intelligenceJson0);
        updateValues0[UPDATE_GEOGRAPHY0] = extractGeography(intelligenceJson0);
        updateValues0[UPDATE_PRIOR_BACKED_FUNDS0] = extractPriorBackedFunds(intelligenceJson0);
        updateValues0[UPDATE_INTELLIGENCE_JSON0] = intelligenceJson0.toString();
        updateValues0[UPDATE_LAST_ENRICHED_AT0] = now0;
        updateValues0[UPDATE_ENRICHMENT_STATUS0] = STATUS_COMPLETED0;

        return updateValues0;
    }

    private static void executeColumnBatchUpdates(
        String spreadsheetId0,
        String crmTabName0,
        int[] updateCols0,
        LinkedHashMap<Integer, String[]> rowUpdates0) throws Exception
    {
        int minRow0 = findMinKey(rowUpdates0);
        int maxRow0 = findMaxKey(rowUpdates0);
        int rowCount0 = maxRow0 - minRow0 + 1;

        String[][][] columnUpdateData0 = new String[UPDATE_FIELD_COUNT0][rowCount0][1];

        for (int columnIndex0 = 0; columnIndex0 < UPDATE_FIELD_COUNT0; columnIndex0++)
        {
            columnUpdateData0[columnIndex0] = SheetsApp.readRangeMatrix(
                spreadsheetId0,
                crmTabName0,
                minRow0,
                updateCols0[columnIndex0],
                maxRow0,
                updateCols0[columnIndex0]
            );
        }

        for (Map.Entry<Integer, String[]> entry0 : rowUpdates0.entrySet())
        {
            int rowNumber0 = entry0.getKey();
            String[] updateValues0 = entry0.getValue();

            int localRowIndex0 = rowNumber0 - minRow0;

            for (int fieldIndex0 = 0; fieldIndex0 < updateValues0.length; fieldIndex0++)
            {
                columnUpdateData0[fieldIndex0][localRowIndex0][0] =
                    updateValues0[fieldIndex0] == null ? "" : updateValues0[fieldIndex0];
            }
        }

        for (int columnIndex0 = 0; columnIndex0 < UPDATE_FIELD_COUNT0; columnIndex0++)
        {
            SheetsApp.updateRangeMatrix(
                spreadsheetId0,
                crmTabName0,
                minRow0,
                updateCols0[columnIndex0],
                columnUpdateData0[columnIndex0]
            );
        }
    }

    private static int[] buildUpdateColumns(
        SessionContext context0,
        HashMap<String, Integer> crmHeaderMap0)
    {
        String[] headers0 = new String[]
        {
            context0.config.mainTabTypeOfInvestorCol,
            context0.config.mainTabSectorTagsCol,
            context0.config.mainTabMicrosectorTagsCol,
            context0.config.mainTabGeographyCol,
            context0.config.mainTabPriorBackedFundsCol,
            context0.config.mainTabIntelligenceJsonCol,
            context0.config.mainTabLastEnrichedAtCol,
            context0.config.mainTabEnrichmentStatusCol
        };

        int[] columns0 = new int[headers0.length];

        for (int i = 0; i < headers0.length; i++)
        {
            columns0[i] = SheetsApp.findColumnInHeaderMap(
                crmHeaderMap0,
                headers0[i]
            );

            if (columns0[i] == -1)
            {
                System.out.println("Header not found: " + headers0[i]);
            }
        }

        return columns0;
    }

    private static String buildOpenAiSourceText(
        LinkedHashMap<String, String> scrapedPages0)
    {
        StringBuilder builder0 = new StringBuilder();

        int pageCount0 = 0;

        for (String pageUrl0 : scrapedPages0.keySet())
        {
            String html0 = scrapedPages0.get(pageUrl0);
            String cleanText0 = WebsiteCrawlerService.extractVisibleText(html0);

            if (isBlank(cleanText0))
            {
                continue;
            }

            builder0.append("\n\n===== SOURCE PAGE ")
                .append(pageCount0 + 1)
                .append(" =====\n");

            builder0.append("URL: ")
                .append(pageUrl0)
                .append("\n\n");

            builder0.append(cleanText0);

            pageCount0++;

            if (builder0.length() >= MAX_TEXT_CHARS0)
            {
                break;
            }
        }

        String sourceText0 = builder0.toString();

        if (sourceText0.length() > MAX_TEXT_CHARS0)
        {
            sourceText0 = sourceText0.substring(0, MAX_TEXT_CHARS0);
        }

        return sourceText0;
    }

    private static JSONObject analyzeWithOpenAI(
        String websiteUrl0,
        String sourceText0) throws Exception
    {
        String prompt0 =
            "You are an LP intelligence extraction engine for a VC CRM.\n"
            + "Extract investor/allocator details from scraped website text.\n\n"
            + "Return ONLY valid JSON. No markdown. No explanation.\n\n"
            + "Use exactly this JSON structure:\n"
            + "{\n"
            + "  \"allocator_profile\": {\"allocator_type\": {\"value\": \"\", \"confidence\": 0.0}},\n"
            + "  \"sector_focus\": {\"sector_tags\": [{\"value\": \"\", \"confidence\": 0.0}]},\n"
            + "  \"microsector_focus\": {\"microsector_tags\": [{\"value\": \"\", \"confidence\": 0.0}]},\n"
            + "  \"prior_relationships\": {\"prior_backed_funds\": [{\"name\": \"\", \"confidence\": 0.0}]},\n"
            + "  \"geography\": {\"locations\": [{\"value\": \"\", \"confidence\": 0.0}]},\n"
            + "  \"evidence\": {\"allocator_type\": [], \"sector_focus\": [], \"microsector_focus\": [], \"prior_relationships\": [], \"geography\": []},\n"
            + "  \"search_generation\": {},\n"
            + "  \"metadata\": {\"analysis_version\": \"lp_intelligence_v1\", \"last_analyzed_at\": \"\", \"confidence_score\": 0.0, \"brightdata_snapshot_id\": \"\", \"analysis_status\": \"completed\"}\n"
            + "}\n\n"
            + "Rules:\n"
            + "1. allocator_type should use values like Family Office, Foundation, Corporation, Fund of Funds, Endowment, Pension, Nonprofit, Government, Unknown.\n"
            + "2. sector_tags should be broad categories like Healthcare, AI, Climate, Education, Fintech, Enterprise, Consumer, Impact, Deep Tech.\n"
            + "3. microsector_tags should be more specific.\n"
            + "4. prior_backed_funds should only include funds/managers clearly supported by the text.\n"
            + "5. geography should include locations or allocation regions supported by the text.\n"
            + "6. If unknown, return empty arrays or blank values with low confidence.\n"
            + "7. Evidence arrays should contain objects with source_url and quote fields when possible.\n\n"
            + "Website URL: " + websiteUrl0 + "\n\n"
            + "Scraped source text:\n"
            + sourceText0;

        String aiText0 = OpenAIClient.getTextResponse(prompt0);

        System.out.println("OpenAI response received.");

        return parseJsonObjectFromText(aiText0);
    }

    private static String[] buildFailedUpdate(String website0, String errorMessage0)
    {
        String[] failedUpdate0 = new String[UPDATE_FIELD_COUNT0];

        failedUpdate0[UPDATE_ALLOCATOR_TYPE0] = "";
        failedUpdate0[UPDATE_SECTOR_TAGS0] = "";
        failedUpdate0[UPDATE_MICROSECTOR_TAGS0] = "";
        failedUpdate0[UPDATE_GEOGRAPHY0] = "";
        failedUpdate0[UPDATE_PRIOR_BACKED_FUNDS0] = "";
        failedUpdate0[UPDATE_INTELLIGENCE_JSON0] = buildFailureJson(website0, errorMessage0).toString();
        failedUpdate0[UPDATE_LAST_ENRICHED_AT0] = java.time.Instant.now().toString();
        failedUpdate0[UPDATE_ENRICHMENT_STATUS0] = STATUS_FAILED0;

        return failedUpdate0;
    }

    private static JSONObject buildFailureJson(String website0, String errorMessage0)
    {
        JSONObject root0 = new JSONObject();

        root0.put("allocator_profile", new JSONObject().put("allocator_type", new JSONObject().put("value", "").put("confidence", 0.0)));
        root0.put("sector_focus", new JSONObject().put("sector_tags", new JSONArray()));
        root0.put("microsector_focus", new JSONObject().put("microsector_tags", new JSONArray()));
        root0.put("prior_relationships", new JSONObject().put("prior_backed_funds", new JSONArray()));
        root0.put("geography", new JSONObject().put("locations", new JSONArray()));
        root0.put("evidence", new JSONObject()
            .put("allocator_type", new JSONArray())
            .put("sector_focus", new JSONArray())
            .put("microsector_focus", new JSONArray())
            .put("prior_relationships", new JSONArray())
            .put("geography", new JSONArray()));
        root0.put("search_generation", new JSONObject());
        root0.put("metadata", new JSONObject()
            .put("analysis_version", "lp_intelligence_v1")
            .put("last_analyzed_at", java.time.Instant.now().toString())
            .put("confidence_score", 0.0)
            .put("brightdata_snapshot_id", "")
            .put("analysis_status", "failed")
            .put("source_website", website0)
            .put("error_message", errorMessage0 == null ? "" : errorMessage0));

        return root0;
    }

    private static void forceMetadata(JSONObject intelligenceJson0, String now0, String status0)
    {
        JSONObject metadata0 = intelligenceJson0.optJSONObject("metadata");

        if (metadata0 == null)
        {
            metadata0 = new JSONObject();
            intelligenceJson0.put("metadata", metadata0);
        }

        metadata0.put("analysis_version", "lp_intelligence_v1");
        metadata0.put("last_analyzed_at", now0);
        metadata0.put("analysis_status", status0.toLowerCase());
    }

    private static String extractAllocatorType(JSONObject intelligenceJson0)
    {
        JSONObject allocatorProfile0 = intelligenceJson0.optJSONObject("allocator_profile");
        if (allocatorProfile0 == null) return "";

        JSONObject allocatorType0 = allocatorProfile0.optJSONObject("allocator_type");
        if (allocatorType0 == null) return "";

        return allocatorType0.optString("value", "");
    }

    private static String extractSectorTags(JSONObject intelligenceJson0)
    {
        JSONObject sectorFocus0 = intelligenceJson0.optJSONObject("sector_focus");
        if (sectorFocus0 == null) return "";

        return joinJsonArrayValues(sectorFocus0.optJSONArray("sector_tags"), "value");
    }

    private static String extractMicrosectorTags(JSONObject intelligenceJson0)
    {
        JSONObject microsectorFocus0 = intelligenceJson0.optJSONObject("microsector_focus");
        if (microsectorFocus0 == null) return "";

        return joinJsonArrayValues(microsectorFocus0.optJSONArray("microsector_tags"), "value");
    }

    private static String extractGeography(JSONObject intelligenceJson0)
    {
        JSONObject geography0 = intelligenceJson0.optJSONObject("geography");
        if (geography0 == null) return "";

        return joinJsonArrayValues(geography0.optJSONArray("locations"), "value");
    }

    private static String extractPriorBackedFunds(JSONObject intelligenceJson0)
    {
        JSONObject priorRelationships0 = intelligenceJson0.optJSONObject("prior_relationships");
        if (priorRelationships0 == null) return "";

        return joinJsonArrayValues(priorRelationships0.optJSONArray("prior_backed_funds"), "name");
    }

    private static String joinJsonArrayValues(JSONArray array0, String key0)
    {
        if (array0 == null) return "";

        ArrayList<String> values0 = new ArrayList<String>();

        for (int i = 0; i < array0.length(); i++)
        {
            JSONObject object0 = array0.optJSONObject(i);

            if (object0 == null) continue;

            String value0 = object0.optString(key0, "").trim();

            if (!isBlank(value0))
            {
                values0.add(value0);
            }
        }

        return joinWithPipe(values0);
    }

    private static JSONObject parseJsonObjectFromText(String text0)
    {
        String trimmedText0 = text0.trim();

        try
        {
            return new JSONObject(trimmedText0);
        }
        catch (Exception exception0)
        {
            int startIndex0 = trimmedText0.indexOf("{");
            int endIndex0 = trimmedText0.lastIndexOf("}");

            if (startIndex0 == -1 || endIndex0 == -1 || endIndex0 <= startIndex0)
            {
                throw exception0;
            }

            return new JSONObject(trimmedText0.substring(startIndex0, endIndex0 + 1));
        }
    }

    private static boolean shouldEnrich(String status0)
    {
        if (isBlank(status0)) return true;

        return status0.equalsIgnoreCase(STATUS_QUEUED0)
            || status0.equalsIgnoreCase(STATUS_FAILED_RETRY0);
    }

    private static String getLocalColumnValue(String[][] columnData0, int rowIndex0)
    {
        if (columnData0 == null ||
            rowIndex0 < 0 ||
            rowIndex0 >= columnData0.length ||
            columnData0[rowIndex0].length == 0 ||
            columnData0[rowIndex0][0] == null)
        {
            return "";
        }

        return columnData0[rowIndex0][0].trim();
    }

    private static boolean hasMissingColumn(int[] columns0)
    {
        if (columns0 == null) return true;

        for (int i = 0; i < columns0.length; i++)
        {
            if (columns0[i] == -1) return true;
        }

        return false;
    }

    private static int findMinKey(LinkedHashMap<Integer, String[]> map0)
    {
        int min0 = Integer.MAX_VALUE;

        for (Integer key0 : map0.keySet())
        {
            if (key0 < min0) min0 = key0;
        }

        return min0;
    }

    private static int findMaxKey(LinkedHashMap<Integer, String[]> map0)
    {
        int max0 = -1;

        for (Integer key0 : map0.keySet())
        {
            if (key0 > max0) max0 = key0;
        }

        return max0;
    }

    private static String joinWithPipe(ArrayList<String> values0)
    {
        String result0 = "";

        for (int i = 0; i < values0.size(); i++)
        {
            if (i > 0) result0 += "|";
            result0 += values0.get(i);
        }

        return result0;
    }

    private static boolean isBlank(String value0)
    {
        return value0 == null || value0.trim().length() == 0;
    }
}