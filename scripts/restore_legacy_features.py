from pathlib import Path

p = Path('app/src/main/java/com/nexvary/shurayh/ShurayhApp.kt')
s = p.read_text(encoding='utf-8')

routes_anchor = '    const val TOR_MESSAGING = "tor_messaging"\n'
routes_add = routes_anchor + '    const val ASK_SHURAYH = "ask_shurayh"\n    const val SCAN_REVIEW = "scan_review"\n    const val DOCUMENT_FORENSICS = "document_forensics"\n    const val LAWYER_PROFILE = "lawyer_profile"\n    const val LAWYER_NETWORK = "lawyer_network"\n'
if 'const val ASK_SHURAYH' not in s:
    s = s.replace(routes_anchor, routes_add)

nav_anchor = '                composable(Routes.TOR_MESSAGING) { TorMessagingScreen(nav) }\n'
nav_add = nav_anchor + '                composable(Routes.ASK_SHURAYH) { AskShurayhScreen(nav, state.lawBooks) }\n                composable(Routes.SCAN_REVIEW) { ScanReviewScreen(nav) }\n                composable(Routes.DOCUMENT_FORENSICS) { DocumentForensicsScreen(nav) }\n                composable(Routes.LAWYER_PROFILE) { LawyerProfileScreen(nav) }\n                composable(Routes.LAWYER_NETWORK) { LawyerNetworkScreen(nav) }\n'
if 'AskShurayhScreen(nav' not in s:
    s = s.replace(nav_anchor, nav_add)

old_modules = '        ModuleItem("ملف المكتب", "مؤشرات العمل", Icons.Outlined.FolderShared, Routes.OFFICE),\n        ModuleItem("المراسلة الآمنة", "Tor • محادثات واتصال آمن", Icons.Outlined.Lock, Routes.TOR_MESSAGING),\n        ModuleItem("المحاكم", "دليل الجهات", Icons.Outlined.AccountBalance, Routes.COURTS)'
new_modules = '        ModuleItem("ملف المكتب", "مؤشرات العمل", Icons.Outlined.FolderShared, Routes.OFFICE),\n        ModuleItem("اسأل شُرَيْح", "بحث قانوني محلي بالمصادر والصوت", Icons.Outlined.PsychologyAlt, Routes.ASK_SHURAYH),\n        ModuleItem("مسح ومراجعة المحاضر", "استيراد المستند ثم مراجعة النص", Icons.Outlined.DocumentScanner, Routes.SCAN_REVIEW),\n        ModuleItem("الفحص الجنائي للمستند", "مؤشرات أولية دون ادعاء إثبات التزوير", Icons.Outlined.FactCheck, Routes.DOCUMENT_FORENSICS),\n        ModuleItem("صفحة المحامي", "الملف المهني والبيانات المحلية", Icons.Outlined.Badge, Routes.LAWYER_PROFILE),\n        ModuleItem("شبكة المحامين", "تنظيم جهات الاتصال والتعاون المهني", Icons.Outlined.Groups, Routes.LAWYER_NETWORK),\n        ModuleItem("المراسلة الآمنة", "Tor • محادثات واتصال آمن", Icons.Outlined.Lock, Routes.TOR_MESSAGING),\n        ModuleItem("المحاكم", "دليل الجهات", Icons.Outlined.AccountBalance, Routes.COURTS)'
if 'ModuleItem("اسأل شُرَيْح"' not in s:
    s = s.replace(old_modules, new_modules)

p.write_text(s, encoding='utf-8')
print('legacy feature recovery patch applied')
