import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;

public class EmailIntakeProcessor
{
    private static final int MAX_ROWS0 = 1000;
    private static final int MAX_COLUMNS0 = 100;
    private static final int MAX_ROWS_PER_OPENAI_BATCH0 = 20;

    private static final String PROCESSED_STATUS0 = "PROCESSED";

    public static String processUnprocessedIntakeRows(SessionContext context0) throws Exception
    {
        if (context0 == null || context0.user == null || context0.config == null)
        {
            return "ERROR: Missing session context.";
        }

        String spreadsheetId0 = context0.config.spreadsheetId;
        String intakeTabName0 = context0.config.intakeTabName;

        String[] readHeaders0 = buildReadHeaders(context0);
        String[] updateHeaders0 = buildUpdateHeaders(context0);

        HashMap<String, Integer> sheetHeaderMap0 = SheetsApp.buildHeaderMap(
            spreadsheetId0,
            intakeTabName0,
            context0.config.intakeTabHeaderRow,
            MAX_COLUMNS0
        );

        HashMap<String, Integer> neededHeaderMap0 = buildNeededHeaderMap(
            sheetHeaderMap0,
            readHeaders0,
            updateHeaders0
        );

        int maxColumn0 = getMaxColumn(neededHeaderMap0);

        String[][] sheetData0 = SheetsApp.readRangeMatrix(
            spreadsheetId0,
            intakeTabName0,
            1,
            1,
            MAX_ROWS0,
            maxColumn0
        );

        int lastRow0 = SheetsApp.findLastRow(sheetData0);

        if (lastRow0 < context0.config.intakeTabDataStartRow)
        {
            return "No intake data found.";
        }

        int processingStatusCol0 = getSheetColumn(
            neededHeaderMap0,
            context0.config.intakeTabProcessingStatusCol
        );

        int firstRow0 = findFirstUnprocessedRow(
            sheetData0,
            context0.config.intakeTabDataStartRow,
            lastRow0,
            processingStatusCol0
        );

        System.out.println("lastRow0 = " + lastRow0);
        System.out.println("firstRow0 = " + firstRow0);

        if (firstRow0 == -1)
        {
            return "No unprocessed intake rows found.";
        }

        JSONArray rowsToProcess0 = buildRowsToProcess(
            sheetData0,
            context0,
            neededHeaderMap0,
            firstRow0,
            lastRow0
        );

        System.out.println("Found " + rowsToProcess0.length() + " rows to process.");

        if (rowsToProcess0.length() == 0)
        {
            return "No unprocessed intake rows found.";
        }

        System.out.println("Sending data to OpenAI in batches (" + rowsToProcess0.length() + " rows)...");

        JSONArray extractedRows0 = processRowsInOpenAIBatches(
            rowsToProcess0,
            context0
        );

        if (extractedRows0.length() != rowsToProcess0.length())
        {
            return "ERROR: OpenAI returned the wrong number of rows. No sheet updates were made.";
        }

        String[][] processedInfo0 = buildInitialProcessedInfo(
            sheetData0,
            neededHeaderMap0,
            updateHeaders0,
            firstRow0,
            lastRow0
        );

        int successCount0 = 0;
        int errorCount0 = 0;

        for (int resultIndex0 = 0; resultIndex0 < extractedRows0.length(); resultIndex0++)
        {
            try
            {
                JSONObject resultObject0 = extractedRows0.getJSONObject(resultIndex0);

                int rowNumber0 = resultObject0.getInt("rowNumber");
                int processedInfoRowIndex0 = rowNumber0 - firstRow0;

                String cleanedEmail0 = cleanEmail(resultObject0.optString("cleanedEmail", ""));
                String firstName0 = resultObject0.optString("firstName", "");
                String lastName0 = resultObject0.optString("lastName", "");
                String fundName0 = resultObject0.optString("fundName", "");
                String fundWebsite0 = cleanWebsite(resultObject0.optString("fundWebsite", ""));
                String conversationLabel0 = resultObject0.optString("conversationLabel", "Reached Out");
                String conversationSummary0 = resultObject0.optString("conversationSummary", "");

                if (isBlank(firstName0))
                {
                    firstName0 = "Unknown";
                }

                if (!isAllowedConversationLabel(conversationLabel0))
                {
                    conversationLabel0 = "Reached Out";
                }

                String needsReview0 = "FALSE";

                if (isBlank(cleanedEmail0) ||
                    !isValidEmail(cleanedEmail0) ||
                    isInternalEmail(cleanedEmail0, context0.user.internalEmails))
                {
                    needsReview0 = "TRUE";
                }

                if (isInternalName(firstName0, lastName0, context0.user.internalNames))
                {
                    needsReview0 = "TRUE";
                    cleanedEmail0 = "";
                    firstName0 = "Unknown";
                    lastName0 = "";
                }

                if (isInternalFund(fundName0, context0.user.internalFundName))
                {
                    fundName0 = "";
                }

                if (isInternalWebsite(fundWebsite0, context0.user.internalWebsite))
                {
                    fundWebsite0 = "";
                }

                setProcessedInfoValue(
                    processedInfo0,
                    updateHeaders0,
                    processedInfoRowIndex0,
                    context0.config.intakeTabProcessingStatusCol,
                    PROCESSED_STATUS0
                );

                setProcessedInfoValue(
                    processedInfo0,
                    updateHeaders0,
                    processedInfoRowIndex0,
                    context0.config.intakeTabCleanedEmailCol,
                    cleanedEmail0
                );

                setProcessedInfoValue(
                    processedInfo0,
                    updateHeaders0,
                    processedInfoRowIndex0,
                    context0.config.intakeTabExtractedFirstNameCol,
                    firstName0
                );

                setProcessedInfoValue(
                    processedInfo0,
                    updateHeaders0,
                    processedInfoRowIndex0,
                    context0.config.intakeTabExtractedLastNameCol,
                    lastName0
                );

                setProcessedInfoValue(
                    processedInfo0,
                    updateHeaders0,
                    processedInfoRowIndex0,
                    context0.config.intakeTabExtractedFundNameCol,
                    fundName0
                );

                setProcessedInfoValue(
                    processedInfo0,
                    updateHeaders0,
                    processedInfoRowIndex0,
                    context0.config.intakeTabExtractedFundWebsiteCol,
                    fundWebsite0
                );

                setProcessedInfoValue(
                    processedInfo0,
                    updateHeaders0,
                    processedInfoRowIndex0,
                    context0.config.intakeTabConversationLabelCol,
                    conversationLabel0
                );

                setProcessedInfoValue(
                    processedInfo0,
                    updateHeaders0,
                    processedInfoRowIndex0,
                    context0.config.intakeTabConversationSummaryCol,
                    conversationSummary0
                );

                setProcessedInfoValue(
                    processedInfo0,
                    updateHeaders0,
                    processedInfoRowIndex0,
                    context0.config.intakeTabUpdatedCrmCol,
                    "FALSE"
                );

                setProcessedInfoValue(
                    processedInfo0,
                    updateHeaders0,
                    processedInfoRowIndex0,
                    context0.config.intakeTabNeedsReviewCol,
                    needsReview0
                );

                successCount0++;
            }
            catch (Exception exception0)
            {
                errorCount0++;
                System.out.println("ERROR processing extracted row index " + resultIndex0);
                System.out.println(exception0.getMessage());
            }
        }

        System.out.println("Processed info array:");
        printMatrix(processedInfo0);

        String[][][] columnsToProcess0 = buildColumnsToProcess(processedInfo0, updateHeaders0);

        System.out.println("Updating sheet by columns...");

        for (int headerIndex0 = 0; headerIndex0 < updateHeaders0.length; headerIndex0++)
        {
            String header0 = updateHeaders0[headerIndex0];

            int sheetColumn0 = getSheetColumn(
                neededHeaderMap0,
                header0
            );

            SheetsApp.updateRangeMatrix(
                spreadsheetId0,
                intakeTabName0,
                firstRow0,
                sheetColumn0,
                columnsToProcess0[headerIndex0]
            );
        }

        return "Processed intake rows with column updates. Success: "
            + successCount0
            + ", Errors: "
            + errorCount0
            + ".";
    }

    public static String processUnprocessedIntakeRows() throws Exception
    {
        SessionContext context0 = CRMRegistry.login("rohanblahblah@gmail.com");

        if (context0 == null)
        {
            return "ERROR: Could not create default session context.";
        }

        return processUnprocessedIntakeRows(context0);
    }

    private static String[] buildReadHeaders(SessionContext context0)
    {
        return new String[]
        {
            context0.config.intakeTabIntakeIdCol,
            context0.config.intakeTabToCol,
            context0.config.intakeTabFromCol,
            context0.config.intakeTabSubjectCol,
            context0.config.intakeTabBodyCol,
            context0.config.intakeTabProcessingStatusCol
        };
    }

    private static String[] buildUpdateHeaders(SessionContext context0)
    {
        return new String[]
        {
            context0.config.intakeTabProcessingStatusCol,
            context0.config.intakeTabCleanedEmailCol,
            context0.config.intakeTabExtractedFirstNameCol,
            context0.config.intakeTabExtractedLastNameCol,
            context0.config.intakeTabExtractedFundNameCol,
            context0.config.intakeTabExtractedFundWebsiteCol,
            context0.config.intakeTabConversationLabelCol,
            context0.config.intakeTabConversationSummaryCol,
            context0.config.intakeTabUpdatedCrmCol,
            context0.config.intakeTabNeedsReviewCol
        };
    }

    private static HashMap<String, Integer> buildNeededHeaderMap(
        HashMap<String, Integer> sheetHeaderMap0,
        String[] readHeaders0,
        String[] updateHeaders0) throws Exception
    {
        HashMap<String, Integer> neededHeaderMap0 = new HashMap<>();

        addHeadersToNeededHeaderMap(
            neededHeaderMap0,
            sheetHeaderMap0,
            readHeaders0
        );

        addHeadersToNeededHeaderMap(
            neededHeaderMap0,
            sheetHeaderMap0,
            updateHeaders0
        );

        return neededHeaderMap0;
    }

    private static void addHeadersToNeededHeaderMap(
        HashMap<String, Integer> neededHeaderMap0,
        HashMap<String, Integer> sheetHeaderMap0,
        String[] headers0) throws Exception
    {
        for (int i0 = 0; i0 < headers0.length; i0++)
        {
            String header0 = headers0[i0];

            if (isBlank(header0))
            {
                continue;
            }

            int column0 = SheetsApp.findColumnInHeaderMap(
                sheetHeaderMap0,
                header0
            );

            if (column0 == -1)
            {
                throw new Exception("Header not found: " + header0);
            }

            neededHeaderMap0.put(header0.trim(), column0);
        }
    }

    private static JSONArray buildRowsToProcess(
        String[][] sheetData0,
        SessionContext context0,
        HashMap<String, Integer> neededHeaderMap0,
        int firstRow0,
        int lastRow0) throws Exception
    {
        JSONArray rowsToProcess0 = new JSONArray();

        int intakeIdCol0 = getOptionalSheetColumn(
            neededHeaderMap0,
            context0.config.intakeTabIntakeIdCol
        );

        int toCol0 = getSheetColumn(
            neededHeaderMap0,
            context0.config.intakeTabToCol
        );

        int fromCol0 = getSheetColumn(
            neededHeaderMap0,
            context0.config.intakeTabFromCol
        );

        int subjectCol0 = getSheetColumn(
            neededHeaderMap0,
            context0.config.intakeTabSubjectCol
        );

        int bodyCol0 = getSheetColumn(
            neededHeaderMap0,
            context0.config.intakeTabBodyCol
        );

        int processingStatusCol0 = getSheetColumn(
            neededHeaderMap0,
            context0.config.intakeTabProcessingStatusCol
        );

        System.out.println("Scanning for unprocessed rows from row " + firstRow0 + " to row " + lastRow0 + "...");

        for (int rowNumber0 = firstRow0; rowNumber0 <= lastRow0; rowNumber0++)
        {
            String intakeId0 = "";

            if (intakeIdCol0 != -1)
            {
                intakeId0 = getCellValue(sheetData0, rowNumber0, intakeIdCol0);
            }

            String to0 = getCellValue(sheetData0, rowNumber0, toCol0);
            String from0 = getCellValue(sheetData0, rowNumber0, fromCol0);
            String subject0 = getCellValue(sheetData0, rowNumber0, subjectCol0);
            String body0 = getCellValue(sheetData0, rowNumber0, bodyCol0);
            String processingStatus0 = getCellValue(sheetData0, rowNumber0, processingStatusCol0);

            if (isBlank(intakeId0) &&
                isBlank(to0) &&
                isBlank(from0) &&
                isBlank(subject0) &&
                isBlank(body0))
            {
                continue;
            }

            if (processingStatus0.equalsIgnoreCase(PROCESSED_STATUS0))
            {
                continue;
            }

            String crmCandidateEmail0 = chooseCrmCandidateEmail(
                to0,
                from0,
                context0.user.internalEmails
            );

            JSONObject rowObject0 = new JSONObject();
            rowObject0.put("rowNumber", rowNumber0);
            rowObject0.put("crmCandidateEmail", crmCandidateEmail0);
            rowObject0.put("to", to0);
            rowObject0.put("from", from0);
            rowObject0.put("subject", subject0);
            rowObject0.put("body", body0);

            rowsToProcess0.put(rowObject0);
        }

        return rowsToProcess0;
    }

    private static JSONArray processRowsInOpenAIBatches(
        JSONArray rowsToProcess0,
        SessionContext context0) throws Exception
    {
        JSONArray allExtractedRows0 = new JSONArray();

        for (int startIndex0 = 0;
            startIndex0 < rowsToProcess0.length();
            startIndex0 += MAX_ROWS_PER_OPENAI_BATCH0)
        {
            int endIndexExclusive0 = Math.min(
                startIndex0 + MAX_ROWS_PER_OPENAI_BATCH0,
                rowsToProcess0.length()
            );

            JSONArray batchRows0 = new JSONArray();

            for (int i = startIndex0; i < endIndexExclusive0; i++)
            {
                batchRows0.put(rowsToProcess0.getJSONObject(i));
            }

            System.out.println(
                "Sending OpenAI batch rows "
                + (startIndex0 + 1)
                + "-"
                + endIndexExclusive0
                + " of "
                + rowsToProcess0.length()
                + "..."
            );

            String prompt0 = buildExtractionPrompt(
                batchRows0,
                context0.user.internalNames,
                context0.user.internalEmails,
                context0.user.internalFundName,
                context0.user.internalWebsite
            );

            String aiOutput0 = OpenAIClient.getTextResponse(prompt0);

            JSONArray extractedBatch0 = parseJsonArrayFromText(aiOutput0);

            if (extractedBatch0.length() != batchRows0.length())
            {
                throw new Exception(
                    "OpenAI returned "
                    + extractedBatch0.length()
                    + " rows, expected "
                    + batchRows0.length()
                    + " rows for batch starting at index "
                    + startIndex0
                );
            }

            for (int j = 0; j < extractedBatch0.length(); j++)
            {
                allExtractedRows0.put(extractedBatch0.getJSONObject(j));
            }
        }

        return allExtractedRows0;
    }

    private static String[][] buildInitialProcessedInfo(
        String[][] sheetData0,
        HashMap<String, Integer> neededHeaderMap0,
        String[] updateHeaders0,
        int firstRow0,
        int lastRow0) throws Exception
    {
        String[][] processedInfo0 = new String[lastRow0 - firstRow0 + 1][updateHeaders0.length];

        for (int rowNumber0 = firstRow0; rowNumber0 <= lastRow0; rowNumber0++)
        {
            int processedInfoRowIndex0 = rowNumber0 - firstRow0;

            for (int headerIndex0 = 0; headerIndex0 < updateHeaders0.length; headerIndex0++)
            {
                String header0 = updateHeaders0[headerIndex0];

                int sheetColumn0 = getSheetColumn(
                    neededHeaderMap0,
                    header0
                );

                processedInfo0[processedInfoRowIndex0][headerIndex0] = getCellValue(
                    sheetData0,
                    rowNumber0,
                    sheetColumn0
                );
            }
        }

        return processedInfo0;
    }

    private static String[][][] buildColumnsToProcess(
        String[][] processedInfo0,
        String[] updateHeaders0)
    {
        String[][][] columnsToProcess0 = new String[updateHeaders0.length][processedInfo0.length][1];

        for (int headerIndex0 = 0; headerIndex0 < updateHeaders0.length; headerIndex0++)
        {
            for (int rowIndex0 = 0; rowIndex0 < processedInfo0.length; rowIndex0++)
            {
                columnsToProcess0[headerIndex0][rowIndex0][0] = processedInfo0[rowIndex0][headerIndex0];
            }
        }

        return columnsToProcess0;
    }

    private static void setProcessedInfoValue(
        String[][] processedInfo0,
        String[] updateHeaders0,
        int rowIndex0,
        String header0,
        String value0) throws Exception
    {
        int headerIndex0 = getHeaderIndex(
            updateHeaders0,
            header0
        );

        if (headerIndex0 == -1)
        {
            throw new Exception("Header not found in update header list: " + header0);
        }

        processedInfo0[rowIndex0][headerIndex0] = value0 == null ? "" : value0;
    }

    private static int getHeaderIndex(
        String[] headers0,
        String header0)
    {
        if (headers0 == null || header0 == null)
        {
            return -1;
        }

        for (int i0 = 0; i0 < headers0.length; i0++)
        {
            if (headers0[i0] != null && headers0[i0].trim().equals(header0.trim()))
            {
                return i0;
            }
        }

        return -1;
    }

    private static int getSheetColumn(
        HashMap<String, Integer> headerMap0,
        String header0) throws Exception
    {
        if (headerMap0 == null || isBlank(header0))
        {
            throw new Exception("Cannot find sheet column for blank header.");
        }

        Integer column0 = headerMap0.get(header0.trim());

        if (column0 == null)
        {
            throw new Exception("Header missing from neededHeaderMap0: " + header0);
        }

        return column0;
    }

    private static int getOptionalSheetColumn(
        HashMap<String, Integer> headerMap0,
        String header0)
    {
        if (headerMap0 == null || isBlank(header0))
        {
            return -1;
        }

        Integer column0 = headerMap0.get(header0.trim());

        if (column0 == null)
        {
            return -1;
        }

        return column0;
    }

    private static int findFirstUnprocessedRow(
        String[][] sheetData0,
        int startRow0,
        int lastRow0,
        int processingStatusCol0)
    {
        for (int rowNumber0 = startRow0; rowNumber0 <= lastRow0; rowNumber0++)
        {
            String processingStatus0 = getCellValue(
                sheetData0,
                rowNumber0,
                processingStatusCol0
            );

            if (!processingStatus0.equalsIgnoreCase(PROCESSED_STATUS0))
            {
                return rowNumber0;
            }
        }

        return -1;
    }

    private static int getMaxColumn(HashMap<String, Integer> headerMap0)
    {
        int maxColumn0 = -1;

        for (Integer column0 : headerMap0.values())
        {
            if (column0 != null && column0 > maxColumn0)
            {
                maxColumn0 = column0;
            }
        }

        return maxColumn0;
    }

    private static void printMatrix(String[][] matrix0)
    {
        for (int rowIndex0 = 0; rowIndex0 < matrix0.length; rowIndex0++)
        {
            String line0 = "";

            for (int colIndex0 = 0; colIndex0 < matrix0[rowIndex0].length; colIndex0++)
            {
                if (colIndex0 > 0)
                {
                    line0 += " | ";
                }

                line0 += matrix0[rowIndex0][colIndex0];
            }

            System.out.println(line0);
        }
    }

    private static String getCellValue(
        String[][] sheetData0,
        int rowNumber0,
        int oneBasedColumn0)
    {
        int rowIndex0 = rowNumber0 - 1;
        int columnIndex0 = oneBasedColumn0 - 1;

        if (rowIndex0 < 0 || rowIndex0 >= sheetData0.length)
        {
            return "";
        }

        if (columnIndex0 < 0 || columnIndex0 >= sheetData0[rowIndex0].length)
        {
            return "";
        }

        if (sheetData0[rowIndex0][columnIndex0] == null)
        {
            return "";
        }

        return sheetData0[rowIndex0][columnIndex0].trim();
    }

    private static boolean isBlank(String value0)
    {
        return value0 == null || value0.trim().length() == 0;
    }

    private static boolean isAllowedConversationLabel(String label0)
    {
        return label0.equals("Reached Out")
            || label0.equals("First Interest")
            || label0.equals("Meetings")
            || label0.equals("Prospective Close")
            || label0.equals("Rejected");
    }

    private static String chooseCrmCandidateEmail(
        String to0,
        String from0,
        ArrayList<String> internalEmails0)
    {
        String fromEmail0 = extractFirstEmail(from0);
        String toEmail0 = extractFirstExternalEmail(to0, internalEmails0);

        if (!isBlank(fromEmail0) && !isInternalEmail(fromEmail0, internalEmails0))
        {
            return fromEmail0;
        }

        if (!isBlank(toEmail0) && !isInternalEmail(toEmail0, internalEmails0))
        {
            return toEmail0;
        }

        return "";
    }

    private static String extractFirstExternalEmail(
        String commaList0,
        ArrayList<String> internalEmails0)
    {
        if (isBlank(commaList0))
        {
            return "";
        }

        String[] splitList0 = commaList0.split(",");

        for (int i0 = 0; i0 < splitList0.length; i0++)
        {
            String email0 = cleanEmail(splitList0[i0]);

            if (!isBlank(email0) && !isInternalEmail(email0, internalEmails0))
            {
                return email0;
            }
        }

        return "";
    }

    private static String extractFirstEmail(String value0)
    {
        if (isBlank(value0))
        {
            return "";
        }

        String[] splitList0 = value0.split(",");

        if (splitList0.length == 0)
        {
            return "";
        }

        return cleanEmail(splitList0[0]);
    }

    private static String cleanEmail(String value0)
    {
        if (isBlank(value0))
        {
            return "";
        }

        String trimmed0 = value0.trim();

        int start0 = trimmed0.indexOf("<");
        int end0 = trimmed0.indexOf(">");

        if (start0 != -1 && end0 != -1 && end0 > start0)
        {
            trimmed0 = trimmed0.substring(start0 + 1, end0);
        }

        return trimmed0.trim().toLowerCase();
    }

    private static String cleanWebsite(String value0)
    {
        if (isBlank(value0))
        {
            return "";
        }

        String cleaned0 = value0.trim();

        cleaned0 = cleaned0.replaceAll("[,.;)\\]]+$", "");

        if (cleaned0.startsWith("http://"))
        {
            cleaned0 = cleaned0.substring(7);
        }

        if (cleaned0.startsWith("https://"))
        {
            cleaned0 = cleaned0.substring(8);
        }

        if (cleaned0.startsWith("www."))
        {
            cleaned0 = cleaned0.substring(4);
        }

        return cleaned0.toLowerCase();
    }

    private static boolean isInternalEmail(
        String email0,
        ArrayList<String> internalEmails0)
    {
        if (isBlank(email0))
        {
            return false;
        }

        String cleanedEmail0 = cleanEmail(email0);

        for (int i0 = 0; i0 < internalEmails0.size(); i0++)
        {
            String internalEmail0 = cleanEmail(internalEmails0.get(i0));

            if (cleanedEmail0.equalsIgnoreCase(internalEmail0))
            {
                return true;
            }
        }

        return false;
    }

    private static boolean isInternalName(
        String firstName0,
        String lastName0,
        ArrayList<String> internalNames0)
    {
        if (internalNames0 == null)
        {
            return false;
        }

        String candidate0 = normalizeText(firstName0 + " " + lastName0);

        if (isBlank(candidate0))
        {
            return false;
        }

        for (int i0 = 0; i0 < internalNames0.size(); i0++)
        {
            String internalName0 = normalizeText(internalNames0.get(i0));

            if (!isBlank(internalName0) && candidate0.equals(internalName0))
            {
                return true;
            }
        }

        return false;
    }

    private static boolean isInternalFund(
        String fundName0,
        String internalFundName0)
    {
        if (isBlank(fundName0) || isBlank(internalFundName0))
        {
            return false;
        }

        return normalizeText(fundName0).equals(normalizeText(internalFundName0));
    }

    private static boolean isInternalWebsite(
        String website0,
        String internalWebsite0)
    {
        if (isBlank(website0) || isBlank(internalWebsite0))
        {
            return false;
        }

        return cleanWebsite(website0).equals(cleanWebsite(internalWebsite0));
    }

    private static String normalizeText(String value0)
    {
        if (value0 == null)
        {
            return "";
        }

        return value0.trim().toLowerCase().replaceAll("\\s+", " ");
    }

    private static boolean isValidEmail(String email0)
    {
        if (isBlank(email0))
        {
            return false;
        }

        String cleanedEmail0 = cleanEmail(email0);

        return cleanedEmail0.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    private static JSONArray parseJsonArrayFromText(String text0)
    {
        String trimmedText0 = text0.trim();

        try
        {
            return new JSONArray(trimmedText0);
        }
        catch (Exception exception0)
        {
            int startIndex0 = trimmedText0.indexOf("[");
            int endIndex0 = trimmedText0.lastIndexOf("]");

            if (startIndex0 == -1 || endIndex0 == -1 || endIndex0 <= startIndex0)
            {
                throw exception0;
            }

            String jsonArrayText0 = trimmedText0.substring(startIndex0, endIndex0 + 1);

            return new JSONArray(jsonArrayText0);
        }
    }

    private static String buildExtractionPrompt(
        JSONArray rowsToProcess0,
        ArrayList<String> internalNames0,
        ArrayList<String> internalEmails0,
        String internalFundName0,
        String internalWebsite0)
    {
        String prompt0 =
            "You are extracting investor CRM fields from email intake rows for a venture capital fundraising CRM.\n\n"
            + "The CRM candidate is the external investor or external fund contact, not the internal team.\n"
            + "Be conservative. Do not guess. If a value is not directly supported by the email headers or body, return an empty string.\n\n"

            + "INTERNAL PEOPLE - DO NOT EXTRACT THESE PEOPLE AS INVESTORS:\n";

        for (int i0 = 0; i0 < internalNames0.size(); i0++)
        {
            prompt0 += "- " + internalNames0.get(i0) + "\n";
        }

        prompt0 += "\nINTERNAL EMAILS - DO NOT RETURN THESE EMAILS:\n";

        for (int i0 = 0; i0 < internalEmails0.size(); i0++)
        {
            prompt0 += "- " + internalEmails0.get(i0) + "\n";
        }

        prompt0 +=
            "\nINTERNAL FUND / COMPANY - DO NOT EXTRACT THIS AS THE INVESTOR FUND:\n"
            + "- " + internalFundName0 + "\n\n"

            + "INTERNAL WEBSITE - DO NOT EXTRACT THIS AS THE INVESTOR WEBSITE:\n"
            + "- " + internalWebsite0 + "\n\n"

            + "Return ONLY valid JSON. Do not include markdown. Do not explain anything.\n\n"

            + "Return a JSON array. Each object must have exactly these fields:\n"
            + "- rowNumber\n"
            + "- cleanedEmail\n"
            + "- firstName\n"
            + "- lastName\n"
            + "- fundName\n"
            + "- fundWebsite\n"
            + "- conversationLabel\n"
            + "- conversationSummary\n\n"

            + "GENERAL RULES:\n"
            + "1. Return exactly one JSON object for every input row. Do not skip rows.\n"
            + "2. Every returned object must use the same rowNumber as the input row.\n"
            + "3. Do not extract internal people, internal emails, the internal fund, or the internal website.\n"
            + "4. Do not guess. Empty string is better than a guessed value.\n"
            + "5. If a field is uncertain, return an empty string for that field.\n\n"

            + "EMAIL RULES:\n"
            + "1. cleanedEmail should be the external CRM candidate email.\n"
            + "2. Use crmCandidateEmail as the primary source.\n"
            + "3. Only use to/from/body if crmCandidateEmail is blank or clearly malformed.\n"
            + "4. Never return an internal email.\n"
            + "5. If no valid external email can be found, return cleanedEmail as an empty string.\n\n"

            + "NAME RULES:\n"
            + "1. firstName and lastName should belong to the external CRM candidate, not an internal person.\n"
            + "2. Names often appear in the email sender display name, email signature, or sign-off.\n"
            + "3. In inbound emails, the signature often belongs to the external investor.\n"
            + "4. In outbound emails, the signature often belongs to the internal sender, so do not use the outbound signature as investor identity.\n"
            + "5. If only the first name is clear, return firstName and leave lastName empty.\n"
            + "6. If only the last name is clear, return lastName and leave firstName empty unless firstName is clear elsewhere.\n"
            + "7. Do not infer a last name from an email domain unless the full name is clearly shown.\n\n"

            + "FUND NAME RULES:\n"
            + "1. fundName should be the external investor's fund, firm, foundation, bank, family office, or investment organization.\n"
            + "2. Fund names often appear in email domains, signatures, sender organization names, or phrases like Partner at, Managing Director at, from, or on behalf of.\n"
            + "3. Do not return the internal fund name.\n"
            + "4. Do not invent a fund name from a generic email domain like gmail.com, outlook.com, yahoo.com, icloud.com, or protonmail.com.\n"
            + "5. If the organization is not clear, return an empty string.\n"
            + "6. Examples of fund-like names include: Sequoia Capital, Andreessen Horowitz, Accel, Bessemer Venture Partners, General Catalyst, Lightspeed Venture Partners, Founders Fund, Khosla Ventures, NEA, Insight Partners, Index Ventures, Union Square Ventures, Kapor Capital, Acumen, BlueOrchard, FMO, LGT Venture Philanthropy, Global Innovation Fund, ImpactAssets, Omidyar Network, Ford Foundation, Rockefeller Foundation.\n\n"

            + "WEBSITE RULES:\n"
            + "1. fundWebsite should be the external investor organization's website.\n"
            + "2. Do not return a random link mentioned in the email.\n"
            + "3. Do not return links to scheduling tools, LinkedIn, Zoom, Google Meet, DocSend, Dropbox, Google Drive, Calendly, YouTube, news articles, PDFs, unsubscribe links, or tracking links.\n"
            + "4. Do not return the internal website.\n"
            + "5. A good website usually matches or closely resembles the fundName or the external email domain.\n"
            + "6. If multiple links exist, only return the one that clearly belongs to the external investor's organization.\n"
            + "7. If the website is not clearly tied to the fundName or external email domain, return an empty string.\n"
            + "8. Return websites as domains only when possible, like examplefund.com.\n\n"

            + "CONVERSATION SUMMARY RULES:\n"
            + "1. conversationSummary should be one concise sentence summarizing the meaningful investor interaction.\n"
            + "2. Mention what happened, requested materials, interest level, scheduling, rejection, or next step if clear.\n"
            + "3. Do not include internal-only signature details.\n"
            + "4. If the email has little substance, summarize it conservatively.\n\n"

            + "CONVERSATION LABEL RULES:\n"
            + "conversationLabel must be exactly one of these labels:\n"
            + "- Reached Out\n"
            + "- First Interest\n"
            + "- Meetings\n"
            + "- Prospective Close\n"
            + "- Rejected\n\n"

            + "Label definitions:\n"
            + "Reached Out = first contact, cold outreach, intro, or no meaningful reply yet.\n"
            + "First Interest = curiosity, reply, prior discussion, request for information, or early interest.\n"
            + "Meetings = call, meeting, deck review, diligence discussion, scheduling, or follow-up after a meeting.\n"
            + "Prospective Close = investor sounds close to committing, allocation approval, or final investment decision.\n"
            + "Rejected = investor declines, passes, says not a fit, or says they cannot invest.\n\n"

            + "Rows to process:\n"
            + rowsToProcess0.toString(2);

        return prompt0;
    }
}