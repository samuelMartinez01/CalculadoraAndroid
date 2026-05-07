package com.ipam.calculadora

import android.icu.text.DecimalFormat
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ipam.calculadora.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var workingsTV: TextView
    private lateinit var resultTV: TextView
    private lateinit var formatoDecimal: DecimalFormat

    private lateinit var binding: ActivityMainBinding
    private var canAddOperation = false
    private var canAddDecimal = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(this.layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        formatoDecimal = DecimalFormat("#.##########")
        resultTV = binding.resultsTV
        workingsTV = binding.workingsTV
    }

    fun equalsAction(view: View) {
        val result = calculatorResults()
        if (result.isNotEmpty()) {
            resultTV.text = result
        }
    }

    private fun calculatorResults(): String {
        val text = workingsTV.text.toString()
        if (text.isEmpty()) return ""

        if (!isExpressionValid(text)) return "Error"

        try {
            val tokens = digitOperators()
            if (tokens.isEmpty()) return ""

            // Resolvemos paréntesis recursivamente
            val finalResult = evaluate(tokens)

            return when {
                finalResult.isNaN() -> "Error"
                finalResult.isInfinite() -> "Infinito"
                else -> formatoDecimal.format(finalResult)
            }
        } catch (e: Exception) {
            return "Error"
        }
    }

    private fun isExpressionValid(expression: String): Boolean {
        // 1. Balance de paréntesis
        var balance = 0
        for (c in expression) {
            if (c == '(') balance++
            if (c == ')') balance--
            if (balance < 0) return false
        }
        if (balance != 0) return false

        // 2. No puede terminar en operador o '('
        val last = expression.last()
        if (last == '(' || last == '+' || last == '-' || last.lowercaseChar() == 'x' || last == '/' || last == '.') {
            return false
        }

        // 3. No puede haber paréntesis vacíos "()"
        if (expression.contains("()")) return false

        return true
    }

    private fun evaluate(tokens: MutableList<Any>): Float {
        var list = tokens.toMutableList()
        
        // Mientras haya paréntesis de apertura, resolvemos el más interno primero
        while (list.contains('(')) {
            val openIndex = list.lastIndexOf('(')
            // Buscamos el cierre correspondiente después de ese índice
            var closeIndex = -1
            for (i in openIndex + 1 until list.size) {
                if (list[i] == ')') {
                    closeIndex = i
                    break
                }
            }
            
            if (closeIndex == -1) throw Exception("Paréntesis sin cerrar")

            val subList = list.subList(openIndex + 1, closeIndex).toMutableList()
            val result = calculateSimple(subList)
            
            // Reemplazamos el bloque "(...)" por el número resultante
            val newList = mutableListOf<Any>()
            newList.addAll(list.subList(0, openIndex))
            newList.add(result)
            newList.addAll(list.subList(closeIndex + 1, list.size))
            list = newList
        }
        
        return calculateSimple(list)
    }

    private fun calculateSimple(passedList: MutableList<Any>): Float {
        if (passedList.isEmpty()) return 0f
        val afterMultiDiv = timeDivisionCalculate(passedList)
        return addSubtractCalculate(afterMultiDiv)
    }

    private fun addSubtractCalculate(passedList: MutableList<Any>): Float {
        if (passedList.isEmpty()) return 0f
        var result = if (passedList[0] is Float) passedList[0] as Float else throw Exception("Sintaxis")

        for (i in passedList.indices) {
            if (passedList[i] is Char && i != passedList.lastIndex) {
                val operator = passedList[i]
                val nextDigit = passedList[i + 1] as? Float ?: throw Exception("Sintaxis")
                when (operator) {
                    '+' -> result += nextDigit
                    '-' -> result -= nextDigit
                }
            }
        }
        return result
    }

    private fun timeDivisionCalculate(passedList: MutableList<Any>): MutableList<Any> {
        var list = passedList.toMutableList()
        while (list.any { it is Char && (it.lowercaseChar() == 'x' || it == '/') }) {
            list = calcTimesDiv(list)
        }
        return list
    }

    private fun calcTimesDiv(passedList: MutableList<Any>): MutableList<Any> {
        val newList = mutableListOf<Any>()
        var skipNext = false

        for (i in passedList.indices) {
            if (skipNext) {
                skipNext = false
                continue
            }

            val current = passedList[i]
            if (current is Char && (current.lowercaseChar() == 'x' || current == '/')) {
                if (newList.isEmpty() || newList.last() !is Float) throw Exception("Sintaxis")
                val prevDigit = newList.removeAt(newList.size - 1) as Float
                
                if (i + 1 >= passedList.size || passedList[i + 1] !is Float) throw Exception("Sintaxis")
                val nextDigit = passedList[i + 1] as Float
                
                val result = if (current.lowercaseChar() == 'x') prevDigit * nextDigit else {
                    if (nextDigit == 0f) Float.NaN else prevDigit / nextDigit
                }
                newList.add(result)
                skipNext = true
            } else {
                newList.add(current)
            }
        }
        return newList
    }

    private fun digitOperators(): MutableList<Any> {
        val list = mutableListOf<Any>()
        var currentDigit = ""
        
        for (character in workingsTV.text) {
            if (character.isDigit() || character == '.') {
                // Multiplicación implícita si viene un número tras un ')'
                if (currentDigit.isEmpty() && list.isNotEmpty() && list.last() == ')') {
                    list.add('X')
                }
                currentDigit += character
            } else {
                if (currentDigit.isNotEmpty()) {
                    list.add(currentDigit.toFloat())
                    currentDigit = ""
                }
                
                // Multiplicación implícita antes de un '(' si hay un número o ')' previo
                if (character == '(' && list.isNotEmpty() && (list.last() is Float || list.last() == ')')) {
                    list.add('X')
                }
                
                list.add(character)
            }
        }
        
        if (currentDigit.isNotEmpty()) {
            list.add(currentDigit.toFloat())
        }
        return list
    }

    fun numberAction(view: View) {
        if (view is Button) {
            if (view.text == ".") {
                if (canAddDecimal) {
                    workingsTV.append(view.text)
                    canAddDecimal = false
                }
            } else {
                workingsTV.append(view.text)
            }
            canAddOperation = true
            resultTV.text = ""
        }
    }

    fun operationAction(view: View) {
        if (view is Button && canAddOperation) {
            workingsTV.append(view.text)
            canAddOperation = false
            canAddDecimal = true
            resultTV.text = ""
        }
    }

    fun parenthesisAction(view: View) {
        if (view is Button) {
            workingsTV.append(view.text)
            resultTV.text = ""
            // Permitimos operaciones después de un paréntesis de cierre
            if (view.text == "(") {
                canAddOperation = false
                canAddDecimal = true
            } else {
                canAddOperation = true
            }
        }
    }

    fun allClearAction(view: View) {
        workingsTV.text = ""
        resultTV.text = ""
        canAddOperation = false
        canAddDecimal = true
    }

    fun backSpaceAction(view: View) {
        val length = workingsTV.length()
        if (length > 0) {
            workingsTV.text = workingsTV.text.subSequence(0, length - 1)
            resultTV.text = ""
            // Al borrar, reseteamos permisos para no trabar al usuario
            canAddOperation = true
            canAddDecimal = true
        }
    }
}
