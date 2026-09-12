package com.nexvary.shurayh

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
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

    @Test
    fun home_rtl_workspace_is_visible() {
        composeRule.onNodeWithText("شُرَيْح").assertIsDisplayed()
        composeRule.onNodeWithText("القضايا").assertIsDisplayed()
        composeRule.onNodeWithText("العملاء").assertIsDisplayed()
        composeRule.onNodeWithText("الجلسات").assertIsDisplayed()
    }

    @Test
    fun previously_dead_cards_are_interactive() {
        composeRule.onNodeWithText("مكتبة القانون").performClick()
        composeRule.onNodeWithText("القانون المدني المصري").assertIsDisplayed().performClick()
        composeRule.onNodeWithText("إغلاق").assertIsDisplayed().performClick()

        composeRule.runOnUiThread { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()

        composeRule.onNodeWithText("ملف المكتب").performClick()
        composeRule.onNodeWithText("العملاء").assertIsDisplayed().performClick()
        composeRule.onNodeWithText("ملفات الموكلين").assertIsDisplayed()

        composeRule.runOnUiThread { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeRule.runOnUiThread { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()

        composeRule.onNodeWithText("المحاكم").performClick()
        composeRule.onNodeWithText("المحاكم الابتدائية").assertIsDisplayed().performClick()
        composeRule.onNodeWithText("إغلاق").assertIsDisplayed()
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
