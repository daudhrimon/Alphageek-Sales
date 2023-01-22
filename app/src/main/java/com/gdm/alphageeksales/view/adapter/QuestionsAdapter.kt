package com.gdm.alphageeksales.view.adapter

import android.app.DatePickerDialog
import android.content.Context
import android.text.InputType
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.gdm.alphageeksales.R
import com.gdm.alphageeksales.data.local.down_sync.Questions
import com.gdm.alphageeksales.databinding.QuestionItemBinding
import com.gdm.alphageeksales.utils.isVISIBLE
import java.text.SimpleDateFormat
import java.util.*

class QuestionsAdapter(
    private val questionsList: List<Questions>,
    private val context: Context
) : RecyclerView.Adapter<QuestionsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = QuestionItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(questionsList[position])
    }
    override fun getItemCount(): Int { return  questionsList.size }

    inner class ViewHolder(private val binding: QuestionItemBinding) : RecyclerView.ViewHolder(binding.root) {
         fun onBind(questionItem: Questions) {
             when(questionItem.ans_type?:"") {
                 "text","number"-> {
                     binding.textLayout.isVISIBLE()
                     binding.textQuestion.text = "${adapterPosition+1}. ${questionItem.question}${
                         when {(questionItem.is_required?:0) == 1-> " (Required)" else -> ""}
                     }"
                     when(questionItem.ans_type?:""){
                         "text"-> binding.textAnswer.inputType = InputType.TYPE_CLASS_TEXT
                         "number"-> binding.textAnswer.inputType = InputType.TYPE_CLASS_NUMBER
                     }
                     questionItem.ans?.let { when{it.isNotEmpty()-> binding.textAnswer.setText(it)} }

                     binding.textAnswer.doAfterTextChanged {
                         if (it.isNullOrEmpty()) {
                             questionItem.ans = null
                         } else {
                             questionItem.ans = it.toString()
                         }
                     }
                 }
                 "date"-> {
                     binding.dateLayout.isVISIBLE()
                     binding.dateQuestion.text = "${adapterPosition+1}. ${questionItem.question}${
                         when {(questionItem.is_required?:0) == 1-> " (Required)" else -> ""}
                     }"
                     questionItem.ans?.let { when{it.isNotEmpty()-> binding.dateAnswer.text = it } }

                     binding.datePicker.setOnClickListener {
                         //setup calender
                         val calendar = Calendar.getInstance()
                         val dateSetListener = DatePickerDialog.OnDateSetListener { date_picker, year, monthOfYear, dayOfMonth ->
                             date_picker.minDate = System.currentTimeMillis() - 1000
                             calendar.set(Calendar.YEAR, year)
                             calendar.set(Calendar.MONTH, monthOfYear)
                             calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                             val myFormat = "yyyy-MM-dd"
                             val sdf = SimpleDateFormat(myFormat, Locale.ENGLISH)
                             questionItem.ans = sdf.format(calendar.time)
                             binding.dateAnswer.text = questionItem.ans
                         }
                         DatePickerDialog(
                             context,
                             R.style.Calender_dialog_theme,
                             dateSetListener,
                             calendar[Calendar.YEAR],
                             calendar[Calendar.MONTH],
                             calendar[Calendar.DAY_OF_MONTH]
                         ).show()
                     }
                 }
                 "checkbox"-> {
                     binding.ckBoxLayout.isVISIBLE()
                     binding.ckBoxQuestion.text = "${adapterPosition+1}. ${questionItem.question}${
                         when {(questionItem.is_required?:0) == 1-> " (Required)" else -> ""}
                     }"

                     val ckBoxAns: List<String>? = questionItem.ck_ans?.let{it.split(",").toList()}
                     ckBoxAns?.let {
                         it.forEachIndexed { index, ansItem ->
                             val radioButton = RadioButton(context)
                             radioButton.layoutParams = LinearLayout.LayoutParams(
                                 ViewGroup.LayoutParams.WRAP_CONTENT,
                                 ViewGroup.LayoutParams.WRAP_CONTENT
                             )
                             radioButton.isChecked = ansItem == (questionItem.ans?:"")
                             radioButton.text = ansItem
                             radioButton.id = index
                             binding.radioGroup.addView(radioButton)
                         }

                         binding.radioGroup.setOnCheckedChangeListener { _, radioId ->
                             questionItem.ans = it[radioId]
                         }
                     }
                 }
             }
         }
    }

    fun getAnswersList() = questionsList
}