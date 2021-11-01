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

class PelvicBridge(
    context: Context
) : IExercise(
    context = context,
    id = 122,
    imageResourceId = R.drawable.pelvic_bridge
) {
    private var hipAngleDownMin = 115f
    private var hipAngleDownMax = 135f
    private var hipAngleUpMin = 160f
    private var hipAngleUpMax = 190f

    private var wrongHipAngleDownMin = 115f
    private var wrongHipAngleDownMax = 135f
    private var wrongHipAngleUpMin = 140f
    private var wrongHipAngleUpMax = 160f

    private val totalStates = 3
    private var rightStateIndex = 0
    private var wrongStateIndex = 0

    override fun exerciseCount(person: Person, canvasHeight: Int, canvasWidth: Int, phases: List<Phase>) {
        val rightShoulderPoint = Point(
            person.keyPoints[6].coordinate.x,
            -person.keyPoints[6].coordinate.y
        )
        val rightHipPoint = Point(
            person.keyPoints[12].coordinate.x,
            -person.keyPoints[12].coordinate.y
        )
        val rightKneePoint = Point(
            person.keyPoints[14].coordinate.x,
            -person.keyPoints[14].coordinate.y
        )
        if (phases.size >= 2) {
            hipAngleDownMin = phases[0].constraints[0].minValue.toFloat()
            hipAngleDownMax = phases[0].constraints[0].maxValue.toFloat()
            hipAngleUpMin = phases[1].constraints[0].minValue.toFloat()
            hipAngleUpMax = phases[1].constraints[0].maxValue.toFloat()
        } else {
            hipAngleDownMin = 115f
            hipAngleDownMax = 135f
            hipAngleUpMin = 160f
            hipAngleUpMax = 190f
        }
        val insideBox = isInsideBox(person, canvasHeight, canvasWidth)
        val hipAngle = Utilities.angle(rightShoulderPoint, rightHipPoint, rightKneePoint)
        val rightCountStates: Array<FloatArray> = arrayOf(
            floatArrayOf(
                hipAngleDownMin,
                hipAngleDownMax
            ),
            floatArrayOf(
                hipAngleUpMin,
                hipAngleUpMax
            ),
            floatArrayOf(
                hipAngleDownMin,
                hipAngleDownMax
            )
        )
        if (hipAngle > rightCountStates[rightStateIndex][0] && hipAngle < rightCountStates[rightStateIndex][1] && insideBox) {
            rightStateIndex += 1
            if (rightStateIndex == rightCountStates.size - 1) {
                wrongStateIndex = 0
            }
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
        val rightShoulderPoint = Point(
            person.keyPoints[6].coordinate.x,
            -person.keyPoints[6].coordinate.y
        )
        val rightHipPoint = Point(
            person.keyPoints[12].coordinate.x,
            -person.keyPoints[12].coordinate.y
        )
        val rightKneePoint = Point(
            person.keyPoints[14].coordinate.x,
            -person.keyPoints[14].coordinate.y
        )

        wrongHipAngleDownMin = hipAngleDownMin
        wrongHipAngleDownMax = hipAngleDownMax
        wrongHipAngleUpMin = hipAngleUpMin - 20
        wrongHipAngleUpMax = hipAngleUpMax - 30

        val wrongCountStates: Array<FloatArray> = arrayOf(
            floatArrayOf(
                wrongHipAngleDownMin,
                wrongHipAngleDownMax
            ),
            floatArrayOf(
                wrongHipAngleUpMin,
                wrongHipAngleUpMax
            ),
            floatArrayOf(
                wrongHipAngleDownMin,
                wrongHipAngleDownMax
            )
        )
        val insideBox = isInsideBox(person, canvasHeight, canvasWidth)
        val hipAngle = Utilities.angle(rightShoulderPoint, rightHipPoint, rightKneePoint)
        if (hipAngle > wrongCountStates[wrongStateIndex][0] && hipAngle < wrongCountStates[wrongStateIndex][1] && insideBox) {
            if (insideBox) {
                wrongStateIndex += 1
                if (wrongStateIndex == wrongCountStates.size) {
                    wrongStateIndex = 0
                    wrongCount()
                }
            }
        }
    }

    override fun drawingRules(person: Person, phases: List<Phase>): List<Rule> {
        val rightShoulderPoint = Point(
            person.keyPoints[6].coordinate.x,
            person.keyPoints[6].coordinate.y
        )
        val rightHipPoint = Point(
            person.keyPoints[12].coordinate.x,
            person.keyPoints[12].coordinate.y
        )
        val rightKneePoint = Point(
            person.keyPoints[14].coordinate.x,
            person.keyPoints[14].coordinate.y
        )
        return mutableListOf(
            Rule(
                type = RuleType.ANGLE,
                startPoint = rightShoulderPoint,
                middlePoint = rightHipPoint,
                endPoint = rightKneePoint
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