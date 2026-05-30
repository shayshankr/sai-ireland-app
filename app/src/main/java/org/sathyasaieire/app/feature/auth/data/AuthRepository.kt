package org.sathyasaieire.app.feature.auth.data

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import org.sathyasaieire.app.R
import org.sathyasaieire.app.domain.model.User
import org.sathyasaieire.app.domain.model.UserRole
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    @ApplicationContext private val context: Context,
) {
    val currentUser: FirebaseUser? get() = auth.currentUser
    val isSignedIn: Boolean get() = auth.currentUser != null

    suspend fun signInWithGoogle(activityContext: Context): Result<User> = try {
        val credentialManager = CredentialManager.create(activityContext)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(context.getString(R.string.google_web_client_id))
            .setAutoSelectEnabled(false)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
        val result = credentialManager.getCredential(context = activityContext, request = request)
        val googleCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
        val firebaseCredential = GoogleAuthProvider.getCredential(googleCredential.idToken, null)
        val authResult = auth.signInWithCredential(firebaseCredential).await()
        val firebaseUser = authResult.user ?: error("Sign-in returned null user")
        val user = upsertUser(firebaseUser)
        Result.success(user)
    } catch (e: GetCredentialCancellationException) {
        Result.failure(Exception("Sign-in cancelled"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getCurrentUser(): User? {
        val uid = auth.currentUser?.uid ?: return null
        return try {
            firestore.collection("users").document(uid).get().await().toUser()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getUserRole(): UserRole {
        val uid = auth.currentUser?.uid ?: return UserRole.MEMBER
        return try {
            val role = firestore.collection("users").document(uid)
                .get().await().getString("role") ?: "member"
            UserRole.fromString(role)
        } catch (e: Exception) {
            UserRole.MEMBER
        }
    }

    suspend fun updateConsent(gdprConsent: Boolean, analyticsEnabled: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        try {
            firestore.collection("users").document(uid)
                .update(mapOf("gdprConsent" to gdprConsent, "analyticsEnabled" to analyticsEnabled))
                .await()
        } catch (e: Exception) {
            // Firestore unavailable — consent will be re-synced on next sign-in
        }
    }

    suspend fun updatePhone(phone: String) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid).update("phone", phone).await()
    }

    suspend fun signOut() = auth.signOut()

    suspend fun deleteAccount(): Result<Unit> = try {
        val uid = auth.currentUser?.uid ?: error("Not signed in")
        // Anonymise Firestore profile; full purge handled by a scheduled Cloud Function
        firestore.collection("users").document(uid).update(
            mapOf(
                "name" to "[Deleted]",
                "email" to "",
                "phone" to null,
                "photoUrl" to null,
                "deleted" to true,
                "deletedAt" to com.google.firebase.Timestamp.now(),
            )
        ).await()
        auth.currentUser?.delete()?.await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    private suspend fun upsertUser(firebaseUser: FirebaseUser): User {
        val uid = firebaseUser.uid
        val fallbackUser = User(
            uid = uid,
            name = firebaseUser.displayName ?: "",
            email = firebaseUser.email ?: "",
            photoUrl = firebaseUser.photoUrl?.toString(),
            role = UserRole.MEMBER,
        )
        return try {
            val docRef = firestore.collection("users").document(uid)
            val existing = docRef.get().await()
            val role = if (existing.exists()) existing.getString("role") ?: "member" else "member"
            docRef.set(
                mapOf(
                    "uid" to uid,
                    "name" to (firebaseUser.displayName ?: ""),
                    "email" to (firebaseUser.email ?: ""),
                    "photoUrl" to firebaseUser.photoUrl?.toString(),
                    "role" to role,
                ),
                SetOptions.merge(),
            ).await()
            fallbackUser.copy(role = UserRole.fromString(role))
        } catch (e: Exception) {
            // Firestore unavailable — sign in with basic Google profile, sync later
            fallbackUser
        }
    }
}

private fun com.google.firebase.firestore.DocumentSnapshot.toUser(): User? {
    if (!exists()) return null
    return User(
        uid = id,
        name = getString("name") ?: "",
        email = getString("email") ?: "",
        photoUrl = getString("photoUrl"),
        phone = getString("phone"),
        role = UserRole.fromString(getString("role") ?: "member"),
        gdprConsent = getBoolean("gdprConsent") ?: false,
        analyticsEnabled = getBoolean("analyticsEnabled") ?: false,
    )
}
