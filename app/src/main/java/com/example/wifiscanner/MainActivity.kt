package com.example.wifiscanner

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.calculateSignalLevel
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQ_CODE = 123
        private const val TAG = "WIFI_TAG"
    }

    // экземпляр класса для выдачи разрешений.
    // lateinit показывает, что свойство будет определено в дальнейшем
    private lateinit var managePermissions: ManagePermissions

    // list для хранения результатов сканирования WiFi
    private var resultList = ArrayList<ScanResult>()

    // переменная для доступа к информации о WiFi
    private lateinit var wifiManager: WifiManager

    // приёмник широковещательных сообщений для информации о WiFi точках
    private val wifiReceiver = object : BroadcastReceiver() {
        // единственная необходимая функция в классе
        // она вызывается, если приходит какое-либо сообщения
        override fun onReceive(context: Context, intent: Intent) {
            // освобождаем приёмник, чтобы не расходовать ресурсы
            unregisterReceiver(this)
            // через интент проверяем, что сканирование было успешным
            val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
            // обрабатываем результаты сканирования
            if (success) {
                scanSuccess()
                Log.d(TAG, "scan successful")
            }
            else {
                scanFailure()
                Log.d(TAG, "scan failed")
            }

        }
    }


    fun scanSuccess() {
        resultList.clear()
        // заполняем массив новыми WiFi точками
        resultList = wifiManager.scanResults as ArrayList<ScanResult>
        // обновляем результаты в ListView
        //uploadListView()
        // только для отладки
        for (result in resultList) {
            Log.d(TAG, "SSID: ${result.SSID}")
            Log.d(TAG, "BSSID: ${result.BSSID}")
            Log.d(TAG, "level: ${calculateSignalLevel(result.level)}")
            Log.d(TAG, "frequency: ${result.frequency} hHz")
            Log.d(TAG, "capabilities: ${result.capabilities}")
        }
    }

    private fun scanFailure() {}

    fun calculateSignalLevel(level: Int) = when {
        level > -50 -> "Excellent"
        level in -60..-50 -> "Good"
        level in -70..-60 -> "Fair"
        level < -70 -> "Weak"
        else -> "No signal"
    }

    // обработка результата запроса привелегий
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        when(requestCode) {
            REQ_CODE -> {
                val isPermissionsGranted = managePermissions
                    .processPermissionsResult(grantResults)

                if(isPermissionsGranted){
                    Toast.makeText(this, "Permissions granted.", Toast.LENGTH_LONG).show()
                }
                else {
                    Toast.makeText(this, "Permissions denied.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun initPermissions() {
        // задаём необходимые привелегии
        val permissionsList = listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        managePermissions = ManagePermissions(this, permissionsList, REQ_CODE)
        // проверяем, получены ли привелегии
        managePermissions.checkPermissions()
    }

    private fun checkWifi() {
        if (!wifiManager.isWifiEnabled) {
            Toast.makeText(this, "WiFi is disabled. Enabling WiFi...", Toast.LENGTH_LONG).show()
            wifiManager.isWifiEnabled = true
        }
    }

    private fun scanWifi() {
        registerReceiver(wifiReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
        wifiManager.startScan()
        Toast.makeText(this, "Scanning WiFi ...", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // вызывает метод onCreate() для activity
        super.onCreate(savedInstanceState)

        // устанавливает layout для отображения
        setContentView(R.layout.activity_main)

        // Запрос разрешений
        initPermissions()
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        scan_btn.setOnClickListener {
            // проверяем, включен ли Wi-Fi, включаем, если не был включен
            checkWifi()
            // начинаем сканирование ближайших точек
            scanWifi()
        }

        // Список из элементов SSID
        resultList = wifiManager.scanResults as ArrayList<ScanResult>
        val resultSSIDs = arrayListOf<String>()
        for (result in resultList) {
            if (result.SSID != "") {
                resultSSIDs.add(result.SSID)
            } else {
                resultSSIDs.add("hidden SSID")
            }
        }
        // изменяемый список из 5 элементов через lambda-функцию
        //val listData = MutableList(5, {x -> "${x+1}"})
        var allStrExludeSSID = arrayListOf<String>()
        for (result in resultList) {
            allStrExludeSSID.add(
                        "SSID: ${result.SSID}\n" +
                        "BSSID: ${result.BSSID}\n" +
                        "level: ${calculateSignalLevel(result.level)}\n" +
                        "frequency: ${result.frequency} hHz\n" +
                        "capabilities: ${result.capabilities}"
            )
        }
        // создаём массив адаптеров, элементами которого будут TextView
        // с текстом из listData
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, resultSSIDs)

        // связываем элемент ListView с id=main_list с адаптером
        main_list.adapter = adapter
        // задаём функцию-обработчик при нажатие на элемент через lambda-функцию
        main_list.setOnItemClickListener { parent, view, position, id ->
            // создаём intent в контексте MainActivity для вызова ListItemActivity
            val intent = Intent(this, ListItemActivity::class.java)
            // передаём содержимое элемента TextView
            intent.putExtra("text",  allStrExludeSSID[position]/*(view as TextView).text*/)
            // вызываем ListItemActivity
            startActivity(intent)
        }

    }
}