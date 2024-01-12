package com.example.spotify_clone.presentation.landing

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spotify_clone.AppTheme
import com.example.spotify_clone.R
import com.example.spotify_clone.presentation.landing.utils.ButtonType
import com.example.spotify_clone.presentation.landing.widgets.CustomOutLinedButton
import kotlinx.coroutines.delay

@Composable
fun LandingScreen(modifier: Modifier = Modifier,viewModel: LandingScreenViewModel) {
    val arrayOfTitles = arrayOf("Google","Facebook","Apple")
    val context = LocalContext.current
    val isLandScape = Resources.getSystem().configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val scrollState = rememberScrollState()
    val isVisible = viewModel.isVisible.collectAsState()

    LaunchedEffect(Unit) {
        delay(500L)
        viewModel.updateIsVisible(true)
    }

    if (isLandScape){
        //LandScape ui
        Row(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0XFF121212)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            CoverPicSection(isVisible.value, true)
            AuthButtonSection(context, arrayOfTitles, true)
        }
    } else {
        //Portrait ui
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0XFF121212))
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CoverPicSection(isVisible.value,false)
            AuthButtonSection(context, arrayOfTitles,false)
        }
    }
}

@Composable
private fun AuthButtonSection(
    context: Context,
    arrayOfTitles: Array<String>,
    isLandScape: Boolean,
) {

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isLandScape){
            Text(text = "Millions of Songs.\n Free on Spotify.", style = TextStyle(fontSize = 28.sp))
            Spacer(modifier = Modifier.height(10.dp))
        }
        Button(
            modifier = Modifier
                .height(50.dp)
                .padding(1.dp)
                .fillMaxWidth(.8f),
            onClick = { /*TODO*/ }
        ) {
            Text(text = "Sign up for free")
        }
        for (i in 0..1) {
            Spacer(modifier = Modifier.height(10.dp))
            CustomOutLinedButton(
                modifier = Modifier
                    .height(50.dp)
                    .padding(1.dp)
                    .fillMaxWidth(.8f),
                index = i,
                onClick = { itemIndex: Int ->
                    when (itemIndex) {
                        ButtonType.GOOGLE -> Toast.makeText(context, "Google button", Toast.LENGTH_SHORT).show()
                        ButtonType.FACEBOOK -> Toast.makeText(context, "Facebook button", Toast.LENGTH_SHORT).show()
                        ButtonType.APPLE -> Toast.makeText(context, "Apple button", Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Text(
                    text = "Sign in with ${arrayOfTitles[i]}",
                    style = TextStyle(color = Color.White)
                )
            }
        }
    }
}

@Composable
private fun CoverPicSection(isVisible: Boolean, isLandScape: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            modifier = Modifier.fillMaxWidth(if (isLandScape) 0.5f else 1f),
            painter = painterResource(id = R.drawable.img),
            contentDescription = "cover image",
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(30.dp))
        if(!isLandScape)
            AnimatedTitle(isVisible)
        Spacer(modifier = Modifier.height(30.dp))
    }
}



@Composable
private fun AnimatedTitle(isVisible: Boolean) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(durationMillis = 1000)),
        exit = fadeOut(animationSpec = tween(durationMillis = 500))
    ) {
        Text(
            text = "Millions of Songs.\nFree on Spotify.",
            style = TextStyle(
                fontSize = 28.sp,
                fontWeight = FontWeight.W700,
                textAlign = TextAlign.Center
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LandingScreenPreview() {
    AppTheme {
        LandingScreen(viewModel = LandingScreenViewModel())
    }
}
