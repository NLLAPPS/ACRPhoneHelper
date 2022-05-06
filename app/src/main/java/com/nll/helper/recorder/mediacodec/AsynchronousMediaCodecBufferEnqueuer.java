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

import android.media.MediaCodec;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import androidx.annotation.GuardedBy;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;


/**
 * A {@link MediaCodecInputBufferEnqueuer} that defers queueing operations on a background thread.
 *
 * <p>The implementation of this class assumes that its public methods will be called from the same
 * thread.
 */
class AsynchronousMediaCodecBufferEnqueuer implements com.nll.helper.recorder.mediacodec.MediaCodecInputBufferEnqueuer {

  private static final int MSG_QUEUE_INPUT_BUFFER = 0;
  private static final int MSG_FLUSH = 2;

  @GuardedBy("MESSAGE_PARAMS_INSTANCE_POOL")
  private static final ArrayDeque<MessageParams> MESSAGE_PARAMS_INSTANCE_POOL = new ArrayDeque<>();

  private static final Object QUEUE_SECURE_LOCK = new Object();

  private final MediaCodec codec;
  private final HandlerThread handlerThread;
  private  Handler handler;
  private final AtomicReference<RuntimeException> pendingRuntimeException;
  private final ConditionVariable conditionVariable;
  private boolean started;

  /**
   * Creates a new instance that submits input buffers on the specified {@link MediaCodec}.
   *
   * @param codec The {@link MediaCodec} to submit input buffers to.
   */
  public AsynchronousMediaCodecBufferEnqueuer(MediaCodec codec) {
    this(
        codec,
        new HandlerThread(createThreadLabel()),
        /* conditionVariable= */ new ConditionVariable());
  }

  @VisibleForTesting
  /* package */ AsynchronousMediaCodecBufferEnqueuer(
      MediaCodec codec, HandlerThread handlerThread, ConditionVariable conditionVariable) {
    this.codec = codec;
    this.handlerThread = handlerThread;
    this.conditionVariable = conditionVariable;
    pendingRuntimeException = new AtomicReference<>();
  }

  @Override
  public void start() {
    if (!started) {
      handlerThread.start();
      handler =
          new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
              doHandleMessage(msg);
            }
          };
      started = true;
    }
  }

  @Override
  public void queueInputBuffer(
      int index, int offset, int size, long presentationTimeUs, int flags) {
    maybeThrowException();
    MessageParams messageParams = getMessageParams();
    messageParams.setQueueParams(index, offset, size, presentationTimeUs, flags);
    Message message =        handler.obtainMessage(MSG_QUEUE_INPUT_BUFFER, messageParams);
    message.sendToTarget();
  }



  @Override
  public void flush() {
    if (started) {
      try {
        flushHandlerThread();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        // The playback thread should not be interrupted. Raising this as an
        // IllegalStateException.
        throw new IllegalStateException(e);
      }
    }
  }

  @Override
  public void shutdown() {
    if (started) {
      flush();
      handlerThread.quit();
    }
    started = false;
  }

  private void doHandleMessage(Message msg) {
    MessageParams params = null;
    switch (msg.what) {
      case MSG_QUEUE_INPUT_BUFFER:
        params = (MessageParams) msg.obj;
        doQueueInputBuffer(
            params.index, params.offset, params.size, params.presentationTimeUs, params.flags);
        break;
      case MSG_FLUSH:
        conditionVariable.open();
        break;
      default:
        setPendingRuntimeException(new IllegalStateException(String.valueOf(msg.what)));
    }
    if (params != null) {
      recycleMessageParams(params);
    }
  }

  private void maybeThrowException() {
    RuntimeException exception = pendingRuntimeException.getAndSet(null);
    if (exception != null) {
      throw exception;
    }
  }

  /**
   * Empties all tasks enqueued on the {@link #handlerThread} via the {@link #handler}. This method
   * blocks until the {@link #handlerThread} is idle.
   */
  private void flushHandlerThread() throws InterruptedException {
    Handler handler = this.handler;
    handler.removeCallbacksAndMessages(null);
    conditionVariable.close();
    handler.obtainMessage(MSG_FLUSH).sendToTarget();
    conditionVariable.block();
    // Check if any exceptions happened during the last queueing action.
    maybeThrowException();
  }

  // Called from the handler thread

  @VisibleForTesting
  /* package */ void setPendingRuntimeException(RuntimeException exception) {
    pendingRuntimeException.set(exception);
  }

  private void doQueueInputBuffer(
      int index, int offset, int size, long presentationTimeUs, int flag) {
    try {
      codec.queueInputBuffer(index, offset, size, presentationTimeUs, flag);
    } catch (RuntimeException e) {
      setPendingRuntimeException(e);
    }
  }



  @VisibleForTesting
  /* package */ static int getInstancePoolSize() {
    synchronized (MESSAGE_PARAMS_INSTANCE_POOL) {
      return MESSAGE_PARAMS_INSTANCE_POOL.size();
    }
  }

  private static MessageParams getMessageParams() {
    synchronized (MESSAGE_PARAMS_INSTANCE_POOL) {
      if (MESSAGE_PARAMS_INSTANCE_POOL.isEmpty()) {
        return new MessageParams();
      } else {
        return MESSAGE_PARAMS_INSTANCE_POOL.removeFirst();
      }
    }
  }

  private static void recycleMessageParams(MessageParams params) {
    synchronized (MESSAGE_PARAMS_INSTANCE_POOL) {
      MESSAGE_PARAMS_INSTANCE_POOL.add(params);
    }
  }

  /** Parameters for queue input buffer and queue secure input buffer tasks. */
  private static class MessageParams {
    public int index;
    public int offset;
    public int size;
    public final MediaCodec.CryptoInfo cryptoInfo;
    public long presentationTimeUs;
    public int flags;

    MessageParams() {
      cryptoInfo = new MediaCodec.CryptoInfo();
    }

    /** Convenience method for setting the queueing parameters. */
    public void setQueueParams(
        int index, int offset, int size, long presentationTimeUs, int flags) {
      this.index = index;
      this.offset = offset;
      this.size = size;
      this.presentationTimeUs = presentationTimeUs;
      this.flags = flags;
    }
  }

  private static String createThreadLabel() {

    return "MediaCodecCallBackHandlerThread";
  }


  /**
   * Copies {@code src}, reusing {@code dst} if it's at least as long as {@code src}.
   *
   * @param src The source array.
   * @param dst The destination array, which will be reused if it's at least as long as {@code src}.
   * @return The copy, which may be {@code dst} if it was reused.
   */
  @Nullable
  private static int[] copy(@Nullable int[] src, @Nullable int[] dst) {
    if (src == null) {
      return dst;
    }

    if (dst == null || dst.length < src.length) {
      return Arrays.copyOf(src, src.length);
    } else {
      System.arraycopy(src, 0, dst, 0, src.length);
      return dst;
    }
  }

  /**
   * Copies {@code src}, reusing {@code dst} if it's at least as long as {@code src}.
   *
   * @param src The source array.
   * @param dst The destination array, which will be reused if it's at least as long as {@code src}.
   * @return The copy, which may be {@code dst} if it was reused.
   */
  @Nullable
  private static byte[] copy(@Nullable byte[] src, @Nullable byte[] dst) {
    if (src == null) {
      return dst;
    }

    if (dst == null || dst.length < src.length) {
      return Arrays.copyOf(src, src.length);
    } else {
      System.arraycopy(src, 0, dst, 0, src.length);
      return dst;
    }
  }
}
