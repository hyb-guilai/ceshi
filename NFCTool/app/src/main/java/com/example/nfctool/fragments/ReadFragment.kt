package com.example.nfctool.fragments

import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.MifareClassic
import android.nfc.tech.Ndef
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.nfctool.databinding.FragmentReadBinding
import java.io.IOException
import java.security.SecurityException

class ReadFragment : Fragment() {
    private var _binding: FragmentReadBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReadBinding.inflate(inflater, container, false)
        return binding.root
    }

    fun handleNfcTag(tag: Tag) {
        val stringBuilder = StringBuilder()
        
        // 读取基本信息
        stringBuilder.append("NFC卡片ID: ${bytesToHexString(tag.id)}\n\n")
        
        // 读取NDEF数据
        val ndef = Ndef.get(tag)
        if (ndef != null) {
            stringBuilder.append("NDEF容量: ${ndef.maxSize} bytes\n")
            val ndefMessage = ndef.cachedNdefMessage
            if (ndefMessage != null) {
                for (record in ndefMessage.records) {
                    stringBuilder.append("NDEF记录: ${String(record.payload)}\n")
                }
            }
        }
        
        // 读取Mifare卡数据
        val mifare = MifareClassic.get(tag)
        if (mifare != null) {
            try {
                mifare.connect()
                stringBuilder.append("\nMifare卡类型: ${getMifareType(mifare)}\n")
                stringBuilder.append("扇区数量: ${mifare.sectorCount}\n")
                stringBuilder.append("块数量: ${mifare.blockCount}\n")
                mifare.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        binding.tvNfcContent.text = stringBuilder.toString()
    }

    private fun bytesToHexString(bytes: ByteArray): String {
        val sb = StringBuilder()
        for (b in bytes) {
            sb.append(String.format("%02X", b))
        }
        return sb.toString()
    }

    private fun getMifareType(mifare: MifareClassic): String {
        return when (mifare.type) {
            MifareClassic.TYPE_CLASSIC -> "Mifare Classic"
            MifareClassic.TYPE_PLUS -> "Mifare Plus"
            MifareClassic.TYPE_PRO -> "Mifare Pro"
            else -> "Unknown"
        }
    }

    private fun handleError(e: Exception) {
        val errorMessage = when (e) {
            is IOException -> "读取NFC标签时发生错误：${e.message}"
            is SecurityException -> "没有权限访问NFC功能"
            else -> "发生未知错误：${e.message}"
        }
        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 