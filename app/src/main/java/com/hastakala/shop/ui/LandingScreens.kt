package com.hastakala.shop.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hastakala.shop.R
import com.hastakala.shop.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    val scale = remember { Animatable(0.8f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1200, easing = EaseOutExpo)
            )
        }
        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1000)
            )
        }
        delay(2500)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ArtisanCream),
        contentAlignment = Alignment.Center
    ) {
        // Subtle Background Glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(TerracottaLight.copy(alpha = 0.1f), Color.Transparent),
                    center = center,
                    radius = size.minDimension * 0.8f
                )
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value)
        ) {
            Surface(
                modifier = Modifier
                    .size(140.dp)
                    .shadow(24.dp, CircleShape, spotColor = TerracottaPrimary.copy(alpha = 0.3f)),
                shape = CircleShape,
                color = Color.White
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "HASTA KALA Logo",
                        modifier = Modifier.size(90.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            Text(
                text = "HASTA KALA",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 8.sp,
                    color = ArtisanWarmBlack
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = stringResource(R.string.credit_crafted_by).uppercase(),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp,
                    color = TerracottaPrimary.copy(alpha = 0.6f)
                )
            )
        }
    }
}

@Composable
fun WelcomeScreen(onGetStarted: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "welcome_infinite")

    // --- ANIMATION STATES ---
    val floatingAnim by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating_hero"
    )

    val backgroundPulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bg_pulse"
    )

    val rotationAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pattern_rotate"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ArtisanCream)
    ) {
        // --- LAYERED BACKGROUND ART ---
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Warm Glows
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(TerracottaLight.copy(alpha = 0.15f), Color.Transparent),
                    center = Offset(width * 0.8f, height * 0.2f),
                    radius = width * 1.2f
                )
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(GoldAccent.copy(alpha = 0.1f), Color.Transparent),
                    center = Offset(width * 0.2f, height * 0.8f),
                    radius = width
                )
            )
        }

        // 2. Artistic Patterns (Subtle overlay)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.03f)
                .graphicsLayer { rotationZ = rotationAnim }
        ) {
            Icon(
                imageVector = Icons.Default.FilterVintage,
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(600.dp),
                tint = TerracottaPrimary
            )
        }

        // 3. Faded Motifs
        Icon(
            imageVector = Icons.Default.Grain,
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-40).dp, y = 100.dp)
                .size(200.dp)
                .alpha(0.05f),
            tint = ArtisanWarmBlack
        )

        Icon(
            imageVector = Icons.Default.AllInclusive,
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 60.dp, y = (-150).dp)
                .size(250.dp)
                .alpha(0.04f),
            tint = TerracottaPrimary
        )

        // --- MAIN CONTENT ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.15f))

            // --- HERO VISUAL & LOGO SECTION ---
            Box(
                modifier = Modifier
                    .offset(y = floatingAnim.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer ambient glow
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .blur(60.dp)
                        .background(TerracottaPrimary.copy(alpha = 0.12f * backgroundPulse), CircleShape)
                )

                // The "Pottery/Artisan" Geometric Background
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .drawBehind {
                            drawCircle(
                                color = TerracottaPrimary.copy(alpha = 0.05f),
                                style = Stroke(width = 2.dp.toPx())
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Frosted Glass Logo Container
                    Surface(
                        modifier = Modifier
                            .size(160.dp)
                            .shadow(
                                elevation = 40.dp,
                                shape = CircleShape,
                                spotColor = TerracottaPrimary.copy(alpha = 0.3f)
                            ),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.85f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize().blur(0.5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logo),
                                contentDescription = "Logo",
                                modifier = Modifier
                                    .size(90.dp)
                                    .padding(8.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // --- TYPOGRAPHY SECTION ---
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "HASTA KALA",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 8.sp,
                        color = ArtisanWarmBlack
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "EMPOWERING ARTISANS DIGITALLY",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 4.sp,
                        color = TerracottaPrimary.copy(alpha = 0.8f)
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(2.dp)
                        .background(GoldAccent.copy(alpha = 0.4f))
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Where tradition meets technology",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Light,
                        textAlign = TextAlign.Center,
                        color = ArtisanWarmBlack.copy(alpha = 0.7f),
                        lineHeight = 32.sp
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // --- ACTION SECTION ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Primary Action: Get Started (Now goes straight to ArtisanAuthActivity)
                Button(
                    onClick = { onGetStarted() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(68.dp)
                        .shadow(
                            elevation = 20.dp,
                            shape = RoundedCornerShape(34.dp),
                            spotColor = TerracottaPrimary.copy(alpha = 0.6f)
                        ),
                    shape = RoundedCornerShape(34.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(TerracottaPrimary, Color(0xFFC96B54))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "GET STARTED".uppercase(),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 2.sp
                                ),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp),
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            // --- FOOTER ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(
                    text = "v1.0.0 • Developed by Bdriii",
                    style = MaterialTheme.typography.labelMedium,
                    color = ArtisanWarmBlack.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtisanLoginScreen(
    isOtpSent: Boolean,
    isLoading: Boolean = false,
    onOtpSentChange: (Boolean) -> Unit,
    onLoginClick: (String, String?) -> Unit,
    onVerifyOtp: (String, String) -> Unit,
    onForgotPassword: (String) -> Unit,
    onGoogleLogin: () -> Unit,
    onCreateAccount: () -> Unit,
    onGuestLogin: () -> Unit
) {
    var loginTab by remember { mutableIntStateOf(0) } // 0: Mobile, 1: Email
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordStage by remember { mutableStateOf(false) }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var otpCode by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ArtisanCream)
    ) {
        // --- PREMIUM BACKGROUND DECORATION ---
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Soft atmospheric glows (matching WelcomeScreen)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(TerracottaLight.copy(alpha = 0.12f), Color.Transparent),
                    center = Offset(width * 0.9f, height * 0.1f),
                    radius = width
                )
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(GoldAccent.copy(alpha = 0.08f), Color.Transparent),
                    center = Offset(width * 0.1f, height * 0.4f),
                    radius = width * 0.8f
                )
            )
        }

        // Submerged Logo Motif
        Icon(
            imageVector = Icons.Default.FilterVintage,
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-60).dp, y = 40.dp)
                .size(240.dp)
                .alpha(0.03f),
            tint = TerracottaPrimary
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // --- HEADER SECTION ---
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        modifier = Modifier
                            .size(60.dp)
                            .shadow(12.dp, CircleShape, spotColor = TerracottaPrimary.copy(alpha = 0.3f)),
                        shape = CircleShape,
                        color = Color.White
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Hasta Kala",
                            modifier = Modifier.padding(12.dp),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "HASTA KALA",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 4.sp
                        ),
                        color = ArtisanWarmBlack
                    )

                    Text(
                        text = stringResource(R.string.empowering_artisans).uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            fontSize = 9.sp
                        ),
                        color = TerracottaPrimary.copy(alpha = 0.7f)
                    )
                }


                Spacer(modifier = Modifier.height(24.dp))

                // --- LOGIN FORM CARD ---
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 24.dp,
                            shape = RoundedCornerShape(28.dp),
                            spotColor = TerracottaPrimary.copy(alpha = 0.12f)
                        ),
                    shape = RoundedCornerShape(28.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Welcome Back Text
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.welcome_back),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ArtisanWarmBlack
                                )
                                Text(
                                    text = stringResource(R.string.login_subtitle),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ArtisanWarmBlack.copy(alpha = 0.5f)
                                )
                            }
                            
                            // Decorative Accent
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = GoldAccent.copy(alpha = 0.6f)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // --- CUSTOM TAB SELECTOR ---
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(ArtisanCream.copy(alpha = 0.7f))
                                .padding(3.dp)
                        ) {
                            LoginTabItem(
                                text = stringResource(R.string.tab_mobile_login),
                                selected = loginTab == 0,
                                onClick = { 
                                    loginTab = 0 
                                    onOtpSentChange(false)
                                    isPasswordStage = false
                                },
                                modifier = Modifier.weight(1f)
                            )
                            LoginTabItem(
                                text = stringResource(R.string.tab_email_login),
                                selected = loginTab == 1,
                                onClick = { 
                                    loginTab = 1 
                                    onOtpSentChange(false)
                                    isPasswordStage = false
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // --- INPUT FIELDS ---
                        AnimatedContent(
                            targetState = Triple(loginTab, isOtpSent, isPasswordStage),
                            transitionSpec = {
                                if (targetState.first != initialState.first) {
                                    // Tab change: Vertical slide
                                    fadeIn(animationSpec = tween(300)) +
                                            slideInVertically(initialOffsetY = { 40 }) togetherWith
                                            fadeOut(animationSpec = tween(150))
                                } else {
                                    // Stage change within tab: Horizontal slide
                                    val isForward = (targetState.second && !initialState.second) || 
                                                  (targetState.third && !initialState.third)
                                    
                                    if (isForward) {
                                        slideInHorizontally { it / 2 } + fadeIn() togetherWith 
                                        slideOutHorizontally { -it / 2 } + fadeOut()
                                    } else {
                                        slideInHorizontally { -it / 2 } + fadeIn() togetherWith 
                                        slideOutHorizontally { it / 2 } + fadeOut()
                                    }
                                }
                            },
                            label = "login_stage_content"
                        ) { (targetTab, otpVisible, passwordVisibleStage) ->
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                if (targetTab == 0) {
                                    OutlinedTextField(
                                        value = phoneNumber,
                                        onValueChange = { if (it.length <= 10) phoneNumber = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = !otpVisible,
                                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                                            color = ArtisanWarmBlack,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        placeholder = {
                                            Text(
                                                stringResource(R.string.placeholder_mobile),
                                                color = ArtisanWarmBlack.copy(alpha = 0.3f)
                                            )
                                        },
                                        leadingIcon = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(start = 16.dp)
                                            ) {
                                                Text("🇮🇳", fontSize = 18.sp)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    "+91",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = ArtisanWarmBlack
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .height(24.dp)
                                                        .width(1.dp)
                                                        .background(ArtisanWarmBlack.copy(alpha = 0.1f))
                                                )
                                            }
                                        },
                                        trailingIcon = {
                                            if (otpVisible) {
                                                IconButton(onClick = { onOtpSentChange(false) }) {
                                                    Icon(
                                                        Icons.Default.Edit,
                                                        "Edit Phone Number",
                                                        tint = TerracottaPrimary,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                        },
                                        shape = RoundedCornerShape(18.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = TerracottaPrimary,
                                            unfocusedBorderColor = ArtisanWarmBlack.copy(alpha = 0.1f),
                                            focusedContainerColor = ArtisanCream.copy(alpha = 0.3f),
                                            unfocusedContainerColor = ArtisanCream.copy(alpha = 0.3f),
                                            disabledContainerColor = ArtisanCream.copy(alpha = 0.1f),
                                            cursorColor = TerracottaPrimary
                                        ),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                        singleLine = true
                                    )

                                    if (otpVisible) {
                                        OutlinedTextField(
                                            value = otpCode,
                                            onValueChange = { if (it.length <= 6) otpCode = it },
                                            modifier = Modifier.fillMaxWidth(),
                                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                                color = ArtisanWarmBlack,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 8.sp,
                                                textAlign = TextAlign.Center
                                            ),
                                            placeholder = {
                                                Text(
                                                    "ENTER 6-DIGIT OTP",
                                                    modifier = Modifier.fillMaxWidth(),
                                                    textAlign = TextAlign.Center,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = ArtisanWarmBlack.copy(alpha = 0.3f)
                                                )
                                            },
                                            shape = RoundedCornerShape(18.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = TerracottaPrimary,
                                                unfocusedBorderColor = ArtisanWarmBlack.copy(alpha = 0.1f),
                                                focusedContainerColor = ArtisanCream.copy(alpha = 0.3f),
                                                unfocusedContainerColor = ArtisanCream.copy(alpha = 0.3f),
                                                cursorColor = TerracottaPrimary
                                            ),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true
                                        )

                                        Text(
                                            text = "Didn't receive OTP? Resend in 00:30",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ArtisanWarmBlack.copy(alpha = 0.5f),
                                            modifier = Modifier
                                                .align(Alignment.End)
                                                .padding(end = 8.dp)
                                        )
                                    }
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                        ArtisanTextField(
                                            value = email,
                                            onValueChange = { email = it },
                                            placeholder = stringResource(R.string.placeholder_email),
                                            leadingIcon = Icons.Default.Email,
                                            keyboardType = KeyboardType.Email,
                                            enabled = !passwordVisibleStage,
                                            trailingIcon = if (passwordVisibleStage) {
                                                {
                                                    IconButton(onClick = { isPasswordStage = false }) {
                                                        Icon(
                                                            Icons.Default.Edit,
                                                            "Edit Email",
                                                            tint = TerracottaPrimary,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    }
                                                }
                                            } else null
                                        )

                                        if (passwordVisibleStage) {
                                            Column {
                                                ArtisanTextField(
                                                    value = password,
                                                    onValueChange = { password = it },
                                                    placeholder = "Password",
                                                    leadingIcon = Icons.Default.Lock,
                                                    keyboardType = KeyboardType.Password,
                                                    isPassword = !isPasswordVisible,
                                                    trailingIcon = {
                                                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                                            Icon(
                                                                imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                                contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                                                                tint = TerracottaPrimary.copy(alpha = 0.6f),
                                                                modifier = Modifier.size(20.dp)
                                                            )
                                                        }
                                                    }
                                                )
                                                
                                                TextButton(
                                                    onClick = { onForgotPassword(email) },
                                                    modifier = Modifier.align(Alignment.End),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Text(
                                                        "Forgot Password?",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        color = TerracottaPrimary,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        val isInputValid = if (loginTab == 0) {
                            if (isOtpSent) otpCode.length == 6 else phoneNumber.length == 10
                        } else {
                            val emailValid = email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
                            if (isPasswordStage) emailValid && password.length >= 6 else emailValid
                        }

                        // --- PRIMARY ACTION BUTTON (Matching Welcome Screen) ---
                        Button(
                            onClick = {
                                if (loginTab == 0) {
                                    if (isOtpSent) {
                                        onVerifyOtp(phoneNumber, otpCode)
                                    } else {
                                        onLoginClick(phoneNumber, null)
                                    }
                                } else {
                                    if (isPasswordStage) {
                                        onLoginClick(email, password)
                                    } else {
                                        isPasswordStage = true
                                    }
                                }
                            },
                            enabled = isInputValid && !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .shadow(if (isInputValid && !isLoading) 10.dp else 0.dp, RoundedCornerShape(27.dp), spotColor = TerracottaPrimary.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(27.dp),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = if (isInputValid && !isLoading) {
                                                listOf(TerracottaPrimary, Color(0xFFC96B54))
                                            } else {
                                                listOf(ArtisanWarmBlack.copy(alpha = 0.12f), ArtisanWarmBlack.copy(alpha = 0.12f))
                                            }
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (loginTab == 0) {
                                                if (isOtpSent) "VERIFY & LOGIN" else stringResource(R.string.btn_send_otp).uppercase()
                                            } else {
                                                if (isPasswordStage) "LOG IN" else stringResource(R.string.btn_continue).uppercase()
                                            },
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                letterSpacing = 1.sp,
                                                color = if (isInputValid) Color.White else ArtisanWarmBlack.copy(alpha = 0.3f)
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowForward,
                                            null,
                                            modifier = Modifier.size(16.dp),
                                            tint = if (isInputValid) Color.White else ArtisanWarmBlack.copy(alpha = 0.3f)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // OR Divider
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.weight(1f).height(1.dp).background(ArtisanWarmBlack.copy(alpha = 0.05f)))
                            Text(
                                "  OR  ",
                                style = MaterialTheme.typography.labelSmall,
                                color = ArtisanWarmBlack.copy(alpha = 0.3f),
                                fontWeight = FontWeight.Bold
                            )
                            Box(modifier = Modifier.weight(1f).height(1.dp).background(ArtisanWarmBlack.copy(alpha = 0.05f)))
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // --- SOCIAL LOGIN ---
                        OutlinedButton(
                            onClick = onGoogleLogin,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(25.dp),
                            border = BorderStroke(1.dp, ArtisanWarmBlack.copy(alpha = 0.1f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ArtisanWarmBlack)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_google_logo),
                                contentDescription = "Google",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                stringResource(R.string.btn_continue_google),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // --- GUEST LOGIN ---
                        TextButton(
                            onClick = onGuestLogin,
                            modifier = Modifier.fillMaxWidth().height(40.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.continue_guest),
                                style = MaterialTheme.typography.labelLarge,
                                color = ArtisanWarmBlack.copy(alpha = 0.6f),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // --- FEATURE HIGHLIGHTS ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FeatureSmallCard(
                        icon = Icons.Default.Shield,
                        text = stringResource(R.string.feature_secure),
                        modifier = Modifier.weight(1f)
                    )
                    FeatureSmallCard(
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        text = stringResource(R.string.feature_grow),
                        modifier = Modifier.weight(1f)
                    )
                    FeatureSmallCard(
                        icon = Icons.Default.Brush,
                        text = "ARTISAN",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- FOOTER ACTION ---
                TextButton(
                    onClick = onCreateAccount,
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {
                    Text(
                        buildAnnotatedString {
                            append(stringResource(R.string.new_to_hastakala) + " ")
                            withStyle(SpanStyle(color = TerracottaPrimary, fontWeight = FontWeight.ExtraBold)) {
                                append(stringResource(R.string.create_account).uppercase())
                            }
                        },
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.ArrowOutward, null, modifier = Modifier.size(12.dp), tint = TerracottaPrimary)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ArtisanRegistrationScreen(
    isLoading: Boolean = false,
    onRegisterClick: (String, String, String, String) -> Unit,
    onBackToLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ArtisanCream)
    ) {
        // Background Glows
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(TerracottaLight.copy(alpha = 0.12f), Color.Transparent),
                    center = Offset(size.width * 0.1f, size.height * 0.1f),
                    radius = size.width
                )
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(GoldAccent.copy(alpha = 0.08f), Color.Transparent),
                    center = Offset(size.width * 0.9f, size.height * 0.4f),
                    radius = size.width * 0.8f
                )
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Header
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        modifier = Modifier
                            .size(60.dp)
                            .shadow(
                                12.dp,
                                CircleShape,
                                spotColor = TerracottaPrimary.copy(alpha = 0.3f)
                            ),
                        shape = CircleShape,
                        color = Color.White
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Hasta Kala",
                            modifier = Modifier.padding(12.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "CREATE ACCOUNT",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp
                        ),
                        color = ArtisanWarmBlack
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(24.dp, RoundedCornerShape(28.dp), spotColor = TerracottaPrimary.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(28.dp),
                    color = Color.White
                ) {
                    Column(modifier = Modifier.padding(28.dp)) {
                        ArtisanTextField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = "Full Name",
                            leadingIcon = Icons.Default.Person
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        ArtisanTextField(
                            value = email,
                            onValueChange = { email = it },
                            placeholder = "Email Address",
                            leadingIcon = Icons.Default.Email,
                            keyboardType = KeyboardType.Email
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        ArtisanTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            placeholder = "Mobile Number",
                            leadingIcon = Icons.Default.Phone,
                            keyboardType = KeyboardType.Phone
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        ArtisanTextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = "Password",
                            leadingIcon = Icons.Default.Lock,
                            keyboardType = KeyboardType.Password,
                            isPassword = true
                        )

                        Spacer(modifier = Modifier.height(36.dp))

                        val isFormValid = name.isNotBlank() && 
                                         email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() && 
                                         phone.length >= 10 && 
                                         password.length >= 6

                        Button(
                            onClick = { onRegisterClick(name, email, phone, password) },
                            enabled = isFormValid && !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .shadow(if (isFormValid && !isLoading) 10.dp else 0.dp, RoundedCornerShape(27.dp), spotColor = TerracottaPrimary.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(27.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = if (isFormValid && !isLoading) {
                                                listOf(TerracottaPrimary, Color(0xFFC96B54))
                                            } else {
                                                listOf(ArtisanWarmBlack.copy(alpha = 0.12f), ArtisanWarmBlack.copy(alpha = 0.12f))
                                            }
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        "JOIN THE COMMUNITY",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 1.sp,
                                            color = if (isFormValid) Color.White else ArtisanWarmBlack.copy(alpha = 0.3f)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                TextButton(
                    onClick = onBackToLogin,
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {
                    Text(
                        buildAnnotatedString {
                            append("ALREADY HAVE AN ACCOUNT? ")
                            withStyle(SpanStyle(color = TerracottaPrimary, fontWeight = FontWeight.ExtraBold)) {
                                append("LOG IN")
                            }
                        },
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ArtisanTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    enabled: Boolean = true,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = ArtisanWarmBlack, fontWeight = FontWeight.SemiBold),
        placeholder = { Text(placeholder, color = ArtisanWarmBlack.copy(alpha = 0.3f)) },
        leadingIcon = { Icon(leadingIcon, null, tint = TerracottaPrimary.copy(alpha = 0.6f)) },
        trailingIcon = trailingIcon,
        shape = RoundedCornerShape(18.dp),
        singleLine = true,
        visualTransformation = if (isPassword) androidx.compose.ui.text.input.PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = TerracottaPrimary,
            unfocusedBorderColor = ArtisanWarmBlack.copy(alpha = 0.1f),
            focusedContainerColor = ArtisanCream.copy(alpha = 0.3f),
            unfocusedContainerColor = ArtisanCream.copy(alpha = 0.3f),
            disabledContainerColor = ArtisanCream.copy(alpha = 0.1f),
            cursorColor = TerracottaPrimary
        )
    )
}

@Composable
fun LoginTabItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) Color.White else Color.Transparent,
        animationSpec = tween(300),
        label = "tab_bg"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) TerracottaPrimary else ArtisanWarmBlack.copy(alpha = 0.5f),
        animationSpec = tween(300),
        label = "tab_text"
    )

    Surface(
        modifier = modifier
            .padding(2.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp),
        tonalElevation = if (selected) 4.dp else 0.dp,
        shadowElevation = if (selected) 4.dp else 0.dp
    ) {
        Box(
            modifier = Modifier.padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Bold,
                color = textColor,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun FeatureSmallCard(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(TerracottaPrimary.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, modifier = Modifier.size(16.dp), tint = TerracottaPrimary)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp,
                    fontSize = 8.sp
                ),
                textAlign = TextAlign.Center,
                maxLines = 1,
                color = ArtisanWarmBlack.copy(alpha = 0.6f)
            )
        }
    }
}
