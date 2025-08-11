# Multipopulation Optimization With LLM-Driven Knowledge Discovery for Large-Scale HFVRP
IEEE TRANSACTIONS ON COMPUTATIONAL SOCIAL SYSTEMS

## Quick Start for MP-LLM
> 1. Open <AILS-HVRP-population-llm> as a Java project
> 2. Set up SDK, additional necessary package can be found in /lib
> 3. Configure path for saving result files in src/Metaheuristicas/Population.java
> 4. Run src/Metaheuristicas/Population.java

## Quick Start for MP-LLM-sample
> [!Note]
> Configure your LLM api before running the script. For example:
>
> 1) Set `host`: 'api.deepseek.com' or 'api.metaihub.cn' or ...
> 2) Set `key`: 'your api key'
> 3) Set `model`: 'deepseek-v3' or 'gpt-4o-mini' or ...

In MP-LLM-sample/eoh/src/eoh/run_EOH.py:
```python
# Set parameters #
paras.set_paras(method = "eoh",
                problem = "hvrp_java",
                problem_type = "white-box", # ['black-box','white-box']
                llm_api_endpoint = "api.metaihub.cn", # set your LLM endpoint
                llm_api_key = "",   # set your key
                llm_model = "gpt-4o",
                ec_pop_size = 20, # number of samples in each population 20
                ec_n_pop = 20, # number of populations 20
                exp_debug_mode = False)

# Set seed path #     
paras.exp_seed_path = "problems\optimization\cvrp_java\seeds.json"  # enter your seed path here seeds.json
```

In MP-LLM-sample/eoh/src/eoh/problem/optimization/hvrp_java/run.py
```python
# Set parallel evaluation pack path #
# One index is for one parallel evaluation
target_dir = rf"F:\XZL\AILS_HVRP_LLM_parallel_pack/AILS_HVRP_population_llm_{index}/"  # enter your project root here

# Set java SDK path #
if "JDK_PATH" not in os.environ:
    os.environ['PATH'] += r";C:\Users\DELL\.jdks\corretto-21.0.4\bin"  # enter your jdk path here
    os.environ["JDK_PATH"] = "set"

# Mind your CPU cores (inner parallel) #
# Initialization usually only evaluate a seed algorithm
# In LLM iteration phase, cores: outer parallel * inner parallel
if task == 'ini':
    results = Parallel(n_jobs=20, timeout=500)(delayed(self.run_command)(i) for i in self.instance_commands)
else:
    results = Parallel(n_jobs=2, timeout=500)(delayed(self.run_command)(i) for i in self.instance_commands)
```

In MP-LLM-sample/eoh/src/eoh/methods/eoh/eoh_interface_EC.py
```python
# Mind your CPU cores (inner parallel) #
results = Parallel(n_jobs=20, timeout=1000)(delayed(self.get_offspring)(pop, operator) for _ in range(20))
```
## Abstract
Logistics transportation plays a critical role in real- world applications. The Heterogeneous Fleet Vehicle Routing Problem (HFVRP), characterized by varying vehicle capacities and costs, are the key optimization challenges in many lo- gistic scenarios. Despite its importance, it presents substantial challenges due to its NP-hard nature and large scale. Existing methods only study HFVRP instances of moderate size (i.e., about 300 nodes), which is insufficient for real-world application. In this paper, we introduce MP-LLM, a novel Multi-Population (MP) optimization method with Large Language Model (LLM)-driven knowledge discovery. MP-LLM employs multiple populations with iterated local search and dynamic updating to balance explo- ration and exploitation. An LLM-driven knowledge discovery is adopted to design a parameter adjustment strategy to pinpoint features specific to each instance, thereby facilitating a more effective dynamic parameter adjustment. We comprehensively evaluate MP-LLM on four benchmark test sets with 170 instances of diverse distributions and sizes. Our results show that when compared to stat-of-the-art methods, MP-LLM not only achieves superior solution quality but also significantly enhances efficiency. Notably, MP-LLM generates new best-known solutions on 18 out of 90 classic instances. It significantly expands HFVRP-solving capabilities from approximately 300 nodes to instances with up to 3,000 nodes.