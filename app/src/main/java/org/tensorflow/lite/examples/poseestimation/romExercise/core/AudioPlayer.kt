package org.tensorflow.lite.examples.poseestimation.romExercise.core

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import org.tensorflow.lite.examples.poseestimation.R


class AudioPlayer(
    private val context: Context,
    private var lastTimePlayed: Int = System.currentTimeMillis().toInt(),
) {
    private val player: MediaPlayer = MediaPlayer.create(this.context, R.raw.please_come_forward)

    fun play() {
        val timestamp = System.currentTimeMillis().toInt()
        if (timestamp - lastTimePlayed >= 3000) {
            Log.d("lastTimePlay", "${timestamp - lastTimePlayed}")
            player.start()
            player.setOnCompletionListener {this.context
                if (player != null && player.isPlaying) {
                    player.stop()
                    player.release()
                    try {
                        player.prepare()
                    }
                    catch (e: Exception) {
                        Log.d("error", "$e")
                    }

                }
            }
            lastTimePlayed = timestamp
        }

    }
}