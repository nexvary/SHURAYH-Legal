package com.nexvary.shurayh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FolderShared
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.PeopleAlt
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.LayoutDirection.Rtl
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.runtime.CompositionLocalProvider

private val Navy = Color(0xFF0C1319)
private val Gunmetal = Color(0xFF2E3945)
private val Silver = Color(0xFF9E9B98)
private val Platinum = Color(0xFFD9D7D4)
private val ElectricBlue = Color(0xFF6A88A0)
private val RoyalGold = Color(0xFFC9A44A)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides Rtl) {
                ShurayhApp()
            }
        }
    }
}

data class LegalModule(
    val title: String,
    val subtitle: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShurayhApp() {
    MaterialTheme {
        Scaffold(
            containerColor = Navy,
            topBar = {
                TopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "شُرَيْح",
                                color = Platinum,
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp
                            )
                            Text(
                                text = "SHURAYH • المساعد القانوني الذكي للمحامي",
                                color = Silver,
                                fontSize = 12.sp
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy)
                )
            }
        ) { padding ->
            HomeScreen(padding)
        }
    }
}

@Composable
private fun HomeScreen(padding: PaddingValues) {
    val modules = listOf(
        LegalModule("القضايا", "ملفات القضايا والمستندات والملاحظات", Icons.Outlined.Gavel),
        LegalModule("العملاء", "الملفات الشخصية وبيانات التواصل", Icons.Outlined.PeopleAlt),
        LegalModule("الجلسات", "المواعيد والتنبيهات ومتابعة الجلسات", Icons.Outlined.CalendarMonth),
        LegalModule("مكتبة القانون", "القوانين المصرية والبحث القانوني", Icons.Outlined.MenuBook),
        LegalModule("المذكرات والعقود", "إنشاء وحفظ ومراجعة المستندات", Icons.Outlined.Description),
        LegalModule("ملف المكتب", "إدارة المكتب والفريق والملفات", Icons.Outlined.FolderShared),
        LegalModule("البحث الذكي", "بحث موحد في القضايا والقوانين والمستندات", Icons.Outlined.Search),
        LegalModule("المحاكم والجهات", "دليل المحاكم والجهات القانونية", Icons.Outlined.AccountBalance)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy)
            .padding(padding),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            WelcomeCard()
        }
        item {
            Text(
                text = "مساحة العمل القانونية",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
                color = Platinum,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
        item {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.height(610.dp),
                userScrollEnabled = false,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(modules) { module ->
                    ModuleCard(module)
                }
            }
        }
    }
}

@Composable
private fun WelcomeCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Gunmetal),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "مرحبًا بك في شُرَيْح",
                color = RoyalGold,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "منصة قانونية احترافية لتنظيم القضايا والعملاء والجلسات والبحث والمستندات في مكان واحد.",
                color = Platinum,
                lineHeight = 22.sp
            )
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusPill("RTL", ElectricBlue)
                StatusPill("Android 15", Silver)
                StatusPill("Local-first", RoyalGold)
            }
        }
    }
}

@Composable
private fun StatusPill(text: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.16f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text = text, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ModuleCard(module: LegalModule) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Gunmetal.copy(alpha = 0.94f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Icon(
                imageVector = module.icon,
                contentDescription = module.title,
                tint = RoyalGold
            )
            Text(
                text = module.title,
                color = Platinum,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = module.subtitle,
                color = Silver,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        }
    }
}
