package com.hastakala.shop

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.card.MaterialCardView
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class AuthActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    
    private var verificationId: String? = null
    private lateinit var phoneEditText: EditText
    private lateinit var otpEditText: EditText
    private lateinit var sendOtpButton: Button
    private lateinit var verifyOtpButton: Button
    private lateinit var loadingBar: ProgressBar

    // Layout containers
    private lateinit var authOptionsContainer: LinearLayout
    private lateinit var emailSection: LinearLayout
    private lateinit var phoneSection: LinearLayout
    private lateinit var registerSection: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        auth = FirebaseAuth.getInstance()

        // Initialize UI Elements
        loadingBar = findViewById(R.id.loadingBar)
        authOptionsContainer = findViewById(R.id.authOptionsContainer)
        emailSection = findViewById(R.id.emailSection)
        phoneSection = findViewById(R.id.phoneSection)
        registerSection = findViewById(R.id.registerSection)

        val emailOptionCard = findViewById<MaterialCardView>(R.id.emailOptionCard)
        val phoneOptionCard = findViewById<MaterialCardView>(R.id.phoneOptionCard)
        val googleOptionCard = findViewById<MaterialCardView>(R.id.googleOptionCard)

        val backToOptionsEmail = findViewById<Button>(R.id.backToOptionsEmail)
        val backToOptionsPhone = findViewById<Button>(R.id.backToOptionsPhone)
        val backToOptionsReg = findViewById<Button>(R.id.backToOptionsReg)
        val backToLoginFromReg = findViewById<Button>(R.id.backToLoginFromReg)

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data: Intent? = result.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                showLoading(false)
                Toast.makeText(this, getString(R.string.google_sign_in_failed, e.message), Toast.LENGTH_SHORT).show()
            }
        }

        // Section Toggles
        emailOptionCard.setOnClickListener {
            showSection(emailSection)
        }

        phoneOptionCard.setOnClickListener {
            showSection(phoneSection)
        }

        googleOptionCard.setOnClickListener {
            showLoading(true)
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }

        backToOptionsEmail.setOnClickListener { showSection(authOptionsContainer) }
        backToOptionsPhone.setOnClickListener { showSection(authOptionsContainer) }
        backToOptionsReg.setOnClickListener { showSection(authOptionsContainer) }
        backToLoginFromReg.setOnClickListener { showSection(emailSection) }

        // Email Section
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerButton = findViewById<Button>(R.id.registerButton)
        val forgotPasswordText = findViewById<View>(R.id.forgotPasswordText)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (validateInput(email, password)) {
                showLoading(true)
                val currentUser = auth.currentUser
                if (currentUser != null && currentUser.isAnonymous) {
                    val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, password)
                    currentUser.linkWithCredential(credential)
                        .addOnCompleteListener(this) { task ->
                            showLoading(false)
                            if (task.isSuccessful) {
                                handleLoginSuccess(false, email)
                            } else {
                                // If linking fails (e.g. account exists), try normal sign in
                                auth.signInWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(this) { loginTask ->
                                        if (loginTask.isSuccessful) {
                                            handleLoginSuccess(false, email)
                                        } else {
                                            Toast.makeText(this, getString(R.string.auth_failed, loginTask.exception?.message), Toast.LENGTH_LONG).show()
                                        }
                                    }
                            }
                        }
                } else {
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) { task ->
                            showLoading(false)
                            if (task.isSuccessful) {
                                val isNewUser = task.result?.additionalUserInfo?.isNewUser ?: false
                                handleLoginSuccess(isNewUser, email)
                            } else {
                                Toast.makeText(this, getString(R.string.auth_failed, task.exception?.message), Toast.LENGTH_LONG).show()
                            }
                        }
                }
            }
        }

        registerButton.setOnClickListener {
            showSection(registerSection)
        }

        // Register Section
        val regNameEditText = findViewById<EditText>(R.id.regNameEditText)
        val regContactEditText = findViewById<EditText>(R.id.regContactEditText)
        val regEmailEditText = findViewById<EditText>(R.id.regEmailEditText)
        val regPasswordEditText = findViewById<EditText>(R.id.regPasswordEditText)
        val btnFinalRegister = findViewById<Button>(R.id.btnFinalRegister)

        btnFinalRegister.setOnClickListener {
            val name = regNameEditText.text.toString().trim()
            val contact = regContactEditText.text.toString().trim()
            val email = regEmailEditText.text.toString().trim()
            val password = regPasswordEditText.text.toString().trim()

            if (name.isEmpty() || contact.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (validateInput(email, password)) {
                showLoading(true)
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        showLoading(false)
                        if (task.isSuccessful) {
                            // Optionally save name and contact to Firebase Profile or Database
                            handleLoginSuccess(true, email)
                        } else {
                            Toast.makeText(this, getString(R.string.registration_failed, task.exception?.message), Toast.LENGTH_LONG).show()
                        }
                    }
            }
        }

        forgotPasswordText.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            if (email.isNotEmpty()) {
                auth.sendPasswordResetEmail(email).addOnCompleteListener {
                    Toast.makeText(this, getString(R.string.reset_email_sent, email), Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, getString(R.string.enter_email_reset), Toast.LENGTH_SHORT).show()
            }
        }

        // Phone Section
        phoneEditText = findViewById(R.id.phoneEditText)
        otpEditText = findViewById(R.id.otpEditText)
        sendOtpButton = findViewById(R.id.sendOtpButton)
        verifyOtpButton = findViewById(R.id.verifyOtpButton)
        val otpLayout = findViewById<View>(R.id.otpLayout)

        sendOtpButton.setOnClickListener {
            val phoneNumber = phoneEditText.text.toString().trim()
            if (phoneNumber.isNotEmpty()) {
                showLoading(true)
                startPhoneNumberVerification("+" + phoneNumber)
            } else {
                Toast.makeText(this, getString(R.string.enter_phone), Toast.LENGTH_SHORT).show()
            }
        }

        verifyOtpButton.setOnClickListener {
            val code = otpEditText.text.toString().trim()
            if (code.isNotEmpty() && verificationId != null) {
                showLoading(true)
                val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
                signInWithPhoneAuthCredential(credential)
            } else {
                Toast.makeText(this, getString(R.string.enter_otp), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showSection(section: View) {
        authOptionsContainer.visibility = View.GONE
        emailSection.visibility = View.GONE
        phoneSection.visibility = View.GONE
        registerSection.visibility = View.GONE
        
        section.visibility = View.VISIBLE
    }

    private fun showLoading(isLoading: Boolean) {
        loadingBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        emailSection.isEnabled = !isLoading
        phoneSection.isEnabled = !isLoading
        registerSection.isEnabled = !isLoading
        authOptionsContainer.isEnabled = !isLoading
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    showLoading(false)
                    signInWithPhoneAuthCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    showLoading(false)
                    Toast.makeText(this@AuthActivity, getString(R.string.verification_failed, e.message), Toast.LENGTH_LONG).show()
                }

                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    showLoading(false)
                    this@AuthActivity.verificationId = verificationId
                    findViewById<View>(R.id.otpLayout).visibility = View.VISIBLE
                    verifyOtpButton.visibility = View.VISIBLE
                    sendOtpButton.visibility = View.GONE
                    Toast.makeText(this@AuthActivity, getString(R.string.otp_sent), Toast.LENGTH_SHORT).show()
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        val currentUser = auth.currentUser
        if (currentUser != null && currentUser.isAnonymous) {
            currentUser.linkWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    showLoading(false)
                    if (task.isSuccessful) {
                        handleLoginSuccess(false, auth.currentUser?.phoneNumber)
                    } else {
                        // Link failed, attempt standard sign in
                        auth.signInWithCredential(credential)
                            .addOnCompleteListener(this) { loginTask ->
                                if (loginTask.isSuccessful) {
                                    handleLoginSuccess(false, auth.currentUser?.phoneNumber)
                                } else {
                                    Toast.makeText(this, getString(R.string.phone_auth_failed, loginTask.exception?.message), Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                }
        } else {
            auth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    showLoading(false)
                    if (task.isSuccessful) {
                        val isNewUser = task.result?.additionalUserInfo?.isNewUser ?: false
                        handleLoginSuccess(isNewUser, auth.currentUser?.phoneNumber)
                    } else {
                        Toast.makeText(this, getString(R.string.phone_auth_failed, task.exception?.message), Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun handleLoginSuccess(isNewUser: Boolean, identifier: String?) {
        if (isNewUser) {
            val intent = Intent(this, ProfileSetupActivity::class.java)
            startActivity(intent)
        } else {
            // Check if email verified if using email/pass
            val user = auth.currentUser
            if (user != null && user.providerData.any { it.providerId == "password" } && !user.isEmailVerified) {
                Toast.makeText(this, getString(R.string.verify_email_first), Toast.LENGTH_LONG).show()
                // Optionally resend verification
                // user.sendEmailVerification()
            }

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        finish()
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val currentUser = auth.currentUser
        
        if (currentUser != null && currentUser.isAnonymous) {
            currentUser.linkWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    showLoading(false)
                    if (task.isSuccessful) {
                        handleLoginSuccess(false, auth.currentUser?.email)
                    } else {
                        // Linking failed (account likely already exists), proceed with sign in
                        auth.signInWithCredential(credential)
                            .addOnCompleteListener(this) { loginTask ->
                                if (loginTask.isSuccessful) {
                                    handleLoginSuccess(false, auth.currentUser?.email)
                                } else {
                                    Toast.makeText(this, getString(R.string.google_auth_failed), Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                }
        } else {
            auth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    showLoading(false)
                    if (task.isSuccessful) {
                        val isNewUser = task.result?.additionalUserInfo?.isNewUser ?: false
                        handleLoginSuccess(isNewUser, auth.currentUser?.email)
                    } else {
                        Toast.makeText(this, getString(R.string.google_auth_failed), Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun validateInput(email: String, pass: String): Boolean {
        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show()
            return false
        }
        if (pass.length < 6) {
            Toast.makeText(this, getString(R.string.password_too_short), Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}