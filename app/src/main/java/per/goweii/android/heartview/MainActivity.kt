package per.goweii.android.heartview

import android.graphics.Color
import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private class OnProgressChangeListener(
        private val listener: () -> Unit
    ) : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            listener()
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        heartView.setOnTouchListener { _, event ->
            val cx = (event.x - heartView.width / 2F) / (heartView.width / 2F)
            val cy = (event.y - heartView.height / 2F) / (heartView.height / 2F)
            heartView.setCenter(cx, cy)
            return@setOnTouchListener true
        }
        heartView.setCenter(-0.5F, -0.5F)
        sb_radius.setOnSeekBarChangeListener(OnProgressChangeListener {
            heartView.setRadiusPercent(sb_radius.progress.toFloat() / sb_radius.max.toFloat())
        })
        heartView.setRadiusPercent(sb_radius.progress.toFloat() / sb_radius.max.toFloat())
        sb_padding.setOnSeekBarChangeListener(OnProgressChangeListener {
            heartView.setPadding(sb_padding.progress.toFloat())
        })
        heartView.setPadding(sb_padding.progress.toFloat())
        val solidColorListener = OnProgressChangeListener {
            setSolidColor()
        }
        sb_solid_a.setOnSeekBarChangeListener(solidColorListener)
        sb_solid_r.setOnSeekBarChangeListener(solidColorListener)
        sb_solid_g.setOnSeekBarChangeListener(solidColorListener)
        sb_solid_b.setOnSeekBarChangeListener(solidColorListener)
        setSolidColor()
        val solidEdgeColorListener = OnProgressChangeListener {
            setSolidEdgeColor()
        }
        sb_solid_edge_a.setOnSeekBarChangeListener(solidEdgeColorListener)
        sb_solid_edge_r.setOnSeekBarChangeListener(solidEdgeColorListener)
        sb_solid_edge_g.setOnSeekBarChangeListener(solidEdgeColorListener)
        sb_solid_edge_b.setOnSeekBarChangeListener(solidEdgeColorListener)
        setSolidEdgeColor()
        sb_stroke_w.setOnSeekBarChangeListener(OnProgressChangeListener {
            heartView.setStrokeWidth(sb_stroke_w.progress.toFloat())
        })
        heartView.setStrokeWidth(sb_stroke_w.progress.toFloat())
        val strokeColorListener = OnProgressChangeListener {
            setStrokeColor()
        }
        sb_stroke_a.setOnSeekBarChangeListener(strokeColorListener)
        sb_stroke_r.setOnSeekBarChangeListener(strokeColorListener)
        sb_stroke_g.setOnSeekBarChangeListener(strokeColorListener)
        sb_stroke_b.setOnSeekBarChangeListener(strokeColorListener)
        setStrokeColor()
    }

    private fun setSolidColor() {
        heartView.setColor(
            Color.argb(
                sb_solid_a.progress,
                sb_solid_r.progress,
                sb_solid_g.progress,
                sb_solid_b.progress
            )
        )
    }

    private fun setSolidEdgeColor() {
        heartView.setEdgeColor(
            Color.argb(
                sb_solid_edge_a.progress,
                sb_solid_edge_r.progress,
                sb_solid_edge_g.progress,
                sb_solid_edge_b.progress
            )
        )
    }

    private fun setStrokeColor() {
        heartView.setStrokeColor(
            Color.argb(
                sb_stroke_a.progress,
                sb_stroke_r.progress,
                sb_stroke_g.progress,
                sb_stroke_b.progress
            )
        )
    }
}
