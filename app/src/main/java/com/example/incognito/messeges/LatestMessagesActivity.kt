package com.example.incognito.messeges

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.incognito.messeges.NewMessageActivity
import com.example.incognito.R
import com.example.incognito.RegisterActivity
import com.example.incognito.messeges.NewMessageActivity.Companion.USER_KEY
import com.example.incognito.models.ChatMessage
import com.example.incognito.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_latest_messages.*
import kotlinx.android.synthetic.main.latest_message_row.view.*


class LatestMessagesActivity : AppCompatActivity() {

    companion object{
        var currentUser: User?= null
        val TAG="LatestMessages"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messages)

        recyclerview_latest_messages.adapter= adapter
        recyclerview_latest_messages.addItemDecoration(DividerItemDecoration(this,DividerItemDecoration.VERTICAL))

        //set item click listener on your adapter
        adapter.setOnItemClickListener { item, view ->
            Log.d(TAG,"check_intent")
            val intent= Intent(this,ChatLogActivity::class.java)
            //chatPatterner uid is required
            val row= item as LatestMessagesRow

            intent.putExtra(NewMessageActivity.USER_KEY,row.chatPartnerUser)
            startActivity(intent)
        }

//        setupDummyRows()
        listenForLatestMessages()
        fetchCurrentUser()

        verifyUserIsLoggedIn()
    }
    class LatestMessagesRow(val chatMessage: ChatMessage): Item<ViewHolder>(){
        var chatPartnerUser: User?=null

        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.message_textView_latest_message.text= chatMessage.text

            val chatPartnerId : String
            if(chatMessage.fromId==FirebaseAuth.getInstance().uid){
                chatPartnerId=chatMessage.fromId
            }
            else{
                chatPartnerId=chatMessage.fromId
            }
            val ref = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")
            ref.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(p0: DataSnapshot) {
                    chatPartnerUser = p0.getValue(User::class.java)
                    viewHolder.itemView.username_textView_latest_message.text=chatPartnerUser?.username

                    val targetImageView = viewHolder.itemView.imageView_latest_message
                    Picasso.get().load(chatPartnerUser?.profileImageUrl).into(targetImageView)
                }
                override fun onCancelled(p0: DatabaseError) {
                }
            })



        }
        override fun getLayout(): Int {
            return R.layout.latest_message_row
        }
    }

    val latestMessagesMap = HashMap<String, ChatMessage>()

    private fun refreshRecyclerViewMessages(){
        adapter.clear()
        latestMessagesMap.values.forEach {
            adapter.add(LatestMessagesRow(it))
        }
    }

    private fun listenForLatestMessages(){
        val fromId=FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId")
        ref.addChildEventListener(object:ChildEventListener{

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage=p0.getValue(ChatMessage::class.java)?:return
                latestMessagesMap[p0.key!!]= chatMessage
                refreshRecyclerViewMessages()

            }
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val chatMessage=p0.getValue(ChatMessage::class.java)?:return
                latestMessagesMap[p0.key!!]= chatMessage
                refreshRecyclerViewMessages()

            }


            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onCancelled(p0: DatabaseError) {

            }
            override fun onChildRemoved(p0: DataSnapshot) {

            }
        })
    }
    val adapter = GroupAdapter<ViewHolder>()

//        private fun setupDummyRows(){
//
//            adapter.add(LatestMessagesRow())
//            adapter.add(LatestMessagesRow())
//            adapter.add(LatestMessagesRow())
//
//
//        }


        private fun fetchCurrentUser(){
            val uid=FirebaseAuth.getInstance().uid
            val ref= FirebaseDatabase.getInstance().getReference("/users/$uid")
            ref.addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(p0: DataSnapshot) {
                    currentUser = p0.getValue(User::class.java)
                    Log.d("LatestActivity","Current User ${currentUser?.username}")
                }


                override fun onCancelled(p0: DatabaseError) {

                }


            })
        }

        private fun verifyUserIsLoggedIn(){
            val uid = FirebaseAuth.getInstance().uid
            if (uid == null) {
                val intent = Intent(this, RegisterActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId){
            R.id.menu_new_message ->{
                val intent=Intent(this, NewMessageActivity::class.java)
                startActivity(intent)
            }
            R.id.menu_sign_out ->{
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, RegisterActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)

            }

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
}
