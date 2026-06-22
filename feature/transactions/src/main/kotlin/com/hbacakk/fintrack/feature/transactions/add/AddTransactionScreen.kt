package com.hbacakk.fintrack.feature.transactions.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hbacakk.fintrack.core.ui.component.FinTrackButton
import com.hbacakk.fintrack.core.ui.component.FinTrackTextField
import com.hbacakk.fintrack.core.ui.theme.FinTrackTheme
import com.hbacakk.fintrack.domain.model.Category
import com.hbacakk.fintrack.domain.model.TransactionType
import org.koin.androidx.compose.koinViewModel

@Composable
fun AddTransactionScreen(
    onTransactionSaved: () -> Unit,
    viewModel: AddTransactionViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onTransactionSaved()
        }
    }

    AddTransactionContent(
        uiState = uiState,
        onAmountChange = viewModel::onAmountChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onTypeSelected = viewModel::onTypeSelected,
        onCategorySelected = viewModel::onCategorySelected,
        onSaveClick = viewModel::onSaveClick,
    )
}

@Composable
private fun AddTransactionContent(
    uiState: AddTransactionUiState,
    onAmountChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onTypeSelected: (TransactionType) -> Unit,
    onCategorySelected: (Category) -> Unit,
    onSaveClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Yeni İşlem", style = MaterialTheme.typography.headlineSmall)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = uiState.selectedType == TransactionType.EXPENSE,
                    onClick = { onTypeSelected(TransactionType.EXPENSE) },
                    label = { Text("Gider") },
                )
                FilterChip(
                    selected = uiState.selectedType == TransactionType.INCOME,
                    onClick = { onTypeSelected(TransactionType.INCOME) },
                    label = { Text("Gelir") },
                )
            }

            FinTrackTextField(
                value = uiState.amount,
                onValueChange = onAmountChange,
                label = "Tutar",
                placeholder = "0.00",
                errorMessage = uiState.amountError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )

            FinTrackTextField(
                value = uiState.description,
                onValueChange = onDescriptionChange,
                label = "Açıklama",
                placeholder = "örn. Market alışverişi",
                errorMessage = uiState.descriptionError,
            )

            Text("Kategori", style = MaterialTheme.typography.titleSmall)

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.availableCategories) { category ->
                    FilterChip(
                        selected = uiState.selectedCategory == category,
                        onClick = { onCategorySelected(category) },
                        label = { Text(category.displayName) },
                    )
                }
            }

            FinTrackButton(
                text = "Kaydet",
                onClick = onSaveClick,
                isLoading = uiState.isLoading,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AddTransactionScreenPreview() {
    FinTrackTheme {
        AddTransactionContent(
            uiState = AddTransactionUiState(),
            onAmountChange = {},
            onDescriptionChange = {},
            onTypeSelected = {},
            onCategorySelected = {},
            onSaveClick = {},
        )
    }
}
