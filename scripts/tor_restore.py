from pathlib import Path

p = Path("app/src/main/java/com/nexvary/shurayh/ShurayhApp.kt")
s = p.read_text()

if 'const val TOR_MESSAGING = "tor_messaging"' not in s:
    s = s.replace('    const val MENU = "menu"\n', '    const val MENU = "menu"\n    const val TOR_MESSAGING = "tor_messaging"\n')

if 'composable(Routes.TOR_MESSAGING) { TorMessagingScreen(nav) }' not in s:
    s = s.replace('                composable(Routes.MENU) { MenuScreen(nav) }\n', '                composable(Routes.MENU) { MenuScreen(nav) }\n                composable(Routes.TOR_MESSAGING) { TorMessagingScreen(nav) }\n')

module_line = '        ModuleItem("المحاكم", "دليل الجهات", Icons.Outlined.AccountBalance, Routes.COURTS)\n'
insert_line = '        ModuleItem("المراسلة الآمنة", "Tor • محادثات واتصال آمن", Icons.Outlined.Lock, Routes.TOR_MESSAGING),\n'
if insert_line not in s:
    s = s.replace(module_line, insert_line + module_line)

p.write_text(s)
