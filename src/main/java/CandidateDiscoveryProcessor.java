import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import org.json.JSONObject;

/*
 * CandidateDiscoveryProcessor orchestrates the first runnable version of Pipeline 3.
 *
 * Workflow:
 * 1. Input seed InvestorProfiles.
 * 2. Generate search queries from sectors, microsectors, geographies, and thesis.
 * 3. Run Bright Data SERP for each query.
 * 4. Extract LinkedIn person/company URLs.
 * 5. Scrape each LinkedIn URL with Bright Data LinkedIn Scraper API.
 * 6. If a company website is found, scrape the website.
 * 7. Ask InvestorProfileExtractor/OpenAI to turn all evidence into an InvestorProfile.
 * 8. Return CandidateInvestor objects.
 * 9. Optionally append non-duplicate candidates to the CRM with Conversation Status = Cold.
 */
public class CandidateDiscoveryProcessor
{
    private static final int MAX_COLUMNS0 = 200;
    private static final int MAX_CRM_ROWS0 = 2000;

    private SearchTermGenerator searchTermGenerator;
    private BrightDataSerpClient serpClient;
    private LinkedInUrlExtractor linkedInUrlExtractor;
    private BrightDataLinkedInClient linkedInClient;
    private InvestorProfileExtractor investorProfileExtractor;

    public CandidateDiscoveryProcessor()
    {
        searchTermGenerator = new SearchTermGenerator();
        serpClient = new BrightDataSerpClient();
        linkedInUrlExtractor = new LinkedInUrlExtractor();
        linkedInClient = new BrightDataLinkedInClient();
        investorProfileExtractor = new InvestorProfileExtractor();
    }

    public ArrayList<CandidateInvestor> discoverCandidates(
        ArrayList<InvestorProfile> seedProfiles0,
        int maxResultsPerQuery0,
        int maxCandidates0,
        boolean scrapeLinkedIn0,
        boolean scrapeWebsites0,
        boolean extractInvestorProfiles0) throws Exception
    {
        ArrayList<CandidateInvestor> candidates0 = new ArrayList<CandidateInvestor>();

        if (seedProfiles0 == null || seedProfiles0.size() == 0)
        {
            return candidates0;
        }

        ArrayList<String> queries0 = searchTermGenerator.generateCandidateDiscoveryQueries(seedProfiles0);

        System.out.println("Generated " + queries0.size() + " candidate discovery queries.");
        for (int i = 0; i < queries0.size(); i++)
        {
            System.out.println("Query " + (i + 1) + ": " + queries0.get(i));
        }

        LinkedHashMap<String, DiscoveredLinkedInTarget> targetMap0 = new LinkedHashMap<String, DiscoveredLinkedInTarget>();

        for (String query0 : queries0)
        {
            if (targetMap0.size() >= maxCandidates0)
            {
                break;
            }

            System.out.println("Running SERP query: " + query0);
            ArrayList<SerpResult> serpResults0 = serpClient.search(query0, maxResultsPerQuery0);
            ArrayList<DiscoveredLinkedInTarget> targets0 = linkedInUrlExtractor.extract(serpResults0);

            System.out.println("SERP results: " + serpResults0.size() + " | LinkedIn targets: " + targets0.size());

            for (DiscoveredLinkedInTarget target0 : targets0)
            {
                if (targetMap0.size() >= maxCandidates0)
                {
                    break;
                }

                if (!targetMap0.containsKey(target0.url))
                {
                    targetMap0.put(target0.url, target0);
                }
            }
        }

        int processedCount0 = 0;

        for (DiscoveredLinkedInTarget target0 : targetMap0.values())
        {
            processedCount0++;
            System.out.println("Building candidate " + processedCount0 + " of " + targetMap0.size() + ": " + target0.url);

            RawCandidateData raw0 = buildRawCandidateFromTarget(
                target0,
                scrapeLinkedIn0,
                scrapeWebsites0
            );

            CandidateInvestor candidate0 = buildCandidateFromRaw(raw0, extractInvestorProfiles0);
            candidates0.add(candidate0);
        }

        return dedupeCandidates(candidates0);
    }

    public String discoverAndAppendColdCandidates(
        SessionContext context0,
        ArrayList<InvestorProfile> seedProfiles0,
        int maxResultsPerQuery0,
        int maxCandidates0,
        boolean scrapeLinkedIn0,
        boolean scrapeWebsites0,
        boolean extractInvestorProfiles0) throws Exception
    {
        ArrayList<CandidateInvestor> candidates0 = discoverCandidates(
            seedProfiles0,
            maxResultsPerQuery0,
            maxCandidates0,
            scrapeLinkedIn0,
            scrapeWebsites0,
            extractInvestorProfiles0
        );

        return appendColdCandidatesToCrm(context0, candidates0);
    }

    private RawCandidateData buildRawCandidateFromTarget(
        DiscoveredLinkedInTarget target0,
        boolean scrapeLinkedIn0,
        boolean scrapeWebsites0)
    {
        RawCandidateData raw0 = new RawCandidateData();

        raw0.candidateType = target0.targetType;
        raw0.discoveryQuery = target0.queryUsed;
        raw0.serpTitle = target0.serpTitle;
        raw0.serpSnippet = target0.serpSnippet;
        raw0.serpRank = target0.serpRank;

        if (target0.isPerson())
        {
            raw0.linkedinProfileUrl = target0.url;
        }
        else
        {
            raw0.linkedinCompanyUrl = target0.url;
        }

        if (scrapeLinkedIn0)
        {
            try
            {
                LinkedInScrapeResult scrape0 = linkedInClient.scrape(target0);
                applyLinkedInScrape(raw0, scrape0);
            }
            catch (Exception exception0)
            {
                System.out.println("Skipping LinkedIn enrichment for " + target0.url + ": " + exception0.getMessage());
            }
        }
        else
        {
            inferBasicFieldsFromSerp(raw0);
        }

        if (scrapeWebsites0 && !RawCandidateData.isBlank(raw0.websiteUrl))
        {
            try
            {
                String normalizedWebsite0 = WebsiteCrawlerService.normalizeRootUrl(raw0.websiteUrl);
                java.util.LinkedHashMap<String, String> scrapedPages0 = WebsiteCrawlerService.crawlWebsite(normalizedWebsite0);
                raw0.websiteUrl = normalizedWebsite0;
                raw0.rawWebsiteText = InvestorProfileExtractor.buildOpenAiSourceText(scrapedPages0);
                raw0.rawWebsiteJson = WebsiteCrawlerService.buildOutputJson(normalizedWebsite0, scrapedPages0);
            }
            catch (Exception exception0)
            {
                System.out.println("Website scrape failed for " + raw0.websiteUrl + ": " + exception0.getMessage());
            }
        }

        return raw0;
    }

    private void applyLinkedInScrape(RawCandidateData raw0, LinkedInScrapeResult scrape0)
    {
        if (scrape0 == null)
        {
            return;
        }

        raw0.rawLinkedInJson = scrape0.rawJson;
        raw0.name = firstNonBlank(scrape0.name, raw0.name, raw0.serpTitle);
        raw0.firstName = firstNonBlank(scrape0.firstName, raw0.firstName, "");
        raw0.lastName = firstNonBlank(scrape0.lastName, raw0.lastName, "");
        raw0.position = firstNonBlank(scrape0.position, scrape0.headline, raw0.position);
        raw0.websiteUrl = firstNonBlank(scrape0.companyWebsite, raw0.websiteUrl, "");

        if (DiscoveredLinkedInTarget.TYPE_PERSON.equals(raw0.candidateType))
        {
            raw0.fundName = firstNonBlank(scrape0.currentCompanyName, raw0.fundName, "");
        }
        else
        {
            raw0.fundName = firstNonBlank(scrape0.name, scrape0.currentCompanyName, raw0.fundName);
        }
    }

    private void inferBasicFieldsFromSerp(RawCandidateData raw0)
    {
        if (!RawCandidateData.isBlank(raw0.serpTitle))
        {
            raw0.name = raw0.serpTitle;
        }

        if (DiscoveredLinkedInTarget.TYPE_COMPANY.equals(raw0.candidateType))
        {
            raw0.fundName = raw0.serpTitle;
        }
    }

    private CandidateInvestor buildCandidateFromRaw(
        RawCandidateData raw0,
        boolean extractInvestorProfile0)
    {
        CandidateInvestor candidate0 = CandidateInvestor.fromRawCandidateData(raw0);

        if (extractInvestorProfile0)
        {
            try
            {
                candidate0.ip = investorProfileExtractor.getInvestorProfile(raw0);
            }
            catch (Exception exception0)
            {
                System.out.println("InvestorProfile extraction failed for candidate " + candidate0.fundName + ": " + exception0.getMessage());
            }
        }

        return candidate0;
    }

    private ArrayList<CandidateInvestor> dedupeCandidates(ArrayList<CandidateInvestor> candidates0)
    {
        ArrayList<CandidateInvestor> deduped0 = new ArrayList<CandidateInvestor>();
        HashSet<String> seen0 = new HashSet<String>();

        for (CandidateInvestor candidate0 : candidates0)
        {
            String key0 = candidate0.getDeduplicationKey();
            if (RawCandidateData.isBlank(key0))
            {
                key0 = candidate0.linkedInUrl;
            }

            if (!seen0.contains(key0))
            {
                seen0.add(key0);
                deduped0.add(candidate0);
            }
        }

        return deduped0;
    }

    /*
     * Appends discovered candidates into the user's CRM as Cold rows.
     * This is intentionally conservative: it only fills columns that definitely exist in
     * your schema config, and it skips candidates already present by LinkedIn URL, website,
     * fund name, or person name.
     */
    public static String appendColdCandidatesToCrm(
        SessionContext context0,
        ArrayList<CandidateInvestor> candidates0) throws Exception
    {
        if (context0 == null || context0.config == null)
        {
            return "ERROR: Missing session context.";
        }

        if (candidates0 == null || candidates0.size() == 0)
        {
            return "Candidate discovery complete. No candidates to append.";
        }

        String spreadsheetId0 = context0.config.spreadsheetId;
        String crmTabName0 = context0.config.mainTabName;

        java.util.HashMap<String, Integer> crmHeaderMap0 = SheetsApp.buildHeaderMap(
            spreadsheetId0,
            crmTabName0,
            context0.config.mainTabHeaderRow,
            MAX_COLUMNS0
        );

        int width0 = getMaxHeaderColumn(crmHeaderMap0);
        if (width0 <= 0)
        {
            return "ERROR: Could not read CRM headers.";
        }

        String[][] crmData0 = SheetsApp.readRangeMatrix(
            spreadsheetId0,
            crmTabName0,
            1,
            1,
            MAX_CRM_ROWS0,
            width0
        );

        ArrayList<CandidateInvestor> newCandidates0 = new ArrayList<CandidateInvestor>();

        for (CandidateInvestor candidate0 : candidates0)
        {
            if (!candidateExistsInCrm(candidate0, crmData0, crmHeaderMap0, context0.config))
            {
                newCandidates0.add(candidate0);
            }
        }

        if (newCandidates0.size() == 0)
        {
            return "Candidate discovery complete. All candidates already exist in CRM.";
        }

        /*
         * Score newly discovered candidates before writing them to the CRM.
         *
         * Candidate discovery should not just create Cold rows; it should create
         * prioritized Cold rows. This uses CandidateScorer's CRM-derived overload,
         * which builds an average basis profile from existing CRM investors with
         * Conversation Status of First Interest or better.
         *
         * If OpenAI scoring fails, CandidateScorer falls back to deterministic
         * scoring. If no usable InvestorProfile exists on a candidate, that
         * candidate will keep score 0.0 and a blank explanation.
         */
        try
        {
            CandidateScorer scorer0 = new CandidateScorer();
            scorer0.scoreCandidates(context0, newCandidates0);
        }
        catch (Exception exception0)
        {
            System.out.println("Candidate scoring before CRM append failed: " + exception0.getMessage());
            System.out.println("Continuing CRM append without blocking candidate creation.");
        }

        int firstCol0 = 1;
        int lastCol0 = width0;
        int nextRow0 = SheetsApp.findLastRow(
            spreadsheetId0,
            crmTabName0,
            firstCol0,
            lastCol0,
            MAX_CRM_ROWS0
        ) + 1;

        if (nextRow0 < context0.config.mainTabDataStartRow)
        {
            nextRow0 = context0.config.mainTabDataStartRow;
        }

        String[][] rows0 = new String[newCandidates0.size()][width0];

        for (int i = 0; i < rows0.length; i++)
        {
            for (int j = 0; j < width0; j++)
            {
                rows0[i][j] = "";
            }

            fillCrmRow(rows0[i], crmHeaderMap0, context0.config, newCandidates0.get(i));
        }

        SheetsApp.updateRangeMatrix(
            spreadsheetId0,
            crmTabName0,
            nextRow0,
            1,
            rows0
        );

        return "Candidate discovery complete. New cold CRM rows added: " + newCandidates0.size() + ".";
    }

    private static void fillCrmRow(
        String[] row0,
        java.util.HashMap<String, Integer> headerMap0,
        CRMSchemaConfig config0,
        CandidateInvestor candidate0)
    {
        put(row0, headerMap0, config0.mainTabFundNameCol, candidate0.fundName);
        put(row0, headerMap0, config0.mainTabContact1FirstNameCol, candidate0.firstName);
        put(row0, headerMap0, config0.mainTabContact1LastNameCol, candidate0.lastName);
        put(row0, headerMap0, config0.mainTabContact1PositionCol, candidate0.position);
        put(row0, headerMap0, config0.mainTabStatusCol, "Cold");
        put(row0, headerMap0, config0.mainTabWebsiteCol, candidate0.website);
        put(row0, headerMap0, config0.mainTabLinkedInCol, candidate0.linkedInUrl);
        put(row0, headerMap0, config0.mainTabNotesCol, candidate0.discoveryReason);
        put(row0, headerMap0, config0.mainTabInvestorProfileSimilarityCol, formatScore(candidate0.finalScore));
        put(row0, headerMap0, config0.mainTabCommentsCol, candidate0.scoreExplanation);
        put(row0, headerMap0, config0.mainTabLastEnrichedAtCol, java.time.Instant.now().toString());
        put(row0, headerMap0, config0.mainTabEnrichmentStatusCol, candidate0.hasInvestorProfile() ? "COMPLETED" : "DISCOVERED");

        if (candidate0.ip != null)
        {
            put(row0, headerMap0, config0.mainTabTypeOfInvestorCol, candidate0.ip.allocatorType);
            put(row0, headerMap0, config0.mainTabSectorTagsCol, InvestorProfile.joinWithPipe(candidate0.ip.sectors));
            put(row0, headerMap0, config0.mainTabMicrosectorTagsCol, InvestorProfile.joinWithPipe(candidate0.ip.microsectors));
            put(row0, headerMap0, config0.mainTabGeographyCol, InvestorProfile.joinWithPipe(candidate0.ip.geographies));
            put(row0, headerMap0, config0.mainTabPriorBackedFundsCol, InvestorProfile.joinWithPipe(candidate0.ip.priorBackedFunds));
            put(row0, headerMap0, config0.mainTabInvestmentThesisCol, candidate0.ip.investmentThesis);
            put(row0, headerMap0, config0.mainTabIntelligenceJsonCol, candidate0.ip.intelligenceJson);
        }
        else
        {
            JSONObject minimalJson0 = new JSONObject();
            minimalJson0.put("discovery_query", candidate0.discoveryQuery);
            minimalJson0.put("discovery_reason", candidate0.discoveryReason);
            minimalJson0.put("linkedin_url", candidate0.linkedInUrl);
            minimalJson0.put("website", candidate0.website);
            minimalJson0.put("status", "discovered_not_profile_extracted");
            put(row0, headerMap0, config0.mainTabIntelligenceJsonCol, minimalJson0.toString());
        }
    }

    private static boolean candidateExistsInCrm(
        CandidateInvestor candidate0,
        String[][] crmData0,
        java.util.HashMap<String, Integer> headerMap0,
        CRMSchemaConfig config0)
    {
        int fundCol0 = getColumn(headerMap0, config0.mainTabFundNameCol);
        int linkedinCol0 = getColumn(headerMap0, config0.mainTabLinkedInCol);
        int linkedin2Col0 = getColumn(headerMap0, config0.mainTabLinkedIn2Col);
        int websiteCol0 = getColumn(headerMap0, config0.mainTabWebsiteCol);
        int firstNameCol0 = getColumn(headerMap0, config0.mainTabContact1FirstNameCol);
        int lastNameCol0 = getColumn(headerMap0, config0.mainTabContact1LastNameCol);

        for (int row = config0.mainTabDataStartRow; row <= crmData0.length; row++)
        {
            String existingLinkedIn0 = getCell(crmData0, row, linkedinCol0);
            String existingLinkedIn20 = getCell(crmData0, row, linkedin2Col0);
            String existingWebsite0 = getCell(crmData0, row, websiteCol0);
            String existingFund0 = getCell(crmData0, row, fundCol0);
            String existingFirst0 = getCell(crmData0, row, firstNameCol0);
            String existingLast0 = getCell(crmData0, row, lastNameCol0);

            if (!RawCandidateData.isBlank(candidate0.linkedInUrl) &&
                (normalizeUrl(candidate0.linkedInUrl).equals(normalizeUrl(existingLinkedIn0)) ||
                 normalizeUrl(candidate0.linkedInUrl).equals(normalizeUrl(existingLinkedIn20))))
            {
                return true;
            }

            if (!RawCandidateData.isBlank(candidate0.website) &&
                normalizeUrl(candidate0.website).equals(normalizeUrl(existingWebsite0)))
            {
                return true;
            }

            if (!RawCandidateData.isBlank(candidate0.fundName) &&
                normalizeText(candidate0.fundName).equals(normalizeText(existingFund0)))
            {
                return true;
            }

            if (!RawCandidateData.isBlank(candidate0.firstName) &&
                !RawCandidateData.isBlank(candidate0.lastName) &&
                normalizeText(candidate0.firstName).equals(normalizeText(existingFirst0)) &&
                normalizeText(candidate0.lastName).equals(normalizeText(existingLast0)))
            {
                return true;
            }
        }

        return false;
    }

    public static ArrayList<InvestorProfile> buildSeedProfilesFromClientInput(
        String sectors0,
        String microsectors0,
        String geographies0,
        String thesis0)
    {
        ArrayList<InvestorProfile> profiles0 = new ArrayList<InvestorProfile>();
        InvestorProfile profile0 = new InvestorProfile();
        profile0.sectors = splitPipeOrComma(sectors0);
        profile0.microsectors = splitPipeOrComma(microsectors0);
        profile0.geographies = splitPipeOrComma(geographies0);
        profile0.investmentThesis = thesis0 == null ? "" : thesis0;
        profiles0.add(profile0);
        return profiles0;
    }

    private static String[] splitPipeOrComma(String value0)
    {
        if (RawCandidateData.isBlank(value0))
        {
            return new String[0];
        }

        String[] pieces0 = value0.split("[|,]");
        ArrayList<String> cleaned0 = new ArrayList<String>();

        for (String piece0 : pieces0)
        {
            if (!RawCandidateData.isBlank(piece0))
            {
                cleaned0.add(piece0.trim());
            }
        }

        return cleaned0.toArray(new String[0]);
    }

    private static String formatScore(double score0)
    {
        if (Double.isNaN(score0) || Double.isInfinite(score0))
        {
            return "0.00";
        }

        if (score0 < 0.0)
        {
            score0 = 0.0;
        }

        if (score0 > 1.0)
        {
            score0 = 1.0;
        }

        return String.format("%.2f", score0);
    }

    private static void put(String[] row0, java.util.HashMap<String, Integer> headerMap0, String header0, String value0)
    {
        int col0 = getColumn(headerMap0, header0);
        if (col0 == -1)
        {
            return;
        }

        int index0 = col0 - 1;
        if (index0 >= 0 && index0 < row0.length)
        {
            row0[index0] = value0 == null ? "" : value0;
        }
    }

    private static int getColumn(java.util.HashMap<String, Integer> headerMap0, String header0)
    {
        if (headerMap0 == null || RawCandidateData.isBlank(header0))
        {
            return -1;
        }

        Integer col0 = headerMap0.get(header0.trim());
        return col0 == null ? -1 : col0;
    }

    private static String getCell(String[][] data0, int rowNumber0, int oneBasedColumn0)
    {
        int rowIndex0 = rowNumber0 - 1;
        int colIndex0 = oneBasedColumn0 - 1;

        if (rowIndex0 < 0 || rowIndex0 >= data0.length || colIndex0 < 0 || colIndex0 >= data0[rowIndex0].length)
        {
            return "";
        }

        return data0[rowIndex0][colIndex0] == null ? "" : data0[rowIndex0][colIndex0].trim();
    }

    private static int getMaxHeaderColumn(java.util.HashMap<String, Integer> headerMap0)
    {
        int max0 = 0;
        for (Integer value0 : headerMap0.values())
        {
            if (value0 != null && value0 > max0)
            {
                max0 = value0;
            }
        }
        return max0;
    }

    private static String normalizeText(String value0)
    {
        return value0 == null ? "" : value0.toLowerCase().replaceAll("[^a-z0-9]", "").trim();
    }

    private static String normalizeUrl(String value0)
    {
        if (value0 == null)
        {
            return "";
        }

        return value0.toLowerCase()
            .replace("https://", "")
            .replace("http://", "")
            .replace("www.", "")
            .replaceAll("/$", "")
            .trim();
    }

    private static String firstNonBlank(String a0, String b0, String c0)
    {
        if (!RawCandidateData.isBlank(a0)) return a0;
        if (!RawCandidateData.isBlank(b0)) return b0;
        if (!RawCandidateData.isBlank(c0)) return c0;
        return "";
    }
}
