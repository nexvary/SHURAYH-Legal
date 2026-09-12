package com.nexvary.shurayh

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nexvary.shurayh.core.*

private val AppBlack = Color(0xFF070A0D)
private val CardBlack = Color(0xFF0A0D10)
private val Silver = Color(0xFFB7BABE)
private val Platinum = Color(0xFFE1E2E4)
private val RoyalGold = Color(0xFFC9A44A)
private val MutedGold = Color(0xFF9A7630)
private val Danger = RoyalGold

private object Routes {
    const val HOME = "home"
    const val CASES = "cases"
    const val CLIENTS = "clients"
    const val HEARINGS = "hearings"
    const val LAWS = "laws"
    const val DOCUMENTS = "documents"
    const val SEARCH = "search"
    const val OFFICE = "office"
    const val COURTS = "courts"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
    const val NOTIFICATIONS = "notifications"
    const val MENU = "menu"
    const val CASE_DETAIL = "case/{caseId}"
    fun caseDetail(id: String) = "case/$id"
}

data class ModuleItem(val title: String, val subtitle: String, val icon: ImageVector, val route: String)
data class BottomItem(val title: String, val icon: ImageVector, val route: String)

@Composable
fun ShurayhRoot(vm: ShurayhViewModel) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val currentRoute = entry?.destination?.route
    val bottomRoutes = setOf(Routes.HOME, Routes.MENU, Routes.NOTIFICATIONS, Routes.ABOUT, Routes.SETTINGS)

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = RoyalGold,
            secondary = Silver,
            background = AppBlack,
            surface = CardBlack,
            surfaceVariant = CardBlack,
            onBackground = Platinum,
            onSurface = Platinum,
            outline = Silver
        )
    ) {
        Scaffold(
            containerColor = AppBlack,
            bottomBar = {
                if (currentRoute in bottomRoutes) ShurayhBottomBar(nav, currentRoute)
            }
        ) { shellPadding ->
            NavHost(
                navController = nav,
                startDestination = Routes.HOME,
                modifier = Modifier.padding(shellPadding)
            ) {
                composable(Routes.HOME) { HomeScreen(nav, state) }
                composable(Routes.CASES) { CasesScreen(nav, state, vm) }
                composable(Routes.CLIENTS) { ClientsScreen(nav, state, vm) }
                composable(Routes.HEARINGS) { HearingsScreen(nav, state, vm) }
                composable(Routes.LAWS) { LawsScreen(nav, state.lawBooks) }
                composable(Routes.DOCUMENTS) { DocumentsScreen(nav, state, vm) }
                composable(Routes.SEARCH) { SearchScreen(nav, state, vm::setSearchQuery) }
                composable(Routes.OFFICE) { OfficeScreen(nav, state) }
                composable(Routes.COURTS) { CourtsScreen(nav) }
                composable(Routes.SETTINGS) { SettingsScreen(nav, vm) }
                composable(Routes.ABOUT) { AboutScreen(nav) }
                composable(Routes.NOTIFICATIONS) { NotificationsScreen(nav, state) }
                composable(Routes.MENU) { MenuScreen(nav) }
                composable(
                    route = Routes.CASE_DETAIL,
                    arguments = listOf(navArgument("caseId") { type = NavType.StringType })
                ) { detailEntry ->
                    val id = detailEntry.arguments?.getString("caseId")
                    CaseDetailScreen(nav, state, vm, id)
                }
            }
        }
    }
}

@Composable
private fun ShurayhBottomBar(nav: NavHostController, currentRoute: String?) {
    val items = listOf(
        BottomItem("القائمة", Icons.Outlined.Menu, Routes.MENU),
        BottomItem("الإشعارات", Icons.Outlined.NotificationsNone, Routes.NOTIFICATIONS),
        BottomItem("الرئيسية", Icons.Outlined.Home, Routes.HOME),
        BottomItem("عنا", Icons.Outlined.Info, Routes.ABOUT),
        BottomItem("الضبط", Icons.Outlined.Settings, Routes.SETTINGS)
    )
    Surface(
        color = CardBlack,
        border = BorderStroke(1.dp, Silver.copy(alpha = .75f)),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        NavigationBar(containerColor = Color.Transparent, tonalElevation = 0.dp) {
            items.forEach { item ->
                val selected = currentRoute == item.route
                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        if (!selected) nav.navigate(item.route) {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(Routes.HOME) { saveState = true }
                        }
                    },
                    icon = {
                        Icon(
                            item.icon,
                            contentDescription = item.title,
                            tint = if (selected) RoyalGold else MutedGold
                        )
                    },
                    label = {
                        Text(
                            item.title,
                            color = if (selected) RoyalGold else Platinum,
                            fontSize = 10.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = RoyalGold,
                        selectedTextColor = RoyalGold,
                        unselectedIconColor = MutedGold,
                        unselectedTextColor = Platinum,
                        indicatorColor = RoyalGold.copy(alpha = .10f)
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Page(
    title: String,
    subtitle: String? = null,
    nav: NavHostController? = null,
    fab: (@Composable () -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    if (nav != null) BackHandler { nav.popBackStack() }
    Scaffold(
        containerColor = AppBlack,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(title, color = Platinum, fontWeight = FontWeight.Black)
                        subtitle?.let { Text(it, color = Silver, fontSize = 11.sp) }
                    }
                },
                navigationIcon = {
                    if (nav != null) IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, "رجوع", tint = RoyalGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBlack)
            )
        },
        floatingActionButton = { fab?.invoke() },
        content = content
    )
}

@Composable
private fun HomeScreen(nav: NavHostController, state: ShurayhUiState) {
    val modules = listOf(
        ModuleItem("القضايا", "إدارة ملفات القضايا", Icons.Outlined.Gavel, Routes.CASES),
        ModuleItem("العملاء", "بيانات الموكلين", Icons.Outlined.PeopleAlt, Routes.CLIENTS),
        ModuleItem("الجلسات", "الأجندة والتنبيهات", Icons.Outlined.CalendarMonth, Routes.HEARINGS),
        ModuleItem("مكتبة القانون", "مصادر قانونية مرجعية", Icons.Outlined.MenuBook, Routes.LAWS),
        ModuleItem("المستندات", "مذكرات وعقود ونماذج", Icons.Outlined.Description, Routes.DOCUMENTS),
        ModuleItem("البحث", "بحث موحد محلي", Icons.Outlined.Search, Routes.SEARCH),
        ModuleItem("ملف المكتب", "مؤشرات العمل", Icons.Outlined.FolderShared, Routes.OFFICE),
        ModuleItem("المحاكم", "دليل الجهات", Icons.Outlined.AccountBalance, Routes.COURTS)
    )
    LazyColumn(
        Modifier.fillMaxSize().background(AppBlack),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text("شُرَيْح", color = RoyalGold, fontSize = 30.sp, fontWeight = FontWeight.Black)
                Text("SHURAYH", color = RoyalGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("المساعد القانوني الذكي للمحامي", color = Silver, fontSize = 12.sp)
            }
        }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBlack),
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.dp, Silver.copy(alpha = .90f))
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Balance, null, tint = RoyalGold, modifier = Modifier.size(34.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("مساحة العمل القانونية", color = RoyalGold, fontSize = 22.sp, fontWeight = FontWeight.Black)
                    }
                    Text(
                        "${state.cases.size} قضايا  •  ${state.clients.size} عملاء  •  ${state.hearings.size} جلسات  •  ${state.documents.size} مستندات",
                        color = Platinum
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Security, null, tint = RoyalGold, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("البيانات تحفظ محليًا على الجهاز", color = Silver, fontSize = 12.sp)
                    }
                }
            }
        }
        items(modules.chunked(2)) { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { item ->
                    ModuleCard(item, Modifier.weight(1f)) { nav.navigate(item.route) }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
        item { Spacer(Modifier.height(4.dp)) }
    }
}

@Composable
private fun ModuleCard(item: ModuleItem, modifier: Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.height(136.dp).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CardBlack),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Silver.copy(alpha = .82f))
    ) {
        Column(
            Modifier.fillMaxSize().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.End
        ) {
            Icon(item.icon, item.title, tint = RoyalGold, modifier = Modifier.size(30.dp))
            Text(item.title, color = Platinum, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            Text(item.subtitle, color = Silver, fontSize = 11.sp, lineHeight = 16.sp)
        }
    }
}

@Composable
private fun MenuScreen(nav: NavHostController) {
    val modules = listOf(
        ModuleItem("القضايا", "إدارة ملفات القضايا", Icons.Outlined.Gavel, Routes.CASES),
        ModuleItem("العملاء", "بيانات الموكلين", Icons.Outlined.PeopleAlt, Routes.CLIENTS),
        ModuleItem("الجلسات", "الأجندة والتنبيهات", Icons.Outlined.CalendarMonth, Routes.HEARINGS),
        ModuleItem("مكتبة القانون", "مصادر قانونية مرجعية", Icons.Outlined.MenuBook, Routes.LAWS),
        ModuleItem("المستندات", "مذكرات وعقود ونماذج", Icons.Outlined.Description, Routes.DOCUMENTS),
        ModuleItem("البحث", "بحث موحد محلي", Icons.Outlined.Search, Routes.SEARCH),
        ModuleItem("ملف المكتب", "مؤشرات العمل", Icons.Outlined.FolderShared, Routes.OFFICE),
        ModuleItem("المحاكم", "دليل الجهات", Icons.Outlined.AccountBalance, Routes.COURTS)
    )
    Page("القائمة", "كل أقسام SHURAYH") { p ->
        LazyColumn(
            Modifier.fillMaxSize().padding(p),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(modules) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { nav.navigate(item.route) },
                    colors = CardDefaults.cardColors(containerColor = CardBlack),
                    border = BorderStroke(1.dp, Silver.copy(alpha = .72f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(item.icon, null, tint = RoyalGold)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(item.title, color = Platinum, fontWeight = FontWeight.Bold)
                            Text(item.subtitle, color = Silver, fontSize = 11.sp)
                        }
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, null, tint = Silver)
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationsScreen(nav: NavHostController, state: ShurayhUiState) {
    Page("الإشعارات", "الجلسات والتنبيهات") { p ->
        LazyColumn(
            Modifier.fillMaxSize().padding(p),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (state.hearings.isEmpty()) item { EmptyCard("لا توجد إشعارات جلسات حاليًا") }
            items(state.hearings, key = { it.id }) { h ->
                InfoCard(h.caseTitle, "${h.date} • ${h.time} • ${h.court}", Icons.Outlined.NotificationsActive)
            }
        }
    }
}

@Composable
private fun CasesScreen(nav: NavHostController, state: ShurayhUiState, vm: ShurayhViewModel) {
    var showAdd by remember { mutableStateOf(false) }
    Page("القضايا", "ملفات القضايا وحالتها", nav, fab = {
        FloatingActionButton(onClick = { showAdd = true }, containerColor = RoyalGold) { Icon(Icons.Outlined.Add, "إضافة قضية", tint = AppBlack) }
    }) { p ->
        LazyColumn(Modifier.fillMaxSize().padding(p), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (state.cases.isEmpty()) item { EmptyCard("لا توجد قضايا بعد") }
            items(state.cases, key = { it.id }) { c ->
                Card(
                    Modifier.fillMaxWidth().clickable { nav.navigate(Routes.caseDetail(c.id)) },
                    colors = CardDefaults.cardColors(containerColor = CardBlack),
                    border = BorderStroke(1.dp, Silver.copy(alpha = .65f))
                ) {
                    Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(c.title, color = Platinum, fontWeight = FontWeight.Bold)
                            StatusChip(c.status)
                        }
                        Text("${c.caseNumber} • ${c.court}", color = Silver, fontSize = 12.sp)
                        Text("الموكل: ${c.clientName}", color = Platinum, fontSize = 13.sp)
                        Text("الجلسة القادمة: ${c.nextSession}", color = RoyalGold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
    if (showAdd) AddCaseDialog(state.clients.map { it.name }, onDismiss = { showAdd = false }) { title, court, number, client, session ->
        if (vm.addCase(title, court, number, client, session)) showAdd = false
    }
}

@Composable
private fun CaseDetailScreen(nav: NavHostController, state: ShurayhUiState, vm: ShurayhViewModel, caseId: String?) {
    val legalCase = state.cases.firstOrNull { it.id == caseId }
    var confirmDelete by remember { mutableStateOf(false) }
    Page("تفاصيل القضية", legalCase?.caseNumber, nav) { p ->
        LazyColumn(Modifier.fillMaxSize().padding(p), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (legalCase == null) item { EmptyCard("تعذر العثور على القضية") } else {
                item { InfoCard("عنوان القضية", legalCase.title, Icons.Outlined.Gavel) }
                item { InfoCard("المحكمة", legalCase.court, Icons.Outlined.AccountBalance) }
                item { InfoCard("الموكل", legalCase.clientName, Icons.Outlined.Person) }
                item { InfoCard("الجلسة القادمة", legalCase.nextSession, Icons.Outlined.CalendarMonth) }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { vm.cycleCaseStatus(legalCase.id) }, modifier = Modifier.weight(1f)) { Text("تغيير الحالة") }
                        OutlinedButton(onClick = { confirmDelete = true }, modifier = Modifier.weight(1f)) { Text("حذف", color = Danger) }
                    }
                }
                val hearings = state.hearings.filter { it.caseId == legalCase.id }
                val docs = state.documents.filter { it.caseTitle == legalCase.title }
                item { SectionTitle("الجلسات المرتبطة (${hearings.size})") }
                items(hearings) { h -> InfoCard(h.date, "${h.time} • ${h.court}\n${h.notes}", Icons.Outlined.CalendarMonth) }
                item { SectionTitle("المستندات المرتبطة (${docs.size})") }
                items(docs) { d -> InfoCard(d.title, "${d.type} • ${d.updatedAt}", Icons.Outlined.Description) }
            }
        }
    }
    if (confirmDelete && legalCase != null) ConfirmDeleteDialog("حذف القضية؟", "سيتم حذف الجلسات والمستندات المرتبطة بهذه القضية.", onDismiss = { confirmDelete = false }) {
        vm.deleteCase(legalCase.id); confirmDelete = false; nav.popBackStack()
    }
}

@Composable
private fun ClientsScreen(nav: NavHostController, state: ShurayhUiState, vm: ShurayhViewModel) {
    var add by remember { mutableStateOf(false) }
    var deleteId by remember { mutableStateOf<String?>(null) }
    Page("العملاء", "ملفات الموكلين", nav, fab = {
        FloatingActionButton(onClick = { add = true }, containerColor = RoyalGold) { Icon(Icons.Outlined.PersonAdd, "إضافة عميل", tint = AppBlack) }
    }) { p ->
        LazyColumn(Modifier.fillMaxSize().padding(p), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (state.clients.isEmpty()) item { EmptyCard("لا يوجد عملاء") }
            items(state.clients, key = { it.id }) { c ->
                LuxuryCard {
                    Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Person, null, tint = RoyalGold)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(c.name, color = Platinum, fontWeight = FontWeight.Bold)
                            Text(c.phone, color = RoyalGold, fontSize = 12.sp)
                            if (c.notes.isNotBlank()) Text(c.notes, color = Silver, fontSize = 11.sp)
                        }
                        IconButton(onClick = { deleteId = c.id }) { Icon(Icons.Outlined.Delete, "حذف", tint = MutedGold) }
                    }
                }
            }
        }
    }
    if (add) AddClientDialog({ add = false }) { name, phone, nationalId, notes -> if (vm.addClient(name, phone, notes, nationalId)) add = false }
    deleteId?.let { id -> ConfirmDeleteDialog("حذف العميل؟", "لن يتم حذف القضايا تلقائيًا.", { deleteId = null }) { vm.deleteClient(id); deleteId = null } }
}

@Composable
private fun HearingsScreen(nav: NavHostController, state: ShurayhUiState, vm: ShurayhViewModel) {
    var add by remember { mutableStateOf(false) }
    Page("الجلسات", "أجندة مرتبطة بملفات القضايا", nav, fab = {
        FloatingActionButton(onClick = { add = true }, containerColor = RoyalGold) { Icon(Icons.Outlined.AddAlert, "إضافة جلسة", tint = AppBlack) }
    }) { p ->
        LazyColumn(Modifier.fillMaxSize().padding(p), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (state.hearings.isEmpty()) item { EmptyCard("لا توجد جلسات مسجلة") }
            items(state.hearings, key = { it.id }) { h ->
                LuxuryCard {
                    Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.CalendarMonth, null, tint = RoyalGold)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(h.caseTitle, color = Platinum, fontWeight = FontWeight.Bold)
                            Text("${h.date} • ${h.time}", color = RoyalGold, fontSize = 12.sp)
                            Text(h.court, color = Silver, fontSize = 11.sp)
                        }
                        IconButton(onClick = { vm.deleteHearing(h.id) }) { Icon(Icons.Outlined.Delete, "حذف", tint = MutedGold) }
                    }
                }
            }
        }
    }
    if (add) AddHearingDialog(state.cases, { add = false }) { caseId, court, date, time, notes -> if (vm.addHearing(caseId, court, date, time, notes)) add = false }
}

@Composable
private fun DocumentsScreen(nav: NavHostController, state: ShurayhUiState, vm: ShurayhViewModel) {
    var add by remember { mutableStateOf(false) }
    Page("المستندات", "المذكرات والعقود والنماذج", nav, fab = {
        FloatingActionButton(onClick = { add = true }, containerColor = RoyalGold) { Icon(Icons.Outlined.NoteAdd, "إضافة مستند", tint = AppBlack) }
    }) { p ->
        LazyColumn(Modifier.fillMaxSize().padding(p), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (state.documents.isEmpty()) item { EmptyCard("لا توجد مستندات") }
            items(state.documents, key = { it.id }) { d ->
                LuxuryCard {
                    Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Description, null, tint = RoyalGold)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(d.title, color = Platinum, fontWeight = FontWeight.Bold)
                            Text("${d.type} • ${d.updatedAt}", color = Silver, fontSize = 11.sp)
                            d.caseTitle?.let { Text("القضية: $it", color = RoyalGold, fontSize = 11.sp) }
                        }
                        IconButton(onClick = { vm.deleteDocument(d.id) }) { Icon(Icons.Outlined.Delete, "حذف", tint = MutedGold) }
                    }
                }
            }
        }
    }
    if (add) AddDocumentDialog(state.cases.map { it.title }, { add = false }) { title, type, caseTitle, date -> if (vm.addDocument(title, type, caseTitle, date)) add = false }
}

@Composable
private fun LawsScreen(nav: NavHostController, laws: List<LawBook>) {
    Page("مكتبة القانون", "محتوى مرجعي يحتاج تحققًا من آخر تعديل", nav) { p ->
        LazyColumn(Modifier.fillMaxSize().padding(p), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { WarningCard("تنبيه مهني", "المكتبة الحالية مرجعية تجريبية. يجب التحقق من النص الرسمي وآخر تعديل قبل الاستناد إليه في مذكرة أو إجراء.") }
            items(laws) { l -> InfoCard(l.title, "${l.category}\n${l.sourceNote}", Icons.Outlined.MenuBook) }
        }
    }
}

@Composable
private fun SearchScreen(nav: NavHostController, state: ShurayhUiState, onQuery: (String) -> Unit) {
    val q = state.searchQuery.trim()
    val cases = state.cases.filter { listOf(it.title, it.caseNumber, it.clientName, it.court).any { v -> v.contains(q, true) } }
    val clients = state.clients.filter { listOf(it.name, it.phone, it.notes).any { v -> v.contains(q, true) } }
    val laws = state.lawBooks.filter { listOf(it.title, it.category).any { v -> v.contains(q, true) } }
    val docs = state.documents.filter { listOf(it.title, it.type, it.caseTitle.orEmpty()).any { v -> v.contains(q, true) } }
    Page("البحث", "بحث موحد داخل بيانات SHURAYH", nav) { p ->
        LazyColumn(Modifier.fillMaxSize().padding(p), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = onQuery,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("اسم، رقم قضية، محكمة، مستند...") },
                    leadingIcon = { Icon(Icons.Outlined.Search, null, tint = RoyalGold) }
                )
            }
            if (q.isBlank()) item { EmptyCard("اكتب كلمة للبحث") } else {
                item { SectionTitle("القضايا (${cases.size})") }
                items(cases) { InfoCard(it.title, "${it.caseNumber} • ${it.clientName}", Icons.Outlined.Gavel) }
                item { SectionTitle("العملاء (${clients.size})") }
                items(clients) { InfoCard(it.name, it.phone, Icons.Outlined.Person) }
                item { SectionTitle("المستندات (${docs.size})") }
                items(docs) { InfoCard(it.title, it.type, Icons.Outlined.Description) }
                item { SectionTitle("القوانين (${laws.size})") }
                items(laws) { InfoCard(it.title, it.category, Icons.Outlined.MenuBook) }
            }
        }
    }
}

@Composable
private fun OfficeScreen(nav: NavHostController, state: ShurayhUiState) {
    Page("ملف المكتب", "مؤشرات تشغيلية محلية", nav) { p ->
        LazyColumn(Modifier.fillMaxSize().padding(p), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { MetricCard("القضايا النشطة", state.cases.count { it.status == CaseStatus.ACTIVE }, Icons.Outlined.Gavel) }
            item { MetricCard("القضايا المغلقة", state.cases.count { it.status == CaseStatus.CLOSED }, Icons.Outlined.DoneAll) }
            item { MetricCard("العملاء", state.clients.size, Icons.Outlined.PeopleAlt) }
            item { MetricCard("الجلسات", state.hearings.size, Icons.Outlined.CalendarMonth) }
            item { MetricCard("المستندات", state.documents.size, Icons.Outlined.Description) }
        }
    }
}

@Composable
private fun CourtsScreen(nav: NavHostController) {
    val entries = listOf(
        "المحاكم الابتدائية", "محاكم الاستئناف", "محاكم الأسرة", "المحاكم الاقتصادية",
        "المحاكم الجنائية", "مجلس الدولة", "مكاتب الشهر العقاري", "النيابات"
    )
    Page("المحاكم والجهات", "دليل تنظيمي أولي", nav) { p ->
        LazyColumn(Modifier.fillMaxSize().padding(p), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(entries) { InfoCard(it, "سيتم ربط العناوين والدوائر وبيانات الاتصال بمصدر موثق.", Icons.Outlined.AccountBalance) }
        }
    }
}

@Composable
private fun SettingsScreen(nav: NavHostController, vm: ShurayhViewModel) {
    var reset by remember { mutableStateOf(false) }
    Page("الضبط", "الخصوصية والبيانات") { p ->
        LazyColumn(Modifier.fillMaxSize().padding(p), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { InfoCard("التخزين", "محلي على الجهاز • لا يتم رفع ملفات العملاء تلقائيًا", Icons.Outlined.Storage) }
            item { InfoCard("الاتجاه", "العربية RTL مفعلة افتراضيًا", Icons.Outlined.FormatTextdirectionRToL) }
            item { InfoCard("Android", "متوافق مع Android 15 / API 35", Icons.Outlined.Android) }
            item { OutlinedButton(onClick = { reset = true }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Outlined.Restore, null, tint = RoyalGold); Spacer(Modifier.width(8.dp)); Text("استعادة البيانات التجريبية") } }
        }
    }
    if (reset) AlertDialog(
        onDismissRequest = { reset = false },
        title = { Text("استعادة البيانات التجريبية؟") },
        text = { Text("سيتم استبدال البيانات المحلية الحالية بالبيانات التجريبية.") },
        confirmButton = { TextButton(onClick = { vm.resetDemoData(); reset = false }) { Text("استعادة") } },
        dismissButton = { TextButton(onClick = { reset = false }) { Text("إلغاء") } }
    )
}

@Composable
private fun AboutScreen(nav: NavHostController) {
    Page("عنا", "SHURAYH • NEXVARY Legal Technology") { p ->
        LazyColumn(Modifier.fillMaxSize().padding(p), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { InfoCard("SHURAYH", "منصة عمل قانونية للمحامي: قضايا، عملاء، جلسات، مستندات وبحث قانوني محلي.", Icons.Outlined.Balance) }
            item { InfoCard("مبدأ الخصوصية", "Local-first مع تقليل الصلاحيات وعدم إرسال بيانات العملاء تلقائيًا.", Icons.Outlined.Security) }
            item { WarningCard("تنبيه قانوني", "أي تلخيص أو صياغة أو مادة مرجعية داخل التطبيق أداة مساعدة للمحامي وليست بديلًا عن المراجعة المهنية أو النص الرسمي.") }
        }
    }
}

@Composable
private fun LuxuryCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBlack),
        border = BorderStroke(1.dp, Silver.copy(alpha = .65f)),
        shape = RoundedCornerShape(16.dp),
        content = content
    )
}

@Composable
private fun MetricCard(title: String, value: Int, icon: ImageVector) {
    LuxuryCard {
        Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = RoyalGold)
            Spacer(Modifier.width(12.dp))
            Text(title, modifier = Modifier.weight(1f), color = Platinum, fontWeight = FontWeight.Bold)
            Text(value.toString(), color = RoyalGold, fontSize = 24.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun InfoCard(title: String, body: String, icon: ImageVector) {
    LuxuryCard {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.Top) {
            Icon(icon, null, tint = RoyalGold)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = Platinum, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(body, color = Silver, fontSize = 12.sp, lineHeight = 18.sp)
            }
        }
    }
}

@Composable private fun SectionTitle(text: String) { Text(text, color = RoyalGold, fontWeight = FontWeight.Bold, fontSize = 16.sp) }

@Composable
private fun EmptyCard(text: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBlack),
        border = BorderStroke(1.dp, Silver.copy(alpha = .45f))
    ) { Text(text, Modifier.fillMaxWidth().padding(18.dp), color = Silver) }
}

@Composable
private fun WarningCard(title: String, body: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBlack),
        border = BorderStroke(1.dp, RoyalGold.copy(alpha = .65f))
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(title, color = RoyalGold, fontWeight = FontWeight.Bold)
            Text(body, color = Platinum, fontSize = 12.sp, lineHeight = 18.sp)
        }
    }
}

@Composable
private fun StatusChip(status: CaseStatus) {
    val text = when (status) {
        CaseStatus.ACTIVE -> "نشطة"
        CaseStatus.PENDING -> "معلقة"
        CaseStatus.CLOSED -> "مغلقة"
    }
    val color = if (status == CaseStatus.CLOSED) Silver else RoyalGold
    Surface(
        color = CardBlack,
        border = BorderStroke(1.dp, color.copy(alpha = .7f)),
        shape = RoundedCornerShape(50)
    ) {
        Text(text, color = color, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun AddCaseDialog(clients: List<String>, onDismiss: () -> Unit, onSave: (String, String, String, String, String) -> Unit) {
    var title by remember { mutableStateOf("") }; var court by remember { mutableStateOf("") }; var number by remember { mutableStateOf("") }; var client by remember { mutableStateOf(clients.firstOrNull().orEmpty()) }; var session by remember { mutableStateOf("") }
    FormDialog("إضافة قضية", onDismiss, { onSave(title, court, number, client, session) }) {
        Field("عنوان القضية", title) { title = it }; Field("المحكمة", court) { court = it }; Field("رقم القضية", number) { number = it }; Field("اسم الموكل", client) { client = it }; Field("الجلسة القادمة", session) { session = it }
    }
}

@Composable
private fun AddClientDialog(onDismiss: () -> Unit, onSave: (String, String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }; var phone by remember { mutableStateOf("") }; var nationalId by remember { mutableStateOf("") }; var notes by remember { mutableStateOf("") }
    FormDialog("إضافة عميل", onDismiss, { onSave(name, phone, nationalId, notes) }) {
        Field("الاسم", name) { name = it }; Field("الهاتف", phone) { phone = it }; Field("الرقم القومي (اختياري)", nationalId) { nationalId = it }; Field("ملاحظات", notes) { notes = it }
    }
}

@Composable
private fun AddHearingDialog(cases: List<LegalCase>, onDismiss: () -> Unit, onSave: (String, String, String, String, String) -> Unit) {
    var caseId by remember { mutableStateOf(cases.firstOrNull()?.id.orEmpty()) }; var court by remember { mutableStateOf(cases.firstOrNull()?.court.orEmpty()) }; var date by remember { mutableStateOf("") }; var time by remember { mutableStateOf("") }; var notes by remember { mutableStateOf("") }
    FormDialog("إضافة جلسة", onDismiss, { onSave(caseId, court, date, time, notes) }) {
        Text("القضية: ${cases.firstOrNull { it.id == caseId }?.title ?: "لا توجد قضية"}", color = Platinum)
        if (cases.size > 1) TextButton(onClick = { val i = cases.indexOfFirst { it.id == caseId }; val next = cases[(i + 1).mod(cases.size)]; caseId = next.id; court = next.court }) { Text("اختيار القضية التالية") }
        Field("المحكمة", court) { court = it }; Field("التاريخ", date) { date = it }; Field("الوقت", time) { time = it }; Field("ملاحظات", notes) { notes = it }
    }
}

@Composable
private fun AddDocumentDialog(cases: List<String>, onDismiss: () -> Unit, onSave: (String, String, String?, String) -> Unit) {
    var title by remember { mutableStateOf("") }; var type by remember { mutableStateOf("مذكرة") }; var caseTitle by remember { mutableStateOf(cases.firstOrNull().orEmpty()) }; var date by remember { mutableStateOf("اليوم") }
    FormDialog("إضافة مستند", onDismiss, { onSave(title, type, caseTitle.ifBlank { null }, date) }) {
        Field("اسم المستند", title) { title = it }; Field("النوع", type) { type = it }; Field("القضية المرتبطة (اختياري)", caseTitle) { caseTitle = it }; Field("تاريخ التحديث", date) { date = it }
    }
}

@Composable
private fun FormDialog(title: String, onDismiss: () -> Unit, onSave: () -> Unit, fields: @Composable ColumnScope.() -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp), content = fields) },
        confirmButton = { TextButton(onClick = onSave) { Text("حفظ") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@Composable
private fun Field(label: String, value: String, onValue: (String) -> Unit) {
    OutlinedTextField(value = value, onValueChange = onValue, label = { Text(label) }, singleLine = true, modifier = Modifier.fillMaxWidth())
}

@Composable
private fun ConfirmDeleteDialog(title: String, text: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) }, text = { Text(text) },
        confirmButton = { TextButton(onClick = onConfirm) { Text("حذف", color = Danger) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}
