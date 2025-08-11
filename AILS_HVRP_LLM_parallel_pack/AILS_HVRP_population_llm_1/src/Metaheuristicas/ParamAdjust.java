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

        // Polynomial decay through quartic progress
        double polynomialDecay = 1 - Math.pow(searchProgress, 4);

        // Normalized entropy for diversity measurement
        double entropy = -populationRatio * Math.log(populationRatio + 1e-10) - (1 - populationRatio) * Math.log(1 - populationRatio + 1e-10);
        
        // Fractional Brownian motion based stochastic modulation
        double stochasticModulation = Math.abs(rand.nextGaussian()) * Math.pow(instanceSize, -0.5);

        // Decay-momentum synergy
        double decayMomentum = polynomialDecay * entropy + stochasticModulation;

        // Combined adjustment with constraints
        double refDist = decayMomentum * (0.5 + 0.5 * lastParamValue);
        
        return Math.max(0.0, Math.min(1.0, refDist));
    }
}