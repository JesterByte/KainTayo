
package com.example.kaintayo.domain.usecase

import com.example.kaintayo.domain.model.MeasurementUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MeasurementUnitConverterTest {

    @Test
    fun convertsKilogramsToGrams() {
        val result = MeasurementUnitConverter.convert(
            1.0,
            MeasurementUnit.KILOGRAM,
            MeasurementUnit.GRAM
        )

        assertEquals(1000.0, result!!, 0.0001)
    }

    @Test
    fun convertsGramsToKilograms() {
        val result = MeasurementUnitConverter.convert(
            500.0,
            MeasurementUnit.GRAM,
            MeasurementUnit.KILOGRAM
        )

        assertEquals(0.5, result!!, 0.0001)
    }

    @Test
    fun convertsLitersToMilliliters() {
        val result = MeasurementUnitConverter.convert(
            2.0,
            MeasurementUnit.LITER,
            MeasurementUnit.MILLILITER
        )

        assertEquals(2000.0, result!!, 0.0001)
    }

    @Test
    fun convertsCupsToMilliliters() {
        val result = MeasurementUnitConverter.convert(
            2.0,
            MeasurementUnit.CUP,
            MeasurementUnit.MILLILITER
        )

        assertEquals(480.0, result!!, 0.0001)
    }

    @Test
    fun convertsTablespoonsToTeaspoons() {
        val result = MeasurementUnitConverter.convert(
            1.0,
            MeasurementUnit.TABLESPOON,
            MeasurementUnit.TEASPOON
        )

        assertEquals(3.0, result!!, 0.0001)
    }

    @Test
    fun rejectsMassToVolumeConversion() {
        val result = MeasurementUnitConverter.convert(
            1.0,
            MeasurementUnit.GRAM,
            MeasurementUnit.CUP
        )

        assertNull(result)
    }

    @Test
    fun rejectsNegativeQuantities() {
        val result = MeasurementUnitConverter.convert(
            -5.0,
            MeasurementUnit.GRAM,
            MeasurementUnit.GRAM
        )

        assertNull(result)
    }

    @Test
    fun rejectsInvalidQuantities() {
        val result = MeasurementUnitConverter.convert(
            Double.NaN,
            MeasurementUnit.GRAM,
            MeasurementUnit.GRAM
        )

        assertNull(result)
    }
}
