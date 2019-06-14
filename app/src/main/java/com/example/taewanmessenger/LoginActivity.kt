package com.example.taewanmessenger

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.taewanmessenger.Utils.FirestoreUtil
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.*
import org.json.JSONObject
import java.util.*

class LoginActivity : AppCompatActivity() {

    private val TAG = "TAG_LoginActivity"
    private val providers = arrayListOf(
        AuthUI.IdpConfig.EmailBuilder()
            .setRequireName(true)
            .setAllowNewAccounts(true)
            .build())
    private val RC_EMAIL_SIGN_IN = 1001
    private val RC_GOOGLE_SIGN_IN = 1002
    var callbackManager : CallbackManager? = null
    private val auth : FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailLogin_btn_login.setOnClickListener {
            startActivityForResult(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .setLogo(R.drawable.messenger_logo)
                    .setTheme(R.style.otherTheme)
                    .build(),
                RC_EMAIL_SIGN_IN
            )
        }
        googleLogin_btn_login.setOnClickListener {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
            val signInIntent: Intent = mGoogleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN)
        }
        facebookLogin_btn_login.setOnClickListener {
            val accessToken = AccessToken.getCurrentAccessToken()
            val isLoggedIn = accessToken != null && !accessToken.isExpired
            LoginManager.getInstance().logInWithReadPermissions(this@LoginActivity,
                Arrays.asList("email", "public_profile"))
            facebookLoginFunc()
        }
    }
    private fun facebookLoginFunc() {
        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult?) {
//                requestMe(result!!.getAccessToken());
                Log.d(TAG, "페이스북 로그인 성공")
                handleFacebookAccessToken(result!!.accessToken)
            }


            override fun onCancel() {
                Log.d(TAG, "페이스북 로그인 실패")
            }

            override fun onError(error: FacebookException?) {
                Log.d(TAG, "페이스북 로그인 에러  = ${error?.message}")
            }
            private fun handleFacebookAccessToken(token : AccessToken) {
                Log.d(TAG, "handleFacebookAccessToken : $token")

                val credential = FacebookAuthProvider.getCredential(token.token)
                auth.signInWithCredential(credential).addOnCompleteListener {
                    if(it.isSuccessful){
                        Log.d(TAG, "signInWithCredential이 성공적")
                        val user = auth.currentUser
                        FirestoreUtil.firstFacebookLoginUser(user){
                            startActivity(intentFor<MainActivity>().newTask().clearTask())
                        }
                    }
                    else{
                        Log.w(TAG, "signInWithCredential이 실패 = ", it.exception)
                        toast("페이스북 인증에 실패했습니다.")
                    }
                }
            }
            fun requestMe(token : AccessToken){
                val graphRequest = GraphRequest.newMeRequest(token, object : GraphRequest.GraphJSONObjectCallback{
                    override fun onCompleted(`object`: JSONObject?, response: GraphResponse?) {
                        Log.d(TAG, "토큰요청 성공의 결과물 = ${`object`.toString()}")
                    }
                })
                val parameters = Bundle()
                parameters.putString("fields",  "id,name,email,gender,birthday")
                graphRequest.setParameters(parameters);
                graphRequest.executeAsync();
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //이메일 로그인
        if (requestCode == RC_EMAIL_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                val progressDialog = indeterminateProgressDialog("회원정보 확인중...")
                progressDialog.setCancelable(false)
                FirestoreUtil.firstEmailLoginUser {
                    startActivity(intentFor<MainActivity>().newTask().clearTask())
                    progressDialog.dismiss()
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                if (response == null) return
                when (response.error?.errorCode) {
                    ErrorCodes.NO_NETWORK -> toast("와이파이나 데이터를 연결해주세요.")
                    ErrorCodes.UNKNOWN_ERROR -> toast("알 수 없는 에러입니다.")
                }
            } else {
                Log.d("LoginActivity", "결과 요청코드 에러")
            }
        }
        //구글 로그인
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                Log.d(TAG, "구글에서 받은 계정정보 = ${account.toString()}")
                val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
                val progressDialog = indeterminateProgressDialog("회원정보 확인중...")
                FirebaseAuth.getInstance().signInWithCredential(credential)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "계정정보 받은 후 signInWithCredential:성공")
                            val progressDialog = indeterminateProgressDialog("로딩중...")
                            progressDialog.setCancelable(false)

                            FirestoreUtil.firstGoogleLoginUser(account!!) {
                                startActivity(intentFor<MainActivity>().newTask().clearTask())
                                progressDialog.dismiss()
                            }
                        } else {
                            Log.w(TAG, "계정 정보 받은 후 signInWithCredential:실패 = ", task.exception)
                            progressDialog.dismiss()
                        }
                    }
            } catch (e: ApiException) {
                Log.w(TAG, "onActivityResult에서 계정 받는데 실패사유 = ", e)
            }
        }
        //페이스북 로그인
        callbackManager?.onActivityResult(requestCode, resultCode, data)
    }
}
