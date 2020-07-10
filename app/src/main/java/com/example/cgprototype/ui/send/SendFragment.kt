package com.example.cgprototype.ui.send

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.cgprototype.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot

class SendFragment : Fragment() {

    private lateinit var sendViewModel: SendViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        sendViewModel =
            ViewModelProviders.of(this).get(SendViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_send, container, false)
        val textView: TextView = root.findViewById(R.id.text_send)
        sendViewModel.text.observe(this, Observer {
            textView.text = it
        })

        val currentUser = FirebaseAuth.getInstance().currentUser

        val uids = listOf( "D2JSV7Wcg9VQMo1fmFZzB2EAgVy1", "8LqC5xBaVcNOSpkfpsxZtRQLaRt1")
        object : FirestoreDataClient(){
            override fun queryResult(data: MutableMap<String, DocumentSnapshot>) {
                textView.text = data.toString()
                data.forEach {doc->

//                    val halfStory = doc.value.toObject(HalfStory::class.java)
//
                    val profileId = doc.key
                    val profile = doc.value.toObject(Profile::class.java)

                    textView.text = profile?.uid.toString()
//                    Toast.makeText(requireContext(), , Toast.LENGTH_SHORT).show()
                }
//                data.keys.forEach { key->
//                    val story = data[key]
//                    textView.text = story!![DATA_KEY_UID].toString()
//
//                }
            }
        }.queryAll(COLLECTION_PROFILE)


//        val auth = FirebaseAuth.getInstance()
//
//        val user = MUser()
////
////
//        val userKey = "8LqC5xBaVcNOSpkfpsxZtRQLaRt1"
//        db.collection("half_stories").document(userKey)
//            .get()
//            .addOnSuccessListener {data->
//                if (data!=null) {
////                    it.documents.forEach {data->
//
//                        Toast.makeText(
//                            requireContext(),
//                            data["name"] as String?,
//                            Toast.LENGTH_SHORT
//                        ).show()
////                    }
//                }
//            }
//        db.collection("users")
//            .add(user)
//            .addOnSuccessListener {
//                Toast.makeText(requireContext(), it.id, Toast.LENGTH_SHORT).show()
//            }

        return root
    }

    data class MUser(val name: String? = null, val phone: String? = null)
}