package com.example.climbingteam.composables.specifics

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.climbingteam.R
import com.example.climbingteam.ui.Mods.backMain
import com.example.climbingteam.ui.Mods.fillMax
import com.example.climbingteam.ui.Styles

@Preview
@Composable
fun ScreenLogin (
   // navController: NavController,
    viewModel: LoginScreenViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
){

    //true = login; false = Create
    val showLoginForm = rememberSaveable{
        mutableStateOf(true)
    }
    Surface( modifier = Modifier
        .fillMaxSize()) {
        Column (
            fillMax.padding().then(backMain),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement =  Arrangement.Center

        )
        {
            Row (
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom){
                Image(painter = painterResource(R.drawable.applogo), contentDescription ="")
            }

            if (showLoginForm.value){
                Text("Inicia sesión")
                UserForm(
                    isCreateAccount = false
                )
                {
                        email, password ->

                    Log.d("1","logueando con $email y $password")
                   /*viewModel.SingInWithEmailAndPassword(email,password){

                   }*/
                }
            }
            else{
                Text("Crea una cuenta")
                UserForm(
                    isCreateAccount = true
                )
                {
                        email, password ->
                    Log.d("1","creando cuenta con $email y $password")
                }
            }



            Spacer(modifier = Modifier.height(15.dp))
            Row (

                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ){
                val text1=
                    if (showLoginForm.value)"¿No tienes cuenta?"
                    else "¿Ya tienes cuenta?"
                val text2=
                    if (showLoginForm.value)"Registrate"
                    else "Inicia Sesión"
                Text(text = text1)
                Text(text = text2,
                    modifier = Modifier
                        .clickable { showLoginForm.value = !showLoginForm.value }
                        .padding(start = 5.dp),
                    color = Styles.color_secondary
                )
            }
        }

    }




}

@Composable
fun UserForm(
    isCreateAccount: Boolean = false,
    onDone: (String, String)-> Unit = {email, pwd ->}
) {
    val email = rememberSaveable {
        mutableStateOf("")
    }
    val password = rememberSaveable {
        mutableStateOf("")
    }
    val passwordVisible = rememberSaveable {
        mutableStateOf(false)
    }
    val valido = remember(email.value,password.value){
        email.value.trim().isNotEmpty() &&
                password.value.trim().isNotEmpty()
    }


    val KeyboardController = LocalSoftwareKeyboardController.current
    Column (horizontalAlignment = Alignment.CenterHorizontally){
        EmailInput(
            emailState = email
        )
        PasswordInput(
            passwordState = password,
            labelId = "password",
            passwordVisible = passwordVisible
        )
        SubmitButton(
            textId = if (isCreateAccount)"crear cuenta" else "login",
            inputValido = valido
        ){
            onDone(email.value.trim(), password.value.trim())
            KeyboardController?.hide()
        }
       // onDone(email.value.trim(),password.value.trim())
        //KeyboardController?.hide()
    }
}

@Composable
fun SubmitButton(
    textId: String,
    inputValido: Boolean,
    onClic: () ->Unit

) {
    Button(onClick = onClic,
        enabled = inputValido) {
        Text(text = textId,
            modifier = Modifier
                .padding(5.dp))
    }

}

@Composable
fun PasswordInput(
    passwordState: MutableState<String>,
    labelId: String = "Contraseña",
    passwordVisible: MutableState<Boolean> = remember { mutableStateOf(false) }
) {
    val visualTransformation = if (passwordVisible.value) {
        VisualTransformation.None
    } else {
        PasswordVisualTransformation()
    }

    val icon = if (passwordVisible.value) {
        Icons.Filled.Favorite // Cambia esto por un ícono adecuado
    } else {
        Icons.Filled.FavoriteBorder // Cambia esto por un ícono adecuado
    }

    OutlinedTextField(
        value = passwordState.value,
        onValueChange = { passwordState.value = it },
        label = { Text(text = labelId) },
        singleLine = true,
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth(),
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        trailingIcon = {
            IconButton(onClick = {
                passwordVisible.value = !passwordVisible.value
            }) {
                Icon(imageVector = icon, contentDescription = "Toggle password visibility")
            }
        }
    )
}



@Composable
fun EmailInput
            (
    emailState: MutableState<String>,
    labelId: String ="Email"
) {
    InputFields (
        valueState = emailState,
        labelId =  labelId,
        inputType = KeyboardType.Email
    )
}

@Composable
fun InputFields(
    valueState: MutableState<String>,
    labelId: String,
    isSingleLine: Boolean = true,
    inputType: KeyboardType
){
    OutlinedTextField(
        value = valueState.value,
        onValueChange = {valueState.value = it},
        label = { Text(text = labelId)},
        singleLine = isSingleLine,

        modifier = Modifier
            .padding(bottom = 10.dp, start = 10.dp, end = 10.dp)
            .fillMaxWidth(),
        keyboardOptions = KeyboardOptions(
            keyboardType = inputType,

            )

    )

}