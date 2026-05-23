package com.vahitkeskin.loopsweep.ui.screen.cloud

import com.vahitkeskin.loopsweep.ui.theme.*

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.loopsweep.domain.model.XiaomiDevice
import com.vahitkeskin.loopsweep.presentation.XiaomiCloudViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun XiaomiCloudScreen(
    viewModel: XiaomiCloudViewModel,
    activeIp: String,
    activeToken: String,
    onSaveConnection: (ip: String, token: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val username by viewModel.username.collectAsState()
    val password by viewModel.password.collectAsState()
    val region by viewModel.region.collectAsState()
    
    val manualUserId by viewModel.manualUserId.collectAsState()
    val manualServiceToken by viewModel.manualServiceToken.collectAsState()
    val manualSsecurity by viewModel.manualSsecurity.collectAsState()
    
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val devices by viewModel.devices.collectAsState()
    val loginStatus by viewModel.loginStatus.collectAsState()
    
    var regionDropdownExpanded by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    
    val scrollState = rememberScrollState()

    // Premium Background Gradient
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DeepPurpleBg,
                        DeepPurpleCard,
                        DeepPurpleDarkBg
                    )
                )
            )
    ) {
        // Decorative glowing orbs in the background
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.TopEnd)
                .offset(x = 100.dp, y = (-50).dp)
                .background(TranslucentPurple0C, shape = RoundedCornerShape(150.dp))
                .blur(50.dp)
        )
        Box(
            modifier = Modifier
                .size(250.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-80).dp, y = 80.dp)
                .background(TranslucentBlue0C, shape = RoundedCornerShape(125.dp))
                .blur(50.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // Header Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "☁️",
                    fontSize = 32.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "Mi Bulut Entegrasyonu",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Text(
                text = "Xiaomi Home hesabınız üzerinden süpürgenizin token ve IP bilgilerini otomatik çekin",
                fontSize = 13.sp,
                color = Color.LightGray.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 20.dp)
            )

            // Current Config Summary Card
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Aktif Süpürge Bağlantısı",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = LavenderPurple
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "IP ADRESİ", fontSize = 10.sp, color = Color.Gray)
                            Text(text = activeIp.ifBlank { "Tanımlanmamış" }, fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                        Column {
                            Text(text = "TOKEN", fontSize = 10.sp, color = Color.Gray)
                            Text(
                                text = if (activeToken.length > 8) "${activeToken.take(6)}...${activeToken.takeLast(4)}" else activeToken.ifBlank { "Tanımlanmamış" },
                                fontSize = 14.sp,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Info messages (Loading / Success / Error)
            AnimatedVisibility(
                visible = error != null || loginStatus != null || isLoading,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            error != null -> TranslucentRed26
                            isLoading -> TranslucentBlue1F
                            else -> TranslucentGreen1F
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .border(
                            1.dp,
                            when {
                                error != null -> AlertRed.copy(alpha = 0.5f)
                                isLoading -> ThemeBlue.copy(alpha = 0.5f)
                                else -> EmeraldGreen.copy(alpha = 0.5f)
                            },
                            RoundedCornerShape(12.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = ThemeBlue,
                                modifier = Modifier.size(24.dp).padding(end = 12.dp),
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                text = if (error != null) "❌" else "✅",
                                fontSize = 18.sp,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                        }
                        Text(
                            text = error ?: loginStatus ?: "İşlem yapılıyor...",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Fetch Results Card (Device List)
            if (devices.isNotEmpty()) {
                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Bulunan Xiaomi Cihazları (${devices.size})",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Bağlanmak istediğiniz akıllı süpürgeyi seçin:",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        devices.forEachIndexed { index, device ->
                            DeviceRow(
                                device = device,
                                onSelect = {
                                    onSaveConnection(device.ip, device.token)
                                }
                            )
                            if (index < devices.lastIndex) {
                                Divider(
                                    color = Color.White.copy(alpha = 0.08f),
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Main Tab View for login modes
            var selectedTab by remember { mutableStateOf(0) }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.04f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val tabs = listOf("Bulut Girişi", "Token Override", "Manuel Kayıt")
                tabs.forEachIndexed { index, title ->
                    val selected = selectedTab == index
                    val scale by animateFloatAsState(
                        targetValue = if (selected) 1.04f else 1.0f,
                        animationSpec = spring(stiffness = Spring.StiffnessLow),
                        label = "CloudTabScale"
                    )
                    
                    val bg = if (selected) {
                        Brush.horizontalGradient(listOf(ThemeIndigo, MediumPurple))
                    } else {
                        Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                    }
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .background(brush = bg, shape = RoundedCornerShape(10.dp))
                            .border(
                                width = 0.5.dp,
                                color = if (selected) Color.White.copy(alpha = 0.15f) else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedTab = index }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            color = if (selected) Color.White else Color.White.copy(alpha = 0.55f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            when (selectedTab) {
                0 -> {
                    // Cloud Login Form Card
                    GlassmorphicCard(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Xiaomi Hesabı ile Bağlan",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            // Username Input
                            OutlinedTextField(
                                value = username,
                                onValueChange = { viewModel.username.value = it },
                                label = { Text("E-posta, Telefon veya Mi ID") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MediumPurple,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                                    focusedLabelColor = MediumPurple,
                                    unfocusedLabelColor = Color.White.copy(alpha = 0.4f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color.White.copy(alpha = 0.02f),
                                    unfocusedContainerColor = Color.White.copy(alpha = 0.02f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                            )

                            // Password Input with Visibility Toggle
                            OutlinedTextField(
                                value = password,
                                onValueChange = { viewModel.password.value = it },
                                label = { Text("Şifre") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MediumPurple,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                                    focusedLabelColor = MediumPurple,
                                    unfocusedLabelColor = Color.White.copy(alpha = 0.4f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color.White.copy(alpha = 0.02f),
                                    unfocusedContainerColor = Color.White.copy(alpha = 0.02f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    Text(
                                        text = if (passwordVisible) "👁️" else "👁️‍🗨️",
                                        modifier = Modifier
                                            .clickable { passwordVisible = !passwordVisible }
                                            .padding(8.dp),
                                        fontSize = 14.sp
                                    )
                                },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                            )

                            // Region Selector
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 20.dp)
                            ) {
                                val selectedRegionName = viewModel.regionsList.firstOrNull { it.code == region }?.displayName ?: region
                                OutlinedTextField(
                                    value = selectedRegionName,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Mi Home Sunucu Bölgesi") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MediumPurple,
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                                        focusedLabelColor = MediumPurple,
                                        unfocusedLabelColor = Color.White.copy(alpha = 0.4f),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedContainerColor = Color.White.copy(alpha = 0.02f),
                                        unfocusedContainerColor = Color.White.copy(alpha = 0.02f)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    trailingIcon = {
                                        Text(
                                            text = "▼",
                                            color = Color.Gray,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                    }
                                )
                                // Transparent overlay click catcher
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clickable { regionDropdownExpanded = true }
                                )

                                DropdownMenu(
                                    expanded = regionDropdownExpanded,
                                    onDismissRequest = { regionDropdownExpanded = false },
                                    modifier = Modifier
                                        .fillMaxWidth(0.85f)
                                        .background(PurpleGrayBg)
                                ) {
                                    viewModel.regionsList.forEach { reg ->
                                        DropdownMenuItem(
                                            text = { Text(reg.displayName, color = Color.White) },
                                            onClick = {
                                                viewModel.region.value = reg.code
                                                regionDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Submit Button (Modern Gradient Style)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .background(
                                        brush = Brush.horizontalGradient(listOf(ThemeIndigo, MediumPurple)),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable(enabled = !isLoading) { viewModel.loginAndFetchDevices() },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isLoading) "Giriş Yapılıyor..." else "Giriş Yap ve Cihazları Çek",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
                1 -> {
                    // Manual Token Paste Card
                    GlassmorphicCard(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Token Girişi ile Çek (Bypass)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = "Şifreli girişte CAPTCHA veya 2FA koruması takılıyorsa, tarayıcıdan aldığınız session tokenlarını buraya yapıştırıp cihazları listeleyebilirsiniz.",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            OutlinedTextField(
                                value = manualUserId,
                                onValueChange = { viewModel.manualUserId.value = it },
                                label = { Text("Mi Account UserId") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MediumPurple,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                                    focusedLabelColor = MediumPurple,
                                    unfocusedLabelColor = Color.White.copy(alpha = 0.4f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color.White.copy(alpha = 0.02f),
                                    unfocusedContainerColor = Color.White.copy(alpha = 0.02f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = manualServiceToken,
                                onValueChange = { viewModel.manualServiceToken.value = it },
                                label = { Text("serviceToken (xiaomiio)") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MediumPurple,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                                    focusedLabelColor = MediumPurple,
                                    unfocusedLabelColor = Color.White.copy(alpha = 0.4f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color.White.copy(alpha = 0.02f),
                                    unfocusedContainerColor = Color.White.copy(alpha = 0.02f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = manualSsecurity,
                                onValueChange = { viewModel.manualSsecurity.value = it },
                                label = { Text("ssecurity") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MediumPurple,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                                    focusedLabelColor = MediumPurple,
                                    unfocusedLabelColor = Color.White.copy(alpha = 0.4f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color.White.copy(alpha = 0.02f),
                                    unfocusedContainerColor = Color.White.copy(alpha = 0.02f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                singleLine = true
                            )

                            // Submit Button (Green Gradient Style)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .background(
                                        brush = Brush.horizontalGradient(listOf(EmeraldGreen, ThemeCyan)),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable(enabled = !isLoading) { viewModel.fetchDevicesWithManualTokens() },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isLoading) "Cihazlar Getiriliyor..." else "Tokenlar ile Bağlan ve Çek",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
                2 -> {
                    // Direct Manual IP & Token Save Form
                    var directIp by remember { mutableStateOf(activeIp) }
                    var directToken by remember { mutableStateOf(activeToken) }

                    GlassmorphicCard(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Doğrudan IP ve Token Tanımla",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = "Bulut hesabı kullanmadan, süpürgenizin yerel IP adresi ile 32 karakterli Hex Token bilgisini elle girerek doğrudan kaydedin.",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            OutlinedTextField(
                                value = directIp,
                                onValueChange = { directIp = it },
                                label = { Text("Robot Yerel IP Adresi") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MediumPurple,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                                    focusedLabelColor = MediumPurple,
                                    unfocusedLabelColor = Color.White.copy(alpha = 0.4f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color.White.copy(alpha = 0.02f),
                                    unfocusedContainerColor = Color.White.copy(alpha = 0.02f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                singleLine = true,
                                placeholder = { Text("Örn: 192.168.1.150") }
                            )

                            OutlinedTextField(
                                value = directToken,
                                onValueChange = { directToken = it },
                                label = { Text("32 Karakterli Hex Token") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MediumPurple,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                                    focusedLabelColor = MediumPurple,
                                    unfocusedLabelColor = Color.White.copy(alpha = 0.4f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color.White.copy(alpha = 0.02f),
                                    unfocusedContainerColor = Color.White.copy(alpha = 0.02f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                singleLine = true,
                                placeholder = { Text("Örn: 4a526f6b5f546f6b656e5f5f5f5f5f5f") }
                            )

                            // Save Button (Gradient style)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .background(
                                        brush = Brush.horizontalGradient(listOf(ThemeIndigo, MediumPurple)),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        if (directIp.isBlank() || directToken.length != 32) {
                                            viewModel.clear()
                                        } else {
                                            onSaveConnection(directIp.trim(), directToken.trim())
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Doğrudan Bağlantıyı Kaydet",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(136.dp))
        }
    }
}

@Composable
fun DeviceRow(
    device: XiaomiDevice,
    onSelect: () -> Unit
) {
    // Pulse animation for online/offline status dot
    val infiniteTransition = rememberInfiniteTransition()
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "StatusDotPulse"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = device.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                // Online/Offline glowing pulse status dot
                val dotColor = if (device.isOnline) EmeraldGreen else AlertRed
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(dotColor.copy(alpha = pulseAlpha))
                        .border(0.5.dp, dotColor, RoundedCornerShape(4.dp))
                )
            }
            Text(
                text = "Model: ${device.model} • IP: ${device.ip}",
                color = Color.Gray,
                fontSize = 11.sp
            )
            Text(
                text = "Token: ${device.token.take(6)}...${device.token.takeLast(4)}",
                color = Color.Gray.copy(alpha = 0.7f),
                fontSize = 10.sp
            )
        }
        
        Button(
            onClick = onSelect,
            colors = ButtonDefaults.buttonColors(
                containerColor = MediumPurple.copy(alpha = 0.15f)
            ),
            border = BorderStroke(1.dp, MediumPurple.copy(alpha = 0.5f)),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.height(34.dp)
        ) {
            Text(
                text = "Seç",
                color = BrightPurple,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.03f)
        ),
        modifier = modifier
            .border(
                1.dp,
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.12f),
                        Color.White.copy(alpha = 0.02f)
                    )
                ),
                RoundedCornerShape(16.dp)
            )
    ) {
        content()
    }
}
