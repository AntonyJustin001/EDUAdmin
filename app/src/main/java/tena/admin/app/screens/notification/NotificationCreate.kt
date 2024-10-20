package tena.admin.app.screens.notification

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import tena.admin.app.R
import tena.admin.app.models.Notification
import tena.admin.app.models.Product
import tena.admin.app.utils.loadImageFromUrl
import tena.health.care.models.Student
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class NotificationCreate() : Fragment() {

    private lateinit var etNotificationTitle: EditText
    private lateinit var etNotificationDescription: EditText
    private lateinit var btnSend: Button
    private lateinit var progressBar: LottieAnimationView
    private lateinit var ivBack: ImageView

    // Replace with your OneSignal REST API Key and OneSignal App ID
    private val oneSignalAppId = "d973d0fa-e459-47f7-b591-d297636693e3"
    private val oneSignalApiKey = "NDRlOGEwMWQtOGFjZi00MTBlLWE5ZGMtM2ZjMTgwNWY3MTRh"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_notification, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById<LottieAnimationView>(R.id.progressBar)
        etNotificationTitle = view.findViewById(R.id.etNotificationTitle)
        etNotificationDescription = view.findViewById(R.id.etNotificationDescription)
        btnSend = view.findViewById(R.id.btnSend)
        btnSend.setOnClickListener {
            if (etNotificationTitle.text.toString() != "") {
                if (etNotificationDescription.text.toString() != "") {
                        val playerId = "62cea79d-8159-4763-be00-d4e821009112"  // Replace with actual OneSignal player ID of the user
                        sendNotification(etNotificationTitle.text.toString(), etNotificationDescription.text.toString())
                } else {
                    Snackbar.make(
                        requireView(),
                        "Please Enter Notification Description",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            } else {
                Snackbar.make(requireView(), "Please Enter Notification Name", Snackbar.LENGTH_LONG)
                    .show()
            }
        }

        ivBack = view.findViewById(R.id.ivBack)
        ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

    }

    fun sendNotification(title: String, message: String) {
        val client = OkHttpClient()

        // Create JSON object to define the notification payload
        val jsonBody = JSONObject().apply {
            put("app_id", oneSignalAppId) // Your OneSignal app ID
            put("included_segments", JSONArray(listOf("All"))) // Player ID(s) (User)
            put("headings", JSONObject().put("en", title)) // Notification title
            put("contents", JSONObject().put("en", message)) // Notification message
        }

        val requestBody = RequestBody.create(
            MediaType.get("application/json; charset=utf-8"),
            jsonBody.toString()
        )

        // Create HTTP request to OneSignal API
        val request = Request.Builder()
            .url("https://onesignal.com/api/v1/notifications")
            .post(requestBody)
            .addHeader("Authorization", "Basic $oneSignalApiKey") // Your REST API Key
            .addHeader("Content-Type", "application/json")
            .build()

        // Execute the request
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Snackbar.make(
                    requireView(),
                    "Something Went Wrong Try Again Later",
                    Snackbar.LENGTH_LONG
                ).show()
                e.printStackTrace() // Log failure
            }

            override fun onResponse(call: Call, response: Response) = if (response.isSuccessful) {
                println("Notification sent successfully resposne - $response")
                val current = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                storeNotification(
                    Notification(
                        notificationId = UUID.randomUUID().toString(),
                        notificationTitle = "$title",
                        notificationMessage = "$message",
                        receivedDate = current.format(formatter),
                    ))

            } else {
                Snackbar.make(
                    requireView(),
                    "Something Went Wrong Try Again Later",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        })
    }

    fun storeNotification(notification: Notification) {
        val db = FirebaseFirestore.getInstance()
        db.collection("notifications")
            .document(notification.notificationId)
            .set(notification)
            .addOnSuccessListener {
                Snackbar.make(
                    requireView(),
                    "Notification added successfully",
                    Snackbar.LENGTH_LONG
                ).show()
                parentFragmentManager.popBackStack()

            }
            .addOnFailureListener { e ->
                Snackbar.make(
                    requireView(),
                    "Something Went Wrong Try Again Later",
                    Snackbar.LENGTH_LONG
                ).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

}