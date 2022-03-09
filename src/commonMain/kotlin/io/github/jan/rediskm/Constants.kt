package io.github.jan.rediskm

object Constants {

    const val CHAR_STAR = '*';
    const val CHAR_DOLLAR = '$';
    const val CHAR_COLON = ':';
    const val CHAR_PLUS = '+';
    const val CHAR_MINUS = '-';
    const val CHAR_SLASH_R = '\r';
    const val CHAR_SLASH_N = '\n';

    val NULL_REPRESENTATION = null;
    val BYTE_CRLN = byteArrayOf(CHAR_SLASH_R.code.toByte(), CHAR_SLASH_N.code.toByte());

}
