import os.path
import sys
import numpy as np
import json
import time
from .eoh_evolution import Evolution
import warnings
from joblib import Parallel, delayed
from .evaluator_accelerate import add_numba_decorator
import re
import multiprocessing

class InterfaceEC():
    def __init__(self, paras, pop_size, m, api_endpoint, api_key, llm_model, debug_mode, interface_prob, select,n_p,timeout,use_numba,**kwargs):
        # -------------------- RZ: use local LLM --------------------
        assert 'use_local_llm' in kwargs
        assert 'url' in kwargs
        # -----------------------------------------------------------

        # LLM settings
        self.paras = paras
        self.pop_size = pop_size
        self.interface_eval = interface_prob
        prompts = interface_prob.prompts
        self.evol = Evolution(api_endpoint, api_key, llm_model, debug_mode,prompts, **kwargs)
        self.m = m
        self.debug = debug_mode

        if not self.debug:
            warnings.filterwarnings("ignore")

        self.select = select
        self.n_p = n_p
        
        self.timeout = timeout
        self.use_numba = use_numba
        
    def code2file(self,code):
        with open("./ael_alg.py", "w") as file:
        # Write the code to the file
            file.write(code)
        return 
    
    def add2pop(self,population,offspring):
        for ind in population:
            if ind['objective'] == offspring['objective']:
                if self.debug:
                    print("duplicated result, retrying ... ")
                return False
        population.append(offspring)
        return True
    
    def check_duplicate_lcs(self, curr_ind, new_ind):
        if new_ind['code'] == curr_ind['code'] or new_ind['objective'] == curr_ind['objective']:
            return True
        return False

    def check_duplicate(self,population,code):
        for ind in population:
            if code == ind['code']:
                return True
        return False
    
    def initial_generation_seed(self, seed):

        fitness = self.interface_eval.evaluate(seed['code'])

        try:
            seed_alg = {
                'algorithm': seed['algorithm'],
                'code': seed['code'],
                'objective': None,
                'other_inf': None
            }

            obj = np.array(fitness)
            seed_alg['objective'] = np.round(obj, 5)
            seed_alg['objective'] = float(seed_alg['objective'])

        except Exception as e:
            print("Error in seed algorithm")
            exit()

        print("Initiliazation finished! Get seed algorithm with objective {}".format(seed_alg['objective']))

        return seed_alg

    def population_generation(self):

        n_create = 1

        population = []

        for i in range(n_create):
            _, pop = self.get_algorithm([], 'i1', initial=True)
            while None in [e['objective'] for e in pop]:
                _, pop = self.get_algorithm([], 'i1', initial=True)
            for p in pop:
                population.append(p)

        return population

    def population_generation_seed(self,seeds,n_p):

        population = []

        fitness = Parallel(n_jobs=n_p)(delayed(self.interface_eval.evaluate)(seed['code']) for seed in seeds)

        for i in range(len(seeds)):
            try:
                seed_alg = {
                    'algorithm': seeds[i]['algorithm'],
                    'code': seeds[i]['code'],
                    'objective': None,
                    'other_inf': None
                }

                obj = np.array(fitness[i])
                seed_alg['objective'] = np.round(obj, 5)
                population.append(seed_alg)

            except Exception as e:
                print("Error in seed algorithm")
                exit()

        initial_objs = [s["objective"] for s in population]
        print("Initiliazation finished! Get "+str(len(seeds))+f" seed algorithms: {initial_objs}")

        return population

    def get_offspring(self, pop, operator):

        try:
            p, offspring = self._get_alg(pop, operator)

            if self.use_numba:

                # Regular expression pattern to match function definitions
                pattern = r"def\s+(\w+)\s*\(.*\):"

                # Search for function definitions in the code
                match = re.search(pattern, offspring['code'])

                function_name = match.group(1)

                code = add_numba_decorator(program=offspring['code'], function_name=function_name)
            else:
                code = offspring['code']

            n_retry = 1
            while self.check_duplicate(pop, offspring['code']):

                n_retry += 1
                if self.debug:
                    print("duplicated code, wait 1 second and retrying ... ")

                p, offspring = self._get_alg(pop, operator)

                if self.use_numba:
                    # Regular expression pattern to match function definitions
                    pattern = r"def\s+(\w+)\s*\(.*\):"

                    # Search for function definitions in the code
                    match = re.search(pattern, offspring['code'])

                    function_name = match.group(1)

                    code = add_numba_decorator(program=offspring['code'], function_name=function_name)
                else:
                    code = offspring['code']

                if n_retry > 1:
                    break

            # self.code2file(offspring['code'])
            offspring['objective'] = self.interface_eval.evaluate(code)
            offspring['objective'] = np.round(offspring['objective'], 5)
            offspring['objective'] = float(offspring['objective'])
            # with concurrent.futures.ThreadPoolExecutor() as executor:
            #     future = executor.submit(self.interface_eval.evaluate, code)
            #     fitness = future.result(timeout=self.timeout)
            #     offspring['objective'] = np.round(fitness, 5)
            #     offspring['objective'] = float(offspring['objective'])
            #     future.cancel()


        except Exception as e:

            offspring = {
                'algorithm': None,
                'code': None,
                'objective': None,
                'other_inf': None
            }
            p = None

        # Round the objective values
        return p, offspring

    def get_algorithm(self, pop, operator, initial=False):
        results = []
        try:
            results = Parallel(n_jobs=20, timeout=1000)(delayed(self.get_offspring)(pop, operator) for _ in range(20))
            # for i in range(self.pop_size):
            #     results.append(self.get_offspring(pop, operator))

        except Exception as e:
            if self.debug:
                print(f"Error: {e}")
            print("Parallel time out .")

        if initial:
            ind = 0
            for _, off in results:
                while off['objective'] is None:
                    _, re_off = self.get_offspring(pop, operator)
                    if re_off['objective'] is not None:
                        results[ind] = (_, re_off)
                        break
                ind += 1

        time.sleep(1)

        out_p = []
        out_off = []

        for p, off in results:
            out_p.append(p)
            out_off.append(off)
            if self.debug:
                print(f">>> check offsprings: \n {off}")
        return out_p, out_off

    def _get_alg(self,pop,operator):
        offspring = {
            'algorithm': None,
            'code': None,
            'objective': None,
            'other_inf': None
        }
        if operator == "i1":
            parents = None
            [offspring['code'],offspring['algorithm']] =  self.evol.i1()
        elif operator == "e1":
            parents = self.select.parent_selection(pop,self.m)
            [offspring['code'],offspring['algorithm']] = self.evol.e1(parents)
        elif operator == "e2":
            parents = self.select.parent_selection(pop,self.m)
            [offspring['code'],offspring['algorithm']] = self.evol.e2(parents)
        elif operator == "m1":
            parents = self.select.parent_selection(pop,1)
            [offspring['code'],offspring['algorithm']] = self.evol.m1(parents[0])
        elif operator == "m2":
            parents = self.select.parent_selection(pop,1)
            [offspring['code'],offspring['algorithm']] = self.evol.m2(parents[0])
        elif operator == "m3":
            parents = self.select.parent_selection(pop,1)
            [offspring['code']] = self.evol.m3(parents[0])
            offspring['algorithm'] = parents[0]['algorithm']
        else:
            print(f"Evolution operator [{operator}] has not been implemented ! \n")

        return parents, offspring

    def _get_alg_lcs(self, curr_ind, operator, ratio, ts_ele):
        new_ind = {
            'algorithm': None,
            'code': None,
            'objective': None,
            'other_inf': None
        }
        if operator == "i1":
            parent = None
            [new_ind['code'], new_ind['algorithm']] = self.evol.i1()
        elif operator == "rr":
            parent = curr_ind
            [new_ind['code'], new_ind['algorithm'], new_ind['other_inf']] = self.evol.rr(parent, ratio)
        elif operator == "p1":
            parent = curr_ind
            [new_ind['code'], new_ind['algorithm'], new_ind['other_inf']] = self.evol.p1(parent)
        elif operator == "p2":
            parent = curr_ind
            [new_ind['code'], new_ind['algorithm'], new_ind['other_inf']] = self.evol.p2(parent, ts_ele)
        else:
            print(f"Evolution operator [{operator}] has not been implemented ! \n") 

        return parent, new_ind

    def get_new_ind(self, curr_ind, operator, ratio, ts_ele):

        try:
            parent, next_ind = self._get_alg_lcs(curr_ind, operator, ratio, ts_ele)

            if self.use_numba:

                # Regular expression pattern to match function definitions
                pattern = r"def\s+(\w+)\s*\(.*\):"

                # Search for function definitions in the code
                match = re.search(pattern, next_ind['code'])

                function_name = match.group(1)

                code = add_numba_decorator(program=next_ind['code'], function_name=function_name)
            else:
                code = next_ind['code']

            n_retry = 1
            while self.check_duplicate_lcs(curr_ind, next_ind):

                n_retry += 1
                if self.debug:
                    print("duplicated code, wait 1 second and retrying ... ")

                parent, next_ind = self._get_alg_lcs(curr_ind, operator, ratio, ts_ele)

                if self.use_numba:
                    # Regular expression pattern to match function definitions
                    pattern = r"def\s+(\w+)\s*\(.*\):"

                    # Search for function definitions in the code
                    match = re.search(pattern, next_ind['code'])

                    function_name = match.group(1)

                    code = add_numba_decorator(program=next_ind['code'], function_name=function_name)
                else:
                    code = next_ind['code']

                if n_retry > 1:
                    break

            # with concurrent.futures.ThreadPoolExecutor() as executor:
            #     future = executor.submit(self.interface_eval.evaluate, code)
            #     fitness = future.result(timeout=self.timeout)
            #     next_ind['objective'] = np.round(fitness, 5)
            #     future.cancel()
                # fitness = self.interface_eval.evaluate(code)

            next_ind['objective'] = self.interface_eval.evaluate(code)
            next_ind['objective'] = np.round(next_ind['objective'], 5)
            next_ind['objective'] = float(next_ind['objective'])

            # if self.paras.problem_type == 'white-box':
            #     if next_ind['objective'] == curr_ind['objective']:
            #         raise Exception


        except Exception as e:

            next_ind = {
                'algorithm': None,
                'code': None,
                'objective': None,
                'other_inf': None
            }
            parent = None

        # Round the objective values
        return parent, next_ind

    def get_next_heuristic(self, curr_ind, operator, ratio=0.5, ts_ele=None):

        new_ind = None
        parent = None
        while parent == None:
            try:
                parent, new_ind = self.get_new_ind(curr_ind, operator, ratio, ts_ele)
                # parent, new_ind = Parallel(n_jobs=self.n_p,timeout=self.timeout+15)(delayed(self.get_new_ind)(curr_ind, operator, ratio, ts_ele))
            except Exception as e:
                if self.debug:
                    print(f"Error: {e}")
                # print("Parallel time out .")

        return parent, new_ind

