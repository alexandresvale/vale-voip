package com.valevoip.feature.dialer.model

data class KeypadKey(
    val digit: String,
    val letters: String = "",
    val isActionKey: Boolean = false
)

object KeypadUtil {
    val matrix = listOf(
        listOf(KeypadKey("1", ""), KeypadKey("2", "ABC"), KeypadKey("3", "DEF")),
        listOf(KeypadKey("4", "GHI"), KeypadKey("5", "JKL"), KeypadKey("6", "MNO")),
        listOf(KeypadKey("7", "PQRS"), KeypadKey("8", "TUV"), KeypadKey("9", "WXYZ")),
        listOf(KeypadKey("*", ""), KeypadKey("0", "+"), KeypadKey("#", ""))
    )
}

