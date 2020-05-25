package com.dna.jopt.touroptimizer.java.examples.expert.uncaughtexception;
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

public class FaultyInjectedRestriction
    extends AbstractCustomNodeLevelRestriction {

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

    // This example simply triggers a java.lang.ArithmeticException: / by zero at some point
	int randomNum = ThreadLocalRandom.current().nextInt(0, 1000);
	if(randomNum == 0) {
		throw new ArithmeticException(); 
	}

    // Create the result
    EntityRestrictionResult myRestrictionResult = new EntityRestrictionResult();

    return myRestrictionResult;
  }

}
