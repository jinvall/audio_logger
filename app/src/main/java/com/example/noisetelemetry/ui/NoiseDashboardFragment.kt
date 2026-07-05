package com.example.noisetelemetry.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.noisetelemetry.R
import com.example.noisetelemetry.databinding.FragmentNoiseDashboardBinding
import com.example.noisetelemetry.ui.viewmodel.NoiseDashboardViewModel
import com.example.noisetelemetry.ui.viewmodel.NoiseDashboardViewModelFactory
import kotlinx.coroutines.launch

class NoiseDashboardFragment : Fragment() {
    private var _binding: FragmentNoiseDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NoiseDashboardViewModel by viewModels { NoiseDashboardViewModelFactory(requireActivity().application) }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (!allGranted) {
            Toast.makeText(requireContext(), getString(R.string.permission_required), Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNoiseDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestPermissions()

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.rmsDb.collect { rms ->
                        binding.tvRmsLevel.text = "%.1f dB".format(rms)
                        val progress = (rms + 96).toInt().coerceIn(0, 96)
                        binding.progressRms.progress = progress
                        binding.progressRms.max = 96
                    }
                }
                launch {
                    viewModel.triggerCount.collect { count ->
                        binding.tvTriggerCount.text = "Triggers: $count"
                    }
                }
                launch {
                    viewModel.isAudioRunning.collect { running ->
                        binding.btnStartAudio.isEnabled = !running
                        binding.btnStopAudio.isEnabled = running
                    }
                }
                launch {
                    viewModel.isCameraRunning.collect { running ->
                        binding.btnStartCamera.isEnabled = !running
                        binding.btnStopCamera.isEnabled = running
                    }
                }
            }
        }

        binding.btnStartAudio.setOnClickListener { viewModel.startAudio() }
        binding.btnStopAudio.setOnClickListener { viewModel.stopAudio() }
        binding.btnStartCamera.setOnClickListener { viewModel.startCamera(viewLifecycleOwner) }
        binding.btnStopCamera.setOnClickListener { viewModel.stopCamera() }
        binding.btnClearBuffers.setOnClickListener { viewModel.clearBuffers() }
    }

    private fun requestPermissions() {
        val required = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val missing = required.filter {
            ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            permissionLauncher.launch(missing.toTypedArray())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
