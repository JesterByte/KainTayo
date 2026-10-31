
package com.example.kaintayo.domain.usecase

import com.example.kaintayo.domain.model.MeasurementUnit

object MeasurementUnitConverter {

    private enum class Dimension {
        MASS,
        VOLUME,
        PIECE,
        PACK
    }

    private data class UnitDefinition(
        val dimension: Dimension,
        val baseUnitsPerUnit: Double
    )

    private fun getDefinition(unit: MeasurementUnit): UnitDefinition {
        return when (unit) {
            MeasurementUnit.GRAM ->
                UnitDefinition(Dimension.MASS, 1.0)

            MeasurementUnit.KILOGRAM ->
                UnitDefinition(Dimension.MASS, 1000.0)

            MeasurementUnit.MILLILITER ->
                UnitDefinition(Dimension.VOLUME, 1.0)

            MeasurementUnit.LITER ->
                UnitDefinition(Dimension.VOLUME, 1000.0)

            MeasurementUnit.CUP ->
                UnitDefinition(Dimension.VOLUME, 240.0)

            MeasurementUnit.TABLESPOON ->
                UnitDefinition(Dimension.VOLUME, 15.0)

            MeasurementUnit.TEASPOON ->
                UnitDefinition(Dimension.VOLUME, 5.0)

            MeasurementUnit.PIECE ->
                UnitDefinition(Dimension.PIECE, 1.0)

            MeasurementUnit.PACK ->
                UnitDefinition(Dimension.PACK, 1.0)
        }
    }

    fun convert(
        quantity: Double,
        from: MeasurementUnit,
        to: MeasurementUnit
    ): Double? {
        if (!quantity.isFinite() || quantity < 0.0) {
            return null
        }

        val source = getDefinition(from)
        val target = getDefinition(to)

        if (source.dimension != target.dimension) {
            return null
        }

        return quantity *
                source.baseUnitsPerUnit /
                target.baseUnitsPerUnit
    }
}
