package org.tensorflow.lite.examples.poseestimation.core

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import org.tensorflow.lite.examples.poseestimation.R

class ExerciseGuidelineImageListAdapter(
    private val exerciseImageUrls: List<String>
) : RecyclerView.Adapter<ExerciseGuidelineImageListAdapter.ExerciseImageItemViewHolder>() {

    class ExerciseImageItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val exerciseImageUrlsView: ImageView =
            view.findViewById(R.id.item_exercise_guideline_image_list_container)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseImageItemViewHolder {
        return ExerciseImageItemViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_exercise_guideline_image_list, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ExerciseImageItemViewHolder, position: Int) {
        val imageUrl = exerciseImageUrls[position]
        Picasso.get().load(imageUrl).into(holder.exerciseImageUrlsView)
    }

    override fun getItemCount(): Int = exerciseImageUrls.size
}