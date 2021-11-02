package org.tensorflow.lite.examples.poseestimation.romExercise

import android.util.Log
import org.tensorflow.lite.examples.poseestimation.romExercise.data.MaskData
import org.tensorflow.lite.examples.poseestimation.romExercise.data.MaskDetails
import org.tensorflow.lite.examples.poseestimation.romExercise.data.Point
import com.google.mlkit.vision.segmentation.SegmentationMask
import java.nio.ByteBuffer

class BaseROMExercise: IROMModel {

    override fun getModelMask(modelMask: SegmentationMask): List<MaskData> {
        val mask = modelMask.buffer
        val maskWidth = modelMask.width
        val maskHeight = modelMask.height
        return listOf(MaskData(maskHeight, maskWidth, mask))
    }

    override fun getMaskData(
        maskHeight: Int,
        maskWidth: Int,
        mask: ByteBuffer
    ): List<MaskDetails> {
//        var background = 0f
        var totalConfidence = 0f
        var topX: Float = ((maskHeight * maskWidth) + 100).toFloat()
        var topY: Float = ((maskHeight * maskWidth) + 100).toFloat()
        var bottomX: Float = -1f
        var bottomY: Float = -1f
        var leftX: Float = ((maskHeight * maskWidth) + 100).toFloat()
        var leftY: Float = ((maskHeight * maskWidth) + 100).toFloat()
        var rightX: Float = -1f
        var rightY: Float = -1f

        for (y in 0 until maskHeight) {
            for (x in 0 until maskWidth) {
                val byteBufferValue = mask.float
                totalConfidence += byteBufferValue
//                background = 1 - byteBufferValue
                if (byteBufferValue >= 0.8f) {
                    if (y < topY) {
                        topY = (y + 1).toFloat()
                        topX = (x + 1).toFloat()
                    }
                    if (y > bottomY) {
                        bottomY = (y + 1).toFloat()
                        bottomX = (x + 1).toFloat()
                    }
                    if (x < leftX){
                        leftX = (x + 1).toFloat()
                        leftY = (y + 1).toFloat()
                    }
                    if (x > rightX){
                        rightX = (x + 1).toFloat()
                        rightY = (y + 1).toFloat()
                    }
                }
            }
        }
        val pixelDifferenceY: Float = bottomY - topY
        val pixelDifferenceX: Float = rightX - leftX
        val topPoint = Point(topX, topY)
        val bottomPoint = Point(bottomX,bottomY)
        val leftPoint = Point(leftX,leftY)
        val rightPoint = Point(rightX,rightY)
        Log.d("maskQuestion", " pixel value: ($pixelDifferenceX, $pixelDifferenceY)")
        mask.rewind()
        return (listOf(MaskDetails(totalConfidence, pixelDifferenceX, pixelDifferenceY, topPoint, bottomPoint, leftPoint, rightPoint)))
    }
}