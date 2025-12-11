package com.application.requiemproject.ui.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.application.requiemproject.R
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SearchFragment : Fragment(R.layout.fragment_search) {

    private lateinit var adapter: HelpAdapter
    private val helpDao by lazy {
        AppDatabase.getDatabase(requireContext()).helpDao()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView(view)
        setupSearchBar(view)
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_search_results)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = HelpAdapter(emptyList()) { selectedItem ->
            showSolutionBottomSheet(selectedItem)
        }
        recyclerView.adapter = adapter
    }

    private fun showSolutionBottomSheet(item: HelpItem) {
        val bottomSheet = SolutionBottomSheet.newInstance(
            question = item.question,
            solution = item.solution
        )
        bottomSheet.show(parentFragmentManager, "SolutionSheet")
    }

    private fun setupSearchBar(view: View){
        val searchInput = view.findViewById<EditText>(R.id.input_search)

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                observeQuestion(s.toString())
            }

        })

    }

    private fun observeQuestion(query: String){
        viewLifecycleOwner.lifecycleScope.launch {
            helpDao.searchHelpItems(query).collectLatest { items ->
                adapter.updateList(items)
            }
        }
    }

}