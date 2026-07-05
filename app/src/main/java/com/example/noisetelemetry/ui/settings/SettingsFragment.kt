package com.example.noisetelemetry.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.noisetelemetry.R
import com.example.noisetelemetry.databinding.FragmentSettingsBinding
import com.example.noisetelemetry.ui.viewmodel.SettingsViewModel

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModels { SettingsViewModelFactory(requireActivity().application) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.config.collect { config ->
                    binding.switchOverlayEnabled.isChecked = config.enabled
                    binding.switchShowLocation.isChecked = config.showLocation
                    binding.seekQuality.progress = config.jpegQuality
                    binding.tvQualityValue.text = "${config.jpegQuality}%"
                }
            }
        }

        binding.switchOverlayEnabled.setOnCheckedChangeListener { _, checked ->
            viewModel.setOverlayEnabled(checked)
        }
        binding.switchShowLocation.setOnCheckedChangeListener { _, checked ->
            viewModel.setShowLocation(checked)
        }
        binding.seekQuality.addOnChangeListener { _, value, _ ->
            viewModel.setJpegQuality(value.toInt())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
