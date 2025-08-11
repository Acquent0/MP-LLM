# from machinelearning import *
# from mathematics import *
# from optimization import *
# from physics import *
class Probs():
    def __init__(self,paras):

        if not isinstance(paras.problem, str):
            self.prob = paras.problem
            print("- Prob local loaded ")
        elif paras.problem == "tsp_construct":
            from .optimization.tsp_construct import run
            self.prob = run.TSPCONST(problem_type=paras.problem_type)
            print("- Prob "+paras.problem+" loaded ")
        elif paras.problem == "bp_online":
            from .optimization.bp_online import run
            self.prob = run.BPONLINE(problem_type=paras.problem_type)
            print("- Prob "+paras.problem+" loaded ")
        elif paras.problem == "admissible_set":
            from .optimization.admissible_set import run
            self.prob = run.ADMISSIBLESET(problem_type=paras.problem_type)
            print("- Prob "+paras.problem+" loaded ")
        elif paras.problem == "tsp_gls":
            from .optimization.tsp_gls import run
            self.prob = run.TSPGLS(problem_type=paras.problem_type)
            print("- Prob "+paras.problem+" loaded ")
        elif paras.problem == "cvrp_java":
            from .optimization.cvrp_java import run
            self.prob = run.CVRPJAVA(problem_type=paras.problem_type)
            print("- Prob "+paras.problem+" loaded ")
        elif paras.problem == "hvrp_java":
            from .optimization.hvrp_java import run
            self.prob = run.HVRPJAVA(problem_type=paras.problem_type)
            print("- Prob "+paras.problem+" loaded ")
        else:
            print("problem "+paras.problem+" not found!")


    def get_problem(self):

        return self.prob
