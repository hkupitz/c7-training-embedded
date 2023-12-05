# Set up the Camunda 7 Project

## Goal

The goal of this lab is to run the embedded engine from the provided template.

## Detailed steps

1. Download or clone the [template project](https://github.com/hkupitz/c7-training-embedded) from GitHub.
2. Import the pom.xml as an existing maven project into your IDE.
  1. For the Eclipse IDE use **File > Open Projects from Filesystem**. Click on Directory, point to the parent folder of the pom.xml. The pom.xml should appear as a selection. Click on **Finish**.
  2. For IntelliJ use **File > Open** and navigate to the **pom.xml** file unzipped in Step 1. A dialog box should appear to open the pom.xml either as a project or file. Select **Open as Project**.
3. Set up the project according to the instructions in `README.md`.
3. Run `CamundaApplication.java` in your IDE.
4. Open your browser and navigate to `localhost:8080`.
5. Log-in using the user `demo` and password `demo`.
6. Open the `Admin` webapp.
7. Navigate to the `License` page and enter your license.

## Summary

In this exercise you have set up your IDE and are running Camunda 7 using an embedded engine.