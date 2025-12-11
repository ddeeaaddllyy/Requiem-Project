package com.application.requiemproject.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.application.requiemproject.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SolutionBottomSheet : BottomSheetDialogFragment() {

    lateinit var contentText: TextView
    lateinit var questionTitle: TextView
    lateinit var closeButton: Button

    companion object {
        const val ARG_SOLUTION = "arg_solution"
        const val ARG_QUESTION = "arg_question"

        fun newInstance(question: String, solution: String) : SolutionBottomSheet {
            val fragment = SolutionBottomSheet()
            val args = Bundle()
            args.putString(ARG_QUESTION, question)
            args.putString(ARG_SOLUTION, solution)
            fragment.arguments = args
            return fragment
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_solution_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val solutionText = arguments?.getString(ARG_SOLUTION) ?: "Load error. . ."
        val questionText = arguments?.getString(ARG_QUESTION) ?: "Load error. . ."

        questionTitle = view.findViewById(R.id.question_sheet_title)
        contentText = view.findViewById(R.id.text_solution_content)
        closeButton = view.findViewById(R.id.button_close_sheet)

        contentText.text = solutionText
        questionTitle.text = questionText

        closeButton.setOnClickListener {
            dismiss()
        }
    }

    override fun getTheme(): Int = com.google.android.material.R.style.Theme_Design_BottomSheetDialog
}