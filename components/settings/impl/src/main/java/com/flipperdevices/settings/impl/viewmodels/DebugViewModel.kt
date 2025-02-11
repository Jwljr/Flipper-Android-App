package com.flipperdevices.settings.impl.viewmodels

import android.app.Application
import android.widget.Toast
import androidx.datastore.core.DataStore
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.flipperdevices.bridge.service.api.provider.FlipperServiceProvider
import com.flipperdevices.bridge.synchronization.api.SynchronizationApi
import com.flipperdevices.core.preference.pb.Settings
import com.flipperdevices.core.ui.lifecycle.AndroidLifecycleViewModel
import com.flipperdevices.debug.api.StressTestFeatureEntry
import com.flipperdevices.nfc.mfkey32.api.MfKey32ScreenEntry
import com.flipperdevices.settings.impl.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tangle.viewmodel.VMInject

class DebugViewModel @VMInject constructor(
    application: Application,
    private val synchronizationApi: SynchronizationApi,
    private val settingsDataStore: DataStore<Settings>,
    private val serviceProvider: FlipperServiceProvider,
    private val mfKey32ScreenEntry: MfKey32ScreenEntry,
    private val stressTestFeatureEntry: StressTestFeatureEntry
) : AndroidLifecycleViewModel(application) {

    fun onOpenStressTest(navController: NavController) {
        navController.navigate(stressTestFeatureEntry.ROUTE.name)
    }

    fun onStartSynchronization() {
        synchronizationApi.startSynchronization(force = true)
    }

    fun onSwitchIgnoreSupportedVersion(ignored: Boolean) {
        viewModelScope.launch(Dispatchers.Default) {
            settingsDataStore.updateData {
                it.toBuilder()
                    .setIgnoreUnsupportedVersion(ignored)
                    .build()
            }

            askRestartApp()
        }
    }

    fun onSwitchIgnoreUpdaterVersion(alwaysUpdate: Boolean) {
        viewModelScope.launch(Dispatchers.Default) {
            settingsDataStore.updateData {
                it.toBuilder()
                    .setAlwaysUpdate(alwaysUpdate)
                    .build()
            }
        }
    }

    fun onSwitchIgnoreSubGhzProvisioning(ignoreSubGhzProvisioningOnZeroRegion: Boolean) {
        viewModelScope.launch(Dispatchers.Default) {
            settingsDataStore.updateData {
                it.toBuilder()
                    .setIgnoreSubghzProvisioningOnZeroRegion(ignoreSubGhzProvisioningOnZeroRegion)
                    .build()
            }
        }
    }

    fun onSwitchSkipAutoSync(skipAutoSync: Boolean) {
        viewModelScope.launch(Dispatchers.Default) {
            settingsDataStore.updateData {
                it.toBuilder()
                    .setSkipAutoSyncInDebug(skipAutoSync)
                    .build()
            }
        }
    }

    fun onSwitchApplicationCatalog(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.updateData {
                it.toBuilder()
                    .setApplicationCatalog(enabled)
                    .build()
            }
        }
    }

    fun restartRpc() {
        serviceProvider.provideServiceApi(this) {
            it.restartRPC()
        }
    }

    fun openMfKey32(navController: NavController) {
        navController.navigate(mfKey32ScreenEntry.startDestination())
    }

    private suspend fun askRestartApp() = withContext(Dispatchers.Main) {
        val context = getApplication<Application>()
        Toast.makeText(
            context,
            R.string.debug_ignored_unsupported_version_toast,
            Toast.LENGTH_LONG
        ).show()
    }
}
