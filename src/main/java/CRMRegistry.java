import java.util.HashMap;
import java.util.ArrayList;

public class CRMRegistry
{
    public static final String CRM_USER_DATABASE_SPREADSHEET_ID = "1GUBAD6csIMjVY6MtWlTGJM98wT8hTb5nwmElxJBePFM";

    public static final String USERS_TAB = "Users";
    public static final String CONFIGS_TAB = "CRM Configs";

    public static final int MAX_ROWS = 1000;

    public static final String USERS_READ_RANGE0 = "A1:Z";
    public static final String CONFIGS_READ_RANGE0 = "A1:CZ";
    public static final String CONFIGS_HEADER_RANGE0 = "A1:CZ1";

    // USERS TAB HEADERS
    public static final String USER_ID_HEADER = "User ID";
    public static final String EMAIL_HEADER = "Email";
    public static final String FUND_NAME_HEADER = "Fund Name";
    public static final String CRM_CONFIG_ID_HEADER = "CRM Config ID";
    public static final String INTERNAL_NAMES_HEADER = "Internal Names";
    public static final String INTERNAL_EMAILS_HEADER = "Internal Emails";
    public static final String INTERNAL_FUND_NAME_HEADER = "Internal Fund Name";
    public static final String INTERNAL_WEBSITE_HEADER = "Internal Website";
    public static final String USER_EXTRA_DATA_HEADER = "Extra Data";
    public static final String CLIENT_SECTOR_TAGS_HEADER = "Client Sector Tags";
    public static final String CLIENT_MICROSECTOR_TAGS_HEADER = "Client Microsector Tags";
    public static final String CLIENT_GEOGRAPHY_HEADER = "Client Geography";
    public static final String CLIENT_INVESTMENT_THESIS_HEADER = "Client Investment Thesis";
    public static final String CLIENT_PROFILE_JSON_HEADER = "Client Profile JSON";

    // CONFIG TAB HEADERS
    public static final String CONFIG_ID_HEADER = "Config ID";
    public static final String CONFIG_USER_ID_HEADER = "User ID";
    public static final String CRM_NAME_HEADER = "CRM Name";
    public static final String CLIENT_SPREADSHEET_ID_HEADER = "Client Spreadsheet ID";

    public static final String MAIN_TAB_NAME_HEADER = "mainTabName";
    public static final String INTAKE_TAB_NAME_HEADER = "intakeTabName";
    public static final String INTERACTIONS_TAB_NAME_HEADER = "interactionsTabName";
    public static final String TASKS_TAB_NAME_HEADER = "tasksTabName";

    public static final String MAIN_TAB_HEADER_ROW_HEADER = "mainTabHeaderRow";
    public static final String MAIN_TAB_DATA_START_ROW_HEADER = "mainTabDataStartRow";

    public static final String MAIN_TAB_FUND_NAME_COL_HEADER = "mainTabFundNameCol";
    public static final String MAIN_TAB_CONTACT_1_FIRST_NAME_COL_HEADER = "mainTabContact1FirstNameCol";
    public static final String MAIN_TAB_CONTACT_1_LAST_NAME_COL_HEADER = "mainTabContact1LastNameCol";
    public static final String MAIN_TAB_CONTACT_2_FIRST_NAME_COL_HEADER = "mainTabContact2FirstNameCol";
    public static final String MAIN_TAB_CONTACT_2_LAST_NAME_COL_HEADER = "mainTabContact2LastNameCol";
    public static final String MAIN_TAB_CONTACT_1_EMAIL_COL_HEADER = "mainTabContact1EmailCol";
    public static final String MAIN_TAB_CONTACT_2_EMAIL_COL_HEADER = "mainTabContact2EmailCol";
    public static final String MAIN_TAB_CONTACT_1_POSITION_COL_HEADER = "mainTabContact1PositionCol";
    public static final String MAIN_TAB_NOTES_COL_HEADER = "mainTabNotesCol";
    public static final String MAIN_TAB_TYPE_OF_INVESTOR_COL_HEADER = "mainTabTypeOfInvestorCol";
    public static final String MAIN_TAB_STATUS_COL_HEADER = "mainTabStatusCol";
    public static final String MAIN_TAB_COLD_EMAIL_COL_HEADER = "mainTabColdEmailCol";
    public static final String MAIN_TAB_LINKEDIN_COL_HEADER = "mainTabLinkedInCol";
    public static final String MAIN_TAB_NAME_LINKEDIN_QUERY_COL_HEADER = "mainTabNameLinkedInQueryCol";
    public static final String MAIN_TAB_COMPANY_LINKEDIN_QUERY_COL_HEADER = "mainTabCompanyLinkedInQueryCol";
    public static final String MAIN_TAB_LINKEDIN_2_COL_HEADER = "mainTabLinkedIn2Col";
    public static final String MAIN_TAB_WEBSITE_COL_HEADER = "mainTabWebsiteCol";
    public static final String MAIN_TAB_INVESTOR_PROFILE_SIMILARITY_COL_HEADER = "mainTabInvestorProfileSimilarityCol";
    public static final String MAIN_TAB_COMMENTS_COL_HEADER = "mainTabCommentsCol";
    public static final String MAIN_TAB_COUNTRY_COL_HEADER = "mainTabCountryCol";
    public static final String MAIN_TAB_CITY_COL_HEADER = "mainTabCityCol";
    public static final String MAIN_TAB_LAST_CONTACT_DATE_COL_HEADER = "mainTabLastContactDateCol";
    public static final String MAIN_TAB_NEXT_ACTION_COL_HEADER = "mainTabNextActionCol";
    public static final String MAIN_TAB_PRIORITY_SCORE_COL_HEADER = "mainTabPriorityScoreCol";
    public static final String MAIN_TAB_PRIORITY_REASON_COL_HEADER = "mainTabPriorityReasonCol";
    public static final String MAIN_TAB_PRIORITY_LAST_CALCULATED_AT_COL_HEADER = "mainTabPriorityLastCalculatedAtCol";
    public static final String MAIN_TAB_INTERACTION_HISTORY_COL_HEADER = "mainTabInteractionHistoryCol";
    public static final String MAIN_TAB_CONTACT_2_POSITION_COL_HEADER = "mainTabContact2PositionCol";
    public static final String MAIN_TAB_FOLLOW_UP_RECOMMENDATION_COL_HEADER = "mainTabFollowUpRecommendationCol";

    // LP INTELLIGENCE CONFIG HEADERS
    public static final String MAIN_TAB_SECTOR_TAGS_COL_HEADER = "mainTabSectorTagsCol";
    public static final String MAIN_TAB_MICROSECTOR_TAGS_COL_HEADER = "mainTabMicrosectorTagsCol";
    public static final String MAIN_TAB_GEOGRAPHY_COL_HEADER = "mainTabGeographyCol";
    public static final String MAIN_TAB_PRIOR_BACKED_FUNDS_COL_HEADER = "mainTabPriorBackedFundsCol";
    public static final String MAIN_TAB_INTELLIGENCE_JSON_COL_HEADER = "mainTabIntelligenceJsonCol";
    public static final String MAIN_TAB_LAST_ENRICHED_AT_COL_HEADER = "mainTabLastEnrichedAtCol";
    public static final String MAIN_TAB_ENRICHMENT_STATUS_COL_HEADER = "mainTabEnrichmentStatusCol";
    public static final String MAIN_TAB_INVESTMENT_THESIS_COL_HEADER = "mainTabInvestmentThesisCol";

    public static final String INTAKE_TAB_HEADER_ROW_HEADER = "intakeTabHeaderRow";
    public static final String INTAKE_TAB_DATA_START_ROW_HEADER = "intakeTabDataStartRow";
    public static final String INTAKE_TAB_INTAKE_ID_COL_HEADER = "intakeTabIntakeIdCol";
    public static final String INTAKE_TAB_GMAIL_MESSAGE_ID_COL_HEADER = "intakeTabGmailMessageIdCol";
    public static final String INTAKE_TAB_GMAIL_THREAD_ID_COL_HEADER = "intakeTabGmailThreadIdCol";
    public static final String INTAKE_TAB_INTAKE_TYPE_COL_HEADER = "intakeTabIntakeTypeCol";
    public static final String INTAKE_TAB_TIMESTAMP_COL_HEADER = "intakeTabTimestampCol";
    public static final String INTAKE_TAB_TO_COL_HEADER = "intakeTabToCol";
    public static final String INTAKE_TAB_FROM_COL_HEADER = "intakeTabFromCol";
    public static final String INTAKE_TAB_SUBJECT_COL_HEADER = "intakeTabSubjectCol";
    public static final String INTAKE_TAB_BODY_COL_HEADER = "intakeTabBodyCol";
    public static final String INTAKE_TAB_PROCESSING_STATUS_COL_HEADER = "intakeTabProcessingStatusCol";
    public static final String INTAKE_TAB_CLEANED_EMAIL_COL_HEADER = "intakeTabCleanedEmailCol";
    public static final String INTAKE_TAB_EXTRACTED_FIRST_NAME_COL_HEADER = "intakeTabExtractedFirstNameCol";
    public static final String INTAKE_TAB_CONVERSATION_LABEL_COL_HEADER = "intakeTabConversationLabelCol";
    public static final String INTAKE_TAB_UPDATED_CRM_COL_HEADER = "intakeTabUpdatedCrmCol";
    public static final String INTAKE_TAB_NEEDS_REVIEW_COL_HEADER = "intakeTabNeedsReviewCol";
    public static final String INTAKE_TAB_EXTRACTED_LAST_NAME_COL_HEADER = "intakeTabExtractedLastNameCol";
    public static final String INTAKE_TAB_EXTRACTED_FUND_NAME_COL_HEADER = "intakeTabExtractedFundNameCol";
    public static final String INTAKE_TAB_EXTRACTED_FUND_WEBSITE_COL_HEADER = "intakeTabExtractedFundWebsiteCol";
    public static final String INTAKE_TAB_CONVERSATION_SUMMARY_COL_HEADER = "intakeTabConversationSummaryCol";

    public static final String CONFIG_EXTRA_DATA_HEADER = "Extra Data";

    public static SessionContext login(String email) throws Exception
    {
        UserAccount user = loadUserByEmail(email);

        if (user == null)
        {
            System.out.println("No user found for email: " + email);
            return null;
        }

        CRMSchemaConfig config = loadConfigById(user.crmConfigId);

        if (config == null)
        {
            System.out.println("No CRM config found for user: " + email);
            return null;
        }

        return new SessionContext(user, config);
    }

    public static UserAccount loadUserByEmail(String email) throws Exception
    {
        String[][] rows = SheetsApp.readRangeMatrixA1(
            CRM_USER_DATABASE_SPREADSHEET_ID,
            USERS_TAB,
            USERS_READ_RANGE0 + MAX_ROWS
        );

        if (rows == null || rows.length < 2)
        {
            return null;
        }

        HashMap<String, Integer> headerMap = buildHeaderMap(rows[0]);

        for (int i = 1; i < rows.length; i++)
        {
            String rowEmail = getCell(rows[i], headerMap, EMAIL_HEADER);

            if (rowEmail.equalsIgnoreCase(email))
            {
                return new UserAccount(
                    getCell(rows[i], headerMap, USER_ID_HEADER),
                    rowEmail,
                    getCell(rows[i], headerMap, FUND_NAME_HEADER),
                    getCell(rows[i], headerMap, CRM_CONFIG_ID_HEADER),
                    parsePipeSeparatedList(getCell(rows[i], headerMap, INTERNAL_NAMES_HEADER)),
                    parsePipeSeparatedList(getCell(rows[i], headerMap, INTERNAL_EMAILS_HEADER)),
                    getCell(rows[i], headerMap, INTERNAL_FUND_NAME_HEADER),
                    getCell(rows[i], headerMap, INTERNAL_WEBSITE_HEADER),
                    getCell(rows[i], headerMap, USER_EXTRA_DATA_HEADER),
                    getCell(rows[i], headerMap, CLIENT_SECTOR_TAGS_HEADER),
                    getCell(rows[i], headerMap, CLIENT_MICROSECTOR_TAGS_HEADER),
                    getCell(rows[i], headerMap, CLIENT_GEOGRAPHY_HEADER),
                    getCell(rows[i], headerMap, CLIENT_INVESTMENT_THESIS_HEADER),
                    getCell(rows[i], headerMap, CLIENT_PROFILE_JSON_HEADER)
                );
            }
        }

        return null;
    }

    public static CRMSchemaConfig loadConfigById(String configId) throws Exception
    {
        String[][] rows = SheetsApp.readRangeMatrixA1(
            CRM_USER_DATABASE_SPREADSHEET_ID,
            CONFIGS_TAB,
            CONFIGS_READ_RANGE0 + MAX_ROWS
        );

        if (rows == null || rows.length < 2)
        {
            return null;
        }

        HashMap<String, Integer> headerMap = buildHeaderMap(rows[0]);

        for (int i = 1; i < rows.length; i++)
        {
            String rowConfigId = getCell(rows[i], headerMap, CONFIG_ID_HEADER);

            if (rowConfigId.equals(configId))
            {
                return rowToConfig(rows[i], headerMap);
            }
        }

        return null;
    }

    private static CRMSchemaConfig rowToConfig(
        String[] row,
        HashMap<String, Integer> headerMap)
    {
        CRMSchemaConfig config = new CRMSchemaConfig(
            getCell(row, headerMap, CONFIG_ID_HEADER),
            getCell(row, headerMap, CONFIG_USER_ID_HEADER),
            getCell(row, headerMap, CRM_NAME_HEADER),
            getCell(row, headerMap, CLIENT_SPREADSHEET_ID_HEADER)
        );

        config.mainTabName = getCell(row, headerMap, MAIN_TAB_NAME_HEADER);
        config.intakeTabName = getCell(row, headerMap, INTAKE_TAB_NAME_HEADER);
        config.interactionsTabName = getCell(row, headerMap, INTERACTIONS_TAB_NAME_HEADER);
        config.tasksTabName = getCell(row, headerMap, TASKS_TAB_NAME_HEADER);

        config.mainTabHeaderRow = parseIntOrDefault(getCell(row, headerMap, MAIN_TAB_HEADER_ROW_HEADER), 1);
        config.mainTabDataStartRow = parseIntOrDefault(getCell(row, headerMap, MAIN_TAB_DATA_START_ROW_HEADER), 2);

        config.mainTabFundNameCol = getCell(row, headerMap, MAIN_TAB_FUND_NAME_COL_HEADER);
        config.mainTabContact1FirstNameCol = getCell(row, headerMap, MAIN_TAB_CONTACT_1_FIRST_NAME_COL_HEADER);
        config.mainTabContact1LastNameCol = getCell(row, headerMap, MAIN_TAB_CONTACT_1_LAST_NAME_COL_HEADER);
        config.mainTabContact2FirstNameCol = getCell(row, headerMap, MAIN_TAB_CONTACT_2_FIRST_NAME_COL_HEADER);
        config.mainTabContact2LastNameCol = getCell(row, headerMap, MAIN_TAB_CONTACT_2_LAST_NAME_COL_HEADER);
        config.mainTabContact1EmailCol = getCell(row, headerMap, MAIN_TAB_CONTACT_1_EMAIL_COL_HEADER);
        config.mainTabContact2EmailCol = getCell(row, headerMap, MAIN_TAB_CONTACT_2_EMAIL_COL_HEADER);
        config.mainTabContact1PositionCol = getCell(row, headerMap, MAIN_TAB_CONTACT_1_POSITION_COL_HEADER);
        config.mainTabNotesCol = getCell(row, headerMap, MAIN_TAB_NOTES_COL_HEADER);
        config.mainTabTypeOfInvestorCol = getCell(row, headerMap, MAIN_TAB_TYPE_OF_INVESTOR_COL_HEADER);
        config.mainTabStatusCol = getCell(row, headerMap, MAIN_TAB_STATUS_COL_HEADER);
        config.mainTabColdEmailCol = getCell(row, headerMap, MAIN_TAB_COLD_EMAIL_COL_HEADER);
        config.mainTabLinkedInCol = getCell(row, headerMap, MAIN_TAB_LINKEDIN_COL_HEADER);
        config.mainTabNameLinkedInQueryCol = getCell(row, headerMap, MAIN_TAB_NAME_LINKEDIN_QUERY_COL_HEADER);
        config.mainTabCompanyLinkedInQueryCol = getCell(row, headerMap, MAIN_TAB_COMPANY_LINKEDIN_QUERY_COL_HEADER);
        config.mainTabLinkedIn2Col = getCell(row, headerMap, MAIN_TAB_LINKEDIN_2_COL_HEADER);
        config.mainTabWebsiteCol = getCell(row, headerMap, MAIN_TAB_WEBSITE_COL_HEADER);
        config.mainTabInvestorProfileSimilarityCol = getCell(row, headerMap, MAIN_TAB_INVESTOR_PROFILE_SIMILARITY_COL_HEADER);
        config.mainTabCommentsCol = getCell(row, headerMap, MAIN_TAB_COMMENTS_COL_HEADER);
        config.mainTabCountryCol = getCell(row, headerMap, MAIN_TAB_COUNTRY_COL_HEADER);
        config.mainTabCityCol = getCell(row, headerMap, MAIN_TAB_CITY_COL_HEADER);
        config.mainTabLastContactDateCol = getCell(row, headerMap, MAIN_TAB_LAST_CONTACT_DATE_COL_HEADER);
        config.mainTabNextActionCol = getCell(row, headerMap, MAIN_TAB_NEXT_ACTION_COL_HEADER);
        config.mainTabPriorityScoreCol = getCell(row, headerMap, MAIN_TAB_PRIORITY_SCORE_COL_HEADER);
        config.mainTabPriorityReasonCol = getCell(row, headerMap, MAIN_TAB_PRIORITY_REASON_COL_HEADER);
        config.mainTabPriorityLastCalculatedAtCol = getCell(row, headerMap, MAIN_TAB_PRIORITY_LAST_CALCULATED_AT_COL_HEADER);
        config.mainTabInteractionHistoryCol = getCell(row, headerMap, MAIN_TAB_INTERACTION_HISTORY_COL_HEADER);
        config.mainTabFollowUpRecommendationCol = getCell(row, headerMap, MAIN_TAB_FOLLOW_UP_RECOMMENDATION_COL_HEADER);
        config.mainTabContact2PositionCol = getCell(row, headerMap, MAIN_TAB_CONTACT_2_POSITION_COL_HEADER);

        config.mainTabSectorTagsCol = getCell(row, headerMap, MAIN_TAB_SECTOR_TAGS_COL_HEADER);
        config.mainTabMicrosectorTagsCol = getCell(row, headerMap, MAIN_TAB_MICROSECTOR_TAGS_COL_HEADER);
        config.mainTabGeographyCol = getCell(row, headerMap, MAIN_TAB_GEOGRAPHY_COL_HEADER);
        config.mainTabPriorBackedFundsCol = getCell(row, headerMap, MAIN_TAB_PRIOR_BACKED_FUNDS_COL_HEADER);
        config.mainTabIntelligenceJsonCol = getCell(row, headerMap, MAIN_TAB_INTELLIGENCE_JSON_COL_HEADER);
        config.mainTabLastEnrichedAtCol = getCell(row, headerMap, MAIN_TAB_LAST_ENRICHED_AT_COL_HEADER);
        config.mainTabEnrichmentStatusCol = getCell(row, headerMap, MAIN_TAB_ENRICHMENT_STATUS_COL_HEADER);
        config.mainTabInvestmentThesisCol = getCell(row, headerMap, MAIN_TAB_INVESTMENT_THESIS_COL_HEADER);

        config.intakeTabHeaderRow = parseIntOrDefault(getCell(row, headerMap, INTAKE_TAB_HEADER_ROW_HEADER), 1);
        config.intakeTabDataStartRow = parseIntOrDefault(getCell(row, headerMap, INTAKE_TAB_DATA_START_ROW_HEADER), 2);

        config.intakeTabIntakeIdCol = getCell(row, headerMap, INTAKE_TAB_INTAKE_ID_COL_HEADER);
        config.intakeTabGmailMessageIdCol = getCell(row, headerMap, INTAKE_TAB_GMAIL_MESSAGE_ID_COL_HEADER);
        config.intakeTabGmailThreadIdCol = getCell(row, headerMap, INTAKE_TAB_GMAIL_THREAD_ID_COL_HEADER);
        config.intakeTabIntakeTypeCol = getCell(row, headerMap, INTAKE_TAB_INTAKE_TYPE_COL_HEADER);
        config.intakeTabTimestampCol = getCell(row, headerMap, INTAKE_TAB_TIMESTAMP_COL_HEADER);
        config.intakeTabToCol = getCell(row, headerMap, INTAKE_TAB_TO_COL_HEADER);
        config.intakeTabFromCol = getCell(row, headerMap, INTAKE_TAB_FROM_COL_HEADER);
        config.intakeTabSubjectCol = getCell(row, headerMap, INTAKE_TAB_SUBJECT_COL_HEADER);
        config.intakeTabBodyCol = getCell(row, headerMap, INTAKE_TAB_BODY_COL_HEADER);
        config.intakeTabProcessingStatusCol = getCell(row, headerMap, INTAKE_TAB_PROCESSING_STATUS_COL_HEADER);
        config.intakeTabCleanedEmailCol = getCell(row, headerMap, INTAKE_TAB_CLEANED_EMAIL_COL_HEADER);
        config.intakeTabExtractedFirstNameCol = getCell(row, headerMap, INTAKE_TAB_EXTRACTED_FIRST_NAME_COL_HEADER);
        config.intakeTabExtractedLastNameCol = getCell(row, headerMap, INTAKE_TAB_EXTRACTED_LAST_NAME_COL_HEADER);
        config.intakeTabExtractedFundNameCol = getCell(row, headerMap, INTAKE_TAB_EXTRACTED_FUND_NAME_COL_HEADER);
        config.intakeTabExtractedFundWebsiteCol = getCell(row, headerMap, INTAKE_TAB_EXTRACTED_FUND_WEBSITE_COL_HEADER);
        config.intakeTabConversationSummaryCol = getCell(row, headerMap, INTAKE_TAB_CONVERSATION_SUMMARY_COL_HEADER);
        config.intakeTabConversationLabelCol = getCell(row, headerMap, INTAKE_TAB_CONVERSATION_LABEL_COL_HEADER);
        config.intakeTabUpdatedCrmCol = getCell(row, headerMap, INTAKE_TAB_UPDATED_CRM_COL_HEADER);
        config.intakeTabNeedsReviewCol = getCell(row, headerMap, INTAKE_TAB_NEEDS_REVIEW_COL_HEADER);

        config.extraData = getCell(row, headerMap, CONFIG_EXTRA_DATA_HEADER);

        return config;
    }

    public static void registerUser(UserAccount user, CRMSchemaConfig config) throws Exception
    {
        saveUser(user);
        saveConfig(config);
    }

    public static void saveUser(UserAccount user) throws Exception
    {
        HashMap<String, Integer> headerMap0 = buildDatabaseHeaderMap(
            USERS_TAB,
            "A1:Z1"
        );

        String[] row0 = buildRowFromHeaderMap(headerMap0);

        putCell(row0, headerMap0, USER_ID_HEADER, user.userId);
        putCell(row0, headerMap0, EMAIL_HEADER, user.email);
        putCell(row0, headerMap0, FUND_NAME_HEADER, user.fundName);
        putCell(row0, headerMap0, CRM_CONFIG_ID_HEADER, user.crmConfigId);
        putCell(row0, headerMap0, INTERNAL_NAMES_HEADER, joinWithPipe(user.internalNames));
        putCell(row0, headerMap0, INTERNAL_EMAILS_HEADER, joinWithPipe(user.internalEmails));
        putCell(row0, headerMap0, INTERNAL_FUND_NAME_HEADER, user.internalFundName);
        putCell(row0, headerMap0, INTERNAL_WEBSITE_HEADER, user.internalWebsite);
        putCell(row0, headerMap0, USER_EXTRA_DATA_HEADER, user.extraData);
        putCell(row0, headerMap0, CLIENT_SECTOR_TAGS_HEADER, user.clientSectorTags);
        putCell(row0, headerMap0, CLIENT_MICROSECTOR_TAGS_HEADER, user.clientMicrosectorTags);
        putCell(row0, headerMap0, CLIENT_GEOGRAPHY_HEADER, user.clientGeography);
        putCell(row0, headerMap0, CLIENT_INVESTMENT_THESIS_HEADER, user.clientInvestmentThesis);
        putCell(row0, headerMap0, CLIENT_PROFILE_JSON_HEADER, user.clientProfileJson);

        SheetsApp.appendRow(
            CRM_USER_DATABASE_SPREADSHEET_ID,
            USERS_TAB,
            row0
        );
    }

    public static void saveConfig(CRMSchemaConfig config) throws Exception
    {
        HashMap<String, Integer> headerMap0 = buildDatabaseHeaderMap(
            CONFIGS_TAB,
            CONFIGS_HEADER_RANGE0
        );

        String[] row0 = buildRowFromHeaderMap(headerMap0);

        putCell(row0, headerMap0, CONFIG_ID_HEADER, config.configId);
        putCell(row0, headerMap0, CONFIG_USER_ID_HEADER, config.userId);
        putCell(row0, headerMap0, CRM_NAME_HEADER, config.crmName);
        putCell(row0, headerMap0, CLIENT_SPREADSHEET_ID_HEADER, config.spreadsheetId);

        putCell(row0, headerMap0, MAIN_TAB_NAME_HEADER, config.mainTabName);
        putCell(row0, headerMap0, INTAKE_TAB_NAME_HEADER, config.intakeTabName);
        putCell(row0, headerMap0, INTERACTIONS_TAB_NAME_HEADER, config.interactionsTabName);
        putCell(row0, headerMap0, TASKS_TAB_NAME_HEADER, config.tasksTabName);

        putCell(row0, headerMap0, MAIN_TAB_HEADER_ROW_HEADER, config.mainTabHeaderRow);
        putCell(row0, headerMap0, MAIN_TAB_DATA_START_ROW_HEADER, config.mainTabDataStartRow);

        putCell(row0, headerMap0, MAIN_TAB_FUND_NAME_COL_HEADER, config.mainTabFundNameCol);
        putCell(row0, headerMap0, MAIN_TAB_CONTACT_1_FIRST_NAME_COL_HEADER, config.mainTabContact1FirstNameCol);
        putCell(row0, headerMap0, MAIN_TAB_CONTACT_1_LAST_NAME_COL_HEADER, config.mainTabContact1LastNameCol);
        putCell(row0, headerMap0, MAIN_TAB_CONTACT_2_FIRST_NAME_COL_HEADER, config.mainTabContact2FirstNameCol);
        putCell(row0, headerMap0, MAIN_TAB_CONTACT_2_LAST_NAME_COL_HEADER, config.mainTabContact2LastNameCol);
        putCell(row0, headerMap0, MAIN_TAB_CONTACT_1_EMAIL_COL_HEADER, config.mainTabContact1EmailCol);
        putCell(row0, headerMap0, MAIN_TAB_CONTACT_2_EMAIL_COL_HEADER, config.mainTabContact2EmailCol);
        putCell(row0, headerMap0, MAIN_TAB_CONTACT_1_POSITION_COL_HEADER, config.mainTabContact1PositionCol);
        putCell(row0, headerMap0, MAIN_TAB_NOTES_COL_HEADER, config.mainTabNotesCol);
        putCell(row0, headerMap0, MAIN_TAB_TYPE_OF_INVESTOR_COL_HEADER, config.mainTabTypeOfInvestorCol);
        putCell(row0, headerMap0, MAIN_TAB_STATUS_COL_HEADER, config.mainTabStatusCol);
        putCell(row0, headerMap0, MAIN_TAB_COLD_EMAIL_COL_HEADER, config.mainTabColdEmailCol);
        putCell(row0, headerMap0, MAIN_TAB_LINKEDIN_COL_HEADER, config.mainTabLinkedInCol);
        putCell(row0, headerMap0, MAIN_TAB_NAME_LINKEDIN_QUERY_COL_HEADER, config.mainTabNameLinkedInQueryCol);
        putCell(row0, headerMap0, MAIN_TAB_COMPANY_LINKEDIN_QUERY_COL_HEADER, config.mainTabCompanyLinkedInQueryCol);
        putCell(row0, headerMap0, MAIN_TAB_LINKEDIN_2_COL_HEADER, config.mainTabLinkedIn2Col);
        putCell(row0, headerMap0, MAIN_TAB_WEBSITE_COL_HEADER, config.mainTabWebsiteCol);
        putCell(row0, headerMap0, MAIN_TAB_INVESTOR_PROFILE_SIMILARITY_COL_HEADER, config.mainTabInvestorProfileSimilarityCol);
        putCell(row0, headerMap0, MAIN_TAB_COMMENTS_COL_HEADER, config.mainTabCommentsCol);
        putCell(row0, headerMap0, MAIN_TAB_COUNTRY_COL_HEADER, config.mainTabCountryCol);
        putCell(row0, headerMap0, MAIN_TAB_CITY_COL_HEADER, config.mainTabCityCol);
        putCell(row0, headerMap0, MAIN_TAB_LAST_CONTACT_DATE_COL_HEADER, config.mainTabLastContactDateCol);
        putCell(row0, headerMap0, MAIN_TAB_NEXT_ACTION_COL_HEADER, config.mainTabNextActionCol);
        putCell(row0, headerMap0, MAIN_TAB_PRIORITY_SCORE_COL_HEADER, config.mainTabPriorityScoreCol);
        putCell(row0, headerMap0, MAIN_TAB_PRIORITY_REASON_COL_HEADER, config.mainTabPriorityReasonCol);
        putCell(row0, headerMap0, MAIN_TAB_PRIORITY_LAST_CALCULATED_AT_COL_HEADER, config.mainTabPriorityLastCalculatedAtCol);
        putCell(row0, headerMap0, MAIN_TAB_INTERACTION_HISTORY_COL_HEADER, config.mainTabInteractionHistoryCol);
        putCell(row0, headerMap0, MAIN_TAB_FOLLOW_UP_RECOMMENDATION_COL_HEADER, config.mainTabFollowUpRecommendationCol);
        putCell(row0, headerMap0, MAIN_TAB_CONTACT_2_POSITION_COL_HEADER, config.mainTabContact2PositionCol);

        putCell(row0, headerMap0, MAIN_TAB_SECTOR_TAGS_COL_HEADER, config.mainTabSectorTagsCol);
        putCell(row0, headerMap0, MAIN_TAB_MICROSECTOR_TAGS_COL_HEADER, config.mainTabMicrosectorTagsCol);
        putCell(row0, headerMap0, MAIN_TAB_GEOGRAPHY_COL_HEADER, config.mainTabGeographyCol);
        putCell(row0, headerMap0, MAIN_TAB_PRIOR_BACKED_FUNDS_COL_HEADER, config.mainTabPriorBackedFundsCol);
        putCell(row0, headerMap0, MAIN_TAB_INTELLIGENCE_JSON_COL_HEADER, config.mainTabIntelligenceJsonCol);
        putCell(row0, headerMap0, MAIN_TAB_LAST_ENRICHED_AT_COL_HEADER, config.mainTabLastEnrichedAtCol);
        putCell(row0, headerMap0, MAIN_TAB_ENRICHMENT_STATUS_COL_HEADER, config.mainTabEnrichmentStatusCol);
        putCell(row0, headerMap0, MAIN_TAB_INVESTMENT_THESIS_COL_HEADER, config.mainTabInvestmentThesisCol);

        putCell(row0, headerMap0, INTAKE_TAB_HEADER_ROW_HEADER, config.intakeTabHeaderRow);
        putCell(row0, headerMap0, INTAKE_TAB_DATA_START_ROW_HEADER, config.intakeTabDataStartRow);
        putCell(row0, headerMap0, INTAKE_TAB_INTAKE_ID_COL_HEADER, config.intakeTabIntakeIdCol);
        putCell(row0, headerMap0, INTAKE_TAB_GMAIL_MESSAGE_ID_COL_HEADER, config.intakeTabGmailMessageIdCol);
        putCell(row0, headerMap0, INTAKE_TAB_GMAIL_THREAD_ID_COL_HEADER, config.intakeTabGmailThreadIdCol);
        putCell(row0, headerMap0, INTAKE_TAB_INTAKE_TYPE_COL_HEADER, config.intakeTabIntakeTypeCol);
        putCell(row0, headerMap0, INTAKE_TAB_TIMESTAMP_COL_HEADER, config.intakeTabTimestampCol);
        putCell(row0, headerMap0, INTAKE_TAB_TO_COL_HEADER, config.intakeTabToCol);
        putCell(row0, headerMap0, INTAKE_TAB_FROM_COL_HEADER, config.intakeTabFromCol);
        putCell(row0, headerMap0, INTAKE_TAB_SUBJECT_COL_HEADER, config.intakeTabSubjectCol);
        putCell(row0, headerMap0, INTAKE_TAB_BODY_COL_HEADER, config.intakeTabBodyCol);
        putCell(row0, headerMap0, INTAKE_TAB_PROCESSING_STATUS_COL_HEADER, config.intakeTabProcessingStatusCol);
        putCell(row0, headerMap0, INTAKE_TAB_CLEANED_EMAIL_COL_HEADER, config.intakeTabCleanedEmailCol);
        putCell(row0, headerMap0, INTAKE_TAB_EXTRACTED_FIRST_NAME_COL_HEADER, config.intakeTabExtractedFirstNameCol);
        putCell(row0, headerMap0, INTAKE_TAB_EXTRACTED_LAST_NAME_COL_HEADER, config.intakeTabExtractedLastNameCol);
        putCell(row0, headerMap0, INTAKE_TAB_EXTRACTED_FUND_NAME_COL_HEADER, config.intakeTabExtractedFundNameCol);
        putCell(row0, headerMap0, INTAKE_TAB_EXTRACTED_FUND_WEBSITE_COL_HEADER, config.intakeTabExtractedFundWebsiteCol);
        putCell(row0, headerMap0, INTAKE_TAB_CONVERSATION_SUMMARY_COL_HEADER, config.intakeTabConversationSummaryCol);
        putCell(row0, headerMap0, INTAKE_TAB_CONVERSATION_LABEL_COL_HEADER, config.intakeTabConversationLabelCol);
        putCell(row0, headerMap0, INTAKE_TAB_UPDATED_CRM_COL_HEADER, config.intakeTabUpdatedCrmCol);
        putCell(row0, headerMap0, INTAKE_TAB_NEEDS_REVIEW_COL_HEADER, config.intakeTabNeedsReviewCol);

        putCell(row0, headerMap0, CONFIG_EXTRA_DATA_HEADER, config.extraData);

        SheetsApp.appendRow(
            CRM_USER_DATABASE_SPREADSHEET_ID,
            CONFIGS_TAB,
            row0
        );
    }

    private static HashMap<String, Integer> buildHeaderMap(String[] headerRow)
    {
        HashMap<String, Integer> headerMap = new HashMap<>();

        for (int i = 0; i < headerRow.length; i++)
        {
            if (headerRow[i] != null)
            {
                String header = headerRow[i].trim();

                if (!header.equals(""))
                {
                    headerMap.put(header, i);
                }
            }
        }

        return headerMap;
    }

    private static String getCell(
        String[] row,
        HashMap<String, Integer> headerMap,
        String header)
    {
        if (!headerMap.containsKey(header))
        {
            return "";
        }

        int index = headerMap.get(header);

        if (index < 0 || index >= row.length || row[index] == null)
        {
            return "";
        }

        return row[index].trim();
    }

    private static int parseIntOrDefault(String value, int defaultValue)
    {
        try
        {
            if (value == null || value.trim().equals(""))
            {
                return defaultValue;
            }

            return Integer.parseInt(value.trim());
        }
        catch (Exception e)
        {
            return defaultValue;
        }
    }

    private static ArrayList<String> parsePipeSeparatedList(String value)
    {
        ArrayList<String> list = new ArrayList<>();

        if (value == null)
        {
            return list;
        }

        value = value.trim();

        if (value.equals(""))
        {
            return list;
        }

        String[] split = value.split("\\|");

        for (String item : split)
        {
            String trimmed = item.trim();

            if (!trimmed.equals(""))
            {
                list.add(trimmed);
            }
        }

        return list;
    }

    private static String joinWithPipe(ArrayList<String> list)
    {
        if (list == null || list.size() == 0)
        {
            return "";
        }

        String result = "";

        for (int i = 0; i < list.size(); i++)
        {
            if (i > 0)
            {
                result += "|";
            }

            result += list.get(i);
        }

        return result;
    }

    private static HashMap<String, Integer> buildDatabaseHeaderMap(
        String tabName0,
        String range0) throws Exception
    {
        String[][] rows0 = SheetsApp.readRangeMatrixA1(
            CRM_USER_DATABASE_SPREADSHEET_ID,
            tabName0,
            range0
        );

        if (rows0 == null || rows0.length == 0)
        {
            return new HashMap<String, Integer>();
        }

        return buildHeaderMap(rows0[0]);
    }

    private static String[] buildRowFromHeaderMap(
        HashMap<String, Integer> headerMap0)
    {
        int maxIndex0 = -1;

        for (Integer index0 : headerMap0.values())
        {
            if (index0 != null && index0 > maxIndex0)
            {
                maxIndex0 = index0;
            }
        }

        String[] row0 = new String[maxIndex0 + 1];

        for (int i = 0; i < row0.length; i++)
        {
            row0[i] = "";
        }

        return row0;
    }

    private static void putCell(
        String[] row0,
        HashMap<String, Integer> headerMap0,
        String header0,
        Object value0)
    {
        if (!headerMap0.containsKey(header0))
        {
            return;
        }

        int index0 = headerMap0.get(header0);

        if (index0 < 0 || index0 >= row0.length)
        {
            return;
        }

        row0[index0] = value0 == null ? "" : value0.toString();
    }

    public static String deleteUser(
        String email0,
        String userId0) throws Exception
    {
        if (email0 == null || email0.trim().equals(""))
        {
            return "ERROR: Missing email.";
        }

        if (userId0 == null || userId0.trim().equals(""))
        {
            return "ERROR: Missing user ID.";
        }

        email0 = email0.trim();
        userId0 = userId0.trim();

        int userRowNumber0 = findUserRowByEmailAndUserId(email0, userId0);

        if (userRowNumber0 == -1)
        {
            return "ERROR: No matching user found for email "
                + email0
                + " and user ID "
                + userId0
                + ".";
        }

        int configRowNumber0 = findConfigRowByUserId(userId0);

        if (configRowNumber0 == -1)
        {
            return "ERROR: Matching user found, but no config found for user ID "
                + userId0
                + ". No rows were deleted.";
        }

        SheetsApp.deleteRow(
            CRM_USER_DATABASE_SPREADSHEET_ID,
            CONFIGS_TAB,
            configRowNumber0
        );

        SheetsApp.deleteRow(
            CRM_USER_DATABASE_SPREADSHEET_ID,
            USERS_TAB,
            userRowNumber0
        );

        return "Deleted user and config for email "
            + email0
            + " and user ID "
            + userId0
            + ".";
    }

    private static int findUserRowByEmailAndUserId(
        String email0,
        String userId0) throws Exception
    {
        String[][] rows0 = SheetsApp.readRangeMatrixA1(
            CRM_USER_DATABASE_SPREADSHEET_ID,
            USERS_TAB,
            USERS_READ_RANGE0 + MAX_ROWS
        );

        if (rows0 == null || rows0.length < 2)
        {
            return -1;
        }

        HashMap<String, Integer> headerMap0 = buildHeaderMap(rows0[0]);

        for (int rowIndex0 = 1; rowIndex0 < rows0.length; rowIndex0++)
        {
            String rowEmail0 = getCell(rows0[rowIndex0], headerMap0, EMAIL_HEADER);
            String rowUserId0 = getCell(rows0[rowIndex0], headerMap0, USER_ID_HEADER);

            if (rowEmail0.equalsIgnoreCase(email0) &&
                rowUserId0.equals(userId0))
            {
                return rowIndex0 + 1;
            }
        }

        return -1;
    }

    private static int findConfigRowByUserId(
        String userId0) throws Exception
    {
        String[][] rows0 = SheetsApp.readRangeMatrixA1(
            CRM_USER_DATABASE_SPREADSHEET_ID,
            CONFIGS_TAB,
            CONFIGS_READ_RANGE0 + MAX_ROWS
        );

        if (rows0 == null || rows0.length < 2)
        {
            return -1;
        }

        HashMap<String, Integer> headerMap0 = buildHeaderMap(rows0[0]);

        for (int rowIndex0 = 1; rowIndex0 < rows0.length; rowIndex0++)
        {
            String rowUserId0 = getCell(
                rows0[rowIndex0],
                headerMap0,
                CONFIG_USER_ID_HEADER
            );

            if (rowUserId0.equals(userId0))
            {
                return rowIndex0 + 1;
            }
        }

        return -1;
    }

    public static String generateUserId() throws Exception
    {
        String userId0 = "";

        do
        {
            userId0 = "user_" + java.util.UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12);
        }
        while (userIdExists(userId0));

        return userId0;
    }

    private static boolean userIdExists(String userId0) throws Exception
    {
        String[][] rows0 = SheetsApp.readRangeMatrixA1(
            CRM_USER_DATABASE_SPREADSHEET_ID,
            USERS_TAB,
            USERS_READ_RANGE0 + MAX_ROWS
        );

        if (rows0 == null || rows0.length < 2)
        {
            return false;
        }

        HashMap<String, Integer> headerMap0 = buildHeaderMap(rows0[0]);

        for (int rowIndex0 = 1; rowIndex0 < rows0.length; rowIndex0++)
        {
            String rowUserId0 = getCell(
                rows0[rowIndex0],
                headerMap0,
                USER_ID_HEADER
            );

            if (rowUserId0.equals(userId0))
            {
                return true;
            }
        }

        return false;
    }
}