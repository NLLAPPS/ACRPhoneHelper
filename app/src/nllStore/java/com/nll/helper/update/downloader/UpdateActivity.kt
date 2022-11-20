package com.nll.helper.update.downloader


import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.content.getSystemService
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nll.helper.R
import com.nll.helper.databinding.UpdateActivityBinding
import com.nll.helper.recorder.CLog
import com.nll.helper.util.extHumanReadableByteCount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


class UpdateActivity : AppCompatActivity() {
    private val logTag = "UpdateActivity"
    private var appVersionData: AppVersionData? = null

    companion object {
        fun start(context: Context, appVersionData: AppVersionData) {
            val intent = appVersionData.toIntent(Intent(context, UpdateActivity::class.java))
            context.startActivity(intent)
        }
    }

    private lateinit var binding: UpdateActivityBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = UpdateActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)


        appVersionData = AppVersionData.fromIntent(intent)

        if (appVersionData == null) {
            Toast.makeText(this, R.string.no_url_handle, Toast.LENGTH_LONG).show()
            finish()
        } else {

            binding.versionNotes.text = appVersionData!!.versionNotes?.ifEmpty {
                getString(R.string.forced_update_message_generic)
            }


            if (oldIsOnline()) {
                lifecycleScope.launch {
                    val targetFile = appVersionData!!.getDestinationFile(this@UpdateActivity)

                    val shouldDownload = if (targetFile.exists()) {
                        /**
                         * While we check packageInfo at setProgress when state is Completed
                         * We still need to check here too.
                         * Imagine a previously downloaded file being invalid and new version info being published.
                         * We must make sure we ignore invalid files
                         */
                        val packageInfo = FileDownloader.getPackageInfoFromApk(this@UpdateActivity, targetFile)
                        if (packageInfo != null) {
                            val installedVersion = getVersionCode()
                            val downloadedApkVersion =  packageInfo.longVersionCode
                            if (CLog.isDebug()) {
                                CLog.log(logTag, "onCreate() -> downloadedApkVersion: $downloadedApkVersion, installedVersion: $installedVersion")
                            }


                            val isNewOrSameVersion = downloadedApkVersion >= installedVersion
                            if (isNewOrSameVersion) {
                                if (CLog.isDebug()) {
                                    CLog.log(logTag, "onCreate() -> We have a downloaded file with same or new version as requested. No need to download")
                                }
                                setProgress(FileDownloader.DownloadStatus.Completed(targetFile, packageInfo))
                                false
                            } else {
                                if (CLog.isDebug()) {
                                    CLog.log(logTag, "onCreate() -> We have a downloaded file but it is NOT same or new version as requested. Allow download")
                                }
                                true
                            }

                        } else {
                            if (CLog.isDebug()) {
                                CLog.log(logTag, "onCreate() -> We have a download file but it is corrupt. Allow download")
                            }
                            true
                        }
                    } else {
                        if (CLog.isDebug()) {
                            CLog.log(logTag, "onCreate() -> We have no file. Allow download")
                        }
                        true
                    }


                    if (shouldDownload) {
                        withContext(Dispatchers.IO) {
                            FileDownloader().download(this@UpdateActivity, appVersionData!!.downloadUrl, targetFile).collect { downloadState ->
                                withContext(Dispatchers.Main) {
                                    setProgress(downloadState)
                                }
                            }
                        }
                    }
                }

            } else {
                Toast.makeText(this, R.string.cloud2_internet_conn_required, Toast.LENGTH_LONG).show()
                extTryStartActivity(Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY), getString(R.string.no_url_handle))
            }

        }
    }
    private fun Context.extConnectivityManager(): ConnectivityManager? = getSystemService()
    private fun oldIsOnline() = extConnectivityManager()?.activeNetworkInfo != null
    /**
     * If @param errorMessage is provided, a toast message with provided  be shown on failure to start activity
     */
    private fun extTryStartActivity(intent: Intent, errorMessage: String? = null) {
        try {
            if (intent.flags or Intent.FLAG_ACTIVITY_NEW_TASK == 0) {
                if (CLog.isDebug()) {
                    CLog.log("Context.extTryStartActivity", "FLAG_ACTIVITY_NEW_TASK was not added! Adding it")
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        } catch (e: Exception) {
            errorMessage?.let {
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            }
            CLog.logPrintStackTrace(e)
        }
    }
    @Suppress("DEPRECATION")
    private fun getVersionCode(): Long {
        return try {
            applicationContext.packageManager.getPackageInfo(applicationContext.packageName, 0).longVersionCode
        } catch (e: Exception) {
            CLog.logPrintStackTrace(e)
            0L
        }
    }

    private fun setProgress(downloadState: FileDownloader.DownloadStatus) {
        if (CLog.isDebug()) {
            CLog.log(logTag, "setProgress() -> downloadState $downloadState")
        }
        when (downloadState) {
            is FileDownloader.DownloadStatus.Completed -> {
                val totalBytes = downloadState.downloadedFile.length().extHumanReadableByteCount(true)
                binding.progressText.text = getString(R.string.update_downloader_download_completed)
                binding.downloadedBytes.text = totalBytes
                binding.loadingProgress.progress = 100
                binding.totalBytes.text = totalBytes

                with(binding.installButton) {
                    isEnabled = true
                    setOnClickListener {
                        if (CLog.isDebug()) {
                            CLog.log(logTag, "installButton() clicked")
                        }
                        validateAndInstallDownloadedApk(downloadState.downloadedFile, downloadState.packageInfo)
                    }
                }


            }
            is FileDownloader.DownloadStatus.Error -> {
                binding.progressText.text = downloadState.toString()
                binding.loadingProgress.progress = 0
                binding.installButton.isEnabled = false

                appVersionData?.let {
                    askToDownloadManually(it.downloadUrl)
                }


            }
            is FileDownloader.DownloadStatus.Progress -> {
                val totalBytes = downloadState.totalBytes.extHumanReadableByteCount(true)
                val downloadedBytes = downloadState.bytesCopied.extHumanReadableByteCount(true)
                binding.progressText.text = getString(R.string.update_downloader_downloading)
                binding.downloadedBytes.text = downloadedBytes
                binding.loadingProgress.progress = downloadState.percent
                binding.totalBytes.text = totalBytes

            }
        }

    }

    private fun startManualDownload(downloadUrl: String){
        try {
            val urlToOpen = Uri.parse(downloadUrl)
            try {
                /**
                 * TODO Do we need FLAG_ACTIVITY_NEW_DOCUMENT
                 * An activity that handles documents can use this attribute so that with every document you open you launch a separate instance of the same activity.
                 * If you check your recent apps, then you will see various screens of the same activity of your app, each using a different document.
                 */
                val openIntent = Intent(Intent.ACTION_VIEW, urlToOpen).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
                }
                startActivity(openIntent)
            } catch (e: Exception) {
                CLog.logPrintStackTrace(e)
                Toast.makeText(this@UpdateActivity, R.string.no_url_handle, Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            CLog.logPrintStackTrace(e)
            Toast.makeText(this@UpdateActivity, R.string.url_error, Toast.LENGTH_LONG).show()
        }
    }
    private fun askToDownloadManually(downloadUrl: String) {
        with(MaterialAlertDialogBuilder(this))
        {
            setIcon(R.drawable.ic_question_24dp)
            setMessage(R.string.update_download_manually)
            setCancelable(false)
            setPositiveButton(R.string.download) { _, _ ->
                startManualDownload(downloadUrl)
            }
            setNegativeButton(R.string.cancel, null)
            show()
        }
    }

    private fun validateAndInstallDownloadedApk(downloadedApk: File, packageInfo: PackageInfo) {

        val downloadedApkVersion =
            packageInfo.longVersionCode
        val isNewOrSameVersion = downloadedApkVersion >= getVersionCode()
        if (CLog.isDebug()) {
            CLog.log(logTag, "validateAndInstallDownloadedApk() -> isNewOrSameVersion $isNewOrSameVersion")
        }

        if (isNewOrSameVersion) {
            val installUri = FileProvider.getUriForFile(applicationContext, "${applicationInfo.packageName}.fileprovider", downloadedApk)
            installApk(installUri, false)
        } else {
            setProgress(FileDownloader.DownloadStatus.Error(FileDownloader.DownloadStatus.Error.Message.LowerVersion))
        }


    }


    private fun installApk(uri: Uri, skipXiaomiMIUIOptimizationCheck: Boolean) {

        val shouldAskToDisableXiaomiMIUIOptimization = if (skipXiaomiMIUIOptimizationCheck) {
            false
        } else {
            val isXiaomi = Build.MANUFACTURER.equals("xiaomi", ignoreCase = true)
            isXiaomi && MiuiUtils.isMiui() && !MiuiUtils.isFixedMiui()
        }

        if (CLog.isDebug()) {
            CLog.log(logTag, "installApk() -> uri: $uri, skipXiaomiMIUIOptimizationCheck: $skipXiaomiMIUIOptimizationCheck, shouldAskToDisableXiaomiMIUIOptimization: $shouldAskToDisableXiaomiMIUIOptimization")
        }


        if (shouldAskToDisableXiaomiMIUIOptimization) {
            showXiaomiMIUIOptimizationWarning(uri, appVersionData!!.downloadUrl)
        } else {


            if (!packageManager.canRequestPackageInstalls()) {
                Toast.makeText(this, R.string.update_downloader_enable_install_unknown_apps, Toast.LENGTH_LONG).show()
                val permissionIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(permissionIntent)
                return
            }

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(installIntent)
        }
    }

    private fun showXiaomiMIUIOptimizationWarning(installUri: Uri, downloadUrl: String) {
        if (CLog.isDebug()) {
            CLog.log(logTag, "showXiaomiMIUIOptimizationWarning()")
        }
        with(MaterialAlertDialogBuilder(this))
        {
            setIcon(R.drawable.ic_warning_24)
            setMessage(R.string.update_downloader_miu_optimazation_warning)
            setPositiveButton(R.string.update_downloader_install_app) { _, _ ->
                installApk(installUri, true)
            }
            setNegativeButton(R.string.cancel, null)
            setNeutralButton(R.string.download) { _, _ ->
                startManualDownload(downloadUrl)
            }
            show()
        }
    }


}