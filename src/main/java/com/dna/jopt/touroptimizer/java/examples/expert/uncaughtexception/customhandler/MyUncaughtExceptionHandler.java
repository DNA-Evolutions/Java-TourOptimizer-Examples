package com.dna.jopt.touroptimizer.java.examples.expert.uncaughtexception.customhandler;
/*-
 * #%L
 * JOpt TourOptimizer Examples
 * %%
 * Copyright (C) 2017 - 2023 DNA Evolutions GmbH
 * %%
 * This file is subject to the terms and conditions defined in file 'src/main/resources/LICENSE.txt',
 * which is part of this repository.
 *
 * If not, see <https://www.dna-evolutions.com/>.
 * #L%
 */
import com.dna.jopt.framework.body.IOptimization;
import com.dna.jopt.framework.exception.uncaught.JOptUncaughtExceptionHandler;

/**
 * An uncaught exception handler that prints where an exception came from and completes the result
 * future of the attached optimization exceptionally, so that the wait for the result ends with the
 * exception.
 *
 * <p>Set it with {@code IOptimizationSetup.setSlaveUncaughtExceptionHandler}: JOpt then calls it
 * from its own handler, after that handler has logged the exception and streamed the error event,
 * and attaches the optimization and the error streamer to it.
 *
 * @see <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/uncaughtexception/UncaughtExceptionHandling.md">UncaughtExceptionHandling.md</a>
 */
public class MyUncaughtExceptionHandler extends JOptUncaughtExceptionHandler {

  /** Creates the handler. */
  public MyUncaughtExceptionHandler() {
    super();
  }

  @Override
  public void uncaughtException(Thread t, Throwable e) {

    // Exceptions can arrive from several threads at the same time.
    synchronized (this) {
      String message = "MyUncaughtExceptionHandler - Uncaught exception in " + t.getName();

      if (this.getErrorStreamer() != null) {
        System.out.println("We found an error: " + message);
      } else {
        System.out.println("No error streamer attached: " + message);
      }

      this.completeResultExceptionally(t, e);
    }
  }

  private void completeResultExceptionally(Thread t, Throwable e) {
    if (this.getAttachedOptimization().isPresent()) {
      System.out.println(
          "Completing the result future exceptionally after the exception in thread: "
              + t.getName());

      IOptimization opti = this.getAttachedOptimization().get();
      opti.getOptimizationEvents().resultFuture().completeExceptionally(e);

    } else {
      // Without an attached optimization there is no result future to complete.
      System.out.println("No optimization attached: the result future stays open.");
    }
  }
}
