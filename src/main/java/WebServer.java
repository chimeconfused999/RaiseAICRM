import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.staticfiles.Location;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lightweight web server for RaiseAI.
 *
 * Serves the static frontend (login page + app) and exposes a small JSON API
 * that wraps the existing CLI backend (onboarding, login, Gmail sync, intake
 * processing, CRM updates). Run with:  mvn compile exec:java
 */
public class WebServer
{
    private static final int DEFAULT_PORT = 7070;
    private static final int GMAIL_MAX_MESSAGES = 25;

    private static final Pattern SPREADSHEET_ID_PATTERN =
        Pattern.compile("/spreadsheets/d/([a-zA-Z0-9-_]+)");

    public static void main(String[] args)
    {
        int port0 = resolvePort();

        Javalin app0 = Javalin.create(config0 ->
        {
            config0.staticFiles.add("/public", Location.CLASSPATH);
        });

        app0.get("/api/health", WebServer::handleHealth);
        app0.post("/api/auth/google", WebServer::handleGoogleSignIn);
        app0.get("/api/auth/diagnostics", WebServer::handleDiagnostics);
        app0.post("/api/login", WebServer::handleLogin);
        app0.post("/api/onboard/parse-link", WebServer::handleParseLink);
        app0.post("/api/onboard/preview", WebServer::handlePreview);
        app0.post("/api/onboard/register", WebServer::handleRegister);
        app0.post("/api/pipeline/sync-gmail", WebServer::handleSyncGmail);
        app0.post("/api/pipeline/process-intake", WebServer::handleProcessIntake);
        app0.post("/api/pipeline/update-crm", WebServer::handleUpdateCrm);
        app0.post("/api/pipeline/full", WebServer::handleFullPipeline);
        app0.post("/api/pipeline/follow-ups", WebServer::handleFollowUps);
        app0.post("/api/pipeline/enrich-lps", WebServer::handleEnrichLps);
        app0.post("/api/pipeline/score-candidates", WebServer::handleScoreCandidates);
        app0.post("/api/pipeline/prioritize", WebServer::handlePrioritize);
        app0.post("/api/pipeline/discover-candidates", WebServer::handleDiscoverCandidates);
        app0.post("/api/agent", WebServer::handleAgent);

        app0.start(port0);

        System.out.println("RaiseAI web server running at http://localhost:" + port0);
    }

    private static int resolvePort()
    {
        String env0 = System.getenv("PORT");

        if (env0 != null && !env0.isBlank())
        {
            try
            {
                return Integer.parseInt(env0.trim());
            }
            catch (NumberFormatException ignored0)
            {
            }
        }

        return DEFAULT_PORT;
    }

    // ============================================================
    // HANDLERS
    // ============================================================

    private static void handleHealth(Context ctx0)
    {
        respond(ctx0, 200, ok("Server online", null));
    }

    private static void handleGoogleSignIn(Context ctx0)
    {
        try
        {
            SheetsApp.ensureValidGoogleToken();
            String email0 = GmailService.getSignedInEmail();

            JSONObject data0 = new JSONObject();
            data0.put("email", email0);

            respond(ctx0, 200, ok("Signed in as " + email0, data0));
        }
        catch (Exception e0)
        {
            respond(ctx0, 500, fail(
                "Google sign-in failed: " + e0.getMessage()
                + " (check that credentials.json exists and the consent screen completed)."));
        }
    }

    private static void handleDiagnostics(Context ctx0)
    {
        JSONObject data0 = new JSONObject();

        String spreadsheetId0 = CRMRegistry.CRM_USER_DATABASE_SPREADSHEET_ID;
        data0.put("appDatabaseSpreadsheetId", spreadsheetId0);
        data0.put("appDatabaseUrl", sheetUrl(spreadsheetId0));

        try
        {
            data0.put("oauthEmail", GmailService.getSignedInEmail());
        }
        catch (Exception e0)
        {
            data0.put("oauthEmail", "(not signed in)");
        }

        JSONObject probe0 = new JSONObject();
        try
        {
            SheetsApp.readRangeMatrixA1(spreadsheetId0, CRMRegistry.USERS_TAB, "A1:A1");
            probe0.put("ok", true);
        }
        catch (Exception e0)
        {
            probe0.put("ok", false);
            probe0.put("error", e0.getMessage());
        }
        data0.put("appDatabaseUsersTabProbe", probe0);

        respond(ctx0, 200, ok("Diagnostics", data0));
    }

    private static void handleLogin(Context ctx0)
    {
        try
        {
            JSONObject body0 = body(ctx0);
            String email0 = body0.optString("email", "").trim();

            if (email0.isEmpty())
            {
                respond(ctx0, 400, fail("Enter your email."));
                return;
            }

            SessionContext context0 = CRMRegistry.login(email0);

            if (context0 == null)
            {
                respond(ctx0, 404, fail(
                    "No account found for " + email0 + ". Create an account first."));
                return;
            }

            respond(ctx0, 200, ok("Logged in as " + email0, sessionData(context0)));
        }
        catch (Exception e0)
        {
            respond(ctx0, 500, fail("Login failed: " + e0.getMessage()));
        }
    }

    private static void handleParseLink(Context ctx0)
    {
        JSONObject body0 = body(ctx0);
        String url0 = body0.optString("spreadsheetUrl", "").trim();
        String id0 = parseSpreadsheetId(url0);

        if (id0.isEmpty())
        {
            respond(ctx0, 400, fail("Could not find a spreadsheet ID in that link."));
            return;
        }

        JSONObject data0 = new JSONObject();
        data0.put("spreadsheetId", id0);
        respond(ctx0, 200, ok("Parsed spreadsheet ID.", data0));
    }

    private static void handlePreview(Context ctx0)
    {
        respond(ctx0, 200, fail(
            "AI mapping preview runs during account creation. "
            + "Fill in your details and click Create account."));
    }

    private static void handleRegister(Context ctx0)
    {
        try
        {
            JSONObject body0 = body(ctx0);

            String email0 = body0.optString("email", "").trim();
            String fundName0 = body0.optString("fundName", "").trim();
            String spreadsheetUrl0 = body0.optString("spreadsheetUrl", "").trim();
            String spreadsheetId0 = parseSpreadsheetId(spreadsheetUrl0);

            if (email0.isEmpty() || fundName0.isEmpty() || spreadsheetId0.isEmpty())
            {
                respond(ctx0, 400, fail(
                    "Email, fund name, and a valid Google Sheets link are required."));
                return;
            }

            if (CRMRegistry.loadUserByEmail(email0) != null)
            {
                respond(ctx0, 409, fail(
                    "An account already exists for " + email0 + ". Try logging in."));
                return;
            }

            ArrayList<String> internalNames0 =
                parsePipeList(body0.optString("internalNames", ""));
            ArrayList<String> internalEmails0 =
                parsePipeList(body0.optString("internalEmails", ""));

            String[] tabNames0 = SheetsApp.listTabNames(spreadsheetId0);

            if (tabNames0.length == 0)
            {
                respond(ctx0, 400, fail(
                    "Could not read any tabs from that spreadsheet. "
                    + "Check the link and that it's shared with your Google account."));
                return;
            }

            String userId0 = CRMRegistry.generateUserId();

            SessionContext context0 = CRMOnboard.onboardUser(
                userId0,
                email0,
                fundName0,
                spreadsheetId0,
                internalNames0,
                internalEmails0,
                fundName0,
                "",
                "",
                "",
                "",
                "",
                "{}",
                tabNames0
            );

            respond(ctx0, 200, ok(
                "Account created for " + email0 + ". AI mapped your sheet columns.",
                sessionData(context0)));
        }
        catch (Exception e0)
        {
            respond(ctx0, 500, fail("Account creation failed: " + e0.getMessage()));
        }
    }

    private static void handleSyncGmail(Context ctx0)
    {
        try
        {
            SessionContext context0 = requireSession(ctx0);
            if (context0 == null) return;

            JSONObject body0 = body(ctx0);
            String keyword0 = body0.optString("gmailKeyword", "").trim();

            GmailService.SyncResult syncResult0 =
                GmailService.syncRecentToIntake(context0, keyword0, GMAIL_MAX_MESSAGES);

            JSONObject data0 = new JSONObject();
            data0.put("gmailQuery", syncResult0.query);
            data0.put("intakeTab", context0.config.intakeTabName);
            data0.put("spreadsheetUrl", sheetUrl(context0.config.spreadsheetId));
            data0.put("added", syncResult0.added);
            data0.put("skipped", syncResult0.skipped);

            String message0 = "Synced " + syncResult0.added + " new email(s) into "
                + context0.config.intakeTabName
                + " (" + syncResult0.skipped + " already present).";

            respond(ctx0, 200, ok(message0, data0));
        }
        catch (Exception e0)
        {
            respond(ctx0, 500, fail("Gmail sync failed: " + e0.getMessage()));
        }
    }

    private static void handleProcessIntake(Context ctx0)
    {
        try
        {
            SessionContext context0 = requireSession(ctx0);
            if (context0 == null) return;

            String result0 = EmailIntakeProcessor.processUnprocessedIntakeRows(context0);
            respond(ctx0, 200, ok(result0, null));
        }
        catch (Exception e0)
        {
            respond(ctx0, 500, fail("Process intake failed: " + e0.getMessage()));
        }
    }

    private static void handleUpdateCrm(Context ctx0)
    {
        try
        {
            SessionContext context0 = requireSession(ctx0);
            if (context0 == null) return;

            String result0 = CrmUpdater.updateCrmFromProcessedIntakeRows(context0);
            respond(ctx0, 200, ok(result0, null));
        }
        catch (Exception e0)
        {
            respond(ctx0, 500, fail("Update CRM failed: " + e0.getMessage()));
        }
    }

    private static void handleFullPipeline(Context ctx0)
    {
        try
        {
            SessionContext context0 = requireSession(ctx0);
            if (context0 == null) return;

            JSONObject body0 = body(ctx0);
            String keyword0 = body0.optString("gmailKeyword", "").trim();

            JSONArray steps0 = new JSONArray();

            GmailService.SyncResult syncResult0 =
                GmailService.syncRecentToIntake(context0, keyword0, GMAIL_MAX_MESSAGES);
            steps0.put(step("Sync Gmail",
                "Synced " + syncResult0.added + " new email(s).", true));

            String intakeResult0 = EmailIntakeProcessor.processUnprocessedIntakeRows(context0);
            steps0.put(step("Process intake", intakeResult0, true));

            String crmResult0 = CrmUpdater.updateCrmFromProcessedIntakeRows(context0);
            steps0.put(step("Update CRM", crmResult0, true));

            JSONObject data0 = new JSONObject();
            data0.put("steps", steps0);
            data0.put("gmailQuery", syncResult0.query);
            data0.put("spreadsheetUrl", sheetUrl(context0.config.spreadsheetId));
            data0.put("intakeTab", context0.config.intakeTabName);

            respond(ctx0, 200, ok("Full pipeline complete.", data0));
        }
        catch (Exception e0)
        {
            respond(ctx0, 500, fail("Pipeline failed: " + e0.getMessage()));
        }
    }

    private static void handleFollowUps(Context ctx0)
    {
        try
        {
            SessionContext context0 = requireSession(ctx0);
            if (context0 == null) return;

            ArrayList<String[]> recommendations0 =
                FollowUpRecommender.generateRecommendations(context0);

            String writeResult0 =
                FollowUpRecommender.writeRecommendationsToCrm(context0, recommendations0);

            JSONArray items0 = new JSONArray();
            for (String[] row0 : recommendations0)
            {
                JSONObject item0 = new JSONObject();
                item0.put("email", row0.length > 0 ? row0[0] : "");
                item0.put("recommendation", row0.length > 1 ? row0[1] : "");
                items0.put(item0);
            }

            JSONObject data0 = new JSONObject();
            data0.put("recommendations", items0);
            data0.put("count", recommendations0.size());

            respond(ctx0, 200, ok(
                recommendations0.size() + " follow-up recommendation(s) written. "
                + writeResult0, data0));
        }
        catch (Exception e0)
        {
            respond(ctx0, 500, fail("Follow-ups failed: " + e0.getMessage()));
        }
    }

    private static void handleEnrichLps(Context ctx0)
    {
        try
        {
            SessionContext context0 = requireSession(ctx0);
            if (context0 == null) return;

            String result0 = LPEnrichmentProcessor.enrichLpRows(context0);
            respond(ctx0, 200, ok(result0, null));
        }
        catch (Exception e0)
        {
            respond(ctx0, 500, fail("LP enrichment failed: " + e0.getMessage()));
        }
    }

    private static void handleScoreCandidates(Context ctx0)
    {
        try
        {
            SessionContext context0 = requireSession(ctx0);
            if (context0 == null) return;

            int maxRows0 = body(ctx0).optInt("maxRows", 10);

            String result0 =
                CandidateScoringProcessor.scoreNextUnscoredCandidates(context0, maxRows0);
            respond(ctx0, 200, ok(result0, null));
        }
        catch (Exception e0)
        {
            respond(ctx0, 500, fail("Scoring failed: " + e0.getMessage()));
        }
    }

    private static void handlePrioritize(Context ctx0)
    {
        try
        {
            SessionContext context0 = requireSession(ctx0);
            if (context0 == null) return;

            int maxRows0 = body(ctx0).optInt("maxRows", 25);

            String result0 = PriorityActionProcessor.updatePriorityScores(context0, maxRows0);
            respond(ctx0, 200, ok(result0, null));
        }
        catch (Exception e0)
        {
            respond(ctx0, 500, fail("Prioritization failed: " + e0.getMessage()));
        }
    }

    private static void handleDiscoverCandidates(Context ctx0)
    {
        try
        {
            SessionContext context0 = requireSession(ctx0);
            if (context0 == null) return;

            JSONObject body0 = body(ctx0);

            String sectors0 = firstNonBlank(
                body0.optString("sectors", ""), context0.user.clientSectorTags);
            String microsectors0 = firstNonBlank(
                body0.optString("microsectors", ""), context0.user.clientMicrosectorTags);
            String geographies0 = firstNonBlank(
                body0.optString("geographies", ""), context0.user.clientGeography);
            String thesis0 = firstNonBlank(
                body0.optString("thesis", ""), context0.user.clientInvestmentThesis);

            int maxResultsPerQuery0 = body0.optInt("maxResultsPerQuery", 5);
            int maxCandidates0 = body0.optInt("maxCandidates", 20);
            boolean scrapeLinkedIn0 = body0.optBoolean("scrapeLinkedIn", true);
            boolean scrapeWebsites0 = body0.optBoolean("scrapeWebsites", false);
            boolean extractProfiles0 = body0.optBoolean("extractProfiles", true);

            ArrayList<InvestorProfile> seedProfiles0 =
                CandidateDiscoveryProcessor.buildSeedProfilesFromClientInput(
                    sectors0, microsectors0, geographies0, thesis0);

            String result0 = new CandidateDiscoveryProcessor()
                .discoverAndAppendColdCandidates(
                    context0,
                    seedProfiles0,
                    maxResultsPerQuery0,
                    maxCandidates0,
                    scrapeLinkedIn0,
                    scrapeWebsites0,
                    extractProfiles0);

            respond(ctx0, 200, ok(result0, null));
        }
        catch (Exception e0)
        {
            respond(ctx0, 500, fail("Candidate discovery failed: " + e0.getMessage()));
        }
    }

    private static void handleAgent(Context ctx0)
    {
        try
        {
            SessionContext context0 = requireSession(ctx0);
            if (context0 == null) return;

            String prompt0 = body(ctx0).optString("prompt", "").trim();

            if (prompt0.isEmpty())
            {
                respond(ctx0, 400, fail("Type a request for the sheet agent."));
                return;
            }

            String result0 = WebSheetAgent.run(context0, prompt0);
            respond(ctx0, 200, ok(result0, null));
        }
        catch (Exception e0)
        {
            respond(ctx0, 500, fail("Sheet agent failed: " + e0.getMessage()));
        }
    }

    // ============================================================
    // HELPERS
    // ============================================================

    private static SessionContext requireSession(Context ctx0) throws Exception
    {
        JSONObject body0 = body(ctx0);
        String email0 = body0.optString("email", "").trim();

        if (email0.isEmpty())
        {
            respond(ctx0, 401, fail("Log in or create an account first."));
            return null;
        }

        SessionContext context0 = CRMRegistry.login(email0);

        if (context0 == null)
        {
            respond(ctx0, 401, fail("Session expired. Log in again."));
            return null;
        }

        return context0;
    }

    private static JSONObject sessionData(SessionContext context0)
    {
        JSONObject data0 = new JSONObject();
        data0.put("email", context0.user.email);
        data0.put("fundName", context0.user.fundName);
        data0.put("mainTab", context0.config.mainTabName);
        data0.put("intakeTab", context0.config.intakeTabName);
        data0.put("spreadsheetUrl", sheetUrl(context0.config.spreadsheetId));
        return data0;
    }

    private static String sheetUrl(String spreadsheetId0)
    {
        if (spreadsheetId0 == null || spreadsheetId0.isBlank())
        {
            return "";
        }

        return "https://docs.google.com/spreadsheets/d/" + spreadsheetId0;
    }

    private static String parseSpreadsheetId(String url0)
    {
        if (url0 == null || url0.isBlank())
        {
            return "";
        }

        Matcher matcher0 = SPREADSHEET_ID_PATTERN.matcher(url0);

        if (matcher0.find())
        {
            return matcher0.group(1);
        }

        if (url0.matches("[a-zA-Z0-9-_]{20,}"))
        {
            return url0.trim();
        }

        return "";
    }

    private static String firstNonBlank(String primary0, String fallback0)
    {
        if (primary0 != null && !primary0.isBlank())
        {
            return primary0.trim();
        }

        return fallback0 == null ? "" : fallback0;
    }

    private static ArrayList<String> parsePipeList(String value0)
    {
        ArrayList<String> list0 = new ArrayList<>();

        if (value0 == null || value0.isBlank())
        {
            return list0;
        }

        for (String part0 : value0.split("\\|"))
        {
            String trimmed0 = part0.trim();
            if (!trimmed0.isEmpty())
            {
                list0.add(trimmed0);
            }
        }

        return list0;
    }

    private static JSONObject body(Context ctx0)
    {
        String raw0 = ctx0.body();

        if (raw0 == null || raw0.isBlank())
        {
            return new JSONObject();
        }

        try
        {
            return new JSONObject(raw0);
        }
        catch (Exception e0)
        {
            return new JSONObject();
        }
    }

    private static JSONObject ok(String message0, JSONObject data0)
    {
        JSONObject json0 = new JSONObject();
        json0.put("ok", true);
        json0.put("message", message0);
        json0.put("data", data0 == null ? JSONObject.NULL : data0);
        return json0;
    }

    private static JSONObject fail(String message0)
    {
        JSONObject json0 = new JSONObject();
        json0.put("ok", false);
        json0.put("message", message0);
        json0.put("data", JSONObject.NULL);
        return json0;
    }

    private static JSONObject step(String name0, String message0, boolean ok0)
    {
        JSONObject json0 = new JSONObject();
        json0.put("name", name0);
        json0.put("message", message0);
        json0.put("ok", ok0);
        return json0;
    }

    private static void respond(Context ctx0, int status0, JSONObject json0)
    {
        ctx0.status(status0);
        ctx0.contentType("application/json");
        ctx0.result(json0.toString());
    }
}
