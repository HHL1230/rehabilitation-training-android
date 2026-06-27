package com.example.rehabilitationtraining.sharing

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.rehabilitationtraining.BuildConfig
import com.example.rehabilitationtraining.data.TrainingRecordEntity
import java.io.File

object RecordShareManager {
    fun createShareIntent(
        context: Context,
        records: List<TrainingRecordEntity>,
        title: String = "復健訓練紀錄",
    ): Intent {
        require(records.isNotEmpty()) { "records must not be empty before sharing" }

        val exportDir = File(context.cacheDir, "exports")
        check(exportDir.exists() || exportDir.mkdirs()) {
            "Unable to create export directory: ${exportDir.absolutePath}"
        }

        val csvFile = File(exportDir, "rehabilitation-training-records.csv")
        csvFile.writeText("\uFEFF" + TrainingRecordExporter.buildCsv(records), Charsets.UTF_8)
        val uri = FileProvider.getUriForFile(
            context,
            "${BuildConfig.APPLICATION_ID}.fileprovider",
            csvFile,
        )

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, TrainingRecordExporter.buildSummary(records, title))
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = ClipData.newUri(context.contentResolver, title, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(sendIntent, "分享復健紀錄")
        if (context !is Activity) {
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return chooser
    }

    fun shareRecords(
        context: Context,
        records: List<TrainingRecordEntity>,
        title: String = "復健訓練紀錄",
    ) {
        val chooser = createShareIntent(context, records, title)
        context.startActivity(chooser)
    }
}
