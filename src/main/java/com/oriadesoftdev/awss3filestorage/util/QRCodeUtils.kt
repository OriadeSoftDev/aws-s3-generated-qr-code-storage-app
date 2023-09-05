package com.oriadesoftdev.awss3filestorage.util

import io.github.g0dkar.qrcode.render.Colors
import io.github.g0dkar.qrcode.render.QRCodeGraphics
import java.awt.Color

fun color(str: String): Color = Color(Colors.css(str), true)

private fun generateColorArray(backgroundGradientColors: String): Array<Color> {
    return arrayOf()
}

fun colorWithAlpha(alpha: Int) = Colors.withAlpha(Colors.BLACK, alpha)
