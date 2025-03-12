package com.example.nfctool.fragments

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.nfctool.databinding.FragmentWriteBinding

class WriteFragment : Fragment() {
    private var _binding: FragmentWriteBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWriteBinding.inflate(inflater, container, false)
        
        binding.btnWrite.setOnClickListener {
            binding.tvStatus.text = "请将NFC标签贴近手机背面..."
        }
        
        return binding.root
    }

    fun handleNfcTag(tag: Tag) {
        val text = binding.etContent.text.toString()
        if (text.isEmpty()) {
            Toast.makeText(context, "请输入要写入的内容", Toast.LENGTH_SHORT).show()
            return
        }

        val ndef = Ndef.get(tag)
        if (ndef == null) {
            Toast.makeText(context, "该标签不支持NDEF格式", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            ndef.connect()
            val ndefRecord = NdefRecord.createTextRecord(null, text)
            val ndefMessage = NdefMessage(arrayOf(ndefRecord))
            
            if (ndef.maxSize < ndefMessage.byteArrayLength) {
                Toast.makeText(context, "数据太大，无法写入", Toast.LENGTH_SHORT).show()
                return
            }

            ndef.writeNdefMessage(ndefMessage)
            binding.tvStatus.text = "写入成功！"
            Toast.makeText(context, "数据写入成功", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            binding.tvStatus.text = "写入失败：${e.message}"
            Toast.makeText(context, "写入失败：${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            try {
                ndef.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 