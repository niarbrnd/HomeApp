package io.github.domi04151309.home.activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import io.github.domi04151309.home.R
import io.github.domi04151309.home.adapters.IconSpinnerAdapter
import io.github.domi04151309.home.data.DeviceItem
import io.github.domi04151309.home.helpers.DeviceSecrets
import io.github.domi04151309.home.helpers.Devices
import io.github.domi04151309.home.helpers.Global

class EditDeviceActivity : BaseActivity() {
    companion object {
        private val SUPPORTS_DIRECT_VIEW =
            arrayOf(
                "ESP Easy",
                "Hue API",
                "Shelly Gen 1",
                "Shelly Gen 2",
                "SimpleHome API",
                "Tasmota",
            )
        private val HAS_CONFIG =
            arrayOf(
                "Hue API",
                "ESP Easy",
                "Node-RED",
                "Shelly Gen 1",
                "Shelly Gen 2",
            )
        private val HAS_INFO =
            arrayOf(
                "Hue API",
                "Shelly Gen 2",
            )
    }

    private lateinit var devices: Devices
    private lateinit var deviceId: String
    private lateinit var deviceSecrets: DeviceSecrets
    private lateinit var deviceIcon: ImageView
    private lateinit var nameText: TextView
    private lateinit var nameBox: TextInputLayout
    private lateinit var addressBox: TextInputLayout
    private lateinit var iconSpinner: AutoCompleteTextView
    private lateinit var modeSpinner: AutoCompleteTextView
    private lateinit var specialDivider: View
    private lateinit var specialSection: LinearLayout
    private lateinit var usernameBox: TextInputLayout
    private lateinit var passwordBox: TextInputLayout
    private lateinit var configHide: CheckBox
    private lateinit var configDirectView: CheckBox
    private lateinit var configButton: Button
    private lateinit var infoButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_device)

        devices = Devices(this)
        var deviceId = intent.getStringExtra("deviceId")
        val editing =
            if (deviceId == null) {
                deviceId = devices.generateNewId()
                false
            } else {
                true
            }
        this.deviceId = deviceId

        deviceSecrets = DeviceSecrets(this, deviceId)

        deviceIcon = findViewById(R.id.deviceIcn)
        nameText = findViewById(R.id.nameTxt)
        nameBox = findViewById(R.id.nameBox)
        addressBox = findViewById(R.id.addressBox)
        iconSpinner = findViewById<TextInputLayout>(R.id.iconSpinner).editText as AutoCompleteTextView
        modeSpinner = findViewById<TextInputLayout>(R.id.modeSpinner).editText as AutoCompleteTextView
        specialDivider = findViewById(R.id.specialDivider)
        specialSection = findViewById(R.id.specialSection)
        usernameBox = findViewById(R.id.usernameBox)
        passwordBox = findViewById(R.id.passwordBox)
        configHide = findViewById(R.id.configHide)
        configDirectView = findViewById(R.id.configDirectView)
        configButton = findViewById(R.id.configBtn)
        infoButton = findViewById(R.id.infoBtn)

        findViewById<TextView>(R.id.idTxt).text = (resources.getString(R.string.pref_add_id, deviceId))

        iconSpinner.addTextChangedListener(
            object : TextWatcher {
                override fun afterTextChanged(s: Editable) {
                    // Do nothing.
                }

                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int,
                ) {
                    // Do nothing.
                }

                override fun onTextChanged(
                    s: CharSequence,
                    start: Int,
                    before: Int,
                    count: Int,
                ) {
                    deviceIcon.setImageResource(Global.getIcon(s.toString()))
                }
            },
        )
        modeSpinner.addTextChangedListener(
            object : TextWatcher {
                override fun afterTextChanged(s: Editable) {
                    // Do nothing.
                }

                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int,
                ) {
                    // Do nothing.
                }

                override fun onTextChanged(
                    s: CharSequence,
                    start: Int,
                    before: Int,
                    count: Int,
                ) {
                    val string = s.toString()
                    val specialVisibility =
                        if (string == "Fritz! Auto-Login" || string == "Shelly Gen 1") {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }
                    val usernameVisibility = if (string == "Shelly Gen 1") View.VISIBLE else View.GONE
                    specialDivider.visibility = specialVisibility
                    specialSection.visibility = specialVisibility
                    usernameBox.visibility = usernameVisibility

                    if (SUPPORTS_DIRECT_VIEW.contains(string)) {
                        configDirectView.isEnabled = true
                    } else {
                        configDirectView.isEnabled = false
                        configDirectView.isChecked = false
                    }

                    if (editing) {
                        configButton.visibility =
                            if (HAS_CONFIG.contains(string)) {
                                View.VISIBLE
                            } else {
                                View.GONE
                            }
                        infoButton.visibility =
                            if (HAS_INFO.contains(string)) {
                                View.VISIBLE
                            } else {
                                View.GONE
                            }
                    }
                }
            },
        )
        nameBox.editText?.addTextChangedListener(
            object : TextWatcher {
                override fun afterTextChanged(s: Editable) {
                    // Do nothing.
                }

                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int,
                ) {
                    // Do nothing.
                }

                override fun onTextChanged(
                    s: CharSequence,
                    start: Int,
                    before: Int,
                    count: Int,
                ) {
                    val string = s.toString()
                    if (string == "") {
                        nameText.text = resources.getString(R.string.pref_add_name_empty)
                    } else {
                        nameText.text = string
                    }
                }
            },
        )

        if (editing) {
            onEditDevice()
        } else {
            onCreateDevice()
        }

        iconSpinner.setAdapter(IconSpinnerAdapter(resources.getStringArray(R.array.pref_icons)))
        modeSpinner.setAdapter(
            ArrayAdapter(
                this,
                R.layout.dropdown_item,
                resources.getStringArray(R.array.pref_add_mode_array),
            ),
        )

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            onFloatingActionButtonClicked()
        }
    }

    private fun onEditDevice() {
        title = resources.getString(R.string.pref_edit_device)
        val device = devices.getDeviceById(deviceId)
        nameBox.editText?.setText(device.name)
        addressBox.editText?.setText(device.address)
        iconSpinner.setText(device.iconName)
        modeSpinner.setText(device.mode)
        usernameBox.editText?.setText(deviceSecrets.username)
        passwordBox.editText?.setText(deviceSecrets.password)
        configHide.isChecked = device.hide
        configDirectView.isChecked = device.directView

        configButton.setOnClickListener {
            onConfigButtonClicked()
        }

        infoButton.setOnClickListener {
            startActivity(Intent(this, DeviceInfoActivity::class.java).putExtra("device", deviceId))
        }

        findViewById<Button>(R.id.shortcutBtn).setOnClickListener {
            onShortcutButtonClicked(device)
        }

        findViewById<Button>(R.id.deleteBtn).setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.str_delete)
                .setMessage(R.string.pref_delete_device_question)
                .setPositiveButton(R.string.str_delete) { _, _ ->
                    devices.deleteDevice(deviceId)
                    finish()
                }
                .setNegativeButton(android.R.string.cancel) { _, _ -> }
                .show()
        }
    }

    private fun onConfigButtonClicked() {
        when (modeSpinner.text.toString()) {
            "ESP Easy", "Shelly Gen 1", "Shelly Gen 2" -> {
                startActivity(
                    Intent(this, WebActivity::class.java)
                        .putExtra("URI", addressBox.editText?.text.toString())
                        .putExtra("title", resources.getString(R.string.pref_device_config)),
                )
            }
            "Node-RED" -> {
                startActivity(
                    Intent(this, WebActivity::class.java)
                        .putExtra("URI", formatNodeREDAddress(addressBox.editText?.text.toString()))
                        .putExtra("title", resources.getString(R.string.pref_device_config)),
                )
            }
            "Hue API" -> {
                val huePackageName = "com.philips.lighting.hue2"
                val launchIntent = packageManager.getLaunchIntentForPackage(huePackageName)
                if (launchIntent == null) {
                    try {
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=$huePackageName"),
                            ),
                        )
                    } catch (e: ActivityNotFoundException) {
                        Log.w(EditDeviceActivity::class.simpleName, e)
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store/apps/details?id=$huePackageName"),
                            ),
                        )
                    }
                } else {
                    startActivity(launchIntent)
                }
            }
        }
    }

    private fun onShortcutButtonClicked(device: DeviceItem) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val shortcutManager = this.getSystemService(ShortcutManager::class.java)
            if (shortcutManager != null) {
                if (shortcutManager.isRequestPinShortcutSupported) {
                    val shortcut =
                        ShortcutInfo.Builder(this, deviceId)
                            .setShortLabel(
                                device.name.ifEmpty {
                                    resources.getString(R.string.pref_add_name_empty)
                                },
                            )
                            .setLongLabel(
                                device.name.ifEmpty {
                                    resources.getString(R.string.pref_add_name_empty)
                                },
                            )
                            .setIcon(Icon.createWithResource(this, device.iconId))
                            .setIntent(
                                Intent(this, MainActivity::class.java)
                                    .putExtra("device", deviceId)
                                    .setAction(Intent.ACTION_MAIN)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK),
                            )
                            .build()
                    shortcutManager.requestPinShortcut(shortcut, null)
                } else {
                    Toast.makeText(this, R.string.pref_add_shortcut_failed, Toast.LENGTH_LONG).show()
                }
            }
        } else {
            Toast.makeText(this, R.string.pref_add_shortcut_failed, Toast.LENGTH_LONG).show()
        }
    }

    private fun onCreateDevice() {
        iconSpinner.setText(resources.getStringArray(R.array.pref_icons)[0])
        modeSpinner.setText(resources.getStringArray(R.array.pref_add_mode_array)[0])
        findViewById<View>(R.id.editDivider).visibility = View.GONE
        findViewById<LinearLayout>(R.id.editSection).visibility = View.GONE
    }

    private fun onFloatingActionButtonClicked() {
        val name = nameBox.editText?.text.toString()
        if (name == "") {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.err_missing_name)
                .setMessage(R.string.err_missing_name_summary)
                .setPositiveButton(android.R.string.ok) { _, _ -> }
                .show()
            return
        } else if (addressBox.editText?.text.toString() == "") {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.err_missing_address)
                .setMessage(R.string.err_missing_address_summary)
                .setPositiveButton(android.R.string.ok) { _, _ -> }
                .show()
            return
        }

        val tempAddress =
            if (modeSpinner.text.toString() == "Node-RED") {
                formatNodeREDAddress(addressBox.editText?.text.toString())
            } else {
                addressBox.editText?.text.toString()
            }

        val newItem = DeviceItem(deviceId)
        newItem.name = name
        newItem.address = tempAddress
        newItem.mode = modeSpinner.text.toString()
        newItem.iconName = iconSpinner.text.toString()
        newItem.hide = configHide.isChecked
        newItem.directView = configDirectView.isChecked
        devices.addDevice(newItem)
        deviceSecrets.username = usernameBox.editText?.text.toString()
        deviceSecrets.password = passwordBox.editText?.text.toString()
        deviceSecrets.updateDeviceSecrets()
        finish()
    }

    private fun formatNodeREDAddress(url: String): String {
        var result = url
        if (!result.contains(":1880")) {
            if (result.endsWith('/')) result = result.dropLast(1)
            result += ":1880/"
        }
        return result
    }
}
