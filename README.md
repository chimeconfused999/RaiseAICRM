# RaiseAI

**AI-powered investor relationship management for emerging venture capital partners.**

RaiseAI turns a messy inbox and a spreadsheet into a living fundraising CRM. It syncs
investor emails from Gmail, uses AI to structure every conversation, enriches investor
profiles from the web, scores and prioritizes relationships, and writes everything back
to a Google Sheet you already own — all from a clean web app (or the command line).

---

## What it does

| Capability | Description |
| --- | --- |
| **Gmail sync** | Pulls recent investor emails into an intake tab, de-duplicated by message ID. |
| **AI intake** | Extracts contacts, funds, sentiment, and next steps from each email. |
| **CRM update** | Writes structured rows into your Google Sheets CRM, merging duplicates intelligently. |
| **LP enrichment** | Fills in sector, geography, and an intelligence summary by crawling the web (Bright Data). |
| **Candidate scoring** | Builds a profile from your warmest investors, then scores everyone else by fit. |
| **Relationship priorities** | Ranks who to act on next and writes a priority score + reasoning. |
| **Follow-up recommendations** | Generates the concrete next step for each relationship. |
| **Find candidates** | Discovers brand-new investor candidates via LinkedIn / SERP and adds them as Cold leads. |
| **Sheet agent** | Natural-language spreadsheet edits ("mark Acme as First Interest"). |

Everything runs against **your own Google Sheet** — RaiseAI never stores your data.

---

## Architecture

```
Browser (login.html / app.html)
        │  JSON over HTTP
        ▼
WebServer.java (Javalin)  ──►  CRMRegistry / CRMOnboard      (accounts, config)
        │                 ──►  GmailService                   (Gmail → intake tab)
        │                 ──►  EmailIntakeProcessor            (AI extraction)
        │                 ──►  CrmUpdater                      (write to CRM tab)
        │                 ──►  LPEnrichmentProcessor           (web enrichment)
        │                 ──►  PriorityActionProcessor         (scoring + priorities)
        │                 ──►  FollowUpRecommender             (next-step recs)
        │                 ──►  WebSheetAgent                   (natural-language edits)
        ▼
SheetsApp.java  ──►  Google Sheets + Gmail  (shared OAuth credential)
OpenAIClient · WebsiteCrawlerService  ──►  OpenAI + Bright Data
```

**Tech stack:** Java 11+ · [Javalin](https://javalin.io/) web server · Google Sheets &
Gmail APIs · OpenAI API · Bright Data · vanilla HTML/CSS/JS frontend · Maven build.

---

## Prerequisites

- **JDK 11+** (built and tested on Temurin 21) — check with `java -version`
- **Maven 3.9+** — check with `mvn -version`
- A **Google Cloud project** with the **Sheets API** and **Gmail API** enabled
- An **OpenAI API key**
- *(Optional, for enrichment & candidate discovery)* a **Bright Data API token**

---

## Getting started

### 1. Clone

```bash
git clone https://github.com/rohanpunnoosep-afk/raiseai.git
cd raiseai
```

### 2. Add your Google OAuth credentials

RaiseAI uses Google OAuth to read your Sheets and Gmail.

1. In [Google Cloud Console → Credentials](https://console.cloud.google.com/apis/credentials),
   create an **OAuth client ID** of type **Desktop app**.
2. Enable the **Google Sheets API** and **Gmail API** for the project.
3. Download the client JSON and save it as `src/main/resources/credentials.json`.
   (See `src/main/resources/credentials.json.example` for the expected shape.)

This file is gitignored and must never be committed.

### 3. Add your API keys

Copy the template and fill in your own values:

```bash
cp .env.example .env       # PowerShell: Copy-Item .env.example .env
```

```ini
OPENAI_API_KEY=sk-your-openai-key
BRIGHT_DATA_API_TOKEN=your-bright-data-token   # optional
BRIGHT_DATA_SERP_ZONE=serp_api1                # optional, defaults to serp_api1
PORT=7070                                       # optional, defaults to 7070
```

`.env` is gitignored — keep your keys out of source control.

### 4. Run

**Windows (PowerShell)** — `run.ps1` loads `.env` and starts the server:

```powershell
./run.ps1
```

**macOS / Linux:**

```bash
set -a && source .env && set +a
mvn compile exec:java
```

Then open **<http://localhost:7070>**. On first sign-in, Google opens a consent screen
to authorize Sheets + Gmail; the resulting token is cached in `tokens/` (gitignored).

### 5. Use it

1. Create an account / sign in with Google on the login page.
2. Paste the link to your Google Sheet (with an intake tab and a CRM tab).
3. Sync Gmail → process intake → update CRM, then run enrichment, scoring,
   priorities, and follow-ups from the workspace.

---

## Project layout

```
raiseai/
├─ src/main/java/                 # Backend (Java)
│  ├─ WebServer.java              # HTTP API + static hosting (entry point)
│  ├─ SheetsApp.java              # Google Sheets + Gmail OAuth client
│  ├─ GmailService.java           # Gmail → intake sync
│  ├─ EmailIntakeProcessor.java   # AI email extraction
│  ├─ CrmUpdater.java             # CRM writes + dedup
│  ├─ LPEnrichmentProcessor.java  # Web enrichment
│  ├─ PriorityActionProcessor.java# Scoring + relationship priorities
│  ├─ FollowUpRecommender.java    # Next-step recommendations
│  ├─ WebSheetAgent.java          # Natural-language sheet agent
│  └─ ...                         # Tool framework, OpenAI/Bright Data clients
├─ src/main/resources/public/     # Frontend (login + workspace UI)
├─ .env.example                   # API key template
├─ run.ps1                        # Windows run helper
└─ pom.xml                        # Maven build (mainClass = WebServer)
```

The original CLI still works: temporarily set the main class to `AgentMain` in
`pom.xml`, then `mvn compile exec:java`.

---

