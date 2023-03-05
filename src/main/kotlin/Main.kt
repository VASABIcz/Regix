data class Lexer(val src: String) {
    var index = 0


    fun isPeek(str: String): Boolean {
        if (index + str.length > src.length) {
            return false
        }
        return src.slice(index until index+str.length) == str
    }

    fun peek(amount: Int = 1): String? {
        if (index + amount > src.length) {
            return null
        }

        return src.slice(index until index+amount)
    }

    fun consume(amount: Int = 1) {
        index += amount
    }
}

sealed interface Regix {
    data class Char(val c: kotlin.Char): Regix {
        override fun match(str: String, captures: MutableList<MutableList<String>>): String? {
            val c1 = str.getOrNull(0)
            return if (c1 == c) {
                c.toString()
            } else {
                null
            }
        }
    }

    data class Group(val inner: List<Regix>): Regix {
        override fun match(str: String, captures: MutableList<MutableList<String>>): String? {
            val matched = StringBuilder()
            inner.forEach {
                val res = it.match(str.drop(matched.length), captures)
                if (res == null) {
                    return null
                }
                else {
                    matched.append(res)
                }
            }
            return matched.toString()
        }
    }

    data class Or(val right: Regix, val left: Regix): Regix {
        override fun match(str: String, captures: MutableList<MutableList<String>>): String? {
            val l = left.match(str, captures)
            if (l != null) {
                return l
            }

            val r = right.match(str, captures)
            if (r != null) {
                return r
            }

            return null
        }
    }

    data class Capture(val inner: List<Regix>, val id: Int): Regix {
        override fun match(str: String, captures: MutableList<MutableList<String>>): String? {
            val matched = StringBuilder()
            val cs = captures.getOrElse(id) {
                val m = mutableListOf<String>()
                captures.add(m)
                m
            }

            inner.forEach {
                val res = it.match(str.drop(matched.length), captures)
                if (res == null) {
                    return null
                }
                else {
                    matched.append(res)
                }
            }
            cs.add(matched.toString())
            return matched.toString()
        }
    }

    data class Optional(val inner: Regix): Regix {
        override fun match(str: String, captures: MutableList<MutableList<String>>): String? {
            return inner.match(str, captures) ?: ""
        }
    }

    data class ZeroAndMore(val inner: Regix): Regix {
        override fun match(str: String, captures: MutableList<MutableList<String>>): String? {
            var offset = 0
            val buf = StringBuilder()
            while (true) {
                val res = inner.match(str.drop(offset), captures)
                if (res != null) {
                    buf.append(res)
                    offset += res.length
                }
                else {
                    return buf.toString()
                }
            }
        }
    }

    data class OneAndMore(val inner: Regix): Regix {
        override fun match(str: String, captures: MutableList<MutableList<String>>): String? {
            var offset = 0
            var matchCount = 0
            val buf = StringBuilder()
            while (true) {
                if (offset >= str.length) {
                    return if (matchCount < 1) {
                        null
                    } else {
                        buf.toString()
                    }
                }

                val res = inner.match(str.drop(offset), captures)
                if (res != null) {
                    buf.append(res)
                    offset += res.length
                    matchCount += 1
                }
                else {
                    return if (matchCount < 1) {
                        null
                    } else {
                        buf.toString()
                    }
                }
            }
        }
    }

    object Digits: Regix {
        override fun match(str: String, captures: MutableList<MutableList<String>>): String? {
            val c = str.getOrNull(0)
            if (c?.isDigit() != true) {
                return null
            }
            return c.toString()
        }
    }

    object Whitespace: Regix {
        override fun match(str: String, captures: MutableList<MutableList<String>>): String? {
            val c = str.getOrNull(0)
            if (c?.isWhitespace() != true) {
                return null
            }
            return c.toString()
        }
    }
    object Letters: Regix {
        override fun match(str: String, captures: MutableList<MutableList<String>>): String? {
            val c = str.getOrNull(0)
            if (c?.isLetter() != true) {
                return null
            }
            return c.toString()
        }
    }

    object Any: Regix {
        override fun match(str: String, captures: MutableList<MutableList<String>>): String? {
            return str.getOrNull(0)?.toString()
        }
    }

    fun print(space: Int = 0) {
        fun spacing(s: Int = space) {
            print("  ".repeat(s))
        }

        when (this) {
            is Capture -> {
                spacing()
                println("CAPTURE")
                this.inner.forEach {
                    it.print(space+1)
                }
            }
            is Char -> {
                spacing()
                println("CHAR (${this.c})")
            }
            Digits -> {
                spacing()
                println("DIGITS")
            }
            Letters -> {
                spacing()
                println("LETTERS")
            }
            is OneAndMore -> {
                spacing()
                println("1..")
                this.inner.print(space+1)
            }
            is Optional -> {
                spacing()
                println("OPTIONAL")
                this.inner.print(space+1)
            }
            is Or -> {
                spacing()
                println("OR")
                spacing(space+1)
                println("A:")
                this.left.print(space+2)
                spacing(space+1)
                println("B:")
                this.right.print(space+2)
            }
            Whitespace -> {
                spacing()
                println("WHITESPACE")
            }
            is ZeroAndMore -> {
                spacing()
                println("0..")
                this.inner.print(space+1)
            }
            is Group -> {
                this.inner.forEach {
                    it.print(space)
                }
            }

            Any -> {
                spacing()
                println("ANY")
            }
        }
    }

    fun match(str: String, captures: MutableList<MutableList<String>>): String?
}

data class Wrapper<T>(var value: T)

fun parseRegix2(l: Lexer, previous: MutableList<Regix>, groups: Wrapper<Int>) {
    val c = l.peek()!!.first()
    val r = when (c) {
        '(' -> {
            l.consume() // (

            val buf = mutableListOf<Regix>()
            while (!l.isPeek(")")) {
                parseRegix2(l, buf, groups)
            }
            l.consume()
            Regix.Capture(buf, groups.value++)
        }
        '[' -> {
            l.consume() // (

            val buf = mutableListOf<Regix>()
            while (!l.isPeek("]")) {
                parseRegix2(l, buf, groups)
            }
            l.consume()
            Regix.Group(buf)
        }
        '|' -> {
            val left = previous.removeLast()
            l.consume()

            val right = mutableListOf<Regix>()
            parseRegix2(l, right, groups)

            if (right.size > 1) {
                throw Throwable()
            }

            Regix.Or(right.last(), left)
        }
        '?' -> {
            l.consume()
            Regix.Optional(previous.removeLast())
        }
        '*' -> {
            l.consume()
            Regix.ZeroAndMore(previous.removeLast())
        }
        '+' -> {
            l.consume()
            Regix.OneAndMore(previous.removeLast())
        }
        '.' -> {
            l.consume()
            Regix.Any
        }
        '\\' -> {
            l.consume()
            val peek = l.peek()?.first() ?: throw Throwable()
            l.consume()
            when (peek) {
                'l' -> Regix.Letters
                'd' -> Regix.Digits
                'w' -> Regix.Whitespace
                else -> Regix.Char(peek)
            }
        }
        else -> {
            l.consume()
            Regix.Char(c)
        }
    }
    previous.add(r)
}

fun constructRegix2(l: Lexer): List<Regix> {
    val buf = mutableListOf<Regix>()
    val g = Wrapper(0)
    while (l.peek() != null) {
        parseRegix2(l, buf, g)
    }

    return buf
}

fun main() {
    val test = "-8588.9"
    val res = constructRegix2(Lexer("(-|\\+)?(\\d*)\\.?(\\d*)"))
    res.forEach {
        it.print()
    }
    var capturedAmount = 0
    val cs = mutableListOf<MutableList<String>>()
    res.forEach {
        val r = it.match(test.drop(capturedAmount), cs)
        capturedAmount += r!!.length
    }
    println(cs)
}