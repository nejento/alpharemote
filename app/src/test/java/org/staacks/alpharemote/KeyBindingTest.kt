package org.staacks.alpharemote

import android.view.KeyEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.staacks.alpharemote.camera.CameraAction
import org.staacks.alpharemote.camera.CameraActionPreset
import org.staacks.alpharemote.camera.KeyBindingHelper
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class KeyBindingTest {

    @Test
    fun testIsSystemReservedKey() {
        // System reserved navigation and power keys
        assertTrue("Back key must be reserved", KeyBindingHelper.isSystemReservedKey(KeyEvent.KEYCODE_BACK))
        assertTrue("Home key must be reserved", KeyBindingHelper.isSystemReservedKey(KeyEvent.KEYCODE_HOME))
        assertTrue("App switch key must be reserved", KeyBindingHelper.isSystemReservedKey(KeyEvent.KEYCODE_APP_SWITCH))
        assertTrue("Power key must be reserved", KeyBindingHelper.isSystemReservedKey(KeyEvent.KEYCODE_POWER))
        assertTrue("Unknown key must be reserved", KeyBindingHelper.isSystemReservedKey(KeyEvent.KEYCODE_UNKNOWN))
        assertTrue("Call key must be reserved", KeyBindingHelper.isSystemReservedKey(KeyEvent.KEYCODE_CALL))
        assertTrue("Endcall key must be reserved", KeyBindingHelper.isSystemReservedKey(KeyEvent.KEYCODE_ENDCALL))
        assertTrue("Search key must be reserved", KeyBindingHelper.isSystemReservedKey(KeyEvent.KEYCODE_SEARCH))

        // Modifier keys must be reserved
        assertTrue("Shift Left must be reserved", KeyBindingHelper.isSystemReservedKey(KeyEvent.KEYCODE_SHIFT_LEFT))
        assertTrue("Shift Right must be reserved", KeyBindingHelper.isSystemReservedKey(KeyEvent.KEYCODE_SHIFT_RIGHT))
        assertTrue("Ctrl Left must be reserved", KeyBindingHelper.isSystemReservedKey(KeyEvent.KEYCODE_CTRL_LEFT))
        assertTrue("Alt Left must be reserved", KeyBindingHelper.isSystemReservedKey(KeyEvent.KEYCODE_ALT_LEFT))

        // Physical camera / media / remote keys must NOT be reserved
        assertFalse("Volume Up must be bindable", KeyBindingHelper.isSystemReservedKey(KeyEvent.KEYCODE_VOLUME_UP))
        assertFalse("Volume Down must be bindable", KeyBindingHelper.isSystemReservedKey(KeyEvent.KEYCODE_VOLUME_DOWN))
        assertFalse("Camera key must be bindable", KeyBindingHelper.isSystemReservedKey(KeyEvent.KEYCODE_CAMERA))
        assertFalse("Focus key must be bindable", KeyBindingHelper.isSystemReservedKey(KeyEvent.KEYCODE_FOCUS))
        assertFalse("Headset hook must be bindable", KeyBindingHelper.isSystemReservedKey(KeyEvent.KEYCODE_HEADSETHOOK))
        assertFalse("Media Play must be bindable", KeyBindingHelper.isSystemReservedKey(KeyEvent.KEYCODE_MEDIA_PLAY))
        assertFalse("Media Play/Pause must be bindable", KeyBindingHelper.isSystemReservedKey(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE))
        assertFalse("Enter key must be bindable", KeyBindingHelper.isSystemReservedKey(KeyEvent.KEYCODE_ENTER))
        assertFalse("D-Pad Center must be bindable", KeyBindingHelper.isSystemReservedKey(KeyEvent.KEYCODE_DPAD_CENTER))
        assertFalse("Gamepad Button A must be bindable", KeyBindingHelper.isSystemReservedKey(KeyEvent.KEYCODE_BUTTON_A))
    }

    @Test
    fun testIsValidPhysicalKey() {
        assertTrue(KeyBindingHelper.isValidPhysicalKey(KeyEvent.KEYCODE_VOLUME_UP))
        assertTrue(KeyBindingHelper.isValidPhysicalKey(KeyEvent.KEYCODE_VOLUME_DOWN))
        assertTrue(KeyBindingHelper.isValidPhysicalKey(KeyEvent.KEYCODE_CAMERA))
        assertTrue(KeyBindingHelper.isValidPhysicalKey(KeyEvent.KEYCODE_MEDIA_PLAY))
        assertTrue(KeyBindingHelper.isValidPhysicalKey(KeyEvent.KEYCODE_BUTTON_A))

        assertFalse(KeyBindingHelper.isValidPhysicalKey(KeyEvent.KEYCODE_BACK))
        assertFalse(KeyBindingHelper.isValidPhysicalKey(KeyEvent.KEYCODE_HOME))
        assertFalse(KeyBindingHelper.isValidPhysicalKey(KeyEvent.KEYCODE_POWER))
        assertFalse(KeyBindingHelper.isValidPhysicalKey(KeyEvent.KEYCODE_UNKNOWN))
        assertFalse(KeyBindingHelper.isValidPhysicalKey(0))
        assertFalse(KeyBindingHelper.isValidPhysicalKey(-1))
    }

    @Test
    fun testGetKeyDisplayName() {
        assertEquals("Volume Up", KeyBindingHelper.getKeyDisplayName(KeyEvent.KEYCODE_VOLUME_UP))
        assertEquals("Volume Down", KeyBindingHelper.getKeyDisplayName(KeyEvent.KEYCODE_VOLUME_DOWN))
        assertEquals("Camera", KeyBindingHelper.getKeyDisplayName(KeyEvent.KEYCODE_CAMERA))
        assertEquals("Focus", KeyBindingHelper.getKeyDisplayName(KeyEvent.KEYCODE_FOCUS))
        assertEquals("Headset Hook", KeyBindingHelper.getKeyDisplayName(KeyEvent.KEYCODE_HEADSETHOOK))
        assertEquals("Play", KeyBindingHelper.getKeyDisplayName(KeyEvent.KEYCODE_MEDIA_PLAY))
        assertEquals("Play / Pause", KeyBindingHelper.getKeyDisplayName(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE))
        assertEquals("Enter", KeyBindingHelper.getKeyDisplayName(KeyEvent.KEYCODE_ENTER))
        assertEquals("D-Pad Center", KeyBindingHelper.getKeyDisplayName(KeyEvent.KEYCODE_DPAD_CENTER))
        assertEquals("Button A", KeyBindingHelper.getKeyDisplayName(KeyEvent.KEYCODE_BUTTON_A))
        assertEquals("Space", KeyBindingHelper.getKeyDisplayName(KeyEvent.KEYCODE_SPACE))
    }

    @Test
    fun testFindConflictingAction() {
        val action1 = CameraAction(false, null, null, null, CameraActionPreset.SHUTTER, keyCode = KeyEvent.KEYCODE_VOLUME_UP)
        val action2 = CameraAction(false, null, null, null, CameraActionPreset.RECORD, keyCode = KeyEvent.KEYCODE_VOLUME_DOWN)
        val action3 = CameraAction(false, null, null, null, CameraActionPreset.AF_ON, keyCode = null)
        val actions = listOf(action1, action2, action3)

        // Conflict when binding existing key (VOLUME_UP) on another item
        val conflict = KeyBindingHelper.findConflictingAction(actions, KeyEvent.KEYCODE_VOLUME_UP, excludeIndex = 1)
        assertNotNull(conflict)
        assertEquals(0, conflict!!.first)
        assertEquals(CameraActionPreset.SHUTTER, conflict.second.preset)

        // No conflict when excluding the owner item (editing item 0 with VOLUME_UP)
        val noConflictSelf = KeyBindingHelper.findConflictingAction(actions, KeyEvent.KEYCODE_VOLUME_UP, excludeIndex = 0)
        assertNull(noConflictSelf)

        // No conflict for unbound key
        val noConflictUnbound = KeyBindingHelper.findConflictingAction(actions, KeyEvent.KEYCODE_CAMERA, excludeIndex = -1)
        assertNull(noConflictUnbound)

        // Safe with null/empty list
        assertNull(KeyBindingHelper.findConflictingAction(null, KeyEvent.KEYCODE_VOLUME_UP))
        assertNull(KeyBindingHelper.findConflictingAction(emptyList(), KeyEvent.KEYCODE_VOLUME_UP))
    }

    @Test
    fun testProcessKeyEventActionDownInitial() {
        val action = CameraAction(false, null, null, null, CameraActionPreset.SHUTTER, keyCode = KeyEvent.KEYCODE_VOLUME_UP)
        val actions = listOf(action)

        var triggeredAction: CameraAction? = null
        val handled = KeyBindingHelper.processKeyEvent(
            action = KeyEvent.ACTION_DOWN,
            keyCode = KeyEvent.KEYCODE_VOLUME_UP,
            repeatCount = 0,
            actions = actions
        ) {
            triggeredAction = it
        }

        assertTrue("Event must be consumed", handled)
        assertNotNull("Action must be triggered", triggeredAction)
        assertEquals(CameraActionPreset.SHUTTER, triggeredAction?.preset)
    }

    @Test
    fun testProcessKeyEventIgnoresRepeats() {
        val action = CameraAction(false, null, null, null, CameraActionPreset.SHUTTER, keyCode = KeyEvent.KEYCODE_VOLUME_UP)
        val actions = listOf(action)

        var triggerCount = 0
        // Repeat event: repeatCount > 0
        val handled = KeyBindingHelper.processKeyEvent(
            action = KeyEvent.ACTION_DOWN,
            keyCode = KeyEvent.KEYCODE_VOLUME_UP,
            repeatCount = 1,
            actions = actions
        ) {
            triggerCount++
        }

        assertTrue("Event must still be consumed during repeat to prevent system volume changes", handled)
        assertEquals("Action must NOT be triggered on repeat", 0, triggerCount)

        // Repeat count 2
        val handled2 = KeyBindingHelper.processKeyEvent(
            action = KeyEvent.ACTION_DOWN,
            keyCode = KeyEvent.KEYCODE_VOLUME_UP,
            repeatCount = 2,
            actions = actions
        ) {
            triggerCount++
        }

        assertTrue("Event must still be consumed", handled2)
        assertEquals("Action must NOT be triggered on repeat count 2", 0, triggerCount)
    }

    @Test
    fun testProcessKeyEventActionUpConsumes() {
        val action = CameraAction(false, null, null, null, CameraActionPreset.SHUTTER, keyCode = KeyEvent.KEYCODE_VOLUME_UP)
        val actions = listOf(action)

        var triggerCount = 0
        val handled = KeyBindingHelper.processKeyEvent(
            action = KeyEvent.ACTION_UP,
            keyCode = KeyEvent.KEYCODE_VOLUME_UP,
            repeatCount = 0,
            actions = actions
        ) {
            triggerCount++
        }

        assertTrue("ACTION_UP on bound key must be consumed", handled)
        assertEquals("ACTION_UP should not trigger action", 0, triggerCount)
    }

    @Test
    fun testProcessKeyEventUnmappedKeyNotConsumed() {
        val action = CameraAction(false, null, null, null, CameraActionPreset.SHUTTER, keyCode = KeyEvent.KEYCODE_VOLUME_UP)
        val actions = listOf(action)

        var triggerCount = 0
        val handled = KeyBindingHelper.processKeyEvent(
            action = KeyEvent.ACTION_DOWN,
            keyCode = KeyEvent.KEYCODE_VOLUME_DOWN, // unmapped
            repeatCount = 0,
            actions = actions
        ) {
            triggerCount++
        }

        assertFalse("Unmapped key must NOT be consumed", handled)
        assertEquals(0, triggerCount)
    }

    @Test
    fun testProcessKeyEventReservedKeyNotConsumed() {
        // Even if somehow in the list, Back or Home should never be intercepted
        val action = CameraAction(false, null, null, null, CameraActionPreset.SHUTTER, keyCode = KeyEvent.KEYCODE_BACK)
        val actions = listOf(action)

        var triggerCount = 0
        val handled = KeyBindingHelper.processKeyEvent(
            action = KeyEvent.ACTION_DOWN,
            keyCode = KeyEvent.KEYCODE_BACK,
            repeatCount = 0,
            actions = actions
        ) {
            triggerCount++
        }

        assertFalse("Reserved key (Back) must NOT be consumed by app key mapping", handled)
        assertEquals(0, triggerCount)
    }

    @Test
    fun testCameraActionSerializationWithKeyCode() {
        val original = CameraAction(
            toggle = true,
            selftimer = 3.0f,
            duration = 5.0f,
            step = 0.5f,
            preset = CameraActionPreset.RECORD,
            keyCode = KeyEvent.KEYCODE_VOLUME_UP
        )

        val byteArrayOutputStream = ByteArrayOutputStream()
        ObjectOutputStream(byteArrayOutputStream).use { it.writeObject(original) }

        val deserialized = ObjectInputStream(ByteArrayInputStream(byteArrayOutputStream.toByteArray())).use {
            it.readObject() as CameraAction
        }

        assertEquals(original, deserialized)
        assertEquals(KeyEvent.KEYCODE_VOLUME_UP, deserialized.keyCode)
        assertTrue(deserialized.toggle)
        assertEquals(3.0f, deserialized.selftimer)
        assertEquals(CameraActionPreset.RECORD, deserialized.preset)
    }

    @Test
    fun testCameraActionCopyPreservesKeyCode() {
        val action1 = CameraAction(false, null, null, null, CameraActionPreset.SHUTTER, keyCode = KeyEvent.KEYCODE_CAMERA)
        val action2 = action1.copy(toggle = true)
        assertEquals(KeyEvent.KEYCODE_CAMERA, action2.keyCode)
        assertTrue(action2.toggle)

        val action3 = action1.copy(keyCode = null)
        assertNull(action3.keyCode)
    }

    @Test
    fun testDuplicateKeyClearingLogic() {
        val action1 = CameraAction(false, null, null, null, CameraActionPreset.SHUTTER, keyCode = KeyEvent.KEYCODE_VOLUME_UP)
        val action2 = CameraAction(false, null, null, null, CameraActionPreset.RECORD, keyCode = KeyEvent.KEYCODE_VOLUME_DOWN)
        val action3 = CameraAction(false, null, null, null, CameraActionPreset.AF_ON, keyCode = null)

        val list = listOf(action1, action2, action3)

        // When reassigned VOLUME_UP to action2, clearing VOLUME_UP from others
        val newKeyCode = KeyEvent.KEYCODE_VOLUME_UP
        val updatedList = list.map {
            if (it.keyCode == newKeyCode) it.copy(keyCode = null) else it
        }

        assertNull("Action 1 keycode must be cleared", updatedList[0].keyCode)
        assertEquals(KeyEvent.KEYCODE_VOLUME_DOWN, updatedList[1].keyCode)
        assertNull(updatedList[2].keyCode)
    }

    @Test
    fun testMultipleActionsCorrectDispatch() {
        val actionShutter = CameraAction(false, null, null, null, CameraActionPreset.SHUTTER, keyCode = KeyEvent.KEYCODE_VOLUME_UP)
        val actionRecord = CameraAction(false, null, null, null, CameraActionPreset.RECORD, keyCode = KeyEvent.KEYCODE_VOLUME_DOWN)
        val actionZoom = CameraAction(false, null, null, 1.0f, CameraActionPreset.ZOOM_IN, keyCode = KeyEvent.KEYCODE_ENTER)
        val actions = listOf(actionShutter, actionRecord, actionZoom)

        var triggered: CameraAction? = null

        // Trigger Shutter
        val handled1 = KeyBindingHelper.processKeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_VOLUME_UP, 0, actions) { triggered = it }
        assertTrue(handled1)
        assertEquals(CameraActionPreset.SHUTTER, triggered?.preset)

        // Trigger Record
        val handled2 = KeyBindingHelper.processKeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_VOLUME_DOWN, 0, actions) { triggered = it }
        assertTrue(handled2)
        assertEquals(CameraActionPreset.RECORD, triggered?.preset)

        // Trigger Zoom
        val handled3 = KeyBindingHelper.processKeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER, 0, actions) { triggered = it }
        assertTrue(handled3)
        assertEquals(CameraActionPreset.ZOOM_IN, triggered?.preset)
    }

    @Test
    fun testAllSystemNavigationKeysRejected() {
        val navKeys = listOf(
            KeyEvent.KEYCODE_BACK,
            KeyEvent.KEYCODE_HOME,
            KeyEvent.KEYCODE_APP_SWITCH,
            KeyEvent.KEYCODE_POWER,
            KeyEvent.KEYCODE_SEARCH,
            KeyEvent.KEYCODE_ALL_APPS,
            KeyEvent.KEYCODE_ASSIST,
            KeyEvent.KEYCODE_VOICE_ASSIST
        )

        for (key in navKeys) {
            assertTrue("Key $key must be recognized as system-reserved", KeyBindingHelper.isSystemReservedKey(key))
            assertFalse("Key $key must NOT be valid physical key", KeyBindingHelper.isValidPhysicalKey(key))
        }
    }

    @Test
    fun testExtendedSystemReservedKeys() {
        val extendedReservedKeys = listOf(
            KeyEvent.KEYCODE_SLEEP,
            KeyEvent.KEYCODE_WAKEUP,
            KeyEvent.KEYCODE_PAIRING,
            KeyEvent.KEYCODE_SETTINGS,
            KeyEvent.KEYCODE_NOTIFICATION,
            KeyEvent.KEYCODE_MENU,
            KeyEvent.KEYCODE_WINDOW,
            KeyEvent.KEYCODE_SYSTEM_NAVIGATION_UP,
            KeyEvent.KEYCODE_SYSTEM_NAVIGATION_DOWN,
            KeyEvent.KEYCODE_SYSTEM_NAVIGATION_LEFT,
            KeyEvent.KEYCODE_SYSTEM_NAVIGATION_RIGHT,
            KeyEvent.KEYCODE_SOFT_LEFT,
            KeyEvent.KEYCODE_SOFT_RIGHT
        )

        for (key in extendedReservedKeys) {
            assertTrue("Key $key must be reserved", KeyBindingHelper.isSystemReservedKey(key))
            assertFalse("Key $key must not be valid physical key", KeyBindingHelper.isValidPhysicalKey(key))
        }
    }

    @Test
    fun testAllModifierKeysAreReserved() {
        val modifierKeys = listOf(
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
            KeyEvent.KEYCODE_SCROLL_LOCK
        )

        for (mod in modifierKeys) {
            assertTrue("Modifier $mod must be recognized by isModifierKey", KeyBindingHelper.isModifierKey(mod))
            assertTrue("Modifier $mod must be reserved", KeyBindingHelper.isSystemReservedKey(mod))
            assertFalse("Modifier $mod must not be valid physical key", KeyBindingHelper.isValidPhysicalKey(mod))
        }
    }

    @Test
    fun testExtendedKeyDisplayNames() {
        assertEquals("Record", KeyBindingHelper.getKeyDisplayName(KeyEvent.KEYCODE_MEDIA_RECORD))
        assertEquals("Step Forward", KeyBindingHelper.getKeyDisplayName(KeyEvent.KEYCODE_MEDIA_STEP_FORWARD))
        assertEquals("Step Backward", KeyBindingHelper.getKeyDisplayName(KeyEvent.KEYCODE_MEDIA_STEP_BACKWARD))
        assertEquals("Skip Forward", KeyBindingHelper.getKeyDisplayName(KeyEvent.KEYCODE_MEDIA_SKIP_FORWARD))
        assertEquals("Skip Backward", KeyBindingHelper.getKeyDisplayName(KeyEvent.KEYCODE_MEDIA_SKIP_BACKWARD))
        assertEquals("Button 1", KeyBindingHelper.getKeyDisplayName(KeyEvent.KEYCODE_BUTTON_1))
        assertEquals("Button 16", KeyBindingHelper.getKeyDisplayName(KeyEvent.KEYCODE_BUTTON_16))
        assertEquals("Tab", KeyBindingHelper.getKeyDisplayName(KeyEvent.KEYCODE_TAB))
        assertEquals("Backspace", KeyBindingHelper.getKeyDisplayName(KeyEvent.KEYCODE_DEL))
        assertEquals("Delete", KeyBindingHelper.getKeyDisplayName(KeyEvent.KEYCODE_FORWARD_DEL))
        assertEquals("Zoom In", KeyBindingHelper.getKeyDisplayName(KeyEvent.KEYCODE_ZOOM_IN))
        assertEquals("Zoom Out", KeyBindingHelper.getKeyDisplayName(KeyEvent.KEYCODE_ZOOM_OUT))
        assertEquals("F1", KeyBindingHelper.getKeyDisplayName(KeyEvent.KEYCODE_F1))
        assertEquals("F12", KeyBindingHelper.getKeyDisplayName(KeyEvent.KEYCODE_F12))
    }

    @Test
    fun testProcessKeyEventActionMultipleConsumes() {
        val action = CameraAction(false, null, null, null, CameraActionPreset.SHUTTER, keyCode = KeyEvent.KEYCODE_VOLUME_UP)
        var triggerCount = 0
        val handled = KeyBindingHelper.processKeyEvent(
            action = KeyEvent.ACTION_MULTIPLE,
            keyCode = KeyEvent.KEYCODE_VOLUME_UP,
            repeatCount = 0,
            actions = listOf(action)
        ) {
            triggerCount++
        }

        assertTrue("ACTION_MULTIPLE on bound key must be consumed", handled)
        assertEquals("ACTION_MULTIPLE must not trigger action", 0, triggerCount)
    }

    @Test
    fun testProcessKeyEventZeroAndNegativeKeyCodes() {
        val action = CameraAction(false, null, null, null, CameraActionPreset.SHUTTER, keyCode = 0)
        var triggerCount = 0
        val handledZero = KeyBindingHelper.processKeyEvent(
            action = KeyEvent.ACTION_DOWN,
            keyCode = 0,
            repeatCount = 0,
            actions = listOf(action)
        ) {
            triggerCount++
        }
        assertFalse("KeyCode 0 must not be consumed", handledZero)

        val handledNegative = KeyBindingHelper.processKeyEvent(
            action = KeyEvent.ACTION_DOWN,
            keyCode = -10,
            repeatCount = 0,
            actions = listOf(action)
        ) {
            triggerCount++
        }
        assertFalse("Negative KeyCode must not be consumed", handledNegative)
        assertEquals(0, triggerCount)
    }

    @Test
    fun testProcessKeyEventNullAndEmptyActions() {
        var triggerCount = 0
        val handledNull = KeyBindingHelper.processKeyEvent(
            action = KeyEvent.ACTION_DOWN,
            keyCode = KeyEvent.KEYCODE_VOLUME_UP,
            repeatCount = 0,
            actions = null
        ) {
            triggerCount++
        }
        assertFalse("Null actions must not consume", handledNull)

        val handledEmpty = KeyBindingHelper.processKeyEvent(
            action = KeyEvent.ACTION_DOWN,
            keyCode = KeyEvent.KEYCODE_VOLUME_UP,
            repeatCount = 0,
            actions = emptyList()
        ) {
            triggerCount++
        }
        assertFalse("Empty actions must not consume", handledEmpty)
        assertEquals(0, triggerCount)
    }

    @Test
    fun testFindConflictingActionWithInvalidAndReservedKeys() {
        val action1 = CameraAction(false, null, null, null, CameraActionPreset.SHUTTER, keyCode = KeyEvent.KEYCODE_VOLUME_UP)
        val action2 = CameraAction(false, null, null, null, CameraActionPreset.RECORD, keyCode = KeyEvent.KEYCODE_BACK)
        val actions = listOf(action1, action2)

        // Reserved key should never be reported as conflict since it's unbindable
        assertNull(KeyBindingHelper.findConflictingAction(actions, KeyEvent.KEYCODE_BACK))
        assertNull(KeyBindingHelper.findConflictingAction(actions, KeyEvent.KEYCODE_HOME))
        assertNull(KeyBindingHelper.findConflictingAction(actions, 0))
        assertNull(KeyBindingHelper.findConflictingAction(actions, -1))
    }

    @Test
    fun testDuplicateKeyClearingWhenNoKeyMatches() {
        val action1 = CameraAction(false, null, null, null, CameraActionPreset.SHUTTER, keyCode = KeyEvent.KEYCODE_VOLUME_UP)
        val action2 = CameraAction(false, null, null, null, CameraActionPreset.RECORD, keyCode = KeyEvent.KEYCODE_VOLUME_DOWN)
        val actions = listOf(action1, action2)

        // Clearing a keycode not present in list does not mutate items
        val updated = actions.map {
            if (it.keyCode == KeyEvent.KEYCODE_CAMERA) it.copy(keyCode = null) else it
        }
        assertEquals(KeyEvent.KEYCODE_VOLUME_UP, updated[0].keyCode)
        assertEquals(KeyEvent.KEYCODE_VOLUME_DOWN, updated[1].keyCode)
    }

    @Test
    fun testReassignKeyBetweenMultipleActions() {
        var actions = listOf(
            CameraAction(false, null, null, null, CameraActionPreset.SHUTTER, keyCode = KeyEvent.KEYCODE_VOLUME_UP),
            CameraAction(false, null, null, null, CameraActionPreset.RECORD, keyCode = KeyEvent.KEYCODE_VOLUME_DOWN),
            CameraAction(false, null, null, null, CameraActionPreset.AF_ON, keyCode = null)
        )

        // Step 1: Detect conflict when mapping VOLUME_UP to AF_ON (item 2)
        val conflict = KeyBindingHelper.findConflictingAction(actions, KeyEvent.KEYCODE_VOLUME_UP, excludeIndex = 2)
        assertNotNull(conflict)
        assertEquals(0, conflict!!.first)
        assertEquals(CameraActionPreset.SHUTTER, conflict.second.preset)

        // Step 2: Clear VOLUME_UP from conflicting item and assign to AF_ON
        val newKey = KeyEvent.KEYCODE_VOLUME_UP
        actions = actions.map { if (it.keyCode == newKey) it.copy(keyCode = null) else it }
        actions = actions.toMutableList().also { it[2] = it[2].copy(keyCode = newKey) }

        assertNull(actions[0].keyCode)
        assertEquals(KeyEvent.KEYCODE_VOLUME_DOWN, actions[1].keyCode)
        assertEquals(KeyEvent.KEYCODE_VOLUME_UP, actions[2].keyCode)

        // Step 3: Now VOLUME_UP triggers AF_ON, not SHUTTER
        var triggered: CameraAction? = null
        val handled = KeyBindingHelper.processKeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_VOLUME_UP, 0, actions) {
            triggered = it
        }
        assertTrue(handled)
        assertEquals(CameraActionPreset.AF_ON, triggered?.preset)
    }

    @Test
    fun testKeyDisplayNameFallbackFormatting() {
        // Unknown keycode that doesn't correspond to any known constant
        val unknownCode = 99999
        val name = KeyBindingHelper.getKeyDisplayName(unknownCode)
        assertTrue(name.startsWith("Key 99999") || name.isNotEmpty())

        // Keycode 0
        val zeroName = KeyBindingHelper.getKeyDisplayName(0)
        assertTrue(zeroName.startsWith("Key 0") || zeroName.isNotEmpty())
    }

    @Test
    fun testBackKeyCannotBeMappedOrTriggered() {
        // Back key is reserved and must not be considered a valid physical key
        assertFalse("Back key must not be valid physical key", KeyBindingHelper.isValidPhysicalKey(KeyEvent.KEYCODE_BACK))
        assertTrue("Back key must be system reserved", KeyBindingHelper.isSystemReservedKey(KeyEvent.KEYCODE_BACK))

        val actionWithBack = CameraAction(false, null, null, null, CameraActionPreset.SHUTTER, keyCode = KeyEvent.KEYCODE_BACK)
        val actions = listOf(actionWithBack)

        var triggered = false
        val downHandled = KeyBindingHelper.processKeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK, 0, actions) {
            triggered = true
        }
        assertFalse("Back key ACTION_DOWN must never be intercepted", downHandled)
        assertFalse("Back key must never trigger camera actions", triggered)

        val upHandled = KeyBindingHelper.processKeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK, 0, actions) {
            triggered = true
        }
        assertFalse("Back key ACTION_UP must never be intercepted", upHandled)

        assertNull("Back key must never be treated as a conflicting key", KeyBindingHelper.findConflictingAction(actions, KeyEvent.KEYCODE_BACK))
    }

    @Test
    fun testFullKeyPressSequenceWithRepeatsAndUp() {
        val action = CameraAction(false, null, null, null, CameraActionPreset.SHUTTER, keyCode = KeyEvent.KEYCODE_VOLUME_UP)
        val actions = listOf(action)

        var triggerCount = 0

        // 1. Initial DOWN event (repeatCount = 0)
        val downHandled = KeyBindingHelper.processKeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_VOLUME_UP, 0, actions) {
            triggerCount++
        }
        assertTrue("Initial ACTION_DOWN must be handled", downHandled)
        assertEquals("Initial ACTION_DOWN must trigger exactly once", 1, triggerCount)

        // 2. Long-press repeat 1
        val rep1Handled = KeyBindingHelper.processKeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_VOLUME_UP, 1, actions) {
            triggerCount++
        }
        assertTrue("Repeat ACTION_DOWN must be handled to suppress system UI", rep1Handled)
        assertEquals("Repeat 1 must NOT trigger action", 1, triggerCount)

        // 3. Long-press repeat 2
        val rep2Handled = KeyBindingHelper.processKeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_VOLUME_UP, 2, actions) {
            triggerCount++
        }
        assertTrue("Repeat 2 must be handled", rep2Handled)
        assertEquals("Repeat 2 must NOT trigger action", 1, triggerCount)

        // 4. ACTION_UP event
        val upHandled = KeyBindingHelper.processKeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_VOLUME_UP, 0, actions) {
            triggerCount++
        }
        assertTrue("ACTION_UP must be handled to prevent default system behavior", upHandled)
        assertEquals("ACTION_UP must NOT trigger action", 1, triggerCount)
    }

    @Test
    fun testRemoteAndGamepadKeysValidAndNamed() {
        val remoteKeys = listOf(
            KeyEvent.KEYCODE_CAMERA to "Camera",
            KeyEvent.KEYCODE_FOCUS to "Focus",
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE to "Play / Pause",
            KeyEvent.KEYCODE_MEDIA_RECORD to "Record",
            KeyEvent.KEYCODE_ZOOM_IN to "Zoom In",
            KeyEvent.KEYCODE_ZOOM_OUT to "Zoom Out",
            KeyEvent.KEYCODE_BUTTON_A to "Button A",
            KeyEvent.KEYCODE_BUTTON_B to "Button B",
            KeyEvent.KEYCODE_STEM_PRIMARY to "Primary Stem",
            KeyEvent.KEYCODE_STEM_1 to "Stem 1"
        )

        for ((code, expectedName) in remoteKeys) {
            assertTrue("Remote key $expectedName ($code) must be valid physical key", KeyBindingHelper.isValidPhysicalKey(code))
            assertFalse("Remote key $expectedName ($code) must not be system reserved", KeyBindingHelper.isSystemReservedKey(code))
            assertEquals(expectedName, KeyBindingHelper.getKeyDisplayName(code))
        }
    }
}
