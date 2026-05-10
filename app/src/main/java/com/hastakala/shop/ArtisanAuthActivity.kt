package com.hastakala.shop

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.hastakala.shop.ui.ArtisanLoginScreen
import com.hastakala.shop.ui.ArtisanRegistrationScreen
import com.hastakala.shop.ui.theme.MyApplicationTheme
import java.util.concurrent.TimeUnit

class ArtisanAuthActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    
    private var isLoadingState = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        auth = FirebaseAuth.getInstance()

        setContent {
            MyApplicationTheme(darkTheme = false) {
                var showRegistration by remember { mutableStateOf(intent.getStringExtra("action") == "register") }
                var isOtpSent by remember { mutableStateOf(false) }
                val isLoading by isLoadingState

                if (showRegistration) {
                    ArtisanRegistrationScreen(
                        isLoading = isLoading,
                        onRegisterClick = { name, email, phone, password ->
                            isLoadingState.value = true
                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        // Send verification email
                                        auth.currentUser?.sendEmailVerification()
                                        
                                        // In a real app, you might want to save the name and phone to Firestore here
                                        Toast.makeText(this@ArtisanAuthActivity, "Account created! Please verify your email.", Toast.LENGTH_LONG).show()
                                        isLoadingState.value = false
                                        showRegistration = false // Back to login
                                    } else {
                                        isLoadingState.value = false
                                        Toast.makeText(this@ArtisanAuthActivity, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        },
                        onBackToLogin = { showRegistration = false }
                    )
                } else {
                    ArtisanLoginScreen(
                        isOtpSent = isOtpSent,
                        isLoading = isLoading,
                        onOtpSentChange = { isOtpSent = it },
                        onLoginClick = { identifier, password ->
                            if (!identifier.contains("@")) {
                                isLoadingState.value = true
                                startPhoneNumberVerification("+91$identifier") {
                                    isLoadingState.value = false
                                    isOtpSent = true
                                }
                            } else {
                                if (password == null) {
                                    // This should not be reachable now with inline password but added for safety
                                } else {
                                    isLoadingState.value = true
                                    auth.signInWithEmailAndPassword(identifier, password)
                                        .addOnCompleteListener { task ->
                                            isLoadingState.value = false
                                            if (task.isSuccessful) {
                                                navigateToMain()
                                            } else {
                                                Toast.makeText(this@ArtisanAuthActivity, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                }
                            }
                        },
                        onVerifyOtp = { _, otp ->
                            if (verificationId != null) {
                                isLoadingState.value = true
                                val credential = PhoneAuthProvider.getCredential(verificationId!!, otp)
                                signInWithPhoneAuthCredential(credential) {
                                    isLoadingState.value = false
                                }
                            } else {
                                Toast.makeText(this, "Session expired. Please resend OTP.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onForgotPassword = { email ->
                            if (email.isNotBlank()) {
                                auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(this, "Password reset email sent to $email", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                Toast.makeText(this, "Please enter your email first", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onGoogleLogin = {
                            triggerGoogleSignIn()
                        },
                        onCreateAccount = {
                            showRegistration = true
                        },
                        onGuestLogin = {
                            auth.signInAnonymously().addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    navigateToMain()
                                } else {
                                    Toast.makeText(this@ArtisanAuthActivity, "Guest access failed.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                }
            }
        }
        setupGoogleSignIn()
    }

    private lateinit var googleSignInClient: com.google.android.gms.auth.api.signin.GoogleSignInClient
    private lateinit var googleSignInLauncher: androidx.activity.result.ActivityResultLauncher<Intent>

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        googleSignInLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()) { result ->
            val data: Intent? = result.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: com.google.android.gms.common.api.ApiException) {
                Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun triggerGoogleSignIn() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    navigateToMain()
                } else {
                    Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun startPhoneNumberVerification(phoneNumber: String, onCodeSent: () -> Unit) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithPhoneAuthCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    isLoadingState.value = false
                    Toast.makeText(this@ArtisanAuthActivity, "Verification failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }

                override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                    verificationId = id
                    resendToken = token
                    onCodeSent()
                    Toast.makeText(this@ArtisanAuthActivity, "OTP Sent Successfully", Toast.LENGTH_SHORT).show()
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential, onComplete: (() -> Unit)? = null) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                onComplete?.invoke()
                if (task.isSuccessful) {
                    navigateToMain()
                } else {
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
