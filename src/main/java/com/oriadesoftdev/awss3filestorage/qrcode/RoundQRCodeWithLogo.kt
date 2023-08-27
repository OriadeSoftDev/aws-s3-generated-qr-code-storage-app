package com.oriadesoftdev.awss3filestorage.qrcode

import com.oriadesoftdev.awss3filestorage.util.color
import io.github.g0dkar.qrcode.QRCode
import io.github.g0dkar.qrcode.internals.QRCodeRegion
import io.github.g0dkar.qrcode.internals.QRCodeSquareType
import io.github.g0dkar.qrcode.render.Colors
import io.github.g0dkar.qrcode.render.QRCodeGraphics
import java.awt.*
import java.awt.geom.Point2D
import java.awt.image.BufferedImage
import java.io.FileOutputStream
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.math.roundToInt

class RoundQRCodeWithLogo {
    companion object {
        val SQUARE_COLOR: Int = Colors.withAlpha(Colors.BLACK, 200)
    }

    @JvmOverloads
    fun createQRCodeWithLogo(
        data: String,
        logoPath: Path,
        outputPath: String,
        colors: Array<Color> = arrayOf(color("#ef4857"), color("#de4970"), color("#b44db0"), color("#7f52ff")),
        cellSize: Int,
        margin: Int = (cellSize / 1.16).roundToInt(),
    ) {
        val renderedQrCodeImage =
            QRCode(data).renderShaded(cellSize, margin) { qrCodeSquare, qrCodeGraphics ->
                if (qrCodeSquare.dark) {
                    when (qrCodeSquare.squareInfo.type) {
                        QRCodeSquareType.POSITION_PROBE -> when (qrCodeSquare.squareInfo.region) {
                            QRCodeRegion.TOP_LEFT_CORNER -> drawTopLeftCorner(qrCodeGraphics)
                            QRCodeRegion.TOP_RIGHT_CORNER -> drawTopRightCorner(qrCodeGraphics)
                            QRCodeRegion.BOTTOM_LEFT_CORNER -> drawBottomLeftCorner(qrCodeGraphics)
                            QRCodeRegion.BOTTOM_RIGHT_CORNER -> drawBottomRightCorner(qrCodeGraphics)
                            else -> qrCodeGraphics.fill(SQUARE_COLOR)
                        }

                        QRCodeSquareType.MARGIN -> qrCodeGraphics.fill(SQUARE_COLOR)
                        else -> qrCodeGraphics.fillRoundRect(
                            0, 0, qrCodeGraphics.width, qrCodeGraphics.height,
                            (qrCodeGraphics.width / 1.16).toInt(), SQUARE_COLOR
                        )
                    }
                }
            }.nativeImage() as BufferedImage

        val background =
            BufferedImage(renderedQrCodeImage.width, renderedQrCodeImage.height, BufferedImage.TYPE_INT_ARGB)

        // This was based on a Radial Gradient found on the Kotlin official Website
        val sizeFloat = renderedQrCodeImage.width.toFloat()
        val gradientCenter = Point2D.Float(-0.0f, sizeFloat)
        val dist = floatArrayOf(0.25f, 0.5768f, 0.7641f, 0.9703f)
        val radialGradientPaint = RadialGradientPaint(
            gradientCenter, sizeFloat, gradientCenter,
            dist, colors,
            MultipleGradientPaint.CycleMethod.NO_CYCLE
        )

        val graphics = background.graphics as Graphics2D
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        // Draw the Kotlin Background
        graphics.paint = radialGradientPaint
        graphics.fillRoundRect(0, 0, background.width, background.height, margin, margin)

        // Draw the QRCode
        graphics.drawImage(renderedQrCodeImage, 0, 0, null)
        val logoSquarePosition = 3 * background.width / 8
        val logoSquareSize = background.width / 4

        // Wipe the middle
        graphics.fillRoundRect(
            logoSquarePosition,
            logoSquarePosition,
            logoSquareSize,
            logoSquareSize,
            (logoSquareSize / 3.0).toInt(),
            (logoSquareSize / 3.0).toInt()
        )
        // Draw the Kotlin Logo
        val logoWidth: Int =  background.width / 5 // Adjust logo size as needed

        val logoHeight: Int = background.width / 5// Adjust logo size as needed

        val finalImageHeight: Int = (renderedQrCodeImage.height - logoHeight) / 2
        val finalImageWidth: Int = (renderedQrCodeImage.width - logoWidth) / 2

        drawLogo(
            graphics,
            ImageIO.read(logoPath.toFile()),
            finalImageWidth,
            finalImageHeight,
            logoWidth,
            logoHeight
        )

        graphics.dispose()

        FileOutputStream(outputPath).use {
            ImageIO.write(background, "PNG", it)
        }
    }

    private fun size(canvas: QRCodeGraphics) = canvas.width * 4
    private fun circleSize(canvas: QRCodeGraphics): Int = (canvas.width * 1.8).toInt()

    private fun drawTopLeftCorner(canvas: QRCodeGraphics) {
        val size = size(canvas)
        val circleSize = circleSize(canvas)
        canvas.fillRoundRect(0, 0, size, size, circleSize, SQUARE_COLOR)
    }

    private fun drawTopRightCorner(canvas: QRCodeGraphics) {
        val size = size(canvas)
        val circleSize = circleSize(canvas)
        canvas.fillRoundRect(-size + canvas.width, 0, size, size, circleSize, SQUARE_COLOR)
    }

    private fun drawBottomLeftCorner(canvas: QRCodeGraphics) {
        val size = size(canvas)
        val circleSize = circleSize(canvas)
        canvas.fillRoundRect(0, -size + canvas.width, size, size, circleSize, SQUARE_COLOR)
    }

    private fun drawBottomRightCorner(canvas: QRCodeGraphics) {
        val size = size(canvas)
        val circleSize = circleSize(canvas)
        canvas.fillRoundRect(
            -size + canvas.width, -size + canvas.width, size, size, circleSize,
            SQUARE_COLOR
        )
    }

    private fun drawLogo(
        graphics: Graphics2D,
        logoImage: BufferedImage?,
        finalImageWidth: Int,
        finalImageHeight: Int,
        logoWidth: Int,
        logoHeight: Int
    ) {
        graphics.drawImage(logoImage, finalImageWidth, finalImageHeight, logoWidth, logoHeight, null)
    }
}