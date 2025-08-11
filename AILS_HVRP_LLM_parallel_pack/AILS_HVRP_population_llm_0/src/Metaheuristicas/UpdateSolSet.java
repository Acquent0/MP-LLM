package Metaheuristicas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;

public class UpdateSolSet {
    public static void updateSolSet(SolSetParams gp, Solucao[] solSet, Solucao sol, double refDist, BiFunction<Solucao, Solucao, Double> hammingDist, Random rand) {

        /*
         * Updates the solution set solSet by incorporating a new solution and maintaining the quality of the set.
         * It ensures that high-quality solutions are retained while adhering to the maximum solution set size.
         *
         * @param gp           SolSetParams, the parameters controlling the solution set, including size limits and indexes.
         *                     Contains metadata: the current number of solutions (`numIdv`) and the index of the worst solution (`worstIndex`) and the max number of solutions ('maxSize').
         * @param solSet       Solucao[], an array representing the current solution set. Each element is a high-quality solution
         *                     identified during the search process.
         * @param sol          Solucao, the new solution to be evaluated and potentially added to the solution set.
         * @param refDist      double, the maximum Hamming distance threshold used to determine whether a solution is sufficiently
         *                     distinct from others in the solution set.
         * @param hammingDist  BiFunction<Solucao, Solucao, Double>, a function to compute the Hamming distance between two solutions.
         *                     Used to measure the similarity between solutions.
         * @param rand         Random, an instance for performing any stochastic operations needed during the update process.
         */

        double baseDiversityWeight = 0.2;
        double baseFitnessWeight = 0.4;
        double baseNoveltyWeight = 0.4;

        double entropy = calculateEntropy(solSet, gp.numIdv, hammingDist);
        double diversityWeight = baseDiversityWeight + (1 - entropy) * 0.5;
        double fitnessWeight = baseFitnessWeight + entropy * 0.3;
        double noveltyWeight = baseNoveltyWeight + entropy * 0.2;

        if (gp.numIdv == 0) {
            solSet[gp.numIdv++] = sol;
            return;
        }

        List<List<Integer>> clusters = new ArrayList<>();
        for (int i = 0; i < gp.numIdv; i++) {
            boolean addedToCluster = false;
            for (List<Integer> cluster : clusters) {
                if (hammingDist.apply(solSet[cluster.get(0)], solSet[i]) <= refDist) {
                    cluster.add(i);
                    addedToCluster = true;
                    break;
                }
            }
            if (!addedToCluster) {
                List<Integer> newCluster = new ArrayList<>();
                newCluster.add(i);
                clusters.add(newCluster);
            }
        }

        int bestClusterIndex = -1;
        double bestClusterScore = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < clusters.size(); i++) {
            double clusterDiversity = 0;
            double clusterFitness = 0;
            double clusterNovelty = 0;
            for (int j = 0; j < clusters.get(i).size(); j++) {
                int index1 = clusters.get(i).get(j);
                for (int k = j + 1; k < clusters.get(i).size(); k++) {
                    int index2 = clusters.get(i).get(k);
                    clusterDiversity += hammingDist.apply(solSet[index1], solSet[index2]);
                    clusterNovelty += rand.nextDouble(); // Pseudo novelty score
                }
                clusterFitness += solSet[index1].f;
            }
            clusterDiversity /= clusters.get(i).size();
            clusterFitness /= clusters.get(i).size();
            clusterNovelty /= clusters.get(i).size();

            double clusterScore = (diversityWeight * clusterDiversity) + (fitnessWeight * clusterFitness) + (noveltyWeight * clusterNovelty);
            if (clusterScore > bestClusterScore) {
                bestClusterScore = clusterScore;
                bestClusterIndex = i;
            }
        }

        boolean newClusterFormation = true;
        for (int idx : clusters.get(bestClusterIndex)) {
            if (hammingDist.apply(sol, solSet[idx]) <= refDist) {
                newClusterFormation = false;
                break;
            }
        }

        if (newClusterFormation || sol.f < solSet[gp.worstIndex].f) {
            if (gp.numIdv < gp.maxSize) {
                solSet[gp.numIdv++] = sol;
            } else {
                solSet[gp.worstIndex] = sol;
            }
        }
        
        Arrays.sort(solSet, 0, gp.numIdv, new SolSetParams.SolucaoCostComparator());
        gp.worstIndex = gp.numIdv - 1;
    }
    
    public static double calculateEntropy(Solucao[] solSet, int numIdv, BiFunction<Solucao, Solucao, Double> hammingDist) {
        double entropy = 0.0;
        if (numIdv < 2) return entropy;
        for (int i = 0; i < numIdv; i++) {
            for (int j = i + 1; j < numIdv; j++) {
                double dist = hammingDist.apply(solSet[i], solSet[j]);
                entropy += dist * Math.log(dist + 1);
            }
        }
        return entropy / (numIdv * (numIdv - 1) / 2);
    }
}