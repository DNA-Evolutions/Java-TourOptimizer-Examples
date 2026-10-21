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
import java.util.concurrent.ThreadLocalRandom;

import com.dna.jopt.assessment.costassessor.EvaluatedNodeDataHolder;
import com.dna.jopt.assessment.costassessor.IEntityCostAssessor;
import com.dna.jopt.assessment.costassessorrestriction.nodelevel.custom.AbstractCustomNodeLevelRestriction;
import com.dna.jopt.assessment.costassessorrestriction.restrictionresult.EntityRestrictionResult;
import com.dna.jopt.assessment.costassessorrestriction.restrictionresult.IEntityRestrictionResult;
import com.dna.jopt.framework.inputplausibility.properties.IPropertyProvider;
import com.dna.jopt.member.bucket.entity.IEntity;
import com.dna.jopt.member.bucket.route.ILogicEntityRoute;
import com.dna.jopt.member.unit.IOptimizationElement;
import com.dna.jopt.member.unit.node.INode;

/**
 * A node-level restriction that throws an ArithmeticException in about one of 1000 calls and
 * otherwise returns an empty result, without cost and without violation.
 *
 * @see <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/uncaughtexception/UncaughtExceptionHandling.md">UncaughtExceptionHandling.md</a>
 */
public class FaultyInjectedRestriction extends AbstractCustomNodeLevelRestriction {

  /**
   * Creates the restriction.
   *
   * @param propertyProvider the property provider of the optimization
   */
  public FaultyInjectedRestriction(IPropertyProvider propertyProvider) {
    super(propertyProvider);
  }

  @Override
  public IEntityRestrictionResult invokeRestriction(
      IEntity en,
      ILogicEntityRoute route,
      INode node,
      IOptimizationElement lastGeoLocationElement,
      IOptimizationElement prevElement,
      EvaluatedNodeDataHolder holder,
      IEntityCostAssessor ca,
      boolean resultRequested) {

    // The fault: JOpt calls the restriction for every node it assesses, so one throw in about
    // 1000 calls ends the run soon, at a point that changes between runs.
    int randomNum = ThreadLocalRandom.current().nextInt(0, 1000);
    if (randomNum == 0) {
      throw new ArithmeticException("Test Exception");
    }

    // An empty result: no cost and no violation.
    return new EntityRestrictionResult();
  }
}
