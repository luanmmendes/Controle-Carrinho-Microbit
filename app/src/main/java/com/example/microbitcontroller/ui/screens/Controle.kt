package com.example.microbitcontroller.ui.screens

import android.util.Log
import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.Paint
import android.widget.Button
import androidx.collection.emptyLongSet
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.microbitcontroller.R
import com.example.microbitcontroller.ble.BleConnectionStatus
import com.example.microbitcontroller.ui.theme.LedActive
import com.example.microbitcontroller.ui.theme.LedInactive
import kotlin.math.log


@Composable
fun Botoes(
    ratioX: Float,
    ratioY: Float,
    maxWidth: Dp,
    maxHeight: Dp,
    content: @Composable () -> Unit = {},
    onPressChanged: (Boolean) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(isPressed) {
        onPressChanged(isPressed)
    }

    Box(
        modifier = Modifier
            .size(125.dp)
            .offset(
                x = (maxWidth * ratioX), y = (maxHeight * ratioY)
            )
            .clickable(
                interactionSource = interactionSource, indication = null, onClick = {})
    ) { content() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview(
    showSystemUi = true,
    showBackground = true,
    device = "spec:width=411dp,height=917dp,orientation=landscape"
)
fun PinControlView(
    onPinAction: (pin: Int, isHigh: Boolean) -> Unit = { _, _ -> },
    FBController: (pin: Int, isHigh: Boolean) -> Unit = { _, _ -> },
    setVelocidade: (pin: Int, value: Int) -> Unit = { _, _ -> },
    setCurvatura: (pin: Int, value: Int) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val activity = context as? Activity;
    val interacao = remember { MutableInteractionSource() }
    val isPressed by interacao.collectIsPressedAsState();
    DisposableEffect(BleConnectionStatus.READY) {

        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        Box(Modifier.align(Alignment.BottomCenter)){
            Box(modifier = Modifier.offset(y = -(45).dp)){}
            androidx.compose.material3.Button({}, content = {})
        }
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1.0f, true)
        ) {

            Image(
                painter = painterResource(id = R.drawable.group_1_1_),
                contentDescription = null,
                alignment = Alignment.CenterStart,
                modifier = Modifier
                    .fillMaxHeight()
                    .offset(150.dp)
            )
            LaunchedEffect(isPressed) {
                if (isPressed) {
                    Log.d("controle", "andar!")
                } else {
                    Log.d("controle", "PAROU PAROU PAROU")
                }
            }
            //cima
            Botoes(0.350f, 0.005f, this.maxWidth, this.maxHeight) { isHolding ->
                if (isHolding) {
                    Log.d("controle", "andando")
                    FBController(15, true);
                    setVelocidade(12, 1023)
                    setCurvatura(13, 512);
                } else {
                    Log.d("controle", "parado")
                    FBController(15, false);
                    onPinAction(14, false);
                }

            }
            //baixo
            Botoes(0.350f, 0.700f, this.maxWidth, this.maxHeight) { isHolding ->
                if (isHolding) {
                    Log.d("controle", "baixo")
                    setVelocidade(12, 1023);
                    setCurvatura(13, 512);
                    FBController(15, false);
                } else {
                    Log.d("controle", "parado");
                    setVelocidade(12, 0);
                    setCurvatura(13, 512);
                    onPinAction(14, false);

                }
            }

        }
        BoxWithConstraints(Modifier.align(Alignment.CenterEnd)) {
            Image(
                painter = painterResource(id = R.drawable.group_2),
                contentDescription = null,
                modifier = Modifier
                    .size(400.dp)
                    .offset(-(30).dp)
            )

            //esquerdo
            Botoes(-(0.030f), 0.350f, this.maxWidth, this.maxHeight) { isHolding ->
                if (isHolding) {
                    Log.d("controle", "baixo")
                    setVelocidade(12, 0);
                    setCurvatura(13, 0);
                    FBController(15, true);

                } else {
                    Log.d("controle", "parado");
                    setVelocidade(12, 0);
                    setCurvatura(13, 0);
                    FBController(15, false);

                }
            }
            //direito
            Botoes(0.270f, 0.350f, this.maxWidth, this.maxHeight) { isHolding ->
                if (isHolding) {
                    Log.d("controle", "baixo")
                    setVelocidade(12, 0);
                    setCurvatura(13, 0);
                    FBController(15, true);

                } else {
                    Log.d("controle", "parado");
                    setVelocidade(12, 0);
                    setCurvatura(13, 0);
                    FBController(15, false);

                }
            }
        }
    }
}




