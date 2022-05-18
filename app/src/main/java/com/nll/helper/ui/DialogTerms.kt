package com.nll.helper.ui

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nll.helper.R
import com.nll.helper.StoreConfigImpl
import com.nll.helper.databinding.DialogTermsBinding
import com.nll.helper.recorder.CLog
import com.nll.helper.util.autoCleared
import com.nll.helper.util.extSetHTML

class DialogTerms : DialogFragment() {
    private var listener: Listener? = null

    private var binding by autoCleared<DialogTermsBinding>()


    companion object {
        private const val logTag = "DialogTerms"

        private const val fragmentTag = "terms-dialog"

        fun display(fragmentManager: FragmentManager, listener: Listener) {
            val fragment = DialogTerms().apply {
                this.listener = listener
            }
            fragment.show(fragmentManager, fragmentTag)
        }
    }

    fun interface Listener {
        fun onTermsChoice(accepted: Boolean)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogTermsBinding.inflate(requireActivity().layoutInflater)
        val message = String.format(getString(R.string.privacy_policy_warning), StoreConfigImpl.getPrivacyPolicyUrl())
        binding.termsAgreed.extSetHTML(true, message) { urlToOpen ->
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(urlToOpen)))
            } catch (e: Exception) {
                CLog.logPrintStackTrace(e)
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
            }
        }


        val alertDialog = MaterialAlertDialogBuilder(requireContext(), theme)
            .setCancelable(false)
            .setView(binding.root)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                listener?.onTermsChoice(binding.termsAgreed.isChecked)
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                listener?.onTermsChoice(false)
            }
            .create()


        binding.termsAgreed.setOnCheckedChangeListener { _, isChecked ->
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = isChecked
        }

        alertDialog.setOnShowListener {
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
        }

        return alertDialog
    }

}