package com.example.btl_todoapp

import androidx.fragment.app.Fragment
object FragmentFactory {
    fun createFragment(itemId : Int) : Fragment {
        return when (itemId){
            R.id.home -> home()
            R.id.settings -> settings()
            else -> throw IllegalArgumentException("Unknown FragmentId")
        }
    }
}
