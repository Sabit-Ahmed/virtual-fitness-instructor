package org.tensorflow.lite.examples.poseestimation.romExercise.core

import org.tensorflow.lite.examples.poseestimation.romExercise.data.Line
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
        pointA: Point,
        pointB: Point
    ): Float {
        return pointA.x * pointB.y - pointA.y * pointB.x
    }

    fun lineIntersection(
        lineA: Line,
        lineB: Line
    ): Point {
        val xDiff = Point(lineA.startPoint.x - lineA.endPoint.x, lineB.startPoint.x - lineB.endPoint.x)
        val yDiff = Point(lineA.startPoint.y - lineA.endPoint.y, lineB.startPoint.y - lineB.endPoint.y)
        val divisor = det(xDiff, yDiff)
        val d = Point(det(lineA.endPoint, lineA.endPoint), det(lineB.endPoint, lineB.endPoint))
        val x = det(d, xDiff) / divisor
        val y = det(d, yDiff) / divisor
        return Point(x, y)
    }

    fun detectOrientation(pose){
        let count = 0;
        if (pose.keypoints[5].score > pose.keypoints[6].score) {
            count = count + 1;
        }
        if (pose.keypoints[11].score > pose.keypoints[12].score) {
            count = count + 1;
        }
        if (pose.keypoints[13].score > pose.keypoints[14].score) {
            count = count + 1;
        }
        if (count > 1) {
            return true;
        }
        else {
            return false;
        }

    }

    fun calculateProportion (pose, height) {
        height = height - 8.5; // nose to top of head=6 inch and ankle to floor 2.5 inch
        let lowerPoint;
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
        distance = calculateDistance(upperPoint, lowerPoint);
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

    fun processKeypoints (pose) {
        let keypointsDict = {};
        for (let i=0; i<pose.keypoints.length; i++){
            keypointsDict[pose.keypoints[i].name] = [pose.keypoints[i].x, pose.keypoints[i].y];
        }
        return keypointsDict;
    }

    fun roundArray (my_array) {
        let x = 0;
        let len = my_array.length
                while(x < len){
                    my_array[x] = my_array[x].toFixed(2);
                    x++
                }
        return my_array;
    }
}


