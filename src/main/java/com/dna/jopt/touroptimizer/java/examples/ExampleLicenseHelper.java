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

    public static final String PUBLIC_JSON_LICENSE = "{\r\n" + "	\"version\": \"1.1\",\r\n"
	    + "	\"identifier\": \"PUBLIC-\",\r\n"
	    + "	\"description\": \"Key provided to for evaluation purpose from DNA evolutions GmbH.\",\r\n"
	    + "	\"contact\": \"www.dna-evolutions.com\",\r\n" + "	\"modules\": [{\r\n"
	    + "			\"Module:\": \"Elements\",\r\n" + "			\"max\": 15\r\n"
	    + "		}, {\r\n" + "			\"Module:\": \"Date\",\r\n"
	    + "			\"creation\": \"2021-05-25\",\r\n" + "			\"due\": \"2027-05-25\"\r\n"
	    + "		}\r\n" + "	],\r\n"
	    + "	\"key\": \"PUBLIC-bc799ef350fe9841c1354736d8f863cb85bac88cefd19960c1\"\r\n" + "}";

    private static final String LICENSE_PATH = "src/main/resources/license/YOUR_LICENSE.jli";

    private ExampleLicenseHelper() {
	// Nothing to do
    }

    public static boolean setLicense(IOptimization opti) throws IOException {

	return setLicense(opti, ExampleLicenseHelper.LICENSE_PATH);
    }

    public static boolean setLicense(IOptimization opti, String path) throws IOException {

	File myLicFile = new File(path);

	// Checks whether license file exists, otherwise uses free mode by not setting
	// any license
	if (myLicFile.exists()) {
	    opti.setLicenseJSON(myLicFile);
	}

	return true;
    }
}
