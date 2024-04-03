package com.example.yuyanli_assignment6boggle

import android.content.Context
import android.util.Log
import android.widget.Toast
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

/**
 * A class representing the game logic for a Boggle game.
 * @property context The context of the application, used for loading resources and displaying messages.
 */
class Game(private val context: Context) {
    // Use GPT to help make the code more readable
    val BOARD_SIZE = 5
    private val MIN_WORD_LENGTH = 4
    private val PENALTY_POINTS = 10
    private val VOWEL_POINTS = 5
    private val CONSONANT_POINTS = 1
    private val DOUBLED_LETTERS_SCORE_MULTIPLIER = 2
    private val VOWELS = setOf('a', 'e', 'i', 'o', 'u')
    private val HIGH_SCORING_CONSONANTS = setOf('s', 'z', 'p', 'x', 'q')


    private var board = Array(BOARD_SIZE) { CharArray(BOARD_SIZE) }
    private var visited = Array(BOARD_SIZE) { BooleanArray(BOARD_SIZE) }
    var score = 0
        private set
    private val dictionary = mutableListOf<String>()
    private var lastX = -1
    private var lastY = -1
    var word = ""
        private set
    private val ansSet = mutableSetOf<String>()

    init {
        loadDictionary()
        initializeBoard()
    }

    /**
     * Loads the game dictionary from a raw resource file.
     */
    private fun loadDictionary() {
        val inputStream: InputStream = context.resources.openRawResource(R.raw.words)
        inputStream.bufferedReader().useLines { lines -> lines.forEach(dictionary::add) }
    }

    /**
     * Initializes the board with random letters, with some predefined values for testing.
     */
    private fun initializeBoard() {
        val alphabet = ('A'..'Z').toList()
        for (row in board.indices) {
            for (col in board[row].indices) {
                board[row][col] = alphabet.random()
            }
        }
        // Predefined start for easy testing. Remove in production or shuffle for actual gameplay.
        listOf("APPLE", "MAYBE","MIN","HOST").forEachIndexed { index, word ->
            word.forEachIndexed { charIndex, c ->
                board[index][charIndex] = c
            }
        }
    }

    /**
     * Determines if a given position on the board is clickable based on game rules.
     * @param x2 The x-coordinate of the position.
     * @param y2 The y-coordinate of the position.
     * @return True if the position is clickable, false otherwise.
     */
    fun isPositionClickable(x2: Int, y2: Int): Boolean {
        if (lastX == -1 && lastY == -1) {
            // First move is always valid.
            startNewWord(x2, y2)
            return true
        }

        if (isAdjacentAndUnvisited(x2, y2)) {
            addToWord(x2, y2)
            return true
        }

        return false
    }

    /**
     * Starts tracking a new word from the specified position.
     */
    private fun startNewWord(x: Int, y: Int) {
        lastX = x
        lastY = y
        visited[y][x] = true
        word += board[y][x]
    }

    /**
     * Adds a new letter to the current word from the specified position.
     */
    private fun addToWord(x: Int, y: Int) {
        lastX = x
        lastY = y
        visited[y][x] = true
        word += board[y][x]
    }

    /**
     * Checks if a given position is adjacent to the last one and hasn't been visited.
     */
    private fun isAdjacentAndUnvisited(x: Int, y: Int): Boolean {
        val dx = Math.abs(lastX - x)
        val dy = Math.abs(lastY - y)
        return dx in 0..1 && dy in 0..1 && !visited[y][x]
    }

    /**
     * Resets the game to its initial state.
     */
    fun resetGame() {
        initializeBoard()
        score = 0
        clearState()
    }

    /**
     * Clears the current game state, readying the game for a new word.
     */
    fun clearState() {
        visited = Array(BOARD_SIZE) { BooleanArray(BOARD_SIZE) }
        lastX = -1
        lastY = -1
        word = ""
    }

    /**
     * Submits the current word, checks its validity, and updates the score accordingly.
     * @return True if the word is valid and scored, false otherwise.
     */
    fun submitWord(): Boolean {
        word = word.lowercase()
        if (word.length < MIN_WORD_LENGTH) {
            showToast("Word must be at least $MIN_WORD_LENGTH characters long. -10 points.")
            adjustScoreForInvalidWord()
            return false
        }

        if (word in ansSet) {
            showToast("This word has already been entered.")
            return false
        }

        if (!isValidWord(word)) {
            adjustScoreForInvalidWord()
            return false
        }

        val wordScore = calculateScore(word)
        score += wordScore
        ansSet.add(word)
        showToast("Correct word! +$wordScore points.")
        return true
    }

    /**
     * Displays a Toast message.
     */
    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    /**
     * Checks if the word exists in the dictionary and meets the game's criteria for vowels and consonants.
     */
    private fun isValidWord(word: String): Boolean {
        if (!dictionary.contains(word)) {
            showToast("Incorrect word! -$PENALTY_POINTS points.")
            return false
        }

        val vowelCount = word.count { it in VOWELS }
        val consonantCount = word.length - vowelCount
        if (vowelCount < 2 || consonantCount < 1) {
            showToast("Word must contain at least 2 vowels and 1 consonant.")
            return false
        }

        return true
    }

    /**
     * Adjusts the score for submitting an invalid word.
     */
    private fun adjustScoreForInvalidWord() {
        score = maxOf(score - PENALTY_POINTS, 0)
    }

    /**
     * Calculates the score of the submitted word.
     */
    private fun calculateScore(word: String): Int {
        var thisScore = word.sumOf { letter ->
            if (letter in VOWELS) VOWEL_POINTS else CONSONANT_POINTS
        }

        if (word.any { it in HIGH_SCORING_CONSONANTS }) {
            thisScore *= DOUBLED_LETTERS_SCORE_MULTIPLIER
        }

        return thisScore
    }

    /**
     * Gets the current board.
     */
    fun getBoard(): Array<CharArray> = board
}

