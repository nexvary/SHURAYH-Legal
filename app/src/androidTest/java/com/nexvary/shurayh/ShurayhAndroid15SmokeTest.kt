package com.nexvary.shurayh

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.nexvary.shurayh.core.EncryptedAttachmentStore
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ShurayhAndroid15SmokeTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private fun back() {
        composeRule.onNodeWithContentDescription("رجوع").assertIsDisplayed().performClick()
        composeRule.waitForIdle()
    }

    @Test
    fun home_rtl_workspace_is_visible() {
        composeRule.onNodeWithText("شُرَيْح").assertIsDisplayed()
        composeRule.onNodeWithText("القضايا").assertIsDisplayed()
        composeRule.onNodeWithText("العملاء").assertIsDisplayed()
        composeRule.onNodeWithText("الجلسات").assertIsDisplayed()
    }

    @Test
    fun main_module_buttons_open_real_destinations_and_back_works() {
        composeRule.onNodeWithText("القضايا").performClick()
        composeRule.onNodeWithText("ملفات القضايا وحالتها").assertIsDisplayed()
        back()

        composeRule.onNodeWithText("العملاء").performClick()
        composeRule.onNodeWithText("ملفات الموكلين").assertIsDisplayed()
        back()

        composeRule.onNodeWithText("الجلسات").performClick()
        composeRule.onNodeWithText("أجندة مرتبطة بملفات القضايا").assertIsDisplayed()
        back()

        composeRule.onNodeWithText("مكتبة القانون").performClick()
        composeRule.onNodeWithText("محتوى مرجعي يحتاج تحققًا من آخر تعديل").assertIsDisplayed()
        back()
    }

    @Test
    fun add_action_buttons_open_and_cancel_dialogs() {
        composeRule.onNodeWithText("القضايا").performClick()
        composeRule.onNodeWithContentDescription("إضافة قضية").assertIsEnabled().performClick()
        composeRule.onNodeWithText("إضافة قضية").assertIsDisplayed()
        composeRule.onNodeWithText("إلغاء").performClick()
        back()

        composeRule.onNodeWithText("العملاء").performClick()
        composeRule.onNodeWithContentDescription("إضافة عميل").assertIsEnabled().performClick()
        composeRule.onNodeWithText("إضافة عميل").assertIsDisplayed()
        composeRule.onNodeWithText("إلغاء").performClick()
        back()

        composeRule.onNodeWithText("الجلسات").performClick()
        composeRule.onNodeWithContentDescription("إضافة جلسة").assertIsEnabled().performClick()
        composeRule.onNodeWithText("إضافة جلسة").assertIsDisplayed()
        composeRule.onNodeWithText("إلغاء").performClick()
        back()
    }

    @Test
    fun previously_dead_cards_are_interactive() {
        composeRule.onNodeWithText("مكتبة القانون").performClick()
        composeRule.onNodeWithText("القانون المدني المصري").assertIsDisplayed().performClick()
        composeRule.onNodeWithText("إغلاق").assertIsDisplayed().performClick()
        back()

        composeRule.onNodeWithText("ملف المكتب").performClick()
        composeRule.onNodeWithText("العملاء").assertIsDisplayed().performClick()
        composeRule.onNodeWithText("ملفات الموكلين").assertIsDisplayed()
        back()
        back()

        composeRule.onNodeWithText("المحاكم").performClick()
        composeRule.onNodeWithText("المحاكم الابتدائية").assertIsDisplayed().performClick()
        composeRule.onNodeWithText("إغلاق").assertIsDisplayed().performClick()
        back()
    }

    @Test
    fun bottom_bar_buttons_and_secondary_back_buttons_work() {
        composeRule.onNodeWithText("القائمة").performClick()
        composeRule.onNodeWithText("كل أقسام SHURAYH").assertIsDisplayed()
        back()

        composeRule.onNodeWithText("الإشعارات").performClick()
        composeRule.onNodeWithText("الجلسات والتنبيهات").assertIsDisplayed()
        back()

        composeRule.onNodeWithText("عنا").performClick()
        composeRule.onNodeWithText("SHURAYH • NEXVARY Legal Technology").assertIsDisplayed()
        back()

        composeRule.onNodeWithText("الضبط").performClick()
        composeRule.onNodeWithText("الخصوصية والبيانات").assertIsDisplayed()
        composeRule.onNodeWithText("استعادة البيانات التجريبية").performClick()
        composeRule.onNodeWithText("استعادة البيانات التجريبية؟").assertIsDisplayed()
        composeRule.onNodeWithText("إلغاء").performClick()
        back()
    }

    @Test
    fun tor_messaging_navigation_contacts_refresh_and_back_work() {
        composeRule.onNodeWithText("المراسلة الآمنة").performClick()
        composeRule.onNodeWithText("Tor • SHURAYH Secure Messaging").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("إعادة الفحص").assertIsDisplayed().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("مكتب المحامي").assertIsDisplayed().performClick()
        composeRule.onNodeWithText("المحادثة: مكتب المحامي").assertIsDisplayed()
        back()
    }

    @Test
    fun encrypted_attachment_round_trip_works_on_android_keystore() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val store = EncryptedAttachmentStore(context)
        val original = "مستند قانوني تجريبي مشفر".toByteArray()
        val attachment = store.store("test.txt", "text/plain", original)

        val encryptedFile = context.filesDir.resolve("legal_attachments_v1/${attachment.encryptedFileName}")
        assertTrue(encryptedFile.exists())
        assertFalse(encryptedFile.readBytes().contentEquals(original))

        val restored = store.read(attachment).getOrThrow()
        assertArrayEquals(original, restored)
        assertTrue(store.delete(attachment))
    }
}
