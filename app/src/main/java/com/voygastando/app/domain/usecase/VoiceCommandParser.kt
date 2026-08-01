package com.voygastando.app.domain.usecase

import java.text.Normalizer

data class VoicePurchaseCommand(
    val name: String,
    val price: Long?,
    val quantity: Int,
    val action: VoiceCommandAction
) {
    val shouldAdd: Boolean = action == VoiceCommandAction.ADD
    val shouldSubtract: Boolean = action == VoiceCommandAction.SUBTRACT
}

enum class VoiceCommandAction {
    PREPARE,
    ADD,
    SUBTRACT
}

class VoiceCommandParser {
    fun parsePurchase(text: String): VoicePurchaseCommand? {
        val normalized = text.normalizedWords()
        if (normalized.isEmpty()) return null

        val action = when {
            normalized.any { it in SUBTRACT_WORDS } -> VoiceCommandAction.SUBTRACT
            normalized.any { it in ADD_WORDS } -> VoiceCommandAction.ADD
            else -> VoiceCommandAction.PREPARE
        }
        val quantity = normalized.findQuantity()
        val numberPhrases = normalized.extractNumberPhrases()
        val pricePhrase = numberPhrases
            .filterNot { it.value == quantity.toLong() && it.isQuantityPhrase }
            .maxByOrNull { it.value }

        if (pricePhrase == null && action != VoiceCommandAction.SUBTRACT) return null

        val skipIndexes = (pricePhrase?.indexes.orEmpty()) + normalized.quantityIndexes()
        val name = normalized
            .mapIndexedNotNull { index, word ->
                word.takeIf {
                    index !in skipIndexes &&
                        it !in ADD_WORDS &&
                        it !in SUBTRACT_WORDS &&
                        it !in QUANTITY_WORDS &&
                        it !in PRICE_WORDS
                }
            }
            .joinToString(" ")
            .trim()

        return VoicePurchaseCommand(
            name = name.take(MAX_PRODUCT_NAME_LENGTH),
            price = pricePhrase?.value,
            quantity = quantity,
            action = action
        )
    }

    private fun List<String>.findQuantity(): Int {
        forEachIndexed { index, word ->
            if (word in QUANTITY_WORDS && index > 0) {
                val previous = parseFlexibleNumber(listOf(this[index - 1]))
                if (previous in 1..MAX_QUANTITY) return previous.toInt()
            }
            if ((word == "por" || word == "x") && index < lastIndex) {
                val next = parseFlexibleNumber(listOf(this[index + 1]))
                if (next in 1..MAX_QUANTITY) return next.toInt()
            }
        }
        return 1
    }

    private fun List<String>.quantityIndexes(): Set<Int> {
        val indexes = mutableSetOf<Int>()
        forEachIndexed { index, word ->
            if (word in QUANTITY_WORDS) {
                indexes += index
                if (index > 0) indexes += index - 1
                if (index > 1 && this[index - 2] in NUMBER_WORDS) indexes += index - 2
            }
            if ((word == "por" || word == "x") && index < lastIndex) {
                indexes += index
                indexes += index + 1
            }
        }
        return indexes
    }

    private fun List<String>.extractNumberPhrases(): List<NumberPhrase> {
        val phrases = mutableListOf<NumberPhrase>()
        var index = 0
        while (index <= lastIndex) {
            val word = this[index]
            val digitValue = word.digitsOnlyValue()
            if (digitValue != null) {
                phrases += NumberPhrase(digitValue, setOf(index), isQuantityPhrase = isQuantityNear(index))
                index++
                continue
            }

            if (word in NUMBER_WORDS) {
                val indexes = mutableSetOf<Int>()
                val words = mutableListOf<String>()
                var cursor = index
                while (cursor <= lastIndex && this[cursor] in NUMBER_WORDS) {
                    if (words.isNotEmpty() && getOrNull(cursor + 1) in QUANTITY_WORDS) {
                        break
                    }
                    words += this[cursor]
                    indexes += cursor
                    cursor++
                }
                val value = parseNumberPhrase(words)
                if (value > 0) {
                    phrases += NumberPhrase(value, indexes, isQuantityPhrase = indexes.any { phraseIndex -> isQuantityNear(phraseIndex) })
                }
                index = cursor
                continue
            }
            index++
        }
        return phrases
    }

    private fun List<String>.isQuantityNear(index: Int): Boolean {
        val next = getOrNull(index + 1)
        val previous = getOrNull(index - 1)
        return next in QUANTITY_WORDS || previous in setOf("por", "x")
    }

    private fun parseNumberPhrase(words: List<String>): Long {
        if (words.isEmpty()) return 0
        var total = 0L
        var current = 0L
        words.forEach { word ->
            when (word) {
                "mil" -> {
                    total += (current.takeIf { it > 0 } ?: 1L) * 1000L
                    current = 0
                }
                "cien" -> current += 100
                "ciento" -> current += 100
                else -> current += NUMBER_WORDS[word] ?: 0
            }
        }
        return total + current
    }

    private fun parseFlexibleNumber(words: List<String>): Long {
        val digitValue = words.firstOrNull()?.digitsOnlyValue()
        return digitValue ?: parseNumberPhrase(words)
    }

    private fun String.digitsOnlyValue(): Long? {
        val digits = filter(Char::isDigit)
        return digits.takeIf { it.isNotBlank() }?.toLongOrNull()
    }

    private fun String.normalizedWords(): List<String> =
        Normalizer.normalize(lowercase(), Normalizer.Form.NFD)
            .replace("\\p{Mn}+".toRegex(), "")
            .replace("[^a-z0-9., ]".toRegex(), " ")
            .split(Regex("\\s+"))
            .map { it.trim(',', '.') }
            .filter { it.isNotBlank() }

    private data class NumberPhrase(
        val value: Long,
        val indexes: Set<Int>,
        val isQuantityPhrase: Boolean
    )

    private companion object {
        const val MAX_QUANTITY = 99
        const val MAX_PRODUCT_NAME_LENGTH = 48

        val ADD_WORDS = setOf("sumar", "agregar", "cargar", "anotar")
        val SUBTRACT_WORDS = setOf("restar", "quitar", "sacar", "eliminar", "borrar")
        val QUANTITY_WORDS = setOf("producto", "productos", "unidad", "unidades")
        val PRICE_WORDS = setOf("peso", "pesos", "precio", "importe")
        val NUMBER_WORDS = mapOf(
            "un" to 1L,
            "una" to 1L,
            "uno" to 1L,
            "dos" to 2L,
            "tres" to 3L,
            "cuatro" to 4L,
            "cinco" to 5L,
            "seis" to 6L,
            "siete" to 7L,
            "ocho" to 8L,
            "nueve" to 9L,
            "diez" to 10L,
            "once" to 11L,
            "doce" to 12L,
            "trece" to 13L,
            "catorce" to 14L,
            "quince" to 15L,
            "veinte" to 20L,
            "treinta" to 30L,
            "cuarenta" to 40L,
            "cincuenta" to 50L,
            "sesenta" to 60L,
            "setenta" to 70L,
            "ochenta" to 80L,
            "noventa" to 90L,
            "quinientos" to 500L,
            "mil" to 1000L
        )
    }
}
