package com.hbacakk.fintrack.feature.budget.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import org.koin.androidx.compose.koinViewModel

@Composable
fun CreateBudgetScreen(
    onBudgetSaved: () -> Unit,
    viewModel: CreateBudgetViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onBudgetSaved()
    }

    CreateBudgetContent(
        uiState = uiState,
        onNameChange = viewModel::onNameChange,
        onLimitChange = viewModel::onLimitChange,
        onCategorySelected = viewModel::onCategorySelected,
        onSaveClick = viewModel::onSaveClick,
    )
}

@Composable
private fun CreateBudgetContent(
    uiState: CreateBudgetUiState,
    onNameChange: (String) -> Unit,
    onLimitChange: (String) -> Unit,
    onCategorySelected: (Category) -> Unit,
    onSaveClick: () -> Unit,
) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Yeni Bütçe", style = MaterialTheme.typography.headlineSmall)

            FinTrackTextField(
                value = uiState.name,
                onValueChange = onNameChange,
                label = "Bütçe Adı",
                placeholder = "örn. Yemek Bütçesi",
                errorMessage = uiState.nameError,
            )

            FinTrackTextField(
                value = uiState.limit,
                onValueChange = onLimitChange,
                label = "Aylık Limit",
                placeholder = "0.00",
                errorMessage = uiState.limitError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )

            Text("Kategori", style = MaterialTheme.typography.titleSmall)

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(Category.entries.filter { it.isExpense }) { category ->
                    FilterChip(
                        selected = uiState.selectedCategory == category,
                        onClick = { onCategorySelected(category) },
                        label = { Text(category.displayName) },
                    )
                }
            }

            FinTrackButton(
                text = "Bütçe Oluştur",
                onClick = onSaveClick,
                isLoading = uiState.isLoading,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CreateBudgetScreenPreview() {
    FinTrackTheme {
        CreateBudgetContent(
            uiState = CreateBudgetUiState(),
            onNameChange = {},
            onLimitChange = {},
            onCategorySelected = {},
            onSaveClick = {},
        )
    }
}
