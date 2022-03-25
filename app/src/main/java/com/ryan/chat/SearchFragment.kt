package com.ryan.chat

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ryan.chat.databinding.FragmentSearchBinding
import com.ryan.chat.databinding.RowSearchroomBinding

class SearchFragment : Fragment() {
    companion object {
        val TAG = SearchFragment::class.java.simpleName
        val instance : SearchFragment by lazy {
            SearchFragment()
        }
    }
    lateinit var binding: FragmentSearchBinding
    lateinit var adapter : SearchRoomAdapter
    val roomViewModel by viewModels<RoomViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.searchRecycler.setHasFixedSize(true)
        binding.searchRecycler.layoutManager = GridLayoutManager(requireContext(), 2)
        adapter = SearchRoomAdapter()
        binding.searchRecycler.adapter = adapter

        roomViewModel.searchRooms.observe(viewLifecycleOwner) { rooms ->
            adapter.submitRooms(rooms)
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val keywords = binding.searchView.query.toString()
                roomViewModel.getSearchRooms(keywords)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val keywords = binding.searchView.query.toString()
                roomViewModel.getSearchRooms(keywords)
                return false
            }

        })
    }

    inner class SearchRoomAdapter : RecyclerView.Adapter<SearchViewHolder>() {
        val searchRooms = mutableListOf<Lightyear>()
        override fun getItemCount(): Int {
            return searchRooms.size
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
            val binding = RowSearchroomBinding.inflate(layoutInflater, parent, false)
            return SearchViewHolder(binding)
        }

        override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
            val lightYearSearch = searchRooms[position]
            holder.streaName.setText(lightYearSearch.nickname)
            holder.title.setText(lightYearSearch.stream_title)
            holder.tags.setText(lightYearSearch.tags)
            Glide.with(this@SearchFragment).load(lightYearSearch.head_photo)
                .into(holder.headPhoto)
            holder.itemView.setOnClickListener {
                searchRoomClicked(lightYearSearch)
            }
        }
        fun submitRooms(rooms: List<Lightyear>) {
            searchRooms.clear()
            Log.d(TAG, "count of rooms = ${searchRooms.size}")
            searchRooms.addAll(rooms)
            Log.d(TAG, "count of rooms = ${searchRooms.size}")
            if (rooms.size == 0) {
                binding.tvSearchResult.visibility = View.GONE
            } else {
                binding.tvSearchResult.visibility = View.VISIBLE
            }
            notifyDataSetChanged()
        }

    }

    inner class SearchViewHolder(val binding: RowSearchroomBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val streaName = binding.tvSearchStreamName
        val title = binding.tvSearchTitle
        val headPhoto = binding.imSearchHead
        val tags = binding.tvSearchTags
    }

    fun searchRoomClicked(lightyear: Lightyear) {
        val parentActivity =  requireActivity() as MainActivity
        parentActivity.supportFragmentManager.beginTransaction().run {
            replace(R.id.main_container, parentActivity.mainFragments[0])
            replace(R.id.chat_container, parentActivity.chatFragments[1])
            commit()
        }
        parentActivity.binding.searchContainer.visibility = View.GONE
        parentActivity.binding.bottonNavBar.visibility = View.GONE
    }

}