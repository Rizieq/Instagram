package cookode.instagram_clone.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import cookode.instagram_clone.R
import cookode.instagram_clone.SettingActivity
import cookode.instagram_clone.adapter.MyImagesAdapter
import cookode.instagram_clone.model.Post
import cookode.instagram_clone.model.User
import kotlinx.android.synthetic.main.fragment_profile.view.*
import java.util.*
import kotlin.collections.ArrayList

class ProfileFragment: Fragment() {

    private lateinit var profileId: String
    private lateinit var firebaseUser: FirebaseUser

    var postListGrid: MutableList<Post>? = null
    var myImagesAdapter: MyImagesAdapter? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewProfile = inflater.inflate(R.layout.fragment_profile, container, false)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        if (pref != null)
        {
            this.profileId = pref.getString("profileId","none")!!
        }

        if (profileId == firebaseUser.uid) {

            view?.btn_edit_account?.text = "Edit Profile"

        } else if (profileId != firebaseUser.uid){

            checkFollowAndFollowingStatus()
        }

        var recyclerViewUploadImages: RecyclerView? = null
        recyclerViewUploadImages = viewProfile.findViewById(R.id.recyclerview_upload_picimage)
        recyclerViewUploadImages?.setHasFixedSize(true)
        val linearLayoutManager = GridLayoutManager(context,3)
        recyclerViewUploadImages?.layoutManager = linearLayoutManager

        postListGrid = ArrayList()
        myImagesAdapter = context?.let { MyImagesAdapter(it, postListGrid as ArrayList<Post>) }
        recyclerViewUploadImages?.adapter = myImagesAdapter


        //merubah Account Settings and Profile Page Follow and Following Button
        //sesuai kondisi
        viewProfile.btn_edit_account.setOnClickListener {
            val getButtonText = view?.btn_edit_account?.text.toString()

            //when kondisi
            when{
                //jika button text = Edit profile maka intent ke Setting Account activity
                getButtonText == "Edit Profile" -> startActivity(Intent(context,SettingActivity::class.java))

                getButtonText == "Follow" -> {
                    firebaseUser.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it1.toString())
                            .child("Following").child(profileId).setValue(true)
                    }

                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(profileId)
                            .child("Followers").child(it1.toString()).setValue(true)
                    }
                }

                getButtonText == "Following" -> {
                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it1.toString())
                            .child("Following").child(profileId).removeValue()
                    }

                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(profileId)
                            .child("Followers").child(it1.toString()).removeValue()
                    }
                }
            }

        }

        getFollowers()
        getFollowings()
        userInfo()
        myPost()

        return viewProfile
    }


    private fun checkFollowAndFollowingStatus(){
        val followingRef = firebaseUser?.uid.let { it1 ->
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(it1.toString())
                .child("Following")
        }

        if (followingRef != null)
        {
            followingRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {

                    if (p0.child(profileId).exists())
                    {
                        view?.btn_edit_account?.text = "Following"
                    } else {
                        view?.btn_edit_account?.text = "Follow"
                    }
                }

                override fun onCancelled(p0: DatabaseError) {

                }
            })
        }
    }


    private fun getFollowers()
    {
        val followersRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(profileId)
            .child("Followers")

        followersRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot)
            {
                if (p0.exists()){
                    view?.txt_totalFollowers?.text = p0.childrenCount.toString()
                }
            }
            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun getFollowings()
    {
        val followersRef = FirebaseDatabase.getInstance().reference
            //sesuai yang berada di firebase
            .child("Follow").child(profileId)
            .child("Following")

        followersRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {

                if (p0.exists()){
                    view?.txt_totalFollowing?.text = p0.childrenCount.toString()
                }
            }
            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun myPost(){

        val postRef = FirebaseDatabase.getInstance().reference.child("Posts")
        postRef.addValueEventListener(object :ValueEventListener{

            override fun onDataChange(p0: DataSnapshot) {

                if (p0.exists()){

                    (postListGrid as ArrayList<Post>).clear()

                    for (snapshot in p0.children){
                        val post = snapshot.getValue(Post::class.java)
                        if (post?.getPublisher().equals(profileId))
                        {
                            (postListGrid as ArrayList<Post>).add(post!!)
                        }

//                        postListGrid?.reverse()
                        Collections.reverse(postListGrid)
                        myImagesAdapter!!.notifyDataSetChanged()
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun userInfo(){
        val usersRef = FirebaseDatabase.getInstance().getReference()
            .child("Users").child(profileId)

        usersRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {

                if (p0.exists()){
                    val user = p0.getValue<User>(User::class.java)

                    Picasso.get().load(user?.getImage()).placeholder(R.drawable.profile)
                        .into(view?.profile_image_gbr_frag)
                    view?.profile_fragment_username?.text = user?.getUsername()
                    view?.txt_full_namaProfile?.text = user?.getFullname()
                    view?.txt_bio_profile?.text = user?.getBio()
                }
            }
            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    override fun onStop() {
        super.onStop()
        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()

    }

    override fun onPause() {
        super.onPause()
        val pref = context?.getSharedPreferences("PREFS",Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        val pref = context?.getSharedPreferences("PREFS",Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }



}
