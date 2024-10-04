import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.io.File

class QRCodeDrawer {
    private val size = 21 // Размер сетки для QR-кода версии 1 (21x21)

    // Создание пустой матрицы для QR-кода
    private fun createEmptyMatrix(): Array<IntArray> {
        return Array(size) { IntArray(size) { 0 } } // 0 означает белый (пустой)
    }

    // Рисуем паттерн искателя (finder pattern)
    private fun drawFinderPattern(matrix: Array<IntArray>, x: Int, y: Int) {
        for (i in 0..6) {
            for (j in 0..6) {
                val isBorder = i == 0 || i == 6 || j == 0 || j == 6
                val isCenter = i in 2..4 && j in 2..4
                matrix[x + i][y + j] = if (isBorder || isCenter) 1 else 0
            }
        }
    }

    // Рисуем тайминг-паттерн (timing pattern)
    private fun drawTimingPattern(matrix: Array<IntArray>) {
        for (i in 8 until size - 8) {
            matrix[6][i] = if (i % 2 == 0) 1 else 0
            matrix[i][6] = if (i % 2 == 0) 1 else 0
        }
    }

    // Заполнение базовых структур QR-кода
    private fun setupBasePatterns(matrix: Array<IntArray>) {
        // Рисуем 3 искателя
        drawFinderPattern(matrix, 0, 0) // Верхний левый угол
        drawFinderPattern(matrix, 0, size - 7) // Верхний правый угол
        drawFinderPattern(matrix, size - 7, 0) // Нижний левый угол

        // Рисуем тайминг-паттерн
        drawTimingPattern(matrix)
    }

    // Размещение закодированных данных в матрице (байты)
    private fun placeData(matrix: Array<IntArray>, data: ByteArray) {
        var bitIndex = 0

        // Стартуем с нижнего правого угла и движемся вверх по столбцам
        var x = size - 1
        var y = size - 1

        while (bitIndex < data.size * 8 && x > 0) {
            // Размещаем два столбца данных (с шагом 2)
            for (col in x downTo x - 1) {
                if (col == 6) continue // Пропускаем колонку тайминг-паттерна

                for (row in y downTo 0) {
                    if (matrix[row][col] == 0) {
                        val byteIndex = bitIndex / 8
                        // Проверяем, не превышает ли byteIndex длину data
                        if (byteIndex < data.size) {
                            val bit = (data[byteIndex].toInt() shr (7 - (bitIndex % 8))) and 1
                            matrix[row][col] = bit
                            bitIndex++
                        }
                    }
                }
                // Меняем направление
                y = if (y == 0) size - 1 else 0
            }
            x -= 2 // Сдвигаемся на следующий блок данных
        }
    }


    // Рендеринг QR-кода в изображение
    fun drawQRCode(data: IntArray, outputFilePath: String) {
        val matrix = createEmptyMatrix()

        // Заполняем базовые паттерны
        setupBasePatterns(matrix)

        // Преобразуем IntArray в ByteArray
        val byteArray = ByteArray(data.size) { i -> data[i].toByte() }

        // Размещаем данные
        placeData(matrix, byteArray)

        // Генерируем изображение
        val imageSize = 500 // Размер изображения в пикселях
        val scale = imageSize / size
        val image = BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_RGB)
        val graphics: Graphics2D = image.createGraphics()

        // Рисуем черные и белые клетки
        for (i in 0 until size) {
            for (j in 0 until size) {
                val color = if (matrix[i][j] == 1) Color.BLACK else Color.WHITE
                graphics.color = color
                graphics.fillRect(j * scale, i * scale, scale, scale)
            }
        }

        // Сохраняем изображение
        ImageIO.write(image, "png", File(outputFilePath))
    }
}