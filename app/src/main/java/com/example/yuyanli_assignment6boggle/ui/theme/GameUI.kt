package com.example.yuyanli_assignment6boggle.ui.theme

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yuyanli_assignment6boggle.Game
import kotlin.math.sqrt


@Composable
fun BoardUI(game: Game, context: android.content.Context){
// State to hold the text value
    var boardSize by remember { mutableIntStateOf(game.BOARD_SIZE) }
    var text by remember { mutableStateOf(game.word) }
    var board by remember {mutableStateOf(game.getBoard())}
    val buttonColors = remember { mutableStateOf(List(boardSize) { List(boardSize) { Color.LightGray } }) }
    var score by remember { mutableIntStateOf(0) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        Greeting(name = "Hit Buttons to Form Words")
        for (i in 0 until boardSize) {
            Row {
                for (j in 0 until boardSize) {
                    LetterButton(i, j, game, board, buttonColors,context) { newWord ->
                        // Update text when a button is clicked
                        text += newWord.toString()
                    }
                }
            }
        }
        Text(
            text = text,
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Row {
            ClearButton(resetState = {
                text = ""
                game.clearState()
                buttonColors.value = List(boardSize) { List(boardSize) { Color.LightGray } }
            })
            Spacer(modifier = Modifier.width(32.dp)) // Add spacing between buttons
            SubmitButton(onSubmitClicked = {

                game.submitWord()
                score = game.score
                text = ""
                game.clearState()
                buttonColors.value = List(boardSize) { List(boardSize) { Color.LightGray } }
            }
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Score", fontSize = 20.sp,)// ScoreText takes the left side of the first line
            Spacer(modifier = Modifier.width(8.dp)) // Add a spacer to push the NewGameButton to the right
            Text(text = score.toString(), fontSize = 20.sp,)
            Spacer(modifier = Modifier.weight(1f)) // Add a spacer to push the NewGameButton to the right
        }
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.weight(1f)) // Add a spacer to push the NewGameButton to the right
            NewGameButton(newGame = {
                game.resetGame()
                board = game.getBoard()
                score = game.score
                buttonColors.value = List(boardSize) { List(boardSize) { Color.LightGray } }
                text = game.word
            }) // NewGameButton takes the right side of the second line
        }
        Text(text = "For grading only:\n First line and second line is valid input.\n Third line is less than 4 char. \n Forth is insufficient vowel. \n Shake the device to reset the game",
            fontSize = 15.sp,
            textAlign = TextAlign.Left,
            modifier = Modifier.padding(vertical = 8.dp))

    }


    // Shake detection
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    val shakeListener = object : SensorEventListener {
        private val shakeThresholdGravity = 2.7F
        private var lastShakeTime: Long = 0

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

        override fun onSensorChanged(event: SensorEvent) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            val acceleration = sqrt(x * x + y * y + z * z.toDouble()) - SensorManager.GRAVITY_EARTH
            if (acceleration > shakeThresholdGravity) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastShakeTime > 1000) {
                    lastShakeTime = currentTime
                    // Call new game function here
                    game.resetGame()
                    board = game.getBoard()
                    score = game.score
                    buttonColors.value = List(boardSize) { List(boardSize) { Color.LightGray } }
                    text = game.word
                    Toast.makeText(context, "Shake detected!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Register shake listener
    sensorManager.registerListener(shakeListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)

    // Dispose the listener when composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            sensorManager.unregisterListener(shakeListener)
        }
    }

}


@Composable
fun NewGameButton(newGame: () -> Unit) {
    val context = LocalContext.current
    OutlinedButton(
        onClick = {
            newGame()
            Toast.makeText(context, "New Game Button clicked!", Toast.LENGTH_SHORT).show()
        },
        modifier = Modifier
            .padding(4.dp) // Add padding for the button
    ) {
        Text(
            text = "New Game",
            color = Color.Black
        )
    }
}



@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "$name!",
        fontSize = 20.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(vertical = 16.dp)
    )
}

@Composable
fun SubmitButton(onSubmitClicked: () -> Unit) {
    OutlinedButton(
        onClick = {
            onSubmitClicked()
        },
        modifier = Modifier
            .padding(8.dp) // Add padding for the button
    ) {
        Text(
            text = "Submit",
            color = Color.Black
        )
    }
}


@Composable
fun ClearButton(resetState: () -> Unit) {
    val context = LocalContext.current
    OutlinedButton(
        onClick = {
            resetState()
            Toast.makeText(context, "Clear!", Toast.LENGTH_SHORT).show()

        },
        modifier = Modifier
            .padding(8.dp) // Add padding for the button
    ) {
        Text(
            text = "Clear",
            color = Color.Black
        )
    }
}



@Composable
fun LetterButton(
    y: Int,
    x: Int,
    game: Game,
    board: Array<CharArray>,
    buttonColors: MutableState<List<List<Color>>>,
    context: android.content.Context,
    param: (Any) -> Unit
){
    val boardSize = 8 // Change this to the desired board size
    val buttonSize = 40.dp // Adjust the button size as per requirement
    val fontSize = 12.sp // Adjust the font size for better readability
    TextButton(
        onClick = {
            if(game.isPositionClickable(x, y)){
                buttonColors.value = buttonColors.value.mapIndexed { rowIndex, row ->
                    row.mapIndexed { colIndex, color ->
                        if (rowIndex == y && colIndex == x) {
                            Color.Gray
                        } else {
                            color
                        }
                    }
                }
                param(board[y][x].toString())
            }
            else{
                Toast.makeText(context, "Invalid Move", Toast.LENGTH_SHORT).show()
            }
        },
        colors = ButtonDefaults.textButtonColors(
            contentColor = Color.Black
        ),
        modifier = Modifier
            .size(buttonSize) // Set the size of the button
            .background(buttonColors.value[y][x])
            .border(BorderStroke(2.dp, Color.Black)) // Adjust border thickness and color
            .padding(4.dp) // Adjust padding for better spacing
    ) {
        Text(
            text = game.getBoard()[y][x].toString(),
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = fontSize // Set the font size
            )
        )
    }
}
