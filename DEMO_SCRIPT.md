# RaiseAI — Demo Procedure

No live talking — record the actions, add captions in edit. Each step has an action to film
and an optional **Caption** you can overlay.

---

## Sign-up info (use these exact values)

| Field | Value |
|---|---|
| Email (login ID) | `trusta@demo.com` *(any new ID works)* |
| Fund name | `Trusta Health Fund` |
| Google Sheet link | *(your Test Sheet link)* |
| Internal names | `Rohan\|Rohan Punnoose` |
| Internal emails | `rohan.punnoose.p@gmail.com` |

> The internal name/email values are critical — they tell the system rohan is the *sender*, so
> each email's recipient is recorded as the investor (otherwise everything collapses to one "rohan" row).

---

## Part A — Prep (do once, before recording)

1. Restart the server: in the terminal press `Ctrl+C`, then run `./run.ps1`.
2. Be logged into the account created with the sign-up info above.
3. Trim **EmailIntake** to ~8 rows that have real fund info (delete the rest).
4. For those 8 rows, clear columns: **Processing Status (10)**, **Updated CRM (18)**,
   **Needs Review (19)**, and **Extracted cols (11–15)**.
5. Clear the data rows in **CRMTest** (keep header row 1) so the CRM fills up on camera.
6. Confirm **CRMTest** has a **"Follow Up Recommendation"** column.

---

## Captions (one per section — overlay these)

**Intro / title card**
> **RaiseAI** — the fundraising CRM that runs itself.
> Your inbox in. A prioritized, enriched investor pipeline out.

**1. Login**
> Sign in with Google — your CRM, instantly connected.

**2. Gmail sync** *(point at it, don't click)*
> Investor emails sync straight from Gmail into your sheet.

**3. Process intake**
> AI reads every email — extracting names, funds, and context.

**4. Update CRM**
> 10 investors written into the CRM. Zero manual entry.

**5. Enrich LPs**
> Each investor researched — sector, geography, thesis, and prior funds.

**6. (Set a warm lead — Sheet agent)**
> Mark a warm lead in plain English. No clicking through cells.

**7. Score candidates**
> Builds a profile of your warmest investors — then scores every other investor by how closely they match, so you see who's most likely to convert.

**8. Prioritize**
> A priority score *and* a concrete next action for each investor.

**9. Follow-ups**
> Auto-drafted follow-ups for your top 5 highest-priority contacts.

**10. Find candidates**
> Discovers brand-new investors from the web — added as Cold leads.

**11. Sheet agent**
> Edit the entire spreadsheet by just typing what you want.

**Outro / closing card**
> From a cold inbox to a scored, prioritized, growing pipeline — in minutes.

---

## Run order (what to click)

1. **Login** → dashboard
2. **Gmail sync** — point at it, don't click
3. **Process intake**
4. **Update CRM**
5. **Enrich LPs**
6. **Sheet agent**: `Set FMO's status to First Interest` *(needed before scoring)*
7. **Score candidates**
8. **Prioritize** → open the sheet, show Priority Score + Next Action
9. **Follow-ups**
10. **Find candidates** — fill fields below, **Discover & append**
11. **Sheet agent**: `List every investor with a priority score above 70`

| Find-candidates field | Value |
|---|---|
| Sectors | `Healthcare, Digital Health` |
| Microsectors | `Telehealth` |
| Geographies | `United States` |
| Thesis / notes | `Early-stage health-tech fund` |
| Results / query | `3` |
| Max candidates | `5` |
| Scrape LinkedIn **ON** · Scrape websites **ON** · Extract profiles **ON** | |
