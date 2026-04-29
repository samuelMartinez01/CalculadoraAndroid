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

    fun equalsAction(view: View)
    {
        resultTV.text = calculatorResults()
    }

    private fun calculatorResults(): String
    {
        val digitsOperator = digitOperators()
        if (digitsOperator.isEmpty()) return ""

        val timeDivision = timeDivisionCalculate(digitsOperator)
        if (timeDivision.isEmpty()) return ""

        val result = addSubtractCalculate(timeDivision)

        return result.toString()
    }

    private fun addSubtractCalculate(passedList: MutableList<Any>): Float
    {
        var result = passedList[0] as Float

        for(i in passedList.indices)
        {
            if(passedList[i] is Char && i != passedList.lastIndex)
            {
                val operator = passedList[i]
                val nextDigit = passedList[i + 1] as Float
                if (operator == '+')
                    result += nextDigit
                if (operator == '-')
                    result -= nextDigit
            }
        }

        return result
    }

    private fun timeDivisionCalculate(passedList: MutableList<Any>): MutableList<Any> {
        var list = passedList
        while (list.contains('x') || list.contains('/'))
        {
            list = calcTimesDiv(list)
        }

        return list
    }

    private fun calcTimesDiv(passedList: MutableList<Any>): MutableList<Any> {
        val newList = mutableListOf<Any>()
        var restartIndex = passedList.size
        for (i in passedList.indices)
        {

            if(passedList[i] is Char && i != passedList.lastIndex && i < restartIndex)
            {
                val operator = passedList[i]
                val prevDigit = passedList[i - 1] as Float
                val nextDigit = passedList[i + 1] as Float
                when(operator)
                {
                    'x' ->
                    {
                        newList.add(prevDigit * nextDigit)
                        restartIndex = i + 1
                    }
                    '/' ->
                    {
                        newList.add(prevDigit / nextDigit)
                        restartIndex = i + 1
                    }
                    else ->
                    {
                        newList.add(prevDigit)
                        newList.add(operator)
                    }
                }
            }

            if(i > restartIndex)
                newList.add(passedList[i])


        }
        return newList
    }

    private fun digitOperators(): MutableList<Any>
    {
        val list = mutableListOf<Any>()
        var currentDigit = ""
        for (character in workingsTV.text)
            if (character.isDigit() || character == '.')
                currentDigit += character
            else
            {
                list.add(currentDigit.toFloat())
                currentDigit = ""
                list.add(character)
            }
        if (currentDigit != "")
            list.add(currentDigit.toFloat())
        return list
    }

    fun numberAction(view: View)
    {
        if (view is Button) {
            if(view.text == ".") {
                if(canAddDecimal)
                    workingsTV.append(view.text)
                canAddOperation = false
            }
            else
                workingsTV.append(view.text)

            canAddOperation = true
        }
    }
    fun operationAction(view: View)
    {
        if (view is Button && canAddOperation) {
            workingsTV.append(view.text)
            canAddOperation = false
            canAddDecimal = true
        }
    }
    fun allClearAction(view: View)
    {
        workingsTV.text = ""
        resultTV.text = ""
    }
    fun backSpaceAction(view: View)
    {
        val length = workingsTV.length()
        if (length > 0) {
            workingsTV.text = workingsTV.text.subSequence(0, length - 1) // eliminar una letra
        }
        }



}