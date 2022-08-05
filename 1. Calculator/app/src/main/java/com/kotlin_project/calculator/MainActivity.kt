package com.kotlin_project.calculator

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.room.Room
import com.kotlin_project.calculator.model.History
import java.util.*

class MainActivity : AppCompatActivity() {

    private val expressionTextView: TextView by lazy {
        findViewById<TextView>(R.id.expressionTextView)
    }

    private val resultTextView: TextView by lazy {
        findViewById<TextView>(R.id.resultTextView)
    }

    private val historyConstraintLayout: ConstraintLayout by lazy {
        findViewById<ConstraintLayout>(R.id.historyConstraintLayout)
    }

    private val historyLinearLayout: LinearLayout by lazy {
        findViewById<LinearLayout>(R.id.historyLinearLayout)
    }

    lateinit var db: AppDatabase

    private var numberOfOpenBracket = 0
    private var didPressResult = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "historyDB"
        ).build()
    }

    fun buttonClicked(v: View) {
        when (v.id) {
            R.id.zeroButton -> numberButtonClicked("0")
            R.id.oneButton -> numberButtonClicked("1")
            R.id.twoButton -> numberButtonClicked("2")
            R.id.threeButton -> numberButtonClicked("3")
            R.id.fourButton -> numberButtonClicked("4")
            R.id.fiveButton -> numberButtonClicked("5")
            R.id.sixButton -> numberButtonClicked("6")
            R.id.sevenButton -> numberButtonClicked("7")
            R.id.eightButton -> numberButtonClicked("8")
            R.id.nineButton -> numberButtonClicked("9")
            R.id.plusButton -> operatorButtonClicked("+")
            R.id.minusButton -> operatorButtonClicked("-")
            R.id.divideButton -> operatorButtonClicked("/")
            R.id.multiplyButton -> operatorButtonClicked("*")
            R.id.modButton -> operatorButtonClicked("%")
        }
    }

    private fun numberButtonClicked(number: String) {
        if (didPressResult) {
            expressionTextView.text = ""
            resultTextView.text = ""
            didPressResult = false
        }

        if (expressionTextView.text.isNotEmpty() && expressionTextView.text.length >= 20) {
            Toast.makeText(this, "20자리 까지만 입력할 수 있습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        if (expressionTextView.text.isEmpty() && number == "0")
            return

        if (expressionTextView.text.length > 1 && expressionTextView.text.get(expressionTextView.text.length - 1) == ')') {
            expressionTextView.append("*$number")

            val ssb = SpannableStringBuilder(expressionTextView.text)
            ssb.setSpan(
                ForegroundColorSpan(resources.getColor(R.color.button_color_green)),
                expressionTextView.text.length - 2,
                expressionTextView.text.length - 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            expressionTextView.text = ssb
        } else
            expressionTextView.append(number)

        resultTextView.text = calculateExpression(expressionTextView.text.toString())

    }

    private fun operatorButtonClicked(operator: String) {
        didPressResult = false

        if (expressionTextView.text.isNotEmpty() && expressionTextView.text.length >= 20) {
            Toast.makeText(this, "20자리 까지만 입력할 수 있습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        if (expressionTextView.text.isEmpty())
            return

        if (isOperator(expressionTextView.text.get(expressionTextView.text.length - 1)) && expressionTextView.text.get(
                expressionTextView.text.length - 1
            ) != ')'
        )
            return

        expressionTextView.append("$operator")

        val ssb = SpannableStringBuilder(expressionTextView.text)
        ssb.setSpan(
            ForegroundColorSpan(resources.getColor(R.color.button_color_green)),
            expressionTextView.text.length - 1,
            expressionTextView.text.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        expressionTextView.text = ssb
        resultTextView.text = ""
    }

    fun bracketButtonClicked(v: View) {
        var ssb_from = 0
        var ssb_to = 0

        didPressResult = false

        var result = ""
        // Case 1 : Open bracket on empty expression
        if (expressionTextView.text.isEmpty()) {
            numberOfOpenBracket += 1
            result = "("
        }

        if (expressionTextView.text.isNotEmpty() && expressionTextView.text.length >= 20) {
            Toast.makeText(this, "20자리 까지만 입력할 수 있습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // Case 2 : Open bracket following operator
        else if (isOperator(expressionTextView.text.get(expressionTextView.text.length - 1))) {
            if (expressionTextView.text.get(expressionTextView.text.length - 1) == ')' && numberOfOpenBracket != 0) {
                numberOfOpenBracket -= 1
                result = ")"
            } else {
                if (expressionTextView.text.get(expressionTextView.text.length - 1) == ')') {
                    numberOfOpenBracket += 1
                    result = "*("
                    ssb_from = -2
                    ssb_to = -1
                } else {
                    numberOfOpenBracket += 1
                    result = "("
                }

            }
        }

        // Case 3 : Open bracket with multiplication following operand
        else if (!isOperator(expressionTextView.text.get(expressionTextView.text.length - 1)) && numberOfOpenBracket == 0) {
            numberOfOpenBracket += 1
            result = "*("
            ssb_from = -2
            ssb_to = -1
        } else if (numberOfOpenBracket != 0) {
            numberOfOpenBracket -= 1
            result = ")"
        }

        expressionTextView.append(result)

        val ssb = SpannableStringBuilder(expressionTextView.text)
        ssb.setSpan(
            ForegroundColorSpan(resources.getColor(R.color.button_color_green)),
            expressionTextView.text.length + ssb_from,
            expressionTextView.text.length + ssb_to,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        expressionTextView.text = ssb
    }

    fun resultButtonClicked(v: View) {
        if (!didPressResult) {
            if (expressionTextView.text.isEmpty()) {
                Toast.makeText(this, "계산식을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return
            }

            if (isOperator(expressionTextView.text.get(expressionTextView.text.length - 1)) && expressionTextView.text.get(
                    expressionTextView.text.length - 1
                ) != ')'
            ) {
                Toast.makeText(this, "계산식을 완성해주세요.", Toast.LENGTH_SHORT).show()
                return
            }

            for (i in 1..numberOfOpenBracket)
                expressionTextView.append(")")

            val expression = expressionTextView.text.toString()
            val result = resultTextView.text.toString()

            Thread(Runnable {
                db.HistoryDAO().insertHistory(History(null, expression, result))
            }).start()

            expressionTextView.text = resultTextView.text
            resultTextView.text = ""

            numberOfOpenBracket = 0

            didPressResult = true
        }
    }

    fun historyButtonClicked(v: View) {
        historyConstraintLayout.isVisible = true
        historyLinearLayout.removeAllViews()

        Thread(Runnable {
            db.HistoryDAO().getAll().reversed().forEach {
                runOnUiThread {
                    val historyView =
                        LayoutInflater.from(this).inflate(R.layout.history_row, null, false)
                    historyView.findViewById<TextView>(R.id.expressionTextView).text = it.expression
                    historyView.findViewById<TextView>(R.id.resultTextView).text = "= ${it.result}"

                    historyLinearLayout.addView(historyView)
                }
            }
        }).start()
    }

    fun closeHistoryButtonClicked(v: View) {
        historyConstraintLayout.isVisible = false
    }

    fun clearHistoryButtonClicked(v: View) {
        historyLinearLayout.removeAllViews()

        Thread(Runnable {
            db.HistoryDAO().deleteAll()
        }).start()
    }

    fun clearButtonClicked(v: View) {
        expressionTextView.text = ""
        resultTextView.text = ""
        numberOfOpenBracket = 0
    }

    fun backSpaceButtonClicked(v: View) {
        if (expressionTextView.text.length <= 1) {
            expressionTextView.text = ""
            resultTextView.text = ""
            return
        }

        if (expressionTextView.text.get(expressionTextView.text.length - 1) == '(')
            numberOfOpenBracket -= 1
        else if (expressionTextView.text.get(expressionTextView.text.length - 1) == ')') {
            numberOfOpenBracket += 1
        }


        expressionTextView.text =
            expressionTextView.text.slice(0..expressionTextView.text.length - 2)

        resultTextView.text = calculateExpression(expressionTextView.text.toString())


    }

    private fun calculateExpression(exp: String): String {
        if (isOperator(expressionTextView.text.get(expressionTextView.text.length - 1)))
            return ""

        val operands: Stack<Int> = Stack() //Operand stack

        val operations: Stack<Char> = Stack() //Operator stack

        var i = 0
        while (i < exp.length) {
            var c: Char = exp.get(i)
            if (Character.isDigit(c)) //check if it is number
            {
                //Entry is Digit, and it could be greater than a one-digit number
                var num = 0
                while (Character.isDigit(c)) {
                    num = num * 10 + (c - '0')
                    i++
                    c = if (i < exp.length) {
                        exp.get(i)
                    } else {
                        break
                    }
                }
                i--
                operands.push(num)
            } else if (c == '(') {
                operations.push(c) //push character to operators stack
            } else if (c == ')') {
                while (operations.peek() !== '(') {
                    val output: Int = performOperation(operands, operations)
                    operands.push(output) //push result back to stack
                }
                operations.pop()
            } else if (isOperator(c)) {
                while (!operations.isEmpty() && precedence(c) <= precedence(operations.peek())) {
                    val output: Int = performOperation(operands, operations)
                    operands.push(output) //push result back to stack
                }
                operations.push(c) //push the current operator to stack
            }
            i++
        }

        while (!operations.isEmpty()) {
            if (operands.size < 2)
                break
            val output: Int = performOperation(operands, operations)
            operands.push(output) //push final result back to stack
        }


        return operands.pop().toString()
    }

    private fun performOperation(operands: Stack<Int>, operations: Stack<Char>): Int {
        if (operands.size >= 2) {
            val a = operands.pop()
            val b = operands.pop()
            var operation = operations.pop()
            while (operation == '(')
                operation = operations.pop()
            when (operation) {
                '+' -> return a + b
                '-' -> return b - a
                '*' -> return a * b
                '%' -> {
                    if (a == 0) {
                        return b
                    }
                    return b % a
                }
                '/' -> {
                    if (a == 0) {
                        return 0
                    }
                    return b / a
                }
            }
        }

        return 0
    }

    private fun isOperator(c: Char): Boolean {
        return c == '+' || c == '-' || c == '/' || c == '*' || c == '%' || c == '(' || c == ')'
    }

    private fun precedence(c: Char): Int {
        when (c) {
            '+', '-' -> return 1
            '*', '/', '%' -> return 2
        }
        return -1
    }
}