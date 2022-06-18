package com.example.socialconnect

import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.socialconnect.daos.UserDao
import com.example.socialconnect.models.User
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignInActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var progressBar: ProgressBar
    private lateinit var linearLayout: LinearLayout
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private val REQ_ONE_TAP = 100
    private lateinit var waitText: TextView

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQ_ONE_TAP -> {
                try {
                    val credential = oneTapClient.getSignInCredentialFromIntent(data)
                    val idToken = credential.googleIdToken
                    when {
                        idToken != null -> {
                            // Got an ID token from Google. Use it to authenticate
                            // with your backend.
                            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                            auth.signInWithCredential(firebaseCredential)
                                .addOnCompleteListener(this) { task ->
                                    if (task.isSuccessful) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Toast.makeText(this,"signInWithCredential:success",Toast.LENGTH_SHORT).show()
                                        val user = auth.currentUser
                                        updateUI(user)
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Toast.makeText(this,"signInWithCredential:failure",Toast.LENGTH_SHORT).show()
                                        updateUI(null)
                                    }
                                }
                        }
                        else -> {
                            // Shouldn't happen.
                            Toast.makeText(this,"signInWithCredential:failure",Toast.LENGTH_SHORT).show()
                            updateUI(null)
                        }
                    }
                } catch (e: ApiException) {
                    // ...
                    Toast.makeText(this,"signInWithCredential:failure",Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        auth = Firebase.auth

        linearLayout = findViewById(R.id.vericalLayout)
        progressBar = findViewById(R.id.progressBar)
        waitText = findViewById(R.id.waitText)

        oneTapClient = Identity.getSignInClient(this)

        findViewById<SignInButton>(R.id.googleSignInBtn).setOnClickListener {
            signIn()
        }
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            updateUI(currentUser)
        }
    }

    private fun signIn(){
        //visibility
        progressBar.visibility=View.VISIBLE
        linearLayout.visibility=View.GONE
        waitText.text = "Signing In"
        waitText.visibility=View.VISIBLE

        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Your server's client ID, not your Android client ID.
                    .setServerClientId(getString(R.string.your_web_client_id))
                    // Only show accounts previously used to sign in.
                    .setFilterByAuthorizedAccounts(true)
                    .build())
            // Automatically sign in when exactly one credential is retrieved.
            .setAutoSelectEnabled(true)
            .build()

        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener(this) { result ->
                try {
                    startIntentSenderForResult(
                        result.pendingIntent.intentSender, REQ_ONE_TAP,
                        null, 0, 0, 0, null)
                } catch (e: IntentSender.SendIntentException) {
                    Toast.makeText(this,"Something went wrong",Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
            .addOnFailureListener(this) { e ->
                // No saved credentials found. Launch the One Tap sign-up flow, or
                // do nothing and continue presenting the signed-out UI.
                signUp()
            }
    }

    private fun signUp(){
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Your server's client ID, not your Android client ID.
                    .setServerClientId(getString(R.string.your_web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build())
            // Automatically sign in when exactly one credential is retrieved.
            .setAutoSelectEnabled(true)
            .build()

        waitText.text="Signing Up"

        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener(this) { result ->
                try {
                    startIntentSenderForResult(
                        result.pendingIntent.intentSender, REQ_ONE_TAP,
                        null, 0, 0, 0, null)
                } catch (e: IntentSender.SendIntentException) {
                    Toast.makeText(this,"Something went wrong",Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
            .addOnFailureListener(this) { e ->
                Toast.makeText(this,"Something went wrong",Toast.LENGTH_SHORT).show()
                updateUI(null)
            }
    }

    private fun updateUI(firebaseUser: FirebaseUser?) {
        if(firebaseUser != null){
            UserDao().addUser(User(firebaseUser.uid,firebaseUser.displayName,firebaseUser.photoUrl.toString()))
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        else{
            progressBar.visibility = View.GONE
            linearLayout.visibility=View.VISIBLE
            waitText.visibility = View.GONE
        }
    }
}