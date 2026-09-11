package com.nexvary.shurayh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nexvary.shurayh.core.*

private val Navy = Color(0xFF0C1319)
private val Gunmetal = Color(0xFF2E3945)
private val Silver = Color(0xFF9E9B98)
private val Platinum = Color(0xFFD9D7D4)
private val ElectricBlue = Color(0xFF6A88A0)
private val RoyalGold = Color(0xFFC9A44A)
private val Success = Color(0xFF5FAF83)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                MaterialTheme { ShurayhApp() }
            }
        }
    }
}

private object Routes {
    const val HOME = "home"
    const val CASES = "cases"
    const val CLIENTS = "clients"
    const val HEARINGS = "hearings"
    const val LAWS = "laws"
    const val DOCUMENTS = "documents"
    const val SEARCH = "search"
    const val COURTS = "courts"
    const val OFFICE = "office"
    const val ABOUT = "about"
    const val CASE_DETAIL = "case/{caseId}"
    fun caseDetail(id: String) = "case/$id"
}

data class ModuleItem(val title: String, val subtitle: String, val icon: ImageVector, val route: String)

@Composable
fun ShurayhApp(vm: ShurayhViewModel = viewModel()) {
    val nav = rememberNavController()
    val state by vm.uiState.collectAsStateWithLifecycle()

    NavHost(navController = nav, startDestination = Routes.HOME) {
        composable(Routes.HOME) { HomeScreen(nav, state) }
        composable(Routes.CASES) { CasesScreen(nav, state.cases) }
        composable(Routes.CLIENTS) { ClientsScreen(nav, state.clients) }
        composable(Routes.HEARINGS) { HearingsScreen(nav, state.hearings) }
        composable(Routes.LAWS) { LawsScreen(nav, state.lawBooks) }
        composable(Routes.DOCUMENTS) { DocumentsScreen(nav, state.documents) }
        composable(Routes.SEARCH) { SearchScreen(nav, state, vm::setSearchQuery) }
        composable(Routes.COURTS) { SimpleInfoScreen(nav, "المحاكم والجهات", listOf("المحاكم الابتدائية", "محاكم الاستئناف", "المحاكم الاقتصادية", "مجلس الدولة", "مكاتب الشهر العقاري")) }
        composable(Routes.OFFICE) { OfficeScreen(nav, state) }
        composable(Routes.ABOUT) { AboutScreen(nav) }
        composable(
            Routes.CASE_DETAIL,
            arguments = listOf(navArgument("caseId") { type = NavType.StringType })
        ) { entry ->
            val id = entry.arguments?.getString("caseId")
            CaseDetailScreen(nav, state.cases.firstOrNull { it.id == id })
        }
    }
}

@Composable
private fun HomeScreen(nav: NavHostController, state: ShurayhUiState) {
    val modules = listOf(
        ModuleItem("القضايا", "ملفات القضايا والمستندات والملاحظات", Icons.Outlined.Gavel, Routes.CASES),
        ModuleItem("العملاء", "الملفات الشخصية وبيانات التواصل", Icons.Outlined.PeopleAlt, Routes.CLIENTS),
        ModuleItem("الجلسات", "المواعيد والتنبيهات والمتابعة", Icons.Outlined.CalendarMonth, Routes.HEARINGS),
        ModuleItem("مكتبة القانون", "القوانين والبحث المرجعي", Icons.Outlined.MenuBook, Routes.LAWS),
        ModuleItem("المذكرات والعقود", "إنشاء وتنظيم المستندات", Icons.Outlined.Description, Routes.DOCUMENTS),
        ModuleItem("ملف المكتب", "ملخص المكتب والعمل", Icons.Outlined.FolderShared, Routes.OFFICE),
        ModuleItem("البحث الذكي", "بحث موحد داخل بيانات التطبيق", Icons.Outlined.Search, Routes.SEARCH),
        ModuleItem("المحاكم والجهات", "دليل قانوني منظم", Icons.Outlined.AccountBalance, Routes.COURTS)
    )

    ScreenScaffold(title = "شُرَيْح", subtitle = "SHURAYH • المساعد القانوني الذكي للمحامي", nav = null) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).background(Navy),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = Gunmetal), shape = RoundedCornerShape(22.dp)) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("لوحة العمل القانونية", color = RoyalGold, fontSize = 22.sp, fontWeight = FontWeight.Black)
                        Text("${state.cases.size} قضايا • ${state.clients.size} عملاء • ${state.hearings.size} جلسات قادمة", color = Platinum)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatusPill("RTL", ElectricBlue)
                            StatusPill("Android 15", Silver)
                            StatusPill("Local-first", RoyalGold)
                        }
                    }
                }
            }
            item { SectionTitle("الأقسام الرئيسية") }
            items(modules.chunked(2)) { rowItems ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    rowItems.forEach { module ->
                        ModuleCard(module, Modifier.weight(1f)) { nav.navigate(module.route) }
                    }
                    if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                }
            }
            item {
                TextButton(onClick = { nav.navigate(Routes.ABOUT) }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Outlined.Info, null)
                    Spacer(Modifier.width(8.dp))
                    Text("حول SHURAYH")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScreenScaffold(
    title: String,
    subtitle: String? = null,
    nav: NavHostController?,
    content: @Composable (PaddingValues) -> Unit
) {
    if (nav != null) BackHandler { nav.popBackStack() }
    Scaffold(
        containerColor = Navy,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(title, color = Platinum, fontWeight = FontWeight.Black)
                        subtitle?.let { Text(it, color = Silver, fontSize = 11.sp) }
                    }
                },
                navigationIcon = {
                    if (nav != null) {
                        IconButton(onClick = { nav.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, "رجوع", tint = RoyalGold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy)
            )
        },
        content = content
    )
}

@Composable
private fun CasesScreen(nav: NavHostController, cases: List<LegalCase>) {
    ScreenScaffold("القضايا", "إدارة ومتابعة ملفات القضايا", nav) { p ->
        LegalList(p, cases) { c ->
            Card(
                modifier = Modifier.fillMaxWidth().clickable { nav.navigate(Routes.caseDetail(c.id)) },
                colors = CardDefaults.cardColors(containerColor = Gunmetal)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(c.title, color = Platinum, fontWeight = FontWeight.Bold)
                        StatusPill(statusText(c.status), if (c.status == CaseStatus.ACTIVE) Success else RoyalGold)
                    }
                    Text("${c.court} • ${c.caseNumber}", color = Silver, fontSize = 12.sp)
                    Text("الموكل: ${c.clientName}", color = Platinum, fontSize = 13.sp)
                    Text("الجلسة القادمة: ${c.nextSession}", color = ElectricBlue, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun CaseDetailScreen(nav: NavHostController, legalCase: LegalCase?) {
    ScreenScaffold("تفاصيل القضية", legalCase?.caseNumber, nav) { p ->
        Column(Modifier.fillMaxSize().padding(p).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            if (legalCase == null) {
                EmptyState("لم يتم العثور على القضية")
            } else {
                InfoCard("عنوان القضية", legalCase.title, Icons.Outlined.Gavel)
                InfoCard("المحكمة", legalCase.court, Icons.Outlined.AccountBalance)
                InfoCard("الموكل", legalCase.clientName, Icons.Outlined.Person)
                InfoCard("الجلسة القادمة", legalCase.nextSession, Icons.Outlined.CalendarMonth)
                Text("المستندات والملاحظات الخاصة بالقضية ستُربط بهذه الصفحة في طبقة التخزين الدائم.", color = Silver, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun ClientsScreen(nav: NavHostController, clients: List<Client>) {
    ScreenScaffold("العملاء", "ملفات الموكلين", nav) { p ->
        LegalList(p, clients) { c ->
            InfoCard(c.name, "${c.phone}\n${c.notes}", Icons.Outlined.Person)
        }
    }
}

@Composable
private fun HearingsScreen(nav: NavHostController, hearings: List<Hearing>) {
    ScreenScaffold("الجلسات", "الأجندة القانونية", nav) { p ->
        LegalList(p, hearings) { h ->
            InfoCard(h.caseTitle, "${h.date} • ${h.time}\n${h.court}\n${h.notes}", Icons.Outlined.CalendarMonth)
        }
    }
}

@Composable
private fun LawsScreen(nav: NavHostController, laws: List<LawBook>) {
    ScreenScaffold("مكتبة القانون", "مصادر مرجعية محلية", nav) { p ->
        LegalList(p, laws) { law ->
            InfoCard(law.title, "${law.category}\n${law.sourceNote}", Icons.Outlined.MenuBook)
        }
    }
}

@Composable
private fun DocumentsScreen(nav: NavHostController, docs: List<LegalDocument>) {
    ScreenScaffold("المذكرات والعقود", "المستندات القانونية", nav) { p ->
        LegalList(p, docs) { d ->
            InfoCard(d.title, "${d.type} • آخر تحديث ${d.updatedAt}${d.caseTitle?.let { "\nمرتبط بـ: $it" } ?: ""}", Icons.Outlined.Description)
        }
    }
}

@Composable
private fun SearchScreen(nav: NavHostController, state: ShurayhUiState, onQuery: (String) -> Unit) {
    val q = state.searchQuery.trim()
    val caseResults = state.cases.filter { q.isBlank() || listOf(it.title, it.clientName, it.caseNumber, it.court).any { v -> v.contains(q, true) } }
    val clientResults = state.clients.filter { q.isNotBlank() && listOf(it.name, it.phone).any { v -> v.contains(q, true) } }
    val lawResults = state.lawBooks.filter { q.isNotBlank() && listOf(it.title, it.category).any { v -> v.contains(q, true) } }

    ScreenScaffold("البحث الذكي", "بحث محلي موحد", nav) { p ->
        LazyColumn(Modifier.fillMaxSize().padding(p), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = onQuery,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("ابحث بالاسم أو رقم القضية أو المحكمة") },
                    leadingIcon = { Icon(Icons.Outlined.Search, null) },
                    singleLine = true
                )
            }
            if (q.isBlank()) item { Text("ابدأ الكتابة للبحث في القضايا والعملاء والقوانين.", color = Silver) }
            if (q.isNotBlank()) {
                item { SectionTitle("القضايا (${caseResults.size})") }
                items(caseResults) { c -> InfoCard(c.title, "${c.caseNumber} • ${c.clientName}", Icons.Outlined.Gavel) }
                item { SectionTitle("العملاء (${clientResults.size})") }
                items(clientResults) { c -> InfoCard(c.name, c.phone, Icons.Outlined.Person) }
                item { SectionTitle("القوانين (${lawResults.size})") }
                items(lawResults) { l -> InfoCard(l.title, l.category, Icons.Outlined.MenuBook) }
            }
        }
    }
}

@Composable
private fun OfficeScreen(nav: NavHostController, state: ShurayhUiState) {
    ScreenScaffold("ملف المكتب", "مؤشرات تشغيلية", nav) { p ->
        LazyColumn(Modifier.fillMaxSize().padding(p), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { InfoCard("القضايا النشطة", state.cases.count { it.status == CaseStatus.ACTIVE }.toString(), Icons.Outlined.Gavel) }
            item { InfoCard("إجمالي العملاء", state.clients.size.toString(), Icons.Outlined.PeopleAlt) }
            item { InfoCard("الجلسات المسجلة", state.hearings.size.toString(), Icons.Outlined.CalendarMonth) }
            item { InfoCard("المستندات", state.documents.size.toString(), Icons.Outlined.Description) }
        }
    }
}

@Composable
private fun SimpleInfoScreen(nav: NavHostController, title: String, rows: List<String>) {
    ScreenScaffold(title, null, nav) { p ->
        LazyColumn(Modifier.fillMaxSize().padding(p), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(rows) { InfoCard(it, "سيتم ربط البيانات المعتمدة ومعلومات الاتصال في مرحلة البيانات المرجعية.", Icons.Outlined.AccountBalance) }
        }
    }
}

@Composable
private fun AboutScreen(nav: NavHostController) {
    ScreenScaffold("حول SHURAYH", "الإصدار 0.2.0", nav) { p ->
        Column(Modifier.fillMaxSize().padding(p).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("SHURAYH", color = RoyalGold, fontSize = 30.sp, fontWeight = FontWeight.Black)
            Text("منصة مساعدة قانونية لتنظيم عمل المحامي. المخرجات الذكية والمراجع داخل التطبيق أدوات مساعدة ولا تغني عن المراجعة القانونية المهنية والتحقق من أحدث النصوص الرسمية.", color = Platinum, lineHeight = 23.sp)
            Divider(color = Gunmetal)
            Text("NEXVARY", color = ElectricBlue, fontWeight = FontWeight.Bold)
            Text("https://nexvary.com/\ninfo@nexvary.com", color = Silver)
        }
    }
}

@Composable
private fun <T> LegalList(padding: PaddingValues, data: List<T>, row: @Composable (T) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding).background(Navy),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (data.isEmpty()) item { EmptyState("لا توجد بيانات حتى الآن") }
        else items(data) { row(it) }
    }
}

@Composable
private fun ModuleCard(module: ModuleItem, modifier: Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.height(158.dp).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Gunmetal),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(Modifier.fillMaxSize().padding(15.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(module.icon, module.title, tint = RoyalGold)
            Text(module.title, color = Platinum, fontWeight = FontWeight.Bold)
            Text(module.subtitle, color = Silver, fontSize = 12.sp, lineHeight = 16.sp)
        }
    }
}

@Composable
private fun InfoCard(title: String, body: String, icon: ImageVector) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Gunmetal), shape = RoundedCornerShape(16.dp)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
            Icon(icon, null, tint = RoyalGold)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(title, color = Platinum, fontWeight = FontWeight.Bold)
                Text(body, color = Silver, fontSize = 12.sp, lineHeight = 18.sp)
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start, color = Platinum, fontSize = 18.sp, fontWeight = FontWeight.Bold)
}

@Composable
private fun StatusPill(text: String, color: Color) {
    Box(Modifier.background(color.copy(alpha = .16f), RoundedCornerShape(50)).padding(horizontal = 9.dp, vertical = 5.dp)) {
        Text(text, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun EmptyState(text: String) {
    Box(Modifier.fillMaxWidth().padding(30.dp), contentAlignment = Alignment.Center) { Text(text, color = Silver) }
}

private fun statusText(status: CaseStatus) = when (status) {
    CaseStatus.ACTIVE -> "نشطة"
    CaseStatus.PENDING -> "قيد المتابعة"
    CaseStatus.CLOSED -> "مغلقة"
}
