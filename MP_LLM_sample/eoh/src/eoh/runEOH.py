import os, sys
import logging
from logging import getLogger

import numpy as np

os.chdir(os.path.dirname(os.path.abspath(__file__)))
sys.path.insert(0, "..")  # for problem_def
sys.path.insert(0, "../..")  # for utils

from eoh import eoh
from eoh.utils.getParas import Paras
from log_utils import create_logger, copy_all_src, set_result_folder_ini, get_result_folder

# Parameter initilization #
paras = Paras()

# Set parameters #
paras.set_paras(method = "eoh",
                problem = "hvrp_java",
                problem_type = "white-box", # ['black-box','white-box']
                llm_api_endpoint = "api.metaihub.cn", # set your LLM endpoint
                llm_api_key = "",   # set your key
                llm_model = "gpt-4o",
                ec_pop_size = 20, # number of samples in each population 20
                ec_n_pop = 20, # number of populations 10
                exp_debug_mode = False)

logger_params = {
    'log_file': {
        'desc': '',
        'filename': 'run_log'
    }
}

def _print_config(logger):
    # logger = logging.getLogger('root')
    logger.info("problem: {}".format(paras.problem))
    logger.info("problem_type: {}".format(paras.problem_type))
    logger.info("pop_size: {}".format(paras.ec_pop_size))
    logger.info("n_pop: {}".format(paras.ec_n_pop))

    [logger.info(g_key + "{}".format(globals()[g_key])) for g_key in globals().keys() if g_key.endswith('params')]


# initilization
logger = getLogger('root')
logger_params['log_file']['desc'] = paras.problem + "_" + paras.problem_type  # + "_test_average_c_obpp"
create_logger(**logger_params)
_print_config(logger)
copy_all_src(get_result_folder())
set_result_folder_ini()

history_mean = []
history_best = []
test_results_list = []
test_averages = []
paras.exp_use_seed = True
paras.exp_seed_path = "problems\optimization\cvrp_java\seeds.json"  # enter your seed path here seeds.json

# for i in range(paras.rounds):
for i in [0]:
    evolution = eoh.EVOL(paras)

    # run
    m, b, test_results = evolution.run(i)
    history_mean.extend(m)
    history_best.append(b)
    test_results_list.append(test_results)
    # test_averages.append(np.mean(test_results))


logger.info('=================================================================')
logger.info('Test Done !')
logger.info('Average Obj: {}'.format(np.mean(history_mean)))
logger.info('Best Obj List: {}'.format([e for e in history_best]))
logger.info('Average Best Obj: {}'.format(np.mean(history_best)))
logger.info('Test Best Obj List: {}'.format(test_results_list))
logger.info('Average Test Best Obj: {}'.format(np.mean(test_averages)))
