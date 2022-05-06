/*
 * Copyright (C) 2020 The Android Open Source Project
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


/** Abstracts operations to enqueue input buffer on a {@link android.media.MediaCodec}. */
interface MediaCodecInputBufferEnqueuer {

  /**
   * Starts this instance.
   *
   * <p>Call this method after creating an instance.
   */
  void start();

  /**
   * Submits an input buffer for decoding.
   *
   * @see android.media.MediaCodec#queueInputBuffer
   */
  void queueInputBuffer(int index, int offset, int size, long presentationTimeUs, int flags);


  /** Flushes the instance. */
  void flush();

  /** Shut down the instance. Make sure to call this method to release its internal resources. */
  void shutdown();
}
