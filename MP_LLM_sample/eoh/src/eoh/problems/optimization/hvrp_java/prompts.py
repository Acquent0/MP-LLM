class GetPrompts():
    def __init__(self):
        self.prompt_task = "Task: Design a heuristic strategy to determine 5 parameters within given range."
        self.prompt_func_name = "update_edge_distance"
        self.prompt_func_inputs = ['edge_distance', 'adjacency_matrix', 'average_nodes']
        self.prompt_func_outputs = ['updated_edge_distance']
        self.prompt_inout_inf = "'adjacency_matrix' and 'edge_distance' are matrices, 'demand_capacity' indicates average number of nodes of each tour"
        self.prompt_other_inf = ""

        self.format = r'''
package Perturbation;

import java.util.Random;
import Solution.Node;

public class WeightMatrixUpdator {

public static int[] nodes_seq(int[][] nodesKnn, int numberSelect, Node[] nodes, float average_nodes) {

    // nodesKnn: an int[][] where each element represents the 100 nearest nodes id for a given node, from node 0 to n.
    // numberSelect: the number of selected nodes. 
    // `nodes` is an array of type `Node[]`, from node 1 to n, where each `Node` has only the following properties:
        // - `Node.next`: The next Node connected to the current node.
        // - `Node.prev`: The previous Node connected to the current node.
        // - `Node.nodeBelong`: A boolean indicating whether the node is not selected (initially true, set to false when the node is selected).
        // - `Node.name`: An integer representing the node's ID.
    // average_nodes: the average number of nodes of each route.
    // node 0 should not be selected.

        @ Design Strategy here

        return scores;
    }

}

'''

    def get_task(self):
        return self.prompt_task
    
    def get_func_name(self):
        return self.prompt_func_name
    
    def get_func_inputs(self):
        return self.prompt_func_inputs
    
    def get_func_outputs(self):
        return self.prompt_func_outputs
    
    def get_inout_inf(self):
        return self.prompt_inout_inf

    def get_other_inf(self):
        return self.prompt_other_inf

    def get_format(self):
        return self.format
