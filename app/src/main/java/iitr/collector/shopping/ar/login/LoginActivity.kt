package iitr.collector.shopping.ar.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import iitr.collector.shopping.ar.HomeActivity
import iitr.collector.shopping.ar.R

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        private const val RC_SIGN_IN = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val name = findViewById<EditText>(R.id.nameEditText)
        val nameInputLayout = findViewById<TextInputLayout>(R.id.nameInputLayout)
        val header = findViewById<TextView>(R.id.textViewHeader)
        val loginButton = findViewById<TextView>(R.id.loginButton)
        val registerButton = findViewById<TextView>(R.id.registerButton)
        loginButton.setOnClickListener {
            if (nameInputLayout.visibility == View.GONE) {
                loginUser()
            } else {
                nameInputLayout.visibility = View.GONE
                loginButton.setBackgroundResource(R.drawable.bg_black_ripple)
                loginButton.setTextColor(ContextCompat.getColor(this, R.color.white))
                registerButton.setBackgroundResource(R.drawable.bg_white_ripple)
                registerButton.setTextColor(ContextCompat.getColor(this, R.color.black))
                header.text = "Sign In"
            }
        }
        registerButton.setOnClickListener {
            if (nameInputLayout.visibility == View.GONE) {
                nameInputLayout.visibility = View.VISIBLE
                registerButton.setBackgroundResource(R.drawable.bg_black_ripple)
                registerButton.setTextColor(ContextCompat.getColor(this, R.color.white))
                loginButton.setBackgroundResource(R.drawable.bg_white_ripple)
                loginButton.setTextColor(ContextCompat.getColor(this, R.color.black))
                header.text = "Sign Up"
            } else {
                registerUser()
            }
        }
        findViewById<TextView>(R.id.googleSignInButton).setOnClickListener { signInWithGoogle() }
    }

    private fun loginUser() {
        val email = findViewById<EditText>(R.id.emailEditText).text.toString().trim()
        val password = findViewById<EditText>(R.id.passwordEditText).text.toString().trim()

        if (email.isNotEmpty() && password.isNotEmpty()) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success
                        Log.d("LoginActivity", "signInWithEmail:success")
                        val user = auth.currentUser
                        updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("LoginActivity", "signInWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT)
                            .show()
                        updateUI(null)
                    }
                }
        } else {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
        }
    }

    private fun registerUser() {
        val name = findViewById<EditText>(R.id.nameEditText).text.toString().trim()
        val email = findViewById<EditText>(R.id.emailEditText).text.toString().trim()
        val password = findViewById<EditText>(R.id.passwordEditText).text.toString().trim()

        if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty()) {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success
                        Log.d("LoginActivity", "createUserWithEmail:success")
                        val user = auth.currentUser
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName("New Name")
                            .build()
                        user?.updateProfile(profileUpdates)
                        updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("LoginActivity", "createUserWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT)
                            .show()
                        updateUI(null)
                    }
                }
        } else {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    firebaseAuthWithGoogle(account)
                } else {
                    Log.w("LoginActivity", "Google sign in failed, account is null")
                    updateUI(null)
                }
            } catch (e: ApiException) {
                Log.w("LoginActivity", "Google sign in failed", e)
                updateUI(null)
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d("LoginActivity", "firebaseAuthWithGoogle:" + acct.id)
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("LoginActivity", "signInWithCredential:success")
                    val user = auth.currentUser
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(acct.displayName)
                        .build()

                    user?.updateProfile(profileUpdates)?.addOnCompleteListener { updateTask ->
                        if (updateTask.isSuccessful) {
                            saveUserDetailsToFirestore(user)
                            Log.d("LoginActivity", "User profile updated with Google display name")
                        }
                        updateUI(user)
                    }
                } else {
                    Log.w("LoginActivity", "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }



    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            saveUserDetailsToFirestore(user)
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }

    private fun saveUserDetailsToFirestore(user: FirebaseUser) {
        val db = FirebaseFirestore.getInstance()
        val userDetails = hashMapOf(
            "uid" to user.uid,
            "name" to user.displayName,
            "email" to user.email
        )

        db.collection("users").document(user.uid)
            .set(userDetails)
            .addOnSuccessListener {
                Log.d("LoginActivity", "User details successfully written to Firestore")
            }
            .addOnFailureListener { e ->
                Log.w("LoginActivity", "Error writing user details to Firestore", e)
            }
    }
}
