package com.nll.helper.server

enum class RecorderError {
    AudioRecordInUse,
    MediaCodecQueueInputBufferFailed,
    AudioRecordReadFailed,
    EmptyInputBuffer,
    MediaMuxerWriteFailed,
    MediaRecorderError,
    MediaCodecException,
    RemoteError
}