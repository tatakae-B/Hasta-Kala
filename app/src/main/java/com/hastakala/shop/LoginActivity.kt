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

        setContentView(R.layout.activity_login)

        val startLoginButton = findViewById<Button>(R.id.startLoginButton)
        val startGuestButton = findViewById<Button>(R.id.startGuestButton)

        // 1st button: Navigate to Login/Register options (AuthActivity)
        startLoginButton.setOnClickListener {
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
        }

        // 2nd button: Continue anonymously
        startGuestButton.setOnClickListener {
            auth.signInAnonymously()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        // If sign in fails, display a message to the user.
                        android.widget.Toast.makeText(baseContext, "Authentication failed.",
                            android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}
