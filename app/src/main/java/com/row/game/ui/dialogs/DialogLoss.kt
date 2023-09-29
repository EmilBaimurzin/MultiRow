package com.row.game.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.row.game.R
import com.row.game.core.library.ViewBindingDialog
import com.row.game.databinding.DialogLossBinding

class DialogLoss: ViewBindingDialog<DialogLossBinding>(DialogLossBinding::inflate) {
    private val args: DialogLossArgs by navArgs()
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireContext(), R.style.Dialog_No_Border)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog!!.setCancelable(false)
        dialog!!.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                findNavController().popBackStack(R.id.fragmentMain, false, false)
                true
            } else {
                false
            }
        }

        binding.task1.text = "${args.task1}/5"
        binding.task2.text = "${args.task2}/10"
        binding.task3.text = "${args.task3}/8"
        binding.task4.text = "${args.task4}/8"

        binding.menu.setOnClickListener {
            findNavController().popBackStack(R.id.fragmentMain, false, false)
        }

        binding.restart.setOnClickListener {
            findNavController().popBackStack(R.id.fragmentMain, false, false)
            findNavController().navigate(R.id.action_fragmentMain_to_fragmentAncientGame)
        }
    }
}