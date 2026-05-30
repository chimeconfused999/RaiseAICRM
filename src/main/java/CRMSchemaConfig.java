public class CRMSchemaConfig
{
    public String configId;
    public String userId;
    public String crmName;
    public String spreadsheetId;

    // TAB NAMES
    public String mainTabName;
    public String intakeTabName;
    public String interactionsTabName;
    public String tasksTabName;

    // MAIN CRM TAB STRUCTURE
    public int mainTabHeaderRow;
    public int mainTabDataStartRow;

    public String mainTabFundNameCol;

    public String mainTabContact1FirstNameCol;
    public String mainTabContact1LastNameCol;

    public String mainTabContact2FirstNameCol;
    public String mainTabContact2LastNameCol;

    public String mainTabContact1EmailCol;
    public String mainTabContact2EmailCol;

    public String mainTabContact1PositionCol;

    public String mainTabNotesCol;
    public String mainTabTypeOfInvestorCol;
    public String mainTabStatusCol;
    public String mainTabColdEmailCol;

    public String mainTabLinkedInCol;
    public String mainTabNameLinkedInQueryCol;
    public String mainTabCompanyLinkedInQueryCol;
    public String mainTabLinkedIn2Col;

    public String mainTabWebsiteCol;
    public String mainTabInvestorProfileSimilarityCol;
    public String mainTabCommentsCol;

    public String mainTabCountryCol;
    public String mainTabCityCol;

    public String mainTabLastContactDateCol;
    public String mainTabNextActionCol;
    public String mainTabFollowUpRecommendationCol;
    public String mainTabPriorityScoreCol;
    public String mainTabPriorityReasonCol;
    public String mainTabPriorityLastCalculatedAtCol;
    public String mainTabInteractionHistoryCol;
    public String mainTabContact2PositionCol;

    // LP INTELLIGENCE COLUMNS
    public String mainTabSectorTagsCol;
    public String mainTabMicrosectorTagsCol;
    public String mainTabGeographyCol;
    public String mainTabPriorBackedFundsCol;
    public String mainTabIntelligenceJsonCol;
    public String mainTabLastEnrichedAtCol;
    public String mainTabEnrichmentStatusCol;
    public String mainTabInvestmentThesisCol;

    // EMAIL INTAKE TAB STRUCTURE
    public int intakeTabHeaderRow;
    public int intakeTabDataStartRow;

    public String intakeTabIntakeIdCol;
    public String intakeTabGmailMessageIdCol;
    public String intakeTabGmailThreadIdCol;

    public String intakeTabIntakeTypeCol;
    public String intakeTabTimestampCol;

    public String intakeTabToCol;
    public String intakeTabFromCol;
    public String intakeTabSubjectCol;
    public String intakeTabBodyCol;

    // SYSTEM-GENERATED INTAKE COLUMNS
    public String intakeTabProcessingStatusCol;
    public String intakeTabCleanedEmailCol;
    public String intakeTabExtractedFirstNameCol;
    public String intakeTabExtractedLastNameCol;
    public String intakeTabExtractedFundNameCol;
    public String intakeTabExtractedFundWebsiteCol;
    public String intakeTabConversationSummaryCol;
    public String intakeTabConversationLabelCol;
    public String intakeTabUpdatedCrmCol;
    public String intakeTabNeedsReviewCol;

    public String extraData;

    public CRMSchemaConfig(
        String configId,
        String userId,
        String crmName,
        String spreadsheetId
    )
    {
        this.configId = configId;
        this.userId = userId;
        this.crmName = crmName;
        this.spreadsheetId = spreadsheetId;

        this.mainTabName = "CRM";
        this.intakeTabName = "Email Intake";
        this.interactionsTabName = "Interactions";
        this.tasksTabName = "Tasks";

        this.mainTabHeaderRow = 1;
        this.mainTabDataStartRow = 2;

        this.intakeTabHeaderRow = 1;
        this.intakeTabDataStartRow = 2;

        this.extraData = "";
    }

    public void printSummary()
    {
        System.out.println("===== CRM SCHEMA CONFIG =====");

        System.out.println("Config ID: " + configId);
        System.out.println("User ID: " + userId);
        System.out.println("CRM Name: " + crmName);
        System.out.println("Spreadsheet ID: " + spreadsheetId);

        System.out.println();

        System.out.println("----- TABS -----");

        System.out.println("Main Tab Name: " + mainTabName);
        System.out.println("Intake Tab Name: " + intakeTabName);
        System.out.println("Interactions Tab Name: " + interactionsTabName);
        System.out.println("Tasks Tab Name: " + tasksTabName);

        System.out.println();

        System.out.println("----- MAIN CRM TAB -----");

        System.out.println("Main Tab Header Row: " + mainTabHeaderRow);
        System.out.println("Main Tab Data Start Row: " + mainTabDataStartRow);

        System.out.println();

        System.out.println("Fund Name Header: " + mainTabFundNameCol);

        System.out.println();

        System.out.println("Contact 1 First Name Header: " + mainTabContact1FirstNameCol);
        System.out.println("Contact 1 Last Name Header: " + mainTabContact1LastNameCol);
        System.out.println("Contact 1 Email Header: " + mainTabContact1EmailCol);
        System.out.println("Contact 1 Position Header: " + mainTabContact1PositionCol);
        System.out.println("Contact 1 LinkedIn Header: " + mainTabLinkedInCol);

        System.out.println();

        System.out.println("Contact 2 First Name Header: " + mainTabContact2FirstNameCol);
        System.out.println("Contact 2 Last Name Header: " + mainTabContact2LastNameCol);
        System.out.println("Contact 2 Email Header: " + mainTabContact2EmailCol);
        System.out.println("Contact 2 Position Header: " + mainTabContact2PositionCol);
        System.out.println("Contact 2 LinkedIn Header: " + mainTabLinkedIn2Col);

        System.out.println();

        System.out.println("Status Header: " + mainTabStatusCol);
        System.out.println("Type Of Investor Header: " + mainTabTypeOfInvestorCol);
        System.out.println("Website Header: " + mainTabWebsiteCol);
        System.out.println("Country Header: " + mainTabCountryCol);
        System.out.println("City Header: " + mainTabCityCol);

        System.out.println();

        System.out.println("Sector Tags Header: " + mainTabSectorTagsCol);
        System.out.println("Microsector Tags Header: " + mainTabMicrosectorTagsCol);
        System.out.println("Geography Header: " + mainTabGeographyCol);
        System.out.println("Prior Backed Funds Header: " + mainTabPriorBackedFundsCol);
        System.out.println("Intelligence JSON Header: " + mainTabIntelligenceJsonCol);
        System.out.println("Last Enriched At Header: " + mainTabLastEnrichedAtCol);
        System.out.println("Enrichment Status Header: " + mainTabEnrichmentStatusCol);
        System.out.println("Investment Thesis Header: " + mainTabInvestmentThesisCol);

        System.out.println();

        System.out.println("Last Contact Date Header: " + mainTabLastContactDateCol);
        System.out.println("Next Action Header: " + mainTabNextActionCol);
        System.out.println("Follow Up Recommendation Header: " + mainTabFollowUpRecommendationCol);
        System.out.println("Priority Score Header: " + mainTabPriorityScoreCol);
        System.out.println("Priority Reason Header: " + mainTabPriorityReasonCol);
        System.out.println("Priority Last Calculated At Header: " + mainTabPriorityLastCalculatedAtCol);

        System.out.println();

        System.out.println("Interaction History Header: " + mainTabInteractionHistoryCol);
        System.out.println("Notes Header: " + mainTabNotesCol);
        System.out.println("Comments Header: " + mainTabCommentsCol);

        System.out.println();

        System.out.println("Cold Email Header: " + mainTabColdEmailCol);
        System.out.println("Investor Profile Similarity Header: " + mainTabInvestorProfileSimilarityCol);

        System.out.println();

        System.out.println("Name LinkedIn Query Header: " + mainTabNameLinkedInQueryCol);
        System.out.println("Company LinkedIn Query Header: " + mainTabCompanyLinkedInQueryCol);

        System.out.println();

        System.out.println("----- EMAIL INTAKE TAB -----");

        System.out.println("Intake Tab Header Row: " + intakeTabHeaderRow);
        System.out.println("Intake Tab Data Start Row: " + intakeTabDataStartRow);

        System.out.println();

        System.out.println("Intake ID Header: " + intakeTabIntakeIdCol);
        System.out.println("Gmail Message ID Header: " + intakeTabGmailMessageIdCol);
        System.out.println("Gmail Thread ID Header: " + intakeTabGmailThreadIdCol);
        System.out.println("Intake Type Header: " + intakeTabIntakeTypeCol);
        System.out.println("Timestamp Header: " + intakeTabTimestampCol);

        System.out.println();

        System.out.println("To Header: " + intakeTabToCol);
        System.out.println("From Header: " + intakeTabFromCol);
        System.out.println("Subject Header: " + intakeTabSubjectCol);
        System.out.println("Body Header: " + intakeTabBodyCol);

        System.out.println();

        System.out.println("Processing Status Header: " + intakeTabProcessingStatusCol);
        System.out.println("Cleaned Email Header: " + intakeTabCleanedEmailCol);

        System.out.println();

        System.out.println("Extracted First Name Header: " + intakeTabExtractedFirstNameCol);
        System.out.println("Extracted Last Name Header: " + intakeTabExtractedLastNameCol);

        System.out.println();

        System.out.println("Extracted Fund Name Header: " + intakeTabExtractedFundNameCol);
        System.out.println("Extracted Fund Website Header: " + intakeTabExtractedFundWebsiteCol);

        System.out.println();

        System.out.println("Conversation Label Header: " + intakeTabConversationLabelCol);
        System.out.println("Conversation Summary Header: " + intakeTabConversationSummaryCol);

        System.out.println();

        System.out.println("Updated CRM Header: " + intakeTabUpdatedCrmCol);
        System.out.println("Needs Review Header: " + intakeTabNeedsReviewCol);

        System.out.println();

        System.out.println("----- EXTRA -----");

        System.out.println("Extra Data: " + extraData);

        System.out.println("================================");
    }
}