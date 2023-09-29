package com.row.game.ui.multi_row

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.row.game.R
import com.row.game.core.library.GameFragment
import com.row.game.databinding.FragmentMultiRowBinding
import com.row.game.domain.multi_row.adapter.MultiRowAdapter
import com.row.game.ui.dialogs.ViewModelCallback

class FragmentMultiRow: GameFragment<FragmentMultiRowBinding>(FragmentMultiRowBinding::inflate) {
    private val viewModel: MultiRowViewModel by viewModels()
    private val callbackViewModel: ViewModelCallback by activityViewModels()
    private lateinit var gameAdapter: MultiRowAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter()

        binding.menu.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.restart.setOnClickListener {
            findNavController().popBackStack()
            findNavController().navigate(R.id.action_fragmentMain_to_fragmentAncientGame)
        }

        callbackViewModel.callback = {
            viewModel.pauseState = false
            viewModel.startTimer()
        }

        binding.pause.setOnClickListener {
            viewModel.pauseState = true
            viewModel.stopTimer()
            findNavController().navigate(R.id.action_fragmentAncientGame_to_dialogPause)
        }

        if (viewModel.gameState && !viewModel.pauseState) {
            viewModel.startTimer()
        }

        viewModel.list.observe(viewLifecycleOwner) {
            gameAdapter.items = it.toMutableList()
            gameAdapter.notifyDataSetChanged()
        }
        viewModel.task1.observe(viewLifecycleOwner) {
            binding.task1.text = "$it/5"
            checkTasks()
        }
        viewModel.task2.observe(viewLifecycleOwner) {
            binding.task2.text = "$it/10"
            checkTasks()
        }
        viewModel.task3.observe(viewLifecycleOwner) {
            binding.task3.text = "$it/8"
            checkTasks()
        }
        viewModel.task4.observe(viewLifecycleOwner) {
            binding.task4.text = "$it/8"
            checkTasks()
        }

        viewModel.timer.observe(viewLifecycleOwner) { totalSecs ->
            val hours = totalSecs / 3600;
            val minutes = (totalSecs % 3600) / 60;
            val seconds = totalSecs % 60;

            binding.timer.text = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }

        viewModel.moves.observe(viewLifecycleOwner) {
            binding.moves.text = it.toString()

            if (it == 0 && viewModel.gameState) {
                viewModel.gameState = false
                viewModel.stopTimer()
                findNavController().navigate(FragmentMultiRowDirections.actionFragmentAncientGameToDialogLoss(
                    viewModel.task1.value!!,
                    viewModel.task2.value!!,
                    viewModel.task3.value!!,
                    viewModel.task4.value!!,
                ))
            }
        }
    }

    private fun checkTasks() {
        if (viewModel.task1.value!! == 5 && viewModel.task2.value!! == 10 && viewModel.task3.value!! == 8 && viewModel.task4.value == 8 && viewModel.gameState) {
            viewModel.gameState = false
            viewModel.stopTimer()
            findNavController().navigate(FragmentMultiRowDirections.actionFragmentAncientGameToDialogWin(viewModel.timer.value!!))
        }
    }

    private fun initAdapter() {
        gameAdapter = MultiRowAdapter {
            if (viewModel.moves.value!! > 0) {
                viewModel.itemClick(it)
            }
        }
        with(binding.gameRV) {
            adapter = gameAdapter
            layoutManager = GridLayoutManager(requireContext(), 6).apply {
                orientation = GridLayoutManager.VERTICAL
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopTimer()
    }
}