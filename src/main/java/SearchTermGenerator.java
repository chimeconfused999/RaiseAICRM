import java.util.ArrayList;
import java.util.LinkedHashSet;

/*
 * SearchTermGenerator turns InvestorProfile tags into deterministic Google queries.
 *
 * Version 1 intentionally avoids OpenAI. The goal is a reliable, debuggable query
 * generator before adding AI-generated search strategies later.
 */
public class SearchTermGenerator
{
    public static final int DEFAULT_MAX_QUERIES = 20;

    public SearchTermGenerator()
    {
    }

    public ArrayList<String> generateCandidateDiscoveryQueries(
        ArrayList<InvestorProfile> seedProfiles0)
    {
        return generateCandidateDiscoveryQueries(seedProfiles0, DEFAULT_MAX_QUERIES);
    }

    public ArrayList<String> generateCandidateDiscoveryQueries(
        ArrayList<InvestorProfile> seedProfiles0,
        int maxQueries0)
    {
        LinkedHashSet<String> queries0 = new LinkedHashSet<String>();

        if (seedProfiles0 == null || seedProfiles0.size() == 0)
        {
            return new ArrayList<String>(queries0);
        }

        for (InvestorProfile profile0 : seedProfiles0)
        {
            if (profile0 == null)
            {
                continue;
            }

            addQueriesFromTags(queries0, profile0.microsectors, profile0.geographies, true);
            addQueriesFromTags(queries0, profile0.sectors, profile0.geographies, false);
            addQueriesFromThesis(queries0, profile0.investmentThesis, profile0.geographies);

            if (queries0.size() >= maxQueries0)
            {
                break;
            }
        }

        ArrayList<String> result0 = new ArrayList<String>();
        for (String query0 : queries0)
        {
            result0.add(query0);
            if (result0.size() >= maxQueries0)
            {
                break;
            }
        }

        return result0;
    }

    public ArrayList<String> generateFundWebsiteQueries(String fundName0)
    {
        ArrayList<String> queries0 = new ArrayList<String>();

        if (isBlank(fundName0))
        {
            return queries0;
        }

        String q0 = quote(fundName0);
        queries0.add(q0 + " official website");
        queries0.add(q0 + " venture capital website");
        queries0.add(q0 + " investment firm website");
        queries0.add("site:linkedin.com/company " + q0);

        return queries0;
    }

    public ArrayList<String> generatePersonLinkedInQueries(
        String firstName0,
        String lastName0,
        String fundName0)
    {
        ArrayList<String> queries0 = new ArrayList<String>();

        String fullName0 = (safeString(firstName0) + " " + safeString(lastName0)).trim();

        if (isBlank(fullName0))
        {
            return queries0;
        }

        if (!isBlank(fundName0))
        {
            queries0.add("site:linkedin.com/in " + quote(fullName0) + " " + quote(fundName0));
        }

        queries0.add("site:linkedin.com/in " + quote(fullName0) + " investor");
        queries0.add("site:linkedin.com/in " + quote(fullName0) + " partner");

        return queries0;
    }

    private void addQueriesFromTags(
        LinkedHashSet<String> queries0,
        String[] tags0,
        String[] geographies0,
        boolean microsector0)
    {
        if (tags0 == null)
        {
            return;
        }

        for (String tag0 : tags0)
        {
            if (isBlank(tag0))
            {
                continue;
            }

            String quotedTag0 = quote(tag0);
            String geography0 = firstNonBlank(geographies0);

            queries0.add("site:linkedin.com/company " + quotedTag0 + " \"venture capital\"");
            queries0.add("site:linkedin.com/company " + quotedTag0 + " \"seed fund\"");
            queries0.add("site:linkedin.com/in " + quotedTag0 + " investor");
            queries0.add("site:linkedin.com/in " + quotedTag0 + " \"venture partner\"");
            queries0.add("site:linkedin.com/in " + quotedTag0 + " \"seed investor\"");

            if (!isBlank(geography0))
            {
                queries0.add("site:linkedin.com/in " + quotedTag0 + " investor " + quote(geography0));
                queries0.add("site:linkedin.com/company " + quotedTag0 + " fund " + quote(geography0));
            }

            if (!microsector0)
            {
                queries0.add("site:linkedin.com/in " + quotedTag0 + " \"general partner\"");
            }
        }
    }

    private void addQueriesFromThesis(
        LinkedHashSet<String> queries0,
        String thesis0,
        String[] geographies0)
    {
        if (isBlank(thesis0))
        {
            return;
        }

        String[] usefulPhrases0 = extractUsefulPhrases(thesis0);
        String geography0 = firstNonBlank(geographies0);

        for (String phrase0 : usefulPhrases0)
        {
            if (isBlank(phrase0))
            {
                continue;
            }

            queries0.add("site:linkedin.com/in " + quote(phrase0) + " investor");

            if (!isBlank(geography0))
            {
                queries0.add("site:linkedin.com/in " + quote(phrase0) + " investor " + quote(geography0));
            }
        }
    }

    private String[] extractUsefulPhrases(String thesis0)
    {
        ArrayList<String> phrases0 = new ArrayList<String>();
        String lower0 = thesis0.toLowerCase();

        String[] candidates0 = new String[]
        {
            "ai", "artificial intelligence", "b2b saas", "enterprise software",
            "vertical saas", "developer tools", "sales automation", "crm",
            "fintech", "healthcare", "climate", "deep tech", "seed"
        };

        for (String candidate0 : candidates0)
        {
            if (lower0.contains(candidate0))
            {
                phrases0.add(candidate0);
            }
        }

        return phrases0.toArray(new String[0]);
    }

    private String firstNonBlank(String[] values0)
    {
        if (values0 == null)
        {
            return "";
        }

        for (String value0 : values0)
        {
            if (!isBlank(value0))
            {
                return value0.trim();
            }
        }

        return "";
    }

    private String quote(String value0)
    {
        return "\"" + safeString(value0).replace("\"", "") + "\"";
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
