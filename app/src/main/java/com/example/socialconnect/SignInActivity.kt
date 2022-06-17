package com.example.socialconnect

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.socialconnect.daos.UserDao
import com.example.socialconnect.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignInActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var email: String
    private lateinit var password: String
    private lateinit var emailText: EditText
    private lateinit var passwordText: EditText
    private lateinit var btn: Button
    private lateinit var tv1: TextView
    private lateinit var tv2: TextView
    private lateinit var tv3: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var linearLayout: LinearLayout
    private var accountExist: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        auth = Firebase.auth

        emailText=findViewById(R.id.EmailAddress)
        passwordText=findViewById(R.id.TextPassword)
        btn = findViewById(R.id.button)
        tv1=findViewById(R.id.tv1)
        tv2=findViewById(R.id.tv2)
        tv3=findViewById(R.id.tv3)
        progressBar=findViewById(R.id.progressBar)
        linearLayout=findViewById(R.id.linearLayout)

        signInOrUpUI()

        tv3.setOnClickListener {
            signInOrUpUI()
        }

        btn.setOnClickListener {
            email = emailText.text.toString()
            password = passwordText.text.toString()
            progressBar.visibility=View.VISIBLE
            linearLayout.visibility=View.GONE
            if(accountExist)
                signIn()
            else
                signUp()
        }
    }

    private fun signInOrUpUI(){
        val s1:String
        val s2:String
        val s3:String
        if(accountExist)
        {
            accountExist=false
            s1="Sign Up"
            s2="If you have an account"
            s3= " sign in"
        }
        else
        {
            accountExist=true
            s1="Sign In"
            s2="If you don't have an account"
            s3= " sign up"
        }
        tv1.text = s1
        tv2.text = s2
        tv3.text = s3
        btn.text = s1
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
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }

    private fun signUp(){
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }

    private fun updateUI(firebaseUser: FirebaseUser?) {
        if(firebaseUser != null){
            val userDao = UserDao()
            if(firebaseUser.photoUrl.toString()!=""){
                userDao.addUser(User(firebaseUser.uid,firebaseUser.displayName,firebaseUser.photoUrl.toString()))
            }
            else{
                userDao.addUser(User(firebaseUser.uid,firebaseUser.displayName,"https://firebasestorage.googleapis.com/v0/b/social-connect-f289d." +
                        "appspot.com/o/images%2FemptyUserImg.png?alt=media&token=8eb3221d-4ef7-4a51-8efb-da9f5114e31f"))
            }

            if(firebaseUser.displayName==""){
                val intent = Intent(this,EditProfileActivity::class.java)
                startActivity(intent)
                finish()
            }
            else{
                val intent = Intent(this,MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        else{
            progressBar.visibility = View.GONE
            linearLayout.visibility=View.VISIBLE
        }
    }
}