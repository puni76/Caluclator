package com.example.caluclator

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.caluclator.ui.theme.CaluclatorTheme
import com.example.caluclator.ui.theme.LightGray
import com.example.caluclator.ui.theme.MediumGray
import com.example.caluclator.ui.theme.Orange
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.isFlexibleUpdateAllowed
import com.google.android.play.core.ktx.isImmediateUpdateAllowed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class MainActivity : ComponentActivity() {

    private lateinit var appUpdateManager:AppUpdateManager
    private val updateType = AppUpdateType.IMMEDIATE
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appUpdateManager=AppUpdateManagerFactory.create(applicationContext)
        if (updateType==AppUpdateType.FLEXIBLE){
            appUpdateManager.registerListener(installStateUpdatedListener)
        }
        checkForAppUpdates()
        setContent {
            CaluclatorTheme {
                CalculatorScreen()
                }
            }
        }
    private val installStateUpdatedListener = InstallStateUpdatedListener{ state->
    if (state.installStatus()==InstallStatus.DOWNLOADED){
        Toast.makeText(
            applicationContext,
            "Download successful. Restarting app in 5 seconds.",
            Toast.LENGTH_LONG
        ).show()
        lifecycleScope.launch{
            delay(5.seconds)
            appUpdateManager.completeUpdate()
        }
    }
}
    private fun checkForAppUpdates(){
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info->
            val isUpdateAvailable = info.updateAvailability()==UpdateAvailability.UPDATE_AVAILABLE
            val isUpdateAllowed = when(updateType){
                AppUpdateType.FLEXIBLE->info.isFlexibleUpdateAllowed
                AppUpdateType.IMMEDIATE->info.isImmediateUpdateAllowed
                else-> false
            }
            if (isUpdateAvailable && isUpdateAllowed){
                appUpdateManager.startUpdateFlowForResult(
                    info,
                    updateType,
                    this,
                    123
                )
            }
        }
     }

    override fun onResume() {
        super.onResume()
        if (updateType == AppUpdateType.IMMEDIATE) {


            appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
                if (info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    appUpdateManager.startUpdateFlowForResult(
                        info,
                        updateType,
                        this,
                        123
                    )
                }
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode==123){
            if (resultCode!= RESULT_OK){
                println("Something went wrong updating....")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (updateType==AppUpdateType.FLEXIBLE){
            appUpdateManager.unregisterListener(installStateUpdatedListener)
        }
    }
  }


