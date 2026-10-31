package com.example.kaintayo.core.time

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class PaydayCycleCalculatorTest {

    @Test
    fun october31_returnsOctober30ToNovember14() {
        val cycle = PaydayCycleCalculator.getCurrentCycle(
            LocalDate.of(2026, 10, 31)
        )

        assertEquals(
            LocalDate.of(2026, 10, 30),
            cycle.startDate
        )

        assertEquals(
            LocalDate.of(2026, 11, 14),
            cycle.endDate
        )

        assertEquals(16L, cycle.totalDays)
    }

    @Test
    fun november10_returnsOctober30ToNovember14() {
        val cycle = PaydayCycleCalculator.getCurrentCycle(
            LocalDate.of(2026, 11, 10)
        )

        assertEquals(
            LocalDate.of(2026, 10, 30),
            cycle.startDate
        )

        assertEquals(
            LocalDate.of(2026, 11, 14),
            cycle.endDate
        )
    }

    @Test
    fun february20_returnsFebruary15ToFebruary27() {
        val cycle = PaydayCycleCalculator.getCurrentCycle(
            LocalDate.of(2026, 2, 20)
        )

        assertEquals(
            LocalDate.of(2026, 2, 15),
            cycle.startDate
        )

        assertEquals(
            LocalDate.of(2026, 2, 27),
            cycle.endDate
        )
    }

    @Test
    fun february28_returnsFebruary28ToMarch14() {
        val cycle = PaydayCycleCalculator.getCurrentCycle(
            LocalDate.of(2026, 2, 28)
        )

        assertEquals(
            LocalDate.of(2026, 2, 28),
            cycle.startDate
        )

        assertEquals(
            LocalDate.of(2026, 3, 14),
            cycle.endDate
        )
    }
}
