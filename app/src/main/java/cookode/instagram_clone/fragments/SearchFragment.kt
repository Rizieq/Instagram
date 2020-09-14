package cookode.instagram_clone.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import cookode.instagram_clone.R
import cookode.instagram_clone.adapter.UserAdapter
import cookode.instagram_clone.model.User
import kotlinx.android.synthetic.main.fragment_search.view.*

class SearchFragment: Fragment() {


    private var recyclerView: RecyclerView? = null
    private var userAdapter: UserAdapter? = null
    private var myUser: MutableList<User>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        recyclerView = view.findViewById(R.id.search_recyclerView)
        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = LinearLayoutManager(context)

        myUser = ArrayList()
        userAdapter = context?.let { UserAdapter(it, myUser as ArrayList<User>, true) }
        recyclerView?.adapter = userAdapter


        view.search_editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //id search_editText harus sesuai dengan di layoutnya
                if (view.search_editText.toString() == "") {

                } else {
                    recyclerView?.visibility = View.VISIBLE
                    getUsers() //menganbil data user
                    //method search dan
                    //terdapat 2 parameter toString dan lowerCase agar tidak menggunakan huruf besar
                    searchUser(s.toString().toLowerCase())

                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
        return view
    }

    private fun searchUser(input: String) {
        val query = FirebaseDatabase.getInstance().getReference()
            .child("Users")
            .orderByChild("fullname")
            .startAt(input).endAt(input + "\uf8ff")

        query.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                myUser?.clear()

                for (snapshot in dataSnapshot.children) {
                    val user = snapshot.getValue(User::class.java)
                    if (user != null) {
                        myUser?.add(user)
                    }
                }

                userAdapter?.notifyDataSetChanged()

            }
            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun getUsers() {
        val usersRef = FirebaseDatabase.getInstance().getReference().child("Users")
        usersRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (view?.search_editText?.toString() == "") {
                    myUser?.clear()

                    for (snapshot in dataSnapshot.children) {
                        val user = snapshot.getValue(User::class.java)
                        if (user != null) {
                            myUser?.add(user)
                        }
                    }

                    userAdapter?.notifyDataSetChanged()
                }
            }

            override fun onCancelled(p0: DatabaseError) {
            }
        })
    }
}
