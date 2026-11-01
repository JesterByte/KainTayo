package com.jesterbyte.kaintayo.data.catalog

import com.jesterbyte.kaintayo.domain.model.MealType
import com.jesterbyte.kaintayo.domain.model.MeasurementUnit.GRAM
import com.jesterbyte.kaintayo.domain.model.MeasurementUnit.MILLILITER
import com.jesterbyte.kaintayo.domain.model.MeasurementUnit.PACK
import com.jesterbyte.kaintayo.domain.model.MeasurementUnit.PIECE
import com.jesterbyte.kaintayo.domain.model.Recipe
import com.jesterbyte.kaintayo.domain.model.RecipeIngredient

object FilipinoRecipeCatalog {

    private fun g(id: String, quantity: Double) = RecipeIngredient(id, quantity, GRAM)

    private fun ml(id: String, quantity: Double) = RecipeIngredient(id, quantity, MILLILITER)

    private fun pc(id: String, quantity: Double) = RecipeIngredient(id, quantity, PIECE)

    private fun pack(id: String, quantity: Double) = RecipeIngredient(id, quantity, PACK)

    val recipes = listOf(

        // BREAKFAST (10)

        Recipe(
            "itlog-kamatis",
            "Itlog na may Kamatis",
            "Scrambled eggs with fresh tomatoes and onions.",
            MealType.BREAKFAST,
            4,
            listOf(
                pc("egg", 4.0),
                g("tomato", 200.0),
                g("onion", 50.0),
                ml("cooking-oil", 15.0),
                g("rice", 600.0)
            ),
            listOf(
                "Chop the tomatoes and onions.",
                "Saute onion and tomatoes in oil.",
                "Add beaten eggs and cook until set.",
                "Serve with steamed rice."
            ),
            15
        ),

        Recipe(
            "tortang-talong-breakfast",
            "Tortang Talong",
            "Filipino eggplant omelet.",
            MealType.BREAKFAST,
            4,
            listOf(
                g("eggplant", 500.0),
                pc("egg", 4.0),
                g("onion", 40.0),
                ml("cooking-oil", 30.0),
                g("rice", 600.0)
            ),
            listOf(
                "Grill or boil the eggplants until soft.",
                "Peel and flatten the eggplants.",
                "Dip in beaten eggs with chopped onion.",
                "Pan-fry until golden on both sides.",
                "Serve with rice."
            ),
            25
        ),

        Recipe(
            "sardinas-misua-breakfast",
            "Sardinas with Misua",
            "A quick tomato-based sardine and misua soup.",
            MealType.BREAKFAST,
            4,
            listOf(
                pack("sardines", 2.0),
                pack("misua", 2.0),
                g("tomato", 150.0),
                g("onion", 60.0),
                g("garlic", 15.0),
                ml("cooking-oil", 10.0)
            ),
            listOf(
                "Saute garlic, onion, and tomatoes.",
                "Add sardines and water.",
                "Simmer for five minutes.",
                "Add misua and cook until tender."
            ),
            15
        ),

        Recipe(
            "monggo-malunggay-breakfast",
            "Ginisang Monggo with Malunggay",
            "Affordable mung bean stew with leafy vegetables.",
            MealType.BREAKFAST,
            4,
            listOf(
                g("mung-beans", 200.0),
                pack("malunggay", 1.0),
                g("tomato", 100.0),
                g("onion", 50.0),
                g("garlic", 20.0),
                ml("fish-sauce", 15.0),
                ml("cooking-oil", 15.0),
                g("rice", 600.0)
            ),
            listOf(
                "Boil mung beans until soft.",
                "Saute garlic, onion, and tomatoes.",
                "Add the cooked mung beans and broth.",
                "Season with fish sauce.",
                "Add malunggay leaves and simmer briefly."
            ),
            40
        ),

        Recipe(
            "tokwa-egg-scramble",
            "Tokwa at Itlog",
            "Tofu and scrambled eggs with tomatoes.",
            MealType.BREAKFAST,
            4,
            listOf(
                pack("tofu", 2.0),
                pc("egg", 3.0),
                g("tomato", 100.0),
                g("onion", 50.0),
                g("garlic", 15.0),
                ml("cooking-oil", 15.0),
                g("rice", 600.0)
            ),
            listOf(
                "Cut tofu into small cubes.",
                "Saute garlic, onion, and tomatoes.",
                "Add tofu and cook until lightly browned.",
                "Add beaten eggs and stir until cooked.",
                "Serve with rice."
            ),
            20
        ),

        Recipe(
            "pechay-egg",
            "Ginisang Pechay with Egg",
            "Pechay sauteed with garlic and eggs.",
            MealType.BREAKFAST,
            4,
            listOf(
                g("pechay", 300.0),
                pc("egg", 3.0),
                g("garlic", 15.0),
                ml("soy-sauce", 15.0),
                ml("cooking-oil", 15.0),
                g("rice", 600.0)
            ),
            listOf(
                "Wash and cut the pechay.",
                "Saute garlic in oil.",
                "Add pechay and soy sauce.",
                "Add beaten eggs and cook until set.",
                "Serve with rice."
            ),
            15
        ),

        Recipe(
            "ampalaya-egg-breakfast",
            "Ginisang Ampalaya with Egg",
            "Bitter melon cooked with eggs and tomatoes.",
            MealType.BREAKFAST,
            4,
            listOf(
                g("ampalaya", 250.0),
                pc("egg", 4.0),
                g("tomato", 100.0),
                g("onion", 50.0),
                ml("cooking-oil", 15.0),
                g("rice", 600.0)
            ),
            listOf(
                "Slice the ampalaya thinly.",
                "Saute onion and tomatoes.",
                "Add ampalaya and cook until slightly tender.",
                "Add beaten eggs and stir gently.",
                "Serve with rice."
            ),
            20
        ),

        Recipe(
            "misua-egg-soup",
            "Misua Soup with Egg",
            "Light noodle soup with egg and spring onions.",
            MealType.BREAKFAST,
            4,
            listOf(
                pack("misua", 2.0),
                pc("egg", 3.0),
                g("spring-onion", 30.0),
                g("garlic", 10.0),
                ml("fish-sauce", 15.0)
            ),
            listOf(
                "Saute garlic in a little oil.",
                "Add water and fish sauce.",
                "Bring to a simmer.",
                "Add misua and cook briefly.",
                "Slowly add beaten eggs.",
                "Garnish with spring onions."
            ),
            15
        ),

        Recipe(
            "sayote-egg-breakfast",
            "Ginisang Sayote with Egg",
            "Light sauteed sayote with scrambled egg.",
            MealType.BREAKFAST,
            4,
            listOf(
                g("sayote", 400.0),
                pc("egg", 3.0),
                g("tomato", 100.0),
                g("onion", 50.0),
                g("garlic", 15.0),
                ml("cooking-oil", 15.0),
                g("rice", 600.0)
            ),
            listOf(
                "Peel and slice the sayote.",
                "Saute garlic, onion, and tomatoes.",
                "Add sayote and a little water.",
                "Cook until tender.",
                "Add eggs and stir until cooked."
            ),
            20
        ),

        Recipe(
            "nilagang-saba-itlog",
            "Nilagang Saba at Itlog",
            "Boiled saba bananas served with boiled eggs.",
            MealType.BREAKFAST,
            4,
            listOf(
                g("banana", 600.0), pc("egg", 4.0)
            ),
            listOf(
                "Wash the saba bananas.",
                "Boil bananas until tender.",
                "Boil eggs until fully cooked.",
                "Peel and serve together."
            ),
            25
        ),

        // LUNCH (10)

        Recipe(
            "tinolang-manok",
            "Tinolang Manok",
            "Chicken soup with sayote, ginger, and malunggay.",
            MealType.LUNCH,
            4,
            listOf(
                g("chicken", 600.0),
                g("sayote", 300.0),
                pack("malunggay", 1.0),
                g("ginger", 40.0),
                g("onion", 80.0),
                g("garlic", 20.0),
                ml("fish-sauce", 30.0),
                g("rice", 600.0)
            ),
            listOf(
                "Saute ginger, garlic, and onion.",
                "Add chicken and fish sauce.",
                "Cook until the chicken changes color.",
                "Add water and simmer until chicken is tender.",
                "Add sayote and cook until soft.",
                "Add malunggay before serving."
            ),
            45
        ),

        Recipe(
            "sinigang-tilapia",
            "Sinigang na Tilapia",
            "Sour fish soup with kangkong and tomatoes.",
            MealType.LUNCH,
            4,
            listOf(
                g("tilapia", 700.0),
                g("tomato", 200.0),
                g("onion", 100.0),
                pack("sinigang-mix", 1.0),
                g("kangkong", 200.0),
                g("rice", 600.0)
            ),
            listOf(
                "Boil water with tomatoes and onion.",
                "Add sinigang mix.",
                "Add tilapia and simmer until cooked.",
                "Add kangkong near the end.",
                "Serve hot with rice."
            ),
            30
        ),

        Recipe(
            "paksiw-bangus",
            "Paksiw na Bangus",
            "Milkfish simmered in vinegar, garlic, and ginger.",
            MealType.LUNCH,
            4,
            listOf(
                g("bangus", 700.0),
                ml("vinegar", 100.0),
                g("garlic", 20.0),
                g("ginger", 20.0),
                g("eggplant", 200.0),
                g("black-pepper", 3.0),
                g("rice", 600.0)
            ),
            listOf(
                "Arrange bangus in a cooking pot.",
                "Add vinegar, garlic, ginger, and pepper.",
                "Add eggplant and a little water.",
                "Simmer until fish and vegetables are cooked.",
                "Serve with rice."
            ),
            30
        ),

        Recipe(
            "chicken-adobo",
            "Chicken Adobo",
            "Chicken braised in soy sauce, vinegar, and garlic.",
            MealType.LUNCH,
            4,
            listOf(
                g("chicken", 700.0),
                ml("soy-sauce", 80.0),
                ml("vinegar", 80.0),
                g("garlic", 30.0),
                g("black-pepper", 3.0),
                g("onion", 80.0),
                g("rice", 600.0)
            ),
            listOf(
                "Combine chicken, soy sauce, and garlic.",
                "Let the chicken marinate briefly.",
                "Simmer with vinegar, onion, pepper, and water.",
                "Cook until chicken is tender.",
                "Reduce the sauce to the desired consistency.",
                "Serve with rice."
            ),
            45
        ),

        Recipe(
            "monggo-pork",
            "Ginisang Monggo with Pork",
            "Mung bean stew with pork and leafy vegetables.",
            MealType.LUNCH,
            4,
            listOf(
                g("mung-beans", 250.0),
                g("pork", 200.0),
                g("tomato", 150.0),
                g("onion", 50.0),
                g("garlic", 20.0),
                pack("malunggay", 1.0),
                ml("fish-sauce", 20.0),
                ml("cooking-oil", 15.0),
                g("rice", 600.0)
            ),
            listOf(
                "Boil mung beans until tender.",
                "Saute garlic, onion, and tomatoes.",
                "Add pork and cook through.",
                "Add mung beans and broth.",
                "Season with fish sauce.",
                "Add malunggay and simmer briefly."
            ),
            45
        ),

        Recipe(
            "kalabasa-sitaw-tofu",
            "Ginisang Kalabasa at Sitaw with Tokwa",
            "Squash, string beans, and tofu in a savory sauce.",
            MealType.LUNCH,
            4,
            listOf(
                g("squash", 350.0),
                g("sitaw", 200.0),
                pack("tofu", 2.0),
                g("garlic", 20.0),
                g("onion", 60.0),
                ml("fish-sauce", 20.0),
                ml("cooking-oil", 15.0),
                g("rice", 600.0)
            ),
            listOf(
                "Cut squash, sitaw, and tofu.",
                "Saute garlic and onion.",
                "Add squash and a little water.",
                "Add sitaw and cook until nearly tender.",
                "Add tofu and fish sauce.",
                "Simmer until vegetables are cooked."
            ),
            25
        ),

        Recipe(
            "fried-galunggong",
            "Pritong Galunggong",
            "Crispy fried galunggong with tomato and calamansi.",
            MealType.LUNCH,
            4,
            listOf(
                g("galunggong", 700.0),
                ml("cooking-oil", 60.0),
                g("tomato", 200.0),
                g("onion", 50.0),
                g("calamansi", 100.0),
                g("rice", 600.0)
            ),
            listOf(
                "Clean and season the fish.",
                "Heat oil in a pan.",
                "Fry fish until golden and cooked through.",
                "Slice tomatoes and onion.",
                "Serve with calamansi and rice."
            ),
            25
        ),

        Recipe(
            "sardinas-pechay",
            "Sardinas with Pechay",
            "Canned sardines cooked with fresh pechay.",
            MealType.LUNCH,
            4,
            listOf(
                pack("sardines", 2.0),
                g("pechay", 250.0),
                g("tomato", 150.0),
                g("onion", 50.0),
                ml("cooking-oil", 10.0),
                g("rice", 600.0)
            ),
            listOf(
                "Saute onion and tomatoes.",
                "Add sardines and simmer.",
                "Add pechay.",
                "Cook until the pechay is tender.",
                "Serve with rice."
            ),
            15
        ),

        Recipe(
            "pinakbet-pork",
            "Pinakbet na Baboy",
            "Mixed Filipino vegetables with a little pork.",
            MealType.LUNCH,
            4,
            listOf(
                g("squash", 250.0),
                g("eggplant", 150.0),
                g("okra", 100.0),
                g("sitaw", 150.0),
                g("tomato", 120.0),
                g("onion", 50.0),
                g("garlic", 15.0),
                g("pork", 150.0),
                ml("fish-sauce", 20.0),
                g("rice", 600.0)
            ),
            listOf(
                "Cut all vegetables into serving pieces.",
                "Saute garlic, onion, and tomatoes.",
                "Add pork and cook through.",
                "Add squash and a little water.",
                "Add remaining vegetables and fish sauce.",
                "Cover and simmer until vegetables are tender."
            ),
            35
        ),

        Recipe(
            "cabbage-tofu",
            "Ginisang Repolyo with Tokwa",
            "Cabbage and tofu sauteed with garlic and soy sauce.",
            MealType.LUNCH,
            4,
            listOf(
                g("cabbage", 350.0),
                pack("tofu", 2.0),
                g("carrot", 100.0),
                g("garlic", 15.0),
                ml("soy-sauce", 25.0),
                ml("cooking-oil", 20.0),
                g("rice", 600.0)
            ),
            listOf(
                "Slice cabbage and carrot.",
                "Cut tofu into cubes.",
                "Saute garlic in oil.",
                "Add tofu and cook lightly.",
                "Add vegetables and soy sauce.",
                "Stir-fry until vegetables are tender-crisp."
            ),
            20
        ),

        // DINNER (10)

        Recipe(
            "nilagang-baboy",
            "Nilagang Baboy",
            "Pork soup with potatoes and cabbage.",
            MealType.DINNER,
            4,
            listOf(
                g("pork", 600.0),
                g("potato", 300.0),
                g("cabbage", 250.0),
                g("onion", 100.0),
                g("black-pepper", 3.0),
                ml("fish-sauce", 25.0),
                g("rice", 600.0)
            ),
            listOf(
                "Boil pork in water until tender.",
                "Add onion and pepper.",
                "Add potatoes and cook until soft.",
                "Add cabbage near the end.",
                "Season with fish sauce.",
                "Serve with rice."
            ),
            60
        ),

        Recipe(
            "sinigang-baboy",
            "Sinigang na Baboy",
            "Sour pork soup with kangkong and okra.",
            MealType.DINNER,
            4,
            listOf(
                g("pork", 500.0),
                g("tomato", 150.0),
                g("onion", 80.0),
                pack("sinigang-mix", 1.0),
                g("kangkong", 200.0),
                g("okra", 100.0),
                g("rice", 600.0)
            ),
            listOf(
                "Boil pork until tender.",
                "Add tomatoes and onion.",
                "Add sinigang mix.",
                "Add okra and cook until nearly tender.",
                "Add kangkong before turning off the heat.",
                "Serve hot with rice."
            ),
            60
        ),

        Recipe(
            "chicken-vegetable-soup",
            "Chicken Vegetable Soup",
            "Chicken soup with cabbage, carrots, and potatoes.",
            MealType.DINNER,
            4,
            listOf(
                g("chicken", 500.0),
                g("cabbage", 200.0),
                g("carrot", 120.0),
                g("potato", 200.0),
                g("onion", 70.0),
                g("garlic", 15.0),
                ml("fish-sauce", 20.0),
                g("rice", 600.0)
            ),
            listOf(
                "Saute garlic and onion.",
                "Add chicken and cook until lightly browned.",
                "Add water and simmer until chicken is tender.",
                "Add potatoes and carrots.",
                "Add cabbage near the end.",
                "Season with fish sauce."
            ),
            45
        ),

        Recipe(
            "galunggong-misua-soup",
            "Sinigang-style Galunggong Misua Soup",
            "Fish soup with misua, ginger, and tomatoes.",
            MealType.DINNER,
            4,
            listOf(
                g("galunggong", 600.0),
                pack("misua", 2.0),
                g("tomato", 150.0),
                g("onion", 60.0),
                g("ginger", 25.0),
                g("spring-onion", 30.0),
                ml("fish-sauce", 20.0)
            ),
            listOf(
                "Boil water with ginger, onion, and tomatoes.",
                "Add fish sauce.",
                "Add cleaned galunggong and simmer until cooked.",
                "Add misua and cook briefly.",
                "Garnish with spring onions."
            ),
            25
        ),

        Recipe(
            "fried-tilapia-kangkong",
            "Pritong Tilapia with Ginisang Kangkong",
            "Fried tilapia served with sauteed water spinach.",
            MealType.DINNER,
            4,
            listOf(
                g("tilapia", 700.0),
                g("kangkong", 300.0),
                g("garlic", 20.0),
                ml("soy-sauce", 20.0),
                ml("cooking-oil", 65.0),
                g("rice", 600.0)
            ),
            listOf(
                "Clean and prepare the tilapia.",
                "Fry tilapia until golden and cooked through.",
                "Saute garlic in a little oil.",
                "Add kangkong and soy sauce.",
                "Cook until just wilted.",
                "Serve together with rice."
            ),
            30
        ),

        Recipe(
            "tofu-vegetable-stirfry",
            "Tokwa Vegetable Stir-fry",
            "Tofu with carrots, cabbage, and savory sauce.",
            MealType.DINNER,
            4,
            listOf(
                pack("tofu", 2.0),
                g("carrot", 120.0),
                g("cabbage", 250.0),
                g("garlic", 15.0),
                ml("soy-sauce", 30.0),
                ml("cooking-oil", 20.0),
                g("cornstarch", 15.0),
                g("rice", 600.0)
            ),
            listOf(
                "Cut tofu into cubes and vegetables into strips.",
                "Pan-fry tofu until lightly golden.",
                "Saute garlic and add vegetables.",
                "Add soy sauce and a little water.",
                "Mix cornstarch with water and add to thicken.",
                "Return tofu to the pan and serve with rice."
            ),
            25
        ),

        Recipe(
            "ampalaya-egg-dinner",
            "Ginisang Ampalaya at Itlog",
            "Bitter melon with eggs and garlic.",
            MealType.DINNER,
            4,
            listOf(
                g("ampalaya", 300.0),
                pc("egg", 4.0),
                g("garlic", 15.0),
                g("onion", 50.0),
                g("tomato", 100.0),
                ml("cooking-oil", 15.0),
                g("rice", 600.0)
            ),
            listOf(
                "Slice and wash the ampalaya.",
                "Saute garlic and onion.",
                "Add tomatoes and ampalaya.",
                "Cook until slightly tender.",
                "Add beaten eggs and stir gently.",
                "Serve with rice."
            ),
            20
        ),

        Recipe(
            "chicken-sayote-soup",
            "Chicken Sayote Ginger Soup",
            "Simple chicken soup with sayote and ginger.",
            MealType.DINNER,
            4,
            listOf(
                g("chicken", 500.0),
                g("sayote", 350.0),
                g("ginger", 35.0),
                g("garlic", 15.0),
                g("onion", 60.0),
                ml("fish-sauce", 25.0),
                g("rice", 600.0)
            ),
            listOf(
                "Saute ginger, garlic, and onion.",
                "Add chicken and cook until lightly browned.",
                "Add water and simmer until chicken is tender.",
                "Add sayote and cook until soft.",
                "Season with fish sauce.",
                "Serve hot with rice."
            ),
            40
        ),

        Recipe(
            "ginisang-baguio-beans-egg",
            "Ginisang Baguio Beans with Egg",
            "Baguio beans sauteed with eggs, tomatoes, and garlic.",
            MealType.DINNER,
            4,
            listOf(
                g("baguio-beans", 300.0),
                pc("egg", 4.0),
                g("tomato", 100.0),
                g("onion", 50.0),
                g("garlic", 15.0),
                ml("cooking-oil", 15.0),
                g("rice", 600.0)
            ),
            listOf(
                "Wash and slice the Baguio beans.",
                "Saute garlic, onion, and tomatoes.",
                "Add Baguio beans and a little water.",
                "Cook until the beans are tender.",
                "Add beaten eggs and stir until cooked.",
                "Serve with rice."
            ),
            20
        ),

        Recipe(
            "pechay-tofu-soup",
            "Pechay and Tokwa Soup",
            "Light vegetable soup with tofu and garlic.",
            MealType.DINNER,
            4,
            listOf(
                g("pechay", 300.0),
                pack("tofu", 2.0),
                g("garlic", 15.0),
                g("onion", 50.0),
                ml("fish-sauce", 20.0),
                g("spring-onion", 20.0),
                g("rice", 600.0)
            ),
            listOf(
                "Saute garlic and onion.",
                "Add water and fish sauce.",
                "Add tofu and simmer briefly.",
                "Add pechay and cook until wilted.",
                "Garnish with spring onions.",
                "Serve with rice."
            ),
            20
        ),
    )
}
