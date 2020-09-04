package cookode.instagram_clone

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*

class LoginActivity : AppCompatActivity() {


    val mAuth: FirebaseAuth = FirebaseAuth.getInstance()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        btn_login.setOnClickListener {
            loginUser()
        }

        btn_signup_link.setOnClickListener {
            startActivity(Intent(this,RegisterActivity::class.java))
        }
    }

    private fun loginUser() {
        val email: String = email_login.text.toString()
        val password: String = password_login.text.toString()
        when {
            TextUtils.isEmpty(email) -> Toast.makeText(
                this,
                "Email tidak boleh kosong",
                Toast.LENGTH_SHORT
            ).show()
            TextUtils.isEmpty(password) -> Toast.makeText(
                this,
                "Password tidak boleh kosong",
                Toast.LENGTH_SHORT
            ).show()

           else -> {
               val progressDialog = ProgressDialog(this@LoginActivity)
               progressDialog.setTitle("Login")
               progressDialog.setMessage("Please Wait....")
               progressDialog.setCanceledOnTouchOutside(false)
               progressDialog.show()

               mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener{task ->
                   if (task.isSuccessful){
                       progressDialog.dismiss()
                       startActivity(Intent(this,MainActivity::class.java)
                           .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
                       finish()
                   } else {
                       progressDialog.dismiss()
                       mAuth.signOut()
                       Toast.makeText(
                           this,
                           "Email/Password salah",
                           Toast.LENGTH_SHORT
                       ).show()

                   }
               }
           }
        }
    }


    override fun onStart() {
        super.onStart()
        if (mAuth.currentUser != null){
            startActivity(Intent(this,MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
            finish()
        }
    }
}