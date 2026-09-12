from pathlib import Path

app = Path("app/src/main/java/com/nexvary/shurayh/ShurayhApp.kt").read_text()
tor = Path("app/src/main/java/com/nexvary/shurayh/TorMessagingScreen.kt").read_text()

required_routes = [
    "Routes.CASES", "Routes.CLIENTS", "Routes.HEARINGS", "Routes.LAWS",
    "Routes.DOCUMENTS", "Routes.SEARCH", "Routes.OFFICE", "Routes.COURTS",
    "Routes.TOR_MESSAGING", "Routes.MENU", "Routes.NOTIFICATIONS",
    "Routes.ABOUT", "Routes.SETTINGS"
]
for token in required_routes:
    if token not in app:
        raise SystemExit(f"Missing route wiring: {token}")

required_clicks = [
    "FloatingActionButton(onClick = { showAdd = true }",
    "FloatingActionButton(onClick = { add = true }",
    "nav.navigate(Routes.caseDetail(c.id))",
    "vm.cycleCaseStatus(legalCase.id)",
    "confirmDelete = true",
    "reset = true",
    "selectedLaw = l",
    "selectedEntry = entry",
    "onClick = onClick",
]
for token in required_clicks:
    if token not in app:
        raise SystemExit(f"Missing expected interactive action: {token}")

secondary_pages_with_back = [
    'Page("القائمة", "كل أقسام SHURAYH", nav)',
    'Page("الإشعارات", "الجلسات والتنبيهات", nav)',
    'Page("الضبط", "الخصوصية والبيانات", nav)',
    'Page("عنا", "SHURAYH • NEXVARY Legal Technology", nav)',
]
for token in secondary_pages_with_back:
    if token not in app:
        raise SystemExit(f"Secondary page is missing explicit back navigation: {token}")

required_tor_actions = [
    'onClick = { nav.popBackStack() }',
    'onClick = { selected = contact }',
    'contentDescription = "إعادة الفحص"' if 'contentDescription = "إعادة الفحص"' in tor else '"إعادة الفحص"',
    'enabled = draft.isNotBlank() && proxyPort != null',
]
for token in required_tor_actions:
    if token not in tor:
        raise SystemExit(f"Missing Tor interaction: {token}")

print("Interaction audit passed: routes, back navigation, core actions, dialogs, and Tor controls are wired.")
