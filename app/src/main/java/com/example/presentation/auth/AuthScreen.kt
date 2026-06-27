package com.example.presentation.auth

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

data class CountryInfo(val name: String, val flag: String, val code: String)

@Composable
fun AuthScreen(onAuthSuccess: () -> Unit) {
    val countries = remember {
        listOf(
            CountryInfo("India", "🇮🇳", "+91"),
            CountryInfo("Australia", "🇦🇺", "+61"),
            CountryInfo("Brazil", "🇧🇷", "+55"),
            CountryInfo("Canada", "🇨🇦", "+1"),
            CountryInfo("France", "🇫🇷", "+33"),
            CountryInfo("Germany", "🇩🇪", "+49"),
            CountryInfo("Japan", "🇯🇵", "+81"),
            CountryInfo("United Kingdom", "🇬🇧", "+44"),
            CountryInfo("United States", "🇺🇸", "+1")
        )
    }
    
    var selectedCountry by remember { mutableStateOf(countries.first()) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    var phoneNumber by remember { mutableStateOf("") }
    var smsCode by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf("") }
    var isCodeSent by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val activity = context as? Activity

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFE8E2FF),
            Color(0xFFEDF4F9)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
            .padding(horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))
            
            // Icon
            Surface(
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 16.dp,
                modifier = Modifier.size(120.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Smartphone,
                        contentDescription = null,
                        tint = Color(0xFF5A4EE3),
                        modifier = Modifier.size(56.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
            
            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF131022)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Enter your phone number to continue",
                style = MaterialTheme.typography.titleMedium,
                color = Slate500
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Country Dropdown
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Country",
                    style = MaterialTheme.typography.labelLarge,
                    color = Slate900,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Box {
                    Surface(
                        onClick = { isDropdownExpanded = true },
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Slate200),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = selectedCountry.flag, style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "${selectedCountry.name} (${selectedCountry.code})",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Slate900,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = Slate400)
                        }
                    }
                    
                    DropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.85f).background(Color.White)
                    ) {
                        countries.forEach { country ->
                            DropdownMenuItem(
                                text = { Text("${country.flag}  ${country.name} (${country.code})", color = Slate900) },
                                onClick = {
                                    selectedCountry = country
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (!isCodeSent) {
                // Phone Number Input
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Phone Number",
                        style = MaterialTheme.typography.labelLarge,
                        color = Slate900,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Slate200),
                            modifier = Modifier.width(72.dp)
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = selectedCountry.code,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { 
                                phoneNumber = it
                                errorMessage = null 
                            },
                            placeholder = { Text("123 456 7890", color = Slate400) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White,
                                unfocusedBorderColor = Slate200,
                                focusedBorderColor = DayFlowPrimary,
                                focusedTextColor = Slate900,
                                unfocusedTextColor = Slate900
                            )
                        )
                    }
                }
            } else {
                // OTP Input
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Enter OTP",
                        style = MaterialTheme.typography.labelLarge,
                        color = Slate900,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "We sent a code to ${selectedCountry.code} $phoneNumber",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate500,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = smsCode,
                        onValueChange = { 
                            smsCode = it
                            errorMessage = null 
                        },
                        placeholder = { Text("123456", color = Slate400) },
                        modifier = Modifier.fillMaxWidth(0.6f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White,
                            unfocusedBorderColor = Slate200,
                            focusedBorderColor = DayFlowPrimary,
                            focusedTextColor = Slate900,
                            unfocusedTextColor = Slate900
                        ),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
                    )
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            val isContinueEnabled = if (isCodeSent) {
                smsCode.length >= 6
            } else {
                phoneNumber.length >= 7
            }

            Button(
                onClick = {
                    if (activity == null) {
                        errorMessage = "Cannot start verification without Activity context"
                        return@Button
                    }
                    if (!isCodeSent) {
                        if (phoneNumber.isBlank()) {
                            errorMessage = "Please enter a phone number"
                            return@Button
                        }
                        isLoading = true
                        val fullNumber = selectedCountry.code + phoneNumber.replace(" ", "")
                        val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                            .setPhoneNumber(fullNumber)
                            .setTimeout(60L, TimeUnit.SECONDS)
                            .setActivity(activity)
                            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                                    FirebaseAuth.getInstance().signInWithCredential(credential)
                                        .addOnCompleteListener { task ->
                                            isLoading = false
                                            if (task.isSuccessful) onAuthSuccess()
                                            else errorMessage = task.exception?.localizedMessage
                                        }
                                }
                                override fun onVerificationFailed(e: FirebaseException) {
                                    isLoading = false
                                    errorMessage = e.localizedMessage ?: "Verification failed"
                                }
                                override fun onCodeSent(verId: String, token: PhoneAuthProvider.ForceResendingToken) {
                                    isLoading = false
                                    verificationId = verId
                                    isCodeSent = true
                                }
                            }).build()
                        PhoneAuthProvider.verifyPhoneNumber(options)
                    } else {
                        if (smsCode.isBlank()) {
                            errorMessage = "Please enter the SMS code"
                            return@Button
                        }
                        isLoading = true
                        val credential = PhoneAuthProvider.getCredential(verificationId, smsCode)
                        FirebaseAuth.getInstance().signInWithCredential(credential)
                            .addOnCompleteListener { task ->
                                isLoading = false
                                if (task.isSuccessful) onAuthSuccess()
                                else errorMessage = task.exception?.localizedMessage ?: "Invalid code"
                            }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFA694F5),
                    disabledContainerColor = Color(0xFFA694F5).copy(alpha = 0.5f)
                ),
                enabled = !isLoading && isContinueEnabled
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        if (isCodeSent) "Verify Code" else "Continue",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "By continuing, you agree to Cadence's Terms of\nService and Privacy Policy",
                style = MaterialTheme.typography.bodySmall,
                color = Slate500,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )
        }
    }
}
