package com.jesterbyte.kaintayo.core.time

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class PaydayCycle(
    val startDate: LocalDate,
    val endDate: LocalDate
) {
    val totalDays: Long
        get() = ChronoUnit.DAYS.between(startDate, endDate) + 1
}

object PaydayCycleCalculator {

    fun getCurrentCycle(today: LocalDate): PaydayCycle {

        val currentMonth = today.withDayOfMonth(1)
        val lastDay = currentMonth.lengthOfMonth()

        val startDate = when {
            today.dayOfMonth < 15 -> {
                val previousMonth = currentMonth.minusDays(1)
                val previousPayday =
                    minOf(30, previousMonth.lengthOfMonth())

                previousMonth.withDayOfMonth(previousPayday)
            }

            today.dayOfMonth >= 30 -> {
                currentMonth.withDayOfMonth(
                    minOf(30, lastDay)
                )
            }

            today.dayOfMonth == lastDay && lastDay < 30 -> {
                currentMonth.withDayOfMonth(lastDay)
            }

            else -> {
                currentMonth.withDayOfMonth(15)
            }
        }

        val nextPayday = if (startDate.dayOfMonth == 15) {
            startDate.withDayOfMonth(
                minOf(30, startDate.lengthOfMonth())
            )
        } else {
            startDate.plusMonths(1).withDayOfMonth(15)
        }

        return PaydayCycle(
            startDate = startDate,
            endDate = nextPayday.minusDays(1)
        )
    }
}