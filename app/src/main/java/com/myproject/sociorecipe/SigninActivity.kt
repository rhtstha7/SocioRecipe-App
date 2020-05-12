package com.myproject.sociorecipe

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.sdsmdg.tastytoast.TastyToast
import kotlinx.android.synthetic.main.activity_signin.*

class SigninActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

        //firebase instance
        firebaseAuth=FirebaseAuth.getInstance()

        //underline text
        login_forgetpassword_btn.paint.isUnderlineText = true
        login_signup_btn.paint.isUnderlineText=true
        login_resendEmail_btn.paint.isUnderlineText=true

        //sign up click listener
        login_signup_btn.setOnClickListener {
            val intent=Intent(this,SignupActivity::class.java) //move to sign up activity
            startActivity(intent)
        }

        //forget password click listener
        login_forgetpassword_btn.setOnClickListener {
            val intent= Intent(this,ForgetPasswordActivity::class.java) //move to forget password activity
            startActivity(intent)
        }

        //resend verification email
        login_resendEmail_btn.setOnClickListener {
            login_loading_animation.visibility=View.VISIBLE //animation visible
            //send verification email
            firebaseAuth.currentUser!!.sendEmailVerification().addOnCompleteListener {
                if (it.isSuccessful){ //on verification email sent
                    TastyToast.makeText(this,"Verification email has been sent",TastyToast.LENGTH_LONG,TastyToast.SUCCESS).show()
                    login_resendEmail_btn.visibility=View.INVISIBLE
                    login_loading_animation.visibility=View.INVISIBLE
                }
            }.addOnFailureListener {//email sending failed
                TastyToast.makeText(this,"${it.localizedMessage}",TastyToast.LENGTH_LONG,TastyToast.WARNING).show()
                login_resendEmail_btn.visibility=View.INVISIBLE
                login_loading_animation.visibility=View.INVISIBLE
            }
        }

        //login click listener
        login_login_btn.setOnClickListener {
            val email= login_email_edit.text.toString()
            val password= login_password_edit.text.toString()
            //validations
            if (email.length<=0){
                TastyToast.makeText(this,"Enter email.",TastyToast.LENGTH_LONG,TastyToast.WARNING).show()
            }else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                TastyToast.makeText(this,"Enter valid email.",TastyToast.LENGTH_LONG,TastyToast.WARNING).show()
            }else if (password.length<=0){
                TastyToast.makeText(this,"Enter password.",TastyToast.LENGTH_LONG,TastyToast.WARNING).show()
            }else if (password.length<6){
                TastyToast.makeText(this,"Password must be at least 6 characters.",TastyToast.LENGTH_LONG,TastyToast.WARNING).show()
            }else{ //validations pass
                login_login_btn.visibility=View.INVISIBLE
                login_loading_animation.visibility=View.VISIBLE
                //sign in user
                firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
                    if(task.isSuccessful){ //sign in successful
                        if (firebaseAuth.currentUser!!.isEmailVerified){ //check email verified or not
                            login_loading_animation.visibility=View.INVISIBLE
                            login_login_btn.visibility=View.VISIBLE
                            val intent=Intent(this,MainActivity::class.java) //move to main activity
                            startActivity(intent)
                            finish()
                        }else{  //email not verified
                            login_loading_animation.visibility=View.INVISIBLE
                            login_login_btn.visibility=View.VISIBLE
                            TastyToast.makeText(this,"Verify your email.",TastyToast.LENGTH_LONG,TastyToast.ERROR).show()
                            login_resendEmail_btn.visibility=View.VISIBLE
                        }
                    }
                }.addOnFailureListener {//sign in failed
                    login_loading_animation.visibility=View.INVISIBLE
                    login_login_btn.visibility=View.VISIBLE
                    TastyToast.makeText(this,"${it.localizedMessage}",TastyToast.LENGTH_LONG,TastyToast.ERROR).show()
                }
            }
        }
    }

    override fun onStart() {
        if (firebaseAuth.currentUser!=null){ //if user is not logout
            if (firebaseAuth.currentUser!!.isEmailVerified){
                val intent=Intent(this,MainActivity::class.java)
                startActivity(intent)
            }
        }
        super.onStart()
    }
}
