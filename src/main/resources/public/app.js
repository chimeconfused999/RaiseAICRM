const SESSION_KEY = "raiseai.session";
const GMAIL_KEYWORD_KEY = "raiseai.gmailKeyword";

const $ = (sel) => document.querySelector(sel);
const $$ = (sel) => Array.from(document.querySelectorAll(sel));

const VIEWS = {
  email: { title: "Email pipeline", desc: "Sync investor email into your CRM and let AI structure it." },
  enrich: { title: "LP enrichment", desc: "Add sector, geography, and intelligence data to your LP rows." },
  score: { title: "Candidate scoring", desc: "Score candidates against your warm investor profile." },
  prioritize: { title: "Relationship priorities", desc: "Rank who to act on next across your CRM." },
  followups: { title: "Follow-ups", desc: "Generate next-step recommendations for your top contacts." },
  discover: { title: "Find candidates", desc: "Discover new investor candidates from the web." },
  agent: { title: "Sheet agent", desc: "Natural-language spreadsheet edits via OpenAI." },
};

const ACTIONS = {
  "sync-gmail": { path: "/api/pipeline/sync-gmail", label: "Sync Gmail", body: () => ({ gmailKeyword: gmailKeyword() }) },
  "process-intake": { path: "/api/pipeline/process-intake", label: "Process intake" },
  "update-crm": { path: "/api/pipeline/update-crm", label: "Update CRM" },
  "full": { path: "/api/pipeline/full", label: "Full pipeline", body: () => ({ gmailKeyword: gmailKeyword() }) },
  "enrich-lps": { path: "/api/pipeline/enrich-lps", label: "Enrich LPs" },
  "score-candidates": { path: "/api/pipeline/score-candidates", label: "Score candidates", body: () => ({ maxRows: intVal("#scoreMaxRows", 10) }) },
  "prioritize": { path: "/api/pipeline/prioritize", label: "Prioritize", body: () => ({ maxRows: intVal("#prioritizeMaxRows", 25) }) },
  "follow-ups": { path: "/api/pipeline/follow-ups", label: "Follow-ups", after: renderFollowups },
  "discover-candidates": {
    path: "/api/pipeline/discover-candidates",
    label: "Discover candidates",
    body: () => ({
      sectors: val("#discSectors"),
      microsectors: val("#discMicrosectors"),
      geographies: val("#discGeographies"),
      thesis: val("#discThesis"),
      maxResultsPerQuery: intVal("#discMaxResults", 5),
      maxCandidates: intVal("#discMaxCandidates", 20),
      scrapeLinkedIn: checked("#discScrapeLinkedIn"),
      scrapeWebsites: checked("#discScrapeWebsites"),
      extractProfiles: checked("#discExtractProfiles"),
    }),
  },
  "agent": { path: "/api/agent", label: "Sheet agent", body: () => ({ prompt: val("#agentPrompt") }) },
};

// ---------- session ----------
function getSession() {
  try {
    return JSON.parse(localStorage.getItem(SESSION_KEY) || "null");
  } catch {
    return null;
  }
}

function getEmail() {
  return getSession()?.email || "";
}

// ---------- small helpers ----------
const val = (sel) => ($(sel) ? $(sel).value.trim() : "");
const checked = (sel) => ($(sel) ? $(sel).checked : false);
const intVal = (sel, def) => {
  const n = parseInt(val(sel), 10);
  return Number.isFinite(n) ? n : def;
};
const gmailKeyword = () => val("#gmailKeyword");

function escapeHtml(text) {
  const div = document.createElement("div");
  div.textContent = text == null ? "" : String(text);
  return div.innerHTML;
}

// ---------- ui state ----------
function applySession() {
  const s = getSession();
  if (!s) return;
  $("#acctEmail").textContent = s.email || "";
  $("#acctFund").textContent = s.fundName || "";
  $("#acctAvatar").textContent = (s.fundName || s.email || "?").trim().charAt(0).toUpperCase();
  const sheet = $("#acctSheet");
  if (s.spreadsheetUrl) {
    sheet.href = s.spreadsheetUrl;
  } else {
    sheet.classList.add("hidden");
  }
}

function showView(view) {
  $$(".nav-item").forEach((b) => b.classList.toggle("active", b.dataset.view === view));
  $$(".view").forEach((v) => v.classList.toggle("active", v.dataset.panel === view));
  const meta = VIEWS[view];
  if (meta) {
    $("#viewTitle").textContent = meta.title;
    $("#viewDesc").textContent = meta.desc;
  }
}

function setBusy(busy, text = "Working…") {
  const overlay = $("#overlay");
  $("#overlayText").textContent = text;
  overlay.classList.toggle("hidden", !busy);
  overlay.setAttribute("aria-hidden", busy ? "false" : "true");
  $$("button").forEach((b) => (b.disabled = busy));
}

// ---------- activity log ----------
function clearEmptyLog() {
  const empty = $(".activity-empty");
  if (empty) empty.remove();
}

function appendLog(label, message, ok) {
  clearEmptyLog();
  const log = $("#activityLog");
  const entry = document.createElement("div");
  entry.className = "log-entry";
  entry.innerHTML =
    `<div class="log-meta"><span class="log-dot ${ok ? "ok" : "err"}"></span>` +
    `<span class="log-label">${escapeHtml(label)}</span>` +
    `<span class="log-time">${new Date().toLocaleTimeString()}</span></div>` +
    `<div class="log-body">${escapeHtml(message)}</div>`;
  log.prepend(entry);
}

// ---------- networking ----------
async function apiPost(path, body = {}) {
  const email = getEmail();
  if (email) body.email = email;
  const res = await fetch(path, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
  const text = await res.text();
  if (!text) {
    return { ok: false, message: `Empty response (HTTP ${res.status}). Is the server running?` };
  }
  try {
    return JSON.parse(text);
  } catch {
    return { ok: false, message: `Non-JSON response (HTTP ${res.status}): ${text.slice(0, 200)}` };
  }
}

async function checkHealth() {
  const pill = $("#healthStatus");
  try {
    const res = await fetch("/api/health");
    const data = await res.json();
    if (!data.ok) throw new Error();
    pill.textContent = "Online";
    pill.className = "status-pill ok";
  } catch {
    pill.textContent = "Offline";
    pill.className = "status-pill err";
  }
}

// ---------- action runner ----------
function showResult(action, message, ok) {
  const box = $(`[data-result="${action}"]`);
  if (!box) return;
  box.textContent = message;
  box.className = "result " + (ok ? "ok" : "err");
  box.classList.remove("hidden");
}

async function runAction(action) {
  const cfg = ACTIONS[action];
  if (!cfg) return;

  if (!getEmail()) {
    window.location.replace("/");
    return;
  }

  const body = cfg.body ? cfg.body() : {};
  if (action === "sync-gmail" || action === "full") {
    localStorage.setItem(GMAIL_KEYWORD_KEY, gmailKeyword());
  }

  setBusy(true, cfg.label + "…");
  try {
    const data = await apiPost(cfg.path, body);

    if (data.data?.steps) {
      data.data.steps.forEach((s) => appendLog(s.name, s.message, s.ok));
    }

    showResult(action, data.message || (data.ok ? "Done." : "Failed."), data.ok);
    appendLog(cfg.label, data.message || JSON.stringify(data), data.ok);

    if (cfg.after) cfg.after(data);
  } catch (e) {
    showResult(action, e.message, false);
    appendLog(cfg.label, e.message, false);
  } finally {
    setBusy(false);
  }
}

function renderFollowups(data) {
  const list = $("#followupList");
  const items = data.data?.recommendations || [];
  if (!items.length) {
    list.classList.add("hidden");
    return;
  }
  list.innerHTML = items
    .map(
      (r) =>
        `<li><span class="rec-email">${escapeHtml(r.email)}</span>` +
        `<span class="rec-text">${escapeHtml(r.recommendation)}</span></li>`
    )
    .join("");
  list.classList.remove("hidden");
}

// ---------- init ----------
function init() {
  applySession();

  $$(".nav-item").forEach((btn) => {
    btn.addEventListener("click", () => showView(btn.dataset.view));
  });

  $$("[data-run]").forEach((btn) => {
    btn.addEventListener("click", () => runAction(btn.dataset.run));
  });

  $("#btnLogout").addEventListener("click", () => {
    localStorage.removeItem(SESSION_KEY);
    window.location.replace("/");
  });

  $("#btnClearLog").addEventListener("click", () => {
    $("#activityLog").innerHTML = '<p class="activity-empty">Actions you run will appear here.</p>';
  });

  const savedKeyword = localStorage.getItem(GMAIL_KEYWORD_KEY);
  if (savedKeyword && $("#gmailKeyword")) $("#gmailKeyword").value = savedKeyword;

  showView("email");
  checkHealth();
  setInterval(checkHealth, 30000);
}

init();
