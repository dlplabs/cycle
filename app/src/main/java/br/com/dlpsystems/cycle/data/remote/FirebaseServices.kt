package br.com.dlpsystems.cycle.data.remote

import android.content.Context
import br.com.dlpsystems.cycle.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseServices @Inject constructor(
    @ApplicationContext context: Context,
) {
    val available: Boolean = BuildConfig.FIREBASE_CONFIGURED && initialize(context)

    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance().also { database ->
            database.firestoreSettings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
                .build()
        }
    }

    private fun initialize(context: Context): Boolean =
        FirebaseApp.initializeApp(context) != null || FirebaseApp.getApps(context).isNotEmpty()
}
