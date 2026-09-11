package com.nexvary.shurayh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nexvary.shurayh.core.SharedPreferencesLegalPersistence
import com.nexvary.shurayh.core.ShurayhViewModel
import com.nexvary.shurayh.core.ShurayhViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm: ShurayhViewModel = viewModel(
                factory = ShurayhViewModelFactory(
                    SharedPreferencesLegalPersistence(applicationContext)
                )
            )
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                ShurayhRoot(vm)
            }
        }
    }
}
