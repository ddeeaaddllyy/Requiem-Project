package com.application.requiemproject.ui

import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.application.requiemproject.R
import com.application.requiemproject.ui.home.HomeFragment
import com.application.requiemproject.ui.profile.ProfileFragment
import com.application.requiemproject.ui.search.SearchFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val searchIntent = Intent(this, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                putExtra("open_fragment", "search")
        }

        val profileIntent = Intent(this, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                putExtra("open_fragment", "profile")
        }

        val shortcutProfile = ShortcutInfo.Builder(this, "profile_shortcut")
            .setIcon(Icon.createWithResource(this, R.drawable.ic_home_24))
            .setLongLabel("Open Profile")
            .setShortLabel("Profile")
            .setIntent(profileIntent)
            .build()

        val shortcutSettings = ShortcutInfo.Builder(this, "search_shortcut")
            .setIcon(Icon.createWithResource(this, R.drawable.ic_home_24))
            .setLongLabel("Open Search")
            .setShortLabel("Search")
            .setIntent(searchIntent)
            .build()

        lifecycleScope.launch(Dispatchers.IO) {
            val shortcutManager = getSystemService(ShortcutManager::class.java)
            shortcutManager.dynamicShortcuts = listOf(
                shortcutProfile,
                shortcutSettings
            )
        }

        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)

        openFragment("home")

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    openFragment("home")
                    true
                }
                R.id.navigation_search -> {
                    openFragment("search")
                    true
                }
                R.id.navigation_profile -> {
                    openFragment("profile")
                    true
                }
                else -> false
            }
        }

        val shortcutFragment = intent.getStringExtra("open_fragment")

        when (shortcutFragment) {
            "profile" -> openFragment("profile")
            "search" -> openFragment("search")
            else -> openFragment("home")
        }
    }

    private fun openFragment(tag: String) {
        val fragment = supportFragmentManager.findFragmentByTag(tag)
            ?: when (tag) {
                "home" -> HomeFragment()
                "search" -> SearchFragment()
                "profile" -> ProfileFragment()
                else -> HomeFragment()
            }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment, tag)
            .commit()
    }
}