package com.example.cgprototype

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*
import org.w3c.dom.Text

class DetailActivity : AppCompatActivity() {

    private val dataset = mutableListOf<Double>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        recyclerview.layoutManager = LinearLayoutManager(this)
        val uid = intent.getStringExtra(DATA_KEY_UID)
        if (uid!=null){

            loadProfileWithUid(uid)
        }
        else {
            val UID = FirebaseAuth.getInstance().currentUser?.uid
            if (UID!=null)
                loadProfileWithUid(UID)
        }



        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
    }

    private fun loadProfileWithUid(uid: String){
        object : FirestoreDataClient(){
            override fun queryResult(data: MutableMap<String, DocumentSnapshot>) {

                data.forEach {
                    val profile = it.value.toObject(Profile::class.java)
                    val dataset = arrayOf(
                        DoubleValue(profile?.name, "Name"),
                        DoubleValue(profile?.email, "Email"),
                        DoubleValue("Hi there I'm using Connectography App...", "Intro"))
                    recyclerview.adapter = RecyclerAdapter(this@DetailActivity, dataset)

                    recyclerview.adapter?.notifyDataSetChanged()


                    val lem = {url:String ->
                        if (url.contains("google")){
                            url.replace("s96-c", "s400-c")}
                        else if (url.contains("facebook")){
                            "$url?height=320"}
                        else{url}
                    }
                    val url: String? = profile?.photoUrl

                    if (url!=null)
                        object : UrlImageDownloader(lem(url)){
                            override fun downloadCompletedCallBack(bitmap: Bitmap?) {
                                imageview_profile.setImageBitmap(bitmap)

                                progress_bar.visibility = View.GONE
                                imageview_profile.visibility = View.VISIBLE
                            }
                        }

                }

            }
        }.query(COLLECTION_PROFILE, DATA_KEY_UID, listOf(uid))
    }

    data class DoubleValue(val title: String? = "", val subtitle: String? = "")

    class MViewHolder(v: View): RecyclerView.ViewHolder(v){
        val titletv = v.findViewById<TextView>(R.id.textview_title)
        val subtitletv = v.findViewById<TextView>(R.id.textview_subtitle)
        fun bind(obj: DoubleValue){
            titletv.text = obj.title
            subtitletv.text = obj.subtitle
        }
    }
    class RecyclerAdapter(val context: Context, val dataset: Array<DoubleValue>): RecyclerView.Adapter<MViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MViewHolder(LayoutInflater.from(context).inflate(R.layout.view_double_text, parent, false))
        override fun getItemCount() = dataset.size
        override fun onBindViewHolder(holder: MViewHolder, position: Int) {

            holder.bind(dataset[position])

        }
    }
}
