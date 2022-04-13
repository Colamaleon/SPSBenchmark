# SPSBenchmark

An application intended to run benchmarks on SPS schemes implemented in [Cryptimeleon](https://github.com/cryptimeleon/craco). 

## Running a Benchmark

Benchmarks for 4 schemes (Groth1, AGHO11, AKOT15, KPW15) are already provided. To run them, simply execute the `main` function in `BenchmarkRunner.java`.
The parameters of the benchmark can be modified via the following command line arguments:

```
BenchmarkRunner t|c <Groth1|AGHO11|AKOT15|KPW15> <messageLength> <prewarmIterations> <iterations>
```
* `t|c` : runs the benchmark in either counting or timer mode
* `<Groth1|AGHO11|AKOT15|KPW15>` : selects a scheme for benchmarking
* `<messageLength>` : the length of messages passed to the selected scheme for signing
* `<prewarmIterations>` : runs all benchmark steps (setup,keyGen,sign,verify) for the specified number of times without measuring
* `<iterations>` : runs all benchmark steps (setup,keyGen,sign,verify) for the specified number of times while measuring either time or operations performed



(If using IntelliJ, one can edit these parameters by clicking the current run configuration in the top-right corner, then selecting "Edit Configurations

  <img src="https://user-images.githubusercontent.com/21686797/163207528-234b9f1d-2236-45a5-9f4e-5a155645da01.png" width=60% height=60%>  
