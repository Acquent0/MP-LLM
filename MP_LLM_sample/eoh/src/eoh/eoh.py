
import random

from .utils import createFolders
from .methods import methods
from .problems import problems

# main class for AEL
class EVOL:

    # initilization
    def __init__(self, paras, prob=None, **kwargs):

        print("----------------------------------------- ")
        print("---              Start EoH            ---")
        print("-----------------------------------------")
        # Create folder #
        createFolders.create_folders(paras.exp_output_path)
        print("- output folder created -")

        self.paras = paras

        print("-  parameters loaded -")

        self.prob = prob

        # Set a random seed
        random.seed(2024)

        
    # run methods
    def run(self, num_turns):

        problemGenerator = problems.Probs(self.paras)

        problem = problemGenerator.get_problem()

        methodGenerator = methods.Methods(self.paras, problem)

        method = methodGenerator.get_method()

        m, b, test_results = method.run(num_turns=num_turns)

        print("> End of Evolution! ")
        print("----------------------------------------- ")
        print("---     LCS successfully finished !   ---")
        print("-----------------------------------------")

        return m, b, test_results
