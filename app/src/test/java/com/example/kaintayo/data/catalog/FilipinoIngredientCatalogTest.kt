
package com.example.kaintayo.data.catalog

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FilipinoIngredientCatalogTest {

    @Test
    fun catalogContainsExactly40Ingredients() {
        assertEquals(40, FilipinoIngredientCatalog.ingredients.size)
    }

    @Test
    fun allIngredientIdsAreUnique() {
        val ids = FilipinoIngredientCatalog.ingredients.map { it.id }

        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun allIngredientPricesArePositive() {
        assertTrue(
            FilipinoIngredientCatalog.ingredients.all {
                it.estimatedPriceCentavos > 0 &&
                        it.priceQuantity > 0
            }
        )
    }
}
