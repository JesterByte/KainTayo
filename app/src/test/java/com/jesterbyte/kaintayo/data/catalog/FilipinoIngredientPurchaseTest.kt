package com.jesterbyte.kaintayo.data.catalog

import com.jesterbyte.kaintayo.domain.model.MeasurementUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class FilipinoIngredientPurchaseTest {

    private val ingredients = FilipinoIngredientCatalog.ingredients
    private val ingredientsById = ingredients.associateBy { it.id }

    @Test
    fun catalogContainsExactly40UniqueIngredients() {
        assertEquals(40, ingredients.size)
        assertEquals(40, ingredientsById.size)
    }

    @Test
    fun kilogramIngredientsHaveOneKilogramPurchaseSize() {
        val ids = listOf(
            "rice", "chicken", "tilapia", "galunggong",
            "bangus", "pork", "mung-beans",
            "sayote", "pechay", "kangkong", "squash",
            "carrot", "cabbage", "sitaw", "tomato",
            "eggplant", "onion", "garlic", "ginger",
            "potato", "ampalaya", "okra", "baguio-beans",
            "calamansi", "spring-onion", "banana",
            "salt", "sugar"
        )

        ids.forEach { id ->
            val ingredient = ingredientsById[id]
            assertNotNull("Missing ingredient: $id", ingredient)

            assertEquals(
                "$id purchase quantity",
                1.0,
                ingredient!!.purchaseQuantity,
                0.0001
            )
            assertEquals(
                "$id purchase unit",
                MeasurementUnit.KILOGRAM,
                ingredient.purchaseUnit
            )
        }
    }

    @Test
    fun liquidIngredientsHaveOneLiterPurchaseSize() {
        val ids = listOf(
            "cooking-oil",
            "soy-sauce",
            "vinegar",
            "fish-sauce"
        )

        ids.forEach { id ->
            val ingredient = ingredientsById[id]
            assertNotNull("Missing ingredient: $id", ingredient)

            assertEquals(1.0, ingredient!!.purchaseQuantity, 0.0001)
            assertEquals(
                MeasurementUnit.LITER,
                ingredient.purchaseUnit
            )
        }
    }

    @Test
    fun eggsArePurchasedByPiece() {
        val egg = ingredientsById["egg"]

        assertNotNull(egg)
        assertEquals(1.0, egg!!.purchaseQuantity, 0.0001)
        assertEquals(MeasurementUnit.PIECE, egg.purchaseUnit)
    }

    @Test
    fun packagedIngredientsArePurchasedByPack() {
        val ids = listOf(
            "tofu",
            "sardines",
            "malunggay",
            "misua",
            "sinigang-mix"
        )

        ids.forEach { id ->
            val ingredient = ingredientsById[id]
            assertNotNull("Missing ingredient: $id", ingredient)

            assertEquals(1.0, ingredient!!.purchaseQuantity, 0.0001)
            assertEquals(
                MeasurementUnit.PACK,
                ingredient.purchaseUnit
            )
        }
    }

    @Test
    fun groundIngredientsArePurchasedIn100GramPackages() {
        val ids = listOf("black-pepper", "cornstarch")

        ids.forEach { id ->
            val ingredient = ingredientsById[id]
            assertNotNull("Missing ingredient: $id", ingredient)

            assertEquals(100.0, ingredient!!.purchaseQuantity, 0.0001)
            assertEquals(
                MeasurementUnit.GRAM,
                ingredient.purchaseUnit
            )
            assertEquals(100.0, ingredient.priceQuantity, 0.0001)
            assertEquals(
                MeasurementUnit.GRAM,
                ingredient.priceUnit
            )
        }
    }
}
