# Evaluation of the OWL Obligation State Manager

This repository provides an **evaluation framework** for testing the generation and execution of **obligations** following **the Obligation Compliance Model Ontotlogy** based on patient information.
It includes a **data generator** and a **test harness** for measuring performance in terms of the number of obligations.

## Overview

The evaluation framework consists of two main components:

### 1. Generator
- Generates a configurable number of **obligations** based on input **subsets of patient information**. An example of subsets of patient information can be found here [1].
- Each obligation instance is generated following the Obligation Compliance Model Ontotlogy
- Supports multiple **temporal cases** to simulate realistic scenarios.

### 2. Test Harness
- Evaluates the **execution time** of the OWL Obligation State Manager. 

## Usage

### Running the Generator
1. Configure input parameters (e.g., subsets of patient information, number of obligations) in the benchmark.properties file.  
2. Run the generator to create a set of obligations stored in **Turtle (.ttl) files**.
3. The script for running the Test Harness can be found under the scripts folder.

[1] https://zenodo.org/records/18891663
