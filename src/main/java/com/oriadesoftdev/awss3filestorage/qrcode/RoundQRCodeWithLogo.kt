package com.oriadesoftdev.awss3filestorage.qrcode

import com.oriadesoftdev.awss3filestorage.util.color
import io.github.g0dkar.qrcode.QRCode
import io.github.g0dkar.qrcode.internals.QRCodeRegion
import io.github.g0dkar.qrcode.internals.QRCodeSquareType
import io.github.g0dkar.qrcode.render.Colors
import io.github.g0dkar.qrcode.render.QRCodeGraphics
import java.awt.*
import java.awt.geom.Point2D
import java.awt.geom.RoundRectangle2D
import java.awt.image.BufferedImage
import java.io.FileOutputStream
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.math.roundToInt


class RoundQRCodeWithLogo private constructor(
    private val data: String,
    private val logoPath: Path?,
    private val outputPath: String,
    private val foregroundColor: Color,
    private val backgroundGradientColors: List<Color>,
) {

    class Builder {
        private var data: String = ""
        private var logoPath: Path? = null
        private var outputPath: String = ""
        private var foregroundColor: Color = Color.WHITE
        private var backgroundGradientColors: List<Color> = listOf(color("#003400"), color("#889086"))

        fun data(data: String) = apply { this.data = data }
        fun logoPath(logoPath: Path) = apply { this.logoPath = logoPath }
        fun outputPath(outputPath: String) = apply { this.outputPath = outputPath }

        @JvmOverloads
        fun foregroundColor(foregroundColor: Color = Color.BLACK) = apply {
            this.foregroundColor = foregroundColor
        }

        @JvmOverloads
        fun backgroundGradientColors(
            backgroundGradientColors: List<Color> =
                listOf(color("#ef4857"), color("#de4970"), color("#b44db0"), color("#7f52ff"))
        ) =
            apply { this.backgroundGradientColors = backgroundGradientColors }

        fun build() = RoundQRCodeWithLogo(data, logoPath, outputPath, foregroundColor, backgroundGradientColors)
    }

    companion object {
        val SQUARE_COLOR: Int = Colors.withAlpha(Colors.BLACK, 200)
    }

    @JvmOverloads
    fun createQRCodeWithLogo(
        cellSize: Int,
        margin: Int = (cellSize / 1.16).roundToInt(),
    ) {
        val renderedQrCodeImage =
            QRCode(data).renderShaded(cellSize, margin) { qrCodeSquare, qrCodeGraphics ->
                (qrCodeGraphics.nativeImage() as BufferedImage).createGraphics()
                if (qrCodeSquare.dark) {
                    when (qrCodeSquare.squareInfo.type) {
                        QRCodeSquareType.POSITION_PROBE -> when (qrCodeSquare.squareInfo.region) {
                            QRCodeRegion.TOP_LEFT_CORNER -> doNothing()
                            QRCodeRegion.TOP_RIGHT_CORNER -> doNothing()
                            QRCodeRegion.BOTTOM_LEFT_CORNER -> doNothing()
                            QRCodeRegion.BOTTOM_RIGHT_CORNER -> doNothing()
                            else -> doNothing()
                        }

                        QRCodeSquareType.MARGIN -> qrCodeGraphics.fill(SQUARE_COLOR)
                        else -> drawRoundRect(qrCodeGraphics = qrCodeGraphics, color = foregroundColor)
                    }
                }
            }.nativeImage() as BufferedImage

        val background =
            BufferedImage(renderedQrCodeImage.width, renderedQrCodeImage.height, BufferedImage.TYPE_INT_ARGB)

        // This was based on a Radial Gradient found on the Kotlin official Website
        val sizeFloat = renderedQrCodeImage.width.toFloat()
        val gradientCenter = Point2D.Float(-0.0f, sizeFloat)
        val dist = generateFloatDistArray(backgroundGradientColors)
        val radialGradientPaint = RadialGradientPaint(
            gradientCenter, sizeFloat, gradientCenter,
            dist, backgroundGradientColors.toTypedArray(),
            MultipleGradientPaint.CycleMethod.NO_CYCLE
        )

        val graphics = background.graphics as Graphics2D
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        // Draw the Kotlin Background
        graphics.paint = radialGradientPaint
        graphics.fillRoundRect(0, 0, background.width, background.height, margin, margin)

        drawTopLeftQRCodeSquare(background, margin, radialGradientPaint)
        drawTopRightQRCodeSquare(background, margin, radialGradientPaint)
        drawBottomLeftQRCodeSquare(background, margin, radialGradientPaint)
        drawCentralBottomRightQRCodeSquare(background, margin, radialGradientPaint)
        // Draw the QRCode
        graphics.drawImage(renderedQrCodeImage, 0, 0, null)
        val logoSquarePosition = 3 * background.width / 8
        val logoSquareSize = (background.width / 4)

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
        val logoWidth: Int = (background.width / 4.5).toInt() // Adjust logo size as needed

        val logoHeight: Int = (background.width / 4.5).toInt()// Adjust logo size as needed

        val finalImageHeight: Int = (renderedQrCodeImage.height - logoHeight) / 2
        val finalImageWidth: Int = (renderedQrCodeImage.width - logoWidth) / 2

        drawLogo(
            graphics,
            ImageIO.read(logoPath?.toFile()),
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

    private fun drawCentralBottomRightQRCodeSquare(
        bufferedImage: BufferedImage,
        margin: Int,
        radialGradientPaint: RadialGradientPaint
    ) {
        val qrCodeSquareSize = bufferedImage.width / 10.625
        bufferedImage.createGraphics().apply {
            paint = foregroundColor
            fillRoundRect(
                ((3 * bufferedImage.width / 4.0) + (3 * margin / 8.0) - (qrCodeSquareSize / 2.0)).toInt(),
                ((3 * bufferedImage.width / 4.0) + (3 * margin / 8.0) - (qrCodeSquareSize / 2.0)).toInt(),
                qrCodeSquareSize.toInt(),
                qrCodeSquareSize.toInt(),
                (qrCodeSquareSize / 2.5).toInt(),
                (qrCodeSquareSize / 2.5).toInt()
            )
            paint = radialGradientPaint
            fillRoundRect(
                ((3 * bufferedImage.width / 4.0) + (3 * margin / 8.0) + (3.3 * qrCodeSquareSize / 28) - (qrCodeSquareSize / 2.0)).toInt(),
                ((3 * bufferedImage.width / 4.0) + (3 * margin / 8.0) + (3.3 * qrCodeSquareSize / 28) - (qrCodeSquareSize / 2.0)).toInt(),
                (10.7 * qrCodeSquareSize / 14).toInt(),
                (10.7 * qrCodeSquareSize / 14).toInt(),
                (10.7 * qrCodeSquareSize / 35).toInt(),
                (10.7 * qrCodeSquareSize / 35).toInt()
            )
            paint = foregroundColor
            fillRoundRect(
                ((3 * bufferedImage.width / 4.0) + (3 * margin / 8.0) + (3.3 * qrCodeSquareSize / 14) - (qrCodeSquareSize / 2.0)).toInt(),
                ((3 * bufferedImage.width / 4.0) + (3 * margin / 8.0) + (3.3 * qrCodeSquareSize / 14) - (qrCodeSquareSize / 2.0)).toInt(),
                (3.7 * qrCodeSquareSize / 7).toInt(),
                (3.7 * qrCodeSquareSize / 7).toInt(),
                (3.7 * qrCodeSquareSize / 17.5).toInt(),
                (3.7 * qrCodeSquareSize / 17.5).toInt()
            )
        }
    }

    private fun drawTopRightQRCodeSquare(
        bufferedImage: BufferedImage,
        margin: Int,
        radialGradientPaint: RadialGradientPaint
    ) {
        val qrCodeSquareSize = (bufferedImage.width / 4.25).toInt()
        bufferedImage.createGraphics().apply {
            // Wipe the middle
            paint = foregroundColor
            fillRoundRect(
                bufferedImage.width - margin - qrCodeSquareSize,
                margin,
                qrCodeSquareSize,
                qrCodeSquareSize,
                (qrCodeSquareSize / 2.5).toInt(),
                (qrCodeSquareSize / 2.5).toInt()
            )

            paint = radialGradientPaint;
            fillRoundRect(
                bufferedImage.width - (margin + qrCodeSquareSize) + (3.3 * qrCodeSquareSize / 28).toInt(),
                margin + (3.3 * qrCodeSquareSize / 28).toInt(),
                (10.7 * qrCodeSquareSize / 14).toInt(),
                (10.7 * qrCodeSquareSize / 14).toInt(),
                (10.7 * qrCodeSquareSize / 35).toInt(),
                (10.7 * qrCodeSquareSize / 35).toInt()
            )
            paint = foregroundColor
            fillRoundRect(
                bufferedImage.width - (margin + qrCodeSquareSize) + (3.3 * qrCodeSquareSize / 14).toInt(),
                margin + (3.3 * qrCodeSquareSize / 14).toInt(),
                (3.7 * qrCodeSquareSize / 7).toInt(),
                (3.7 * qrCodeSquareSize / 7).toInt(),
                (3.7 * qrCodeSquareSize / 17.5).toInt(),
                (3.7 * qrCodeSquareSize / 17.5).toInt()
            )
        }

    }

    private fun drawBottomLeftQRCodeSquare(
        bufferedImage: BufferedImage,
        margin: Int,
        radialGradientPaint: RadialGradientPaint
    ) {
        val qrCodeSquareSize = (bufferedImage.width / 4.25).toInt()
        bufferedImage.createGraphics().apply {
            // Wipe the middle
            paint = foregroundColor
            fillRoundRect(
                margin,
                bufferedImage.width - margin - qrCodeSquareSize,
                qrCodeSquareSize,
                qrCodeSquareSize,
                (qrCodeSquareSize / 2.5).toInt(),
                (qrCodeSquareSize / 2.5).toInt()
            )

            paint = radialGradientPaint;
            fillRoundRect(
                margin + (3.3 * qrCodeSquareSize / 28).toInt(),
                bufferedImage.width - (margin + qrCodeSquareSize) + (3.3 * qrCodeSquareSize / 28).toInt(),
                (10.7 * qrCodeSquareSize / 14).toInt(),
                (10.7 * qrCodeSquareSize / 14).toInt(),
                (10.7 * qrCodeSquareSize / 35).toInt(),
                (10.7 * qrCodeSquareSize / 35).toInt()
            )
            paint = foregroundColor
            fillRoundRect(
                margin + (3.3 * qrCodeSquareSize / 14).toInt(),
                bufferedImage.width - (margin + qrCodeSquareSize) + (3.3 * qrCodeSquareSize / 14).toInt(),
                (3.7 * qrCodeSquareSize / 7).toInt(),
                (3.7 * qrCodeSquareSize / 7).toInt(),
                (3.7 * qrCodeSquareSize / 17.5).toInt(),
                (3.7 * qrCodeSquareSize / 17.5).toInt()
            )
        }
    }

    private fun drawTopLeftQRCodeSquare(
        bufferedImage: BufferedImage,
        margin: Int,
        radialGradientPaint: RadialGradientPaint
    ) {
        val qrCodeSquareSize = (bufferedImage.width / 4.25).toInt()
        bufferedImage.createGraphics().apply {
            paint = foregroundColor
            fillRoundRect(
                margin,
                margin,
                qrCodeSquareSize,
                qrCodeSquareSize,
                (qrCodeSquareSize / 2.5).toInt(),
                (qrCodeSquareSize / 2.5).toInt()
            )
            paint = radialGradientPaint;
            fillRoundRect(
                margin + (3.3 * qrCodeSquareSize / 28).toInt(),
                margin + (3.3 * qrCodeSquareSize / 28).toInt(),
                (10.7 * qrCodeSquareSize / 14).toInt(),
                (10.7 * qrCodeSquareSize / 14).toInt(),
                (10.7 * qrCodeSquareSize / 35).toInt(),
                (10.7 * qrCodeSquareSize / 35).toInt()
            )
            paint = foregroundColor
            fillRoundRect(
                margin + (3.3 * qrCodeSquareSize / 14).toInt(),
                margin + (3.3 * qrCodeSquareSize / 14).toInt(),
                (3.7 * qrCodeSquareSize / 7).toInt(),
                (3.7 * qrCodeSquareSize / 7).toInt(),
                (3.7 * qrCodeSquareSize / 17.5).toInt(),
                (3.7 * qrCodeSquareSize / 17.5).toInt()
            )
        }
    }

    private fun generateFloatDistArray(colors: List<Color>): FloatArray {
        val floatArray = mutableListOf<Float>()
        val colorsListSize = colors.size
        var colorRangeValue = 0.0f
        for (i in 1..colorsListSize) {
            colorRangeValue += (1.0f / colorsListSize.toFloat())
            floatArray.add(colorRangeValue)
        }
        return floatArray.toFloatArray()
    }

    private fun doNothing() {

    }

    private fun drawRoundRect(
        x: Int = 0,
        y: Int = 0,
        qrCodeGraphics: QRCodeGraphics,
        width: Int = qrCodeGraphics.width,
        height: Int = qrCodeGraphics.height,
        color: Color,
        widthRadius: Int = (qrCodeGraphics.width / 1.16).toInt(),
        heightRadius: Int = widthRadius,
    ) {
        ((qrCodeGraphics.nativeImage()) as BufferedImage).createGraphics().apply {
            this.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            paint = color
            fillRoundRect(
                x,
                y,
                width,
                height,
                widthRadius,
                heightRadius
            )
        }

    }

    private fun size(canvas: QRCodeGraphics) = canvas.width * 4
    private fun circleSize(canvas: QRCodeGraphics): Int = (canvas.width * 1.8).toInt()

    private fun drawTopLeftCorner(canvas: QRCodeGraphics) {
        val size = size(canvas)
        val circleSize = circleSize(canvas)
        drawRoundRect(
            qrCodeGraphics = canvas,
            width = size,
            height = size,
            widthRadius = circleSize,
            color = foregroundColor
        )

    }

    private fun drawTopRightCorner(canvas: QRCodeGraphics) {
        val size = size(canvas)
        val circleSize = circleSize(canvas)
        drawRoundRect(
            x = -size + canvas.width, y = 0,
            qrCodeGraphics = canvas,
            width = size,
            height = size,
            widthRadius = circleSize,
            color = foregroundColor
        )

    }

    private fun drawBottomLeftCorner(canvas: QRCodeGraphics) {
        val size = size(canvas)
        val circleSize = circleSize(canvas)
        drawRoundRect(
            x = 0,
            y = -size + canvas.width,
            qrCodeGraphics = canvas,
            width = size,
            height = size,
            widthRadius = circleSize,
            color = foregroundColor
        )
    }

    private fun drawBottomRightCorner(canvas: QRCodeGraphics) {
        val size = size(canvas)
        val circleSize = circleSize(canvas)
        drawRoundRect(
            x = -size + canvas.width,
            y = -size + canvas.width,
            qrCodeGraphics = canvas,
            width = size,
            height = size,
            widthRadius = circleSize,
            color = foregroundColor
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
        graphics.clip = RoundRectangle2D.Float(
            finalImageHeight.toFloat(),
            finalImageHeight.toFloat(),
            logoWidth.toFloat(),
            logoHeight.toFloat(),
            (logoWidth/3.0).toFloat(),
            (logoHeight/3.0).toFloat()
        )
        graphics.drawImage(logoImage, finalImageWidth, finalImageHeight, logoWidth, logoHeight, null)
    }
}