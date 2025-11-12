package com.nll.helper.debug

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.nll.helper.R
import com.nll.helper.StoreConfigImpl
import com.nll.helper.databinding.ActivityDebugLogBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.File


class DebugLogActivity : AppCompatActivity() {
    private val logTag = "DebugLogActivity"
    private lateinit var binding: ActivityDebugLogBinding
    private var logList: MutableList<String> = ArrayList()
    private var logAdapter: ArrayAdapter<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.enableEdgeToEdge(window)
        binding = ActivityDebugLogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }



        DebugLogService.observableLogProxy().onEach {

            logList.add(it)
            logAdapter?.notifyDataSetChanged()


            if (!binding.clearButton.isEnabled) {
                binding.clearButton.isEnabled = true
            }

            if (!binding.sendButton.isEnabled) {
                binding.sendButton.isEnabled = true
            }

        }.launchIn(lifecycleScope)

        DebugLogService.serviceMessage().onEach { serviceMessage ->
            android.util.Log.d("APH_$logTag", "serviceMessage -> $serviceMessage")

            when (serviceMessage) {
                is DebugLogServiceMessage.Saved -> {
                    if (serviceMessage.success) {
                        Toast.makeText(this, String.format(getString(R.string.debug_log_dumped), serviceMessage.path), Toast.LENGTH_LONG).show()
                        val i = Intent(Intent.ACTION_SEND).apply {
                            /**
                             *  is the MIME type of the data being sent.
                             *  getExtra can have either a EXTRA_TEXT or EXTRA_STREAM field, containing the data to be sent.
                             *  If using EXTRA_TEXT, the MIME type should be "text/plain";
                             *  otherwise it should be the MIME type of the data in EXTRA_STREAM.
                             */
                            type = "application/zip"
                            putExtra(Intent.EXTRA_EMAIL, arrayOf(StoreConfigImpl.getStoreContactEmail()))
                            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.debug_log))
                            putExtra(Intent.EXTRA_STREAM, DebugLogAttachmentProvider.getAttachmentUri(false, File(serviceMessage.path!!)))
                        }
                        try {
                            startActivity(Intent.createChooser(i, getString(R.string.share)))
                        } catch (ex: ActivityNotFoundException) {
                            Toast.makeText(this, R.string.url_error, Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Unable to dump logcat!", Toast.LENGTH_LONG).show()
                    }
                }
                is DebugLogServiceMessage.Started -> {

                    binding.startButton.isEnabled = false
                    binding.stopButton.isEnabled = true
                    binding.clearButton.isEnabled = serviceMessage.currentLogs.isNotEmpty()
                    binding.sendButton.isEnabled = serviceMessage.currentLogs.isNotEmpty()

                    logList = ArrayList(serviceMessage.currentLogs)
                    logAdapter = ArrayAdapter(this, R.layout.row_debug_log, logList)
                    binding.logView.adapter = logAdapter
                    binding.logView.transcriptMode = ListView.TRANSCRIPT_MODE_NORMAL
                    if (logList.size > 0) {
                        binding.logView.setSelection(logList.size - 1)
                    }

                }
                DebugLogServiceMessage.Stopped -> {
                    logList.clear()
                    logAdapter?.notifyDataSetChanged()
                    binding.startButton.isEnabled = true
                    binding.stopButton.isEnabled = false
                    binding.clearButton.isEnabled = false
                    binding.sendButton.isEnabled = false
                }
            }

        }.launchIn(lifecycleScope)



        binding.startButton.setOnClickListener {
            DebugLogService.startLogging(this)
        }

        binding.stopButton.setOnClickListener {
            lifecycleScope.launch {
                DebugLogService.stopLogging()
            }
        }

        binding.clearButton.setOnClickListener {
            logList.clear()
            logAdapter?.notifyDataSetChanged()
            lifecycleScope.launch {
                DebugLogService.clearLogs()
            }
        }

        binding.sendButton.setOnClickListener {
            lifecycleScope.launch {
                DebugLogService.saveLogs()
            }
        }


    }

    companion object {
        fun startUi(context: Context) {
            context.startActivity(Intent(context, DebugLogActivity::class.java))
        }
    }
}
