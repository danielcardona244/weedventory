package com.example.weedventory.utils

import java.util.Locale
import kotlin.math.roundToInt

enum class UnitType(val code: String) {
    GRAMO("g"),
    ONZA("oz"),
    LIBRA("lb"),
    KILO("kg");

    companion object {
        fun fromCode(code: String?): UnitType = when(code?.lowercase()) {
            "g", "gramo", "gramos" -> GRAMO
            "oz", "onza", "onzas" -> ONZA
            "lb", "libra", "libras", "lib" -> LIBRA
            else -> KILO
        }
    }
}

object UnitConverter {
    private const val GRAMS_PER_KG = 1000.0
    private const val GRAMS_PER_OZ = 28.349523125
    private const val GRAMS_PER_LB = 453.59237

    fun toGrams(amount: Double, unit: UnitType): Int {
        val grams = when (unit) {
            UnitType.GRAMO -> amount
            UnitType.ONZA -> amount * GRAMS_PER_OZ
            UnitType.LIBRA -> amount * GRAMS_PER_LB
            UnitType.KILO -> amount * GRAMS_PER_KG
        }
        return grams.roundToInt()
    }

    fun fromGrams(grams: Int, unit: UnitType): Double {
        return when (unit) {
            UnitType.GRAMO -> grams.toDouble()
            UnitType.ONZA -> grams / GRAMS_PER_OZ
            UnitType.LIBRA -> grams / GRAMS_PER_LB
            UnitType.KILO -> grams / GRAMS_PER_KG
        }
    }

    fun formatGrams(grams: Int, preferredUnit: UnitType? = null): String {
        val unit = preferredUnit ?: if (grams >= GRAMS_PER_KG) UnitType.KILO else UnitType.GRAMO
        val value = fromGrams(grams, unit)
        val formatted = when (unit) {
            UnitType.GRAMO -> value.roundToInt().toString()
            else -> String.format(Locale.getDefault(), "%.2f", value)
        }
        return "$formatted ${unit.code}"
    }
}
