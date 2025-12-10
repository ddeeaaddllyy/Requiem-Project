package com.application.requiemproject.ui.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.application.requiemproject.R

class SearchFragment : Fragment(R.layout.fragment_search) {

    private val allQuestions = listOf(
        HelpItem("Как изменить пароль?", "Чтобы изменить пароль, перейдите в раздел 'Аккаунт' -> 'Настройки' -> 'Безопасность'. Введите старый пароль, а затем дважды новый."),
        HelpItem("Как оплатить Премиум?", "Мы принимаем карты Visa и MasterCard. Нажмите на иконку 'Премиум' в профиле, выберите тариф и следуйте инструкциям."),
        HelpItem("Приложение вылетает", "Попробуйте очистить кэш приложения в настройках телефона. Если проблема сохраняется, переустановите приложение."),
        HelpItem("Как сменить язык?", "Язык перевода выбирается на главном экране. Язык интерфейса соответствует языку вашего телефона.")
    )

    private lateinit var adapter: HelpAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val searchInput = view.findViewById<EditText>(R.id.input_search)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_search_results)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())


        adapter = HelpAdapter(allQuestions) { selectedItem ->
            showSolutionBottomSheet(selectedItem)
        }

        recyclerView.adapter = adapter

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()

                if (query.isNotEmpty()) {
                    performSearch(query)
                }
            }
        })
    }

    private fun performSearch(query: String) {
        val filteredList = if (query.isEmpty()) {
            allQuestions
        } else {
            allQuestions.filter { it.question.contains(query, ignoreCase = true) }
        }
        adapter.updateList(filteredList)
    }

    private fun showSolutionBottomSheet(item: HelpItem) {
        val bottomSheet = SolutionBottomSheet.newInstance(item.solution)
        bottomSheet.show(parentFragmentManager, "SolutionSheet")
    }

}