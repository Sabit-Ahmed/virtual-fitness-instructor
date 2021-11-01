package org.tensorflow.lite.examples.poseestimation.exercise

import android.content.Context
import org.tensorflow.lite.examples.poseestimation.R
import org.tensorflow.lite.examples.poseestimation.core.AudioPlayer
import org.tensorflow.lite.examples.poseestimation.domain.model.Person
import org.tensorflow.lite.examples.poseestimation.domain.model.Phase
import org.tensorflow.lite.examples.poseestimation.domain.model.Rule

abstract class IExercise(
    context: Context,
    val id: Int,
    val imageResourceId: Int,
    private var maxRepCount: Int = 10,
    private var maxSetCount: Int = 1,
    var instruction: String = "",
    var imageUrls: List<String> = listOf(),
    val active: Boolean = true,
    var name: String = "",
    var description: String = "",
    var protocolId: Int = 0
) {
    private val audioPlayer = AudioPlayer(context)
    private var repetitionCounter = 0
    private var setCounter = 0
    private var wrongCounter = 0
    private var lastTimePlayed: Int = System.currentTimeMillis().toInt()

    abstract fun exerciseCount(person: Person, canvasHeight: Int, canvasWidth: Int, phases: List<Phase>)
    abstract fun wrongExerciseCount(person: Person, canvasHeight: Int, canvasWidth: Int)
    abstract fun drawingRules(person: Person, phases: List<Phase>): List<Rule>
    abstract fun getBorderColor(person: Person, canvasHeight: Int, canvasWidth: Int): Int

    fun repetitionCount() {
        repetitionCounter++
        val resourceId = when (repetitionCounter) {
            1 -> R.raw.one
            2 -> R.raw.two
            3 -> R.raw.three
            4 -> R.raw.four
            5 -> R.raw.five
            6 -> R.raw.six
            7 -> R.raw.seven
            8 -> R.raw.eight
            9 -> R.raw.nine
            10 -> R.raw.ten
            else -> R.raw.hello
        }
        audioPlayer.play(resourceId)
        if (repetitionCounter >= maxRepCount) {
            repetitionCounter = 0
            setCounter++
        }
    }

    fun wrongCount() {
        wrongCounter++
    }

    fun standInside() {
        val timestamp = System.currentTimeMillis().toInt()
        if (timestamp - lastTimePlayed >= 3500) {
            lastTimePlayed = timestamp
            audioPlayer.play(R.raw.stand_inside_box)
        }
    }

    fun handNotStraight() {
        val timestamp = System.currentTimeMillis().toInt()
        if (timestamp - lastTimePlayed >= 3500) {
            lastTimePlayed = timestamp
            audioPlayer.play(R.raw.keep_hand_straight)
        }
    }

    fun rightHandNotStraight() {
        val timestamp = System.currentTimeMillis().toInt()
        if (timestamp - lastTimePlayed >= 3500) {
            lastTimePlayed = timestamp
            audioPlayer.play(R.raw.right_hand_straight)
        }
    }

    fun leftHandNotStraight() {
        val timestamp = System.currentTimeMillis().toInt()
        if (timestamp - lastTimePlayed >= 3500) {
            lastTimePlayed = timestamp
            audioPlayer.play(R.raw.left_hand_straight)
        }
    }

    fun setExercise(
        exerciseName: String,
        exerciseDescription: String,
        exerciseInstruction: String,
        exerciseImageUrls: List<String>,
        repetitionLimit: Int,
        setLimit: Int,
        protoId: Int
    ) {
        name = exerciseName
        description = exerciseDescription
        maxRepCount = repetitionLimit
        maxSetCount = setLimit
        protocolId = protoId
        instruction = exerciseInstruction
        imageUrls = exerciseImageUrls
    }

    fun getRepetitionCount() = repetitionCounter

    fun getWrongCount() = wrongCounter

    fun getSetCount() = setCounter
}