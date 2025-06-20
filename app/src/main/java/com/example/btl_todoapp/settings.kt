package com.example.btl_todoapp

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import java.util.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class settings : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var settingsStore: SettingsStore
    private val PREF_NAME = "TodoAppPrefs"
    private val KEY_LANGUAGE = "language"
    private val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        // Decorator pattern: Gói SharedPreferences vào store + decorator
        val sharedPreferences = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        settingsStore = LoggingSettingsStore(SharedPreferencesStore(sharedPreferences))

        val spinner: Spinner = view.findViewById(R.id.spinner)
        val switchNotifications: Switch = view.findViewById(R.id.switchNotifications)

        val languages = arrayOf("English", "Tiếng Việt")
        val languageCodes = arrayOf("en", "vi")

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        val savedLanguage = settingsStore.getString(KEY_LANGUAGE, "en")
        val selectedPosition = languageCodes.indexOf(savedLanguage)
        if (selectedPosition != -1) spinner.setSelection(selectedPosition)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                val selectedLanguage = languageCodes[position]
                if (selectedLanguage != savedLanguage) {
                    setLocale(selectedLanguage)
                    settingsStore.saveString(KEY_LANGUAGE, selectedLanguage)
                    requireActivity().recreate()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val notificationsEnabled = settingsStore.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
        switchNotifications.isChecked = notificationsEnabled
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            settingsStore.saveBoolean(KEY_NOTIFICATIONS_ENABLED, isChecked)
        }

        return view
    }

    private fun setLocale(language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            settings().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
