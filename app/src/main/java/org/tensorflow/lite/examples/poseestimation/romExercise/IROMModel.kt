package org.tensorflow.lite.examples.poseestimation.romExercise


import android.content.Context
import org.tensorflow.lite.examples.poseestimation.romExercise.data.MaskData
import org.tensorflow.lite.examples.poseestimation.romExercise.data.MaskDetails
import com.google.mlkit.vision.segmentation.SegmentationMask
import org.tensorflow.lite.examples.poseestimation.R
import org.tensorflow.lite.examples.poseestimation.core.AudioPlayer
import java.nio.ByteBuffer

abstract class IROMModel(
    context: Context
){
    private var lastTimePlayed: Int = System.currentTimeMillis().toInt()
    private val audioPlayer = AudioPlayer(context)

    abstract fun getModelMask(modelMask: SegmentationMask) : List<MaskData>
    abstract fun getMaskData(maskHeight:Int, maskWidth:Int, mask:ByteBuffer) : List<MaskDetails>

    fun comeForward(){
        val timestamp = System.currentTimeMillis().toInt()
        if (timestamp - lastTimePlayed >= 3500) {
            lastTimePlayed = timestamp
            audioPlayer.play(R.raw.please_come_forward)
        }
    }
}