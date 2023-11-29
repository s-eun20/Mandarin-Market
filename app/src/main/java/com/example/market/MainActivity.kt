package com.example.market

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.market.databinding.ActivityMainBinding

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Handler(Looper.getMainLooper()).postDelayed({
            // 3초 뒤 ListFragment
            replaceWithListFragment()
        }, DURATION)

        //
        this.onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun replaceWithListFragment() {
        val homeFragment = HomeFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, homeFragment)
            .commit()

        binding.fragmentContainer.visibility = View.VISIBLE
    }

    companion object {
        private const val DURATION: Long = 1000
    }

    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (supportFragmentManager.backStackEntryCount > 0) {

                supportFragmentManager.popBackStack()
            } else {

                isEnabled = false
                onBackPressed()
            }
        }
    }
    fun replaceWithMainActivity2() {
        val mainActivity2Intent = Intent(this, BottomActivity::class.java)
        startActivity(mainActivity2Intent)
        finish()
    }
}
