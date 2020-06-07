package com.example.incognito

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.example.incognito.messeges.LatestMessagesActivity
import com.example.incognito.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        register_button_register.setOnClickListener {
            //var dat = FirebaseDatabase.getInstance().reference
            //dat.setValue("MMU")
            performRegister()

        }

        already_have_an_account_textView4.setOnClickListener {
            Log.d("RegisterActivity", "login open")
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        profile_image_button_register.setOnClickListener {
            Log.d("RegisterActivity","try to show to photo selector")

            val intent = Intent(Intent.ACTION_PICK)
            intent.type="image/*"
            startActivityForResult(intent,0)
        }
    }

    var selectedPhotoUri: Uri? =null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==0 && resultCode == Activity.RESULT_OK && data !=null){
            //check what the selected image was...
            Log.d("RegisterActivity","photo was selected")

            selectedPhotoUri = data.data
            val bitmap= MediaStore.Images.Media.getBitmap(contentResolver,selectedPhotoUri)
            profilephoto_imageviw_register.setImageBitmap(bitmap)
            profile_image_button_register.alpha= 0f
            //val bitmapDrawable = BitmapDrawable(bitmap)
            //profile_image_button_register.setBackgroundDrawable(bitmapDrawable)
        }
        else{
            Toast.makeText(this, "Please Select Image", Toast.LENGTH_SHORT).show()
        }
    }


    private fun performRegister(){
        val email = email_editText_registration.text.toString()
        val password = password_editText_registration.text.toString()
        val phone = phone_editText_registration.text.toString()
        if(email.isEmpty())  {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT)
                .show()
            return
        }
        else if (password.isEmpty() ){
            Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT)
                .show()
            return
        }
        else if (profilephoto_imageviw_register.drawable == null){
            Toast.makeText(this, "Please Upload your Photo", Toast.LENGTH_SHORT)
                .show()
            return
        }
        else if(phone.isEmpty())
        {
            Toast.makeText(this, "Please enter you Phone Number", Toast.LENGTH_SHORT)
                .show()
            return
        }

        Log.d("RegisterActivity","Email is: " +email)
        Log.d("RegisterActivity","password: $password")
        //firebase Authentication
        Toast.makeText(this, "Please Wait...", Toast.LENGTH_SHORT)
            .show()
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener{
                if (!it.isSuccessful) return@addOnCompleteListener
                //
                Log.d("RegisterActivity", "Successfully created user with uid: ${it.result?.user?.uid}")
                Toast.makeText(this, "User Created", Toast.LENGTH_SHORT)
                    .show()

                uploadImageToFirebaseStorage()

            }
            .addOnFailureListener {
                Log.d("RegisterActivity","failed to create user: ${it.message}")
                Toast.makeText(this, "failed to create user: ${it.message}", Toast.LENGTH_LONG)
                    .show()
            }
    }

    private fun uploadImageToFirebaseStorage() {
        if (selectedPhotoUri == null) return

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener { it ->
                Log.d("RegisterActivity", "Successfully uploaded image: ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener {
                    Log.d("RegisterActivity", "File Location: $it")

                   saveUserToFirebaseDatabase(it.toString())
                }
            }
            .addOnFailureListener {
                Log.d("RegisterActivity", "Failed to upload image to storage: ${it.message}")
            }
    }
    private fun saveUserToFirebaseDatabase(profileImageUrl: String){
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref=FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(uid,username_editText_registration.text.toString(), profileImageUrl)
        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("RegisterActivity","saved the user to Firebase Data")

                val intent= Intent(this, LatestMessagesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener {
                Log.d("RegisterActivity","failure To Upload User data$it")
            }
    }

}


