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

        // Logarithmic scaling based on search progress
        double logScaling = 1.0 / (1.0 + Math.log10(1 + instanceSize * searchProgress));

        // Adaptive modulation based on population ratio
        double adaptiveModulation = populationRatio * (0.1 + (rand.nextDouble() * 0.3));

        // Combine factors to determine adjusted reference distance
        double refDist = logScaling * (0.9 * lastParamValue) + adaptiveModulation;

        // Constrain value to [0, 1]
        return Math.max(0.0, Math.min(1.0, refDist));
    }
}