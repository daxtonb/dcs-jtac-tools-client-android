package com.daxtonb.dcsjtactoolsclient

import MainViewModel
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import okhttp3.OkHttpClient

class MainActivity : AppCompatActivity() {

    private var client = OkHttpClient()
    private lateinit var viewModel: MainViewModel

    private lateinit var webSocketStatusIcon: ImageView
    private lateinit var unitNameSpinner: Spinner
    private lateinit var unitNameAdapter: ArrayAdapter<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        webSocketStatusIcon = findViewById(R.id.webSocketStatusIcon)
        unitNameSpinner = findViewById(R.id.unitNameSpinner)
        unitNameAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, mutableListOf<String>())
        unitNameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        unitNameSpinner.adapter = unitNameAdapter

        val protocolSpinner: Spinner = findViewById(R.id.protocolSpinner)
        val serverAddressInput: EditText = findViewById(R.id.serverAddressInput)
        val portInput: EditText = findViewById(R.id.portInput)
        val connectSwitch: Switch = findViewById(R.id.connectSwitch)

        setupViewModel()
        setupProtocolSpinner(protocolSpinner)
        handleServerToggled(connectSwitch, protocolSpinner, serverAddressInput, portInput)
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this, MainViewModelFactory(NetworkRepository(client))).get(
            MainViewModel::class.java
        )

        // Observe ViewModel LiveData
        viewModel.unitNames.observe(this, Observer { unitNames ->
            // Remember the currently selected item
            val selectedItem = unitNameSpinner.selectedItem as? String

            // Update the adapter with the new list
            unitNameAdapter.clear()
            unitNameAdapter.addAll(unitNames)
            unitNameAdapter.notifyDataSetChanged()

            // Restore the selection
            selectedItem?.let {
                val position = unitNameAdapter.getPosition(it)
                if (position >= 0) {
                    unitNameSpinner.setSelection(position, false)
                }
            }
        })

        viewModel.webSocketStatusIcon.observe(this, Observer { iconResourceId ->
            webSocketStatusIcon.setImageResource(iconResourceId)
        })

        unitNameSpinner.setOnItemSelectedListener(onItemSelected = { parent, view, position, id ->
            val selectedUnitName = parent?.getItemAtPosition(position).toString()
            viewModel.setSelectedUnitName(selectedUnitName)
        } )
    }

    private fun handleServerToggled(
        connectSwitch: Switch,
        protocolSpinner: Spinner,
        serverAddressInput: EditText,
        portInput: EditText
    ) {
        connectSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Extract connection details from UI, construct the full address and connect
                val protocol = protocolSpinner.selectedItem.toString()
                val serverAddress = serverAddressInput.text.toString()
                val port = portInput.text.toString()
                val fullAddress = "$protocol$serverAddress:$port"
                viewModel.connect(fullAddress, LocationMocker(this))
            } else {
                // Handle disconnection logic
                viewModel.disconnect()
            }
        }
    }

    private fun setupProtocolSpinner(protocolSpinner: Spinner) {
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
        client.dispatcher.executorService.shutdown()
        viewModel.disconnect()
    }
}
