package com.example.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
import com.example.ui.components.*
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.PrimaryIndigoLight
import com.example.ui.theme.SecondaryCyan

@Composable
fun WelcomeScreen(
    onCreateAccountClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Branding Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(PrimaryIndigo, PrimaryIndigoLight, SecondaryCyan)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Hub,
                        contentDescription = "ConnectUp",
                        tint = Color.White,
                        modifier = Modifier.size(60.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Connect",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-1).sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Up",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-1).sp
                        ),
                        color = PrimaryIndigoLight
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Connect. Share. Discover.",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "A modern social space for meaningful connections, creative stories, and genuine community.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ConnectUpButton(
                    text = "Create Account",
                    onClick = onCreateAccountClick,
                    testTag = "welcome_create_account_button"
                )

                ConnectUpButton(
                    text = "Login",
                    onClick = onLoginClick,
                    isSecondary = true,
                    testTag = "welcome_login_button"
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "By continuing, you agree to ConnectUp's Terms of Service and Privacy Policy.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun LoginScreen(
    onNavigateBack: () -> Unit,
    onLoginSubmit: (identifier: String, pass: String, rememberMe: Boolean) -> Unit,
    onNavigateToRegister: () -> Unit,
    isLoading: Boolean,
    errorMessage: String?
) {
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(true) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            ConnectUpTopBar(
                title = "",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Welcome back 👋",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Sign in to catch up with your friends and community.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(28.dp))

            ConnectUpTextField(
                value = identifier,
                onValueChange = { identifier = it },
                label = "Email or Username",
                placeholder = "e.g. jordanlee or jordan@example.com",
                leadingIcon = Icons.Outlined.Person,
                testTag = "login_identifier_input"
            )

            Spacer(modifier = Modifier.height(16.dp))

            ConnectUpTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                placeholder = "Enter your password",
                leadingIcon = Icons.Outlined.Lock,
                isPassword = true,
                testTag = "login_password_input"
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Remember me and Forgot password
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { rememberMe = !rememberMe }
                ) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it },
                        modifier = Modifier.testTag("remember_me_checkbox")
                    )
                    Text(
                        text = "Remember me",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                TextButton(
                    onClick = { showForgotPasswordDialog = true },
                    modifier = Modifier.testTag("forgot_password_button")
                ) {
                    Text(
                        text = "Forgot password?",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = PrimaryIndigo
                    )
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            ConnectUpButton(
                text = "Sign In",
                onClick = { onLoginSubmit(identifier, password, rememberMe) },
                isLoading = isLoading,
                enabled = identifier.isNotBlank() && password.isNotBlank(),
                testTag = "login_submit_button"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Switch to Register
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't have an account?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = onNavigateToRegister,
                    modifier = Modifier.testTag("navigate_to_register_button")
                ) {
                    Text(
                        text = "Sign Up",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryIndigo
                    )
                }
            }

            // Quick demo hint
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Demo Accounts Ready to Test:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• User: jordanlee / Password123!\n• Admin: connectup_admin / AdminPassword123!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (showForgotPasswordDialog) {
        var resetEmail by remember { mutableStateOf(identifier) }
        var resetSent by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = { Text("Reset Password") },
            text = {
                if (resetSent) {
                    Text("If an account exists with $resetEmail, password reset instructions have been sent.")
                } else {
                    Column {
                        Text(
                            "Enter your registered email address and we'll send you a link to reset your password.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = resetEmail,
                            onValueChange = { resetEmail = it },
                            label = { Text("Email") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                if (!resetSent) {
                    Button(
                        onClick = { resetSent = true },
                        enabled = resetEmail.isNotBlank()
                    ) {
                        Text("Send Reset Link")
                    }
                } else {
                    Button(onClick = { showForgotPasswordDialog = false }) {
                        Text("Done")
                    }
                }
            },
            dismissButton = {
                if (!resetSent) {
                    TextButton(onClick = { showForgotPasswordDialog = false }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }
}

@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    onRegisterSubmit: (fullName: String, username: String, email: String, pass: String, dob: String) -> Unit,
    onNavigateToLogin: () -> Unit,
    isLoading: Boolean,
    errorMessage: String?
) {
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("2000-01-01") }

    var fullNameError by remember { mutableStateOf<String?>(null) }
    var usernameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmError by remember { mutableStateOf<String?>(null) }

    fun validate(): Boolean {
        var isValid = true

        if (fullName.isBlank()) {
            fullNameError = "Please enter your full name."
            isValid = false
        } else fullNameError = null

        val cleanUser = username.trim()
        if (cleanUser.length < 3) {
            usernameError = "Username must be at least 3 characters."
            isValid = false
        } else if (!cleanUser.matches(Regex("^[a-zA-Z0-9_.]+$"))) {
            usernameError = "Letters, numbers, underscores and dots only."
            isValid = false
        } else usernameError = null

        if (!email.contains("@") || !email.contains(".")) {
            emailError = "Please enter a valid email address."
            isValid = false
        } else emailError = null

        if (password.length < 6) {
            passwordError = "Password must be at least 6 characters."
            isValid = false
        } else passwordError = null

        if (password != confirmPassword) {
            confirmError = "Passwords do not match."
            isValid = false
        } else confirmError = null

        return isValid
    }

    Scaffold(
        topBar = {
            ConnectUpTopBar(
                title = "",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Join ConnectUp ✨",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Create an account to start sharing and discovering.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            ConnectUpTextField(
                value = fullName,
                onValueChange = { fullName = it; fullNameError = null },
                label = "Full Name",
                placeholder = "e.g. Alex Rivera",
                leadingIcon = Icons.Outlined.Badge,
                errorText = fullNameError,
                testTag = "register_fullname_input"
            )

            Spacer(modifier = Modifier.height(14.dp))

            ConnectUpTextField(
                value = username,
                onValueChange = { username = it; usernameError = null },
                label = "Username",
                placeholder = "e.g. alexrivera",
                leadingIcon = Icons.Outlined.AlternateEmail,
                errorText = usernameError,
                testTag = "register_username_input"
            )

            Spacer(modifier = Modifier.height(14.dp))

            ConnectUpTextField(
                value = email,
                onValueChange = { email = it; emailError = null },
                label = "Email Address",
                placeholder = "e.g. alex@example.com",
                leadingIcon = Icons.Outlined.Email,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                errorText = emailError,
                testTag = "register_email_input"
            )

            Spacer(modifier = Modifier.height(14.dp))

            ConnectUpTextField(
                value = password,
                onValueChange = { password = it; passwordError = null },
                label = "Password",
                placeholder = "At least 6 characters",
                leadingIcon = Icons.Outlined.Lock,
                isPassword = true,
                errorText = passwordError,
                testTag = "register_password_input"
            )

            Spacer(modifier = Modifier.height(14.dp))

            ConnectUpTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; confirmError = null },
                label = "Confirm Password",
                placeholder = "Re-enter password",
                leadingIcon = Icons.Outlined.LockClock,
                isPassword = true,
                errorText = confirmError,
                testTag = "register_confirm_password_input"
            )

            Spacer(modifier = Modifier.height(14.dp))

            ConnectUpTextField(
                value = dateOfBirth,
                onValueChange = { dateOfBirth = it },
                label = "Date of Birth (YYYY-MM-DD)",
                placeholder = "YYYY-MM-DD",
                leadingIcon = Icons.Outlined.Cake,
                testTag = "register_dob_input"
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            ConnectUpButton(
                text = "Create Account",
                onClick = {
                    if (validate()) {
                        onRegisterSubmit(fullName, username, email, password, dateOfBirth)
                    }
                },
                isLoading = isLoading,
                testTag = "register_submit_button"
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = onNavigateToLogin,
                    modifier = Modifier.testTag("navigate_to_login_button")
                ) {
                    Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryIndigo
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun OnboardingScreen(
    user: UserEntity,
    suggestedUsers: List<UserEntity>,
    onFollowUser: (targetUserId: Long) -> Unit,
    onComplete: (bio: String, website: String, avatarUrl: String) -> Unit
) {
    var bio by remember { mutableStateOf(user.bio) }
    var website by remember { mutableStateOf(user.website) }
    var selectedAvatar by remember { mutableStateOf(user.profileImage) }
    val followedSet = remember { mutableStateListOf<Long>() }

    val presetAvatars = listOf(
        "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400",
        "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400",
        "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=400",
        "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400",
        "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400"
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Set Up Your Profile",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Welcome @${user.username}! Let others get to know you.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Avatar Selection
            UserAvatar(
                imageUrl = selectedAvatar,
                name = user.name,
                size = 88.dp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Choose an avatar or photo:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                presetAvatars.forEach { avatarUrl ->
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .clickable { selectedAvatar = avatarUrl }
                    ) {
                        UserAvatar(
                            imageUrl = avatarUrl,
                            name = "Option",
                            size = 44.dp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bio Input
            ConnectUpTextField(
                value = bio,
                onValueChange = { bio = it },
                label = "Short Bio",
                placeholder = "What are you passionate about?",
                maxLines = 3,
                singleLine = false,
                testTag = "onboarding_bio_input"
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Website Input
            ConnectUpTextField(
                value = website,
                onValueChange = { website = it },
                label = "Website / Portfolio (optional)",
                placeholder = "https://yourwebsite.com",
                leadingIcon = Icons.Outlined.Link,
                testTag = "onboarding_website_input"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Suggested Creators to Follow
            if (suggestedUsers.isNotEmpty()) {
                Text(
                    text = "Creators to Follow",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    suggestedUsers.take(3).forEach { suggested ->
                        val isFollowed = followedSet.contains(suggested.id)
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                UserAvatar(
                                    imageUrl = suggested.profileImage,
                                    name = suggested.name,
                                    size = 40.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = suggested.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "@${suggested.username}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Button(
                                    onClick = {
                                        if (isFollowed) {
                                            followedSet.remove(suggested.id)
                                        } else {
                                            followedSet.add(suggested.id)
                                            onFollowUser(suggested.id)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isFollowed) MaterialTheme.colorScheme.surfaceVariant else PrimaryIndigo,
                                        contentColor = if (isFollowed) MaterialTheme.colorScheme.onSurfaceVariant else Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(if (isFollowed) "Following" else "Follow", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            ConnectUpButton(
                text = "Enter ConnectUp",
                onClick = { onComplete(bio, website, selectedAvatar) },
                testTag = "complete_onboarding_button"
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
