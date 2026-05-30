import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;

public class CRMOnboard
{
    private static final int SCAN_HEADER_ROW0 = 1;
    private static final int SCAN_START_COL0 = 1;
    private static final int SCAN_END_COL0 = 150;

    public static SessionContext onboardUser(
        String userId0,
        String email0,
        String fundName0,
        String spreadsheetId0,
        ArrayList<String> internalNames0,
        ArrayList<String> internalEmails0,
        String internalFundName0,
        String internalWebsite0,
        String clientSectorTags0,
        String clientMicrosectorTags0,
        String clientGeography0,
        String clientInvestmentThesis0,
        String clientProfileJson0,
        String[] possibleTabNames0
    ) throws Exception
    {
        JSONArray scannedTabs0 = scanTabs(spreadsheetId0, possibleTabNames0);

        JSONObject schemaResult0 = detectSchemaWithAI(scannedTabs0);

        String configId0 = "config_" + userId0;

        CRMSchemaConfig config0 = buildConfigFromSchema(
            configId0,
            userId0,
            fundName0,
            spreadsheetId0,
            schemaResult0
        );

        ensureSystemIntakeColumnsExist(spreadsheetId0, config0);
        ensureRequiredMainCrmColumnsExist(spreadsheetId0, config0);

        UserAccount user0 = new UserAccount(
            userId0,
            email0,
            fundName0,
            configId0,
            internalNames0,
            internalEmails0,
            internalFundName0,
            internalWebsite0,
            "",
            clientSectorTags0,
            clientMicrosectorTags0,
            clientGeography0,
            clientInvestmentThesis0,
            clientProfileJson0
        );

        CRMRegistry.registerUser(user0, config0);

        return new SessionContext(user0, config0);
    }

    private static JSONArray scanTabs(
        String spreadsheetId0,
        String[] possibleTabNames0
    ) throws Exception
    {
        JSONArray tabsArray0 = new JSONArray();

        for (String tabName0 : possibleTabNames0)
        {
            String[][] headerData0 = SheetsApp.readRangeMatrix(
                spreadsheetId0,
                tabName0,
                SCAN_HEADER_ROW0,
                SCAN_START_COL0,
                SCAN_HEADER_ROW0,
                SCAN_END_COL0
            );

            JSONArray headers0 = new JSONArray();

            for (int colIndex0 = 0; colIndex0 < headerData0[0].length; colIndex0++)
            {
                String header0 = headerData0[0][colIndex0];

                if (header0 != null && header0.trim().length() > 0)
                {
                    headers0.put(header0.trim());
                }
            }

            JSONObject tabObject0 = new JSONObject();
            tabObject0.put("tabName", tabName0);
            tabObject0.put("headers", headers0);

            tabsArray0.put(tabObject0);
        }

        return tabsArray0;
    }

    private static JSONObject detectSchemaWithAI(JSONArray scannedTabs0) throws Exception
    {
        String prompt0 =
            "You are configuring an AI CRM system for a Venture Capital fund.\n"
            + "Given spreadsheet tabs and headers, identify the main CRM tab, email intake tab, and column mappings.\n\n"
            + "Return ONLY valid JSON. No markdown. No explanation.\n\n"
            + "Return exactly this JSON structure:\n"
            + "{\n"
            + "  \"mainTabName\": \"\",\n"
            + "  \"intakeTabName\": \"\",\n"
            + "  \"mainTabMappings\": {\n"
            + "    \"mainTabFundNameCol\": \"\",\n"
            + "    \"mainTabContact1FirstNameCol\": \"\",\n"
            + "    \"mainTabContact1LastNameCol\": \"\",\n"
            + "    \"mainTabContact2FirstNameCol\": \"\",\n"
            + "    \"mainTabContact2LastNameCol\": \"\",\n"
            + "    \"mainTabContact1EmailCol\": \"\",\n"
            + "    \"mainTabContact2EmailCol\": \"\",\n"
            + "    \"mainTabContact1PositionCol\": \"\",\n"
            + "    \"mainTabNotesCol\": \"\",\n"
            + "    \"mainTabTypeOfInvestorCol\": \"\",\n"
            + "    \"mainTabStatusCol\": \"\",\n"
            + "    \"mainTabColdEmailCol\": \"\",\n"
            + "    \"mainTabLinkedInCol\": \"\",\n"
            + "    \"mainTabNameLinkedInQueryCol\": \"\",\n"
            + "    \"mainTabCompanyLinkedInQueryCol\": \"\",\n"
            + "    \"mainTabLinkedIn2Col\": \"\",\n"
            + "    \"mainTabWebsiteCol\": \"\",\n"
            + "    \"mainTabInvestorProfileSimilarityCol\": \"\",\n"
            + "    \"mainTabCommentsCol\": \"\",\n"
            + "    \"mainTabCountryCol\": \"\",\n"
            + "    \"mainTabCityCol\": \"\",\n"
            + "    \"mainTabLastContactDateCol\": \"\",\n"
            + "    \"mainTabNextActionCol\": \"\",\n"
            + "    \"mainTabPriorityScoreCol\": \"\",\n"
            + "    \"mainTabPriorityReasonCol\": \"\",\n"
            + "    \"mainTabPriorityLastCalculatedAtCol\": \"\",\n"
            + "    \"mainTabContact2PositionCol\": \"\",\n"
            + "    \"mainTabInteractionHistoryCol\": \"\",\n"
            + "    \"mainTabSectorTagsCol\": \"\",\n"
            + "    \"mainTabMicrosectorTagsCol\": \"\",\n"
            + "    \"mainTabGeographyCol\": \"\",\n"
            + "    \"mainTabPriorBackedFundsCol\": \"\",\n"
            + "    \"mainTabIntelligenceJsonCol\": \"\",\n"
            + "    \"mainTabLastEnrichedAtCol\": \"\",\n"
            + "    \"mainTabInvestmentThesisCol\": \"\",\n"
            + "    \"mainTabEnrichmentStatusCol\": \"\"\n"
            + "  },\n"
            + "  \"intakeTabMappings\": {\n"
            + "    \"intakeTabIntakeIdCol\": \"\",\n"
            + "    \"intakeTabGmailMessageIdCol\": \"\",\n"
            + "    \"intakeTabGmailThreadIdCol\": \"\",\n"
            + "    \"intakeTabIntakeTypeCol\": \"\",\n"
            + "    \"intakeTabTimestampCol\": \"\",\n"
            + "    \"intakeTabToCol\": \"\",\n"
            + "    \"intakeTabFromCol\": \"\",\n"
            + "    \"intakeTabSubjectCol\": \"\",\n"
            + "    \"intakeTabBodyCol\": \"\"\n"
            + "  }\n"
            + "}\n\n"
            + "Rules:\n"
            + "1. mainTabName should be the tab that looks like the fund's CRM/investor database.\n"
            + "2. intakeTabName should be the tab that looks like raw email or communication intake.\n"
            + "3. Use exact header names from the input.\n"
            + "4. If a field is not found, return an empty string for that field.\n"
            + "5. Do not invent headers.\n"
            + "6. mainTabTypeOfInvestorCol is used as the allocator type column.\n\n"
            + "Tabs and headers:\n"
            + scannedTabs0.toString(2);

        String aiText0 = OpenAIClient.getTextResponse(prompt0);

        return parseJsonObjectFromText(aiText0);
    }

    private static CRMSchemaConfig buildConfigFromSchema(
        String configId0,
        String userId0,
        String crmName0,
        String spreadsheetId0,
        JSONObject schemaResult0
    )
    {
        CRMSchemaConfig config0 = new CRMSchemaConfig(
            configId0,
            userId0,
            crmName0,
            spreadsheetId0
        );

        config0.mainTabName = schemaResult0.optString("mainTabName", "");
        config0.intakeTabName = schemaResult0.optString("intakeTabName", "");

        JSONObject main0 = schemaResult0.optJSONObject("mainTabMappings");
        JSONObject intake0 = schemaResult0.optJSONObject("intakeTabMappings");

        config0.mainTabHeaderRow = 1;
        config0.mainTabDataStartRow = 2;
        config0.intakeTabHeaderRow = 1;
        config0.intakeTabDataStartRow = 2;

        if (main0 != null)
        {
            config0.mainTabFundNameCol = main0.optString("mainTabFundNameCol", "");
            config0.mainTabContact1FirstNameCol = main0.optString("mainTabContact1FirstNameCol", "");
            config0.mainTabContact1LastNameCol = main0.optString("mainTabContact1LastNameCol", "");
            config0.mainTabContact2FirstNameCol = main0.optString("mainTabContact2FirstNameCol", "");
            config0.mainTabContact2LastNameCol = main0.optString("mainTabContact2LastNameCol", "");
            config0.mainTabContact1EmailCol = main0.optString("mainTabContact1EmailCol", "");
            config0.mainTabContact2EmailCol = main0.optString("mainTabContact2EmailCol", "");
            config0.mainTabContact1PositionCol = main0.optString("mainTabContact1PositionCol", "");
            config0.mainTabNotesCol = main0.optString("mainTabNotesCol", "");
            config0.mainTabTypeOfInvestorCol = main0.optString("mainTabTypeOfInvestorCol", "");
            config0.mainTabStatusCol = main0.optString("mainTabStatusCol", "");
            config0.mainTabColdEmailCol = main0.optString("mainTabColdEmailCol", "");
            config0.mainTabLinkedInCol = main0.optString("mainTabLinkedInCol", "");
            config0.mainTabNameLinkedInQueryCol = main0.optString("mainTabNameLinkedInQueryCol", "");
            config0.mainTabCompanyLinkedInQueryCol = main0.optString("mainTabCompanyLinkedInQueryCol", "");
            config0.mainTabLinkedIn2Col = main0.optString("mainTabLinkedIn2Col", "");
            config0.mainTabWebsiteCol = main0.optString("mainTabWebsiteCol", "");
            config0.mainTabInvestorProfileSimilarityCol = main0.optString("mainTabInvestorProfileSimilarityCol", "");
            config0.mainTabCommentsCol = main0.optString("mainTabCommentsCol", "");
            config0.mainTabCountryCol = main0.optString("mainTabCountryCol", "");
            config0.mainTabCityCol = main0.optString("mainTabCityCol", "");
            config0.mainTabLastContactDateCol = main0.optString("mainTabLastContactDateCol", "");
            config0.mainTabNextActionCol = main0.optString("mainTabNextActionCol", "");
            config0.mainTabPriorityScoreCol = main0.optString("mainTabPriorityScoreCol", "");
            config0.mainTabPriorityReasonCol = main0.optString("mainTabPriorityReasonCol", "");
            config0.mainTabPriorityLastCalculatedAtCol = main0.optString("mainTabPriorityLastCalculatedAtCol", "");
            config0.mainTabInteractionHistoryCol = main0.optString("mainTabInteractionHistoryCol", "");
            config0.mainTabContact2PositionCol = main0.optString("mainTabContact2PositionCol", "");

            config0.mainTabSectorTagsCol = main0.optString("mainTabSectorTagsCol", "");
            config0.mainTabMicrosectorTagsCol = main0.optString("mainTabMicrosectorTagsCol", "");
            config0.mainTabGeographyCol = main0.optString("mainTabGeographyCol", "");
            config0.mainTabPriorBackedFundsCol = main0.optString("mainTabPriorBackedFundsCol", "");
            config0.mainTabIntelligenceJsonCol = main0.optString("mainTabIntelligenceJsonCol", "");
            config0.mainTabLastEnrichedAtCol = main0.optString("mainTabLastEnrichedAtCol", "");
            config0.mainTabEnrichmentStatusCol = main0.optString("mainTabEnrichmentStatusCol", "");
            config0.mainTabInvestmentThesisCol = main0.optString("mainTabInvestmentThesisCol", "");
        }

        if (intake0 != null)
        {
            config0.intakeTabIntakeIdCol = intake0.optString("intakeTabIntakeIdCol", "");
            config0.intakeTabGmailMessageIdCol = intake0.optString("intakeTabGmailMessageIdCol", "");
            config0.intakeTabGmailThreadIdCol = intake0.optString("intakeTabGmailThreadIdCol", "");
            config0.intakeTabIntakeTypeCol = intake0.optString("intakeTabIntakeTypeCol", "");
            config0.intakeTabTimestampCol = intake0.optString("intakeTabTimestampCol", "");
            config0.intakeTabToCol = intake0.optString("intakeTabToCol", "");
            config0.intakeTabFromCol = intake0.optString("intakeTabFromCol", "");
            config0.intakeTabSubjectCol = intake0.optString("intakeTabSubjectCol", "");
            config0.intakeTabBodyCol = intake0.optString("intakeTabBodyCol", "");
        }

        config0.intakeTabProcessingStatusCol = "Processing Status";
        config0.intakeTabCleanedEmailCol = "Cleaned Email";
        config0.intakeTabExtractedFirstNameCol = "Extracted First Name";
        config0.intakeTabExtractedLastNameCol = "Extracted Last Name";
        config0.intakeTabExtractedFundNameCol = "Extracted Fund Name";
        config0.intakeTabExtractedFundWebsiteCol = "Extracted Fund Website";
        config0.intakeTabConversationSummaryCol = "Conversation Summary";
        config0.intakeTabConversationLabelCol = "Conversation Label";
        config0.intakeTabUpdatedCrmCol = "Updated CRM";
        config0.intakeTabNeedsReviewCol = "Needs Review";

        return config0;
    }

    private static void ensureSystemIntakeColumnsExist(
        String spreadsheetId0,
        CRMSchemaConfig config0
    ) throws Exception
    {
        String[] requiredHeaders0 = new String[]
        {
            config0.intakeTabProcessingStatusCol,
            config0.intakeTabCleanedEmailCol,
            config0.intakeTabExtractedFirstNameCol,
            config0.intakeTabExtractedLastNameCol,
            config0.intakeTabConversationLabelCol,
            config0.intakeTabExtractedFundNameCol,
            config0.intakeTabExtractedFundWebsiteCol,
            config0.intakeTabConversationSummaryCol,
            config0.intakeTabUpdatedCrmCol,
            config0.intakeTabNeedsReviewCol
        };

        ensureHeadersExist(
            spreadsheetId0,
            config0.intakeTabName,
            config0.intakeTabHeaderRow,
            requiredHeaders0,
            150
        );
    }

    private static void ensureRequiredMainCrmColumnsExist(
        String spreadsheetId0,
        CRMSchemaConfig config0
    ) throws Exception
    {
        setDefaultMainCrmHeaders(config0);

        String[] requiredHeaders0 = new String[]
        {
            config0.mainTabFundNameCol,
            config0.mainTabContact1FirstNameCol,
            config0.mainTabContact1LastNameCol,
            config0.mainTabContact1EmailCol,
            config0.mainTabStatusCol,
            config0.mainTabLastContactDateCol,
            config0.mainTabInteractionHistoryCol,
            config0.mainTabTypeOfInvestorCol,
            config0.mainTabWebsiteCol,
            config0.mainTabNotesCol,
            config0.mainTabContact1PositionCol,
            config0.mainTabLinkedInCol,
            config0.mainTabContact2FirstNameCol,
            config0.mainTabContact2LastNameCol,
            config0.mainTabContact2EmailCol,
            config0.mainTabContact2PositionCol,
            config0.mainTabLinkedIn2Col,
            config0.mainTabCountryCol,

            config0.mainTabSectorTagsCol,
            config0.mainTabMicrosectorTagsCol,
            config0.mainTabGeographyCol,
            config0.mainTabPriorBackedFundsCol,
            config0.mainTabIntelligenceJsonCol,
            config0.mainTabLastEnrichedAtCol,
            config0.mainTabEnrichmentStatusCol,
            config0.mainTabInvestmentThesisCol,
            config0.mainTabInvestorProfileSimilarityCol,
            config0.mainTabPriorityScoreCol,
            config0.mainTabPriorityReasonCol,
            config0.mainTabNextActionCol,
            config0.mainTabPriorityLastCalculatedAtCol,
            config0.mainTabFollowUpRecommendationCol
        };

        ensureHeadersExist(
            spreadsheetId0,
            config0.mainTabName,
            config0.mainTabHeaderRow,
            requiredHeaders0,
            200
        );
    }

    private static void ensureHeadersExist(
        String spreadsheetId0,
        String tabName0,
        int headerRow0,
        String[] requiredHeaders0,
        int scanEndColumn0
    ) throws Exception
    {
        String[][] headerData0 = SheetsApp.readRangeMatrix(
            spreadsheetId0,
            tabName0,
            headerRow0,
            1,
            headerRow0,
            scanEndColumn0
        );

        int lastHeaderCol0 = 0;

        for (int i = 0; i < headerData0[0].length; i++)
        {
            if (headerData0[0][i] != null &&
                headerData0[0][i].trim().length() > 0)
            {
                lastHeaderCol0 = i + 1;
            }
        }

        for (int i = 0; i < requiredHeaders0.length; i++)
        {
            String requiredHeader0 = requiredHeaders0[i];

            if (isBlank(requiredHeader0))
            {
                continue;
            }

            if (!headerExists(headerData0[0], requiredHeader0))
            {
                lastHeaderCol0++;

                SheetsApp.updateCell(
                    spreadsheetId0,
                    tabName0,
                    headerRow0,
                    lastHeaderCol0,
                    requiredHeader0
                );
            }
        }
    }

    private static boolean headerExists(String[] headers0, String target0)
    {
        if (isBlank(target0))
        {
            return false;
        }

        for (int i = 0; i < headers0.length; i++)
        {
            if (headers0[i] != null &&
                headers0[i].trim().equalsIgnoreCase(target0.trim()))
            {
                return true;
            }
        }

        return false;
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

            String jsonObjectText0 = trimmedText0.substring(startIndex0, endIndex0 + 1);

            return new JSONObject(jsonObjectText0);
        }
    }

    private static void setDefaultMainCrmHeaders(CRMSchemaConfig config0)
    {
        if (isBlank(config0.mainTabFundNameCol))
            config0.mainTabFundNameCol = "Fund Name";

        if (isBlank(config0.mainTabFollowUpRecommendationCol))
            config0.mainTabFollowUpRecommendationCol = "Follow Up Recommendation";

        if (isBlank(config0.mainTabContact1FirstNameCol))
            config0.mainTabContact1FirstNameCol = "Contact 1 First Name";

        if (isBlank(config0.mainTabContact1LastNameCol))
            config0.mainTabContact1LastNameCol = "Contact 1 Last Name";

        if (isBlank(config0.mainTabContact1EmailCol))
            config0.mainTabContact1EmailCol = "Contact 1 Email Address";

        if (isBlank(config0.mainTabStatusCol))
            config0.mainTabStatusCol = "Conversation Status";

        if (isBlank(config0.mainTabLastContactDateCol))
            config0.mainTabLastContactDateCol = "Last Contact Date";

        if (isBlank(config0.mainTabInteractionHistoryCol))
            config0.mainTabInteractionHistoryCol = "Interaction History";

        if (isBlank(config0.mainTabTypeOfInvestorCol))
            config0.mainTabTypeOfInvestorCol = "Type of Investor";

        if (isBlank(config0.mainTabWebsiteCol))
            config0.mainTabWebsiteCol = "Fund Website";

        if (isBlank(config0.mainTabNotesCol))
            config0.mainTabNotesCol = "Notes";

        if (isBlank(config0.mainTabContact1PositionCol))
            config0.mainTabContact1PositionCol = "Contact 1 Position";

        if (isBlank(config0.mainTabLinkedInCol))
            config0.mainTabLinkedInCol = "Contact 1 LinkedIn";

        if (isBlank(config0.mainTabContact2FirstNameCol))
            config0.mainTabContact2FirstNameCol = "Contact 2 First Name";

        if (isBlank(config0.mainTabContact2LastNameCol))
            config0.mainTabContact2LastNameCol = "Contact 2 Last Name";

        if (isBlank(config0.mainTabContact2EmailCol))
            config0.mainTabContact2EmailCol = "Contact 2 Email Address";

        if (isBlank(config0.mainTabContact2PositionCol))
            config0.mainTabContact2PositionCol = "Contact 2 Position";

        if (isBlank(config0.mainTabLinkedIn2Col))
            config0.mainTabLinkedIn2Col = "Contact 2 LinkedIn";

        if (isBlank(config0.mainTabCountryCol))
            config0.mainTabCountryCol = "Country";

        if (isBlank(config0.mainTabSectorTagsCol))
            config0.mainTabSectorTagsCol = "Sector Tags";

        if (isBlank(config0.mainTabMicrosectorTagsCol))
            config0.mainTabMicrosectorTagsCol = "Microsector Tags";

        if (isBlank(config0.mainTabGeographyCol))
            config0.mainTabGeographyCol = "Geography";

        if (isBlank(config0.mainTabPriorBackedFundsCol))
            config0.mainTabPriorBackedFundsCol = "Prior Backed Funds";

        if (isBlank(config0.mainTabIntelligenceJsonCol))
            config0.mainTabIntelligenceJsonCol = "Intelligence JSON";

        if (isBlank(config0.mainTabLastEnrichedAtCol))
            config0.mainTabLastEnrichedAtCol = "Last Enriched At";

        if (isBlank(config0.mainTabEnrichmentStatusCol))
            config0.mainTabEnrichmentStatusCol = "Enrichment Status";

        if (isBlank(config0.mainTabInvestmentThesisCol))
            config0.mainTabInvestmentThesisCol = "Investment Thesis";

        if (isBlank(config0.mainTabInvestorProfileSimilarityCol))
            config0.mainTabInvestorProfileSimilarityCol = "Investor Profile Similarity";

        if (isBlank(config0.mainTabPriorityScoreCol))
            config0.mainTabPriorityScoreCol = "Priority Score";

        if (isBlank(config0.mainTabPriorityReasonCol))
            config0.mainTabPriorityReasonCol = "Priority Reason";

        if (isBlank(config0.mainTabNextActionCol))
            config0.mainTabNextActionCol = "Next Action";

        if (isBlank(config0.mainTabPriorityLastCalculatedAtCol))
            config0.mainTabPriorityLastCalculatedAtCol = "Priority Last Calculated At";
    }

    private static boolean isBlank(String value0)
    {
        return value0 == null || value0.trim().length() == 0;
    }
}