package com.zychimne.twozerofoureight

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.games.Games
import com.google.android.gms.games.GamesClient

class MainActivity : AppCompatActivity() {
    private var firstLoginAttempt = false
    private lateinit var view: MainView
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        view = MainView(this)
        val settings: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        view.hasSaveState = settings.getBoolean("save_state", false)
        if (savedInstanceState != null&& savedInstanceState.getBoolean("hasState")) {
            load()
        }
        setContentView(view)
        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { launcherRes ->
            val result: GoogleSignInResult =
                launcherRes.data?.let { Auth.GoogleSignInApi.getSignInResultFromIntent(it) } ?: return@registerForActivityResult
            if (!result.isSuccess) {
                val editor: SharedPreferences.Editor = PreferenceManager.getDefaultSharedPreferences(this).edit()
                editor.putBoolean(NO_LOGIN_PROMPT, true)
                editor.apply()
                println(result.status)
            } else {
                if (result.signInAccount != null) {
                    val client: GamesClient =
                        Games.getGamesClient(this@MainActivity, result.signInAccount!!)
                    view.let { client.setViewForPopups(it) }
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        when(keyCode){
            KeyEvent.KEYCODE_MENU->{
                return true
            }
            KeyEvent.KEYCODE_DPAD_DOWN->{
                view.game.move(2)
                return true
            }
            KeyEvent.KEYCODE_DPAD_UP->{
                view.game.move(0)
                return true
            }
            KeyEvent.KEYCODE_DPAD_LEFT->{
                view.game.move(3)
                return true
            }
            KeyEvent.KEYCODE_DPAD_RIGHT->{
                view.game.move(1)
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putBoolean("hasState", true)
        save()
    }

    override fun onPause() {
        super.onPause()
        save()
    }

    private fun save() {
        val settings: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val editor: SharedPreferences.Editor = settings.edit()
        val field = view.game.grid!!.tiles
        val undoField = view.game.grid!!.undoField
        editor.putInt(WIDTH, field.size)
        editor.putInt(HEIGHT, field.size)
        for (xx in field.indices) {
            for (yy in field[0].indices) {
                if (field[xx][yy] != null) {
                    field[xx][yy]?.let { editor.putInt("$xx $yy", it.value) }
                } else {
                    editor.putInt("$xx $yy", 0)
                }
                if (undoField[xx][yy] != null) {
                    undoField[xx][yy]?.let { editor.putInt("$UNDO_GRID$xx $yy", it.value) }
                } else {
                    editor.putInt("$UNDO_GRID$xx $yy", 0)
                }
            }
        }
        editor.putLong(SCORE, view.game.score)
        editor.putLong(HIGH_SCORE, view.game.highScore)
        editor.putLong(UNDO_SCORE, view.game.lastScore)
        editor.putBoolean(CAN_UNDO, view.game.canUndo)
        editor.putInt(GAME_STATE, view.game.gameState)
        editor.putInt(UNDO_GAME_STATE, view.game.lastGameState)
        editor.apply()
    }

    override fun onResume() {
        super.onResume()
        load()
    }

    private fun load() {
        //Stopping all animations
        view.game.aGrid?.cancelAnimations()
        val settings: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        for (xx in view.game.grid!!.tiles.indices) {
            for (yy in view.game.grid!!.tiles[0].indices) {
                val value: Int = settings.getInt("$xx $yy", -1)
                if (value > 0) {
                    view.game.grid!!.tiles[xx][yy] = Tile(xx, yy, value)
                } else if (value == 0) {
                    view.game.grid!!.tiles[xx][yy] = null
                }
                val undoValue: Int = settings.getInt("$UNDO_GRID$xx $yy", -1)
                if (undoValue > 0) {
                    view.game.grid!!.undoField[xx][yy] = Tile(xx, yy, undoValue)
                } else if (value == 0) {
                    view.game.grid!!.undoField[xx][yy] = null
                }
            }
        }
        view.game.score = settings.getLong(SCORE, view.game.score)
        view.game.highScore = settings.getLong(HIGH_SCORE, view.game.highScore)
        view.game.lastScore = settings.getLong(UNDO_SCORE, view.game.lastScore)
        view.game.canUndo = settings.getBoolean(CAN_UNDO, view.game.canUndo)
        view.game.gameState = settings.getInt(GAME_STATE, view.game.gameState)
        view.game.lastGameState = settings.getInt(UNDO_GAME_STATE, view.game.lastGameState)
    }


    companion object {
        private const val WIDTH = "width"
        private const val HEIGHT = "height"
        private const val SCORE = "score"
        private const val HIGH_SCORE = "high score temp"
        private const val UNDO_SCORE = "undo score"
        private const val CAN_UNDO = "can undo"
        private const val UNDO_GRID = "undo"
        private const val GAME_STATE = "game state"
        private const val UNDO_GAME_STATE = "undo game state"
        private const val NO_LOGIN_PROMPT = "no_login_prompt"
    }
}