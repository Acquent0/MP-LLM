import copy
import random
import re
import time

import numpy as np

from ...llm.interface_LLM import InterfaceLLM

class Evolution():

    def __init__(self, api_endpoint, api_key, model_LLM, debug_mode,prompts, **kwargs):
        # -------------------- RZ: use local LLM --------------------
        assert 'use_local_llm' in kwargs
        assert 'url' in kwargs
        self._use_local_llm = kwargs.get('use_local_llm')
        self._url = kwargs.get('url')
        # -----------------------------------------------------------

        # set prompt interface
        #getprompts = GetPrompts()
        self.prompt_task         = prompts.get_task()
        self.prompt_func_name    = prompts.get_func_name()
        self.prompt_func_inputs  = prompts.get_func_inputs()
        self.prompt_func_outputs = prompts.get_func_outputs()
        self.prompt_inout_inf    = prompts.get_inout_inf()
        self.prompt_other_inf    = prompts.get_other_inf()
        self.format              = prompts.get_format()
        if len(self.prompt_func_inputs) > 1:
            self.joined_inputs = ", ".join("'" + s + "'" for s in self.prompt_func_inputs)
        else:
            self.joined_inputs = "'" + self.prompt_func_inputs[0] + "'"

        if len(self.prompt_func_outputs) > 1:
            self.joined_outputs = ", ".join("'" + s + "'" for s in self.prompt_func_outputs)
        else:
            self.joined_outputs = "'" + self.prompt_func_outputs[0] + "'"

        # set LLMs
        self.api_endpoint = api_endpoint
        self.api_key = api_key
        self.model_LLM = model_LLM
        self.debug_mode = debug_mode # close prompt checking

        # -------------------- RZ: use local LLM --------------------
        if self._use_local_llm:
            self.interface_llm = LocalLLM(self._url)
        else:
            self.interface_llm = InterfaceLLM(self.api_endpoint, self.api_key, self.model_LLM, self.debug_mode)

    def get_prompt_i1_old(self):
        
        prompt_content = self.prompt_task+"\n"\
"First, describe your new algorithm and main steps in one sentence. \
The description must be inside a brace. Next, implement it in Java as a function named \
"+self.prompt_func_name +". This function should accept "+str(len(self.prompt_func_inputs))+" input(s): "\
+self.joined_inputs+". The function should return "+str(len(self.prompt_func_outputs))+" output(s): "\
+self.joined_outputs+". "+self.prompt_inout_inf+" "\
+self.prompt_other_inf+"\n"+"Do not give additional explanations."
        return prompt_content

    def get_prompt_i1(self):
        prompt_content = self.prompt_task + "\n" \
"First, describe your new algorithm and main steps in one sentence. \
The description must be inside a brace. Next, implement it in Java as a function: \
\
" + self.format + "\nDo not give any additional explanations."
        return prompt_content

    def get_prompt_e1(self, indivs):
        prompt_indiv = ""
        for i in range(len(indivs)):
            prompt_indiv = prompt_indiv + "No." + str(i + 1) + " algorithm and the corresponding code are: \n" + \
                           indivs[i]['algorithm'] + "\n" + indivs[i]['code'] + "\n"

        prompt_content = self.prompt_task + "\n" \
                                            "I have " + str(
            len(indivs)) + " existing algorithms with their codes as follows: \n" \
                         + prompt_indiv + \
"Please help me create a new algorithm that has a totally different form from the given ones. \n" \
"First, describe your new algorithm and main steps in one sentence. \
The description must be inside a brace. Next, implement it in Java as a function: \
\
" + self.format + "\nDo not give any additional explanations."
        return prompt_content

    def get_prompt_e2(self, indivs):
        prompt_indiv = ""
        for i in range(len(indivs)):
            prompt_indiv = prompt_indiv + "No." + str(i + 1) + " algorithm and the corresponding code are: \n" + \
                           indivs[i]['algorithm'] + "\n" + indivs[i]['code'] + "\n"

        prompt_content = self.prompt_task + "\n" \
                                            "I have " + str(
            len(indivs)) + " existing algorithms with their codes as follows: \n" \
                         + prompt_indiv + \
"Please help me create a new algorithm that has a totally different form from the given ones but can be motivated from them. \n" \
"Firstly, identify the common backbone idea in the provided algorithms. Secondly, based on the backbone idea describe your new algorithm in one sentence. \
The description must be inside a brace. Thirdly, implement it in Java as a function: \
\
" + self.format + "\nDo not give any additional explanations."
        return prompt_content

    def get_prompt_m1(self, indiv1):
        prompt_content = self.prompt_task + "\n" \
"I have one algorithm with its code as follows. \
Algorithm description: " + indiv1['algorithm'] + "\n\
Code:\n\
" + indiv1['code'] + "\n\
Please assist me in creating a new algorithm that has a different form but can be a modified version of the algorithm provided. \n" \
"First, describe your new algorithm and main steps in one sentence. \
The description must be inside a brace. Next, implement it in Java as a function: \
\
" + self.format + "\nDo not give any additional explanations."
        return prompt_content

    def get_prompt_m2(self, indiv1):
        prompt_content = self.prompt_task + "\n" \
"I have one algorithm with its code as follows. \
Algorithm description: " + indiv1['algorithm'] + "\n\
Code:\n\
" + indiv1['code'] + "\n\
Please identify the main algorithm parameters and assist me in creating a new algorithm that has a different parameter settings of the score function provided. \n" \
"First, describe your new algorithm and main steps in one sentence. \
The description must be inside a brace. Next, implement it in Java as a function: \
\
" + self.format + "\nDo not give any additional explanations."
        return prompt_content

    def get_prompt_simple(self, indiv1):
        prompt_content = "First, you need to identify the main components in the function below. \
Next, analyze whether any of these components can be overfit to the in-distribution instances. \
Then, based on your analysis, simplify the components to enhance the generalization to potential out-of-distribution instances. \
Finally, provide the revised code, keeping the function, inputs, and outputs unchanged. \n" + indiv1['code'] + "\n" \
                         + "format:" + "\n" + self.format + "\nDo not give additional explanations."
        return prompt_content

    def get_prompt_o_index(self, indiv1, operator=0):
        prompt_content = self.prompt_task + "\n" \
"I have one algorithm with its code as follows. \
Algorithm description: " + indiv1['algorithm'] + "\n \
Code:\n \
" + indiv1['code'] + "\n" \
+ self.get_opts(operator) + "\n\
First, describe your new algorithm and main steps in one sentence. \
The description must be inside a brace. Next, implement it in Java as a function named \
" + self.prompt_func_name + ". This function should accept " + str(len(self.prompt_func_inputs)) + " input(s): " \
                         + self.joined_inputs + ". The function should return " + str(
            len(self.prompt_func_outputs)) + " output(s): " \
                         + self.joined_outputs + ". " + self.prompt_inout_inf + " " \
                         + self.prompt_other_inf + "\n" + "Do not give additional explanations."
        return prompt_content



    def _get_alg_python(self,prompt_content):

        response = self.interface_llm.get_response(prompt_content)

        algorithm = re.findall(r"\{(.*)\}", response, re.DOTALL)
        if len(algorithm) == 0:
            if 'python' in response:
                algorithm = re.findall(r'^.*?(?=python)', response,re.DOTALL)
            elif 'import' in response:
                algorithm = re.findall(r'^.*?(?=import)', response,re.DOTALL)
            else:
                algorithm = re.findall(r'^.*?(?=def)', response,re.DOTALL)

        code = re.findall(r"import.*return", response, re.DOTALL)
        if len(code) == 0:
            code = re.findall(r"def.*return", response, re.DOTALL)

        n_retry = 1
        while (len(algorithm) == 0 or len(code) == 0):
            if self.debug_mode:
                print("Error: algorithm or code not identified, wait 1 seconds and retrying ... ")

            response = self.interface_llm.get_response(prompt_content)

            algorithm = re.findall(r"\{(.*)\}", response, re.DOTALL)
            if len(algorithm) == 0:
                if 'python' in response:
                    algorithm = re.findall(r'^.*?(?=python)', response,re.DOTALL)
                elif 'import' in response:
                    algorithm = re.findall(r'^.*?(?=import)', response,re.DOTALL)
                else:
                    algorithm = re.findall(r'^.*?(?=def)', response,re.DOTALL)

            code = re.findall(r"import.*return", response, re.DOTALL)
            if len(code) == 0:
                code = re.findall(r"def.*return", response, re.DOTALL)
                
            if n_retry > 3:
                break
            n_retry +=1

        algorithm = algorithm[0]
        code = code[0] 

        code_all = code+" "+", ".join(s for s in self.prompt_func_outputs) 


        return [code_all, algorithm]

    def _get_alg(self, prompt_content):

        response = self.interface_llm.get_response(prompt_content)

        algorithm = re.findall(r"\{(.*?)\}", response, re.DOTALL)
        if len(algorithm) == 0:
            if 'java' in response:
                algorithm = re.findall(r'^.*?(?=java)', response, re.DOTALL)
            elif 'import' in response:
                algorithm = re.findall(r'^.*?(?=package)', response, re.DOTALL)
            else:
                algorithm = re.findall(r'^.*?(?=public)', response, re.DOTALL)

        code = re.findall(r"package.*return.*}\s*}", response, re.DOTALL)
        if len(code) == 0:
            code = re.findall(r"public.*return.*}\s*}", response, re.DOTALL)

        n_retry = 1
        while (len(algorithm) == 0 or len(code) == 0):
            if self.debug_mode:
                print("Error: algorithm or code not identified, wait 1 seconds and retrying ... ")

            response = self.interface_llm.get_response(prompt_content)

            algorithm = re.findall(r"\{(.*?)\}", response, re.DOTALL)
            if len(algorithm) == 0:
                if 'java' in response:
                    algorithm = re.findall(r'^.*?(?=java)', response, re.DOTALL)
                elif 'import' in response:
                    algorithm = re.findall(r'^.*?(?=package)', response, re.DOTALL)
                else:
                    algorithm = re.findall(r'^.*?(?=public)', response, re.DOTALL)

            code = re.findall(r"package.*return.*}\s*}", response, re.DOTALL)
            if len(code) == 0:
                code = re.findall(r"public.*return.*}\s*}", response, re.DOTALL)

            if n_retry > 3:
                break
            n_retry += 1

        algorithm = algorithm[0]
        code = code[0]

        # code_all = code + " " + ", ".join(s for s in self.prompt_func_outputs)

        return [code, algorithm]

    def _get_simple_code(self,prompt_content):

        response = self.interface_llm.get_response(prompt_content)

        code = re.findall(r"package.*return.*}\s*}", response, re.DOTALL)
        if len(code) == 0:
            code = re.findall(r"public.*return.*}\s*}", response, re.DOTALL)

        while (len(code) == 0):
            print("Error: algorithm or code not identified, wait 1 seconds and retrying ... ")
            # time.sleep(1)

            response = self.interface_llm.get_response(prompt_content)

            code = re.findall(r"package.*return.*}\s*}", response, re.DOTALL)
            if len(code) == 0:
                code = re.findall(r"public.*return.*}\s*}", response, re.DOTALL)

        code = code[0]

        # code_all = code+" "+", ".join(s for s in self.prompt_func_outputs)


        return [code]

    def i1(self):

        prompt_content = self.get_prompt_i1()

        if self.debug_mode:
            print("\n >>> check prompt for creating algorithm using [ i1 ] : \n", prompt_content )
            print(">>> Press 'Enter' to continue")
            input()
      
        [code_all, algorithm] = self._get_alg(prompt_content)

        if self.debug_mode:
            print("\n >>> check designed algorithm: \n", algorithm)
            print("\n >>> check designed code: \n", code_all)
            print(">>> Press 'Enter' to continue")
            input()

        return [code_all, algorithm]

    def e1(self, parents):

        prompt_content = self.get_prompt_e1(parents)

        if self.debug_mode:
            print("\n >>> check prompt for creating algorithm using [ e1 ] : \n", prompt_content)
            print(">>> Press 'Enter' to continue")
            input()

        [code_all, algorithm] = self._get_alg(prompt_content)

        if self.debug_mode:
            print("\n >>> check designed algorithm: \n", algorithm)
            print("\n >>> check designed code: \n", code_all)
            print(">>> Press 'Enter' to continue")
            input()

        return [code_all, algorithm]

    def e2(self, parents):

        prompt_content = self.get_prompt_e2(parents)

        if self.debug_mode:
            print("\n >>> check prompt for creating algorithm using [ e2 ] : \n", prompt_content)
            print(">>> Press 'Enter' to continue")
            input()

        [code_all, algorithm] = self._get_alg(prompt_content)

        if self.debug_mode:
            print("\n >>> check designed algorithm: \n", algorithm)
            print("\n >>> check designed code: \n", code_all)
            print(">>> Press 'Enter' to continue")
            input()

        return [code_all, algorithm]

    def m1(self, parents):

        prompt_content = self.get_prompt_m1(parents)

        if self.debug_mode:
            print("\n >>> check prompt for creating algorithm using [ m1 ] : \n", prompt_content)
            print(">>> Press 'Enter' to continue")
            input()

        [code_all, algorithm] = self._get_alg(prompt_content)

        if self.debug_mode:
            print("\n >>> check designed algorithm: \n", algorithm)
            print("\n >>> check designed code: \n", code_all)
            print(">>> Press 'Enter' to continue")
            input()

        return [code_all, algorithm]

    def m2(self, parents):

        prompt_content = self.get_prompt_m2(parents)

        if self.debug_mode:
            print("\n >>> check prompt for creating algorithm using [ m2 ] : \n", prompt_content)
            print(">>> Press 'Enter' to continue")
            input()

        [code_all, algorithm] = self._get_alg(prompt_content)

        if self.debug_mode:
            print("\n >>> check designed algorithm: \n", algorithm)
            print("\n >>> check designed code: \n", code_all)
            print(">>> Press 'Enter' to continue")
            input()

        return [code_all, algorithm]

    def m3(self, parents):

        prompt_content = self.get_prompt_simple(parents)

        if self.debug_mode:
            print("\n >>> check prompt for creating algorithm using [ simple ] : \n", prompt_content)
            print(">>> Press 'Enter' to continue")
            input()

        [code_all] = self._get_simple_code(prompt_content)

        if self.debug_mode:
            print("\n >>> check designed code: \n", code_all)
            print(">>> Press 'Enter' to continue")
            input()

        return [code_all]

    # ############# code space operators
    def get_opts(self, operator):

        return "Modify the provided algorithm to improve its performance, where you can determine the degree of modification needed."

    def delete_lines(self, code, number_of_delete):
        lines = copy.deepcopy(code.split('\n'))
        to_be_deleted_lines_index = []  # "#" and selected lines
        content_lines_index = []  # not "#", "" and import and def

        for index, line in enumerate(lines):
            stripped_line = line.strip()
            if stripped_line.startswith('def ') or stripped_line.startswith('import') or stripped_line == "":
                continue
            if stripped_line.startswith('#'):
                to_be_deleted_lines_index.append(index)
            else:
                if "#" in line:
                    line.split('#', 1)[0].rstrip()
                content_lines_index.append(index)

        number_of_delete = np.ceil(number_of_delete * len(content_lines_index)).astype(int)  # when number_of_delete is a cofficient
        content_delete_lines = random.choices(content_lines_index, k=min([number_of_delete, len(content_lines_index)]))
        if len(content_delete_lines) == 0:
            raise Exception("No content lines to delete")
        to_be_deleted_lines_index.extend(content_delete_lines)

        new_code = ""
        for indexI, i in enumerate(lines):
            if indexI not in to_be_deleted_lines_index:
                new_code += i
                if i != lines[-1]:
                    new_code += '\n'

        return new_code, number_of_delete

    def ts_merge_features(self, code, code_feature):
        lines = copy.deepcopy(code.split('\n'))
        to_be_deleted_lines_index = []  # "#" and selected lines
        content_lines_index = []  # not "#", "" and import and def

        for index, line in enumerate(lines):
            stripped_line = line.strip()
            if stripped_line.startswith('def ') or stripped_line.startswith('import') or stripped_line == "":
                continue
            if stripped_line.startswith('#'):
                to_be_deleted_lines_index.append(index)
            else:
                if "#" in line:
                    line.split('#', 1)[0].rstrip()
                content_lines_index.append(index)

        new_code = ""
        for indexI, i in enumerate(lines[:-1]):
            if indexI not in to_be_deleted_lines_index:
                new_code += i
                new_code += '\n'

        # insert code feature
        if code_feature is not None:
            for i in code_feature:
                new_code += i
                new_code += '\n'

        new_code += lines[-1]

        return new_code

    def get_rr_opts(self, number_of_delete):

        opt = str(number_of_delete) + "lines have been removed from the provided code. Please review the code, add the necessary lines to get a better result."

        return opt

    def get_prompt_rr(self, indiv1, number_of_delete):
        deleted_code, deleted_lines = self.delete_lines(indiv1['code'], number_of_delete)

        prompt_content = self.prompt_task + "\n" \
"I have one algorithm with its code as follows. \
Algorithm description: " + indiv1['algorithm'] + "\n \
Code:\n \
" + deleted_code + "\n" \
+ self.get_rr_opts(deleted_lines) + "\n\
First, describe your new algorithm and main steps in one sentence. \
The description must be inside a brace. Next, implement it in Java as a function named \
" + self.prompt_func_name + ". This function should accept " + str(len(self.prompt_func_inputs)) + " input(s): " \
                         + self.joined_inputs + ". The function should return " + str(
            len(self.prompt_func_outputs)) + " output(s): " \
                         + self.joined_outputs + ". " + self.prompt_inout_inf + " " \
                         + self.prompt_other_inf + "\n" + "Do not give additional explanations."
        return prompt_content

    def get_prompt_ts_renew(self, indiv1, ts_element):
        renew_code = self.ts_merge_features(indiv1['code'], ts_element['code_feature'])

        prompt_content = self.prompt_task + "\n" \
"I have algorithm A with its code, which inserts key lines from algorithm B's code, inserted just before the 'return' statement. \
Algorithm A description: " + indiv1['algorithm'] + "\n \
Code:\n \
" + renew_code + "\n" + \
"Algorithm B description: " + ts_element['algorithm'] + "\n \
Please review the given code, integrating two algorithm descriptions provided to rearrange it to get a better result." + "\n\
First, describe your new algorithm and main steps in one sentence. \
The description must be inside a brace. Next, implement it in Java as a function named \
" + self.prompt_func_name + ". This function should accept " + str(len(self.prompt_func_inputs)) + " input(s): " \
                         + self.joined_inputs + ". The function should return " + str(
            len(self.prompt_func_outputs)) + " output(s): " \
                         + self.joined_outputs + ". " + self.prompt_inout_inf + " " \
                         + self.prompt_other_inf + "\n" + "Do not give additional explanations."
        return prompt_content

    # ############# proceed operators
    def o_index(self, indiv1, operator):
        prompt_content = self.get_prompt_o_index(indiv1, operator)

        if self.debug_mode:
            print("\n >>> check prompt for creating algorithm using [ m2 ] : \n", prompt_content)
            print(">>> Press 'Enter' to continue")
            input()

        [code_all, algorithm] = self._get_alg(prompt_content)

        if self.debug_mode:
            print("\n >>> check designed algorithm: \n", algorithm)
            print("\n >>> check designed code: \n", code_all)
            print(">>> Press 'Enter' to continue")
            input()

        return [code_all, algorithm]

    def rr(self, indiv1, number_of_delete):

        prompt_content = self.get_prompt_rr(indiv1, number_of_delete)

        if self.debug_mode:
            print("\n >>> check prompt for creating algorithm using [ m2 ] : \n", prompt_content)
            print(">>> Press 'Enter' to continue")
            input()

        [code_all, algorithm] = self._get_alg(prompt_content)
        code_features = self._find_llm_generate_lines(indiv1['code'], code_all)

        if self.debug_mode:
            print("\n >>> check designed algorithm: \n", algorithm)
            print("\n >>> check designed code: \n", code_all)
            print(">>> Press 'Enter' to continue")
            input()

        return [code_all, algorithm, code_features]

    def p1(self, indiv1):
        prompt_content = self.get_prompt_o_index(indiv1, 0)

        if self.debug_mode:
            print("\n >>> check prompt for creating algorithm using [ m2 ] : \n", prompt_content)
            print(">>> Press 'Enter' to continue")
            input()

        [code_all, algorithm] = self._get_alg(prompt_content)
        code_features = self._find_llm_generate_lines(indiv1['code'], code_all)

        if self.debug_mode:
            print("\n >>> check designed algorithm: \n", algorithm)
            print("\n >>> check designed code: \n", code_all)
            print(">>> Press 'Enter' to continue")
            input()

        return [code_all, algorithm, code_features]

    def p2(self, indiv1, ts_element):
        prompt_content = self.get_prompt_ts_renew(indiv1, ts_element)

        if self.debug_mode:
            print("\n >>> check prompt for creating algorithm using [ m2 ] : \n", prompt_content)
            print(">>> Press 'Enter' to continue")
            input()

        [code_all, algorithm] = self._get_alg(prompt_content)
        code_features = self._find_llm_generate_lines(indiv1['code'], code_all)

        if self.debug_mode:
            print("\n >>> check designed algorithm: \n", algorithm)
            print("\n >>> check designed code: \n", code_all)
            print(">>> Press 'Enter' to continue")
            input()

        return [code_all, algorithm, code_features]

    # ##################### other tools

    def _find_llm_generate_lines(self, prev_ind_code, new_ind_code):
        p_code = prev_ind_code
        n_code = new_ind_code
        p_lines = copy.deepcopy(p_code.split('\n'))
        n_lines = copy.deepcopy(n_code.split('\n'))
        prev_content_lines = []  # not "#", "" and import and def
        new_content_lines = []  # not "#", "" and import and def

        code_features = []

        # extract effective lines
        for index, line in enumerate(p_lines):
            stripped_line = line.strip()
            if stripped_line.startswith('def ') or stripped_line.startswith(
                    'import') or stripped_line == "" or stripped_line.startswith('#'):
                continue
            else:
                if "#" in line:
                    line.split('#', 1)[0].rstrip()
                prev_content_lines.append(line.strip())

        for index, line in enumerate(n_lines):
            stripped_line = line.strip()
            if stripped_line.startswith('def ') or stripped_line.startswith(
                    'import') or stripped_line == "" or stripped_line.startswith('#'):
                continue
            else:
                if "#" in line:
                    line.split('#', 1)[0].rstrip()
                new_content_lines.append(line.rstrip())

        # compare
        for line in new_content_lines:
            if line.strip() not in prev_content_lines:
                code_features.append(line)
        return code_features
