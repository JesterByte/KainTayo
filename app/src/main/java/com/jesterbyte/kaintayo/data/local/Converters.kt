package com.jesterbyte.kaintayo.data.local

import androidx.room.TypeConverter
import com.jesterbyte.kaintayo.domain.model.MealType
import com.jesterbyte.kaintayo.domain.model.MeasurementUnit

class Converters {

    @TypeConverter
    fun fromMealType(value: MealType): String {
        return value.name
    }

    @TypeConverter
    fun toMealType(value: String): MealType {
        return MealType.valueOf(value)
    }

    @TypeConverter
    fun fromMeasurementUnit(value: MeasurementUnit): String {
        return value.name
    }

    @TypeConverter
    fun toMeasurementUnit(value: String): MeasurementUnit {
        return MeasurementUnit.valueOf(value)
    }
}