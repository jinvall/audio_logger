package com.example.noisetelemetry.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.noisetelemetry.R
import com.example.noisetelemetry.databinding.FragmentThresholdConfigBinding
import com.example.noisetelemetry.ui.viewmodel.ThresholdConfigViewModel
import com.example.noisetelemetry.ui.viewmodel.ThresholdConfigViewModelFactory
import kotlinx.coroutines.launch

class ThresholdConfigFragment : Fragment() {
    private var _binding: FragmentThresholdConfigBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ThresholdConfigViewModel by viewModels { ThresholdConfigViewModelFactory(requireActivity().application) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentThresholdConfigBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.profile.collect { profile ->
                    binding.sliderThreshold.value = profile.thresholdDb
                    binding.tvThresholdValue.text = "%.1f dB".format(profile.thresholdDb)
                    binding.etNotes.setText(profile.notes)
                    binding.switchEnabled.isChecked = profile.isEnabled
                }
            }
        }

        binding.sliderThreshold.addOnChangeListener { _, value, _ ->
            viewModel.updateThreshold(value)
        }
        binding.switchEnabled.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setEnabled(isChecked)
        }
        binding.etNotes.doAfterTextChanged {
            viewModel.updateNotes(it?.toString().orEmpty())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
