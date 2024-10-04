class ErrorCorrection(private val errorCorrectionWords: Int) {

    // Поле Галуа с размером 256
    private val gfSize = 256
    private val gfPoly = 0x11D // Несократимый полином для GF(256)
    private val gfExp = IntArray(gfSize * 2) // Экспоненциальная таблица
    private val gfLog = IntArray(gfSize) // Логарифмическая таблица

    init {
        // Инициализация таблиц экспонент и логарифмов
        initializeGaloisField()
    }

    private fun initializeGaloisField() {
        var x = 1
        for (i in 0..<gfSize) {
            gfExp[i] = x
            gfLog[x] = i

            x = x shl 1 // Умножаем x на альфа (x + 1 в GF(256))
            if (x >= gfSize) {
                x = x xor gfPoly // Применяем полином для приведения в поле
            }
        }
        for (i in gfSize..<gfExp.size) {
            gfExp[i] = gfExp[i - gfSize] // Дублируем экспоненциальную таблицу
        }
    }

    private fun gfMultiply(x: Int, y: Int): Int {
        if (x == 0 || y == 0) {
            return 0
        }
        return gfExp[(gfLog[x] + gfLog[y]) % (gfSize - 1)]
    }

    private fun gfDivide(x: Int, y: Int): Int {
        if (x == 0) {
            return 0
        }
        if (y == 0) {
            throw ArithmeticException("Division by zero in Galois Field")
        }
        return gfExp[(gfLog[x] + gfSize - 1 - gfLog[y]) % (gfSize - 1)]
    }

    private fun generateGeneratorPolynomial(ecWords: Int): IntArray {
        var generator = intArrayOf(1)
        for (i in 0..<ecWords) {
            val term = intArrayOf(1, gfExp[i])
            generator = multiplyPolynomials(generator, term)
        }
        return generator
    }

    private fun multiplyPolynomials(a: IntArray, b: IntArray): IntArray {
        val product = IntArray(a.size + b.size - 1)
        for (i in a.indices) {
            for (j in b.indices) {
                product[i + j] = product[i + j] xor gfMultiply(a[i], b[j])
            }
        }
        return product
    }

    private fun dividePolynomials(data: IntArray, generator: IntArray): IntArray {
        val dividend = data.copyOf(data.size + generator.size - 1)
        for (i in data.indices) {
            val coefficient = dividend[i]
            if (coefficient != 0) {
                for (j in generator.indices) {
                    dividend[i + j] = dividend[i + j] xor gfMultiply(generator[j], coefficient)
                }
            }
        }
        return dividend.copyOfRange(data.size, dividend.size)
    }

    fun encode(data: IntArray): IntArray {
        if (data.size + errorCorrectionWords >= gfSize) {
            throw IllegalArgumentException("Data length too long for given error correction words")
        }

        val generator = generateGeneratorPolynomial(errorCorrectionWords)
        val remainder = dividePolynomials(data, generator)
        return data + remainder
    }
}
