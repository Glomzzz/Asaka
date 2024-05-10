package com.skillw.asaka.core.error

import com.skillw.asaka.core.Span

private val regex = Regex("[\u3091-\uFFe5]")

/** 获取字符串的实际长度 坏黑提供的函数 */
@Suppress("DEPRECATION")
private fun realLength(str: String): Int {
    return str.sumBy { (if (it.toString().matches(regex)) 2 else 1) }
}

fun generateMessage(vararg sources: Span): String {
    if (sources.isEmpty() || sources[0].isEmpty) {
        return " at unknown location"
    }
    return StringBuilder().apply {
        for (source in sources) {
            if (source.native != null) {
                append("\n   ${source.native}\n")
                continue
            }
            append("\n")
            val range = source.index
            val lineNum = source.line
            val script = source.script
            val (start, end) = range.first to range.last
            val lines = script.split("\n")
            val line = lines[lineNum]
            val column = start - lines.subList(0, lineNum).sumOf { it.length + 1 }
            append("at line ${lineNum + 1}:\n")
            if (lineNum > 0) {
                append(lines[lineNum - 1]).append("\n")
            }
            append(line).append("\n")
            repeat(column) { append(" ") }
            repeat(end - start + 1) { append("^") }
            append("\n")
            if (lineNum < lines.size - 1) {
                append(lines[lineNum + 1]).append("\n")
            }
        }
    }.toString()
}