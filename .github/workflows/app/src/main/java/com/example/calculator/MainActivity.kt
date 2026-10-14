package com.example.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { CalculatorApp() }
    }
}

@Composable
fun CalculatorApp() {
    var display by remember { mutableStateOf("0") }
    var currentInput by remember { mutableStateOf("") }

    val buttons = listOf(
        listOf("AC", "+/-", "%", "÷"),
        listOf("7", "8", "9", "×"),
        listOf("4", "5", "6", "−"),
        listOf("1", "2", "3", "+"),
        listOf("0", ",", "=")
    )

    Column(
        modifier = Modifier.fillMaxSize().background(Color.Black).padding(16.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(
            text = display,
            color = Color.White,
            fontSize = 80.sp,
            fontWeight = FontWeight.Light,
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
        
        buttons.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { label ->
                    val isOrange = label in listOf("÷", "×", "−", "+", "=")
                    val isLightGray = label in listOf("AC", "+/-", "%")
                    val isZero = label == "0"
                    
                    Button(
                        onClick = {
                            when (label) {
                                "AC" -> { display = "0"; currentInput = "" }
                                "=" -> {
                                    try {
                                        val expr = currentInput.replace("×", "*").replace("÷", "/").replace("−", "-").replace(",", ".")
                                        val result = eval(expr)
                                        display = result.toString()
                                        currentInput = result.toString()
                                    } catch (e: Exception) { display = "Ошибка" }
                                }
                                else -> {
                                    if (currentInput == "0" && label != ",") currentInput = label
                                    else currentInput += label
                                    display = currentInput
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when {
                                isOrange -> Color(0xFFFF9F0A)
                                isLightGray -> Color(0xFFA5A5A5)
                                else -> Color(0xFF333333)
                            }
                        ),
                        shape = CircleShape,
                        modifier = Modifier
                            .size(if (isZero) 160.dp else 75.dp)
                            .then(if (isZero) Modifier.width(160.dp) else Modifier)
                    ) {
                        Text(
                            text = label,
                            fontSize = 28.sp,
                            color = if (isLightGray) Color.Black else Color.White
                        )
                    }
                }
            }
        }
    }
}

fun eval(str: String): Double {
    return object : Any() {
        var pos = -1
        var ch = 0
        fun nextChar() { ch = if (++pos < str.length) str[pos].code else -1 }
        fun eat(charToEat: Int): Boolean {
            while (ch == ' '.code) nextChar()
            if (ch == charToEat) { nextChar(); return true }
            return false
        }
        fun parse(): Double {
            nextChar()
            val x = parseExpression()
            if (pos < str.length) throw RuntimeException("Unexpected: " + ch.toChar())
            return x
        }
        fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                if (eat('+'.code)) x += parseTerm()
                else if (eat('-'.code)) x -= parseTerm()
                else return x
            }
        }
        fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                if (eat('*'.code)) x *= parseFactor()
                else if (eat('/'.code)) x /= parseFactor()
                else return x
            }
        }
        fun parseFactor(): Double {
            if (eat('+'.code)) return parseFactor()
            if (eat('-'.code)) return -parseFactor()
            var x: Double
            val startPos = pos
            if (eat('('.code)) {
                x = parseExpression()
                eat(')'.code)
            } else if (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) {
                while (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) nextChar()
                x = str.substring(startPos, pos).toDouble()
            } else throw RuntimeException("Unexpected: " + ch.toChar())
            return x
        }
    }.parse()
}
