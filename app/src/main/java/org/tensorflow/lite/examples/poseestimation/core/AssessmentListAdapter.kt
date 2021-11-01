package org.tensorflow.lite.examples.poseestimation.core

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import org.tensorflow.lite.examples.poseestimation.ExerciseListFragment
import org.tensorflow.lite.examples.poseestimation.R
import org.tensorflow.lite.examples.poseestimation.domain.model.TestId

class AssessmentListAdapter(
    private val testList: List<TestId>,
    private val manager: FragmentManager
) : RecyclerView.Adapter<AssessmentListAdapter.AssessmentItemViewHolder>() {

    class AssessmentItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemHolder: LinearLayout = view.findViewById(R.id.item_holder)
        val testId: TextView = view.findViewById(R.id.test_id)
        val exerciseCount: TextView = view.findViewById(R.id.exercise_count)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssessmentItemViewHolder {
        return AssessmentItemViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_test, parent, false)
        )
    }

    override fun onBindViewHolder(holder: AssessmentItemViewHolder, position: Int) {
        val item = testList[position]
        holder.apply {
            testId.text = testId.context.getString(R.string.test_id).format(item.id)
            exerciseCount.text =
                exerciseCount.context.getString(R.string.exercise_count).format(item.exercises.size)
        }
        if (item.exercises.isNotEmpty()) {
            holder.itemHolder.setOnClickListener {
                manager.beginTransaction().apply {
                    replace(R.id.fragment_container, ExerciseListFragment(item.id, item.exercises))
                    commit()
                }
            }
        } else {
            Toast.makeText(
                holder.itemHolder.context,
                "There is no exercise assigned for this test ID",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun getItemCount() = testList.size
}