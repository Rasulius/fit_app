package com.android.fit_app

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.fit_app.databinding.ItemStepBinding
import java.text.SimpleDateFormat
import java.util.Locale

data class Step(val timestamp: Long, val stepCount: Int)

// Адаптер для отображения списка шагов (Время плюс шаг)
class StepAdapter(private val databaseHelper: DatabaseHelper) : RecyclerView.Adapter<StepAdapter.StepViewHolder>() {

    private val steps = mutableListOf<Step>()

    init {
        loadSteps()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadSteps() {
        steps.clear()
        val cursor = databaseHelper.getAllSteps()

        for (step in cursor) {
            steps.add(Step(step.timestamp, step.stepCount))
        }

        this.notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StepViewHolder {
        val binding = ItemStepBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StepViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StepViewHolder, position: Int) {
        val step = steps[position]
        holder.bind(step)
    }

    override fun getItemCount(): Int = steps.size

    class StepViewHolder(private val binding: ItemStepBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(step: Step) {
            binding.timeTextView.text = formatTime(step.timestamp)
            binding.stepCountTextView.text = step.stepCount.toString()
        }

        private fun formatTime(timeMillis: Long): String {
            val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            return dateFormat.format(timeMillis)
        }
    }
}