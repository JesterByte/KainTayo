package com.jesterbyte.kaintayo.data.local

import android.content.Context
import androidx.core.content.edit

class KainTayoPreferences(context: Context) {

    private val preferences = context.applicationContext
        .getSharedPreferences(
            PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )

    fun getBudgetCentavos(): Long? {
        val value = preferences.getLong(KEY_BUDGET, -1L)
        return value.takeIf { it >= 0L }
    }

    fun saveBudgetCentavos(budgetCentavos: Long) {
        require(budgetCentavos >= 0L) {
            "Budget cannot be negative."
        }

        preferences.edit {
            putLong(KEY_BUDGET, budgetCentavos)
        }
    }

    fun getHouseholdSize(): Int? {
        val value = preferences.getInt(KEY_HOUSEHOLD_SIZE, -1)
        return value.takeIf { it > 0 }
    }

    fun saveHouseholdSize(householdSize: Int) {
        require(householdSize > 0) {
            "Household size must be greater than zero."
        }

        preferences.edit {
            putInt(KEY_HOUSEHOLD_SIZE, householdSize)
        }
    }

    private companion object {
        const val PREFERENCES_NAME = "kaintayo_preferences"
        const val KEY_BUDGET = "budget_centavos"
        const val KEY_HOUSEHOLD_SIZE = "household_size"
    }
}
