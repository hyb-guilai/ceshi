package com.example.nfctool.fragments

import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.MifareClassic
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.DiffUtil
import java.util.*

class EmulateFragment : Fragment() {
    private var _binding: FragmentEmulateBinding? = null
    private val binding get() = _binding!!
    private val savedCards = mutableListOf<CardInfo>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmulateBinding.inflate(inflater, container, false)

        // 初始化RecyclerView
        binding.rvCards.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = CardListAdapter()
        }

        binding.btnReadCard.setOnClickListener {
            binding.tvStatus.text = "请将门禁卡贴近手机背面..."
        }

        binding.btnEmulate.setOnClickListener {
            if (savedCards.isEmpty()) {
                Toast.makeText(context, "请先读取门禁卡", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startEmulation(savedCards[0])
        }

        return binding.root
    }

    fun handleNfcTag(tag: Tag) {
        val mifare = MifareClassic.get(tag)
        if (mifare == null) {
            Toast.makeText(context, "不支持的卡片类型", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            mifare.connect()
            val cardInfo = readCardData(mifare)
            savedCards.add(cardInfo)
            binding.tvStatus.text = "卡片读取成功！\n卡片ID: ${bytesToHexString(tag.id)}"
            updateCardList()
        } catch (e: Exception) {
            binding.tvStatus.text = "读取失败：${e.message}"
        } finally {
            try {
                mifare.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun readCardData(mifare: MifareClassic): CardInfo {
        val cardData = mutableMapOf<Int, ByteArray>()
        for (sector in 0 until mifare.sectorCount) {
            if (mifare.authenticateSectorWithKeyA(sector, MifareClassic.KEY_DEFAULT)) {
                val firstBlock = mifare.sectorToBlock(sector)
                val blockCount = mifare.getBlockCountInSector(sector)
                for (i in 0 until blockCount) {
                    val blockIndex = firstBlock + i
                    cardData[blockIndex] = mifare.readBlock(blockIndex)
                }
            }
        }
        return CardInfo(mifare.tag.id, cardData)
    }

    private fun startEmulation(cardInfo: CardInfo) {
        // 注意：实际的门禁卡模拟需要硬件支持
        Toast.makeText(context, "开始模拟门禁卡...", Toast.LENGTH_SHORT).show()
        binding.tvStatus.text = "正在模拟门禁卡...\n卡片ID: ${bytesToHexString(cardInfo.uid)}"
    }

    private fun bytesToHexString(bytes: ByteArray): String {
        val sb = StringBuilder()
        for (b in bytes) {
            sb.append(String.format("%02X", b))
        }
        return sb.toString()
    }

    private fun updateCardList() {
        val adapter = binding.rvCards.adapter as? CardListAdapter
        adapter?.submitList(savedCards.toList())
    }

    private class CardListAdapter : ListAdapter<CardInfo, CardListAdapter.ViewHolder>(CardDiffCallback()) {
        
        class ViewHolder(private val binding: ItemCardBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(cardInfo: CardInfo) {
                binding.tvCardId.text = "卡片ID: ${bytesToHexString(cardInfo.uid)}"
                binding.tvSectorCount.text = "扇区数: ${cardInfo.data.size}"
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemCardBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(getItem(position))
        }
    }

    private class CardDiffCallback : DiffUtil.ItemCallback<CardInfo>() {
        override fun areItemsTheSame(oldItem: CardInfo, newItem: CardInfo): Boolean {
            return Arrays.equals(oldItem.uid, newItem.uid)
        }

        override fun areContentsTheSame(oldItem: CardInfo, newItem: CardInfo): Boolean {
            return Arrays.equals(oldItem.uid, newItem.uid) && oldItem.data == newItem.data
        }
    }

    data class CardInfo(
        val uid: ByteArray,
        val data: Map<Int, ByteArray>
    )

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 