package com.ryan.chat

import android.os.Bundle
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
import com.ryan.chat.databinding.FragmentHitBinding
import com.ryan.chat.databinding.RowHitroomBinding

class HitFragment : Fragment() {
    companion object {
        val TAG = HitFragment::class.java.simpleName
        val instance : HitFragment by lazy {
            HitFragment()
        }
    }
    lateinit var binding: FragmentHitBinding
    val roomViewModel by viewModels<RoomViewModel>()
    var adapter = HitRoomAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHitBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 由此處開始寫 code
        val parentActivity = requireActivity() as MainActivity
        val prefLogin = requireContext().getSharedPreferences("login", AppCompatActivity.MODE_PRIVATE)
        var login = prefLogin.getBoolean("login_state", false)
        var username = prefLogin.getString("login_userid", "")

        if (login) {
            parentActivity.binding.tvHomeLoginUserid.setText(username)
            parentActivity.binding.imHead.visibility = View.VISIBLE
            parentActivity.binding.tvHomeLoginUserid.setText(username)
        }
        else parentActivity.binding.tvHomeLoginUserid.setText("")

        binding.recyclerHit.setHasFixedSize(true)
        binding.recyclerHit.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerHit.adapter = adapter

        roomViewModel.chatRooms.observe(viewLifecycleOwner) { rooms ->
            adapter.submitHitRooms(rooms)
        }
        roomViewModel.getHitRooms()
    }

    inner class HitRoomAdapter : RecyclerView.Adapter<HitRoomAdapter.HitViewHolder>() {
        val hitRooms = mutableListOf<Lightyear>()
        override fun getItemCount(): Int {
            return hitRooms.size
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HitViewHolder {
            val binding = RowHitroomBinding.inflate(layoutInflater, parent, false)
            return HitViewHolder(binding)
        }

        override fun onBindViewHolder(holder: HitViewHolder, position: Int) {
            val lightYear = hitRooms[position]
            holder.streaName.setText(lightYear.nickname)
            holder.title.setText(lightYear.stream_title)
            holder.tags.setText(lightYear.tags)
            Glide.with(this@HitFragment).load(lightYear.head_photo)
                .into(holder.headPhoto)
            holder.itemView.setOnClickListener {
                hitRoomClicked(lightYear)
            }
        }

        fun submitHitRooms(rooms: List<Lightyear>) {
            hitRooms.clear()
            hitRooms.addAll(rooms)
            Log.d(TAG, "rooms of num = ${rooms.size}")
            Log.d(TAG, "第一間房間是 = ${hitRooms[0].nickname}")
            notifyDataSetChanged()
            Log.d(TAG, "第一間房間是 = ${hitRooms[0].nickname}")

        }

        inner class HitViewHolder(val binding: RowHitroomBinding) :
            RecyclerView.ViewHolder(binding.root) {
            val streaName = binding.tvHitStreamName
            val title = binding.tvHitTitle
            val headPhoto = binding.imHitHead
            val tags = binding.tvHitTags
        }

        fun hitRoomClicked(lightyear: Lightyear) {
            val parentActivity = requireActivity() as MainActivity
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
}