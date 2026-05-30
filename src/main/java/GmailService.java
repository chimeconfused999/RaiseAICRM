import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;

import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Pulls recent Gmail messages into a user's intake tab.
 *
 * Uses the same Google OAuth credential as SheetsApp (Spreadsheets + Gmail
 * read-only scopes), so signing in once authorizes both.
 */
public class GmailService
{
    private static final String APPLICATION_NAME = "RaiseAI";

    private static Gmail service() throws Exception
    {
        return new Gmail.Builder(
            SheetsApp.getHttpTransport(),
            SheetsApp.getJsonFactory(),
            SheetsApp.getAuthorizedCredential()
        )
        .setApplicationName(APPLICATION_NAME)
        .build();
    }

    /** Email address of the currently authorized Google account. */
    public static String getSignedInEmail() throws Exception
    {
        return service().users().getProfile("me").execute().getEmailAddress();
    }

    public static class SyncResult
    {
        public int added = 0;
        public int skipped = 0;
        public int scanned = 0;
        public String query = "";
    }

    /**
     * Fetches up to maxMessages recent messages (optionally filtered by sender
     * keyword) and appends new ones to the configured intake tab. Messages whose
     * Gmail message id is already present are skipped.
     */
    public static SyncResult syncRecentToIntake(
        SessionContext context0,
        String senderKeyword0,
        int maxMessages0) throws Exception
    {
        SyncResult result0 = new SyncResult();

        if (context0 == null || context0.config == null)
        {
            throw new Exception("Missing session context.");
        }

        String spreadsheetId0 = context0.config.spreadsheetId;
        String intakeTab0 = context0.config.intakeTabName;
        int headerRow0 = context0.config.intakeTabHeaderRow;

        if (spreadsheetId0 == null || spreadsheetId0.isBlank()
            || intakeTab0 == null || intakeTab0.isBlank())
        {
            throw new Exception("Your account has no intake spreadsheet/tab configured.");
        }

        HashMap<String, Integer> headerMap0 =
            SheetsApp.buildHeaderMap(spreadsheetId0, intakeTab0, headerRow0, 100);

        if (headerMap0.isEmpty())
        {
            throw new Exception("Could not read intake tab headers (row " + headerRow0 + ").");
        }

        int maxCol0 = 0;
        for (Integer col0 : headerMap0.values())
        {
            if (col0 != null && col0 > maxCol0) maxCol0 = col0;
        }

        HashSet<String> existingIds0 = readExistingMessageIds(
            spreadsheetId0,
            intakeTab0,
            headerMap0,
            context0.config.intakeTabGmailMessageIdCol
        );

        String query0 = buildQuery(senderKeyword0);
        result0.query = query0;

        Gmail gmail0 = service();

        ListMessagesResponse listResponse0 = gmail0.users().messages()
            .list("me")
            .setQ(query0)
            .setMaxResults((long) Math.max(1, maxMessages0))
            .execute();

        List<Message> messageRefs0 = listResponse0.getMessages();

        if (messageRefs0 == null || messageRefs0.isEmpty())
        {
            return result0;
        }

        for (Message ref0 : messageRefs0)
        {
            result0.scanned++;

            String messageId0 = ref0.getId();

            if (messageId0 == null || existingIds0.contains(messageId0))
            {
                result0.skipped++;
                continue;
            }

            Message full0 = gmail0.users().messages()
                .get("me", messageId0)
                .setFormat("full")
                .execute();

            String[] row0 = new String[maxCol0];
            for (int i0 = 0; i0 < row0.length; i0++) row0[i0] = "";

            MessagePart payload0 = full0.getPayload();

            String to0 = headerValue(payload0, "To");
            String from0 = headerValue(payload0, "From");
            String subject0 = headerValue(payload0, "Subject");
            String body0 = extractBody(payload0);

            if (body0.isBlank() && full0.getSnippet() != null)
            {
                body0 = full0.getSnippet();
            }

            put(row0, headerMap0, context0.config.intakeTabIntakeIdCol,
                "gmail_" + messageId0);
            put(row0, headerMap0, context0.config.intakeTabGmailMessageIdCol, messageId0);
            put(row0, headerMap0, context0.config.intakeTabGmailThreadIdCol,
                full0.getThreadId());
            put(row0, headerMap0, context0.config.intakeTabIntakeTypeCol, "EMAIL");
            put(row0, headerMap0, context0.config.intakeTabTimestampCol,
                formatTimestamp(full0.getInternalDate()));
            put(row0, headerMap0, context0.config.intakeTabToCol, to0);
            put(row0, headerMap0, context0.config.intakeTabFromCol, from0);
            put(row0, headerMap0, context0.config.intakeTabSubjectCol, subject0);
            put(row0, headerMap0, context0.config.intakeTabBodyCol, body0);
            put(row0, headerMap0, context0.config.intakeTabProcessingStatusCol, "");

            SheetsApp.appendRow(spreadsheetId0, intakeTab0, row0);

            existingIds0.add(messageId0);
            result0.added++;
        }

        return result0;
    }

    private static String buildQuery(String senderKeyword0)
    {
        if (senderKeyword0 != null && !senderKeyword0.isBlank())
        {
            return "from:(" + senderKeyword0.trim() + ")";
        }

        return "in:inbox";
    }

    private static HashSet<String> readExistingMessageIds(
        String spreadsheetId0,
        String intakeTab0,
        HashMap<String, Integer> headerMap0,
        String messageIdHeader0) throws Exception
    {
        HashSet<String> ids0 = new HashSet<>();

        if (messageIdHeader0 == null || messageIdHeader0.isBlank()
            || !headerMap0.containsKey(messageIdHeader0))
        {
            return ids0;
        }

        int col0 = headerMap0.get(messageIdHeader0);

        String[][] data0 = SheetsApp.readRangeMatrix(
            spreadsheetId0,
            intakeTab0,
            1,
            col0,
            1000,
            col0
        );

        for (String[] cells0 : data0)
        {
            if (cells0.length > 0 && cells0[0] != null && !cells0[0].isBlank())
            {
                ids0.add(cells0[0].trim());
            }
        }

        return ids0;
    }

    private static void put(
        String[] row0,
        HashMap<String, Integer> headerMap0,
        String header0,
        String value0)
    {
        if (header0 == null || header0.isBlank() || !headerMap0.containsKey(header0))
        {
            return;
        }

        int index0 = headerMap0.get(header0) - 1;

        if (index0 >= 0 && index0 < row0.length)
        {
            row0[index0] = value0 == null ? "" : value0;
        }
    }

    private static String headerValue(MessagePart payload0, String name0)
    {
        if (payload0 == null || payload0.getHeaders() == null)
        {
            return "";
        }

        for (MessagePartHeader header0 : payload0.getHeaders())
        {
            if (header0.getName() != null && header0.getName().equalsIgnoreCase(name0))
            {
                return header0.getValue() == null ? "" : header0.getValue();
            }
        }

        return "";
    }

    private static String extractBody(MessagePart part0)
    {
        if (part0 == null)
        {
            return "";
        }

        String mimeType0 = part0.getMimeType();

        if ("text/plain".equalsIgnoreCase(mimeType0)
            && part0.getBody() != null
            && part0.getBody().getData() != null)
        {
            return decode(part0.getBody().getData());
        }

        if (part0.getParts() != null)
        {
            String plain0 = "";
            String html0 = "";

            for (MessagePart child0 : part0.getParts())
            {
                String childMime0 = child0.getMimeType();

                if ("text/plain".equalsIgnoreCase(childMime0))
                {
                    String text0 = extractBody(child0);
                    if (!text0.isBlank()) plain0 = text0;
                }
                else if ("text/html".equalsIgnoreCase(childMime0)
                    && child0.getBody() != null
                    && child0.getBody().getData() != null)
                {
                    html0 = stripHtml(decode(child0.getBody().getData()));
                }
                else
                {
                    String nested0 = extractBody(child0);
                    if (!nested0.isBlank() && plain0.isBlank()) plain0 = nested0;
                }
            }

            if (!plain0.isBlank()) return plain0;
            if (!html0.isBlank()) return html0;
        }

        if (part0.getBody() != null && part0.getBody().getData() != null)
        {
            return decode(part0.getBody().getData());
        }

        return "";
    }

    private static String decode(String data0)
    {
        try
        {
            return new String(Base64.getUrlDecoder().decode(data0), "UTF-8");
        }
        catch (Exception e0)
        {
            return "";
        }
    }

    private static String stripHtml(String html0)
    {
        if (html0 == null) return "";
        return html0.replaceAll("(?s)<[^>]*>", " ")
            .replaceAll("&nbsp;", " ")
            .replaceAll("\\s+", " ")
            .trim();
    }

    private static String formatTimestamp(Long epochMillis0)
    {
        if (epochMillis0 == null)
        {
            return "";
        }

        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            .format(new java.util.Date(epochMillis0));
    }
}
