package com.nexvary.shurayh

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.NetworkCheck
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

private val TorBlack = Color(0xFF070A0D)
private val TorCard = Color(0xFF0A0D10)
private val TorSilver = Color(0xFFB7BABE)
private val TorPlatinum = Color(0xFFE1E2E4)
private val TorGold = Color(0xFFC9A44A)

data class TorContact(val name: String, val onionAddress: String)
data class TorMessage(val fromMe: Boolean, val text: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TorMessagingScreen(nav: NavHostController) {
    var proxyState by remember { mutableStateOf("غير متصل") }
    var proxyPort by remember { mutableStateOf<Int?>(null) }
    var checking by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<TorContact?>(null) }
    var draft by remember { mutableStateOf("") }
    var messages by remember {
        mutableStateOf(
            listOf(
                TorMessage(false, "مساحة مراسلة SHURAYH الآمنة جاهزة للربط بخدمة Tor محلية."),
                TorMessage(false, "لن نعرض حالة «متصل» إلا بعد اكتشاف SOCKS proxy فعلي على الجهاز.")
            )
        )
    }

    val contacts = remember {
        listOf(
            TorContact("مكتب المحامي", "examplelawoffice000000000000000000000000000000000000000000000000.onion"),
            TorContact("جهة اتصال تجريبية", "examplecontact000000000000000000000000000000000000000000000000.onion")
        )
    }

    LaunchedEffect(Unit) {
        checking = true
        val found = detectTorProxy()
        proxyPort = found
        proxyState = if (found != null) "Tor SOCKS مكتشف على 127.0.0.1:$found" else "غير متصل — شغّل Orbot/Tor أولاً"
        checking = false
    }

    Scaffold(
        containerColor = TorBlack,
        topBar = {
            TopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.fillMaxWidth()) {
                        Text("المراسلة الآمنة", color = TorPlatinum, fontWeight = FontWeight.Black)
                        Text("Tor • SHURAYH Secure Messaging", color = TorSilver, fontSize = 11.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, "رجوع", tint = TorGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TorBlack)
            )
        }
    ) { p ->
        LazyColumn(
            Modifier.fillMaxSize().padding(p),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = TorCard),
                    border = BorderStroke(1.dp, TorGold.copy(alpha = .75f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.NetworkCheck, null, tint = TorGold)
                            Spacer(Modifier.width(10.dp))
                            Text("حالة Tor", color = TorPlatinum, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            IconButton(
                                enabled = !checking,
                                onClick = {
                                    checking = true
                                    proxyState = "جارٍ الفحص..."
                                }
                            ) { Icon(Icons.Outlined.Refresh, "إعادة الفحص", tint = TorGold) }
                        }
                        Text(proxyState, color = if (proxyPort != null) TorGold else TorSilver, fontSize = 12.sp)
                        Text(
                            "التطبيق لا يدّعي تشفير Tor أو اتصالًا فعليًا قبل اكتشاف SOCKS محلي صالح. النقل إلى خدمة .onion يحتاج نقطة خدمة متوافقة.",
                            color = TorSilver,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            item { Text("جهات الاتصال", color = TorGold, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
            items(contacts) { contact ->
                Card(
                    onClick = { selected = contact },
                    colors = CardDefaults.cardColors(containerColor = TorCard),
                    border = BorderStroke(1.dp, TorSilver.copy(alpha = .7f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Person, null, tint = TorGold)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                            Text(contact.name, color = TorPlatinum, fontWeight = FontWeight.Bold)
                            Text(contact.onionAddress.take(30) + "…", color = TorSilver, fontSize = 10.sp)
                        }
                    }
                }
            }

            selected?.let { contact ->
                item {
                    Text("المحادثة: ${contact.name}", color = TorGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                items(messages) { m ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = TorCard),
                        border = BorderStroke(1.dp, if (m.fromMe) TorGold.copy(alpha = .65f) else TorSilver.copy(alpha = .45f)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(m.text, Modifier.fillMaxWidth().padding(12.dp), color = TorPlatinum, fontSize = 12.sp)
                    }
                }
                item {
                    OutlinedTextField(
                        value = draft,
                        onValueChange = { draft = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("رسالة") },
                        leadingIcon = { Icon(Icons.Outlined.Lock, null, tint = TorGold) },
                        trailingIcon = {
                            IconButton(
                                enabled = draft.isNotBlank() && proxyPort != null,
                                onClick = {
                                    messages = messages + TorMessage(true, draft.trim())
                                    draft = ""
                                }
                            ) { Icon(Icons.Outlined.Send, "إرسال", tint = TorGold) }
                        }
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        if (proxyPort == null) "الإرسال معطل حتى يتم اكتشاف Tor SOCKS محلي فعلي." else "تم اكتشاف Tor محليًا؛ إرسال الشبكة الخارجي يتطلب خدمة .onion متوافقة.",
                        color = TorSilver,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }

    LaunchedEffect(checking) {
        if (checking && proxyState == "جارٍ الفحص...") {
            val found = detectTorProxy()
            proxyPort = found
            proxyState = if (found != null) "Tor SOCKS مكتشف على 127.0.0.1:$found" else "غير متصل — شغّل Orbot/Tor أولاً"
            checking = false
        }
    }
}

private suspend fun detectTorProxy(): Int? = withContext(Dispatchers.IO) {
    listOf(9050, 9150).firstOrNull { port ->
        runCatching {
            Socket().use { socket ->
                socket.connect(InetSocketAddress("127.0.0.1", port), 500)
            }
            true
        }.getOrDefault(false)
    }
}
