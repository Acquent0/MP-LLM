package Metaheuristicas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;

import Dados.Instancia;


public class SolSetParams {

    Instancia instancia;
    Random rand;

    Solucao[] population;
    Solucao melhorSolucao;
    Distancia distInTwoSolucao;

    int maxSize;
    int numIdv;

    //other parameters
    double melhorF;
    int worstIndex;

    double MAX;
    Config config;
    int numIterUpdate;
    double epsilon;
    public long srcCost = 0;


    public SolSetParams(int maxSize, Instancia instancia, Config config, Random rand, int randNum) {
        this.rand = rand;
        this.srcCost = 0;
        this.maxSize = maxSize;
        this.numIdv = 0;
        this.population = new Solucao[maxSize];
        this.config = config;
        this.instancia = instancia;
        distInTwoSolucao = new Distancia(instancia,config);

        this.epsilon=config.getEpsilon();
        this.melhorSolucao =new Solucao(instancia,config, randNum);
        this.melhorF = Double.MAX_VALUE;
        this.worstIndex = 0;

        this.numIterUpdate=config.getGamma();
        this.config=config;
        this.melhorF=Integer.MAX_VALUE;

    }

    public void empty(){
        srcCost = 0;
        numIdv = 0;
    }

    public Solucao get(int index) {
        return this.population[index];
    }

    public Solucao findBestSolucao() {
        double tempo = Double.MAX_VALUE;
        int bestIndex = 0;

        for (int i = 0; i < numIdv; i++) {
            if (this.population[i].f < tempo) {
                tempo = this.population[i].f;
                bestIndex = i;
            }
        }

        melhorF = tempo;
        melhorSolucao = this.population[bestIndex];

        return this.melhorSolucao;
    }


    // simplifies version
    public void addElite_SV(Solucao solucao) {
        List<Integer> mdP = new ArrayList<>();
        int mdpNum;
        double dist;

        if (numIdv == 0){
            population[numIdv++] = solucao;
            return;
        }

        for (int i = 0; i < numIdv; i++) {
            dist = distInTwoSolucao.CalHammingDis(solucao, population[i]);
            if(population[i].f >= solucao.f && dist <= config.getRefDist()){
                mdP.add(i);
            }

            if(population[i].f > population[worstIndex].f){
                worstIndex = i;
            }
        }

        mdpNum = mdP.size();

        if(numIdv < maxSize || solucao.f - population[worstIndex].f < -epsilon){
            if (mdpNum > 0){
                // remove solucao in population if it's in mdp
                for (int i = 0; i < mdpNum; i++) {
                    population[mdP.get(i)].f = Double.MAX_VALUE;
                }
                numIdv -= mdpNum;

                Arrays.sort(population, 0, numIdv+mdpNum, new SolucaoCostComparator());
                population[numIdv++] = solucao;
            }
            else{
                if (numIdv < maxSize){
                    population[numIdv++] = solucao;
                }
                else{
                    population[worstIndex] = solucao;
                }
            }
        }

    }

    public void addElite1(Solucao solucao) {
        BiFunction<Solucao, Solucao, Double> hammingDist = distInTwoSolucao::CalHammingDis;
        UpdateSolSet.updateSolSet(this, this.population, solucao, config.getRefDist(), hammingDist, rand);
    }

    //keep more sub sols version
    public void addElite(Solucao solucao) {
        List<Integer> mdP = new ArrayList<>();
        int mdpNum;
        double dist;

        if (numIdv == 0){
            population[numIdv++] = solucao;
            return;
        }

        for (int i = 0; i < numIdv; i++) {
            dist = distInTwoSolucao.CalHammingDis(solucao, population[i]);
            if(population[i].f >= solucao.f && dist <= config.getRefDist()){
                mdP.add(i);
            }

            if(population[i].f > population[worstIndex].f){
                worstIndex = i;
            }
        }

        mdpNum = mdP.size();

        if(numIdv < maxSize || solucao.f - population[worstIndex].f < -epsilon){
            if (mdpNum > 0){
                for (int i = 0; i < mdpNum; i++) {
                    population[mdP.get(i)].f = Double.MAX_VALUE;
                }
                numIdv -= mdpNum;

                Arrays.sort(population, 0, numIdv+mdpNum, new SolucaoCostComparator());
                population[numIdv++] = solucao;
            }
            else{
                if (numIdv < maxSize){
                    population[numIdv++] = solucao;
                }
                else{
                    population[worstIndex] = solucao;
                }
            }
        }

    }

    //search and filter
    public void addEliteTrail(Solucao solucao) {
        List<Integer> mdP = new ArrayList<>();
        int mdpNum;
        double dist;

        if (numIdv == 0){
            population[numIdv++] = solucao;
            return;
        }

        for (int i = 0; i < numIdv; i++) {
            dist = distInTwoSolucao.CalHammingDis(solucao, population[i]);
            if(population[i].f >= solucao.f && dist <= config.getRefDist()){
                mdP.add(i);
            }

            if(population[i].f > population[worstIndex].f){
                worstIndex = i;
            }
        }

        mdpNum = mdP.size();

//        if(numIdv < size || solucao.f - population[worstIndex].f < -epsilon){
        if(solucao.f - population[worstIndex].f < -epsilon){
            if (mdpNum > 0){
                // remove solucao in population if it's in mdp
                for (int i = 0; i < mdpNum; i++) {
                    population[mdP.get(i)].f = Double.MAX_VALUE;
                }
                numIdv -= mdpNum;

                Arrays.sort(population, 0, numIdv+mdpNum, new SolucaoCostComparator());
                population[numIdv++] = solucao;
            }
            else {
                population[numIdv++] = solucao;
            }
//            else{
//                if (numIdv < size){
//                    population[numIdv++] = solucao;
//                }
//                else{
//                    population[worstIndex] = solucao;
//                }
//            }
        }

    }

    public void addEliteOri(Solucao solucao) {
        int[] mdP = new int[numIdv];
        int[] mdM = new int[numIdv];
        int mdpNum = 0;
        int mdmNum = 0;
        double dist = 0;
        int worstIndex = 0;

        if (numIdv == 0){
            population[numIdv++] = solucao;
            return;
        }

        for (int i = 0; i < numIdv; i++) {
            dist = distInTwoSolucao.CalHammingDis(solucao, population[i]);
            if(population[i].f >= solucao.f & dist <= config.getRefDist()){
                mdP[mdpNum++] = i;
            }
            else if (population[i].f < solucao.f & dist <= config.getRefDist()){
                mdM[mdmNum++] = i;
            }

            if(population[i].f > population[worstIndex].f){
                worstIndex = i;
            }
        }

        if(mdmNum == 0 & (numIdv < maxSize || solucao.f - population[worstIndex].f < -epsilon)){
            if (mdpNum > 0){
                // remove solucao in population if it's in mdp
                for (int i = 0; i < mdpNum; i++) {
                    population[mdP[i]].f = Double.MAX_VALUE;
                }
                numIdv -= mdpNum;

                Arrays.sort(population, 0, maxSize, new SolucaoCostComparator());
                population[numIdv++] = solucao;
            }
            else{
                if (numIdv < maxSize){
                    population[numIdv++] = solucao;
                }
                else{
                    population[worstIndex] = solucao;
                }
            }
        }
    }

    public Solucao getBestSolucao() {
        return this.melhorSolucao;
    }

    public int getNumIdv() {
        return this.numIdv;
    }

    public Solucao[] getPopulation() {
        return this.population;
    }

    // realize SolucaoComparator
    public static class SolucaoDivComparator implements java.util.Comparator<Solucao> {
        public int compare(Solucao solucaoA, Solucao solucaoB) {
            if (solucaoA.getDivValue() > solucaoB.getDivValue()) {
                return 1;
            } else if (solucaoA.getDivValue() < solucaoB.getDivValue()) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    public static class SolucaoCostComparator implements java.util.Comparator<Solucao> {
        public int compare(Solucao solucaoA, Solucao solucaoB) {
            if (solucaoA.f > solucaoB.f) {
                return 1;
            } else if (solucaoA.f < solucaoB.f) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    public static class SolucaoFitnessComparator implements java.util.Comparator<Solucao> {
        public int compare(Solucao solucaoA, Solucao solucaoB) {
            if (solucaoA.getFitness() > solucaoB.getFitness()) {
                return 1;
            } else if (solucaoA.getFitness() < solucaoB.getFitness()) {
                return -1;
            } else {
                return 0;
            }
        }
    }

}







