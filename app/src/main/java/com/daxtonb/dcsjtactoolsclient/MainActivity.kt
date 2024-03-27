package com.daxtonb.dcsjtactoolsclient

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer

class MainActivity : AppCompatActivity() {
    private var isBound: Boolean = false
    private var hubService: DcsJtacHubService? = null

    private lateinit var _webSocketStatusIcon: ImageView
    private lateinit var _unitNameSpinner: Spinner
    private lateinit var _unitNameAdapter: ArrayAdapter<String>
    private lateinit var _intent: Intent

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as DcsJtacHubService.LocalBinder
            hubService = binder.getService()
            isBound = true

            setupListeners()
            setupProtocolSpinner()
            handleServerToggled()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        _intent = Intent(this, DcsJtacHubService::class.java).also { intent ->
            startForegroundService(intent)
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }

        // Initialize views
        _webSocketStatusIcon = findViewById(R.id.webSocketStatusIcon)
        _unitNameSpinner = findViewById(R.id.unitNameSpinner)
        _unitNameAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, mutableListOf<String>())
        _unitNameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        _unitNameSpinner.adapter = _unitNameAdapter
    }

    private fun setupListeners() {
        // Observe live unit names data
        hubService?.unitNames?.observe(this, Observer { unitNames ->
            // Remember the currently selected item
            val selectedItem = _unitNameSpinner.selectedItem as? String

            // Update the adapter with the new list
            _unitNameAdapter.clear()
            _unitNameAdapter.addAll(unitNames)
            _unitNameAdapter.notifyDataSetChanged()

            // Restore the selection
            selectedItem?.let {
                val position = _unitNameAdapter.getPosition(it)
                if (position >= 0) {
                    _unitNameSpinner.setSelection(position, false)
                }
            }
        })

        hubService?.webSocketStatus?.observe(this, Observer { iconResourceId ->
            _webSocketStatusIcon.setImageResource(iconResourceId)
        })

        _unitNameSpinner.setOnItemSelectedListener(onItemSelected = { parent, view, position, id ->
            val selectedUnitName = parent?.getItemAtPosition(position).toString()
            hubService?.setSelectedUnit(selectedUnitName)
        } )
    }

    private fun handleServerToggled() {
        val serverAddressInput: EditText = findViewById(R.id.serverAddressInput)
        val portInput: EditText = findViewById(R.id.portInput)
        val protocolSpinner: Spinner = findViewById(R.id.protocolSpinner)
        val connectSwitch: Switch = findViewById(R.id.connectSwitch)

        connectSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Extract connection details from UI, construct the full address and connect
                val protocol = protocolSpinner.selectedItem.toString()
                val serverAddress = serverAddressInput.text.toString()
                val port = portInput.text.toString()
                val fullAddress = "$protocol$serverAddress:$port"
                hubService?.connectToHub(fullAddress)
            } else {
                // Handle disconnection logic
                hubService?.disconnectFromHub()
            }
        }
    }

    private fun setupProtocolSpinner() {
        val protocolSpinner: Spinner = findViewById(R.id.protocolSpinner)
        ArrayAdapter.createFromResource(
            this,
            R.array.protocol_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            protocolSpinner.adapter = adapter
        }
    }

    private fun Spinner.setOnItemSelectedListener(
        onItemSelected: (parent: AdapterView<*>?, view: View?, position: Int, id: Long) -> Unit,
        onNothingSelected: (parent: AdapterView<*>?) -> Unit = {}
    ) {
        this.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                onItemSelected(parent, view, position, id)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                onNothingSelected(parent)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        hubService?.disconnectFromHub()
        stopService(_intent)
    }
}
