package org.tensorflow.lite.examples.poseestimation.romExercise.core

import org.tensorflow.lite.examples.poseestimation.domain.model.BodyPart
import org.tensorflow.lite.examples.poseestimation.domain.model.KeyPoint
import org.tensorflow.lite.examples.poseestimation.domain.model.Person
import org.tensorflow.lite.examples.poseestimation.romExercise.data.Line
import org.tensorflow.lite.examples.poseestimation.romExercise.data.MaskDetails
import org.tensorflow.lite.examples.poseestimation.romExercise.data.Point
import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.sqrt

object ROMUtils {

    fun getDistance(startPoint: Point, endPoint: Point): Double {
        val squareDistance = ((startPoint.x - endPoint.x).toDouble()).pow(2.0) + ((startPoint.y - endPoint.y).toDouble()).pow(
            2.0
        )
        return sqrt(squareDistance)
    }

    fun getAngle(
        startPoint: Point,
        middlePoint: Point = Point(0f, 0f),
        endPoint: Point = Point(1f, 0f),
        clockWise: Boolean = false
    ): Float {
        if ((middlePoint != Point(0f, 0f)) && (endPoint != Point(1f, 0f))) {
            val vectorBA = Point(startPoint.x - middlePoint.x, startPoint.y - middlePoint.y)
            val vectorBC = Point(endPoint.x - middlePoint.x, endPoint.y - middlePoint.y)
            val vectorBAAngle = getAngle(vectorBA)
            val vectorBCAngle = getAngle(vectorBC)
            var angleValue = if (vectorBAAngle > vectorBCAngle) {
                vectorBAAngle - vectorBCAngle
            } else {
                360 + vectorBAAngle - vectorBCAngle
            }
            if (clockWise) {
                angleValue = 360 - angleValue
            }
            return angleValue
        } else {
            val x = startPoint.x
            val y = startPoint.y
            val magnitude = sqrt((x * x + y * y).toDouble())
            var angleValue = if (magnitude >= 0.0001) {
                acos(x / magnitude)
            } else {
                0
            }
            angleValue = Math.toDegrees(angleValue.toDouble())
            if (y < 0) {
                angleValue = 360 - angleValue
            }
            return angleValue.toFloat()
        }
    }

    fun det(
        xDiff: List<Float>,
        yDiff: List<Float>
    ): Float {
        return xDiff[0] * yDiff[1] - xDiff[1] * yDiff[0]
    }

    fun lineIntersection(
        lineA: Line,
        lineB: Line
    ): Point {

        val xDiff = listOf(lineA.startPoint.x - lineA.endPoint.x, lineB.startPoint.x - lineB.endPoint.x)
        val yDiff = listOf(lineA.startPoint.y - lineA.endPoint.y, lineB.startPoint.y - lineB.endPoint.y)
        val divisor = det(xDiff, yDiff)
        val dividend = listOf(det(listOf(lineA.startPoint.x, lineA.startPoint.y), listOf(lineA.endPoint.x, lineA.endPoint.y)),
            det(listOf(lineB.startPoint.x, lineB.startPoint.y), listOf(lineB.endPoint.x, lineB.endPoint.y)))
        val x = det(dividend, xDiff) / divisor
        val y = det(dividend, yDiff) / divisor

        return Point(x, y)
    }

    fun detectOrientation(keyPoints: List<KeyPoint>): Boolean {
        var count = 0
        if (keyPoints[BodyPart.LEFT_SHOULDER.position].score > keyPoints[BodyPart.RIGHT_SHOULDER.position].score) {
            count += 1
        }
        if (keyPoints[BodyPart.LEFT_HIP.position].score > keyPoints[BodyPart.RIGHT_HIP.position].score) {
            count += 1
        }
        if (keyPoints[BodyPart.LEFT_KNEE.position].score > keyPoints[BodyPart.RIGHT_KNEE.position].score) {
            count += 1
        }

        return count > 1
    }

    fun calculateProportion (
        keyPoints: List<KeyPoint>,
        originalHeightInch: Float,
        maskDetails: MaskDetails
    ) {

        val bottomPoint = Point(maskDetails.bottomPoint.x, keyPoints[BodyPart.RIGHT_KNEE.position].coordinate.y)
        val topPoint = Point(maskDetails.topPoint.x, keyPoints[BodyPart.RIGHT_KNEE.position].coordinate.y)

        let index;
        let keypointName = processKeypoints(pose);
        // let upperPoint = pose.keypoints[0];
        let upperPoint = keypointName.nose;
        if (pose.keypoints[15].score > pose.keypoints[16].score) {
            index = 15;
            lowerPoint = pose.keypoints[index];
        }
        else {
            index = 16;
            lowerPoint = pose.keypoints[index];
        }
        lowerPoint.x = pose.keypoints[0].x;
        lowerPoint = [lowerPoint.x, lowerPoint.y];
        distance = getDistance(upperPoint, lowerPoint);
        console.log("upper: ", upperPoint)
        console.log("lower: ", lowerPoint)
        console.log("distance: ", distance)
        let proportion = distance/height;
        return proportion;
    }

    fun calculateMiddlePoint (
        pointA: Point,
        pointB: Point
    ): Point {

        var middlePoint = Point(0f, 0f)
        middlePoint.x = ( pointA.x + pointB.x )/2
        middlePoint.y = ( pointA.y + pointB.y )/2

        return middlePoint
    }

    fun roundArray (myArray: Array<Any>): Array<Any> {

        for(i in 0..myArray.size){
            myArray[i] = String.format("%.2f", myArray[i])
        }

        return myArray;
    }
}


