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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.climbingteam.ui.theme.ClimbingColors
import com.example.climbingteam.viewmodels.AuthViewModel

@Composable
fun ScreenLogin(
    navController: NavController,
    vm: AuthViewModel = viewModel()
) {
    val showLoginForm = rememberSaveable { mutableStateOf(true) }
    val error = rememberSaveable { mutableStateOf<String?>(null) }

    // Entrance animation
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0D1117), Color(0xFF0F2744), Color(0xFF0D1117)),
                    startY = 0f, endY = Float.POSITIVE_INFINITY
                )
            )
    ) {
        // Decorative circles
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-80).dp, y = (-80).dp)
                .background(
                    Brush.radialGradient(listOf(ClimbingColors.primary.copy(alpha = 0.08f), Color.Transparent)),
                    shape = RoundedCornerShape(50)
                )
        )
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 60.dp, y = 60.dp)
                .background(
                    Brush.radialGradient(listOf(Color(0xFF3FB950).copy(alpha = 0.06f), Color.Transparent)),
                    shape = RoundedCornerShape(50)
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(60.dp))

            // Logo / Icon
            Box(
                modifier = Modifier
                    .graphicsLayer(scaleX = logoScale, scaleY = logoScale, alpha = logoAlpha)
                    .size(80.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(ClimbingColors.primary, Color(0xFF3FB950))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Landscape,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

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
                        if (isLogin) "Inicia sesi\u00f3n para continuar" else "Reg\u00edstrate gratis",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ClimbingColors.textTertiary
                    )
                }
            }

            Spacer(Modifier.height(36.dp))

            // Form card
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
                                    if (isLogin) {
                                        Log.d("Login", "logueando con $email")
                                        vm.login(email, password, onSucces = {
                                            error.value = null
                                            navController.navigate("compare") {
                                                popUpTo("login") { inclusive = true }
                                            }
                                        }, onError = {
                                            Log.d("err_login", it)
                                            error.value = it
                                        })
                                    } else {
                                        Log.d("Register", "creando cuenta con $email")
                                        vm.register(email, password, onSucces = {
                                            error.value = null
                                            navController.navigate("compare") {
                                                popUpTo("login") { inclusive = true }
                                            }
                                        }, onError = {
                                            Log.d("err_register", it)
                                            error.value = it
                                        })
                                    }
                                }
                            )
                        }

                        // Error
                        AnimatedVisibility(visible = error.value != null) {
                            error.value?.let {
                                Spacer(Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
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
                    if (showLoginForm.value) "\u00bfNo tienes cuenta?" else "\u00bfYa tienes cuenta?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ClimbingColors.textTertiary
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    if (showLoginForm.value) "Reg\u00edstrate" else "Inicia sesi\u00f3n",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ClimbingColors.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable {
                        showLoginForm.value = !showLoginForm.value
                        error.value = null
                    }
                )
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun ModernUserForm(
    isCreateAccount: Boolean,
    onDone: (String, String) -> Unit
) {
    val email = rememberSaveable { mutableStateOf("") }
    val password = rememberSaveable { mutableStateOf("") }
    val passwordVisible = rememberSaveable { mutableStateOf(false) }
    val isValid = remember(email.value, password.value) {
        email.value.trim().isNotEmpty() && password.value.trim().length >= 6
    }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Email
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
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = ClimbingColors.surfaceVariant,
                unfocusedContainerColor = ClimbingColors.surfaceVariant,
                focusedBorderColor = ClimbingColors.primary,
                unfocusedBorderColor = ClimbingColors.searchBarBorder,
                focusedLabelColor = ClimbingColors.primary,
                unfocusedLabelColor = ClimbingColors.textTertiary,
                cursorColor = ClimbingColors.primary,
                focusedTextColor = ClimbingColors.textPrimary,
                unfocusedTextColor = ClimbingColors.textPrimary
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )

        // Password
        OutlinedTextField(
            value = password.value,
            onValueChange = { password.value = it },
            label = { Text("Contrase\u00f1a") },
            leadingIcon = {
                Icon(Icons.Default.Lock, null,
                    tint = if (password.value.isNotEmpty()) ClimbingColors.primary else ClimbingColors.textTertiary,
                    modifier = Modifier.size(20.dp))
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible.value = !passwordVisible.value }) {
                    Icon(
                        if (passwordVisible.value) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Ver contrase\u00f1a",
                        tint = ClimbingColors.textTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            singleLine = true,
            visualTransformation = if (passwordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = ClimbingColors.surfaceVariant,
                unfocusedContainerColor = ClimbingColors.surfaceVariant,
                focusedBorderColor = ClimbingColors.primary,
                unfocusedBorderColor = ClimbingColors.searchBarBorder,
                focusedLabelColor = ClimbingColors.primary,
                unfocusedLabelColor = ClimbingColors.textTertiary,
                cursorColor = ClimbingColors.primary,
                focusedTextColor = ClimbingColors.textPrimary,
                unfocusedTextColor = ClimbingColors.textPrimary
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(4.dp))

        // Submit button with gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (isValid)
                        Brush.horizontalGradient(listOf(ClimbingColors.primary, Color(0xFF3FB950)))
                    else
                        Brush.horizontalGradient(listOf(ClimbingColors.textTertiary, ClimbingColors.textTertiary))
                )
                .clickable(enabled = isValid) {
                    onDone(email.value.trim(), password.value.trim())
                    keyboardController?.hide()
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (isCreateAccount) "Crear cuenta" else "Iniciar sesi\u00f3n",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
