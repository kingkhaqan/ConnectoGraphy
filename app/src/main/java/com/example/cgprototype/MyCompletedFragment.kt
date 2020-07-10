package com.example.cgprototype


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot

/**
 * A simple [Fragment] subclass.
 */
class MyCompletedFragment : Fragment() {

    private var completedStoriesData = mutableMapOf<String, CompletedStory?>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var dataset: MutableList<ImageCard>
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_my_completed, container, false)

        progressBar = root.findViewById(R.id.progress_bar)
        progressBar.visibility = View.VISIBLE
        recyclerView = root.findViewById(R.id.recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        dataset = mutableListOf()

        recyclerView.adapter = object : RecyclerViewAdapter(requireContext(), dataset){
            override fun onClickCallBack() {

                Toast.makeText(context, "an item clicked", Toast.LENGTH_SHORT).show()
            }
        }

        loadUserCompletedImages()


        return root
    }

    fun loadUserCompletedImages(){
        val user = FirebaseAuth.getInstance().currentUser

        if (user!=null)
            object: FirestoreDataClient(){
                override fun queryResult(data: MutableMap<String, DocumentSnapshot>) {

                    if (data.size==0)
                        Toast.makeText(requireContext(), "No stories yet...", Toast.LENGTH_SHORT).show()

                    data.forEach {
                        val completedStoryId = it.key
                        val completedStory = it.value.toObject(CompletedStory::class.java)
                        completedStoriesData[completedStoryId] = completedStory
                        val imageUrl = completedStory?.imageUrl
                        object : UrlImageDownloader(imageUrl){
                            override fun downloadCompletedCallBack(bitmap: Bitmap?) {
                                val imageCard = ImageCard(bitmap, completedStory?.oneLiner, completedStory?.timeStamp?.toString(), "")
                                dataset.add(imageCard)
                                recyclerView.adapter?.notifyDataSetChanged()

                            }
                        }
                    }

                    progressBar.visibility = View.GONE

                }
            }.query(COLLECTION_COMPLETED, DATA_KEY_UID, listOf(user.uid))

        else{

            Toast.makeText(requireContext(), "You're not logged in.", Toast.LENGTH_SHORT).show()
            progressBar.visibility = View.GONE
        }


    }

}
