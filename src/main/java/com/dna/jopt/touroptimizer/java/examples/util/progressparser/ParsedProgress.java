package com.dna.jopt.touroptimizer.java.examples.util.progressparser;

import static tech.units.indriya.unit.Units.METRE;

import java.time.Duration;

import javax.measure.Quantity;
import javax.measure.quantity.Length;

import com.dna.jopt.framework.outcomewrapper.IOptimizationProgress;
import com.dna.jopt.member.bucket.entity.IEntity;

import tech.units.indriya.quantity.Quantities;

/**
 * The Class ParsedProgress helps to parse an existing Progress object received by the Optimizer.
 *
 * @author jrich
 * @version Mar 26, 2021
 * @since Dec 15, 2020
 */
public class ParsedProgress {

  /** The current optimization progress. */
  private final IOptimizationProgress curOptiProgress;
  
  /** The full route time millis excluding flex time. */
  private final long fullRouteTimeMillisExcludingFlexTime;
  
  /** The winning best Entity. */
  private final IEntity winner;

  /**
   * Instantiates a new parsed progress object from an existing progress object.
   *
   * @param p the optimizationProgress
   */
  public ParsedProgress(IOptimizationProgress p) {

    if (p == null) {
      throw new IllegalStateException("IOptimizationProgress cannot be null");
    }

    this.curOptiProgress = p;

    this.winner = p.getResultEntity();

    this.fullRouteTimeMillisExcludingFlexTime =
        winner.getJoinedDetailController().getCurTransitTime()
            + winner.getJoinedDetailController().getCurIdleTime()
            + winner.getJoinedDetailController().getCurProductiveTime();
  }

  /**
   * Gets the progress of the currently running algorithm.
   *
   * @return the progress
   */
  public double getProgress() {
    return this.curOptiProgress.getProgress();
  }

  /**
   * Gets the caller id of the currently running algorithm.
   *
   * @return the caller id
   */
  public String getCallerId() {
    return this.curOptiProgress.getCallerId();
  }

  /**
   * Gets the current total abstract cost of the solution.
   *
   * @return the cost
   */
  public double getCost() {
    return this.winner.getJoinedCost();
  }

  /**
   * Gets the number of Routes of the solution.
   *
   * @return the route count
   */
  public int getRouteCount() {
    return this.winner.getRouteCount();
  }

  /**
   * Gets the optimizable Elements count.
   *
   * @return the optimizable Elements count
   */
  public int getOptimizableElementsCount() {
    return this.winner.getTotalRoutesOptimizableElementsCount();
  }

  /**
   * Gets the Elements count (e.g. Resources + optimizable Elements).
   *
   * @return the elements count
   */
  public int getElementsCount() {
    return this.winner.getTotalRoutesOptimizableElementsCount() + winner.getRouteCount();
  }

  /**
   * Gets the full time time needed by all Routes.
   *
   * @return the time
   */
  public Duration getTime() {
    return Duration.ofMillis(this.fullRouteTimeMillisExcludingFlexTime);
  }

  /**
   * Gets the average utilization of Resource of all Routes.
   *
   * @return the utilization
   */
  public double getUtilization() {

    double utilization = 0.0;

    if (fullRouteTimeMillisExcludingFlexTime > 0) {
      utilization =
          (double) (this.winner.getJoinedDetailController().getCurProductiveTime())
              / ((double) fullRouteTimeMillisExcludingFlexTime);
    }

    return utilization;
  }

  /**
   * Gets the total distance of all Routes.
   *
   * @return the distance
   */
  public Quantity<Length> getDistance() {

    return Quantities.getQuantity(this.winner.getJoinedDetailController().getCurDistance(), METRE);
  }

  /*
   *
   */

  /**
   * Gets the total productive time of all Routes.
   *
   * @return the productive time
   */
  public Duration getProductiveTime() {
    return Duration.ofMillis(this.winner.getJoinedDetailController().getCurProductiveTime());
  }

  /**
   * Gets the total idle time  of all Routes.
   *
   * @return the idle time
   */
  public Duration getIdleTime() {
    return Duration.ofMillis(this.winner.getJoinedDetailController().getCurIdleTime());
  }

  /**
   * Gets the total flex-time of all Routes.
   *
   * @return the flex time
   */
  public Duration getFlexTime() {
    return Duration.ofMillis(this.winner.getJoinedDetailController().getCurFlexTimeUsage());
  }

  /**
   * Gets the total transit time of all Routes.
   *
   * @return the transit time
   */
  public Duration getTransitTime() {
    return Duration.ofMillis(this.winner.getJoinedDetailController().getCurTransitTime());
  }

  /**
   * Gets the total termination transit time  of all Routes.
   *
   * @return the termination transit time
   */
  public Duration getTerminationTransitTime() {
    return Duration.ofMillis(this.winner.getJoinedDetailController().getTerminationTransitTime());
  }

  /**
   * Gets the total termination transit distance of all Routes.
   *
   * @return the termination transit distance
   */
  public Quantity<Length> getTerminationTransitDistance() {
    return Quantities.getQuantity(
        this.winner.getJoinedDetailController().getTerminationTransitDistance(), METRE);
  }
}
