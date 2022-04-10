package com.ryan.chat

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.ryan.chat.databinding.FragmentHomeBinding
import com.ryan.chat.databinding.RowChatroomBinding

class HomeFragment : Fragment() {
    companion object {
        val TAG = HomeFragment::class.java.simpleName
        val instance : HomeFragment by lazy {
            HomeFragment()
        }
    }
    lateinit var binding: FragmentHomeBinding
    private val roomViewModel by viewModels<RoomViewModel>()
    private val userViewModel by activityViewModels<UserViewModel>()
    var adapter = ChatRoomAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // 設置聊天室清單
        binding.recycler.setHasFixedSize(true)
        binding.recycler.layoutManager = GridLayoutManager(requireContext(),2)
        binding.recycler.adapter = adapter

        // viewModel
        // 觀察 RoomViewModel裡的 chatRooms(liveData)
        // 如果 chatRooms
        roomViewModel.chatRooms.observe(viewLifecycleOwner) { rooms ->
            adapter.submitRooms(rooms)
        }
        // 取得全部房間
        roomViewModel.getAllRooms()

    }

    inner class ChatRoomAdapter : RecyclerView.Adapter<BindingViewHolder>() {
        val chatRooms = mutableListOf<Lightyear>()
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder {
            val binding = RowChatroomBinding.inflate(layoutInflater, parent , false)
            return BindingViewHolder(binding)
        }

        override fun onBindViewHolder(holder: BindingViewHolder, position: Int) {
            val lightYear = chatRooms[position]
            holder.streamName.text = lightYear.nickname
            holder.title.text = lightYear.stream_title
            holder.tags.text = lightYear.tags
            Glide.with(this@HomeFragment).load(lightYear.head_photo)
                .into(holder.headPhoto)
            holder.itemView.setOnClickListener {
                chatRoomClicked(lightYear)
            }
        }

        override fun getItemCount(): Int {
            return chatRooms.size
        }
        fun submitRooms(rooms: List<Lightyear>) {
            chatRooms.clear()
            chatRooms.addAll(rooms)
            notifyDataSetChanged()
        }

    }
    inner class BindingViewHolder(val binding: RowChatroomBinding):
        RecyclerView.ViewHolder(binding.root) {
        val streamName = binding.tvStreamName
        val title = binding.tvTitle
        val headPhoto = binding.imHeadPhoto
        val tags = binding.tvTags
    }

    fun chatRoomClicked(lightyear: Lightyear) {
        val parentActivity =  requireActivity() as MainActivity
        parentActivity.supportFragmentManager.beginTransaction().run {
            replace(R.id.main_container, parentActivity.mainFragments[0])
            replace(R.id.chat_container, parentActivity.chatFragments[1])
            commit()
        }
        parentActivity.binding.bottomNavBar.visibility = View.GONE
        parentActivity.binding.searchContainer.visibility = View.GONE
        parentActivity.binding.imHead.visibility = View.GONE
        parentActivity.binding.tvHomeLoginNickname.visibility = View.GONE

    }

}