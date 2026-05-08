package com.hastakala.shop.ui

import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.hastakala.shop.data.CategorySalesTotal
import com.hastakala.shop.data.ColorSalesTotal
import com.hastakala.shop.data.ProductSalesTotal
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate

@Composable
fun CategoryPieChart(
    data: List<CategorySalesTotal>,
    totalRevenue: Double,
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = false
) {
    if (data.isEmpty()) return
    
    val chartColor = if (isDarkMode) Color.WHITE else Color.BLACK
    val centerTextColor = if (isDarkMode) Color.WHITE else Color.DKGRAY
    
    val modernColors = listOf(
        Color.rgb(103, 58, 183), // Deep Purple
        Color.rgb(63, 81, 181),  // Indigo
        Color.rgb(33, 150, 243), // Blue
        Color.rgb(0, 188, 212),  // Cyan
        Color.rgb(0, 150, 136),  // Teal
        Color.rgb(76, 175, 80),   // Green
        Color.rgb(255, 193, 7),  // Amber
        Color.rgb(255, 87, 34)   // Deep Orange
    )

    AndroidView(
        modifier = modifier,
        factory = { context ->
            PieChart(context).apply {
                description.isEnabled = false
                setUsePercentValues(false)
                legend.isEnabled = false // We'll build our own legend
                
                // Donut configuration
                isDrawHoleEnabled = true
                setHoleColor(Color.TRANSPARENT)
                holeRadius = 75f
                transparentCircleRadius = 0f
                
                // Center text
                setDrawCenterText(true)
                centerText = "Total\nRs. ${"%.0f".format(totalRevenue)}"
                setCenterTextSize(16f)
                setCenterTextColor(centerTextColor)
                
                // Disable labels inside
                setDrawEntryLabels(false)
                
                // Animation
                animateY(1400)
            }
        },
        update = { chart ->
            val entries = data.map { PieEntry(it.revenue.toFloat(), it.category) }
            val dataSet = PieDataSet(entries, "").apply {
                colors = modernColors
                setDrawValues(false) // No values inside segments
                sliceSpace = 3f
            }
            chart.data = PieData(dataSet)
            
            // Re-apply center text and colors for theme changes
            chart.centerText = "Total\nRs. ${"%.0f".format(totalRevenue)}"
            chart.setCenterTextColor(centerTextColor)

            chart.invalidate()
        }
    )
}

@Composable
fun ColorPieChart(data: List<ColorSalesTotal>, modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            PieChart(context).apply {
                description.isEnabled = false
                setUsePercentValues(false)
                legend.isEnabled = true
            }
        },
        update = { chart ->
            val entries = data.map { PieEntry(it.totalQty.toFloat(), it.color) }
            val dataSet = PieDataSet(entries, "Color Sales").apply {
                colors = ColorTemplate.MATERIAL_COLORS.toList()
                valueTextSize = 12f
                valueTextColor = Color.BLACK
            }
            chart.data = PieData(dataSet)
            chart.invalidate()
        }
    )
}

@Composable
fun ProductBarChart(data: List<ProductSalesTotal>, modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            BarChart(context).apply {
                description.isEnabled = false
                axisRight.isEnabled = false
            }
        },
        update = { chart ->
            val labels = data.map { it.productName }
            val entries = data.mapIndexed { index, item -> BarEntry(index.toFloat(), item.totalQty.toFloat()) }
            val dataSet = BarDataSet(entries, "Top Products").apply {
                colors = ColorTemplate.COLORFUL_COLORS.toList()
                valueTextColor = Color.BLACK
                valueTextSize = 11f
            }
            chart.xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(labels)
                granularity = 1f
                setDrawGridLines(false)
                labelRotationAngle = -25f
            }
            chart.data = BarData(dataSet)
            chart.invalidate()
        }
    )
}
