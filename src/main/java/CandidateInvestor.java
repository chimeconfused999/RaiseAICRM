import java.util.HashMap;

/*
 * CandidateInvestor represents one possible new investor discovered by Pipeline 3.
 *
 * Candidate Discovery now uses this as the clean final object, not as the raw input.
 * Raw SERP/LinkedIn/website evidence lives in RawCandidateData first. Then the pipeline
 * converts RawCandidateData into CandidateInvestor.
 *
 * Version 1 fields support:
 * - CRM creation as Conversation Status = Cold
 * - LinkedIn person/company URL storage
 * - optional InvestorProfile extraction
 * - later similarity scoring through CandidateScorer
 */
public class CandidateInvestor
{
    public String candidateType; // PERSON or COMPANY

    public String fundName;
    public String website;
    public String linkedInUrl;
    public String linkedInProfileUrl;
    public String linkedInCompanyUrl;

    public String name;
    public String firstName;
    public String lastName;
    public String position;

    public String discoveryQuery;
    public String discoveryReason;
    public String conversationStatus;

    public InvestorProfile ip;

    public double finalScore;
    public HashMap<String, Double> subscores;
    public String scoreExplanation;

    public CandidateInvestor(String fundName0, String website0, String linkedInUrl0)
    {
        candidateType = "";
        fundName = safeString(fundName0);
        website = safeString(website0);
        linkedInUrl = safeString(linkedInUrl0);
        linkedInProfileUrl = "";
        linkedInCompanyUrl = "";
        name = "";
        firstName = "";
        lastName = "";
        position = "";
        discoveryQuery = "";
        discoveryReason = "";
        conversationStatus = "Cold";

        ip = null;
        finalScore = 0.0;
        subscores = new HashMap<String, Double>();
        scoreExplanation = "";
    }

    public CandidateInvestor(String fundName0, String website0)
    {
        this(fundName0, website0, "");
    }

    /*
     * Main constructor for Pipeline 3 discovery. It converts messy raw evidence into
     * a clean CRM-ready candidate.
     */
    public static CandidateInvestor fromRawCandidateData(RawCandidateData raw0)
    {
        if (raw0 == null)
        {
            return new CandidateInvestor("", "", "");
        }

        String bestLinkedInUrl0 = !isBlank(raw0.linkedinProfileUrl)
            ? raw0.linkedinProfileUrl
            : raw0.linkedinCompanyUrl;

        CandidateInvestor candidate0 = new CandidateInvestor(
            raw0.fundName,
            raw0.websiteUrl,
            bestLinkedInUrl0
        );

        candidate0.candidateType = safeString(raw0.candidateType);
        candidate0.linkedInProfileUrl = safeString(raw0.linkedinProfileUrl);
        candidate0.linkedInCompanyUrl = safeString(raw0.linkedinCompanyUrl);
        candidate0.name = safeString(raw0.name);
        candidate0.firstName = safeString(raw0.firstName);
        candidate0.lastName = safeString(raw0.lastName);
        candidate0.position = safeString(raw0.position);
        candidate0.discoveryQuery = safeString(raw0.discoveryQuery);
        candidate0.discoveryReason = buildDiscoveryReason(raw0);
        candidate0.conversationStatus = "Cold";

        if (isBlank(candidate0.fundName) && DiscoveredLinkedInTarget.TYPE_COMPANY.equals(candidate0.candidateType))
        {
            candidate0.fundName = candidate0.name;
        }

        if (isBlank(candidate0.name))
        {
            candidate0.name = candidate0.fundName;
        }

        splitNameIfNeeded(candidate0);

        return candidate0;
    }

    public boolean hasWebsite()
    {
        return website != null && website.trim().length() > 0;
    }

    public boolean hasInvestorProfile()
    {
        return ip != null;
    }

    public String getDeduplicationKey()
    {
        String source0 = linkedInUrl;

        if (isBlank(source0))
        {
            source0 = website;
        }

        if (isBlank(source0))
        {
            source0 = fundName;
        }

        if (isBlank(source0))
        {
            source0 = firstName + lastName + position;
        }

        return normalize(source0);
    }

    public void printSummary()
    {
        System.out.println("===== CANDIDATE INVESTOR =====");
        System.out.println("Type: " + candidateType);
        System.out.println("Name: " + name);
        System.out.println("First Name: " + firstName);
        System.out.println("Last Name: " + lastName);
        System.out.println("Position: " + position);
        System.out.println("Fund Name: " + fundName);
        System.out.println("Website: " + website);
        System.out.println("LinkedIn URL: " + linkedInUrl);
        System.out.println("LinkedIn Profile URL: " + linkedInProfileUrl);
        System.out.println("LinkedIn Company URL: " + linkedInCompanyUrl);
        System.out.println("Conversation Status: " + conversationStatus);
        System.out.println("Discovery Query: " + discoveryQuery);
        System.out.println("Discovery Reason: " + discoveryReason);
        System.out.println("Final Score: " + finalScore);
        System.out.println("Subscores: " + subscores);
        System.out.println("Score Explanation: " + scoreExplanation);

        if (ip == null)
        {
            System.out.println("Investor Profile: null");
        }
        else
        {
            ip.printSummary();
        }
    }

    private static String buildDiscoveryReason(RawCandidateData raw0)
    {
        String reason0 = "Found through SERP query";

        if (!isBlank(raw0.discoveryQuery))
        {
            reason0 += ": " + raw0.discoveryQuery;
        }

        if (!isBlank(raw0.serpSnippet))
        {
            reason0 += " | SERP snippet: " + raw0.serpSnippet;
        }
        else if (!isBlank(raw0.serpTitle))
        {
            reason0 += " | SERP title: " + raw0.serpTitle;
        }

        return reason0;
    }

    private static void splitNameIfNeeded(CandidateInvestor candidate0)
    {
        if (!isBlank(candidate0.firstName) || isBlank(candidate0.name))
        {
            return;
        }

        String cleaned0 = candidate0.name.replace("| LinkedIn", "").trim();
        String[] pieces0 = cleaned0.split("\\s+");

        if (pieces0.length >= 1)
        {
            candidate0.firstName = pieces0[0];
        }

        if (pieces0.length >= 2)
        {
            candidate0.lastName = pieces0[pieces0.length - 1];
        }
    }

    private static String normalize(String value0)
    {
        if (value0 == null)
        {
            return "";
        }

        return value0
            .toLowerCase()
            .replace("https://", "")
            .replace("http://", "")
            .replace("www.", "")
            .replaceAll("[^a-z0-9]", "")
            .trim();
    }

    private static boolean isBlank(String value0)
    {
        return value0 == null || value0.trim().length() == 0;
    }

    private static String safeString(String value0)
    {
        return value0 == null ? "" : value0;
    }
}
