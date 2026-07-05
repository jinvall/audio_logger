package com.example.noisetelemetry.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.noisetelemetry.databinding.FragmentCameraReviewBinding
import com.example.noisetelemetry.ui.viewmodel.CameraReviewViewModel

class CameraReviewFragment : Fragment() {
    private var _binding: FragmentCameraReviewBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CameraReviewViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCameraReviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnRefreshPreview.setOnClickListener {
            Toast.makeText(requireContext(), "Frame buffer refreshed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
