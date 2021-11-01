package org.tensorflow.lite.examples.poseestimation.exercise

import android.content.Context
import android.graphics.Color
import org.tensorflow.lite.examples.poseestimation.R
import org.tensorflow.lite.examples.poseestimation.core.Point
import org.tensorflow.lite.examples.poseestimation.core.Utilities
import org.tensorflow.lite.examples.poseestimation.domain.model.Person
import org.tensorflow.lite.examples.poseestimation.domain.model.Phase
import org.tensorflow.lite.examples.poseestimation.domain.model.Rule
import org.tensorflow.lite.examples.poseestimation.domain.model.RuleType

class IsometricCervicalExtension(
    context: Context
) : IExercise(
    context = context,
    id = 75,
    imageResourceId = R.drawable.isometric_cervical_extension
) {
    private var shoulderAngleDownMin = 0f
    private var shoulderAngleDownMax = 30f
    private var shoulderAngleUpMin = 115f
    private var shoulderAngleUpMax = 140f

    private var wrongShoulderAngleDownMin = 0f
    private var wrongShoulderAngleDownMax = 30f
    private var wrongShoulderAngleUpMin = 150f
    private var wrongShoulderAngleUpMax = 190f

    private val totalStates = 3
    private var rightStateIndex = 0
    private var wrongStateIndex = 0

    override fun exerciseCount(person: Person, canvasHeight: Int, canvasWidth: Int, phases: List<Phase>) {
        val leftShoulderPoint = Point(
            person.keyPoints[5].coordinate.x,
            -person.keyPoints[5].coordinate.y
        )
        val rightShoulderPoint = Point(
            person.keyPoints[6].coordinate.x,
            -person.keyPoints[6].coordinate.y
        )
        val leftElbowPoint = Point(
            person.keyPoints[7].coordinate.x,
            -person.keyPoints[7].coordinate.y
        )
        val rightElbowPoint = Point(
            person.keyPoints[8].coordinate.x,
            -person.keyPoints[8].coordinate.y
        )
        val leftHipPoint = Point(
            person.keyPoints[11].coordinate.x,
            -person.keyPoints[11].coordinate.y
        )
        val rightHipPoint = Point(
            person.keyPoints[12].coordinate.x,
            -person.keyPoints[12].coordinate.y
        )
        if (phases.size >= 2) {
            shoulderAngleDownMin = phases[0].constraints[0].minValue.toFloat()
            shoulderAngleDownMax = phases[0].constraints[0].maxValue.toFloat()
            shoulderAngleUpMin = phases[1].constraints[0].minValue.toFloat()
            shoulderAngleUpMax = phases[1].constraints[0].maxValue.toFloat()
        } else {
            shoulderAngleDownMin = 0f
            shoulderAngleDownMax = 30f
            shoulderAngleUpMin = 115f
            shoulderAngleUpMax = 140f
        }

        val rightCountStates: Array<FloatArray> = arrayOf(
            floatArrayOf(
                shoulderAngleDownMin,
                shoulderAngleDownMax,
                shoulderAngleDownMin,
                shoulderAngleDownMax
            ),
            floatArrayOf(
                shoulderAngleUpMin,
                shoulderAngleUpMax,
                shoulderAngleUpMin,
                shoulderAngleUpMax
            ),
            floatArrayOf(
                shoulderAngleDownMin,
                shoulderAngleDownMax,
                shoulderAngleDownMin,
                shoulderAngleDownMax
            )
        )

        val leftShoulderAngle = Utilities.angle(leftElbowPoint, leftShoulderPoint, leftHipPoint, false)
        val rightShoulderAngle = Utilities.angle(rightElbowPoint, rightShoulderPoint, rightHipPoint, true)
        val insideBox = isInsideBox(person, canvasHeight, canvasWidth)

        if (leftShoulderAngle > rightCountStates[rightStateIndex][0]
            && leftShoulderAngle < rightCountStates[rightStateIndex][1]
            && rightShoulderAngle > rightCountStates[rightStateIndex][2]
            && rightShoulderAngle < rightCountStates[rightStateIndex][3]
            && insideBox
        ) {
            rightStateIndex += 1
            if (rightStateIndex == totalStates) {
                rightStateIndex = 0
                repetitionCount()
            }
        } else {
            if (!insideBox) {
                standInside()
            }
        }
    }

    override fun wrongExerciseCount(person: Person, canvasHeight: Int, canvasWidth: Int) {
        val leftShoulderPoint = Point(
            person.keyPoints[5].coordinate.x,
            -person.keyPoints[5].coordinate.y
        )
        val rightShoulderPoint = Point(
            person.keyPoints[6].coordinate.x,
            -person.keyPoints[6].coordinate.y
        )
        val leftElbowPoint = Point(
            person.keyPoints[7].coordinate.x,
            -person.keyPoints[7].coordinate.y
        )
        val rightElbowPoint = Point(
            person.keyPoints[8].coordinate.x,
            -person.keyPoints[8].coordinate.y
        )
        val leftHipPoint = Point(
            person.keyPoints[11].coordinate.x,
            -person.keyPoints[11].coordinate.y
        )
        val rightHipPoint = Point(
            person.keyPoints[12].coordinate.x,
            -person.keyPoints[12].coordinate.y
        )

        wrongShoulderAngleDownMin = shoulderAngleDownMin
        wrongShoulderAngleDownMax = shoulderAngleDownMax
        wrongShoulderAngleUpMin = shoulderAngleUpMin + 35
        wrongShoulderAngleUpMax = shoulderAngleUpMax + 50

        val wrongCountStates1: Array<FloatArray> = arrayOf(
            floatArrayOf(
                wrongShoulderAngleDownMin,
                wrongShoulderAngleDownMax,
                wrongShoulderAngleDownMin,
                wrongShoulderAngleDownMax
            ),
            floatArrayOf(
                wrongShoulderAngleUpMin,
                wrongShoulderAngleUpMax,
                wrongShoulderAngleUpMin,
                wrongShoulderAngleUpMax
            ),
            floatArrayOf(
                wrongShoulderAngleDownMin,
                wrongShoulderAngleDownMax,
                wrongShoulderAngleDownMin,
                wrongShoulderAngleDownMax
            )
        )
        val wrongCountStates2: Array<FloatArray> = arrayOf(
            floatArrayOf(
                wrongShoulderAngleDownMin,
                wrongShoulderAngleDownMax,
                wrongShoulderAngleDownMin,
                wrongShoulderAngleDownMax
            ),
            floatArrayOf(
                wrongShoulderAngleUpMin,
                wrongShoulderAngleUpMax,
                wrongShoulderAngleDownMin,
                wrongShoulderAngleDownMax
            ),
            floatArrayOf(
                wrongShoulderAngleDownMin,
                wrongShoulderAngleDownMax,
                wrongShoulderAngleDownMin,
                wrongShoulderAngleDownMax
            )
        )

        val leftShoulderAngle = Utilities.angle(leftElbowPoint, leftShoulderPoint, leftHipPoint)
        val rightShoulderAngle =
            Utilities.angle(rightElbowPoint, rightShoulderPoint, rightHipPoint, true)
        val insideBox = isInsideBox(person, canvasHeight, canvasWidth)
        if (
            (leftShoulderAngle > wrongCountStates1[wrongStateIndex][0]
                    && leftShoulderAngle < wrongCountStates1[wrongStateIndex][1]
                    && rightShoulderAngle > wrongCountStates1[wrongStateIndex][2]
                    && rightShoulderAngle < wrongCountStates1[wrongStateIndex][3])
            || (leftShoulderAngle > wrongCountStates2[wrongStateIndex][0]
                    && leftShoulderAngle < wrongCountStates2[wrongStateIndex][1]
                    && rightShoulderAngle > wrongCountStates2[wrongStateIndex][2]
                    && rightShoulderAngle < wrongCountStates2[wrongStateIndex][3])
            || (rightShoulderAngle > wrongCountStates2[wrongStateIndex][0]
                    && rightShoulderAngle < wrongCountStates2[wrongStateIndex][1]
                    && leftShoulderAngle > wrongCountStates2[wrongStateIndex][2]
                    && leftShoulderAngle < wrongCountStates2[wrongStateIndex][3])
            && insideBox
        ) {
            wrongStateIndex += 1
            if (wrongStateIndex == wrongCountStates1.size - 1) {
                rightStateIndex = 0
            }
            if (wrongStateIndex == wrongCountStates1.size) {
                wrongStateIndex = 0
                wrongCount()
            }
        }
    }

    override fun drawingRules(person: Person, phases: List<Phase>): List<Rule> {
        val leftShoulderPoint = Point(
            person.keyPoints[5].coordinate.x,
            person.keyPoints[5].coordinate.y
        )
        val rightShoulderPoint = Point(
            person.keyPoints[6].coordinate.x,
            person.keyPoints[6].coordinate.y
        )
        val leftElbowPoint = Point(
            person.keyPoints[7].coordinate.x,
            person.keyPoints[7].coordinate.y
        )
        val rightElbowPoint = Point(
            person.keyPoints[8].coordinate.x,
            person.keyPoints[8].coordinate.y
        )
        val leftHipPoint = Point(
            person.keyPoints[11].coordinate.x,
            person.keyPoints[11].coordinate.y
        )
        val rightHipPoint = Point(
            person.keyPoints[12].coordinate.x,
            person.keyPoints[12].coordinate.y
        )
        return mutableListOf(
            Rule(
                type = RuleType.ANGLE,
                startPoint = leftElbowPoint,
                middlePoint = leftShoulderPoint,
                endPoint = leftHipPoint,
                clockWise = false
            ),
            Rule(
                type = RuleType.ANGLE,
                startPoint = rightElbowPoint,
                middlePoint = rightShoulderPoint,
                endPoint = rightHipPoint,
                clockWise = true
            )
        )
    }

    override fun getBorderColor(person: Person, canvasHeight: Int, canvasWidth: Int): Int {
        return if (isInsideBox(person, canvasHeight, canvasWidth)) {
            Color.GREEN
        } else {
            Color.RED
        }
    }

    private fun isInsideBox(person: Person, canvasHeight: Int, canvasWidth: Int): Boolean {
        val left = canvasWidth * 2f / 20f
        val right = canvasWidth * 18.5f / 20f
        val top = canvasHeight * 2.5f / 20f
        val bottom = canvasHeight * 18.5f / 20f
        var rightPosition = true
        person.keyPoints.forEach {
            val x = it.coordinate.x
            val y = it.coordinate.y
            if (x < left || x > right || y < top || y > bottom) {
                rightPosition = false
            }
        }
        return rightPosition
    }
}