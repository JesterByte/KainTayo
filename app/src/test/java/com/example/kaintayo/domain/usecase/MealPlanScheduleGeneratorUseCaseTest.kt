package com.example.kaintayo.domain.usecase

import com.example.kaintayo.domain.model.MealType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class MealPlanScheduleGeneratorUseCaseTest {

    @Test
    fun generatesThreeMealsForEveryDate() {
        val result = MealPlanScheduleGeneratorUseCase.generate(
            LocalDate.of(2026, 10, 30),
            LocalDate.of(2026, 11, 14)
        )!!

        assertEquals(48, result.size)
    }

    @Test
    fun includesBothStartAndEndDates() {
        val start = LocalDate.of(2026, 10, 30)
        val end = LocalDate.of(2026, 11, 14)

        val result = MealPlanScheduleGeneratorUseCase.generate(
            start,
            end
        )!!

        assertEquals(start, result.first().date)
        assertEquals(end, result.last().date)
    }

    @Test
    fun generatesMealsInCorrectDailyOrder() {
        val result = MealPlanScheduleGeneratorUseCase.generate(
            LocalDate.of(2026, 11, 1),
            LocalDate.of(2026, 11, 1)
        )!!

        assertEquals(
            listOf(
                MealType.BREAKFAST,
                MealType.LUNCH,
                MealType.DINNER
            ),
            result.map { it.mealType }
        )
    }

    @Test
    fun generatesCorrectNumberOfSlotsForFebruaryCycle() {
        val result = MealPlanScheduleGeneratorUseCase.generate(
            LocalDate.of(2026, 2, 15),
            LocalDate.of(2026, 2, 27)
        )!!

        assertEquals(39, result.size)
    }

    @Test
    fun generatesOneDayWithThreeMeals() {
        val date = LocalDate.of(2026, 11, 1)

        val result = MealPlanScheduleGeneratorUseCase.generate(
            date,
            date
        )!!

        assertEquals(3, result.size)
        assertTrue(result.all { it.date == date })
    }

    @Test
    fun rejectsEndDateBeforeStartDate() {
        val result = MealPlanScheduleGeneratorUseCase.generate(
            LocalDate.of(2026, 11, 14),
            LocalDate.of(2026, 10, 30)
        )

        assertNull(result)
    }

    @Test
    fun generatesDatesInChronologicalOrder() {
        val result = MealPlanScheduleGeneratorUseCase.generate(
            LocalDate.of(2026, 11, 1),
            LocalDate.of(2026, 11, 3)
        )!!

        assertEquals(
            listOf(
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 2),
                LocalDate.of(2026, 11, 2),
                LocalDate.of(2026, 11, 2),
                LocalDate.of(2026, 11, 3),
                LocalDate.of(2026, 11, 3),
                LocalDate.of(2026, 11, 3)
            ),
            result.map { it.date }
        )
    }
}
