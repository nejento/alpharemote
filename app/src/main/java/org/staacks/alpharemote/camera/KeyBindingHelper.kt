package org.staacks.alpharemote.camera

import android.view.KeyEvent

object KeyBindingHelper {

    private val RESERVED_KEY_CODES = setOf(
        KeyEvent.KEYCODE_BACK,
        KeyEvent.KEYCODE_HOME,
        KeyEvent.KEYCODE_APP_SWITCH,
        KeyEvent.KEYCODE_POWER,
        KeyEvent.KEYCODE_SLEEP,
        KeyEvent.KEYCODE_WAKEUP,
        KeyEvent.KEYCODE_PAIRING,
        KeyEvent.KEYCODE_SETTINGS,
        KeyEvent.KEYCODE_NOTIFICATION,
        KeyEvent.KEYCODE_MENU,
        KeyEvent.KEYCODE_WINDOW,
        KeyEvent.KEYCODE_UNKNOWN,
        KeyEvent.KEYCODE_CALL,
        KeyEvent.KEYCODE_ENDCALL,
        KeyEvent.KEYCODE_SEARCH,
        KeyEvent.KEYCODE_ALL_APPS,
        KeyEvent.KEYCODE_ASSIST,
        KeyEvent.KEYCODE_VOICE_ASSIST,
        KeyEvent.KEYCODE_SYSTEM_NAVIGATION_UP,
        KeyEvent.KEYCODE_SYSTEM_NAVIGATION_DOWN,
        KeyEvent.KEYCODE_SYSTEM_NAVIGATION_LEFT,
        KeyEvent.KEYCODE_SYSTEM_NAVIGATION_RIGHT,
        KeyEvent.KEYCODE_SOFT_LEFT,
        KeyEvent.KEYCODE_SOFT_RIGHT
    )

    fun isModifierKey(keyCode: Int): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_SHIFT_LEFT,
            KeyEvent.KEYCODE_SHIFT_RIGHT,
            KeyEvent.KEYCODE_ALT_LEFT,
            KeyEvent.KEYCODE_ALT_RIGHT,
            KeyEvent.KEYCODE_CTRL_LEFT,
            KeyEvent.KEYCODE_CTRL_RIGHT,
            KeyEvent.KEYCODE_META_LEFT,
            KeyEvent.KEYCODE_META_RIGHT,
            KeyEvent.KEYCODE_SYM,
            KeyEvent.KEYCODE_FUNCTION,
            KeyEvent.KEYCODE_CAPS_LOCK,
            KeyEvent.KEYCODE_NUM_LOCK,
            KeyEvent.KEYCODE_SCROLL_LOCK -> true
            else -> false
        }
    }

    fun isSystemReservedKey(keyCode: Int): Boolean {
        if (keyCode in RESERVED_KEY_CODES) return true
        if (isModifierKey(keyCode)) return true
        return false
    }

    fun isValidPhysicalKey(keyCode: Int): Boolean {
        return keyCode > 0 && !isSystemReservedKey(keyCode)
    }

    fun getKeyDisplayName(keyCode: Int): String {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> "Volume Up"
            KeyEvent.KEYCODE_VOLUME_DOWN -> "Volume Down"
            KeyEvent.KEYCODE_VOLUME_MUTE -> "Volume Mute"
            KeyEvent.KEYCODE_CAMERA -> "Camera"
            KeyEvent.KEYCODE_FOCUS -> "Focus"
            KeyEvent.KEYCODE_HEADSETHOOK -> "Headset Hook"
            KeyEvent.KEYCODE_MEDIA_PLAY -> "Play"
            KeyEvent.KEYCODE_MEDIA_PAUSE -> "Pause"
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> "Play / Pause"
            KeyEvent.KEYCODE_MEDIA_STOP -> "Stop"
            KeyEvent.KEYCODE_MEDIA_NEXT -> "Next Track"
            KeyEvent.KEYCODE_MEDIA_PREVIOUS -> "Previous Track"
            KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> "Fast Forward"
            KeyEvent.KEYCODE_MEDIA_REWIND -> "Rewind"
            KeyEvent.KEYCODE_MEDIA_RECORD -> "Record"
            KeyEvent.KEYCODE_MEDIA_STEP_FORWARD -> "Step Forward"
            KeyEvent.KEYCODE_MEDIA_STEP_BACKWARD -> "Step Backward"
            KeyEvent.KEYCODE_MEDIA_SKIP_FORWARD -> "Skip Forward"
            KeyEvent.KEYCODE_MEDIA_SKIP_BACKWARD -> "Skip Backward"
            KeyEvent.KEYCODE_ENTER -> "Enter"
            KeyEvent.KEYCODE_NUMPAD_ENTER -> "Numpad Enter"
            KeyEvent.KEYCODE_DPAD_CENTER -> "D-Pad Center"
            KeyEvent.KEYCODE_DPAD_UP -> "D-Pad Up"
            KeyEvent.KEYCODE_DPAD_DOWN -> "D-Pad Down"
            KeyEvent.KEYCODE_DPAD_LEFT -> "D-Pad Left"
            KeyEvent.KEYCODE_DPAD_RIGHT -> "D-Pad Right"
            KeyEvent.KEYCODE_BUTTON_A -> "Button A"
            KeyEvent.KEYCODE_BUTTON_B -> "Button B"
            KeyEvent.KEYCODE_BUTTON_C -> "Button C"
            KeyEvent.KEYCODE_BUTTON_X -> "Button X"
            KeyEvent.KEYCODE_BUTTON_Y -> "Button Y"
            KeyEvent.KEYCODE_BUTTON_Z -> "Button Z"
            KeyEvent.KEYCODE_BUTTON_L1 -> "Button L1"
            KeyEvent.KEYCODE_BUTTON_R1 -> "Button R1"
            KeyEvent.KEYCODE_BUTTON_L2 -> "Button L2"
            KeyEvent.KEYCODE_BUTTON_R2 -> "Button R2"
            KeyEvent.KEYCODE_BUTTON_THUMBL -> "Thumb Left"
            KeyEvent.KEYCODE_BUTTON_THUMBR -> "Thumb Right"
            KeyEvent.KEYCODE_BUTTON_START -> "Start"
            KeyEvent.KEYCODE_BUTTON_SELECT -> "Select"
            KeyEvent.KEYCODE_BUTTON_MODE -> "Mode"
            KeyEvent.KEYCODE_BUTTON_1 -> "Button 1"
            KeyEvent.KEYCODE_BUTTON_2 -> "Button 2"
            KeyEvent.KEYCODE_BUTTON_3 -> "Button 3"
            KeyEvent.KEYCODE_BUTTON_4 -> "Button 4"
            KeyEvent.KEYCODE_BUTTON_5 -> "Button 5"
            KeyEvent.KEYCODE_BUTTON_6 -> "Button 6"
            KeyEvent.KEYCODE_BUTTON_7 -> "Button 7"
            KeyEvent.KEYCODE_BUTTON_8 -> "Button 8"
            KeyEvent.KEYCODE_BUTTON_9 -> "Button 9"
            KeyEvent.KEYCODE_BUTTON_10 -> "Button 10"
            KeyEvent.KEYCODE_BUTTON_11 -> "Button 11"
            KeyEvent.KEYCODE_BUTTON_12 -> "Button 12"
            KeyEvent.KEYCODE_BUTTON_13 -> "Button 13"
            KeyEvent.KEYCODE_BUTTON_14 -> "Button 14"
            KeyEvent.KEYCODE_BUTTON_15 -> "Button 15"
            KeyEvent.KEYCODE_BUTTON_16 -> "Button 16"
            KeyEvent.KEYCODE_SPACE -> "Space"
            KeyEvent.KEYCODE_TAB -> "Tab"
            KeyEvent.KEYCODE_DEL -> "Backspace"
            KeyEvent.KEYCODE_FORWARD_DEL -> "Delete"
            KeyEvent.KEYCODE_PAGE_UP -> "Page Up"
            KeyEvent.KEYCODE_PAGE_DOWN -> "Page Down"
            KeyEvent.KEYCODE_MOVE_HOME -> "Move Home"
            KeyEvent.KEYCODE_MOVE_END -> "Move End"
            KeyEvent.KEYCODE_INSERT -> "Insert"
            KeyEvent.KEYCODE_ESCAPE -> "Escape"
            KeyEvent.KEYCODE_MUTE -> "Mute"
            KeyEvent.KEYCODE_ZOOM_IN -> "Zoom In"
            KeyEvent.KEYCODE_ZOOM_OUT -> "Zoom Out"
            KeyEvent.KEYCODE_NAVIGATE_NEXT -> "Navigate Next"
            KeyEvent.KEYCODE_NAVIGATE_PREVIOUS -> "Navigate Previous"
            KeyEvent.KEYCODE_NAVIGATE_IN -> "Navigate In"
            KeyEvent.KEYCODE_NAVIGATE_OUT -> "Navigate Out"
            KeyEvent.KEYCODE_STEM_PRIMARY -> "Primary Stem"
            KeyEvent.KEYCODE_STEM_1 -> "Stem 1"
            KeyEvent.KEYCODE_STEM_2 -> "Stem 2"
            KeyEvent.KEYCODE_STEM_3 -> "Stem 3"
            KeyEvent.KEYCODE_F1 -> "F1"
            KeyEvent.KEYCODE_F2 -> "F2"
            KeyEvent.KEYCODE_F3 -> "F3"
            KeyEvent.KEYCODE_F4 -> "F4"
            KeyEvent.KEYCODE_F5 -> "F5"
            KeyEvent.KEYCODE_F6 -> "F6"
            KeyEvent.KEYCODE_F7 -> "F7"
            KeyEvent.KEYCODE_F8 -> "F8"
            KeyEvent.KEYCODE_F9 -> "F9"
            KeyEvent.KEYCODE_F10 -> "F10"
            KeyEvent.KEYCODE_F11 -> "F11"
            KeyEvent.KEYCODE_F12 -> "F12"
            else -> {
                try {
                    val str = KeyEvent.keyCodeToString(keyCode)
                    if (str.startsWith("KEYCODE_")) {
                        val formatted = str.removePrefix("KEYCODE_")
                            .replace('_', ' ')
                            .lowercase()
                            .split(" ")
                            .filter { it.isNotBlank() }
                            .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
                        if (formatted.isBlank() || formatted.equals("Unknown", ignoreCase = true)) {
                            "Key $keyCode"
                        } else {
                            formatted
                        }
                    } else if (str.isNotBlank() && !str.equals("unknown", ignoreCase = true) && str.toIntOrNull() == null) {
                        str
                    } else {
                        "Key $keyCode"
                    }
                } catch (_: Throwable) {
                    "Key $keyCode"
                }
            }
        }
    }

    fun findConflictingAction(actions: List<CameraAction>?, keyCode: Int, excludeIndex: Int = -1): Pair<Int, CameraAction>? {
        if (actions == null || !isValidPhysicalKey(keyCode)) return null
        for ((idx, action) in actions.withIndex()) {
            if (idx != excludeIndex && action.keyCode == keyCode) {
                return Pair(idx, action)
            }
        }
        return null
    }

    fun processKeyEvent(
        event: KeyEvent,
        actions: List<CameraAction>?,
        onTriggerAction: (CameraAction) -> Unit
    ): Boolean {
        return processKeyEvent(event.action, event.keyCode, event.repeatCount, actions, onTriggerAction)
    }

    fun processKeyEvent(
        action: Int,
        keyCode: Int,
        repeatCount: Int,
        actions: List<CameraAction>?,
        onTriggerAction: (CameraAction) -> Unit
    ): Boolean {
        if (!isValidPhysicalKey(keyCode)) {
            return false
        }
        val matchingAction = actions?.firstOrNull { it.keyCode == keyCode } ?: return false

        when (action) {
            KeyEvent.ACTION_DOWN -> {
                if (repeatCount == 0) {
                    onTriggerAction(matchingAction)
                }
                return true
            }
            KeyEvent.ACTION_UP -> {
                return true
            }
            else -> {
                return true
            }
        }
    }
}
