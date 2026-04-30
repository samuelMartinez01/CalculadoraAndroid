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
        val binding = ActivityMainBinding.inflate(this.layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        formatoDecimal = DecimalFormat("#.##########")
        resultTV = findViewById(R.id.resultsTV)
        workingsTV = findViewById(R.id.workingsTV)
    }

    /**
     * Punto de entrada para el cálculo.
     * Primero parseamos la string a una lista de tokens (números y operadores)
     * y luego aplicamos la jerarquía de operaciones.
     */
    fun equalsAction(view: View) {
        resultTV.text = calculatorResults()
    }

    private fun calculatorResults(): String {
        val tokens = digitOperators()
        if (tokens.isEmpty()) return ""

        // Prioridad 1: Multiplicación y División
        val afterMultiDiv = timeDivisionCalculate(tokens)
        if (afterMultiDiv.isEmpty()) return ""

        // Prioridad 2: Suma y Resta
        val finalResult = addSubtractCalculate(afterMultiDiv)

        return formatoDecimal.format(finalResult)
    }

    private fun addSubtractCalculate(passedList: MutableList<Any>): Float {
        var result = passedList[0] as Float

        for (i in passedList.indices) {
            if (passedList[i] is Char && i != passedList.lastIndex) {
                val operator = passedList[i]
                val nextDigit = passedList[i + 1] as Float
                when (operator) {
                    '+' -> result += nextDigit
                    '-' -> result -= nextDigit
                }
            }
        }
        return result
    }

    private fun timeDivisionCalculate(passedList: MutableList<Any>): MutableList<Any> {
        var list = passedList
        // Procesamos hasta que no queden operadores de alta prioridad
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
                // Sacamos el último número agregado para operarlo con el siguiente
                val prevDigit = newList.removeAt(newList.size - 1) as Float
                val nextDigit = passedList[i + 1] as Float
                
                val result = if (current.lowercaseChar() == 'x') prevDigit * nextDigit else prevDigit / nextDigit
                newList.add(result)
                skipNext = true
            } else {
                newList.add(current)
            }
        }
        return newList
    }

    /**
     * Convierte el texto del display en una lista tipada.
     * Separamos los operandos de los operadores para facilitar el procesamiento.
     */
    private fun digitOperators(): MutableList<Any> {
        val list = mutableListOf<Any>()
        var currentDigit = ""
        
        for (character in workingsTV.text) {
            if (character.isDigit() || character == '.') {
                currentDigit += character
            } else {
                // Si encontramos un operador, guardamos el número acumulado y luego el operador
                if (currentDigit.isNotEmpty()) {
                    list.add(currentDigit.toFloat())
                    currentDigit = ""
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
                    canAddDecimal = false // Evitamos doble punto en un mismo número
                    resultTV.text=""
                }
            } else {
                workingsTV.append(view.text)
                resultTV.text=""
            }
            canAddOperation = true
            resultTV.text=""
        }
    }

    fun operationAction(view: View) {
        if (view is Button && canAddOperation) {
            workingsTV.append(view.text)
            canAddOperation = false
            canAddDecimal = true // Al cambiar de operando, reseteamos el permiso del punto
            resultTV.text=""
        }
    }

    fun allClearAction(view: View) {
        workingsTV.text = ""
        resultTV.text = ""
        canAddOperation = false
        canAddDecimal = true
    }

    fun backSpaceAction(view: View) {
        val text=workingsTV.text.toString()
        val length = workingsTV.length()
        if (length > 0) {
        val lastChar=text[length-1]
            when{
                lastChar == '.'->{
                canAddDecimal=true
                }
                lastChar == '+' || lastChar == '-' || lastChar == 'X' || lastChar == '/' -> {
                    canAddOperation = true
                }
            }

            workingsTV.text = workingsTV.text.subSequence(0, length - 1)
            resultTV.text=""
        }
    }



}