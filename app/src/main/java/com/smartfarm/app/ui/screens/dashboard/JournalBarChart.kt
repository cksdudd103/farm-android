package com.smartfarm.app.ui.screens.dashboard

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.material3.MaterialTheme
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

/**
 * Bar chart of the last 7 days of journal entry counts, rendered with
 * MPAndroidChart wrapped for Jetpack Compose via AndroidView.
 */
@Composable
fun JournalBarChart(labels: List<String>, counts: List<Int>) {
    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface.toArgb()

    AndroidView(
        modifier = Modifier.fillMaxWidth().height(220.dp),
        factory = { context ->
            BarChart(context).apply {
                description.isEnabled = false
                legend.isEnabled = false
                setDrawGridBackground(false)
                setFitBars(true)
                axisRight.isEnabled = false
                axisLeft.axisMinimum = 0f
                axisLeft.textColor = onSurfaceColor
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.granularity = 1f
                xAxis.setDrawGridLines(false)
                xAxis.textColor = onSurfaceColor
            }
        },
        update = { chart ->
            val entries = counts.mapIndexed { index, count -> BarEntry(index.toFloat(), count.toFloat()) }
            val dataSet = BarDataSet(entries, "작업일지").apply {
                color = primaryColor
                valueTextColor = onSurfaceColor
                valueTextSize = 11f
            }
            chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            chart.data = BarData(dataSet).apply { barWidth = 0.6f }
            chart.animateY(600)
            chart.invalidate()
        },
    )
}
