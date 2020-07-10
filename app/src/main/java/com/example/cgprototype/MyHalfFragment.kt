package com.example.cgprototype


import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.view.ContextMenu
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot

/**
 * A simple [Fragment] subclass.
 */
class MyHalfFragment : Fragment() {

    private var halfStoriesData = mutableMapOf<String, HalfStory?>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var dataset: MutableList<ImageCard>
    private lateinit var progressBar: ProgressBar
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_my_half, container, false)

        progressBar = root.findViewById(R.id.progress_bar)
        progressBar.visibility = View.VISIBLE
        recyclerView = root.findViewById(R.id.recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        dataset = mutableListOf()

        recyclerView.adapter = object : RecyclerViewAdapter(requireContext(), dataset){
            override fun onClickCallBack() {

                Toast.makeText(requireContext(), "Clicked on a story...", Toast.LENGTH_SHORT).show()

//                startActivity(Intent(requireContext(), DetailActivity::class.java))

            }
        }

        loadUserHalfImages()


        return root
    }


    fun loadUserHalfImages(){

        val user = FirebaseAuth.getInstance().currentUser
        if (user!=null)
        object: FirestoreDataClient(){
            override fun queryResult(data: MutableMap<String, DocumentSnapshot>) {

                if (data.size==0)
                    Toast.makeText(requireContext(), "No stories yet...", Toast.LENGTH_SHORT).show()

                data.forEach {
                    val halfStoryId = it.key
                    val halfStory = it.value.toObject(HalfStory::class.java)
                    halfStoriesData[halfStoryId] = halfStory
                    val imageUrl = halfStory?.imageUrl
                    object : UrlImageDownloader(imageUrl){
                        override fun downloadCompletedCallBack(bitmap: Bitmap?) {
                            val imageCard = ImageCard(bitmap, halfStory?.oneLiner, halfStory?.timeStamp?.toString(), "")
                            dataset.add(imageCard)
                            recyclerView.adapter?.notifyDataSetChanged()

                        }
                    }
                }

                progressBar.visibility = View.GONE

            }
        }.query(COLLECTION_HALF, DATA_KEY_UID, listOf(user.uid))

        else {
            Toast.makeText(requireContext(), "You're not logged in.", Toast.LENGTH_SHORT).show()
            progressBar.visibility = View.GONE
        }



//        val half1 = BitmapFactory.decodeResource(resources, R.drawable.half1)
//        val imageCard = ImageCard(half1, "Shazia Manzoor", "Bahawalnagar", "")
//        dataset.add(imageCard)
//        recyclerView.adapter?.notifyDataSetChanged()
//        progressBar.visibility = View.GONE
    }


}
