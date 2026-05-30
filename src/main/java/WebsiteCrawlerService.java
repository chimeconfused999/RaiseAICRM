import java.net.URI;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebsiteCrawlerService {

    private static final String BRIGHT_DATA_API_TOKEN = System.getenv("BRIGHT_DATA_API_TOKEN");
    private static final String BRIGHT_DATA_ZONE = "web_unlocker1";

    private static final int MAX_PAGES_TO_SCRAPE = 3;

    private static final HttpClient client = HttpClient.newHttpClient();

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Usage: java GenericWebsiteCrawler <websiteUrl>");
            return;
        }

        String rootUrl = normalizeRootUrl(args[0]);

        System.out.println("Starting crawl: " + rootUrl);

        LinkedHashMap<String, String> scrapedPages = crawlWebsite(rootUrl);

        System.out.println("\n===== SCRAPED PAGES =====");
        for (String url : scrapedPages.keySet()) {
            System.out.println(url + " | chars: " + scrapedPages.get(url).length());
        }

        String outputJson = buildOutputJson(rootUrl, scrapedPages);
        System.out.println("\n===== OUTPUT JSON =====");
        System.out.println(outputJson);
    }

    public static LinkedHashMap<String, String> crawlWebsite(String rootUrl) throws Exception {
        LinkedHashMap<String, String> scrapedPages = new LinkedHashMap<>();

        String homeHtml = scrapeUrl(rootUrl);
        scrapedPages.put(rootUrl, homeHtml);

        List<String> allLinks = extractLinks(homeHtml, rootUrl);
        List<String> usefulLinks = filterUsefulLinks(allLinks, rootUrl);

        System.out.println("Found links: " + allLinks.size());
        System.out.println("Useful links selected: " + usefulLinks.size());

        for (String link : usefulLinks) {
            if (scrapedPages.size() >= MAX_PAGES_TO_SCRAPE) {
                break;
            }

            if (scrapedPages.containsKey(link)) {
                continue;
            }

            try {
                System.out.println("Scraping: " + link);
                String html = scrapeUrl(link);
                scrapedPages.put(link, html);
                Thread.sleep(500);
            } catch (Exception e) {
                System.out.println("Failed to scrape: " + link);
                System.out.println("Reason: " + e.getMessage());
            }
        }

        return scrapedPages;
    }

    public static String scrapeUrl(String targetUrl) throws Exception {
        if (BRIGHT_DATA_API_TOKEN == null || BRIGHT_DATA_API_TOKEN.isBlank()) {
            throw new RuntimeException("Missing BRIGHT_DATA_API_TOKEN environment variable.");
        }

        String body = "{"
                + "\"zone\":\"" + escapeJson(BRIGHT_DATA_ZONE) + "\","
                + "\"url\":\"" + escapeJson(targetUrl) + "\","
                + "\"format\":\"raw\""
                + "}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.brightdata.com/request"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + BRIGHT_DATA_API_TOKEN)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        String responseBody0 = response.body();

        if (responseBody0.startsWith("Request Failed"))
        {
            throw new RuntimeException("Bright Data returned failure body: " + responseBody0);
        }

        if (responseBody0.toLowerCase().contains("requested site is not available for immediate access mode"))
        {
            throw new RuntimeException("Bright Data robots/immediate-access failure: " + responseBody0);
        }

        int statusCode = response.statusCode();

        if (statusCode < 200 || statusCode >= 300) {
            throw new RuntimeException("Bright Data request failed. Status: "
                    + statusCode + ". Body: " + response.body());
        }

        return responseBody0;
    }

    public static List<String> extractLinks(String html, String rootUrl) throws Exception {
        LinkedHashSet<String> links = new LinkedHashSet<>();

        Pattern pattern = Pattern.compile("href\\s*=\\s*[\"']([^\"'#]+)[\"']", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(html);

        URI rootUri = URI.create(rootUrl);

        while (matcher.find()) {
            String rawHref = matcher.group(1).trim();

            if (rawHref.isBlank()) {
                continue;
            }

            if (rawHref.startsWith("mailto:")
                    || rawHref.startsWith("tel:")
                    || rawHref.startsWith("javascript:")
                    || rawHref.startsWith("#")) {
                continue;
            }

            try {
                URI resolved = rootUri.resolve(rawHref);
                String normalized = normalizeUrl(resolved.toString());

                if (!normalized.isBlank()) {
                    links.add(normalized);
                }

            } catch (Exception ignored) {
            }
        }

        return new ArrayList<>(links);
    }

    public static List<String> filterUsefulLinks(List<String> links, String rootUrl) throws Exception {
        String rootHost = URI.create(rootUrl).getHost();
        if (rootHost == null) {
            return new ArrayList<>();
        }

        rootHost = stripWww(rootHost);

        List<String> blockedPatterns0 = Arrays.asList(
                "/feed",
                "/wp-json",
                "/xmlrpc.php",
                "/wp-content/",
                "/wp-includes/",
                "/wp-admin/",
                "/plugins/",
                "/themes/",
                "/fonts/",
                "/uploads/",
                "oembed",
                "rsd",
                ".woff",
                ".woff2",
                ".ttf",
                ".otf"
        );

        List<String> usefulKeywords = Arrays.asList(
                "about",
                "mission",
                "vision",
                "strategy",
                "strategies",
                "investment",
                "investments",
                "investing",
                "portfolio",
                "companies",
                "team",
                "people",
                "leadership",
                "grants",
                "grant",
                "programs",
                "focus",
                "impact",
                "thesis",
                "approach",
                "what-we-do",
                "work",
                "sectors",
                "industries"
        );

        List<String> badExtensions = Arrays.asList(
                ".jpg", ".jpeg", ".png", ".gif", ".svg", ".webp",
                ".pdf", ".zip", ".mp4", ".mp3", ".css", ".js",
                ".woff", ".woff2", ".ttf", ".otf", ".ico"
        );

        LinkedHashMap<String, Integer> scoredLinks = new LinkedHashMap<>();

        for (String link : links) {
            URI uri;

            try {
                uri = URI.create(link);
            } catch (Exception e) {
                continue;
            }

            String host = uri.getHost();
            if (host == null) {
                continue;
            }

            if (!stripWww(host).equals(rootHost)) {
                continue;
            }

            String lower = link.toLowerCase();

            boolean blocked0 = false;

            for (String blockedPattern0 : blockedPatterns0)
            {
                if (lower.contains(blockedPattern0))
                {
                    blocked0 = true;
                    break;
                }
            }

            if (blocked0)
            {
                continue;
            }

            boolean badExtension = false;
            for (String ext : badExtensions) {
                if (lower.contains(ext)) {
                    badExtension = true;
                    break;
                }
            }

            if (badExtension) {
                continue;
            }

            if (lower.contains("/tag/")
                    || lower.contains("/category/")
                    || lower.contains("/author/")
                    || lower.contains("/feed/")
                    || lower.contains("?")
                    || lower.contains("privacy")
                    || lower.contains("terms")
                    || lower.contains("login")
                    || lower.contains("signup")) {
                continue;
            }

            int score = 0;

            for (String keyword : usefulKeywords) {
                if (lower.contains(keyword)) {
                    score += 10;
                }
            }

            int slashCount = countChar(uri.getPath(), '/');
            score -= slashCount;

            if (score > 0) {
                scoredLinks.put(link, score);
            }
        }

        List<Map.Entry<String, Integer>> entries = new ArrayList<>(scoredLinks.entrySet());

        entries.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        List<String> result = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : entries) {
            result.add(entry.getKey());
        }

        return result;
    }

    public static String normalizeRootUrl(String input) {
        String url = input.trim();

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }

        return normalizeUrl(url);
    }

    public static String normalizeUrl(String input) {
        try {
            URI uri = URI.create(input.trim());

            String scheme = uri.getScheme() == null ? "https" : uri.getScheme().toLowerCase();
            String host = uri.getHost();

            if (host == null) {
                return "";
            }

            host = host.toLowerCase();

            String path = uri.getPath();

            if (path == null || path.isBlank()) {
                path = "/";
            }

            path = URLDecoder.decode(path, StandardCharsets.UTF_8);

            while (path.contains("//")) {
                path = path.replace("//", "/");
            }

            if (path.length() > 1 && path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }

            return scheme + "://" + host + path;

        } catch (Exception e) {
            return "";
        }
    }

    public static String buildOutputJson(String rootUrl, LinkedHashMap<String, String> scrapedPages) {
        StringBuilder sb = new StringBuilder();

        sb.append("{\n");
        sb.append("  \"root_url\": \"").append(escapeJson(rootUrl)).append("\",\n");
        sb.append("  \"scraped_at\": \"").append(Instant.now()).append("\",\n");
        sb.append("  \"page_count\": ").append(scrapedPages.size()).append(",\n");
        sb.append("  \"pages\": [\n");

        int index = 0;

        for (Map.Entry<String, String> entry : scrapedPages.entrySet()) {
            String url = entry.getKey();
            String html = entry.getValue();

            sb.append("    {\n");
            sb.append("      \"url\": \"").append(escapeJson(url)).append("\",\n");
            sb.append("      \"html_char_count\": ").append(html.length()).append(",\n");
            sb.append("      \"html\": \"").append(escapeJson(html)).append("\"\n");
            sb.append("    }");

            index++;

            if (index < scrapedPages.size()) {
                sb.append(",");
            }

            sb.append("\n");
        }

        sb.append("  ]\n");
        sb.append("}");

        return sb.toString();
    }

    public static String stripWww(String host) {
        if (host.startsWith("www.")) {
            return host.substring(4);
        }
        return host;
    }

    public static int countChar(String text, char target) {
        if (text == null) {
            return 0;
        }

        int count = 0;

        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == target) {
                count++;
            }
        }

        return count;
    }

    public static String escapeJson(String input) {
        if (input == null) {
            return "";
        }

        return input
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public static String extractVisibleText(String html0)
    {
        if (html0 == null)
        {
            return "";
        }

        String text0 = html0;

        text0 = text0.replaceAll("(?is)<script.*?>.*?</script>", " ");
        text0 = text0.replaceAll("(?is)<style.*?>.*?</style>", " ");
        text0 = text0.replaceAll("(?is)<noscript.*?>.*?</noscript>", " ");
        text0 = text0.replaceAll("(?is)<!--.*?-->", " ");

        text0 = text0.replaceAll("(?is)<br\\s*/?>", "\n");
        text0 = text0.replaceAll("(?is)</p>", "\n");
        text0 = text0.replaceAll("(?is)</div>", "\n");
        text0 = text0.replaceAll("(?is)</h[1-6]>", "\n");

        text0 = text0.replaceAll("(?is)<[^>]+>", " ");

        text0 = text0.replace("&amp;", "&");
        text0 = text0.replace("&nbsp;", " ");
        text0 = text0.replace("&quot;", "\"");
        text0 = text0.replace("&#039;", "'");
        text0 = text0.replace("&apos;", "'");
        text0 = text0.replace("&lt;", "<");
        text0 = text0.replace("&gt;", ">");

        text0 = text0.replaceAll("[ \\t]+", " ");
        text0 = text0.replaceAll("\\n\\s*\\n\\s*\\n+", "\n\n");

        return text0.trim();
    }
}