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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
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
    ratioX: Float, ratioY: Float, maxWidth: Dp, maxHeight: Dp,content: @Composable () -> Unit = {}, onPressChanged: (Boolean) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(isPressed) {
        onPressChanged(isPressed)
    }

    Box(
        modifier = Modifier
            .size(85.dp)
            .offset(
                x = (maxWidth * ratioX), y = (maxHeight * ratioY)
            )
            .clickable(
                interactionSource = interactionSource, indication = null, onClick = {})){content()}
}
@Composable
fun botaoAcelerador(){
    Image(
        painter = painterResource(id = R.drawable.shape_set),
        contentDescription = null
    );
}

@Composable
@Preview(
    showSystemUi = true,
    showBackground = true,
    device = "spec:width=411dp,height=917dp,orientation=landscape"
)
fun PinControlView(  onPinAction: (pin: Int, isHigh: Boolean) -> Unit = { _, _ -> }) {
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
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1.0f, true)
        ) {

            Image(
                painter = painterResource(id = R.drawable.group_1),
                contentDescription = null,
                alignment = Alignment.CenterStart,
                modifier = Modifier.fillMaxHeight()
            )
            LaunchedEffect(isPressed) {
                if (isPressed) {
                    Log.d("controle", "andar!")
                } else {
                    Log.d("controle", "PAROU PAROU PAROU")
                }
            }
            //cima
            Botoes(0.400f, 0.04f, this.maxWidth, this.maxHeight) { isHolding ->
                if (isHolding) {
                    Log.d("controle", "andando")
                    onPinAction(15,true,1023);
                } else {
                    Log.d("controle", "parado")
                    onPinAction(15,true,0);
                }

            }
            //baixo
            Botoes(0.400f, 0.750f, this.maxWidth, this.maxHeight) { isHolding ->
                if (isHolding) {
                    Log.d("controle", "baixo")
                } else {
                    Log.d("controle", "parado");

                }
            }
            //direita
            Botoes(0.750f, 0.400f, this.maxWidth, this.maxHeight) { isHolding ->
                if (isHolding) {
                    Log.d("controle", "baixo")
                } else {
                    Log.d("controle", "parado");

                }
            }
            //esquerdo
            Botoes(0.04f, 0.400f, this.maxWidth, this.maxHeight) { isHolding ->
                if (isHolding) {
                    Log.d("controle", "baixo")
                } else {
                    Log.d("controle", "parado");

                }
            }
            //diagonal superior esquerda
            Botoes(0.090f, 0.09f, this.maxWidth, this.maxHeight) { isHolding ->
                if (isHolding) {
                    Log.d("controle", "baixo")
                } else {
                    Log.d("controle", "parado");

                }
            }
            //diagonal superior direita
            Botoes(0.7f, 0.09f, this.maxWidth, this.maxHeight) { isHolding ->
                if (isHolding) {
                    Log.d("controle", "baixo")
                } else {
                    Log.d("controle", "parado");

                }
            }
            //diagonal inferior esquerda
            Botoes(0.10f, 0.700f, this.maxWidth, this.maxHeight) { isHolding ->
                if (isHolding) {
                    Log.d("controle", "baixo")
                } else {
                    Log.d("controle", "parado");

                }
            }
            //diagonal inferior direita
            Botoes(0.70f, 0.700f, this.maxWidth, this.maxHeight) { isHolding ->
                if (isHolding) {
                    Log.d("controle", "baixo")
                } else {
                    Log.d("controle", "parado");

                }
            }


//                    modifier = Modifier
//                        .size(70.dp)
//                        .offset(maxWidth * 0.06f, (maxHeight * 0.400f)),
//                    onClick = {}) { }
////                FloatingActionButton(
//                    modifier = Modifier
//                        .size(70.dp)
//                        .offset(maxWidth * 0.090f, (maxHeight * 0.09f)),
//                    onClick = {}) { }
//                FloatingActionButton(
//                    modifier = Modifier
//                        .size(70.dp)
//                        .offset(maxWidth * 0.7f, (maxHeight * 0.09f)),
//                    onClick = {}) { }


//                FloatingActionButton(
//                    modifier = Modifier
//                        .size(70.dp)
//                        .offset(maxWidth * 0.10f, (maxHeight * 0.700f) - 5.dp), onClick = {}) { }
//                FloatingActionButton(
//                    modifier = Modifier
//                        .size(70.dp)
//                        .offset(maxWidth * 0.70f, (maxHeight * 0.700f) - 3.dp), onClick = {}) { }

        }
        Botoes(0.785f, 0.400f, this.maxWidth, this.maxHeight,{botaoAcelerador()}) {

            isHolding ->
            if (isHolding) {
                Log.d("controle", "baixo");
                onPinAction(14,true,1023);

            } else {
                Log.d("controle", "parado");
                onPinAction(14,false, 1023);
            }
        }
    }
    }




