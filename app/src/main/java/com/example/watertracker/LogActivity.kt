package com.example.watertracker

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.example.watertracker.databinding.ActivityLogBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class LogActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLogBinding

    private var animations = mutableListOf<ValueAnimator?>()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // default
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val cups = listOf(binding.cup1, binding.cup2, binding.cup3, binding.cup4, binding.cup5, binding.cup6, binding.cup7, binding.cup8)
        val checks = listOf(binding.check1, binding.check2, binding.check3, binding.check4, binding.check5, binding.check6, binding.check7, binding.check8)

        for (i in 0..7) {
            val anim = setupCupAnimation(cups[i])
            animations.add(anim)
            cups[i].setOnClickListener(onClick(cups[i], checks[i], i, anim))
        }

        binding.back.setOnClickListener {
            finish() // close screen
        }
    }

    override fun onResume() {
        super.onResume()
        val app = application as MainApplication
        app.syncWithDatabase {
            updateUI()
        }
    }

    private fun updateUI() {
        val app = application as MainApplication
        val checks = listOf(binding.check1, binding.check2, binding.check3, binding.check4, binding.check5, binding.check6, binding.check7, binding.check8)
        val cups = listOf(binding.cup1, binding.cup2, binding.cup3, binding.cup4, binding.cup5, binding.cup6, binding.cup7, binding.cup8)

        for (i in 0..7) {
            checks[i].isVisible = app.cupChecked[i]
            manageAnimationState(shouldAnimate = !app.cupChecked[i], cups[i], animations[i])
        }
    }

    private fun onClick(cup: View, check: View, index: Int, animation: ValueAnimator?): View.OnClickListener {

        val app = application as MainApplication

        return View.OnClickListener {
            check.apply {
                isVisible = !isVisible
            }

            runBlocking(Dispatchers.IO) {
                app.db.waterlogDao().updateCup(index + 1, check.isVisible)
            }

            if (check.isVisible) {
                app.totalWater++
                app.cupChecked[index] = true
                manageAnimationState(shouldAnimate = false, cup, animation)
            } else {
                app.totalWater--
                app.cupChecked[index] = false
                manageAnimationState(shouldAnimate = true, cup, animation)
            }
        }
    }

    private fun setupCupAnimation(button: View): ValueAnimator {
        var cupAnimator: ValueAnimator? = null

        cupAnimator = ValueAnimator.ofFloat(1.0f, 0.8f).apply {
            duration = 1000 // 1 second length
            interpolator = LinearInterpolator()
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE

            // Runs every frame to change scale dynamically
            addUpdateListener { animator ->
                val scaleValue = animator.animatedValue as Float
                button.scaleX = scaleValue
                button.scaleY = scaleValue
            }
        }

        return cupAnimator
    }

    // helper function to turn animation on or lock component at original size
    private fun manageAnimationState(shouldAnimate: Boolean, button: View, cupAnimator: ValueAnimator?) {
        if (shouldAnimate) {
            if (cupAnimator?.isRunning == false) {
                cupAnimator?.start() // starts animation if it wasn't running before
            }
        } else {
            cupAnimator?.cancel()
            // resets back to exactly normal size safely
            button.scaleX = 1.0f
            button.scaleY = 1.0f
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        for (anim in animations) {
            anim?.cancel()
        }
    }
}
