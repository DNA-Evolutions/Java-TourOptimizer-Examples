package com.dna.jopt.touroptimizer.java.examples.advanced.buildinfo;
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
import java.io.IOException;
import java.util.Properties;

import com.dna.jopt.framework.body.Optimization;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;

/** Extracting build info from library.. */
public class ExtractBuildInfoExample extends Optimization {

  public static void main(String[] args) throws IOException {
    new ExtractBuildInfoExample().example();
  }

  public String toString() {
    return "Extracting build info from library.";
  }

  public void example() throws IOException {

    // Set license via helper
    ExampleLicenseHelper.setLicense(this);

    Properties coreVersionProperties = this.getCoreVersionProperties();

    coreVersionProperties
        .entrySet()
        .stream()
        .forEach(e -> System.out.println(e.getKey() + " = " + e.getValue()));
  }
}
