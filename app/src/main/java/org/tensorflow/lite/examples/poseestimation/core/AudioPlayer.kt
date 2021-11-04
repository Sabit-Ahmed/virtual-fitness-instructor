package org.tensorflow.lite.examples.poseestimation.core

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.annotation.RawRes
import org.tensorflow.lite.examples.poseestimation.R

class AudioPlayer(
    private val context: Context
) {
    fun play(@RawRes filepath: Int) {
        val player = MediaPlayer.create(context, filepath)
        player.start()
        player.setOnCompletionListener {
            player.release()
        }
    }
}