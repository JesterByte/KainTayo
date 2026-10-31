package com.example.kaintayo.data.local

import androidx.room.TypeConverter
import com.example.kaintayo.domain.model.MealType
import com.example.kaintayo.domain.model.MeasurementUnit

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