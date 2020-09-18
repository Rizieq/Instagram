package cookode.instagram_clone

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_add_post.*
import kotlinx.android.synthetic.main.activity_setting_account.*

class AddPostActivity : AppCompatActivity() {

    private var myUrl = ""
    private var imageUri: Uri? = null
    private var storageProfilePictureRef: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)

        storageProfilePictureRef = FirebaseStorage.getInstance().reference.child("Post Picture")

        save_new_post_btn.setOnClickListener {
            uploadContent()
        }
        CropImage.activity()
            .setAspectRatio(1,1)
            .start(this@AddPostActivity)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK
            && data != null
        ) {
            val result = CropImage.getActivityResult(data)
            imageUri = result.uri
            image_post.setImageURI(imageUri)
        }
    }

    private fun uploadContent() {
        when (imageUri) {
            null -> Toast.makeText(this, "Gambar tidak boleh kosong", Toast.LENGTH_LONG)
            else -> {
                val fileRef =
                    storageProfilePictureRef?.child(System.currentTimeMillis().toString() + ".jpg")
                val uploadTask: StorageTask<*>
                val ref = FirebaseDatabase.getInstance().reference.child("Posts")
                var postId = ref.push().key
                uploadTask = fileRef!!.putFile(imageUri!!)
                uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (!task.isSuccessful) {
                        task.exception.let {
                            throw it!!
                        }
                    }
                    return@Continuation fileRef.downloadUrl
                }).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUrl = task.result
                        myUrl = downloadUrl.toString()
                        val postMap = HashMap<String, Any>()
                        postMap ["postId"] = postId!!
                        postMap ["description"] = deskripsi_post.text.toString()
                        postMap ["publisher"] = FirebaseAuth.getInstance().currentUser!!.uid
                        postMap ["postimage"] = myUrl
                        ref.child(postId).setValue(postMap)
                        Toast.makeText(this,"Content uploaded", Toast.LENGTH_LONG).show()
                        val intent = Intent(this@AddPostActivity, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this,"Content failed to upload", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}