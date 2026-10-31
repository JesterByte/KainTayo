package com.example.kaintayo.data.catalog

import com.example.kaintayo.domain.model.Ingredient
import com.example.kaintayo.domain.model.MeasurementUnit
import kotlin.math.roundToLong

object FilipinoIngredientCatalog {

    private fun perKg(
        id: String,
        name: String,
        pricePesos: Double
    ) = Ingredient(
        id = id,
        name = name,
        defaultUnit = MeasurementUnit.GRAM,
        estimatedPriceCentavos = (pricePesos * 100).roundToLong(),
        priceQuantity = 1.0,
        priceUnit = MeasurementUnit.KILOGRAM
    )

    private fun perLiter(
        id: String,
        name: String,
        pricePesos: Double
    ) = Ingredient(
        id = id,
        name = name,
        defaultUnit = MeasurementUnit.MILLILITER,
        estimatedPriceCentavos = (pricePesos * 100).roundToLong(),
        priceQuantity = 1.0,
        priceUnit = MeasurementUnit.LITER
    )

    private fun perPiece(
        id: String,
        name: String,
        pricePesos: Double
    ) = Ingredient(
        id = id,
        name = name,
        defaultUnit = MeasurementUnit.PIECE,
        estimatedPriceCentavos = (pricePesos * 100).roundToLong(),
        priceQuantity = 1.0,
        priceUnit = MeasurementUnit.PIECE
    )

    private fun perPack(
        id: String,
        name: String,
        pricePesos: Double
    ) = Ingredient(
        id = id,
        name = name,
        defaultUnit = MeasurementUnit.PACK,
        estimatedPriceCentavos = (pricePesos * 100).roundToLong(),
        priceQuantity = 1.0,
        priceUnit = MeasurementUnit.PACK
    )

    private fun per100g(
        id: String,
        name: String,
        pricePesos: Double
    ) = Ingredient(
        id = id,
        name = name,
        defaultUnit = MeasurementUnit.GRAM,
        estimatedPriceCentavos = (pricePesos * 100).roundToLong(),
        priceQuantity = 100.0,
        priceUnit = MeasurementUnit.GRAM
    )

    val ingredients = listOf(

        // RICE AND PROTEIN (10)

        perKg("rice", "Rice (Well-milled)", 56.25),
        perKg("chicken", "Chicken", 210.00),
        perKg("tilapia", "Tilapia", 185.27),
        perKg("galunggong", "Galunggong", 237.96),
        perKg("bangus", "Bangus", 260.00),
        perKg("pork", "Pork (Liempo)", 342.97),
        perPiece("egg", "Chicken Egg", 8.00),
        perPack("tofu", "Tokwa (Tofu)", 25.00),
        perKg("mung-beans", "Monggo", 130.00),
        perPack("sardines", "Canned Sardines", 25.00),

        // VEGETABLES AND AROMATICS (20)

        perKg("sayote", "Sayote", 60.00),
        perKg("pechay", "Pechay", 100.00),
        perKg("kangkong", "Kangkong", 70.00),
        perPack("malunggay", "Malunggay Leaves", 15.00),
        perKg("squash", "Kalabasa", 40.00),
        perKg("carrot", "Carrot", 100.00),
        perKg("cabbage", "Cabbage", 80.00),
        perKg("sitaw", "Sitaw", 90.00),
        perKg("tomato", "Tomato", 90.00),
        perKg("eggplant", "Talong", 65.00),
        perKg("onion", "Red Onion", 140.00),
        perKg("garlic", "Garlic", 150.00),
        perKg("ginger", "Ginger", 159.66),
        perKg("potato", "Potato", 100.00),
        perKg("ampalaya", "Ampalaya", 90.00),
        perKg("okra", "Okra", 70.00),
        perKg("baguio-beans", "Baguio Beans", 157.76),
        perKg("calamansi", "Calamansi", 100.00),
        perKg("spring-onion", "Spring Onion", 120.00),
        perKg("banana", "Saba Banana", 60.00),

        // COOKING ESSENTIALS (10)

        perLiter("cooking-oil", "Cooking Oil", 100.00),
        perLiter("soy-sauce", "Soy Sauce", 45.00),
        perLiter("vinegar", "Vinegar", 35.00),
        perLiter("fish-sauce", "Patis (Fish Sauce)", 45.00),
        perKg("salt", "Salt", 20.00),
        per100g("black-pepper", "Ground Black Pepper", 35.00),
        perKg("sugar", "White Sugar", 82.08),
        perPack("misua", "Misua", 25.00),
        perPack("sinigang-mix", "Sinigang Mix", 20.00),
        per100g("cornstarch", "Cornstarch", 20.00)
    )
}
