package com.example.cgprototype

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

//        val auth = FirebaseAuth.getInstance()
//
//        auth.addAuthStateListener {
//            val user = it.currentUser
//            if (user == null)
//                startActivity(Intent(this, LoginActivity::class.java))
//            else
//                startActivity(Intent(this, HomeActivity::class.java))
//        }



        startActivity(Intent(this, HomeActivity::class.java))

        finish()
    }
}
