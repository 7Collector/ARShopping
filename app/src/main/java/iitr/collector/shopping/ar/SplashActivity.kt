package iitr.collector.shopping.ar

import android.content.Intent
import android.os.Bundle
import android.window.SplashScreen
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import iitr.collector.shopping.ar.login.LoginActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setKeepOnScreenCondition { true }
        val user = FirebaseAuth.getInstance().currentUser
        if (user!=null){
            val i = Intent(this,HomeActivity::class.java)
            startActivity(i)
            finish()
        } else {
            val i = Intent(this,LoginActivity::class.java)
            startActivity(i)
            finish()
        }
    }
}