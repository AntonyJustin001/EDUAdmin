package com.edu.admin.dialog

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.airbnb.lottie.LottieAnimationView
import com.edu.admin.R

class CustomDialog(imageDrawable: Drawable, title: String, content: String,
    isDismiss:Boolean = false, delayMillis: Long = 0, onDismissListener: () -> Unit = {}) : DialogFragment() {

    private lateinit var ivImage: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvContent: TextView
    private lateinit var btnDone: View
    private lateinit var progressBar: LottieAnimationView

    private var imageDrawable: Drawable
    private var title: String
    private var content: String
    private var isDismiss: Boolean = false
    private var delayMillis: Long = 0
    private var onDismissListener: () -> Unit = {}

    init {
        this.imageDrawable = imageDrawable
        this.title = title
        this.content = content
        this.isDismiss = isDismiss
        this.delayMillis = delayMillis
        this.onDismissListener = onDismissListener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.custom_dialog, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ivImage = view.findViewById(R.id.ivImage)
        tvTitle = view.findViewById(R.id.tvTitle)
        tvContent = view.findViewById(R.id.tvContent)
        btnDone = view.findViewById(R.id.btnDone)
        progressBar = view.findViewById(R.id.progressBar)

        if(isDismiss) {
            btnDone.visibility = View.GONE
            progressBar.visibility = View.VISIBLE
        } else {
            btnDone.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
        }

        btnDone.setOnClickListener {
            onDismissListener()
            dismiss()
        }

        ivImage.setImageDrawable(imageDrawable)
        tvTitle.text = title
        tvContent.text = content

    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(R.drawable.bg_rounded_dialog)
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        // Get the window attributes and set the margin
        val params = dialog?.window?.attributes
        val margin = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._30sdp)
        params?.width = resources.displayMetrics.widthPixels - 2 * margin
        dialog?.window?.attributes = params

        if(isDismiss) {
            dismissAfter(delayMillis)
        }
    }

    private fun dismissAfter(delayMillis: Long) {
        view?.postDelayed({
            if (isAdded) {
                onDismissListener()
                dismiss()
            }
        }, delayMillis)
    }
}