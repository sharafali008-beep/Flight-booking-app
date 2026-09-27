package com.skybook.core.util

import kotlin.random.Random

/** Creates booking references like "SKY7X2K9". */
object PnrGenerator {
    // Letters/digits that are easy to read (no 0/O or 1/I confusion).
    private const val CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

    fun generate(random: Random = Random.Default): String =
        "SKY" + (1..5).map { CHARS[random.nextInt(CHARS.length)] }.joinToString("")
}
