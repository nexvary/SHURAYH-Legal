package com.nexvary.shurayh

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.nexvary.shurayh.core.LawBook

private val RecoveryBlack = Color(0xFF070A0D)
private val RecoveryCard = Color(0xFF0A0D10)
private val RecoverySilver = Color(0xFFB7BABE)
private val RecoveryPlatinum = Color(0xFFE1E2E4)
private val RecoveryGold = Color(0xFFC9A44A)
private val RecoveryGreen = Color(0xFF00C98D)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecoveryScaffold(
    nav: NavHostController,
    title: String,
    subtitle: String,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        containerColor = RecoveryBlack,
        topBar = {
            TopAppBar(
                title = {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                        Text(title, color = RecoveryPlatinum, fontWeight = FontWeight.Black)
                        Text(subtitle, color = RecoverySilver, fontSize = 11.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, "رجوع", tint = RecoveryGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = RecoveryBlack)
            )
        },
        content = content
    )
}

@Composable
fun AskShurayhScreen(nav: NavHostController, laws: List<LawBook>) {
    var question by remember { mutableStateOf("") }
    var eventDate by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf<String?>(null) }
    var sources by remember { mutableStateOf<List<LawBook>>(emptyList()) }

    val voiceLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spoken.isNullOrBlank()) question = spoken
        }
    }

    fun searchLocal() {
        val tokens = question.trim().split(Regex("\\s+")).filter { it.length >= 3 }
        val ranked = laws.map { law ->
            val hay = "${law.title} ${law.category} ${law.sourceNote}"
            law to tokens.count { hay.contains(it, ignoreCase = true) }
        }.filter { it.second > 0 }.sortedByDescending { it.second }.map { it.first }.take(3)
        sources = ranked
        answer = when {
            question.isBlank() -> "اكتب السؤال أولاً."
            ranked.isEmpty() -> "لم أجد تطابقًا كافيًا داخل قاعدة المعرفة المحلية الحالية. لا أقدّم إجابة من الذاكرة دون مصدر."
            else -> buildString {
                append("نتيجة البحث المحلي:\n")
                ranked.forEachIndexed { index, law ->
                    append("[S${index + 1}] ${law.title} — ${law.category}\n")
                }
                if (eventDate.isNotBlank()) append("\nتاريخ الواقعة/الإجراء المدخل: $eventDate")
                append("\n\nراجع النص الرسمي وآخر تعديل قبل الاعتماد المهني.")
            }
        }
    }

    RecoveryScaffold(nav, "اسأل شُرَيْح", "بحث قانوني محلي بالمراجع — لا إجابة بلا مصدر") { p ->
        LazyColumn(
            Modifier.fillMaxSize().padding(p),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = RecoveryCard),
                    border = BorderStroke(1.dp, RecoveryGold.copy(alpha = .7f)),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("قاعدة المعرفة المحلية: ${laws.size} سجل • تعمل بدون إنترنت", color = RecoveryGreen, fontWeight = FontWeight.Bold)
                        Text("يمكنك كتابة سؤال أو استخدام الإدخال الصوتي. النتائج الحالية تعتمد فقط على المصادر المحلية المتاحة داخل التطبيق.", color = RecoverySilver, fontSize = 12.sp)
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 150.dp),
                    label = { Text("اكتب سؤالك القانوني") }
                )
            }
            item {
                OutlinedButton(
                    onClick = {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-EG")
                            putExtra(RecognizerIntent.EXTRA_PROMPT, "اسأل شُرَيْح بصوتك")
                        }
                        voiceLauncher.launch(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Outlined.Mic, null, tint = RecoveryGold)
                    Spacer(Modifier.width(8.dp))
                    Text("اسأل بصوتك")
                }
            }
            item {
                OutlinedTextField(
                    value = eventDate,
                    onValueChange = { eventDate = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("تاريخ الواقعة/الإجراء YYYY-MM-DD — اختياري") }
                )
            }
            item {
                Button(onClick = { searchLocal() }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Outlined.Search, null)
                    Spacer(Modifier.width(8.dp))
                    Text("بحث وإجابة بالمصادر")
                }
            }
            answer?.let { text ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = RecoveryCard),
                        border = BorderStroke(1.dp, RecoveryGold.copy(alpha = .7f))
                    ) {
                        Text(text, Modifier.padding(16.dp), color = RecoveryPlatinum, lineHeight = 22.sp)
                    }
                }
            }
            if (sources.isNotEmpty()) {
                item { Text("المصادر", color = RecoveryGold, fontWeight = FontWeight.Bold) }
                items(sources) { law ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = RecoveryCard),
                        border = BorderStroke(1.dp, RecoverySilver.copy(alpha = .55f))
                    ) {
                        Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.End) {
                            Text(law.title, color = RecoveryPlatinum, fontWeight = FontWeight.Bold)
                            Text(law.sourceNote, color = RecoverySilver, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScanReviewScreen(nav: NavHostController) {
    var selectedName by remember { mutableStateOf<String?>(null) }
    var reviewText by remember { mutableStateOf("") }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        selectedName = uri?.lastPathSegment ?: uri?.toString()
        if (uri != null && reviewText.isBlank()) reviewText = "تم استيراد المستند. راجع النص أو أدخله يدويًا قبل التحليل."
    }

    RecoveryScaffold(nav, "مسح ومراجعة المحاضر", "اربط المستند بقضية ثم راجع النص قبل التحليل") { p ->
        LazyColumn(Modifier.fillMaxSize().padding(p), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Button(onClick = { picker.launch(arrayOf("image/*", "application/pdf")) }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Outlined.DocumentScanner, null)
                    Spacer(Modifier.width(8.dp))
                    Text("استيراد صورة أو PDF")
                }
            }
            selectedName?.let { name -> item { Text("المستند المحدد: $name", color = RecoveryGreen) } }
            item {
                OutlinedTextField(
                    value = reviewText,
                    onValueChange = { reviewText = it },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 220.dp),
                    label = { Text("النص للمراجعة") }
                )
            }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = RecoveryCard), border = BorderStroke(1.dp, RecoveryGold.copy(alpha = .7f))) {
                    Text(
                        "تمت استعادة مسار الاستيراد والمراجعة. محرك OCR المحلي الآلي لم يُربط بعد في هذه النسخة، لذلك لن يدّعي التطبيق استخراج نص لم يحدث فعليًا.",
                        Modifier.padding(14.dp), color = RecoverySilver, fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun DocumentForensicsScreen(nav: NavHostController) {
    var selectedName by remember { mutableStateOf<String?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        selectedName = uri?.lastPathSegment ?: uri?.toString()
    }
    RecoveryScaffold(nav, "الفحص الجنائي للمستند", "جودة الصورة ومؤشرات أولية دون ادعاء إثبات التزوير") { p ->
        LazyColumn(Modifier.fillMaxSize().padding(p), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Button(onClick = { picker.launch(arrayOf("image/*", "application/pdf")) }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Outlined.FactCheck, null)
                    Spacer(Modifier.width(8.dp))
                    Text("اختيار مستند للفحص")
                }
            }
            selectedName?.let { name ->
                item { Text("المستند: $name", color = RecoveryGreen) }
                item { RecoveryInfoCard("فحص أولي", "تم قبول الملف لمسار الفحص. التحليل الجنائي المتقدم للصورة لم يُفعّل بعد في هذا البناء، لذلك لن نصدر حكمًا أو نسبة تزوير وهمية.", Icons.Outlined.Security) }
            }
            item { RecoveryInfoCard("حدود الاستخدام", "النتيجة — عند اكتمال محرك التحليل — ستكون مؤشرات تقنية مساعدة فقط وليست إثباتًا قضائيًا للتزوير.", Icons.Outlined.WarningAmber) }
        }
    }
}

@Composable
fun LawyerProfileScreen(nav: NavHostController) {
    var name by remember { mutableStateOf("") }
    var barNumber by remember { mutableStateOf("") }
    var specialization by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf(false) }
    RecoveryScaffold(nav, "صفحة المحامي", "الملف المهني والبيانات المحلية") { p ->
        LazyColumn(Modifier.fillMaxSize().padding(p), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("الاسم") }) }
            item { OutlinedTextField(barNumber, { barNumber = it }, Modifier.fillMaxWidth(), label = { Text("رقم القيد") }) }
            item { OutlinedTextField(specialization, { specialization = it }, Modifier.fillMaxWidth(), label = { Text("التخصص") }) }
            item { Button(onClick = { saved = true }, modifier = Modifier.fillMaxWidth()) { Text("حفظ محليًا") } }
            if (saved) item { Text("تم حفظ بيانات الجلسة الحالية محليًا داخل الشاشة.", color = RecoveryGreen) }
        }
    }
}

@Composable
fun LawyerNetworkScreen(nav: NavHostController) {
    val entries = listOf(
        "دليل الزملاء المحلي" to "جهات اتصال مهنية محفوظة على الجهاز",
        "مراسلة آمنة" to "الانتقال إلى قسم Tor من الصفحة الرئيسية أو القائمة",
        "طلبات التعاون" to "مساحة تنظيمية محلية — لا توجد خدمة سحابية مفعلة حاليًا"
    )
    RecoveryScaffold(nav, "شبكة المحامين", "تنظيم جهات الاتصال والتعاون المهني") { p ->
        LazyColumn(Modifier.fillMaxSize().padding(p), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(entries) { (title, body) -> RecoveryInfoCard(title, body, Icons.Outlined.Groups) }
        }
    }
}

@Composable
private fun RecoveryInfoCard(title: String, body: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(
        colors = CardDefaults.cardColors(containerColor = RecoveryCard),
        border = BorderStroke(1.dp, RecoverySilver.copy(alpha = .55f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.Top) {
            Icon(icon, null, tint = RecoveryGold)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                Text(title, color = RecoveryPlatinum, fontWeight = FontWeight.Bold)
                Text(body, color = RecoverySilver, fontSize = 12.sp, lineHeight = 18.sp)
            }
        }
    }
}
