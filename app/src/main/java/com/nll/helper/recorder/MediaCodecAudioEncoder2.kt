package com.nll.helper.recorder

import android.annotation.SuppressLint
import android.media.*
import android.media.MediaCodec.CodecException
import com.nll.helper.recorder.mediacodec.AsynchronousMediaCodecAdapter
import com.nll.helper.recorder.mediacodec.MediaCodecAdapter
import com.nll.helper.recorder.mediacodec.MediaCodecAsyncCallback
import com.nll.helper.server.RecorderError
import java.io.File
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit
import kotlin.math.pow


class MediaCodecAudioEncoder2(
    private val recordingFile: File,
    private val audioChannelCount: Int,
    private val audioSource: Int,
    private val audioSamplingRate: Int,
    private val encodingBitrate: Int,
    private val gain: Int,
    val listener: Listener
) {
    private val logTag = "MediaCodecAudioEncoder2"

    interface Listener {
        fun onEncoderError(mediaCodecAudioEncoder: MediaCodecAudioEncoder2, recorderError: RecorderError, exception: Exception)
        fun onEncoderStart(mediaCodecAudioEncoder: MediaCodecAudioEncoder2)
        fun onEncoderPause(mediaCodecAudioEncoder: MediaCodecAudioEncoder2)
        fun onEncoderStop(mediaCodecAudioEncoder: MediaCodecAudioEncoder2)
    }

    private var audioRecorder: AudioRecord? = null
    private lateinit var mediaMuxer: MediaMuxer
    private lateinit var mediaCodecAdapter: MediaCodecAdapter
    private var isRecording = false
    private var isRecordingPaused = false
    private var timeAtPause: Long = 0
    private var elapsedTimeOnResumeInMicroSeconds: Long = 0
    private var recordingStartTime: Long = 0
    private var minimumBufferSize = 0


    @SuppressLint("MissingPermission")// App should already have permission
    private fun setupAudioRecord() {
        val audioFormatChannel = if (audioChannelCount == 1) AudioFormat.CHANNEL_IN_MONO else AudioFormat.CHANNEL_IN_STEREO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        minimumBufferSize = AudioRecord.getMinBufferSize(audioSamplingRate, audioFormatChannel, audioFormat) * 2
        if (CLog.isDebug()) {
            CLog.log(logTag, "setupAudioRecord() -> audioFormatChannel: $audioFormatChannel, audioFormat: $audioFormat, minimumBufferSize: $minimumBufferSize")
        }

        /*audioRecorder = AudioRecord(
                audioSource,
                audioSamplingRate,
                if (audioChannelCount == 1) AudioFormat.CHANNEL_IN_MONO else AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT, mMinimumBufferSize * 2)*/

        audioRecorder = AudioRecord(
            audioSource,
            audioSamplingRate,
            audioFormatChannel,
            audioFormat,
            minimumBufferSize
        )


    }


    private fun setupCodec() {
        if (CLog.isDebug()) {
            CLog.log(logTag, "setupCodec()")
        }
        val mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC)
        val format = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, audioSamplingRate, audioChannelCount).apply {
            setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
            setInteger(MediaFormat.KEY_CHANNEL_MASK, if (audioChannelCount == 1) AudioFormat.CHANNEL_IN_MONO else AudioFormat.CHANNEL_IN_STEREO)
            setInteger(MediaFormat.KEY_BIT_RATE, encodingBitrate)
            setInteger(MediaFormat.KEY_CHANNEL_COUNT, audioChannelCount)
            setInteger(MediaFormat.KEY_SAMPLE_RATE, audioSamplingRate)
        }

        mediaCodecAdapter = AsynchronousMediaCodecAdapter(mediaCodec, true, mediaCodecAsyncCallback).apply {
            configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        }

    }

    private val mediaCodecAsyncCallback = object : MediaCodecAsyncCallback() {
        override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
            if (isRecording) {

                val bufferOriginal = try {
                    codec.getInputBuffer(index)
                } catch (e: Exception) {
                    /*
                    Avoid
                    java.lang.IllegalStateException
                    at android.media.MediaCodec.getBuffer(Native Method)
                    at android.media.MediaCodec.getInputBuffer(MediaCodec.java:2992)
                    at px1$b.onInputBufferAvailable(MediaCodecAudioEncoder2.kt:2)
                    at s9.onInputBufferAvailable(AsynchronousMediaCodecAdapter.java:2)
                    at android.media.MediaCodec$EventHandler.handleCallback(MediaCodec.java:1653)
                    at android.media.MediaCodec$EventHandler.handleMessage(MediaCodec.java:1611)
                    at android.os.Handler.dispatchMessage(Handler.java:102)
                    at android.os.Looper.loop(Looper.java:154)
                    at android.os.HandlerThread.run(HandlerThread.java:61)
                     */
                    CLog.logPrintStackTrace(e)
                    null
                }

                if (bufferOriginal != null) {
                    val readSize = audioRecorder?.read(bufferOriginal, minimumBufferSize)
                        ?: 0//divide by 2 if audio is mono
                    /*if (CommonLogger.isDebug()) {
                        CommonLogger.log(logTag, "readSize: $readSize")
                    }*/
                    if (readSize > 0) {
                        val bufferMorphed = if (gain > 0) {
                            applyGainToByteBuffer(bufferOriginal)
                        } else {
                            bufferOriginal
                        }
                        try {
                            codec.queueInputBuffer(index, bufferMorphed.position(), readSize, 0, 0)
                        } catch (e: Exception) {
                            listener.onEncoderError(this@MediaCodecAudioEncoder2, RecorderError.MediaCodecQueueInputBufferFailed, e)
                            stop()
                        }
                    } else {
                        listener.onEncoderError(this@MediaCodecAudioEncoder2, RecorderError.AudioRecordReadFailed, Exception("AudioRecordReadFailed"))
                        stop()
                    }
                } else {
                    listener.onEncoderError(this@MediaCodecAudioEncoder2, RecorderError.EmptyInputBuffer, Exception("EmptyInputBuffer"))
                    stop()
                }
            }
        }

        override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
            if (isRecording) {
                val buffer = codec.getOutputBuffer(index)
                if (buffer != null) {

                    info.presentationTimeUs = if (isRecordingPaused) {
                        timeAtPause - (recordingStartTime / 1000L)
                    } else {
                        ((System.nanoTime() - recordingStartTime) / 1000L) - elapsedTimeOnResumeInMicroSeconds
                    }

                    /*if (CommonLogger.isDebug()) {
                        CommonLogger.log(logTag, "onOutputBufferAvailable() -> isRecordingPaused: $isRecordingPaused, info.presentationTimeUs: ${info.presentationTimeUs}, ${TimeUnit.MICROSECONDS.toSeconds(info.presentationTimeUs)}s")
                    }*/

                    if (!isRecordingPaused) {
                        try {
                            mediaMuxer.writeSampleData(0, buffer, info)
                        } catch (e: Exception) {
                            listener.onEncoderError(this@MediaCodecAudioEncoder2, RecorderError.MediaMuxerWriteFailed, e)
                            stop()
                        }
                    }

                    try {
                        codec.releaseOutputBuffer(index, false)
                    } catch (e: Exception) {
                        /**
                         *
                         * Against
                         * Java.lang.IllegalStateException
                         * at android.media.MediaCodec.releaseOutputBuffer(Native Method)
                         * at android.media.MediaCodec.releaseOutputBuffer(MediaCodec.java:2682)
                         * at onOutputBufferAvailable(SourceFile:8)
                         *
                         */
                        listener.onEncoderError(this@MediaCodecAudioEncoder2, RecorderError.MediaCodecException, e)
                        stop()
                    }
                } else {
                    listener.onEncoderError(this@MediaCodecAudioEncoder2, RecorderError.EmptyInputBuffer, Exception("EmptyInputBuffer"))
                    stop()
                }
            }
        }

        override fun onError(codec: MediaCodec, e: CodecException) {
            audioRecorder?.stop()
            mediaMuxer.stop()

            listener.onEncoderError(this@MediaCodecAudioEncoder2, RecorderError.MediaCodecException, e)

        }

        override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
            try {

                if (CLog.isDebug()) {
                    CLog.log(logTag, "onOutputFormatChanged $format")
                }
                mediaMuxer.addTrack(format)
                mediaMuxer.start()
                recordingStartTime = System.nanoTime()

            } catch (e: Exception) {
                CLog.logPrintStackTrace(e)

                try {
                    audioRecorder?.stop()
                } catch (ignored: Exception) {
                }

                listener.onEncoderError(this@MediaCodecAudioEncoder2, RecorderError.MediaCodecException, e)
            }
        }
    }

    //only for 16bit sample size
    private fun applyGainToByteBuffer(buf: ByteBuffer): ByteBuffer {
        val shortBuffer = buf.asShortBuffer()
        shortBuffer.rewind()
        val bLength = shortBuffer.capacity() // Faster than accessing buffer.capacity each time
        for (i in 0 until bLength) {
            var curSample = shortBuffer[i]
            curSample = (curSample * 10.0.pow(gain / 20.0).toInt().toShort()).toShort()
            if (curSample > Short.MAX_VALUE) {
                curSample = Short.MAX_VALUE
            }
            if (curSample < Short.MIN_VALUE) {
                curSample = Short.MIN_VALUE
            }
            shortBuffer.put(curSample)
        }
        buf.rewind()
        return buf
    }

    fun start() {
        if (CLog.isDebug()) {
            CLog.log(logTag, "start()")
        }
        setupAudioRecord()
        if (audioRecorder != null && audioRecorder?.state == AudioRecord.STATE_INITIALIZED) {
            elapsedTimeOnResumeInMicroSeconds = 0
            timeAtPause = 0
            try {
                mediaMuxer = MediaMuxer(recordingFile.getAbsolutePath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
                setupCodec()
            } catch (e: Exception) {
                CLog.logPrintStackTrace(e)
            }
            audioRecorder?.startRecording()
            mediaCodecAdapter.start()
            isRecording = true
            isRecordingPaused = false
            listener.onEncoderStart(this@MediaCodecAudioEncoder2)
        } else {
            listener.onEncoderError(this@MediaCodecAudioEncoder2, RecorderError.AudioRecordInUse, Exception("AudioRecordInUse"))
            //no need to call stop as it has not started
        }
    }

    fun pause() {
        setTimeAtPause()
        isRecordingPaused = true
        listener.onEncoderPause(this)
    }

    private fun setTimeAtPause() {
        timeAtPause = System.nanoTime() / 1000L
    }

    fun resume() {
        isRecordingPaused = false
        setElapsedTimeOnResume()
        listener.onEncoderStart(this)
    }

    private fun setElapsedTimeOnResume() {
        val changeInMicroSeconds = (System.nanoTime() / 1000L - timeAtPause)
        elapsedTimeOnResumeInMicroSeconds += changeInMicroSeconds

        if (CLog.isDebug()) {
            CLog.log(logTag, "setElapsedTimeOnResume() -> elapsedTimeOnResume: $elapsedTimeOnResumeInMicroSeconds")
        }
    }


    fun getRoughRecordingTimeInMillis(): Long {
        val roughRecordingTimeInMillis = TimeUnit.NANOSECONDS.toMillis((System.nanoTime() - recordingStartTime) - TimeUnit.MICROSECONDS.toNanos(elapsedTimeOnResumeInMicroSeconds))
        if (CLog.isDebug()) {
            CLog.log(logTag, "roughRecordingTimeInMillis: $roughRecordingTimeInMillis")

        }
        return roughRecordingTimeInMillis

    }

    fun stop() {
        if (CLog.isDebug()) {
            CLog.log(logTag, "Stop called")
        }
        isRecording = false
        isRecordingPaused = false


        try {
            audioRecorder?.stop()
            audioRecorder?.release()

        } catch (e: Exception) {
            if (CLog.isDebug()) {
                CLog.log(logTag, "Error on audioRecorder stop. Safely ignore")
            }
            CLog.logPrintStackTrace(e)
        }

        try {
            mediaCodecAdapter.shutdown()

        } catch (e: Exception) {
            if (CLog.isDebug()) {
                CLog.log(logTag, "Error on mediaCodec stop. Safely ignore")
            }
            CLog.logPrintStackTrace(e)
        }

        try {
            mediaMuxer.stop()
            mediaMuxer.release()
        } catch (e: Exception) {
            if (CLog.isDebug()) {
                CLog.log(logTag, "Error on mediaMuxer stop. Safely ignore")
            }
            CLog.logPrintStackTrace(e)
        }

        listener.onEncoderStop(this)
    }

}
