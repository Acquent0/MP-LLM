import math
import random

def acceptance_probability(old_cost, new_cost, temperature):
    if new_cost < old_cost:
        return 1.0
    return math.exp(((old_cost - new_cost)/old_cost) / temperature)

def population_management(old, new, temperature):
    
    if (new['objective'] != None) and (acceptance_probability(old['objective'], new['objective'], temperature) > random.random()):
        return True
        
    return False