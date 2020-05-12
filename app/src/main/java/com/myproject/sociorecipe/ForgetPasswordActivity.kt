package com.myproject.sociorecipe

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import com.google.firebase.auth.FirebaseAuth
import com.sdsmdg.tastytoast.TastyToast
import kotlinx.android.synthetic.main.activity_forget_password.*

class ForgetPasswordActivity : AppCompatActivity() {
    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forget_password)
        //underline text
        forgetpassword_login_btn.paint.isUnderlineText=true

        //firebase auth instance
        firebaseAuth=FirebaseAuth.getInstance()

        //login click listener
        forgetpassword_login_btn.setOnClickListener {
            onBackPressed()
        }

        //forget password click listener
        forgetpassword_forget_btn.setOnClickListener {
            val email=forgetpassword_email_edit.text.toString()
            //validations
            if (email.length<=0) {
                TastyToast.makeText(this,"Enter email.",TastyToast.LENGTH_LONG,TastyToast.WARNING).show()
            }else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    TastyToast.makeText(this,"Enter valid email.",TastyToast.LENGTH_LONG,TastyToast.WARNING).show()
            }else{ //validation pass
                //send reset email
                firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener {
                    if (it.isSuccessful){//email sent successfully
                        TastyToast.makeText(this,"Password reset email has sent.",TastyToast.LENGTH_LONG,TastyToast.SUCCESS).show()
                        onBackPressed()
                    }
                }.addOnFailureListener {
                    TastyToast.makeText(this,"${it.localizedMessage}",TastyToast.LENGTH_LONG,TastyToast.ERROR).show()
                }
            }
        }
    }

    override fun onBackPressed() {
        finish()
        super.onBackPressed()
    }
}
