package com.ryan.chat

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.ryan.chat.databinding.FragmentSearchBinding
import com.ryan.chat.databinding.RowSearchroomBinding

class SearchFragment : Fragment() {
    companion object {
        val TAG = SearchFragment::class.java.simpleName
        val instance : SearchFragment by lazy {
            SearchFragment()
        }
    }
    lateinit var auth : FirebaseAuth
    lateinit var binding: FragmentSearchBinding
    lateinit var adapter : SearchRoomAdapter
    val roomViewModel by viewModels<RoomViewModel>()
    private val userViewModel by activityViewModels<UserViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 設置搜尋結果聊天室的清單元件
        binding.searchRecycler.setHasFixedSize(true)
        binding.searchRecycler.layoutManager = GridLayoutManager(requireContext(), 2)
        adapter = SearchRoomAdapter()
        binding.searchRecycler.adapter = adapter

        roomViewModel.searchRooms.observe(viewLifecycleOwner) { rooms ->
            adapter.submitRooms(rooms)
        }

        // SearchView 的傾聽器
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
        private val searchRooms = mutableListOf<Lightyear>()
        override fun getItemCount(): Int {
            return searchRooms.size
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
            val binding = RowSearchroomBinding.inflate(layoutInflater, parent, false)
            return SearchViewHolder(binding)
        }

        override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
            val lightYearSearch = searchRooms[position]
            holder.streamName.text = lightYearSearch.nickname
            holder.title.text = lightYearSearch.stream_title
            holder.tags.text = lightYearSearch.tags
            Glide.with(this@SearchFragment).load(lightYearSearch.head_photo)
                .into(holder.headPhoto)
            holder.itemView.setOnClickListener {
                searchRoomClicked(lightYearSearch)
            }
        }

        @SuppressLint("NotifyDataSetChanged")
        fun submitRooms(rooms: List<Lightyear>) {
            searchRooms.clear()
            searchRooms.addAll(rooms)
            if (rooms.isEmpty()) {
                binding.tvSearchResult.visibility = View.GONE
            } else {
                binding.tvSearchResult.visibility = View.VISIBLE
            }
            notifyDataSetChanged()
        }

    }

    inner class SearchViewHolder(val binding: RowSearchroomBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val streamName = binding.tvSearchStreamName
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