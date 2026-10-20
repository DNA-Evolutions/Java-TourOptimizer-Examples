# Set a license from a file

`SetLicenseExample` sets the JOpt license from the file `src/main/resources/license/YOUR_LICENSE.jli` when that file exists, and the public evaluation license otherwise, and prints which of the two it set. It is for developers who move from the evaluation to their own license. The [license guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license) describes the license JSON and what each license allows.

## What the example shows

A JOpt license is provided as a **JSON definition**. You can set it either:

1. **As a JSON string**, with `setLicenseJSON(String)`, or
2. **As a file containing the JSON**, with `setLicenseJSON(File)`.

`SetLicenseExample` looks for the file `src/main/resources/license/YOUR_LICENSE.jli`, the same path that `ExampleLicenseHelper` uses. If the file exists, the example sets the license from it with `setLicenseJSON(File)`. Otherwise it calls `ExampleLicenseHelper.setLicense`, which sets the public evaluation license as a string. Either way, the example prints which license it set.

Without a license, JOpt allows fewer elements than with the public evaluation license; nodes and resources count as elements. The extended free license is available only with a free [sign-up](https://www.dna-evolutions.com/sign-up) on the DNA Evolutions website. Larger problems need a full license. The [license guide: element limits](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license#free-license) lists the limits.

If you need more elements during your evaluation phase, [contact us](https://www.dna-evolutions.com/contact).

The [license guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license) warns: changing any key of the license JSON (deleting, modifying or adding one) deactivates the license; only formatting, such as prettifying, is allowed.

## Scenario

Jack, who starts in Aachen, visits five nodes: Koeln, Oberhausen, Essen, Dueren and Nuernberg. The nodes are open and Jack works from 08:00 to 17:00 on May 6 and May 7, 2020 (Europe/Berlin); the dates are sample values. Each visit takes 20 minutes, and Jack may work 13 hours and drive 1200 km per day. The example also writes the result to `SetLicenseExample.kml`.

## How the code works

The source is [SetLicenseExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/setlicense_08/SetLicenseExample.java).

### The license path

The path is relative to the working directory:

```java
// The file that ExampleLicenseHelper reads. Git ignores *.jli files, so a license stays local.
private static final String LICENSE_PATH = "src/main/resources/license/YOUR_LICENSE.jli";
```

### Setting the license

`applyLicense` checks whether the file exists and sets the license from it, or calls the helper. The comment explains why it uses `setLicenseJSON(File)`: the license file holds the license as JSON.

```java
File licenseFile = new File(LICENSE_PATH);
if (licenseFile.exists()) {
  // setLicenseJSON, not setLicense(File): the license file holds the license as JSON.
  opti.setLicenseJSON(licenseFile);
  System.out.println("License: the license file " + LICENSE_PATH);
} else {
  ExampleLicenseHelper.setLicense(opti);
  System.out.println("License: the public evaluation license, no file " + LICENSE_PATH);
}
```

`ExampleLicenseHelper.setLicense` shows the string variant: it sets the public evaluation license with `setLicenseJSON(String)`.

### The run

`example()` sets the license before anything else. The other steps are those of the [First optimization example](../firstoptimization_01/FirstOptimizationExample.md), and `exportToKml` writes the result to `SetLicenseExample.kml` in the working directory:

```java
applyLicense(this);
addProperties(this);
addNodes(this);
addResources(this);
attachToObservables(this);
IOptimizationResult result = startAndPresentResult(this);
exportToKml(result);
```

The export runs in a try-with-resources block. If it fails, the example prints `KML export failed:` with the file name.

Set your license as early as possible, before starting the run.

## Output and what to look for

The first line of the output names the license that the example set. Without a license file it reads:

```text
License: the public evaluation license, no file src/main/resources/license/YOUR_LICENSE.jli
```

With a license file at that path, the code prints `License: the license file` followed by the path instead.

At the start of the run, a status event `STATUS_INFO` shows the contact, the description and the version of the license that is set. In the result, all five nodes are on May 6, and the route of May 7 is not scheduled (trimmed to these lines of the result header):

```text
 Number of Route         : 2
 Number of Route (sched.): 1
 Total Route Elements    : 6
```

## Run it

Run the `main` method of `SetLicenseExample` from your IDE, with the repository imported as a Maven project. You need a JDK in the version that the examples pom sets. No license file is needed: without one, the example uses the public evaluation license.

To use your own license, save its JSON as `src/main/resources/license/YOUR_LICENSE.jli` and run the example from the repository root, because the path is relative to the working directory. Git ignores `*.jli` files, so the license does not end up in a commit.

### When to use a string or a file

Set the license via JSON string. Use this when:
- you store license content in a secret manager,
- you inject it via environment variables,
- you load it from a database or configuration service.

Set the license via file. Use this when:
- you deploy the license as a file on the host,
- you mount it as a secret/volume (Docker/Kubernetes),
- you want to keep credentials out of your code and out of environment variables.

### Recommended practices for production environments

**Never commit licenses to source control.** Treat the license JSON as a secret:
- do not store it in your repository,
- do not embed it in compiled artifacts.

Prefer:
- secret managers (Vault, AWS Secrets Manager, Azure Key Vault),
- Kubernetes Secrets mounted as files,
- secure CI variables injected at runtime.

**Prefer file-based configuration for containers.** In containerized deployments:
- mount the license file into the container, and
- point the application to that path.

This also makes rotation easier (swap the mounted secret).

**Validate the presence of a license before "real runs".** For long-running services, fail fast with a clear message if the license is missing, instead of silently running in a restricted mode.

A common pattern:
- "demo mode" allowed only in local/dev profiles,
- "production" requires the license to be present.

**Keep the license JSON untouched.** Because modifying keys invalidates the license, any automation around loading/storing should:
- treat the JSON as an opaque value,
- avoid "re-serializing" it through another JSON library that might reorder/alter keys.

Formatting-only changes are acceptable; semantic changes are not.

### Notes for the Docker and REST variant

With JOpt.TourOptimizer in a container, the license JSON goes with each request; the [license guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license) names the field and links the REST server page.

## Related

- [License guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license): the license JSON, the element limits, and how to send a license with a request to JOpt.TourOptimizer.
- [First optimization example](../firstoptimization_01/FirstOptimizationExample.md): the run steps after the license, explained one by one.
- [Basic examples](../README.md): the overview of all Basic examples.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-20
