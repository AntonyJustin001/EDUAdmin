package tena.admin.app.screens.splash

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import tena.admin.app.R
import tena.admin.app.screens.home.HomeScreen
import tena.admin.app.screens.signIn.SignInScreen
import tena.admin.app.utils.USER_DETAILS
import tena.admin.app.utils.loadScreen
import tena.admin.app.utils.prefs

class SplashScreen : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.e("Test","User Details - ${prefs.get(USER_DETAILS, "")}")

        Handler(Looper.getMainLooper()).postDelayed({
            if(prefs.get(USER_DETAILS,"")!="") {
                loadScreen(requireActivity(), HomeScreen(),"SplashScreen")
            } else {
                loadScreen(requireActivity(), SignInScreen(),"SplashScreen")
            }
        }, 5000)
    }

    override fun onDetach() {
        super.onDetach()
    }

}