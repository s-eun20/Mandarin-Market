package com.example.market

// MainActivity2.kt

import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class BottomActivity : AppCompatActivity() {

    private lateinit var fragmentContainer: FrameLayout
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bottom)

        fragmentContainer = findViewById(R.id.fragment_container2)
        bottomNavigationView = findViewById(R.id.bottomNaviView)

        // 기본 Fragment로 fragment_list.xml을 설정
        replaceFragment(ListFragment())

        // BottomNavigationView 아이템 선택에 따른 동작 설정
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    // '홈' 아이템 선택
                    replaceFragment(ListFragment())
                    true
                }
                R.id.chatList -> {
                    // '채팅' 아이템 선택
                    replaceFragment(BottomChatListFragment())
                    true
                }
                R.id.myPage -> {
                    // '감귤' 아이템 선택
                    replaceFragment(MyPageFragment())
                    true
                }
                else -> false
            }
        }
    }


    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.popBackStack()

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container2, fragment)
            .commit()
    }
}
