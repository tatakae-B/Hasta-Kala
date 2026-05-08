package com.hastakala.shop

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

import com.hastakala.shop.data.ShopRepository
import com.hastakala.shop.data.UserProfile
import com.google.firebase.Timestamp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ProfileSetupActivity : AppCompatActivity() {

    @Inject
    lateinit var repository: ShopRepository

    private lateinit var auth: FirebaseAuth
    private lateinit var profileImageView: ImageView
    private var selectedImageUri: Uri? = null

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            profileImageView.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_setup)

        auth = FirebaseAuth.getInstance()

        profileImageView = findViewById(R.id.profileImageView)
        val selectPhotoButton = findViewById<Button>(R.id.selectPhotoButton)
        val nameEditText = findViewById<EditText>(R.id.nameEditText)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val saveProfileButton = findViewById<Button>(R.id.saveProfileButton)

        // Pre-fill email if available (e.g., from Google Sign-in)
        auth.currentUser?.email?.let {
            emailEditText.setText(it)
        }

        selectPhotoButton.setOnClickListener {
            getContent.launch("image/*")
        }

        saveProfileButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(this, getString(R.string.enter_name), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveProfile(name, email)
        }
    }

    private fun saveProfile(name: String, email: String) {
        val user = auth.currentUser ?: return
        
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .setPhotoUri(selectedImageUri)
            .build()

        user.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Create UserProfile for Firestore
                    val profile = UserProfile(
                        uid = user.uid,
                        fullName = name,
                        email = email,
                        loginMethod = user.providerData.lastOrNull()?.providerId ?: "email",
                        createdAt = Timestamp.now()
                    )

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            repository.saveUserProfile(profile)
                            
                            // Send Welcome Email (Simplified: send verification email)
                            user.sendEmailVerification()

                            // Update email if it was changed
                            if (email.isNotEmpty() && email != user.email) {
                                user.updateEmail(email)
                            }

                            runOnUiThread {
                                Toast.makeText(this@ProfileSetupActivity, getString(R.string.profile_welcome_sent), Toast.LENGTH_LONG).show()
                                proceedToMain()
                            }
                        } catch (e: Exception) {
                            runOnUiThread {
                                Toast.makeText(this@ProfileSetupActivity, getString(R.string.error_saving_profile, e.message), Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(this, getString(R.string.failed_update_profile), Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun proceedToMain() {
        Toast.makeText(this, getString(R.string.profile_setup_complete), Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }
}
