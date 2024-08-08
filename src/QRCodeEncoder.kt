import java.nio.charset.Charset

class QREncoder {
    // Определение режимов кодирования
    enum class Mode(val modeIndicator: String) {
        NUMERIC("0001"),
        ALPHANUMERIC("0010"),
        BYTE("0100"),
        KANJI("1000")
    }

    // Таблица символов для буквенно-цифрового режима
    private val alphanumericTable = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ $%*+-./:"

    // Кодирование числового режима
    private fun encodeNumeric(data: String): String {
        val stringBuilder = StringBuilder()

        // Разбиваем строку на группы по 3 цифры
        var i = 0
        while (i < data.length) {
            val groupLength = minOf(3, data.length - i)
            val group = data.substring(i, i + groupLength)
            val binary = when (groupLength) {
                3 -> group.toInt().toString(2).padStart(10, '0')
                2 -> group.toInt().toString(2).padStart(7, '0')
                1 -> group.toInt().toString(2).padStart(4, '0')
                else -> throw IllegalArgumentException("Invalid group length")
            }
            stringBuilder.append(binary)
            i += groupLength
        }

        return stringBuilder.toString()
    }

    // Кодирование буквенно-цифрового режима
    private fun encodeAlphanumeric(data: String): String {
        val stringBuilder = StringBuilder()

        // Разбиваем строку на пары символов
        var i = 0
        while (i < data.length) {
            val groupLength = minOf(2, data.length - i)
            val group = data.substring(i, i + groupLength)

            val binary = when (groupLength) {
                2 -> {
                    val value1 = alphanumericTable.indexOf(group[0])
                    val value2 = alphanumericTable.indexOf(group[1])
                    ((45 * value1) + value2).toString(2).padStart(11, '0')
                }
                1 -> {
                    val value = alphanumericTable.indexOf(group[0])
                    value.toString(2).padStart(6, '0')
                }
                else -> throw IllegalArgumentException("Invalid group length")
            }
            stringBuilder.append(binary)
            i += groupLength
        }

        return stringBuilder.toString()
    }

    // Кодирование байтового режима
    private fun encodeByte(data: String, charset: Charset = Charsets.UTF_8): String {
        val stringBuilder = StringBuilder()
        val bytes = data.toByteArray(charset)

        // Преобразуем каждый байт в 8-битное двоичное число
        for (byte in bytes) {
            stringBuilder.append(byte.toUByte().toString(2).padStart(8, '0'))
        }

        return stringBuilder.toString()
    }

    // Кодирование кандзи режима
    private fun encodeKanji(data: String): String {
        val stringBuilder = StringBuilder()
        val sjisBytes = data.toByteArray(Charset.forName("Shift_JIS"))

        if (sjisBytes.size % 2 != 0) {
            throw IllegalArgumentException("Invalid Shift JIS encoding")
        }

        var i = 0
        while (i < sjisBytes.size) {
            val byte1 = sjisBytes[i].toInt() and 0xFF
            val byte2 = sjisBytes[i + 1].toInt() and 0xFF
            val combined = (byte1 shl 8) or byte2
            val adjusted = if (combined in 0x8140..0x9FFC) {
                combined - 0x8140
            } else if (combined in 0xE040..0xEBBF) {
                combined - 0xC140
            } else {
                throw IllegalArgumentException("Invalid Kanji character")
            }
            val binary = ((adjusted shr 8) * 0xC0 + (adjusted and 0xFF)).toString(2).padStart(13, '0')
            stringBuilder.append(binary)
            i += 2
        }

        return stringBuilder.toString()
    }

    // Добавление индикатора режима и длины данных
    fun encodeData(data: String, mode: Mode, version: Int): String {
        val encodedData = when (mode) {
            Mode.NUMERIC -> encodeNumeric(data)
            Mode.ALPHANUMERIC -> encodeAlphanumeric(data)
            Mode.BYTE -> encodeByte(data)
            Mode.KANJI -> encodeKanji(data)
        }

        // Добавление индикатора режима
        val modeIndicator = mode.modeIndicator

        // Определение длины поля в зависимости от версии
        val lengthBits = when (mode) {
            Mode.NUMERIC -> when (version) {
                in 1..9 -> 10
                in 10..26 -> 12
                in 27..40 -> 14
                else -> throw IllegalArgumentException("Invalid version")
            }
            Mode.ALPHANUMERIC -> when (version) {
                in 1..9 -> 9
                in 10..26 -> 11
                in 27..40 -> 13
                else -> throw IllegalArgumentException("Invalid version")
            }
            Mode.BYTE, Mode.KANJI -> when (version) {
                in 1..9 -> 8
                in 10..40 -> 16
                else -> throw IllegalArgumentException("Invalid version")
            }
        }

        // Добавление длины данных
        val dataLength = data.length
        val lengthIndicator = dataLength.toString(2).padStart(lengthBits, '0')

        // Объединение индикатора режима, длины данных и закодированных данных
        return modeIndicator + lengthIndicator + encodedData
    }
}