package org.staacks.alpharemote.ui.settings

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.staacks.alpharemote.MainActivity
import org.staacks.alpharemote.R
import org.staacks.alpharemote.camera.CameraAction
import org.staacks.alpharemote.camera.CameraActionPreset
import org.staacks.alpharemote.camera.CameraActionTemplateOption
import org.staacks.alpharemote.camera.KeyBindingHelper
import org.staacks.alpharemote.databinding.CameraActionPickerBinding
import kotlin.math.roundToInt

interface CameraActionPickerListener {
    fun onConfirmCameraActionPicker(index: Int, cameraAction: CameraAction, reassignedFromIndex: Int = -1)
    fun onCancelCameraActionPicker()
    fun onDeleteCameraActionPicker(index: Int)
}

class CameraActionPicker : DialogFragment() {

    private var _binding: CameraActionPickerBinding? = null
    private val binding get() = _binding!!

    private var index = -1
    private var reassignedFromIndex = -1
    private var existingActions: ArrayList<CameraAction>? = null
    private var keyCaptureDialog: AlertDialog? = null
    private var conflictDialog: AlertDialog? = null

    val defaultAction = CameraAction(
        false, null, null, null, CameraActionPreset.STOP, null
    )

    private lateinit var cameraAction: MutableStateFlow<CameraAction>

    class SeekBarTimeMap(min: Int, max: Int) {

        private val mapping = generateSequence(min) {
            if (it < 10)
                it + 1
            else if (it < 50)
                it + 5
            else if (it < 300)
                it + 10
            else if (it < 600)
                it + 50
            else
                it + 100
        }.takeWhile { it <= max }.toList()

        fun getMax(): Int {
            return mapping.count() - 1
        }

        fun indexToTime(i: Int): Float {
            return mapping[i] / 10.0f
        }

        fun timeToIndex(t: Float): Int {
            return mapping.indexOf((t * 10.0f).roundToInt())
        }
    }

    val selftimerSeekBarTimeMap = SeekBarTimeMap(10, 600)
    val holdSeekBarTimeMap = SeekBarTimeMap(0, 100)

    companion object {
        const val CAMERA_ACTION_KEY = "cameraAction"
        const val INDEX_KEY = "index"
        const val SHOW_DELETE_KEY = "showDelete"
        const val EXISTING_ACTIONS_KEY = "existingActions"
        const val SAVED_CAMERA_ACTION = "savedCameraAction"
        const val SAVED_REASSIGNED_FROM_INDEX = "savedReassignedFromIndex"

        fun newInstance(
            index: Int,
            cameraAction: CameraAction?,
            showDelete: Boolean,
            existingActions: List<CameraAction>? = null
        ): CameraActionPicker {
            val newInstance = CameraActionPicker()
            val args = Bundle()
            args.putSerializable(CAMERA_ACTION_KEY, cameraAction)
            args.putInt(INDEX_KEY, index)
            args.putBoolean(SHOW_DELETE_KEY, showDelete)
            if (existingActions != null) {
                args.putSerializable(EXISTING_ACTIONS_KEY, ArrayList(existingActions))
            }
            newInstance.setArguments(args)
            return newInstance
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = CameraActionPickerBinding.inflate(layoutInflater)

        index = arguments?.getInt(INDEX_KEY) ?: -1
        @Suppress("UNCHECKED_CAST")
        existingActions = arguments?.getSerializable(EXISTING_ACTIONS_KEY) as? ArrayList<CameraAction>
        val oldAction = arguments?.getSerializable(CAMERA_ACTION_KEY) as? CameraAction
        val savedAction = savedInstanceState?.getSerializable(SAVED_CAMERA_ACTION) as? CameraAction
        val startAction = savedAction ?: oldAction ?: defaultAction
        reassignedFromIndex = savedInstanceState?.getInt(SAVED_REASSIGNED_FROM_INDEX, -1) ?: -1

        val showDelete = arguments?.getBoolean(SHOW_DELETE_KEY) ?: false

        cameraAction = MutableStateFlow(startAction)

        val actionSpinnerAdapter = ArrayAdapter(
            requireActivity(),
            android.R.layout.simple_spinner_item,
            CameraActionPreset.entries.map { getString(it.template.name) }
        ).also{
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.capAction.adapter = actionSpinnerAdapter
        binding.capAction.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val preset = CameraActionPreset.entries[position]
                lifecycleScope.launch {
                    val old = cameraAction.value
                    val opt = preset.template.userOptions
                    cameraAction.emit(old.copy(
                        preset = preset,
                        selftimer = if (opt.contains(CameraActionTemplateOption.SELFTIMER)) old.selftimer else null,
                        duration = if (opt.contains(CameraActionTemplateOption.VARIABLE_DURATION)) old.duration else null,
                        toggle = if (opt.contains(CameraActionTemplateOption.TOGGLE)) old.toggle else false,
                        step = if (opt.contains(CameraActionTemplateOption.ADJUST_SPEED)) old.step ?: 0.5f else null
                    ))
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                lifecycleScope.launch {
                    cameraAction.emit(defaultAction)
                }
            }

        }

        binding.capSelftimerEnable.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                cameraAction.emit(cameraAction.value.copy(
                    selftimer = if (isChecked) (selftimerSeekBarTimeMap.indexToTime(binding.capHold.progress)) else null
                ))
            }
        }
        binding.capSelftimer.max = selftimerSeekBarTimeMap.getMax()
        binding.capSelftimer.setOnSeekBarChangeListener(object: OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                lifecycleScope.launch {
                    cameraAction.emit(
                        cameraAction.value.copy(
                            selftimer = selftimerSeekBarTimeMap.indexToTime(progress)
                        )
                    )
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

        binding.capHoldEnable.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                cameraAction.emit(cameraAction.value.copy(
                    duration = if (isChecked) (holdSeekBarTimeMap.indexToTime(binding.capHold.progress)) else null
                ))
            }
        }
        binding.capHold.max = holdSeekBarTimeMap.getMax()
        binding.capHold.setOnSeekBarChangeListener(object: OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                lifecycleScope.launch {
                    cameraAction.emit(
                        cameraAction.value.copy(
                            duration = holdSeekBarTimeMap.indexToTime(progress)
                        )
                    )
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

        binding.capToggle.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                cameraAction.emit(cameraAction.value.copy(
                    toggle = isChecked
                ))
            }
        }

        binding.capSpeed.setOnSeekBarChangeListener(object: OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                lifecycleScope.launch {
                    cameraAction.emit(
                        cameraAction.value.copy(
                            step = progress / 100.0f
                        )
                    )
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

        binding.capKeyBind.setOnClickListener {
            showKeyCaptureDialog()
        }

        binding.capKeyClear.setOnClickListener {
            reassignedFromIndex = -1
            lifecycleScope.launch {
                cameraAction.emit(cameraAction.value.copy(keyCode = null))
            }
        }

        binding.capCancel.setOnClickListener{
            (parentFragment as? CameraActionPickerListener)?.onCancelCameraActionPicker()
            dismiss()
        }
        binding.capSave.setOnClickListener{
            val action = cameraAction.value
            val options = action.preset.template.userOptions
            val prunedAction = action.copy(
                selftimer = if (options.contains(CameraActionTemplateOption.SELFTIMER)) action.selftimer else null,
                duration = if (options.contains(CameraActionTemplateOption.VARIABLE_DURATION)) action.duration else null,
                toggle = options.contains(CameraActionTemplateOption.TOGGLE) && action.toggle,
                step = if (options.contains(CameraActionTemplateOption.ADJUST_SPEED)) action.step else null,
                keyCode = action.keyCode
            )
            (parentFragment as? CameraActionPickerListener)?.onConfirmCameraActionPicker(
                index, prunedAction, reassignedFromIndex
            )
            dismiss()
        }
        if (showDelete) {
            binding.capDelete.setOnClickListener {
                (parentFragment as? CameraActionPickerListener)?.onDeleteCameraActionPicker(index)
                dismiss()
            }
            binding.capDelete.visibility = VISIBLE
        } else
            binding.capDelete.visibility = GONE

        lifecycleScope.launch {
            cameraAction.collect{
                binding.capIcon.setImageDrawable(it.getIcon(requireContext()))
                binding.capTitle.text = it.getName(requireContext())

                binding.capSelftimerGroup.visibility = if (it.preset.template.userOptions.contains(CameraActionTemplateOption.SELFTIMER)) VISIBLE else GONE
                binding.capHoldGroup.visibility = if (it.preset.template.userOptions.contains(CameraActionTemplateOption.VARIABLE_DURATION)) VISIBLE else GONE
                binding.capToggle.visibility = if (it.preset.template.userOptions.contains(CameraActionTemplateOption.TOGGLE)) VISIBLE else GONE
                binding.capSpeedGroup.visibility = if (it.preset.template.userOptions.contains(CameraActionTemplateOption.ADJUST_SPEED)) VISIBLE else GONE

                binding.capAction.setSelection(it.preset.ordinal)

                binding.capSelftimerEnable.isChecked = (it.selftimer != null)
                if (binding.capSelftimerEnable.isChecked) {
                    binding.capSelftimer.alpha = 1.0f
                    binding.capSelftimer.progress = selftimerSeekBarTimeMap.timeToIndex (it.selftimer ?: 3.0f)
                    binding.capSelftimerSeconds.text = String.format(getString(R.string.seconds_formatted),it.selftimer ?: 3.0f)
                } else {
                    binding.capSelftimer.alpha = 0.5f
                    binding.capSelftimerSeconds.text = "-"
                }
                binding.capHoldEnable.isChecked = (it.duration != null)
                if (binding.capHoldEnable.isChecked) {
                    binding.capHold.alpha = 1.0f
                    binding.capHold.progress = holdSeekBarTimeMap.timeToIndex (it.duration ?: 3.0f)
                    binding.capHoldSeconds.text = String.format(getString(R.string.seconds_formatted),it.duration ?: 3.0f)
                } else {
                    binding.capHold.alpha = 0.5f
                    binding.capHoldSeconds.text = "-"
                }
                binding.capToggle.isChecked = it.toggle
                it.step?.let { step ->
                    binding.capSpeed.progress = (step * 100f).roundToInt()
                }

                if (it.keyCode != null) {
                    binding.capKeyStatus.text = KeyBindingHelper.getKeyDisplayName(it.keyCode)
                    binding.capKeyClear.visibility = VISIBLE
                    binding.capKeyBind.text = getString(R.string.key_binding_change_button)
                } else {
                    binding.capKeyStatus.text = getString(R.string.key_binding_none)
                    binding.capKeyClear.visibility = GONE
                    binding.capKeyBind.text = getString(R.string.key_binding_bind_button)
                }
            }
        }

        return AlertDialog.Builder(requireActivity()).setView(binding.root).create()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (::cameraAction.isInitialized) {
            outState.putSerializable(SAVED_CAMERA_ACTION, cameraAction.value)
        }
        outState.putInt(SAVED_REASSIGNED_FROM_INDEX, reassignedFromIndex)
    }

    private fun showKeyCaptureDialog() {
        keyCaptureDialog?.dismiss()
        MainActivity.isKeyCaptureActive = true
        val captureDialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.key_binding_dialog_title)
            .setMessage(R.string.key_binding_dialog_message)
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .setOnDismissListener {
                MainActivity.isKeyCaptureActive = false
                keyCaptureDialog = null
            }
            .create()

        captureDialog.setOnKeyListener { dialog, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dialog.dismiss()
                    return@setOnKeyListener true
                }
                if (KeyBindingHelper.isSystemReservedKey(keyCode)) {
                    context?.let {
                        Toast.makeText(it, R.string.key_binding_reserved_key_warning, Toast.LENGTH_SHORT).show()
                    }
                    return@setOnKeyListener true
                }
                dialog.dismiss()
                onKeyCaptured(keyCode)
                return@setOnKeyListener true
            } else if (event.action == KeyEvent.ACTION_UP) {
                if (keyCode != KeyEvent.KEYCODE_BACK) {
                    return@setOnKeyListener true
                }
            }
            false
        }

        keyCaptureDialog = captureDialog
        captureDialog.show()
    }

    private fun onKeyCaptured(keyCode: Int) {
        val conflict = KeyBindingHelper.findConflictingAction(existingActions, keyCode, index)
        if (conflict != null) {
            val (conflictingIndex, conflictingAction) = conflict
            val keyName = KeyBindingHelper.getKeyDisplayName(keyCode)
            val actionName = conflictingAction.getName(requireContext())

            conflictDialog?.dismiss()
            conflictDialog = AlertDialog.Builder(requireContext())
                .setTitle(R.string.key_binding_conflict_title)
                .setMessage(getString(R.string.key_binding_conflict_message, keyName, actionName))
                .setPositiveButton(R.string.key_binding_reassign) { _, _ ->
                    reassignedFromIndex = conflictingIndex
                    lifecycleScope.launch {
                        cameraAction.emit(cameraAction.value.copy(keyCode = keyCode))
                    }
                }
                .setNegativeButton(R.string.cancel, null)
                .setOnDismissListener {
                    conflictDialog = null
                }
                .show()
        } else {
            reassignedFromIndex = -1
            lifecycleScope.launch {
                cameraAction.emit(cameraAction.value.copy(keyCode = keyCode))
            }
        }
    }

    override fun onDestroyView() {
        keyCaptureDialog?.dismiss()
        keyCaptureDialog = null
        conflictDialog?.dismiss()
        conflictDialog = null
        MainActivity.isKeyCaptureActive = false
        super.onDestroyView()
        _binding = null
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        MainActivity.isKeyCaptureActive = false
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        (parentFragment as? CameraActionPickerListener)?.onCancelCameraActionPicker()
    }
}