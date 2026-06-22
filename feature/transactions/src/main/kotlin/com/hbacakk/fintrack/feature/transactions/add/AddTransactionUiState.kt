package com.hbacakk.fintrack.feature.transactions.add

import com.hbacakk.fintrack.domain.model.Category
import com.hbacakk.fintrack.domain.model.TransactionType

data class AddTransactionUiState(
    val amount: String = "",
    val description: String = "",
    val selectedType: TransactionType = TransactionType.EXPENSE,
    val selectedCategory: Category = Category.OTHER_EXPENSE,
    val isLoading: Boolean = false,
    val amountError: String? = null,
    val descriptionError: String? = null,
    val isSaved: Boolean = false,
) {
    /**
     * Seçilen işlem tipine göre uygun kategorileri filtreler.
     * Gider seçiliyse sadece gider kategorileri gösterilir.
     */
    val availableCategories: List<Category>
        get() = Category.entries.filter {
            it.isExpense == (selectedType == TransactionType.EXPENSE)
        }
}
