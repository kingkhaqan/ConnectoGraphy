package com.example.cgprototype

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.facebook.*
//import com.facebook.*

import com.facebook.appevents.AppEventsLogger;


import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.type.LatLng
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {

    lateinit var googleSignInClient: GoogleSignInClient
    lateinit var callbackManager: CallbackManager
    lateinit var auth: FirebaseAuth
    val RC_SIGN_IN = 1001

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        FacebookSdk.sdkInitialize(applicationContext);
//        AppEventsLogger.activateApp(this)
        setContentView(R.layout.activity_login)

//        requestPermissions(this)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        callbackManager = CallbackManager.Factory.create()
        auth = FirebaseAuth.getInstance()

        google.setOnClickListener {
//            startActivity(Intent(this, HomeActivity::class.java))

            val signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        }

//        facebook.setOnClickListener {
//            startActivity(Intent(this, HomeActivity::class.java))
//        }

        facebook.setReadPermissions("email", "public_profile")
        facebook.registerCallback(callbackManager, object :
            FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {

//                Toast.makeText(this@LoginActivity, "something else" , Toast.LENGTH_SHORT).show()
             handleFacebookAccessToken(loginResult.accessToken)





            }

            override fun onCancel() {

            }

            override fun onError(error: FacebookException) {
                Toast.makeText(this@LoginActivity, error.message , Toast.LENGTH_LONG).show()

            }
        })


    }

    private fun handleFacebookAccessToken(token: AccessToken) {
//        Log.d(TAG, "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
//                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser

                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
//                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(baseContext, token.dataAccessExpirationTime.toString(),
                        Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }

                // ...
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)




        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {

//                Toast.makeText(this, e.message , Toast.LENGTH_LONG).show()
                // Google Sign In failed, update UI appropriately
//                Log.w(TAG, "Google sign in failed", e)
                // ...
            }
        }
        else
            callbackManager.onActivityResult(requestCode, resultCode, data)


    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
//        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.id!!)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
//                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
//                    Log.w(TAG, "signInWithCredential:failure", task.exception)
//                    Snackbar.make(main_layout, "Authentication Failed.", Snackbar.LENGTH_SHORT).show()
                    updateUI(null)
                }

                // ...
            }
    }

    data class MUser(val name: String? = null, val email: String? = null, val photoUrl: String? = null, val lat: Double? = null, val lng: Double? = null)
    private fun updateUI(user: FirebaseUser?) {


        if (user != null){
            progress_bar.visibility = View.VISIBLE
            val muser = Profile(user.uid,user.displayName, user.email, user.photoUrl.toString())
            val db = FirebaseFirestore.getInstance()
            db.collection(COLLECTION_PROFILE)
                .whereEqualTo(DATA_KEY_UID, user.uid)
                .get()
                .addOnSuccessListener {
                    if (it.documents.size==0)
                        db.collection(COLLECTION_PROFILE)
                            .add(muser)
                }



            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
//            Toast.makeText(this, "${user.displayName}, ${user.email}, ${user.phoneNumber}, ${user.photoUrl}", Toast.LENGTH_SHORT).show()



    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == EXTRA_PERMISSIONS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "granted", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
