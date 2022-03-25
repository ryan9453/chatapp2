package com.ryan.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ryan.chat.databinding.FragmentEmptyBinding

class EmptyFragment : Fragment() {
    companion object {
        val TAG = EmptyFragment::class.java.simpleName
        val instance : EmptyFragment by lazy {
            EmptyFragment()
        }
    }
    lateinit var binding: FragmentEmptyBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEmptyBinding.inflate(inflater)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

}
