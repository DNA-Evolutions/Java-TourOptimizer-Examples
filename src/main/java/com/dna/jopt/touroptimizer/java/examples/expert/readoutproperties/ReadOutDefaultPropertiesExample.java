package com.dna.jopt.touroptimizer.java.examples.expert.readoutproperties;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.dna.jopt.framework.body.IOptimization;
import com.dna.jopt.framework.body.Optimization;
import com.dna.jopt.framework.inputplausibility.properties.validation.PropertyItem;
import com.dna.jopt.framework.outcomewrapper.IOptimizationProgress;
import com.dna.jopt.framework.outcomewrapper.IOptimizationResult;

/** Reading out the default properties. */
public class ReadOutDefaultPropertiesExample extends Optimization {

  public static void main(String[] args) {
    new ReadOutDefaultPropertiesExample();
    ReadOutDefaultPropertiesExample.example();
  }

  public String toString() {
    return "Reading out the default properties.";
  }

  public class PropertyComparator implements Comparator<PropertyItem> {

    @Override
    public int compare(PropertyItem o1, PropertyItem o2) {
      return Integer.compare(o1.getCategoryIdent(), o2.getCategoryIdent());
    }
  }

  public static void example() {

    List<String> categories = new ArrayList<>();
    categories.add("CATEGORY_MISC");
    categories.add("CATEGORY_SYSTEM");
    categories.add("CATEGORY_OPTIMIZATION_GENERAL_SETUP");
    categories.add("CATEGORY_OPTIMIZATION_CONSTRUCTION");
    categories.add("CATEGORY_OPTIMIZATION_PRE_OPTIMIZATION_SETUP");
    categories.add("CATEGORY_OPTIMIZATION_GENETIC_SETUP");
    categories.add("CATEGORY_OPTIMIZATION_2OPT");
    categories.add("CATEGORY_OPTIMIZATION_WEIGHTS");
    categories.add("CATEGORY_OPTIMIZATION_AUTOFILTER");
    categories.add("CATEGORY_OPTIMIZATION_CO_SETUP");
    categories.add("CATEGORY_OPTIMIZATION_DEPRECATED");
    categories.add("CATEGORY_INJECTION");

    IOptimization myDummyOptimization = new Optimization();

    // Read out current properties
    List<PropertyItem> categorySortedPropItems =
        myDummyOptimization
            .getPropertyProvider()
            .getPropertyItems()
            .stream()
            .sorted((o1, o2) -> Integer.compare(o1.getCategoryIdent(), o2.getCategoryIdent()))
            .collect(Collectors.toList());

    categorySortedPropItems
        .stream()
        .forEach(
            i -> {
              if (i.getCategoryIdent() == PropertyItem.CATEGORY_OPTIMIZATION_DEPRECATED) {
                System.out.print("\n-== DEPRECATED PROPERTY ==-");
              } else {
                System.out.print("\n");
              }

              System.out.print(
                  "\nCategory: "
                      + categories.get(i.getCategoryIdent())
                      + "\nDescription: "
                      + i.getDescription()
                      + "\nDefault Key: "
                      + i.getValidatedPropertyName()
                      + "\nDefault Value: "
                      + i.getDefaultValue()
                      + "\nAllowed Values: "
                      + i.getAllowedValues()
                      + "\n");
            
            });
  }

  @Override
  public void onError(int code, String message) {
    System.out.println("code: " + code + " message:" + message);
  }

  @Override
  public void onStatus(int code, String message) {
    System.out.println("code: " + code + " message:" + message);
  }

  @Override
  public void onWarning(int code, String message) {
    //

  }

  @Override
  public void onProgress(String winnerProgressString) {
    //
  }

  @Override
  public void onProgress(IOptimizationProgress rapoptProgress) {
    System.out.print(".");
    //
  }

  @Override
  public void onAsynchronousOptimizationResult(IOptimizationResult rapoptResult) {
    //
  }
}
