package com.hastakala.shop.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hastakala.shop.ui.theme.MyApplicationTheme
import androidx.compose.material3.Surface

@Preview(showBackground = true, widthDp = 320) // Narrow screen
@Composable
fun HeatmapPreviewSmall() {
    MyApplicationTheme {
        Surface {
            SalesHeatmap(
                data = emptyMap<Long, Double>(),
                sales = emptyList(),
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 411) // Standard screen
@Composable
fun HeatmapPreviewStandard() {
    MyApplicationTheme {
        Surface {
            SalesHeatmap(
                data = emptyMap<Long, Double>(),
                sales = emptyList(),
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
