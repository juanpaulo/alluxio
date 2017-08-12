/*
 * The Alluxio Open Foundation licenses this work under the Apache License, version 2.0
 * (the "License"). You may not use this work except in compliance with the License, which is
 * available at www.apache.org/licenses/LICENSE-2.0
 *
 * This software is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied, as more fully set forth in the License.
 *
 * See the NOTICE file distributed with this work for information regarding copyright ownership.
 */

package alluxio.master.journal;

import com.google.common.base.Preconditions;

import java.io.IOException;

/**
 * Base implementation for journal systems.
 */
public abstract class AbstractJournalSystem implements JournalSystem {
  private volatile Mode mMode = Mode.SECONDARY;
  private boolean mRunning = false;

  @Override
  public void start() throws InterruptedException, IOException {
    startInternal();
    mRunning = true;
  }

  @Override
  public void stop() throws InterruptedException, IOException {
    mRunning = false;
    stopInternal();
  }

  @Override
  public void setMode(Mode mode) {
    Preconditions.checkState(isRunning(),
        "Cannot change journal system mode while it is not running");
    if (mMode.equals(mode)) {
      return;
    }
    switch (mode) {
      case PRIMARY:
        gainPrimacy();
        break;
      case SECONDARY:
        losePrimacy();
        break;
      default:
        throw new IllegalStateException("Unrecognized mode: " + mode);
    }
    mMode = mode;
  }

  /**
   * @return the current mode for the journal system
   */
  protected Mode getMode() {
    return mMode;
  }

  /**
   * @return whether the journal system is currently running
   */
  protected boolean isRunning() {
    return mRunning;
  }

  /**
   * Starts the journal system.
   */
  protected abstract void startInternal() throws InterruptedException, IOException;

  /**
   * Stops the journal system.
   */
  protected abstract void stopInternal() throws InterruptedException, IOException;

  /**
   * Transition the journal from secondary to primary mode.
   */
  protected abstract void gainPrimacy();

  /**
   * Transition the journal from primary to secondary mode.
   */
  protected abstract void losePrimacy();
}
