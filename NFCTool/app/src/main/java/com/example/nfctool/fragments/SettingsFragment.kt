package com.example.nfctool.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.nfctool.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        
        binding.switchAutoRead.setOnCheckedChangeListener { _, isChecked ->
            // 保存自动读取设置
            context?.getSharedPreferences("settings", 0)?.edit()?.
                putBoolean("auto_read", isChecked)?.apply()
        }

        // 加载设置
        val prefs = context?.getSharedPreferences("settings", 0)
        binding.switchAutoRead.isChecked = prefs?.getBoolean("auto_read", true) ?: true

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 