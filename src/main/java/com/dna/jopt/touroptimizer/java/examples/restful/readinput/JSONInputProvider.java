package com.dna.jopt.touroptimizer.java.examples.restful.readinput;

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

public class JSONInputProvider {

    private JSONInputProvider() {
	// Nothing to do
    }

    public static final String JSON_INOUT_WITHOUT_SOLUTION = "{\r\n"
    	+ "  \"optimizationStatus\" : {\r\n"
    	+ "    \"statusDescription\" : \"SUCCESS_WITHOUT_SOLUTION\",\r\n"
    	+ "    \"error\" : \"NO_ERROR\",\r\n"
    	+ "    \"status\" : \"SUCCESS_WITHOUT_SOLUTION\"\r\n"
    	+ "  },\r\n"
    	+ "  \"createdTimeStamp\" : 1699518009548,\r\n"
    	+ "  \"creator\" : \"DEFAULT_CREATOR\",\r\n"
    	+ "  \"ident\" : \"MyJOptRun\",\r\n"
    	+ "  \"nodes\" : [ {\r\n"
    	+ "    \"id\" : \"Koeln\",\r\n"
    	+ "    \"type\" : {\r\n"
    	+ "      \"position\" : {\r\n"
    	+ "        \"latitude\" : 50.9333,\r\n"
    	+ "        \"longitude\" : 6.95,\r\n"
    	+ "        \"locationId\" : \"Koeln\"\r\n"
    	+ "      },\r\n"
    	+ "      \"typeName\" : \"Geo\"\r\n"
    	+ "    },\r\n"
    	+ "    \"openingHours\" : [ {\r\n"
    	+ "      \"begin\" : \"2020-05-06T06:00:00Z\",\r\n"
    	+ "      \"end\" : \"2020-05-06T15:00:00Z\",\r\n"
    	+ "      \"zoneId\" : \"Europe/Berlin\"\r\n"
    	+ "    }, {\r\n"
    	+ "      \"begin\" : \"2020-05-07T06:00:00Z\",\r\n"
    	+ "      \"end\" : \"2020-05-07T15:00:00Z\",\r\n"
    	+ "      \"zoneId\" : \"Europe/Berlin\"\r\n"
    	+ "    } ],\r\n"
    	+ "    \"visitDuration\" : \"PT20M\",\r\n"
    	+ "    \"priority\" : 1\r\n"
    	+ "  }, {\r\n"
    	+ "    \"id\" : \"Essen\",\r\n"
    	+ "    \"type\" : {\r\n"
    	+ "      \"position\" : {\r\n"
    	+ "        \"latitude\" : 51.45,\r\n"
    	+ "        \"longitude\" : 7.01667,\r\n"
    	+ "        \"locationId\" : \"Essen\"\r\n"
    	+ "      },\r\n"
    	+ "      \"typeName\" : \"Geo\"\r\n"
    	+ "    },\r\n"
    	+ "    \"openingHours\" : [ {\r\n"
    	+ "      \"begin\" : \"2020-05-06T06:00:00Z\",\r\n"
    	+ "      \"end\" : \"2020-05-06T15:00:00Z\",\r\n"
    	+ "      \"zoneId\" : \"Europe/Berlin\"\r\n"
    	+ "    }, {\r\n"
    	+ "      \"begin\" : \"2020-05-07T06:00:00Z\",\r\n"
    	+ "      \"end\" : \"2020-05-07T15:00:00Z\",\r\n"
    	+ "      \"zoneId\" : \"Europe/Berlin\"\r\n"
    	+ "    } ],\r\n"
    	+ "    \"visitDuration\" : \"PT20M\",\r\n"
    	+ "    \"priority\" : 1\r\n"
    	+ "  }, {\r\n"
    	+ "    \"id\" : \"Dueren\",\r\n"
    	+ "    \"type\" : {\r\n"
    	+ "      \"position\" : {\r\n"
    	+ "        \"latitude\" : 50.8,\r\n"
    	+ "        \"longitude\" : 6.48333,\r\n"
    	+ "        \"locationId\" : \"Dueren\"\r\n"
    	+ "      },\r\n"
    	+ "      \"typeName\" : \"Geo\"\r\n"
    	+ "    },\r\n"
    	+ "    \"openingHours\" : [ {\r\n"
    	+ "      \"begin\" : \"2020-05-06T06:00:00Z\",\r\n"
    	+ "      \"end\" : \"2020-05-06T15:00:00Z\",\r\n"
    	+ "      \"zoneId\" : \"Europe/Berlin\"\r\n"
    	+ "    }, {\r\n"
    	+ "      \"begin\" : \"2020-05-07T06:00:00Z\",\r\n"
    	+ "      \"end\" : \"2020-05-07T15:00:00Z\",\r\n"
    	+ "      \"zoneId\" : \"Europe/Berlin\"\r\n"
    	+ "    } ],\r\n"
    	+ "    \"visitDuration\" : \"PT20M\",\r\n"
    	+ "    \"priority\" : 1\r\n"
    	+ "  }, {\r\n"
    	+ "    \"id\" : \"Nuernberg\",\r\n"
    	+ "    \"type\" : {\r\n"
    	+ "      \"position\" : {\r\n"
    	+ "        \"latitude\" : 49.4478,\r\n"
    	+ "        \"longitude\" : 11.0683,\r\n"
    	+ "        \"locationId\" : \"Nuernberg\"\r\n"
    	+ "      },\r\n"
    	+ "      \"typeName\" : \"Geo\"\r\n"
    	+ "    },\r\n"
    	+ "    \"openingHours\" : [ {\r\n"
    	+ "      \"begin\" : \"2020-05-06T06:00:00Z\",\r\n"
    	+ "      \"end\" : \"2020-05-06T15:00:00Z\",\r\n"
    	+ "      \"zoneId\" : \"Europe/Berlin\"\r\n"
    	+ "    }, {\r\n"
    	+ "      \"begin\" : \"2020-05-07T06:00:00Z\",\r\n"
    	+ "      \"end\" : \"2020-05-07T15:00:00Z\",\r\n"
    	+ "      \"zoneId\" : \"Europe/Berlin\"\r\n"
    	+ "    } ],\r\n"
    	+ "    \"visitDuration\" : \"PT20M\",\r\n"
    	+ "    \"priority\" : 1\r\n"
    	+ "  }, {\r\n"
    	+ "    \"id\" : \"Heilbronn\",\r\n"
    	+ "    \"type\" : {\r\n"
    	+ "      \"position\" : {\r\n"
    	+ "        \"latitude\" : 49.1403,\r\n"
    	+ "        \"longitude\" : 9.22,\r\n"
    	+ "        \"locationId\" : \"Heilbronn\"\r\n"
    	+ "      },\r\n"
    	+ "      \"typeName\" : \"Geo\"\r\n"
    	+ "    },\r\n"
    	+ "    \"openingHours\" : [ {\r\n"
    	+ "      \"begin\" : \"2020-05-06T06:00:00Z\",\r\n"
    	+ "      \"end\" : \"2020-05-06T15:00:00Z\",\r\n"
    	+ "      \"zoneId\" : \"Europe/Berlin\"\r\n"
    	+ "    }, {\r\n"
    	+ "      \"begin\" : \"2020-05-07T06:00:00Z\",\r\n"
    	+ "      \"end\" : \"2020-05-07T15:00:00Z\",\r\n"
    	+ "      \"zoneId\" : \"Europe/Berlin\"\r\n"
    	+ "    } ],\r\n"
    	+ "    \"visitDuration\" : \"PT20M\",\r\n"
    	+ "    \"priority\" : 1\r\n"
    	+ "  }, {\r\n"
    	+ "    \"id\" : \"Wuppertal\",\r\n"
    	+ "    \"type\" : {\r\n"
    	+ "      \"position\" : {\r\n"
    	+ "        \"latitude\" : 51.2667,\r\n"
    	+ "        \"longitude\" : 7.18333,\r\n"
    	+ "        \"locationId\" : \"Wuppertal\"\r\n"
    	+ "      },\r\n"
    	+ "      \"typeName\" : \"Geo\"\r\n"
    	+ "    },\r\n"
    	+ "    \"openingHours\" : [ {\r\n"
    	+ "      \"begin\" : \"2020-05-06T06:00:00Z\",\r\n"
    	+ "      \"end\" : \"2020-05-06T15:00:00Z\",\r\n"
    	+ "      \"zoneId\" : \"Europe/Berlin\"\r\n"
    	+ "    }, {\r\n"
    	+ "      \"begin\" : \"2020-05-07T06:00:00Z\",\r\n"
    	+ "      \"end\" : \"2020-05-07T15:00:00Z\",\r\n"
    	+ "      \"zoneId\" : \"Europe/Berlin\"\r\n"
    	+ "    } ],\r\n"
    	+ "    \"visitDuration\" : \"PT20M\",\r\n"
    	+ "    \"priority\" : 1\r\n"
    	+ "  }, {\r\n"
    	+ "    \"id\" : \"Aachen\",\r\n"
    	+ "    \"type\" : {\r\n"
    	+ "      \"position\" : {\r\n"
    	+ "        \"latitude\" : 50.775346,\r\n"
    	+ "        \"longitude\" : 6.083887,\r\n"
    	+ "        \"locationId\" : \"Aachen\"\r\n"
    	+ "      },\r\n"
    	+ "      \"typeName\" : \"Geo\"\r\n"
    	+ "    },\r\n"
    	+ "    \"openingHours\" : [ {\r\n"
    	+ "      \"begin\" : \"2020-05-06T06:00:00Z\",\r\n"
    	+ "      \"end\" : \"2020-05-06T15:00:00Z\",\r\n"
    	+ "      \"zoneId\" : \"Europe/Berlin\"\r\n"
    	+ "    }, {\r\n"
    	+ "      \"begin\" : \"2020-05-07T06:00:00Z\",\r\n"
    	+ "      \"end\" : \"2020-05-07T15:00:00Z\",\r\n"
    	+ "      \"zoneId\" : \"Europe/Berlin\"\r\n"
    	+ "    } ],\r\n"
    	+ "    \"visitDuration\" : \"PT20M\",\r\n"
    	+ "    \"priority\" : 1\r\n"
    	+ "  } ],\r\n"
    	+ "  \"resources\" : [ {\r\n"
    	+ "    \"id\" : \"Jack from Aachen\",\r\n"
    	+ "    \"type\" : {\r\n"
    	+ "      \"typeName\" : \"Capacity\"\r\n"
    	+ "    },\r\n"
    	+ "    \"position\" : {\r\n"
    	+ "      \"latitude\" : 50.775346,\r\n"
    	+ "      \"longitude\" : 6.083887,\r\n"
    	+ "      \"locationId\" : \"Jack from Aachen\"\r\n"
    	+ "    },\r\n"
    	+ "    \"workingHours\" : [ {\r\n"
    	+ "      \"begin\" : \"2020-05-06T06:00:00Z\",\r\n"
    	+ "      \"end\" : \"2020-05-06T15:00:00Z\",\r\n"
    	+ "      \"zoneId\" : \"Europe/Berlin\",\r\n"
    	+ "      \"isAvailableForStay\" : false\r\n"
    	+ "    }, {\r\n"
    	+ "      \"begin\" : \"2020-05-07T06:00:00Z\",\r\n"
    	+ "      \"end\" : \"2020-05-07T15:00:00Z\",\r\n"
    	+ "      \"zoneId\" : \"Europe/Berlin\",\r\n"
    	+ "      \"isAvailableForStay\" : false\r\n"
    	+ "    } ],\r\n"
    	+ "    \"maxTime\" : \"PT9H\",\r\n"
    	+ "    \"maxDistance\" : \"1200.0 km\",\r\n"
    	+ "    \"co2emissionFactor\" : 0.377\r\n"
    	+ "  } ],\r\n"
    	+ "  \"extension\" : {\r\n"
    	+ "    \"keySetting\" : {\r\n"
    	+ "      \"jsonLicense\" : \"{\\r\\n\\t\\\"version\\\": \\\"1.1\\\",\\r\\n\\t\\\"identifier\\\": \\\"PUBLIC-\\\",\\r\\n\\t\\\"description\\\": \\\"Key provided to for evaluation purpose from DNA evolutions GmbH.\\\",\\r\\n\\t\\\"contact\\\": \\\"www.dna-evolutions.com\\\",\\r\\n\\t\\\"modules\\\": [{\\r\\n\\t\\t\\t\\\"Module:\\\": \\\"Elements\\\",\\r\\n\\t\\t\\t\\\"max\\\": 15\\r\\n\\t\\t}, {\\r\\n\\t\\t\\t\\\"Module:\\\": \\\"Date\\\",\\r\\n\\t\\t\\t\\\"creation\\\": \\\"2021-05-25\\\",\\r\\n\\t\\t\\t\\\"due\\\": \\\"2027-05-25\\\"\\r\\n\\t\\t}\\r\\n\\t],\\r\\n\\t\\\"key\\\": \\\"PUBLIC-bc799ef350fe9841c1354736d8f863cb85bac88cefd19960c1\\\"\\r\\n}\"\r\n"
    	+ "    },\r\n"
    	+ "    \"timeOut\" : \"PT10M\"\r\n"
    	+ "  }\r\n"
    	+ "}";
}