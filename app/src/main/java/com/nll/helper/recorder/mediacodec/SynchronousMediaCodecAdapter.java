/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nll.helper.recorder.mediacodec;

import android.media.MediaCodec;
import android.media.MediaCrypto;
import android.media.MediaFormat;
import android.view.Surface;

import androidx.annotation.Nullable;

/**
 * A {@link MediaCodecAdapter} that operates the underlying {@link MediaCodec} in synchronous mode.
 */
/* package */ final class SynchronousMediaCodecAdapter implements com.nll.helper.recorder.mediacodec.MediaCodecAdapter {

  private final MediaCodec codec;

  public SynchronousMediaCodecAdapter(MediaCodec mediaCodec) {
    this.codec = mediaCodec;
  }

  @Override
  public void configure(
      @Nullable MediaFormat mediaFormat,
      @Nullable Surface surface,
      @Nullable MediaCrypto crypto,
      int flags) {
    codec.configure(mediaFormat, surface, crypto, flags);
  }

  @Override
  public void start() {
    codec.start();
  }

  @Override
  public int dequeueInputBufferIndex() {
    return codec.dequeueInputBuffer(0);
  }

  @Override
  public int dequeueOutputBufferIndex(MediaCodec.BufferInfo bufferInfo) {
    return codec.dequeueOutputBuffer(bufferInfo, 0);
  }

  @Override
  public MediaFormat getOutputFormat() {
    return codec.getOutputFormat();
  }

  @Override
  public void queueInputBuffer(
      int index, int offset, int size, long presentationTimeUs, int flags) {
    codec.queueInputBuffer(index, offset, size, presentationTimeUs, flags);
  }

  @Override
  public void flush() {
    codec.flush();
  }

  @Override
  public void shutdown() {}

  @Override
  public MediaCodec getCodec() {
    return codec;
  }
}
