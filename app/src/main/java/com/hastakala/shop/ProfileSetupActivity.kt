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
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.hastakala.shop.data.ShopRepository
import com.hastakala.shop.data.UserProfile
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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
        val shopNameEditText = findViewById<EditText>(R.id.shopNameEditText)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val contactEditText = findViewById<EditText>(R.id.contactEditText)
        val locationEditText = findViewById<EditText>(R.id.locationEditText)
        val saveProfileButton = findViewById<Button>(R.id.saveProfileButton)

        // Pre-fill fields if available from Intent or Auth
        val intentName = intent.getStringExtra("name")
        val intentPhone = intent.getStringExtra("phone")
        val intentEmail = intent.getStringExtra("email")

        if (!intentName.isNullOrEmpty()) nameEditText.setText(intentName)
        if (!intentPhone.isNullOrEmpty()) contactEditText.setText(intentPhone)
        
        val emailToSet = intentEmail ?: auth.currentUser?.email
        if (!emailToSet.isNullOrEmpty()) {
            emailEditText.setText(emailToSet)
        }

        selectPhotoButton.setOnClickListener {
            getContent.launch("image/*")
        }

        saveProfileButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val shopName = shopNameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val contact = contactEditText.text.toString().trim()
            val location = locationEditText.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(this, getString(R.string.enter_name), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveProfile(name, shopName, email, contact, location)
        }
    }

    private fun saveProfile(name: String, shopName: String, email: String, contact: String, location: String) {
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
                        shopName = shopName,
                        email = email,
                        contact = contact,
                        location = location,
                        profileImageUrl = selectedImageUri?.toString() ?: "",
                        loginMethod = user.providerData.lastOrNull()?.providerId ?: "email",
                        createdAt = Timestamp.now()
                    )

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            repository.saveUserProfile(profile)
                            
                            // Set Firebase Auth language to match app language for the email
                            val prefs = getSharedPreferences("settings", android.content.Context.MODE_PRIVATE)
                            val currentLang = prefs.getString("language", "English") ?: "English"
                            
                            // Map user-friendly language name to ISO code if necessary
                            val langCode = when(currentLang) {
                                "Hindi" -> "hi"
                                "Malayalam" -> "ml"
                                "Kannada" -> "kn"
                                "Tamil" -> "ta"
                                "Telugu" -> "te"
                                else -> "en"
                            }
                            auth.setLanguageCode(langCode)

                            // 1. Update email if it was changed (do this before sending verification)
                            if (email.isNotEmpty() && email != user.email) {
                                try {
                                    user.updateEmail(email).await()
                                    android.util.Log.d("ProfileSetup", "Email updated to $email")
                                } catch (e: Exception) {
                                    android.util.Log.e("ProfileSetup", "Failed to update email: ${e.message}")
                                }
                            }

                            // 2. Send Verification Email and await result
                            try {
                                user.sendEmailVerification().await()
                                android.util.Log.d("ProfileSetup", "Verification email sent to ${user.email}")
                            } catch (e: Exception) {
                                android.util.Log.e("ProfileSetup", "Failed to send verification email: ${e.message}")
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
