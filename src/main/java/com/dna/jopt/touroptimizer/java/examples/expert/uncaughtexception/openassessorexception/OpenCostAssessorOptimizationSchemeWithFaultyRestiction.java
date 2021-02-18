package com.dna.jopt.touroptimizer.java.examples.expert.uncaughtexception.openassessorexception;
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

import com.dna.jopt.assessment.costassessorrestriction.nodelevel.custom.ICustomNodeLevelRestriction;
import com.dna.jopt.framework.body.IOptimization;
import com.dna.jopt.framework.body.scheme.DefaultOptimizationScheme;

public class OpenCostAssessorOptimizationSchemeWithFaultyRestiction extends DefaultOptimizationScheme {

  public OpenCostAssessorOptimizationSchemeWithFaultyRestiction(IOptimization optimization) {
    super(optimization);
  }

  @Override
  public void postCreate() {
	  
	  this.getOptimization().getPropertyProvider();

    ICustomNodeLevelRestriction restiction =
        new FaultyInjectedRestriction(this.getOptimization().getPropertyProvider());

    this.attachCustomNodeLevelRestriction(restiction);
  }
}
