# Setting the JOpt License — `setLicenseJSON(...)` in Java

This document explains how to configure the **optimization license** for **JOpt TourOptimizer (Java)** and how the repository example `SetLicenseExample` applies it.

## Clickable links

- [License documentation](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license)
- [SetLicenseExample.java (GitHub)](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/setlicense_08/SetLicenseExample.java)

---

## When you need a license

According to the official documentation, a license is required **when you use more than 30 optimization elements** (nodes and resources are counted as elements).

> If you need more than 30 elements during your evaluation phase, feel free to [contanct us](https://www.dna-evolutions.com/contact).

Implication:
- Small “hello world” scenarios may run without an explicit license (depending on element count).
- Realistic scenarios typically exceed the limit and must set a valid license.

---

## What the license is

A JOpt license is provided as a **JSON definition**. You can set it either:

1. **As a JSON string**, or  
2. **As a file containing the JSON**.

The documentation also warns:

- Changing any JSON key (deleting/modifying/adding) deactivates the license.
- The only allowed modification is formatting (e.g., prettifying).

---

## Two supported application methods

### Option A — Set license via JSON string

Use this when:
- you store license content in a secret manager,
- you inject it via environment variables,
- you load it from a database or configuration service.

Minimal pattern:

```java
String myJsonLicense = "{ ... your license JSON ... }";
myOpti.setLicenseJSON(myJsonLicense);
```

In the example repository, this is demonstrated by a helper method that sets `setLicenseJSON(String)`.

---

### Option B — Set license via file

Use this when:
- you deploy the license as a file on the host,
- you mount it as a secret/volume (Docker/Kubernetes),
- you want to keep credentials out of your code and out of environment variables.

Minimal pattern:

```java
File licFile = new File("/path/to/license.dli");
myOpti.setLicenseJSON(licFile);
```

In the example repository, the license file path is:

- `src/main/resources/YOUR_LICENSE.dli`

The example checks whether the file exists and only sets the license if it does. If the file is missing, it simply does not set a license (which is a safe behavior for demo/free-mode scenarios).

---

## How the example (`SetLicenseExample`) is structured

The example follows a clean “bootstrap first” approach:

1. **Set license** (file-based by default)
2. Add properties
3. Add nodes
4. Add resources
5. Run optimization

Key takeaway: set your license as early as possible, before starting the run.

---

## Recommended practices for production environments

### 1) Never commit licenses to source control
Treat the license JSON as a secret:
- do not store it in your repository,
- do not embed it in compiled artifacts.

Prefer:
- secret managers (Vault, AWS Secrets Manager, Azure Key Vault),
- Kubernetes Secrets mounted as files,
- secure CI variables injected at runtime.

### 2) Prefer file-based configuration for containers
In containerized deployments, the simplest secure pattern is:
- mount the license file into the container, and
- point the application to that path.

This also makes rotation easier (swap the mounted secret).

### 3) Validate the presence of a license before “real runs”
For long-running services, fail fast with a clear message if the license is missing, instead of silently running in a restricted mode.

A common pattern:
- “demo mode” allowed only in local/dev profiles,
- “production” requires the license to be present.

### 4) Keep the license JSON untouched
Because modifying keys invalidates the license, any automation around loading/storing should:
- treat the JSON as an opaque value,
- avoid “re-serializing” it through another JSON library that might reorder/alter keys.

Formatting-only changes are acceptable; semantic changes are not.

---

## Notes for the Docker / REST variant

The license documentation states that when using the **Docker** version of JOpt TourOptimizer, you must send the JSON license definition along with your request.

If you use the REST service:
- keep the license in your request-building pipeline,
- and handle errors centrally so a missing/invalid license is diagnosed immediately.

---

## Quick checklist

- [ ] Using more than 10 elements? Provide a license.
- [ ] Decide: string-based or file-based injection.
- [ ] Store license securely (never in git).
- [ ] Do not modify JSON keys.
- [ ] Set license before starting the optimization run.
