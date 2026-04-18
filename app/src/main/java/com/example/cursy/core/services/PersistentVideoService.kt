package com.example.cursy.core.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PersistentVideoService : Service() {

    private var exoPlayer: ExoPlayer? = null
    private val binder = VideoBinder()

    inner class VideoBinder : Binder() {
        fun getService(): PersistentVideoService = this@PersistentVideoService
    }

    override fun onCreate() {
        super.onCreate()
        exoPlayer = ExoPlayer.Builder(this).build()
        exoPlayer?.repeatMode = Player.REPEAT_MODE_OFF

        exoPlayer?.addListener(object : Player.Listener {
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                Log.e("PersistentVideoService", "Error de reproducción: ${error.message}")
            }
            override fun onPlaybackStateChanged(state: Int) {
                val stateStr = when(state) {
                    Player.STATE_IDLE -> "IDLE"
                    Player.STATE_BUFFERING -> "BUFFERING"
                    Player.STATE_READY -> "READY"
                    Player.STATE_ENDED -> "ENDED"
                    else -> "UNKNOWN"
                }
                Log.d("PersistentVideoService", "Estado del reproductor: $stateStr")
            }
        })
    }

    override fun onBind(intent: Intent): IBinder = binder

    fun getPlayer(): ExoPlayer? = exoPlayer

    fun playVideo(url: String) {
        exoPlayer?.let { player ->
            val currentUri = player.currentMediaItem?.localConfiguration?.uri?.toString()

            if (currentUri == url && player.playbackState != Player.STATE_IDLE) {
                Log.d("PersistentVideoService", "El video ya se está reproduciendo: $url")
                player.play()
                return
            }

            Log.d("PersistentVideoService", "Cargando nuevo video: $url")
            val mediaItem = MediaItem.fromUri(url)
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()
        }
    }

    fun stopVideo() {
        exoPlayer?.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.release()
        exoPlayer = null
    }
}
