package com.example.kaintayo.domain.usecase

import com.example.kaintayo.domain.model.MealPlanSlot
import com.example.kaintayo.domain.model.MealType
import com.example.kaintayo.domain.model.Recipe
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class MealPlanRecipeSelectorUseCaseTest {

    private fun createRecipe(
        id: String,
        mealType: MealType
    ) = Recipe(
        id = id,
        name = id,
        description = "Test recipe",
        mealType = mealType,
        servings = 4,
        ingredients = emptyList(),
        instructions = listOf("Prepare the meal."),
        preparationMinutes = 20
    )

    private fun createRecipes(): List<Recipe> {
        return MealType.values().flatMap { mealType ->
            (1..10).map { number ->
                createRecipe(
                    id = "${mealType.name.lowercase()}-$number",
                    mealType = mealType
                )
            }
        }
    }

    @Test
    fun assignsOneRecipeToEverySlot() {
        val slots = MealPlanScheduleGeneratorUseCase.generate(
            LocalDate.of(2026, 10, 30),
            LocalDate.of(2026, 11, 14)
        )!!

        val result = MealPlanRecipeSelectorUseCase.select(
            slots,
            createRecipes()
        )!!

        assertEquals(slots.size, result.size)
    }

    @Test
    fun selectsRecipesMatchingMealTypes() {
        val slots = MealPlanScheduleGeneratorUseCase.generate(
            LocalDate.of(2026, 11, 1),
            LocalDate.of(2026, 11, 5)
        )!!

        val recipes = createRecipes().associateBy { it.id }

        val result = MealPlanRecipeSelectorUseCase.select(
            slots,
            recipes.values.toList()
        )!!

        result.forEach { meal ->
            assertEquals(
                meal.mealType,
                recipes[meal.recipeId]?.mealType
            )
        }
    }

    @Test
    fun doesNotRepeatRecipeWithinNextThreeOccurrences() {
        val slots = MealPlanScheduleGeneratorUseCase.generate(
            LocalDate.of(2026, 11, 1),
            LocalDate.of(2026, 11, 16)
        )!!

        val result = MealPlanRecipeSelectorUseCase.select(
            slots,
            createRecipes()
        )!!

        MealType.values().forEach { mealType ->
            val selectedIds = result
                .filter { it.mealType == mealType }
                .map { it.recipeId }

            for (index in selectedIds.indices) {
                val nextThree = selectedIds.drop(index + 1).take(3)

                assertTrue(selectedIds[index] !in nextThree)
            }
        }
    }

    @Test
    fun distributesRecipesAcrossAvailableCatalog() {
        val slots = MealPlanScheduleGeneratorUseCase.generate(
            LocalDate.of(2026, 11, 1),
            LocalDate.of(2026, 11, 16)
        )!!

        val result = MealPlanRecipeSelectorUseCase.select(
            slots,
            createRecipes()
        )!!

        MealType.values().forEach { mealType ->
            val selectedIds = result
                .filter { it.mealType == mealType }
                .map { it.recipeId }

            assertEquals(10, selectedIds.distinct().size)
        }
    }

    @Test
    fun producesSameResultForSameInputs() {
        val slots = MealPlanScheduleGeneratorUseCase.generate(
            LocalDate.of(2026, 11, 1),
            LocalDate.of(2026, 11, 5)
        )!!

        val recipes = createRecipes()

        val first = MealPlanRecipeSelectorUseCase.select(
            slots,
            recipes
        )

        val second = MealPlanRecipeSelectorUseCase.select(
            slots,
            recipes.reversed()
        )

        assertEquals(first, second)
    }

    @Test
    fun rejectsInsufficientRecipesForVarietyRule() {
        val slots = MealPlanScheduleGeneratorUseCase.generate(
            LocalDate.of(2026, 11, 1),
            LocalDate.of(2026, 11, 5)
        )!!

        val recipes = listOf(
            createRecipe("breakfast-1", MealType.BREAKFAST),
            createRecipe("breakfast-2", MealType.BREAKFAST),
            createRecipe("breakfast-3", MealType.BREAKFAST)
        )

        assertNull(
            MealPlanRecipeSelectorUseCase.select(
                slots,
                recipes
            )
        )
    }

    @Test
    fun rejectsDuplicateRecipeIds() {
        val slots = listOf(
            MealPlanSlot(
                LocalDate.of(2026, 11, 1),
                MealType.BREAKFAST
            )
        )

        val recipes = listOf(
            createRecipe("same-id", MealType.BREAKFAST),
            createRecipe("same-id", MealType.BREAKFAST)
        )

        assertNull(
            MealPlanRecipeSelectorUseCase.select(
                slots,
                recipes
            )
        )
    }

    @Test
    fun rejectsDuplicateMealSlots() {
        val date = LocalDate.of(2026, 11, 1)

        val slots = listOf(
            MealPlanSlot(date, MealType.BREAKFAST),
            MealPlanSlot(date, MealType.BREAKFAST)
        )

        assertNull(
            MealPlanRecipeSelectorUseCase.select(
                slots,
                createRecipes()
            )
        )
    }

    @Test
    fun returnsEmptyListForEmptySchedule() {
        val result = MealPlanRecipeSelectorUseCase.select(
            emptyList(),
            createRecipes()
        )

        assertEquals(emptyList<Any>(), result)
    }
}
