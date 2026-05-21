package com.vahitkeskin.loopsweep.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.vahitkeskin.loopsweep.utils.AndroidContextProvider
import okio.Path.Companion.toPath

actual fun createDataStore(): DataStore<Preferences> {
    return PreferenceDataStoreFactory.createWithPath(
        produceFile = {
            AndroidContextProvider.context.filesDir.resolve("loopsweep_prefs.preferences_pb").absolutePath.toPath()
        }
    )
}
