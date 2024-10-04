import kotlin.math.*

fun bitStringToByteArray(bitString: String): IntArray {
    // Разбиваем битовую строку на группы по 8 бит и преобразуем в байты
    val byteArray = IntArray((bitString.length + 7) / 8)
    for (i in byteArray.indices) {
        val byteStart = i * 8
        val byteEnd = min(byteStart + 8, bitString.length)
        val byteString = bitString.substring(byteStart, byteEnd).padEnd(8, '0')
        byteArray[i] = byteString.toInt(2)
    }
    return byteArray    
}

fun main() {
    val qrEncoder = QREncoder()

    // Выберите данные для кодирования и режим
    val data = "HELLO WORLD"
    val mode = QREncoder.Mode.ALPHANUMERIC
    val version = 1

    // Шаг 1: Кодируем данные
    val encodedData = qrEncoder.encodeData(data, mode, version)
    println("Encoded data bits: $encodedData")

    // Шаг 2: Преобразуем битовую строку в массив байт
    val dataBytes = bitStringToByteArray(encodedData)
    println("Encoded data bytes: ${dataBytes.joinToString(", ")}")

    // Шаг 3: Добавляем коррекцию ошибок
    val errorCorrectionWords = 10 // Задайте количество слов коррекции ошибок
    val rsEncoder = ErrorCorrection(errorCorrectionWords)
    val finalData = rsEncoder.encode(dataBytes)
    val byteArray = ByteArray(finalData.size) { i -> finalData[i].toByte() }


    println("Final encoded data with error correction: ${finalData.joinToString(", ")}")

    //Отрисовка QR кода
    val qrDrawer = QRCodeDrawer()
    // Рисуем QR-код и сохраняем в файл
    qrDrawer.drawQRCode(finalData, "D:\\ЛЭТИ\\АиСД\\Второй семестр\\Курсовая работа\\qrcode_v1.png")
    println("QR-код сохранён как 'qrcode_v1.png'.")
    /*val qrEncoder = QREncoder()

    val numericData = "01234567"
    val alphanumericData = "HELLO 123"
    val byteData = "Hello, World!"
    val kanjiData = "漢字"

    // Пример кодирования в различных режимах
    val numericEncoded = qrEncoder.encodeData(numericData, QREncoder.Mode.NUMERIC, version = 1)
    val alphanumericEncoded = qrEncoder.encodeData(alphanumericData, QREncoder.Mode.ALPHANUMERIC, version = 1)
    val byteEncoded = qrEncoder.encodeData(byteData, QREncoder.Mode.BYTE, version = 1)
    val kanjiEncoded = qrEncoder.encodeData(kanjiData, QREncoder.Mode.KANJI, version = 1)

    println("Numeric Encoded: $numericEncoded")
    println("Alphanumeric Encoded: $alphanumericEncoded")
    println("Byte Encoded: $byteEncoded")
    println("Kanji Encoded: $kanjiEncoded")*/
}