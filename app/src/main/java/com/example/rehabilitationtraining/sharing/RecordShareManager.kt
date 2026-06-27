package com.example.rehabilitationtraining.sharing

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.example.rehabilitationtraining.data.TrainingRecordEntity

object RecordShareManager {
    fun createShareIntent(
        context: Context,
        records: List<TrainingRecordEntity>,
        title: String = "復健訓練紀錄",
    ): Intent {
        require(records.isNotEmpty()) { "records must not be empty before sharing" }

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, TrainingRecordExporter.buildSummary(records, title))
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
