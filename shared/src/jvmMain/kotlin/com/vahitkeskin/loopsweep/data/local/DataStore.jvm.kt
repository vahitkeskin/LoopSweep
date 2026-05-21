package com.vahitkeskin.loopsweep.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath
import java.io.File

actual fun createDataStore(): DataStore<Preferences> {
    val userHome = System.getProperty("user.home") ?: "."
    val path = File(userHome, ".loopsweep_prefs.preferences_pb").absolutePath
    return PreferenceDataStoreFactory.createWithPath(
        produceFile = { path.toPath() }
    )
}
