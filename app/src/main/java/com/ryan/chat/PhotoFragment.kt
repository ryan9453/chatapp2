package com.ryan.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ryan.chat.databinding.FragmentPhotoBinding

class PhotoFragment : Fragment () {
    companion object {
        val TAG = PhotoFragment::class.java.simpleName
        val instance : PhotoFragment by lazy {
            PhotoFragment()
        }
    }
    lateinit var binding: FragmentPhotoBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPhotoBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
}