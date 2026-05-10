package com.hastakala.shop

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        auth = FirebaseAuth.getInstance()

        // Session Handling: If already logged in, go straight to MainActivity
        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        // The old LoginActivity is no longer needed as a UI entry point.
        // If we reach here, we should redirect to the ArtisanAuthActivity via the Splash/Welcome flow,
        // but for safety/legacy entry points, we redirect immediately to ArtisanAuthActivity.
        val intent = Intent(this, ArtisanAuthActivity::class.java)
        startActivity(intent)
        finish()
    }
}
