package com.example.climbingteam.composables.specifics

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.climbingteam.ui.theme.ClimbingColors
import com.example.climbingteam.viewmodels.AuthState
import com.example.climbingteam.viewmodels.AuthViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

private const val GOOGLE_WEB_CLIENT_ID =
    "220128782759-qp3ncrakne4gv3clbvkumrm9qjvgd7ej.apps.googleusercontent.com"

@Composable
fun ScreenLogin(
    navController: NavController,
    vm: AuthViewModel = viewModel()
) {
    val showLoginForm = rememberSaveable { mutableStateOf(true) }
    val error = rememberSaveable { mutableStateOf<String?>(null) }
    val authState by vm.authState.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val logoScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.5f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "logoScale"
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(600),
        label = "logoAlpha"
    )

    fun navigateHome() {
        navController.navigate("compare") { popUpTo("login") { inclusive = true } }
    }

    fun handleGoogleSignIn() {
        scope.launch {
            try {
                val credentialManager = CredentialManager.create(context)
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(GOOGLE_WEB_CLIENT_ID)
                    .setAutoSelectEnabled(true)
                    .build()
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()
                val result = credentialManager.getCredential(context, request)
                val googleCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
                vm.signInWithGoogle(
                    idToken = googleCredential.idToken,
                    onSucces = { navigateHome() },
                    onError = { error.value = it }
                )
            } catch (e: GetCredentialException) {
                Log.e("GoogleSignIn", "Credential error", e)
                error.value = "No se pudo iniciar sesión con Google"
            } catch (e: Exception) {
                Log.e("GoogleSignIn", "Error", e)
                error.value = "Error al conectar con Google"
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0D1117), Color(0xFF0F2744), Color(0xFF0D1117))
                )
            )
    ) {
        // Decorative circles
        Box(
            modifier = Modifier.size(300.dp).offset(x = (-80).dp, y = (-80).dp)
                .background(
                    Brush.radialGradient(listOf(ClimbingColors.primary.copy(alpha = 0.08f), Color.Transparent)),
                    shape = RoundedCornerShape(50)
                )
        )
        Box(
            modifier = Modifier.size(200.dp).align(Alignment.BottomEnd).offset(x = 60.dp, y = 60.dp)
                .background(
                    Brush.radialGradient(listOf(Color(0xFF3FB950).copy(alpha = 0.06f), Color.Transparent)),
                    shape = RoundedCornerShape(50)
                )
        )

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(60.dp))

            // Logo
            Box(
                modifier = Modifier
                    .graphicsLayer(scaleX = logoScale, scaleY = logoScale, alpha = logoAlpha)
                    .size(80.dp).clip(RoundedCornerShape(24.dp))
                    .background(Brush.linearGradient(listOf(ClimbingColors.primary, Color(0xFF3FB950)))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Landscape, null, tint = Color.White, modifier = Modifier.size(44.dp))
            }

            Spacer(Modifier.height(24.dp))

            // ── Email verification sent screen ─────────────────────────────
            if (authState is AuthState.VerificationSent) {
                VerificationSentCard(
                    email = (authState as AuthState.VerificationSent).email,
                    onBack = {
                        vm.clearAuthState()
                        showLoginForm.value = true
                    }
                )
                Spacer(Modifier.height(40.dp))
                return@Column
            }

            // ── Title ──────────────────────────────────────────────────────
            AnimatedContent(
                targetState = showLoginForm.value,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
                label = "titleAnim"
            ) { isLogin ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        if (isLogin) "Bienvenido" else "Crear cuenta",
                        style = MaterialTheme.typography.headlineMedium,
                        color = ClimbingColors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (isLogin) "Inicia sesión para continuar" else "Regístrate gratis",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ClimbingColors.textTertiary
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── Google Sign-In button ──────────────────────────────────────
            AnimatedVisibility(visible = visible, enter = fadeIn(tween(600))) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    OutlinedButton(
                        onClick = { handleGoogleSignIn() },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = ClimbingColors.surfaceVariant,
                            contentColor = ClimbingColors.textPrimary
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, ClimbingColors.searchBarBorder
                        )
                    ) {
                        Icon(
                            Icons.Default.AccountCircle, null,
                            tint = Color(0xFF4285F4),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            if (showLoginForm.value) "Continuar con Google" else "Registrarse con Google",
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Divider(modifier = Modifier.weight(1f), color = ClimbingColors.divider)
                        Text(
                            "  o con email  ",
                            style = MaterialTheme.typography.bodySmall,
                            color = ClimbingColors.textTertiary
                        )
                        Divider(modifier = Modifier.weight(1f), color = ClimbingColors.divider)
                    }

                    Spacer(Modifier.height(16.dp))
                }
            }

            // ── Email/password form card ───────────────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)
                ) + fadeIn(tween(500))
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ClimbingColors.cardBackground),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        AnimatedContent(
                            targetState = showLoginForm.value,
                            transitionSpec = {
                                (slideInHorizontally { if (targetState) -it else it } + fadeIn()) togetherWith
                                        (slideOutHorizontally { if (targetState) it else -it } + fadeOut())
                            },
                            label = "formAnim"
                        ) { isLogin ->
                            ModernUserForm(
                                isCreateAccount = !isLogin,
                                onDone = { email, password ->
                                    error.value = null
                                    if (isLogin) {
                                        vm.login(email, password,
                                            onSucces = { navigateHome() },
                                            onError = { error.value = it }
                                        )
                                    } else {
                                        vm.register(email, password,
                                            onSucces = { /* AuthState.VerificationSent will show the card */ },
                                            onError = { error.value = it }
                                        )
                                    }
                                }
                            )
                        }

                        // Needs verification notice + resend button
                        if (authState is AuthState.NeedsVerification) {
                            Spacer(Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ClimbingColors.aceptable.copy(alpha = 0.12f))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Email, null,
                                    tint = ClimbingColors.aceptable, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Email no verificado. Revisa tu bandeja de entrada.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ClimbingColors.aceptable,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Error
                        AnimatedVisibility(visible = error.value != null) {
                            error.value?.let {
                                Spacer(Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(ClimbingColors.adverso.copy(alpha = 0.12f))
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Warning, null,
                                        tint = ClimbingColors.adverso, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(it, style = MaterialTheme.typography.bodySmall,
                                        color = ClimbingColors.adverso, lineHeight = 16.sp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Toggle login / register
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (showLoginForm.value) "¿No tienes cuenta?" else "¿Ya tienes cuenta?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ClimbingColors.textTertiary
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    if (showLoginForm.value) "Regístrate" else "Inicia sesión",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ClimbingColors.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable {
                        showLoginForm.value = !showLoginForm.value
                        error.value = null
                        vm.clearAuthState()
                    }
                )
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

// ── Email verification sent card ──────────────────────────────────────────────
@Composable
private fun VerificationSentCard(email: String, onBack: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ClimbingColors.cardBackground)
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("📧", fontSize = 48.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                "¡Revisa tu email!",
                style = MaterialTheme.typography.headlineSmall,
                color = ClimbingColors.textPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Hemos enviado un enlace de verificación a:",
                style = MaterialTheme.typography.bodyMedium,
                color = ClimbingColors.textSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                email,
                style = MaterialTheme.typography.bodyLarge,
                color = ClimbingColors.primary,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Verifica tu cuenta y luego inicia sesión.",
                style = MaterialTheme.typography.bodySmall,
                color = ClimbingColors.textTertiary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ClimbingColors.primary)
            ) {
                Text("Ir al inicio de sesión", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ── Email/password form ───────────────────────────────────────────────────────
@Composable
private fun ModernUserForm(isCreateAccount: Boolean, onDone: (String, String) -> Unit) {
    val email = rememberSaveable { mutableStateOf("") }
    val password = rememberSaveable { mutableStateOf("") }
    val passwordVisible = rememberSaveable { mutableStateOf(false) }
    val isValid = remember(email.value, password.value) {
        email.value.trim().isNotEmpty() && password.value.trim().length >= 6
    }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = email.value,
            onValueChange = { email.value = it },
            label = { Text("Email") },
            leadingIcon = {
                Icon(Icons.Default.Email, null,
                    tint = if (email.value.isNotEmpty()) ClimbingColors.primary else ClimbingColors.textTertiary,
                    modifier = Modifier.size(20.dp))
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            colors = loginFieldColors(),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password.value,
            onValueChange = { password.value = it },
            label = { Text("Contraseña") },
            leadingIcon = {
                Icon(Icons.Default.Lock, null,
                    tint = if (password.value.isNotEmpty()) ClimbingColors.primary else ClimbingColors.textTertiary,
                    modifier = Modifier.size(20.dp))
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible.value = !passwordVisible.value }) {
                    Icon(
                        if (passwordVisible.value) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        "Ver contraseña", tint = ClimbingColors.textTertiary, modifier = Modifier.size(20.dp)
                    )
                }
            },
            singleLine = true,
            visualTransformation = if (passwordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            colors = loginFieldColors(),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )

        if (isCreateAccount) {
            Text(
                "La contraseña debe tener al menos 6 caracteres.\nRecibirás un email de verificación.",
                style = MaterialTheme.typography.labelSmall,
                color = ClimbingColors.textTertiary,
                lineHeight = 14.sp
            )
        }

        Spacer(Modifier.height(4.dp))

        Box(
            modifier = Modifier.fillMaxWidth().height(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (isValid) Brush.horizontalGradient(listOf(ClimbingColors.primary, Color(0xFF3FB950)))
                    else Brush.horizontalGradient(listOf(ClimbingColors.textTertiary, ClimbingColors.textTertiary))
                )
                .clickable(enabled = isValid) {
                    onDone(email.value.trim(), password.value.trim())
                    keyboardController?.hide()
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (isCreateAccount) "Crear cuenta" else "Iniciar sesión",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun loginFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = ClimbingColors.surfaceVariant,
    unfocusedContainerColor = ClimbingColors.surfaceVariant,
    focusedBorderColor = ClimbingColors.primary,
    unfocusedBorderColor = ClimbingColors.searchBarBorder,
    focusedLabelColor = ClimbingColors.primary,
    unfocusedLabelColor = ClimbingColors.textTertiary,
    cursorColor = ClimbingColors.primary,
    focusedTextColor = ClimbingColors.textPrimary,
    unfocusedTextColor = ClimbingColors.textPrimary
)
