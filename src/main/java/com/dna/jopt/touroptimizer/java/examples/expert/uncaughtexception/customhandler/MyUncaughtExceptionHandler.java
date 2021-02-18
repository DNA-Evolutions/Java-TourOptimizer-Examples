package com.dna.jopt.touroptimizer.java.examples.expert.uncaughtexception.customhandler;
/*-
 * #%L
 * JOpt TourOptimizer Examples
 * %%
 * Copyright (C) 2017 - 2020 DNA Evolutions GmbH
 * %%
 * This file is subject to the terms and conditions defined in file 'src/main/resources/LICENSE.txt',
 * which is part of this repository.
 * 
 * If not, see <https://www.dna-evolutions.com/>.
 * #L%
 */
import com.dna.jopt.framework.body.IOptimization;
import com.dna.jopt.framework.exception.uncaught.JOptUncaughtExceptionHandler;

public class MyUncaughtExceptionHandler extends JOptUncaughtExceptionHandler {

  public MyUncaughtExceptionHandler() {
    super();
  }

  @Override
  public void uncaughtException(Thread t, Throwable e) {

    synchronized (this) {
      String message = "MyUncaughtExceptionHandler - Uncaught exception in " + t.getName();

      if (this.getErrorStreamer() != null) {

        System.out.println("We found an error: " + message);

      } else {

        System.out.println("Fallback (No erroStreamer present): 1000 " + message);
      }

      // Terminating thread
      this.endFaultyThread(t, e);
    }
  }

  private void endFaultyThread(Thread t, Throwable e) {

    Thread.currentThread().interrupt();

    if (Thread.currentThread().isInterrupted()) {
      // This is quite brute force
      System.out.println(
          "Trying to terminate thread by completing result future exceptionally of thread: "
              + t.getName());

      if (this.getAttachedOptimization().isPresent()) {
        IOptimization opti = this.getAttachedOptimization().get();

        opti.getOptimizationEvents().result.completeExceptionally(e);

      } else {
        System.out.println("No attached opti - Brute force terminating optimization");
        System.exit(0);
      }
    }
  }
}
