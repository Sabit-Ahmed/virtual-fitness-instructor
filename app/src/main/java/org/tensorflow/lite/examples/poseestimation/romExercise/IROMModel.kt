package org.tensorflow.lite.examples.poseestimation.romExercise


import org.tensorflow.lite.examples.poseestimation.romExercise.data.MaskData
import org.tensorflow.lite.examples.poseestimation.romExercise.data.MaskDetails
import com.google.mlkit.vision.segmentation.SegmentationMask
import java.nio.ByteBuffer

interface IROMModel{
    fun getModelMask(modelMask: SegmentationMask) : List<MaskData>
    fun getMaskData(maskHeight:Int, maskWidth:Int, mask:ByteBuffer) : List<MaskDetails>
}