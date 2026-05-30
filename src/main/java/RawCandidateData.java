/*
 * RawCandidateData is the messy evidence object for Pipeline 3 Candidate Discovery.
 *
 * This class sits between scraping and clean candidate creation:
 * SERP result + LinkedIn scrape + website scrape -> RawCandidateData -> InvestorProfile -> CandidateInvestor.
 *
 * Keeping this object separate prevents CandidateInvestor from becoming a dumping ground
 * for raw HTML, raw API JSON, source snippets, and debug metadata.
 */
public class RawCandidateData
{
    public String candidateType; // PERSON or COMPANY

    public String name;
    public String firstName;
    public String lastName;
    public String position;
    public String fundName;

    public String linkedinProfileUrl;
    public String linkedinCompanyUrl;
    public String websiteUrl;

    public String serpTitle;
    public String serpSnippet;
    public String discoveryQuery;
    public int serpRank;

    public String rawLinkedInJson;
    public String rawWebsiteText;
    public String rawWebsiteJson;
    public String scrapedAt;

    public RawCandidateData()
    {
        candidateType = "";
        name = "";
        firstName = "";
        lastName = "";
        position = "";
        fundName = "";
        linkedinProfileUrl = "";
        linkedinCompanyUrl = "";
        websiteUrl = "";
        serpTitle = "";
        serpSnippet = "";
        discoveryQuery = "";
        serpRank = 0;
        rawLinkedInJson = "";
        rawWebsiteText = "";
        rawWebsiteJson = "";
        scrapedAt = java.time.Instant.now().toString();
    }

    public String buildEvidenceTextForOpenAI()
    {
        StringBuilder builder0 = new StringBuilder();

        builder0.append("===== DISCOVERY CONTEXT =====\n");
        builder0.append("Candidate Type: ").append(candidateType).append("\n");
        builder0.append("Discovery Query: ").append(discoveryQuery).append("\n");
        builder0.append("SERP Rank: ").append(serpRank).append("\n");
        builder0.append("SERP Title: ").append(serpTitle).append("\n");
        builder0.append("SERP Snippet: ").append(serpSnippet).append("\n\n");

        builder0.append("===== BASIC FIELDS =====\n");
        builder0.append("Name: ").append(name).append("\n");
        builder0.append("First Name: ").append(firstName).append("\n");
        builder0.append("Last Name: ").append(lastName).append("\n");
        builder0.append("Position: ").append(position).append("\n");
        builder0.append("Fund Name: ").append(fundName).append("\n");
        builder0.append("Website: ").append(websiteUrl).append("\n");
        builder0.append("LinkedIn Profile URL: ").append(linkedinProfileUrl).append("\n");
        builder0.append("LinkedIn Company URL: ").append(linkedinCompanyUrl).append("\n\n");

        if (!isBlank(rawLinkedInJson))
        {
            builder0.append("===== LINKEDIN JSON =====\n");
            builder0.append(limit(rawLinkedInJson, 30000)).append("\n\n");
        }

        if (!isBlank(rawWebsiteText))
        {
            builder0.append("===== WEBSITE TEXT =====\n");
            builder0.append(limit(rawWebsiteText, 30000)).append("\n\n");
        }

        return limit(builder0.toString(), InvestorProfileExtractor.MAX_TEXT_CHARS0);
    }

    public void printSummary()
    {
        System.out.println("===== RAW CANDIDATE DATA =====");
        System.out.println("Type: " + candidateType);
        System.out.println("Name: " + name);
        System.out.println("Fund Name: " + fundName);
        System.out.println("Position: " + position);
        System.out.println("Website: " + websiteUrl);
        System.out.println("LinkedIn Profile: " + linkedinProfileUrl);
        System.out.println("LinkedIn Company: " + linkedinCompanyUrl);
        System.out.println("Discovery Query: " + discoveryQuery);
        System.out.println("SERP Rank: " + serpRank);
    }

    public static boolean isBlank(String value0)
    {
        return value0 == null || value0.trim().length() == 0;
    }

    private static String limit(String value0, int maxChars0)
    {
        if (value0 == null)
        {
            return "";
        }

        if (value0.length() <= maxChars0)
        {
            return value0;
        }

        return value0.substring(0, maxChars0);
    }
}
