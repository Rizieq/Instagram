package cookode.instagram_clone.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import cookode.instagram_clone.CommentActivity
import cookode.instagram_clone.MainActivity
import cookode.instagram_clone.R
import cookode.instagram_clone.model.Post
import cookode.instagram_clone.model.User
import de.hdodenhof.circleimageview.CircleImageView

class PostAdapter(private val mContext: Context, private val mPost:List<Post>):
    RecyclerView.Adapter<PostAdapter.ViewHolder>() {
    private var firebaseUser: FirebaseUser? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.post_layout,parent,false))
    }

    override fun getItemCount(): Int {
        return mPost.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        firebaseUser = FirebaseAuth.getInstance().currentUser

        val post = mPost[position]

        Picasso.get().load(post.getPostimage()).into(holder.postImage)

        if (post.getDescription().equals("")){
            holder.description.visibility = View.GONE
        } else {
            holder.description.visibility = View.VISIBLE
            holder.description.setText(post.getDescription())
        }

        publisherInfo(holder.profileImage, holder.userName, holder.publisher, post.getPublisher())

        //method untuk seting icon
        isLikes(post.getPostid(), holder.likeButton)
        //method untuk melihat berapa user yg like
        numberOfLikes(holder.likes, post.getPostid()) //dengan membawa 1 widget like
        //method untuk menampilkan total user comment
        getTotalComment(holder.comments, post.getPostid())


        holder.likeButton.setOnClickListener {
            if (holder.likeButton.tag == "Like"){

                FirebaseDatabase.getInstance().reference
                    .child("Likes").child(post.getPostid()).child(firebaseUser!!.uid)
                    .setValue(true)
            }
            else {
                FirebaseDatabase.getInstance().reference
                    .child("Likes").child(post.getPostid()).child(firebaseUser!!.uid)
                    .removeValue()

                val intent = Intent(mContext, MainActivity::class.java)
                mContext.startActivity(intent)
            }
        }

        holder.commentButton.setOnClickListener {
            val intentComment = Intent(mContext, CommentActivity::class.java) //18
            intentComment.putExtra("postId", post.getPostid())
            intentComment.putExtra("publisherId", post.getPublisher())
            mContext.startActivity(intentComment)
        }

        holder.comments.setOnClickListener {
            val intentComment = Intent(mContext, CommentActivity::class.java) //18
            intentComment.putExtra("postId", post.getPostid())
            intentComment.putExtra("publisherId", post.getPublisher())
            mContext.startActivity(intentComment)
        }
    }

    private fun numberOfLikes(likes: TextView, postid: String) {

        val likesRef = FirebaseDatabase.getInstance().reference
            .child("Likes").child(postid)

        likesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()){
                    likes.text = p0.childrenCount.toString() + " likes"
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }


    private fun publisherInfo(profileImage: CircleImageView, userName: TextView, publisher: TextView, publisherID: String) {

        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(publisherID)
        usersRef.addValueEventListener(object :ValueEventListener
        {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()){
                    val user = p0.getValue<User>(User::class.java)

                    Picasso.get().load(user?.getImage()).placeholder(R.drawable.profile)
                        .into(profileImage)
                    userName.text = user?.getUsername()
                    publisher.text = user?.getFullname()
                }
            }
            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun getTotalComment(comments: TextView, postid: String) {

        val commentsRef = FirebaseDatabase.getInstance().reference
            .child("Comments").child(postid)

        commentsRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()){
                    comments.text = "view all" + p0.childrenCount.toString() + " comments"
                }

            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun isLikes(postid: String, likeButton: ImageView) {

        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val likesRef = FirebaseDatabase.getInstance().reference
            .child("Likes").child(postid)

        likesRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.child(firebaseUser!!.uid).exists()){
                    likeButton.setImageResource(R.drawable.heart_clicked)
                    likeButton.tag = "Liked"
                }
                else {
                    likeButton.setImageResource(R.drawable.heart_not_clicked)
                    likeButton.tag = "Like"
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

    }


    class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        var profileImage: CircleImageView = itemView.findViewById(R.id.user_profile_image_post)
        var postImage: ImageView = itemView.findViewById(R.id.post_image_home)
        var likeButton: ImageView = itemView.findViewById(R.id.post_image_like_btn)
        var commentButton: ImageView = itemView.findViewById(R.id.post_image_comment_btn)
        var saveButton: ImageView = itemView.findViewById(R.id.post_save_btn)
        var userName: TextView = itemView.findViewById(R.id.post_user_name)
        var likes: TextView = itemView.findViewById(R.id.post_likes)
        var publisher: TextView = itemView.findViewById(R.id.post_publisher)
        var description: TextView = itemView.findViewById(R.id.post_description)
        var comments: TextView = itemView.findViewById(R.id.post_comments)

    }

}