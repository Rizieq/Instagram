package cookode.instagram_clone

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import cookode.instagram_clone.model.User
import kotlinx.android.synthetic.main.activity_setting_account.*

class SettingActivity : AppCompatActivity() {
    private lateinit var firebaseUser: FirebaseUser
    private var myUrl = ""
    private var imageUri : Uri? = null
    private var storageProfilePictureRef: StorageReference? = null

    private var usersRef = FirebaseDatabase.getInstance()
    private var fbImage: String = ""
    private var fbUsername: String = ""
    private var fbBiodata: String = ""
    private var fbFullname: String = ""

    private var inUsername: String = ""
    private var inBiodata: String = ""
    private var inFullname: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_account)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        storageProfilePictureRef = FirebaseStorage.getInstance().reference.child("Profile Picture")



        save_info_profile_btn.setOnClickListener {
            finalDataUpload()
        }

        setprofile_image_view.setOnClickListener{
            ambilFoto()
        }


        logout_btn_setprofile.setOnClickListener {
            logout()
        }
        userInfo()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK
            && data!= null){
            val result = CropImage.getActivityResult(data)
            imageUri = result.uri
            setprofile_image_view.setImageURI(imageUri)
        }
    }

    private fun userInfo(){
        usersRef.reference.child("Users").child(firebaseUser.uid).
            addValueEventListener(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()){
                        val user = p0.getValue<User>(User::class.java)
                        fbImage = user!!.getImage()!!
                        fbUsername = user.getUsername()
                        fbFullname = user.getFullname()
                        fbBiodata = user.getBio()
                        Picasso.get().load(fbImage).placeholder(R.drawable.profile).into(setprofile_image_view)
                        username_setprofile_edittext.setText(fbUsername).toString()
                        fullname_setprofile_edittext.setText(fbFullname).toString()
                        bio_setprofile_edittext.setText(fbBiodata).toString()
                    }
                }

                override fun onCancelled(p0: DatabaseError) {

                }
            })
    }

    private fun ambilFoto(){
        CropImage.activity()
            .setAspectRatio(1,1)
            .start(this@SettingActivity)
    }

    private fun finalDataUpload() {
        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("ACCOUNT SETTING")
        progressDialog.setMessage("Please wait.., we are updating profile..")
        progressDialog.show()

        inFullname = fullname_setprofile_edittext.text.toString()
        inUsername = username_setprofile_edittext.text.toString()
        inBiodata = bio_setprofile_edittext.text.toString()

        if(imageUri != null && inFullname != fbFullname || inUsername != fbUsername || inBiodata != fbBiodata){
            val usersRef = FirebaseDatabase.getInstance().reference.child("Users")
            val fileRef = storageProfilePictureRef!!.child(firebaseUser.uid + "jpg")
            val uploadTask: StorageTask<*>
            val userMap = HashMap<String, Any>()
            uploadTask = fileRef.putFile(imageUri!!)
            uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot, Task<Uri>>{ task ->
                if (!task.isSuccessful){
                    task.exception.let {
                        throw it!!
                    }
                }
                return@Continuation fileRef.downloadUrl
            }).addOnCompleteListener ( OnCompleteListener<Uri> { task ->
                if (task.isSuccessful) {
                    progressDialog.dismiss()
                    val downloadUrl = task.result
                    myUrl = downloadUrl.toString()
                    userMap["image"] = myUrl
                    userMap["fullname"] = inFullname
                    userMap["username"] = inUsername
                    userMap["bio"]      = inBiodata
                    usersRef.child(firebaseUser.uid).updateChildren(userMap)
                    Toast.makeText(this,"Info Profile has been update", Toast.LENGTH_LONG).show()
                    val intent = Intent(this@SettingActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }else{
                    progressDialog.dismiss()
                }
            })
        }else if(imageUri == null){
            Toast.makeText(this, "Foto harus diubah", Toast.LENGTH_SHORT).show()
            progressDialog.dismiss()
        }
    }


    private fun logout(){
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this,LoginActivity::class.java).addFlags(
            Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        ))
    }


}