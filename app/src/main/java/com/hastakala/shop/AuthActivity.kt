package com.hastakala.shop

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.card.MaterialCardView
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
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
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private var countDownTimer: CountDownTimer? = null

    private lateinit var phoneEditText: EditText
    private lateinit var otpEditText: EditText
    private lateinit var sendOtpButton: Button
    private lateinit var verifyOtpButton: Button
    private lateinit var resendOtpButton: Button
    private lateinit var resendTimerText: TextView
    private lateinit var resendLayout: LinearLayout
    private lateinit var editPhoneBtn: Button
    private lateinit var loadingBar: ProgressBar

    // Layout containers
    private lateinit var authOptionsContainer: LinearLayout
    private lateinit var emailSection: LinearLayout
    private lateinit var phoneSection: LinearLayout
    private lateinit var registerSection: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        // Restore state if available
        if (savedInstanceState != null) {
            verificationId = savedInstanceState.getString("verificationId")
            // Note: resendToken cannot be easily serialized, but verificationId helps
        }

        auth = FirebaseAuth.getInstance()
        auth.useAppLanguage() 

        val prefill = intent.getStringExtra("prefill")
        val action = intent.getStringExtra("action")
        val regEmail = intent.getStringExtra("email")
        val regPassword = intent.getStringExtra("password")
        val regName = intent.getStringExtra("name")
        val regPhone = intent.getStringExtra("phone")

        Log.d("AuthActivity", "Firebase Auth initialized")
        
        // Initialize UI Elements
        loadingBar = findViewById(R.id.loadingBar)
        authOptionsContainer = findViewById(R.id.authOptionsContainer)
        emailSection = findViewById(R.id.emailSection)
        phoneSection = findViewById(R.id.phoneSection)
        registerSection = findViewById(R.id.registerSection)

        phoneEditText = findViewById(R.id.phoneEditText)
        otpEditText = findViewById(R.id.otpEditText)
        
        if (prefill != null) {
            if (prefill.contains("@")) {
                findViewById<EditText>(R.id.emailEditText).setText(prefill)
                showSection(emailSection)
            } else {
                phoneEditText.setText(prefill)
                showSection(phoneSection)
            }
        }

        if (action == "register") {
            showSection(registerSection)
            findViewById<EditText>(R.id.regNameEditText).setText(regName)
            findViewById<EditText>(R.id.regEmailEditText).setText(regEmail)
            findViewById<EditText>(R.id.regPasswordEditText).setText(regPassword)
            findViewById<EditText>(R.id.regContactEditText).setText(regPhone)
        } else if (action == "google") {
            showLoading(true)
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }
        sendOtpButton = findViewById(R.id.sendOtpButton)
        verifyOtpButton = findViewById(R.id.verifyOtpButton)
        resendOtpButton = findViewById(R.id.resendOtpButton)
        resendTimerText = findViewById(R.id.resendTimerText)
        resendLayout = findViewById(R.id.resendLayout)
        editPhoneBtn = findViewById(R.id.editPhoneBtn)

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
        backToOptionsPhone.setOnClickListener { 
            resetPhoneSection()
            showSection(authOptionsContainer) 
        }

        val editPhoneBtn = findViewById<Button>(R.id.editPhoneBtn)
        editPhoneBtn.setOnClickListener {
            resetPhoneSection()
        }
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
                            if (task.isSuccessful) {
                                showLoading(false)
                                handleLoginSuccess(false, email)
                            } else {
                                // If linking fails (e.g. account exists), try normal sign in
                                auth.signInWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(this) { loginTask ->
                                        showLoading(false)
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
                            auth.currentUser?.sendEmailVerification()
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
        sendOtpButton.setOnClickListener {
            val phoneNumber = phoneEditText.text.toString().trim()
            if (phoneNumber.length >= 10) {
                showLoading(true)
                startPhoneNumberVerification("+91" + phoneNumber) // Assuming default region is India, fix later with picker
            } else {
                Toast.makeText(this, getString(R.string.invalid_phone), Toast.LENGTH_SHORT).show()
            }
        }

        verifyOtpButton.setOnClickListener {
            val code = otpEditText.text.toString().trim()
            if (code.length == 6 && verificationId != null) {
                showLoading(true)
                val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
                signInWithPhoneAuthCredential(credential)
            } else {
                Toast.makeText(this, getString(R.string.enter_otp), Toast.LENGTH_SHORT).show()
            }
        }

        resendOtpButton.setOnClickListener {
            val phoneNumber = phoneEditText.text.toString().trim()
            if (phoneNumber.isNotEmpty() && resendToken != null) {
                showLoading(true)
                resendVerificationCode("+91$phoneNumber", resendToken)
            }
        }
    }

    private fun showTooManyRequestsDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.too_many_requests_title))
            .setMessage(getString(R.string.too_many_requests_message))
            .setPositiveButton(getString(R.string.btn_use_google)) { _, _ ->
                showSection(authOptionsContainer)
                showLoading(true)
                val signInIntent = googleSignInClient.signInIntent
                googleSignInLauncher.launch(signInIntent)
            }
            .setNegativeButton(getString(R.string.btn_cancel), null)
            .show()
    }

    private fun showProviderDisabledDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.config_issue_title))
            .setMessage(getString(R.string.config_issue_phone_disabled))
            .setPositiveButton(getString(R.string.btn_use_google)) { _, _ ->
                showSection(authOptionsContainer)
                showLoading(true)
                val signInIntent = googleSignInClient.signInIntent
                googleSignInLauncher.launch(signInIntent)
            }
            .setNegativeButton(getString(R.string.btn_cancel), null)
            .show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("verificationId", verificationId)
    }

    private fun showSection(section: View) {
        authOptionsContainer.visibility = View.GONE
        emailSection.visibility = View.GONE
        phoneSection.visibility = View.GONE
        registerSection.visibility = View.GONE
        
        section.visibility = View.VISIBLE
    }

    private fun resetPhoneSection() {
        countDownTimer?.cancel()
        resendLayout.visibility = View.GONE
        editPhoneBtn.visibility = View.GONE
        findViewById<View>(R.id.otpLayout).visibility = View.GONE
        verifyOtpButton.visibility = View.GONE
        sendOtpButton.visibility = View.VISIBLE
        phoneEditText.isEnabled = true
        otpEditText.setText("")
    }

    private fun showLoading(isLoading: Boolean) {
        loadingBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        findViewById<View>(R.id.rootLayout).alpha = if (isLoading) 0.5f else 1.0f
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
                    Log.d("AuthActivity", "onVerificationCompleted: $credential")
                    showLoading(false)
                    // Auto-retrieval or instant verification
                    otpEditText.setText(credential.smsCode)
                    signInWithPhoneAuthCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Log.e("AuthActivity", "Full Error: ${e.javaClass.simpleName} - ${e.message}")
                    showLoading(false)
                    
                    // Detailed Logcat output for developer
                    e.printStackTrace()

                    when {
                        e is FirebaseAuthInvalidCredentialsException -> {
                            Toast.makeText(this@AuthActivity, "Invalid Request: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                        e is FirebaseTooManyRequestsException -> {
                            showTooManyRequestsDialog()
                        }
                        e.message?.contains("This operation is not allowed", ignoreCase = true) == true -> {
                            showProviderDisabledDialog()
                        }
                        else -> {
                            Toast.makeText(this@AuthActivity, getString(R.string.verification_failed, e.localizedMessage), Toast.LENGTH_LONG).show()
                        }
                    }
                }

                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    Log.d("AuthActivity", "onCodeSent: $verificationId")
                    showLoading(false)
                    this@AuthActivity.verificationId = verificationId
                    this@AuthActivity.resendToken = token
                    
                    findViewById<View>(R.id.otpLayout).visibility = View.VISIBLE
                    verifyOtpButton.visibility = View.VISIBLE
                    editPhoneBtn.visibility = View.VISIBLE
                    sendOtpButton.visibility = View.GONE
                    phoneEditText.isEnabled = false // Lock phone number during verification
                    
                    startResendTimer()
                    Toast.makeText(this@AuthActivity, getString(R.string.otp_sent), Toast.LENGTH_SHORT).show()
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun resendVerificationCode(phoneNumber: String, token: PhoneAuthProvider.ForceResendingToken?) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setForceResendingToken(token!!)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    showLoading(false)
                    signInWithPhoneAuthCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Log.e("AuthActivity", "resend onVerificationFailed: ${e.message}", e)
                    showLoading(false)
                    if (e.message?.contains("This operation is not allowed", ignoreCase = true) == true) {
                        showProviderDisabledDialog()
                    } else {
                        Toast.makeText(this@AuthActivity, getString(R.string.verification_failed, e.localizedMessage), Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    showLoading(false)
                    this@AuthActivity.verificationId = verificationId
                    startResendTimer()
                    Toast.makeText(this@AuthActivity, "OTP Resent", Toast.LENGTH_SHORT).show()
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun startResendTimer() {
        resendLayout.visibility = View.VISIBLE
        resendOtpButton.visibility = View.GONE
        resendTimerText.visibility = View.VISIBLE
        
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                resendTimerText.text = getString(R.string.resend_in, millisUntilFinished / 1000)
            }

            override fun onFinish() {
                resendTimerText.visibility = View.GONE
                resendOtpButton.visibility = View.VISIBLE
            }
        }.start()
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
            intent.putExtra("name", findViewById<EditText>(R.id.regNameEditText).text.toString())
            intent.putExtra("phone", findViewById<EditText>(R.id.regContactEditText).text.toString())
            intent.putExtra("email", findViewById<EditText>(R.id.regEmailEditText).text.toString())
            startActivity(intent)
        } else {
            val user = auth.currentUser
            if (user != null && user.providerData.any { it.providerId == "password" } && !user.isEmailVerified) {
                Toast.makeText(this, getString(R.string.verify_email_first), Toast.LENGTH_LONG).show()
                user.sendEmailVerification()
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

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}