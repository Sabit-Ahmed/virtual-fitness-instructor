package org.tensorflow.lite.examples.poseestimation.domain.model

data class ExerciseItem(
    val AssignReps: String?,
    val AssignSets: String?,
    val BodySubRegionList: String?,
    val CompletedReps: String,
    val CompletedSets: String,
    val CreatedOnUtc: String,
    val EvalExerciseMediaList: String?,
    val EvalExerciseTypeList: String?,
    val Exercise: String,
    val ExerciseCategory: String?,
    val ExercisesCategoriesList: String?,
    val FrequencyId: Int,
    val FrequencyList: String?,
    val FrequencyName: String,
    val HideBodyJoint: Boolean,
    val Id: Int,
    val Image: String?,
    val ImageUrl: List<String>,
    val Instructions: String,
    val IsActive: Boolean,
    val IsActiveList: String?,
    val IsPostureExercise: Boolean,
    val NoOfWrongCount: Int,
    val PhaseId: String,
    val PhaseName: String,
    val ProtocolId: Int,
    val ReportExerciseId: Int,
    val ReportName: String,
    val RepsId: Int,
    val RepsList: String?,
    val RepsName: String,
    val ResistanceId: Int,
    val ResistanceList: String?,
    val ResistanceName: String,
    val SavedFiles: String?,
    val SelectedBodySubRegion: String?,
    val SelectedEvalExerciseTypeList: String?,
    val SelectedExercisesCategoriesList: String?,
    val SelectedFrequencyList: Int,
    val SelectedIsActiveList: String?,
    val SelectedRepsList: Int,
    val SelectedResistanceList: Int,
    val SelectedSetList: Int,
    val SetId: Int,
    val SetList: String?,
    val SetName: String,
    val StartDate: String?,
    val TestId: String,
    val UpdatedOnUtc: String?
)