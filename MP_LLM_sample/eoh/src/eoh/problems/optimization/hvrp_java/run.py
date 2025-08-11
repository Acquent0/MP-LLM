import subprocess
from concurrent.futures import ProcessPoolExecutor

import numpy as np
from joblib import Parallel, delayed
import os
import msvcrt
import multiprocessing
import sys

# from func_timeout import func_set_timeout, FunctionTimedOut

class HVRPJAVA():
    def __init__(self, problem_type='white-box') -> None:
        self.perturbation_moves = 1 # movers of each edge in each perturbation
        path = os.path.dirname(os.path.abspath(__file__))

        self.instances = []
        self.instance_commands = []

        from .prompts import GetPrompts
        from .prompts_black import GetPromptsBlack

        self.problem_type = problem_type
        if self.problem_type == 'white-box':
            self.prompts = GetPrompts()
        else:
            self.prompts = GetPromptsBlack()

    # @func_set_timeout(150)
    def run_command(self, commands):
        process = subprocess.run(
            commands,
            capture_output=True,
            # stdout=subprocess.PIPE,
            # stderr=subprocess.PIPE,
            text=True
            # shell=True
        )

        last_line = process.stdout.splitlines()[-1]

        # process.wait()

        return last_line, process.returncode

    def evaluate(self, code_string, index=0, task=None):

        target_dir = rf"F:\XZL\AILS_HVRP_LLM_parallel_pack/AILS_HVRP_population_llm_{index}/"  # enter your project root here

        # commands

        instances = [["HVRPFD", "Instances_original/Li_H3.txt", 2, 2025],
                     ["HVRPFD", "Instances_original/Li_H5.txt", 4, 2025],
                     ["HVRPFD", "Instances_original/N2.txt", 6, 2025],
                     ["HVRPD", "Instances_original/Li_H2.txt", 1, 2025],
                     ["HVRPD", "Instances_original/Li_H3.txt", 2, 2025],
                     ["HVRPD", "Instances_original/N2.txt", 6, 2025],
                     ["FSMFD", "Instances_original/Li_H5.txt", 4, 2025],
                     ["FSMFD", "Instances_original/Li_H3.txt", 2, 2025],
                     ["FSMFD", "Instances_original/N2.txt", 6, 2025],
                     ["FSMF", "Instances_original/Li_H3.txt", 2, 2025],
                     ["FSMF", "Instances_original/Taillard_16.txt", 13, 2025],
                     ["FSMF", "Instances_original/Taillard_20.txt", 17, 2025],
                     ["FSMD", "Instances_original/Li_H4.txt", 3, 2025],
                     ["FSMD", "Instances_original/Li_H5.txt", 4, 2025],
                     ["FSMD", "Instances_original/N2.txt", 6, 2025]]

        modified_instances = [instance[:-1] + [2026] for instance in instances]
        instances.extend(modified_instances)

        self.instances = [[target_dir + instances[i][1], instances[i][0], instances[i][2], instances[i][3]] for i in
                          range(len(instances))]

        self.instance_commands = [
            ["java", "-cp", target_dir+'out/production/AILS-HVRP;libs/*', "Metaheuristicas.Population", "-file", e[0],
             "-variant", f"{e[1]}", "-problemType", "Classic", "-timeLimit", "60", "-stoppingCriterion",
             "Iteration", "-insNo", f"{e[2]}", "-rand", f"{e[3]}"] for e in self.instances]

        # elif task == 'test':
        #     self.instance_path = path + f'/TestingData/TSPAEL{scale}.pkl'

        # write code to file
        code_path = target_dir +  f"src/Metaheuristicas/ParamAdjust.java"

        if "JDK_PATH" not in os.environ:
            os.environ['PATH'] += r";C:\Users\DELL\.jdks\corretto-21.0.4\bin"  # enter your jdk path here
            os.environ["JDK_PATH"] = "set"
        src_path = os.path.join(target_dir, "src")
        output_dir = os.path.join(target_dir, "out/production/AILS_HVRP")
        sources_file = os.path.join(target_dir, "sources.txt")

        with open(code_path, "w") as f:
            f.write(code_string)

            # compile
        with open(sources_file, 'w') as fs:
            subprocess.run(f'dir /s /b "{src_path}\\*.java"', shell=True, text=True, stdout=fs)

        compile_process = subprocess.run(
            ["javac", "-d", target_dir+"out/production/AILS-HVRP", "-sourcepath", "src", "-cp",
             target_dir+"out/production/AILS-HVRP;libs/*", f"@{sources_file}"],
            capture_output=True,
            text=True
            # shell=True
        )

        try:
            if compile_process.returncode != 0:
                assert print("Compilation failed!")
            else:
                # assert print("Compilation succeeded!")
                pass
        except Exception as e:
            print(e)
            return None

        # evaluate
        try:
            if task == 'ini':
                results = Parallel(n_jobs=22, timeout=500)(delayed(self.run_command)(i) for i in self.instance_commands)
            else:
                results = Parallel(n_jobs=2, timeout=500)(delayed(self.run_command)(i) for i in self.instance_commands)

            # 打印结果
            fitness = []
            for i, (last_line, return_code) in enumerate(results):
                fitness.append(last_line)

            fitness = [float(e) for e in fitness]
            fitness = np.mean(fitness)

            return fitness

        except Exception as e:
            print(e, file=sys.stderr)
            return None

        # try:
        #     # Suppress warnings
        #     with warnings.catch_warnings():
        #         warnings.simplefilter("ignore")
        #
        #         # Create a new module object
        #         heuristic_module = types.ModuleType("heuristic_module")
        #
        #         # Execute the code string in the new module's namespace
        #         exec(code_string, heuristic_module.__dict__)
        #
        #         # Add the module to sys.modules so it can be imported
        #         sys.modules[heuristic_module.__name__] = heuristic_module
        #
        #         #print(code_string)
        #         fitness = self.evaluateGLS(heuristic_module)
        #
        #         return fitness
        #
        # except FunctionTimedOut or Exception as e:
        #     # print("Error:", str(e))
        #     return None



