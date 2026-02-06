package com.nutritiontracker.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PrivacyPolicyScreen() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "Privacy Policy",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Last updated: February 2026",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        PolicySection(
            title = "Overview",
            content = "Nutrition Tracker is designed with your privacy in mind. This policy explains what data we collect, how it is used, and your rights regarding that data."
        )

        PolicySection(
            title = "Data Collection",
            content = """The app collects and processes the following data:

- Food Images: Photos you take or select are processed to identify food items and estimate nutritional content.

- Nutrition Data: Recognized food names, calorie counts, and macronutrient information are stored locally on your device.

- API Keys: Your AI provider API keys are stored using Android's EncryptedSharedPreferences with AES-256 encryption."""
        )

        PolicySection(
            title = "Image Transmission",
            content = """When you scan a food item, the image is:

1. Resized to a maximum of 1024 pixels on the longest edge to reduce data transmission.

2. Stripped of all EXIF metadata (location, device info, timestamps) before being sent.

3. Compressed to JPEG format at 80% quality.

4. Transmitted via HTTPS to the selected AI provider (Anthropic, Google, or xAI) for analysis.

5. NOT stored on any remote server by this app. The AI providers may process images according to their own privacy policies."""
        )

        PolicySection(
            title = "Local Storage",
            content = """All nutrition tracking data is stored locally on your device in an encrypted database. This includes:

- Food entry names and nutritional values
- Meal type and timestamp
- Local file paths to saved images (images stay on your device)

No tracking data is transmitted to any server."""
        )

        PolicySection(
            title = "Third-Party Services",
            content = """This app communicates with the following third-party AI services for food recognition:

- Anthropic (Claude API): https://www.anthropic.com/privacy
- Google (Gemini API): https://ai.google.dev/terms
- xAI (Grok API): https://x.ai/legal/privacy-policy

Images are sent to only the provider you select. You control which service processes your data."""
        )

        PolicySection(
            title = "API Key Security",
            content = "Your API keys are encrypted at rest using the Android Keystore system with AES-256-GCM encryption. Keys are never transmitted anywhere except directly to the respective AI provider's API endpoint over HTTPS."
        )

        PolicySection(
            title = "Offline Mode",
            content = "When your device is offline, images are queued locally for later analysis. Queued images are stored on your device's internal storage and are only transmitted when connectivity is restored. Failed queue items are automatically cleaned up."
        )

        PolicySection(
            title = "Your Rights",
            content = """You can at any time:

- Delete all stored food entries from the Daily or Weekly tracker screens.
- Clear your API keys from the Settings screen.
- Uninstall the app to remove all local data.
- Use Demo Mode without any API key to avoid sending any data externally."""
        )

        PolicySection(
            title = "Children's Privacy",
            content = "This app is not directed at children under 13. We do not knowingly collect personal information from children."
        )

        PolicySection(
            title = "Changes to This Policy",
            content = "We may update this privacy policy from time to time. Changes will be reflected in the app with an updated revision date."
        )

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun PolicySection(title: String, content: String) {
    Spacer(modifier = Modifier.height(12.dp))
    HorizontalDivider()
    Spacer(modifier = Modifier.height(12.dp))

    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = content,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface
    )
}
