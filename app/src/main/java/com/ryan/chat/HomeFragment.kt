package com.ryan.chat

import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
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
    val roomViewModel by viewModels<RoomViewModel>()
    private val headViewModel by viewModels<HeadViewModel>()
    var adapter = ChatRoomAdapter()
    lateinit var auth : FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater)
//        return super.onCreateView(inflater, container, savedInstanceState)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 由此處開始寫 code
        val parentActivity = requireActivity() as MainActivity
        val prefLogin = requireContext().getSharedPreferences("login", AppCompatActivity.MODE_PRIVATE)
        val login = prefLogin.getBoolean("login_state", false)
        val username = prefLogin.getString("login_userid", "")
        val resolver = requireContext().contentResolver

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        headViewModel.getHeadImageByGlide()
        headViewModel.head.observe(viewLifecycleOwner) { uri ->
            parentActivity.displaySmallHeadImage(uri)
        }
        if (user != null) {
            Log.d(TAG, "onViewCreated: dispalyname = ${user.displayName}")
            Log.d(TAG, "onViewCreated: head = ${user.photoUrl}")
            parentActivity.binding.tvHomeLoginUserid.text = user.displayName
//            val bitMap = MediaStore.Images.Media.getBitmap(resolver, user.photoUrl)
            parentActivity.binding.imHead.visibility = View.VISIBLE
            Glide.with(parentActivity).load(headViewModel.head.value)
                .into(parentActivity.binding.imHead)
        } else {
            val defaultImagePath = "android.resource://com.ryan.chat/drawable/picpersonal"
            val defaultImageUri = Uri.parse(defaultImagePath).toString()
            parentActivity.binding.tvHomeLoginUserid.text = ""
            parentActivity.binding.imHead.visibility = View.GONE
//            parentActivity.displaySmallHeadImage(defaultImageUri)

        }


//        if (login) {
//            parentActivity.binding.tvHomeLoginUserid.setText(username)
////            parentActivity.binding.imHead.visibility = View.VISIBLE
//        }
//        else parentActivity.binding.tvHomeLoginUserid.setText(getString(R.string.guest))

        binding.recycler.setHasFixedSize(true)
        binding.recycler.layoutManager = GridLayoutManager(requireContext(),2)
        binding.recycler.adapter = adapter

        // viewModel
        // 觀察 RoomViewModel裡的 chatRooms(liveData)
        // 如果 chatRooms
        roomViewModel.chatRooms.observe(viewLifecycleOwner) { rooms ->
            adapter.submitRooms(rooms)
        }
        roomViewModel.getAllRooms()
        Log.d(TAG, "跑過 getAllRooms")
//        roomViewModel.getHitRooms()
//        Log.d(TAG, "跑過 getHitRooms")


    }
    inner class ChatRoomAdapter : RecyclerView.Adapter<BindingViewHolder>() {
        val chatRooms = mutableListOf<Lightyear>()
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder {
            val binding = RowChatroomBinding.inflate(layoutInflater, parent , false)
            return BindingViewHolder(binding)
        }

        override fun onBindViewHolder(holder: BindingViewHolder, position: Int) {
            val lightYear = chatRooms[position]
            holder.streamName.setText(lightYear.nickname)
            holder.title.setText(lightYear.stream_title)
            holder.tags.setText(lightYear.tags)
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
            Log.d(TAG, "rooms of num = ${rooms.size}")
            Log.d(TAG, "第一間房間是 = ${chatRooms[0].nickname}")
            notifyDataSetChanged()
            Log.d(TAG, "第一間房間是 = ${chatRooms[0].nickname}")
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
        parentActivity.binding.bottonNavBar.visibility = View.GONE
        parentActivity.binding.searchContainer.visibility = View.GONE
        parentActivity.binding.imHead.visibility = View.GONE
        parentActivity.binding.tvHomeLoginUserid.visibility = View.GONE

    }

}