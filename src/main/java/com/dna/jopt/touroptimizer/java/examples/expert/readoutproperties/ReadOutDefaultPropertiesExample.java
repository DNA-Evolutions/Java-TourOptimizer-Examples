package com.dna.jopt.touroptimizer.java.examples.expert.readoutproperties;
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
import java.util.List;
import java.util.stream.Collectors;

import com.dna.jopt.framework.body.IOptimization;
import com.dna.jopt.framework.body.Optimization;
import com.dna.jopt.framework.inputplausibility.properties.validation.PropertyItem;

/**
 * Prints the property catalog of the JOpt core: the category, description, key, default value and
 * allowed values of each property.
 *
 * <p>Scenario: no optimization runs. The example reads the property items of a new optimization
 * and prints them sorted by category.
 *
 * <p>Look for: one block per property, from "Category:" to "Allowed Values:", and the line
 * "-== DEPRECATED PROPERTY ==-" above each property of the deprecated category.
 *
 * <p>Prerequisites: None: the example runs no optimization and needs no license.
 *
 * @see <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/readoutproperties/ReadOutDefaultProperties.md">ReadOutDefaultProperties.md</a>
 */
public class ReadOutDefaultPropertiesExample {

  /**
   * The names of the property categories, in the order of their numbers in PropertyItem
   * (CATEGORY_MISC is 0, CATEGORY_INJECTION is 11).
   */
  private static final List<String> CATEGORIES =
      List.of(
          "CATEGORY_MISC",
          "CATEGORY_SYSTEM",
          "CATEGORY_OPTIMIZATION_GENERAL_SETUP",
          "CATEGORY_OPTIMIZATION_CONSTRUCTION",
          "CATEGORY_OPTIMIZATION_PRE_OPTIMIZATION_SETUP",
          "CATEGORY_OPTIMIZATION_GENETIC_SETUP",
          "CATEGORY_OPTIMIZATION_2OPT",
          "CATEGORY_OPTIMIZATION_WEIGHTS",
          "CATEGORY_OPTIMIZATION_AUTOFILTER",
          "CATEGORY_OPTIMIZATION_CO_SETUP",
          "CATEGORY_OPTIMIZATION_DEPRECATED",
          "CATEGORY_INJECTION");

  /**
   * Runs the example.
   *
   * @param args not used
   */
  public static void main(String[] args) {
    new ReadOutDefaultPropertiesExample().example();
  }

  @Override
  public String toString() {
    return "Prints the property catalog of the JOpt core: the category, description, key, default"
        + " value and allowed values of each property.";
  }

  /** Reads the property catalog of a new optimization and prints it. */
  public void example() {
    // A new optimization holds the property catalog with the default values; nothing runs.
    IOptimization myDummyOptimization = new Optimization();
    printPropertyItems(myDummyOptimization);
  }

  /**
   * Prints every property item of the optimization, sorted by category.
   *
   * @param opti the optimization
   */
  private static void printPropertyItems(IOptimization opti) {
    // Sorted by category, so that the properties of one category stand together.
    List<PropertyItem> categorySortedPropItems =
        opti.getPropertyProvider().getPropertyItems().stream()
            .sorted((o1, o2) -> Integer.compare(o1.getCategoryIdent(), o2.getCategoryIdent()))
            .collect(Collectors.toList());

    categorySortedPropItems.stream()
        .forEach(
            i -> {
              if (i.getCategoryIdent() == PropertyItem.CATEGORY_OPTIMIZATION_DEPRECATED) {
                System.out.print("\n-== DEPRECATED PROPERTY ==-");
              } else {
                System.out.print("\n");
              }

              System.out.print(
                  "\nCategory: "
                      + CATEGORIES.get(i.getCategoryIdent())
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
}
