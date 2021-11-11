package org.tensorflow.lite.examples.poseestimation.romExercise

import org.tensorflow.lite.examples.poseestimation.domain.model.BodyPart
import org.tensorflow.lite.examples.poseestimation.domain.model.KeyPoint
import org.tensorflow.lite.examples.poseestimation.romExercise.data.MaskDetails
import org.tensorflow.lite.examples.poseestimation.romExercise.core.ROMUtils
import org.tensorflow.lite.examples.poseestimation.romExercise.data.Point

class Calibration {

    fun getCalibrationMeasurement(
        keyPoints: List<KeyPoint>,
        maskDetails: MaskDetails,
        originalHeightInch: Double
    ): List<Double> {

        val proportion = ROMUtils.calculateProportion(keyPoints, maskDetails, originalHeightInch)

        val shoulderToShoulderDistance = ROMUtils.getDistance(Point(keyPoints[BodyPart.RIGHT_SHOULDER.position].coordinate.x,
            keyPoints[BodyPart.RIGHT_SHOULDER.position].coordinate.y),
            Point(keyPoints[BodyPart.LEFT_SHOULDER.position].coordinate.x,
                keyPoints[BodyPart.LEFT_SHOULDER.position].coordinate.y)) * proportion

        val rightShoulderToRightElbowDistance = ROMUtils.getDistance(Point(keyPoints[BodyPart.RIGHT_SHOULDER.position].coordinate.x,
            keyPoints[BodyPart.RIGHT_SHOULDER.position].coordinate.y),
            Point(keyPoints[BodyPart.RIGHT_ELBOW.position].coordinate.x,
                keyPoints[BodyPart.RIGHT_ELBOW.position].coordinate.y)) * proportion

        val leftShoulderToLeftElbowDistance = ROMUtils.getDistance(Point(keyPoints[BodyPart.LEFT_SHOULDER.position].coordinate.x,
            keyPoints[BodyPart.LEFT_SHOULDER.position].coordinate.y),
            Point(keyPoints[BodyPart.LEFT_ELBOW.position].coordinate.x,
                keyPoints[BodyPart.LEFT_ELBOW.position].coordinate.y)) * proportion

        val rightElbowToRightWristDistance = ROMUtils.getDistance(Point(keyPoints[BodyPart.RIGHT_ELBOW.position].coordinate.x,
            keyPoints[BodyPart.RIGHT_ELBOW.position].coordinate.y),
            Point(keyPoints[BodyPart.RIGHT_WRIST.position].coordinate.x,
                keyPoints[BodyPart.RIGHT_WRIST.position].coordinate.y)) * proportion

        val leftElbowToLeftWristDistance = ROMUtils.getDistance(Point(keyPoints[BodyPart.LEFT_ELBOW.position].coordinate.x,
            keyPoints[BodyPart.LEFT_ELBOW.position].coordinate.y),
            Point(keyPoints[BodyPart.LEFT_WRIST.position].coordinate.x,
                keyPoints[BodyPart.LEFT_WRIST.position].coordinate.y)) * proportion

        val shoulderToElbowDistance = (rightShoulderToRightElbowDistance + leftShoulderToLeftElbowDistance) / 2

        val elbowToWristDistance = (rightElbowToRightWristDistance + leftElbowToLeftWristDistance) / 2

        return listOf(shoulderToShoulderDistance, shoulderToElbowDistance, elbowToWristDistance)
    }

}