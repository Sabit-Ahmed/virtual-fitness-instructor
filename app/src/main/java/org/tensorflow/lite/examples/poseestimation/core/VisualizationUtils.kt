package org.tensorflow.lite.examples.poseestimation.core


import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import org.tensorflow.lite.examples.poseestimation.domain.model.Rule
import org.tensorflow.lite.examples.poseestimation.domain.model.RuleType


object VisualizationUtils {
    private const val LINE_WIDTH = 3f
    private const val BORDER_WIDTH = 6f

    fun drawBodyKeyPoints(
        input: Bitmap,
        drawingRules: List<Rule>,
        repCount: Int,
        setCount: Int,
        wrongCount: Int,
        borderColor: Int = Color.GREEN,
        isFrontCamera: Boolean = false
    ): Bitmap {
        val output = input.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(output)
        if (isFrontCamera) {
            canvas.scale(-1f, 1f, canvas.width.toFloat() / 2, canvas.height.toFloat() / 2)
        }
        val draw = Draw(canvas, Color.WHITE, LINE_WIDTH)
        val width = draw.canvas.width
        val height = draw.canvas.height

        for (rule in drawingRules) {
            if (rule.type == RuleType.ANGLE) {
                if (isFrontCamera) {
                    draw.angle(
                        Point(
                            output.width - rule.startPoint.x,
                            rule.startPoint.y
                        ),
                        Point(
                            output.width - rule.middlePoint.x,
                            rule.middlePoint.y
                        ),
                        Point(
                            output.width - rule.endPoint.x,
                            rule.endPoint.y
                        ),
                        _clockWise = !rule.clockWise
                    )
                } else {
                    draw.angle(
                        rule.startPoint,
                        rule.middlePoint,
                        rule.endPoint,
                        _clockWise = rule.clockWise
                    )
                }
            } else {
                if (isFrontCamera) {
                    draw.line(
                        Point(
                            output.width - rule.startPoint.x,
                            rule.startPoint.y
                        ),
                        Point(
                            output.width - rule.endPoint.x,
                            rule.endPoint.y
                        ),
                        _color = rule.color
                    )
                } else {
                    draw.line(
                        rule.startPoint,
                        rule.endPoint,
                        _color = rule.color
                    )
                }
            }
        }
        draw.writeText(
            "$repCount / $setCount",
            Point(width * 1 / 7f, 60f),
            Color.rgb(19, 93, 148),//blue
            65f
        )
        draw.writeText(
            wrongCount.toString(),
            Point(width * 2.4f / 3f, 60f),
            Color.rgb(255, 0, 0),//green
            65f
        )
        if (borderColor != -1) {
            draw.rectangle(
                Point(width * 2f / 20f, height * 2.5f / 20f),
                Point(width * 18.5f / 20f, height * 2.5f / 20f),
                Point(width * 18.5f / 20f, height * 18.5f / 20f),
                Point(width * 2f / 20f, height * 18.5f / 20f),
                _color = borderColor,
                _thickness = BORDER_WIDTH
            )
        }
        return output
    }
}


