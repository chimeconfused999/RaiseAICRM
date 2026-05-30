const SESSION_KEY = "raiseai.session";
const APP_URL = "/app.html";

const $ = (id) => document.getElementById(id);

if (localStorage.getItem(SESSION_KEY)) {
  window.location.replace(APP_URL);
}

function saveSession(data) {
  localStorage.setItem(SESSION_KEY, JSON.stringify(data));
}

function goToApp() {
  window.location.replace(APP_URL);
}

function showResult(message, ok) {
  const box = $("authResult");
  box.textContent = message;
  box.className = "result " + (ok ? "ok" : "err");
  box.classList.remove("hidden");
}

function setBusy(busy) {
  document.querySelectorAll("button").forEach((b) => (b.disabled = busy));
}

async function postJson(path, body) {
  const res = await fetch(path, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body || {}),
  });
  const text = await res.text();
  if (!text) {
    return { ok: false, status: res.status, message: `Empty response (HTTP ${res.status}). Is the server running?` };
  }
  try {
    const json = JSON.parse(text);
    json.status = res.status;
    return json;
  } catch {
    return { ok: false, status: res.status, message: text.slice(0, 200) };
  }
}

function showTab(tab) {
  document.querySelectorAll(".tab").forEach((b) => {
    b.classList.toggle("active", b.dataset.tab === tab);
  });
  $("loginPanel").classList.toggle("hidden", tab !== "login");
  $("signupPanel").classList.toggle("hidden", tab !== "signup");
}

async function checkHealth() {
  const pill = $("healthStatus");
  try {
    const res = await fetch("/api/health");
    const data = await res.json();
    if (data.ok) {
      pill.textContent = "Server online";
      pill.className = "status-pill ok";
      return;
    }
    throw new Error();
  } catch {
    pill.textContent = "Server offline";
    pill.className = "status-pill err";
  }
}

async function signInWithEmail(email) {
  const data = await postJson("/api/login", { email });
  if (data.ok && data.data) {
    saveSession(data.data);
    goToApp();
    return true;
  }
  return false;
}

async function onGoogle() {
  setBusy(true);
  showResult("Opening Google sign-in… complete it in the browser window.", true);
  try {
    const data = await postJson("/api/auth/google", {});
    if (!data.ok || !data.data?.email) {
      showResult(data.message || "Google sign-in failed.", false);
      return;
    }

    const email = data.data.email;
    const loggedIn = await signInWithEmail(email);
    if (!loggedIn) {
      showTab("signup");
      $("signupEmail").value = email;
      showResult(
        `Signed in to Google as ${email}, but there's no RaiseAI account yet. Create one below.`,
        true
      );
    }
  } catch (e) {
    showResult(e.message, false);
  } finally {
    setBusy(false);
  }
}

async function onLogin() {
  const email = $("loginEmail").value.trim();
  if (!email) {
    showResult("Enter your email.", false);
    return;
  }
  setBusy(true);
  try {
    const data = await postJson("/api/login", { email });
    if (data.ok && data.data) {
      saveSession(data.data);
      goToApp();
    } else {
      showResult(data.message || "Login failed.", false);
    }
  } catch (e) {
    showResult(e.message, false);
  } finally {
    setBusy(false);
  }
}

async function onRegister() {
  const body = {
    email: $("signupEmail").value.trim(),
    fundName: $("signupFundName").value.trim(),
    spreadsheetUrl: $("signupSheetLink").value.trim(),
    internalNames: $("signupInternalNames").value.trim(),
    internalEmails: $("signupInternalEmails").value.trim(),
  };

  if (!body.email || !body.fundName || !body.spreadsheetUrl) {
    showResult("Email, fund name, and Google Sheets link are required.", false);
    return;
  }

  setBusy(true);
  showResult("Creating account — AI is mapping your sheet…", true);
  try {
    const data = await postJson("/api/onboard/register", body);
    if (data.ok && data.data) {
      saveSession(data.data);
      goToApp();
    } else {
      showResult(data.message || "Account creation failed.", false);
    }
  } catch (e) {
    showResult(e.message, false);
  } finally {
    setBusy(false);
  }
}

async function parseLink() {
  const link = $("signupSheetLink").value.trim();
  const note = $("parsedSheetId");
  if (!link) {
    note.textContent = "";
    return;
  }
  try {
    const data = await postJson("/api/onboard/parse-link", { spreadsheetUrl: link });
    note.textContent = data.ok ? "Spreadsheet ID: " + data.data.spreadsheetId : data.message;
  } catch (e) {
    note.textContent = e.message;
  }
}

function init() {
  document.querySelectorAll(".tab").forEach((b) => {
    b.addEventListener("click", () => showTab(b.dataset.tab));
  });
  $("btnGoogle").addEventListener("click", onGoogle);
  $("btnLogin").addEventListener("click", onLogin);
  $("btnRegister").addEventListener("click", onRegister);
  $("signupSheetLink").addEventListener("blur", parseLink);
  $("loginEmail").addEventListener("keydown", (e) => {
    if (e.key === "Enter") onLogin();
  });

  checkHealth();
}

init();
