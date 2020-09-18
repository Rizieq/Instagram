package cookode.instagram_clone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import cookode.instagram_clone.adapter.CommentsAdapter
import cookode.instagram_clone.model.Comment
import cookode.instagram_clone.model.User
import kotlinx.android.synthetic.main.activity_comment.*

class CommentActivity : AppCompatActivity() {

    private var postId = ""
    private var publisherId = ""
    private var firebaseUser: FirebaseUser? = null
    private var commentAdapter: CommentsAdapter? = null
    private var commentList: MutableList<Comment>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)
        val intent = intent
        postId = intent.getStringExtra("postId")
        publisherId = intent.getStringExtra("publisherId")

        firebaseUser = FirebaseAuth.getInstance().currentUser


        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        recyclerview_comments.layoutManager = linearLayoutManager

        commentList = ArrayList()
        commentAdapter = CommentsAdapter(this, commentList as ArrayList<Comment>)
        recyclerview_comments.adapter = commentAdapter


        userInfo()
        readComment() //mangambil semua comment user
        getPostImageComment() //menampilkan post Image di menu comment

        txtpost_comments.setOnClickListener {
            //create if statement : comment not be empty
            if (add_comment!!.text.toString() == ""){

                Toast.makeText(this@CommentActivity, "Please write Comment first", Toast.LENGTH_LONG).show()

            } else{
                addComment()
            }
        }
    }


    private fun addComment(){
        val commmentRef = FirebaseDatabase.getInstance().reference
            .child("Comments").child(postId)
        val commentsMap = HashMap<String,Any>()
        commentsMap["comment"] = add_comment.text.toString()
        commentsMap["publisher"] = firebaseUser!!.uid
        commmentRef.push().setValue(commentsMap)
        add_comment.text.clear()
    }

    private fun userInfo(){
        val usersRef = FirebaseDatabase.getInstance().reference
            .child("Users").child(firebaseUser!!.uid)
        usersRef.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val user = snapshot.getValue<User>(User::class.java)
                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                        .into(profile_image_comment)
                }
            }

        })
    }


    private fun getPostImageComment(){
        val postCommentRef = FirebaseDatabase.getInstance().reference
            .child("Posts").child(postId).child("postimage")
        postCommentRef.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){

                    val image = snapshot.value.toString()
                    Picasso.get().load(image).placeholder(R.drawable.profile)
                        .into(post_image_comment)
                }
            }

        })
    }

    private fun readComment(){

        var commentsRef = FirebaseDatabase.getInstance().reference
            .child("Comments").child(postId)

        commentsRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()){

                    commentList!!.clear()

                    for (snapshot in p0.children){

                        val comment = snapshot.getValue(Comment::class.java)

                        commentList!!.add(comment!!)
                    }

                    commentAdapter!!.notifyDataSetChanged()
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }
}
