package com.dna.jopt.touroptimizer.java.examples.expert.openassessor.routelevel.custom;
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
import com.dna.jopt.assessment.costassessorrestriction.routelevel.custom.ICustomRouteLevelRestriction;
import com.dna.jopt.framework.body.IOptimization;
import com.dna.jopt.framework.body.scheme.DefaultOptimizationScheme;

public class OpenCostAssessorOptimizationSchemeWithOddNumberOfElementsRestriction
    extends DefaultOptimizationScheme {

  public OpenCostAssessorOptimizationSchemeWithOddNumberOfElementsRestriction(
      IOptimization optimization) {
    super(optimization);
  }

  @Override
  public void postCreate() {

    this.getOptimization().getPropertyProvider();

    ICustomRouteLevelRestriction restiction =
        new CustomRouteWithOddNumberOfElementsRestriction(
            this.getOptimization().getPropertyProvider());

    this.attachCustomRouteLevelRestriction(restiction);
  }
}
