package com.example.taewanmessenger

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.taewanmessenger.Utils.FirestoreUtil
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.*

class LoginActivity : AppCompatActivity() {

    private val TAG = "TAG_LoginActivity"
    private val providers = arrayListOf(
        AuthUI.IdpConfig.EmailBuilder()
            .setRequireName(true)
            .setAllowNewAccounts(true)
            .build())
    private val RC_EMAIL_SIGN_IN = 1001
    private val RC_GOOGLE_SIGN_IN = 1002


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailLogin_btn_login.setOnClickListener {
            startActivityForResult(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
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
            toast("아직 설정안함")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //이메일 로그인
        if (requestCode == RC_EMAIL_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                val progressDialog = indeterminateProgressDialog("회원정보 확인중...")
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
    }
}
