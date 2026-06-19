package com.hbacakk.fintrack.feature.budget.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hbacakk.fintrack.core.ui.theme.Error
import com.hbacakk.fintrack.core.ui.theme.FinTrackTheme
import com.hbacakk.fintrack.core.ui.theme.Warning
import com.hbacakk.fintrack.domain.model.Budget
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun BudgetListScreen(
    onAddBudgetClick: () -> Unit,
    viewModel: BudgetListViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BudgetListContent(
        uiState = uiState,
        onAddBudgetClick = onAddBudgetClick,
    )
}

@Composable
private fun BudgetListContent(
    uiState: BudgetListUiState,
    onAddBudgetClick: () -> Unit,
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddBudgetClick) {
                Icon(Icons.Filled.Add, contentDescription = "Bütçe ekle")
            }
        },
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
                Text(
                    text = "Bütçeler",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(16.dp),
                )

                if (uiState.budgets.isEmpty() && !uiState.isLoading) {
                    EmptyState()
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(uiState.budgets) { budget ->
                            BudgetCard(budget)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Henüz bütçe oluşturmadın",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun BudgetCard(budget: Budget) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(budget.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    budget.category.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Progress bar rengi: aşılmışsa kırmızı, uyarı seviyesindeyse turuncu, normalse yeşil
            val progressColor = when {
                budget.isExceeded -> Error
                budget.isWarning -> Warning
                else -> MaterialTheme.colorScheme.primary
            }

            LinearProgressIndicator(
                progress = { (budget.spentPercentage / 100).coerceIn(0.0, 1.0).toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .height(8.dp),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "${formatCurrency(budget.spent)} / ${formatCurrency(budget.limit)}",
                    style = MaterialTheme.typography.bodySmall,
                )
                if (budget.isExceeded) {
                    Text(
                        text = "Limit aşıldı!",
                        style = MaterialTheme.typography.bodySmall,
                        color = Error,
                    )
                } else if (budget.isWarning) {
                    Text(
                        text = "%${budget.spentPercentage.toInt()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Warning,
                    )
                }
            }
        }
    }
}

private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("tr", "TR"))
    return format.format(amount)
}

@Preview(showBackground = true)
@Composable
private fun BudgetListScreenPreview() {
    FinTrackTheme {
        BudgetListContent(
            uiState = BudgetListUiState(isLoading = false),
            onAddBudgetClick = {},
        )
    }
}