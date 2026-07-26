package com.example.watertracker

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.example.watertracker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var plant by mutableIntStateOf(R.drawable.plant_stage1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                SimpleSpringExample(plant)
            }
        }

        binding.logButton.setOnClickListener {
            val intent = Intent(this, LogActivity::class.java)
            startActivity(intent)
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

        if (app.totalWater < 3) {
            plant = R.drawable.plant_stage1
        } else if (app.totalWater < 5) {
            plant = R.drawable.plant_stage2
        } else if (app.totalWater < 7) {
            plant = R.drawable.plant_stage3
        } else {
            plant = R.drawable.group_66_cropped
        }

        binding.bar.post {
            val size = binding.barTop.layoutParams
            val size2 = binding.bar.layoutParams
            if (size2.width > 0) {
                size.width = (size2.width / 8) * app.totalWater
                binding.barTop.layoutParams = size
            }
        }
    }
}

@Composable
fun SimpleSpringExample(plant: Int) {
    val size: Dp
    if (plant == R.drawable.plant_stage1) {
        size = 80.dp
    } else if (plant == R.drawable.plant_stage2) {
        size = 130.dp
    } else if (plant == R.drawable.plant_stage3) {
        size = 300.dp
    } else {
        size = 330.dp
    }

    // Step 1: Track whether the box is in its "moved" state
    var moved by remember { mutableStateOf(false) }

    // Step 2: Create an animated float value
    val offsetY by animateFloatAsState(
        targetValue = if (moved) -220f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "boxOffset"
    )

    LifecycleResumeEffect(Unit) {
        moved = true
        onPauseOrDispose {
            moved = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {

        // Step 3: Apply the offset to the Box
        Image(
            painter = painterResource(plant),
            contentDescription = "Sapling",
            modifier = Modifier
                .size(size)
                .offset(y = offsetY.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}
