package org.staacks.alpharemote

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.staacks.alpharemote.camera.CameraAction
import org.staacks.alpharemote.camera.KeyBindingHelper
import org.staacks.alpharemote.databinding.ActivityMainBinding
import org.staacks.alpharemote.service.AlphaRemoteService
import org.staacks.alpharemote.service.ServiceRunning
import java.io.Serializable

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var settingsStore: SettingsStore
    var customButtonList: List<CameraAction>? = null

    companion object {
        const val NAVIGATE_TO_INTENT_EXTRA = "nav_to"
        val TAG: String = "alpharemote"
        var isKeyCaptureActive: Boolean = false
    }
    val SELECTED_PAGE = "selected_page"

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        navView.setupWithNavController(navController)

        var startPage = intent?.getIntExtra(NAVIGATE_TO_INTENT_EXTRA, R.id.navigation_camera) ?: R.id.navigation_camera
        startPage = savedInstanceState?.getInt(SELECTED_PAGE, startPage) ?: startPage
        navigateTo(startPage)

        settingsStore = SettingsStore(applicationContext)
        lifecycleScope.launch {
            settingsStore.customButtonSettings.collectLatest {
                customButtonList = it.customButtonList
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        intent?.getIntExtra(NAVIGATE_TO_INTENT_EXTRA, R.id.navigation_camera)?.let {
            navigateTo(it)
        }
    }

    fun navigateTo(id: Int) {
        binding.navView.selectedItemId = id
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(SELECTED_PAGE, binding.navView.selectedItemId)
    }

    override fun onPause() {
        super.onPause()
        isKeyCaptureActive = false
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (isKeyCaptureActive) {
            return super.dispatchKeyEvent(event)
        }

        val handled = KeyBindingHelper.processKeyEvent(event, customButtonList) { action ->
            triggerCameraAction(action)
        }
        if (handled) {
            return true
        }

        return super.dispatchKeyEvent(event)
    }

    private fun triggerCameraAction(cameraAction: CameraAction) {
        Log.d(TAG, "Triggering mapped camera action: ${cameraAction.preset.name} from key ${cameraAction.keyCode}")
        if (AlphaRemoteService.serviceState.value is ServiceRunning) {
            val intent = Intent(this, AlphaRemoteService::class.java).apply {
                action = AlphaRemoteService.BUTTON_INTENT_ACTION
                putExtra(AlphaRemoteService.BUTTON_INTENT_CAMERA_ACTION_EXTRA, cameraAction as Serializable)
                putExtra(AlphaRemoteService.BUTTON_INTENT_CAMERA_ACTION_UP_EXTRA, true)
                putExtra(AlphaRemoteService.BUTTON_INTENT_CAMERA_ACTION_DOWN_EXTRA, true)
            }
            startService(intent)
        }
    }
}