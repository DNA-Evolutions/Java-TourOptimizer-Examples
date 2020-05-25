package com.dna.jopt.touroptimizer.java.examples;
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
import java.io.File;
import java.io.IOException;

import com.dna.jopt.framework.body.IOptimization;

public class ExampleLicenseHelper {

  private static final String LICENSE_PATH = "src/main/resources/YOUR_LICENSE.jli";

  private ExampleLicenseHelper() {
    // Nothing to do
  }

  public static boolean setLicense(IOptimization opti) throws IOException {


    File myLicFile = new File(ExampleLicenseHelper.LICENSE_PATH);
    
    // Check that file exists, otherwise use free mode implicitly by not setting any license
    if (myLicFile.exists()) {
      opti.setLicenseJSON(myLicFile);
    }

    return true;
  }
}
