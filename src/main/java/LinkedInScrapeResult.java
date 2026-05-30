import org.json.JSONArray;
import org.json.JSONObject;

/*
 * LinkedInScrapeResult is the normalized output from BrightDataLinkedInClient.
 *
 * Bright Data returns different JSON shapes for people and companies. This object keeps
 * the raw JSON, but also pulls out the fields the rest of Pipeline 3 needs immediately:
 * names, role/headline, company/fund name, location, and website.
 */
public class LinkedInScrapeResult
{
    public String url;
    public String targetType;
    public String name;
    public String firstName;
    public String lastName;
    public String headline;
    public String position;
    public String currentCompanyName;
    public String companyWebsite;
    public String location;
    public String about;
    public String rawJson;

    public LinkedInScrapeResult()
    {
        url = "";
        targetType = "";
        name = "";
        firstName = "";
        lastName = "";
        headline = "";
        position = "";
        currentCompanyName = "";
        companyWebsite = "";
        location = "";
        about = "";
        rawJson = "";
    }

    /*
     * Creates a normalized result from one Bright Data JSON record.
     */
    public static LinkedInScrapeResult fromJson(
        String url0,
        String targetType0,
        JSONObject json0)
    {
        LinkedInScrapeResult result0 = new LinkedInScrapeResult();

        result0.url = safeString(url0);
        result0.targetType = safeString(targetType0);

        if (json0 == null)
        {
            return result0;
        }

        result0.rawJson = json0.toString();
        result0.name = firstNonBlank(
            json0.optString("name", ""),
            json0.optString("full_name", ""),
            json0.optString("company_name", ""),
            json0.optString("title", "")
        );
        result0.firstName = json0.optString("first_name", "");
        result0.lastName = json0.optString("last_name", "");
        result0.headline = firstNonBlank(
            json0.optString("headline", ""),
            json0.optString("position", ""),
            json0.optString("subtitle", "")
        );
        result0.position = json0.optString("position", result0.headline);
        result0.location = firstNonBlank(
            json0.optString("location", ""),
            json0.optString("city", ""),
            json0.optString("country", "")
        );
        result0.about = firstNonBlank(
            json0.optString("about", ""),
            json0.optString("description", ""),
            json0.optString("overview", "")
        );
        result0.companyWebsite = firstNonBlank(
            json0.optString("website", ""),
            json0.optString("company_website", ""),
            json0.optString("url_website", "")
        );

        JSONObject currentCompany0 = json0.optJSONObject("current_company");
        if (currentCompany0 != null)
        {
            result0.currentCompanyName = firstNonBlank(
                currentCompany0.optString("name", ""),
                currentCompany0.optString("company_name", "")
            );
        }

        result0.currentCompanyName = firstNonBlank(
            result0.currentCompanyName,
            json0.optString("current_company_name", ""),
            json0.optString("company", ""),
            json0.optString("company_name", "")
        );

        if (isBlank(result0.companyWebsite))
        {
            JSONArray websites0 = json0.optJSONArray("websites");
            if (websites0 != null && websites0.length() > 0)
            {
                result0.companyWebsite = websites0.optString(0, "");
            }
        }

        return result0;
    }

    public void printSummary()
    {
        System.out.println("===== LINKEDIN SCRAPE RESULT =====");
        System.out.println("Type: " + targetType);
        System.out.println("URL: " + url);
        System.out.println("Name: " + name);
        System.out.println("Position: " + position);
        System.out.println("Current Company: " + currentCompanyName);
        System.out.println("Website: " + companyWebsite);
        System.out.println("Location: " + location);
    }

    private static String firstNonBlank(String a0, String b0, String c0, String d0)
    {
        if (!isBlank(a0)) return a0;
        if (!isBlank(b0)) return b0;
        if (!isBlank(c0)) return c0;
        if (!isBlank(d0)) return d0;
        return "";
    }

    /*
     * Two-value overload used when a nested Bright Data object only has two
     * possible names for the same field.
     */
    private static String firstNonBlank(String a0, String b0)
    {
        if (!isBlank(a0)) return a0;
        if (!isBlank(b0)) return b0;
        return "";
    }

    private static String firstNonBlank(String a0, String b0, String c0)
    {
        if (!isBlank(a0)) return a0;
        if (!isBlank(b0)) return b0;
        if (!isBlank(c0)) return c0;
        return "";
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
