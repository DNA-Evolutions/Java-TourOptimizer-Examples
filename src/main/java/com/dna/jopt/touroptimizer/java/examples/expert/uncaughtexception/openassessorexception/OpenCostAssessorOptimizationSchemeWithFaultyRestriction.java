package com.dna.jopt.touroptimizer.java.examples.expert.uncaughtexception.openassessorexception;
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
import com.dna.jopt.assessment.costassessorrestriction.nodelevel.custom.ICustomNodeLevelRestriction;
import com.dna.jopt.framework.body.IOptimization;
import com.dna.jopt.framework.body.scheme.DefaultOptimizationScheme;

/**
 * The default optimization scheme with FaultyInjectedRestriction attached as a custom node-level
 * restriction.
 *
 * @see <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/uncaughtexception/UncaughtExceptionHandling.md">UncaughtExceptionHandling.md</a>
 */
public class OpenCostAssessorOptimizationSchemeWithFaultyRestriction
    extends DefaultOptimizationScheme {

  /**
   * Creates the scheme for an optimization.
   *
   * @param optimization the optimization that uses the scheme
   */
  public OpenCostAssessorOptimizationSchemeWithFaultyRestriction(IOptimization optimization) {
    super(optimization);
  }

  @Override
  public void postCreate() {
    // postCreate runs when the scheme has created its algorithms and its cost assessor, so the
    // restriction can be attached to the cost assessor here.
    ICustomNodeLevelRestriction restriction =
        new FaultyInjectedRestriction(this.getOptimization().getPropertyProvider());

    this.attachCustomNodeLevelRestriction(restriction);
  }
}
