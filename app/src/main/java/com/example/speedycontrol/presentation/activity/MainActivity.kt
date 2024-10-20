package com.example.speedycontrol.presentation.activity

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.ACTION_REQUEST_ENABLE
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.speedycontrol.R
import com.example.speedycontrol.presentation.activity.MainActivity.Companion.m_address
import com.example.speedycontrol.presentation.activity.MainActivity.Companion.m_bluetoothSocket
import com.example.speedycontrol.presentation.activity.MainActivity.Companion.m_isConnected
import com.example.speedycontrol.presentation.activity.MainActivity.Companion.m_myUUID
import com.example.speedycontrol.ui.theme.SpeedyControlTheme
import java.io.IOException
import java.util.UUID.fromString
import java.util.UUID


lateinit var bluetoothManager: BluetoothManager
lateinit var bluetoothAdapter: BluetoothAdapter
lateinit var takePermission: ActivityResultLauncher<String>
lateinit var takeResultLauncher: ActivityResultLauncher<Intent>

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    companion object {
        var m_myUUID: UUID = fromString("b9541130-bc3c-471d-b71c-dc2bffeaa995")
        var m_bluetoothSocket: BluetoothSocket? = null

        var m_isConnected: Boolean = false
        lateinit var m_address: String

        //var enable by mutableStateOf(bluetoothAdapter.isEnabled)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                viewModel.isLoading.value
            }
        }
        enableEdgeToEdge()

        bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        takePermission = registerForActivityResult(ActivityResultContracts.RequestPermission()){
            if (it){
                val intent=Intent(ACTION_REQUEST_ENABLE)
                takeResultLauncher.launch(intent)
            }else{
                Toast.makeText(this, R.string.permi_bluetooth, Toast.LENGTH_SHORT).show()
            }
        }
        takeResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()
        ){
            result ->
            if (result.resultCode == RESULT_OK){
                Toast.makeText(this, R.string.on_bluetooth, Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this, R.string.off_bluetooth, Toast.LENGTH_SHORT).show()
            }
        }

        setContent {
            SpeedyControlTheme {
                MainScreen(context = this)
            }
        }
    }
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    context: Context
) {
    var show by rememberSaveable { mutableStateOf(false) }
    var show2 by rememberSaveable { mutableStateOf(false) }

    DialogDeviceP(show = show, context = context
    ){
        if (bluetoothAdapter.state == BluetoothAdapter.STATE_CONNECTING){
            Toast.makeText(context, R.string.blue_Connecting, Toast.LENGTH_SHORT).show()
        } else if (bluetoothAdapter.state == BluetoothAdapter.STATE_CONNECTED){
            show2 = true
        }
        show = false
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier
                .height(50.dp)
                .fillMaxWidth()
                .background(colorResource(id = R.color.pink_500))
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ){
                FilledTonalButton(
                    modifier = Modifier
                        .width(120.dp),
                    onClick = {
                        takePermission.launch(Manifest.permission.BLUETOOTH_CONNECT)
                    },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = colorResource(id = R.color.pink_200),
                        contentColor = colorResource(id = R.color.pink_500),
                        disabledContainerColor = colorResource(id = R.color.pink_dis),
                        disabledContentColor = colorResource(id = R.color.white)
                    ),
                    enabled = !bluetoothAdapter.isEnabled
                ) {
                    Text(text = stringResource(id =
                        if (bluetoothAdapter.isEnabled) {
                            R.string.on_bluetooth
                        }else{
                            R.string.enable_bluetooth
                        }
                    ),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                OutlinedButton(modifier = Modifier.width(150.dp),
                    onClick = {
                       show = true
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colorResource(id = R.color.pink_500),
                        containerColor = Color.Transparent
                    ),
                    border = BorderStroke(2.dp, colorResource(id = R.color.pink_500))
                ) {
                    Text(text = stringResource(id = R.string.paired_devices),
                        textAlign = TextAlign.Center
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Divider(modifier = Modifier.fillMaxWidth(),
                thickness = 2.dp,
                color = colorResource(id = R.color.pink_500)
            )
            Spacer(modifier = Modifier.height(9.dp))
            if (show2){
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.Start
                ){
                    Text(text = stringResource(id = R.string.blue_Connected),
                        textAlign = TextAlign.Start,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = colorResource(id = R.color.green_s)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    val color = colorResource(id = R.color.green_s)
                    Canvas(modifier = Modifier
                        .size(10.dp)
                        .align(Alignment.CenterVertically)) {
                        drawCircle(color = color)
                    }
                }
            }else{
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.Start
                ){
                    Text(text = stringResource(id = R.string.blue_NoConnected),
                        textAlign = TextAlign.Start,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = colorResource(id = R.color.gray2_s)
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    val color = colorResource(id = R.color.gray2_s)
                    Canvas(modifier = Modifier
                        .size(10.dp)
                        .align(Alignment.CenterVertically)) {
                        drawCircle(
                            color = color,
                            radius = 15f,
                            style = Stroke(width = 5f)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(50.dp))
            ControlM()
        }
    }
}

@Composable
fun ControlM(
    modifier: Modifier = Modifier
){
    Box(modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ){
        Column(horizontalAlignment = Alignment.CenterHorizontally){
            Button(modifier = Modifier.size(100.dp),
                onClick = {
                    sendCommand("A")
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.orange_s),
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(5.dp)
            ){
                Icon(modifier = Modifier.scale(3.5f),
                    imageVector = Icons.Filled.KeyboardArrowUp,
                    contentDescription = null
                )
            }
            Spacer(modifier = Modifier.height(15.dp))
            Row {
                Button(modifier = Modifier.size(100.dp),
                    onClick = {
                        sendCommand("B")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.orange_s),
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(5.dp)
                ){
                    Icon(modifier = Modifier.scale(3.5f),
                        imageVector = Icons.Filled.KeyboardArrowLeft,
                        contentDescription = null
                    )
                }
                Spacer(modifier = Modifier.width(100.dp))
                Button(modifier = Modifier.size(100.dp),
                    onClick = {
                        sendCommand("C")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.orange_s),
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(5.dp)
                ){
                    Icon(modifier = Modifier.scale(3.5f),
                        imageVector = Icons.Filled.KeyboardArrowRight,
                        contentDescription = null
                    )
                }
            }
            Spacer(modifier = Modifier.height(15.dp))
            Button(modifier = Modifier.size(100.dp),
                onClick = {
                    sendCommand("D")
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.orange_s),
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(5.dp)
            ){
                Icon(modifier = Modifier.scale(3.5f),
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
fun DialogDeviceP(
    show: Boolean,
    context: Context,
    onConfirm: () -> Unit
){
    val permissionPairedDevices: Unit = if (ActivityCompat.checkSelfPermission(
            context, Manifest.permission.BLUETOOTH_CONNECT
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        return
    } else {

    }

    val pairedDevices: Set<BluetoothDevice> = bluetoothAdapter.bondedDevices

    if(show){
        AlertDialog(
            onDismissRequest = { onConfirm() },
            containerColor = Color.White,
            modifier = Modifier.fillMaxWidth(),
            confirmButton = { },
            title = {
                Column {
                    Text(text = stringResource(id = R.string.paired_devices),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(id = R.color.red_s)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Divider(modifier = Modifier.fillMaxWidth(),
                        thickness = 1.dp,
                        color = colorResource(id = R.color.gray_s)
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .fillMaxWidth()
                        .padding(horizontal = 5.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ){
                    if (ActivityCompat.checkSelfPermission(
                            context, Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return@Column
                    }
                    pairedDevices.forEach { device ->
                        Card(modifier = Modifier
                            .clickable {
                                try {
                                    if (m_bluetoothSocket == null || !m_isConnected){
                                        m_address = device.address
                                        val deviceC: BluetoothDevice = bluetoothAdapter.getRemoteDevice(m_address)
                                        m_bluetoothSocket = deviceC.createRfcommSocketToServiceRecord(m_myUUID)
                                        m_bluetoothSocket!!.connect()
                                    }

                                    Toast.makeText(context, R.string.blue_Connected, Toast.LENGTH_SHORT).show()
                                    Log.i("MainActivity", R.string.blue_Connected.toString())
                                    onConfirm()
                                    return@clickable

                                } catch (e: IOException){
                                    e.printStackTrace()
                                    Toast.makeText(context, R.string.error_Conexion, Toast.LENGTH_SHORT).show()
                                    Log.i("MainActivity", R.string.error_Conexion.toString())
                                    onConfirm()
                                    return@clickable
                                }
                            }
                            .fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = colorResource(id = R.color.gray_s)
                            )
                        ){
                            Text(modifier = Modifier
                                .padding(start = 10.dp, top = 5.dp),
                                text = device.name,
                                color = colorResource(id = R.color.white)
                            )
                            Divider(thickness = 1.dp)
                            Text(modifier = Modifier
                                .padding(start = 10.dp, bottom = 5.dp),
                                fontSize = 8.sp,
                                text = device.address
                            )
                        }
                    }
                }

            }
        )
    }

}

private fun sendCommand(input: String){
    if (m_bluetoothSocket != null){
        try {
            m_bluetoothSocket!!.outputStream.write(input.toByteArray())
        }catch (e: IOException){
            e.printStackTrace()
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun DialogDevicePPreview(){
//    DialogDeviceP(show = true,
//        onConfirm = { },
//        context = LocalContext.current
//    )
//}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainScreenPreview() {
    MainScreen(context = LocalContext.current)
}