package com.yahorshymanchyk.ai_advent_with_love_2.presentation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yahorshymanchyk.ai_advent_with_love_2.R
import com.yahorshymanchyk.ai_advent_with_love_2.domain.model.ChatMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ClaudeFragment : Fragment(R.layout.fragment_claude) {

    private val viewModel: ClaudeViewModel by viewModels()

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView
    private lateinit var etMaxTokens: EditText
    private lateinit var tvMaxTokensError: TextView
    private lateinit var etStopSequence: EditText
    private lateinit var etSystemPrompt: EditText
    private lateinit var etInput: EditText
    private lateinit var btnSend: Button

    private lateinit var adapter: MessageAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerView)
        progressBar = view.findViewById(R.id.progressBar)
        tvError = view.findViewById(R.id.tvError)
        etMaxTokens = view.findViewById(R.id.etMaxTokens)
        tvMaxTokensError = view.findViewById(R.id.tvMaxTokensError)
        etStopSequence = view.findViewById(R.id.etStopSequence)
        etSystemPrompt = view.findViewById(R.id.etSystemPrompt)
        etInput = view.findViewById(R.id.etInput)
        btnSend = view.findViewById(R.id.btnSend)

        adapter = MessageAdapter { index ->
            val text = buildQAText(adapter.getItems(), index)
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("message", text))
            Toast.makeText(requireContext(), "Copied", Toast.LENGTH_SHORT).show()
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        etInput.addTextChangedListener(/* watcher = */ simpleWatcher { updateSendButton() })
        etMaxTokens.addTextChangedListener(simpleWatcher {
            val valid = isMaxTokensValid()
            tvMaxTokensError.visibility = if (valid) View.GONE else View.VISIBLE
            updateSendButton()
        })

        btnSend.setOnClickListener {
            val input = etInput.text.toString()
            val maxTokens = etMaxTokens.text.toString().toInt()
            val stopSequence = etStopSequence.text.toString().takeIf { it.isNotBlank() }
            val systemPrompt = etSystemPrompt.text.toString().takeIf { it.isNotBlank() }
            viewModel.sendMessage(input, maxTokens, stopSequence, systemPrompt)
            etInput.setText("")
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    adapter.submitList(state.messages)
                    if (state.messages.isNotEmpty()) {
                        recyclerView.post { recyclerView.scrollToPosition(adapter.itemCount - 1) }
                    }
                    progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                    if (state.error != null) {
                        tvError.visibility = View.VISIBLE
                        tvError.text = state.error
                    } else {
                        tvError.visibility = View.GONE
                    }
                    updateSendButton()
                }
            }
        }
    }

    private fun updateSendButton() {
        btnSend.isEnabled = isMaxTokensValid()
            && etInput.text.isNotBlank()
            && !viewModel.uiState.value.isLoading
    }

    private fun isMaxTokensValid(): Boolean =
        etMaxTokens.text.toString().toIntOrNull()?.let { it > 0 } == true

    private fun buildQAText(messages: List<ChatMessage>, index: Int): String {
        val message = messages[index]
        return when (message.role) {
            ChatMessage.Role.USER -> {
                val answer = messages.getOrNull(index + 1)
                    ?.takeIf { it.role == ChatMessage.Role.ASSISTANT }
                    ?.content
                if (answer != null) "Q: ${message.content}\n\nA: $answer"
                else "Q: ${message.content}"
            }
            ChatMessage.Role.ASSISTANT -> {
                val question = messages.getOrNull(index - 1)
                    ?.takeIf { it.role == ChatMessage.Role.USER }
                    ?.content
                if (question != null) "Q: $question\n\nA: ${message.content}"
                else "A: ${message.content}"
            }
        }
    }

    private fun simpleWatcher(action: () -> Unit) = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) { action() }
    }
}
