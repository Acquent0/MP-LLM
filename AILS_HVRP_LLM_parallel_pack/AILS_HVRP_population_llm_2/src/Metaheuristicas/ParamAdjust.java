package Metaheuristicas;
import java.util.Random;

public class ParamAdjust {
    public static double adjustParamRefDist(int instanceSize, double populationRatio, double searchProgress, double lastParamValue, Random rand) {

        /*
         * Adjusts a parameter based on the current search context and progress.
         * 
         * @param instanceSize    int, number of nodes of current instance.
         * @param populationRatio double, a value between [0, 1] indicating the ratio of the current population size to the max population capacity.
         * @param searchProgress  double, a value between [0, 1] indicating the current progress of the search. A value of 0 marks the beginning, and 1 marks completion.
         * @param lastParamValue  double, the previous value of the parameter to be adjusted, serving as a reference for current adjustment.
         * @param rand            Random, an instance for performing any stochastic operations needed to adjust the parameter.
         *
         * @return refDist        double, a value between [0, 1], the adjusted parameter value computed based on input conditions and search state.
         */

        // Use a quadratic progression factor for exploration and convergence 
        double quadraticProgress = Math.pow(searchProgress, 2);

        // Normalize instance size and population ratio 
        double normalizationFactor = 0.5 * (instanceSize / 100.0) * populationRatio;

        // Constant baseline randomness to maintain exploration 
        double constantRandomFactor = 0.05 * rand.nextDouble();

        // Compute the adjusted parameter value
        double refDist = normalizationFactor * quadraticProgress + constantRandomFactor; 

        // Ensure the reference distance stays within valid range [0.0, 1.0]
        return Math.min(1.0, Math.max(0.0, refDist));
    }
}