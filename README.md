# Multipopulation Optimization With LLM-Driven Knowledge Discovery for Large-Scale HFVRP
IEEE TRANSACTIONS ON COMPUTATIONAL SOCIAL SYSTEMS

## Quick Start for MP-LLM
> 1. Open <AILS-HVRP-population-llm> as a Java project
> 2. Set up SDK, additional necessary package can be found in /lib
> 3. Configure path for saving result files in src/Metaheuristicas/Population.java
> 4. Run src/Metaheuristicas/Population.java

## Abstract
Logistics transportation plays a critical role in real- world applications. The Heterogeneous Fleet Vehicle Routing Problem (HFVRP), characterized by varying vehicle capacities and costs, are the key optimization challenges in many lo- gistic scenarios. Despite its importance, it presents substantial challenges due to its NP-hard nature and large scale. Existing methods only study HFVRP instances of moderate size (i.e., about 300 nodes), which is insufficient for real-world application. In this paper, we introduce MP-LLM, a novel Multi-Population (MP) optimization method with Large Language Model (LLM)-driven knowledge discovery. MP-LLM employs multiple populations with iterated local search and dynamic updating to balance explo- ration and exploitation. An LLM-driven knowledge discovery is adopted to design a parameter adjustment strategy to pinpoint features specific to each instance, thereby facilitating a more effective dynamic parameter adjustment. We comprehensively evaluate MP-LLM on four benchmark test sets with 170 instances of diverse distributions and sizes. Our results show that when compared to stat-of-the-art methods, MP-LLM not only achieves superior solution quality but also significantly enhances efficiency. Notably, MP-LLM generates new best-known solutions on 18 out of 90 classic instances. It significantly expands HFVRP-solving capabilities from approximately 300 nodes to instances with up to 3,000 nodes.