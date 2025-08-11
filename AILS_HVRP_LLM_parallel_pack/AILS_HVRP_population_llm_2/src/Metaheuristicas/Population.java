package Metaheuristicas;


import CriterioAceitacao.CA;
import CriterioAceitacao.CALimiar;
import Dados.Instancia;
import Crossover.Crossover;
import Mutation.Mutation;
//import Plot.twoDScatter;

//import org.apache.poi.ss.usermodel.*;

//import java.io.*;
//import java.nio.file.Files;
//import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
//import org.apache.commons.math3.stat.StatUtils;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Population {

    Instancia instancia;
    SolSetParams eliteSet;
    SolSetParams[] elitePop;
    int elitePopSize = 0;
    Solucao melhorSolucao;

    // operations
    Crossover crossover;
    Mutation mutation;
    CA criterioAceitacao;
    Distancia distEntreSolucoes;
//    twoDScatter plot = new twoDScatter();

    //other parameters
    double melhorF;
    double MAX;
    double otimo;
    Config config;
    int numIterUpdate;
    int iterador;
    long inicio,inicioLastBestSol;
    long ini;
    double tempoMF,tempoFinal;
    int iteradorMF;
    Media mFBL;
    Random rand;  // 2025
    double distanciaBLEdge=0;
    int numRotasMin;
    int numRotasMax;
    boolean print=true;
    boolean factibilizou;
    double epsilon;
    private StoppingCriterion stoppingCriterion;
    long srcCost = 0;
    long bestSrcCost = 0;
    public double timeUntilBest = 0;

    public String problemType;
    public static int randNum;

    static double[] optimals;
    static double[] optimals_HVRPFD = {13085.077097956533, 0, 0, 0, 0, 2233.90,
            2851.94, 2378.99082556146, 1839.21673823207, 2047.80590624202, 3185.08870574379, 10107.5290365555, 3065.29388423529, 3265.41226692788,
            2076.95695094159, 3743.58009841382, 10420.3438245153, 4760.68};

    static double[] optimals_HVRPD = {12050.08, 10130.30, 16192.26, 17273.75, 23024.58, 2233.90,
            2851.94, 2378.99082556146, 1839.21673823207, 2047.80590624202, 1517.84, 607.53, 1015.29, 1144.94, 1061.96, 1823.58, 1117.51, 1534.17};

    static double[] optimals_FSMFD = {11747.38747, 0, 0, 0, 0, 2211.63, 2811.37, 2234.57,
            1822.78, 2016.79, 2964.65, 9126.90, 2634.96, 3168.92, 2004.48, 3147.99, 8661.81, 4153.02};

    static double[] optimals_FSMF = {0, 0, 0, 0, 0, 0, 0, 1042.115027, 762.0455829, 1078.101175, 2406.36, 9119.03,
            2586.37, 2720.43, 1734.53, 2369.65, 8661.81, 4029.61};

    static double[] optimals_FSMD = {0, 0, 0, 0, 0, 2211.63, 2811.37, 2234.57,
            1822.78, 2016.79, 1491.86, 603.21, 999.82, 1131.00, 1038.60, 1800.80, 1105.44, 1530.43};


    static double[] cvrpOpt = {27591, 26362, 14971, 12747, 13332, 55539, 28940, 10916, 13590, 15700, 43448, 21220, 16876, 14138, 20557, 45607, 47812, 25569, 24145, 16980,
            44225, 58578, 19565, 30656, 10856, 117595, 40437, 25742, 19230, 27042, 82751, 37274, 38684, 18839, 26558, 75478, 35291, 21245, 33503, 20215, 95151,
            47161, 34231, 21736, 25859, 94043, 78355, 29834, 27532, 31102, 139111, 42050, 25896, 51505, 22814, 147713, 65928,
            38260, 66154, 19712, 107798, 65449, 36391, 55233, 24139, 221824, 89449, 66483, 69226, 24201, 154593, 94846, 86700,
            42717, 50673, 190316, 108451, 59535, 62164, 63682, 106780, 146332, 68205, 81923, 43373, 136187, 77269, 114417, 72386,
            73305, 158121, 193737, 88965, 99299, 53860, 329179, 132715, 85465, 118976, 72355};

    double timeSolCon = 0;
    double timeCluster = 0;
    static int variantType = 0;

    List<Long> srcEachIter = new ArrayList<Long>();
    List<Double> timeEach = new ArrayList<Double>();
    List<Double> fEach = new ArrayList<Double>();

    public Population(Instancia instancia, Config config, double d, double MAX, int randNum){
        this.rand = new Random(randNum);
        this.instancia=instancia;
        this.crossover=new Crossover(instancia,config, randNum);
        this.mutation=new Mutation(instancia,config, randNum);

        this.epsilon=config.getEpsilon();

        this.elitePop = new SolSetParams[config.getPopSize()];
        this.eliteSet = new SolSetParams(config.getPopSize(), instancia, config, rand, randNum);
        for (int i = 0; i < config.getPopSize(); i++) {
            elitePop[i] = new SolSetParams(config.getSubPopSize(), instancia, config, rand, randNum);
        }
        this.melhorSolucao =new Solucao(instancia,config, randNum);
        this.stoppingCriterion=config.getStoppingCriterion();
        this.numIterUpdate=config.getGamma();
        this.config=config;
        this.otimo=d;
        this.MAX=MAX;
        this.melhorF=Integer.MAX_VALUE;

        this.mFBL=new Media(numIterUpdate);

        this.numRotasMin=instancia.getNumRotasMin();
        this.numRotasMax=instancia.getNumRotasMax();

        this.criterioAceitacao=new CALimiar(config);
        this.distEntreSolucoes=new Distancia(instancia,config);

    }

    // non parallel
    public void initilizePopulation(){
        iterador=0;
        inicio=System.currentTimeMillis();
        inicioLastBestSol=inicio;
        eliteSet.numIdv=0;

        for (int i = 0; i < config.getPopSize(); i++) {

            Solucao newSolucao = new Solucao(instancia,config, randNum);

            do
            {
                newSolucao.construirSolucao(numRotasMin);
                factibilizou=newSolucao.factibilizador.factibilizar(newSolucao);
                srcCost += newSolucao.factibilizador.srcCost;
            }
            while (!factibilizou);

            newSolucao.buscaLocal.buscaLocal(newSolucao,true);
            srcCost += newSolucao.buscaLocal.srcCost;
            eliteSet.population[eliteSet.numIdv++] = newSolucao;
        }

    }

    // non parallel
    public List<Solucao> proceedSubGroup(){
        for (int i = 0; i < config.getSubPopSize(); i++) {
            SolSetParams gp = elitePop[i];
            int chosenIndex = 0;
            int cntBest = 0;

            for (int j = 0; j < 100; j++) {
                //choose a solucao among gp according to the f value using roulette wheel
                double sumF = 0;
                Solucao chosenSolucao = new Solucao(instancia,config, randNum);

                for (int k = 0; k < gp.numIdv; k++) {
                    sumF += gp.population[k].f;
                }

                double randNum = rand.nextDouble();
                double accumulativeP = 0;
                for (int k = 0; k < gp.numIdv; k++) {
                    accumulativeP += gp.population[k].f / sumF;
                    if (randNum <= accumulativeP) {
                        chosenIndex = k;
                        break;
                    }
                }

                gp.population[chosenIndex].perturbacaoEscolhida=gp.population[chosenIndex].perturbadores[rand.nextInt(gp.population[chosenIndex].perturbadores.length)];

                //perturbation
                do
                {
                    chosenSolucao.clone(gp.population[chosenIndex]);
                    chosenSolucao.perturbacaoEscolhida=chosenSolucao.perturbadores[rand.nextInt(chosenSolucao.perturbadores.length)];  //randomly select a perturbation
                    chosenSolucao.perturbacaoEscolhida.perturbar(chosenSolucao);
                    srcCost += chosenSolucao.perturbacaoEscolhida.srcCost;
                    factibilizou=chosenSolucao.factibilizador.factibilizar(chosenSolucao);
                    srcCost += chosenSolucao.factibilizador.srcCost;
                }
                while (!factibilizou);

                //LS
                chosenSolucao.buscaLocal.buscaLocal(chosenSolucao,true);
                srcCost += chosenSolucao.buscaLocal.srcCost;
                distanciaBLEdge=distEntreSolucoes.distanciaEdge(chosenSolucao, gp.population[chosenIndex]);
                chosenSolucao.perturbacaoEscolhida.getConfiguradorOmegaEscolhido().setDistancia(distanciaBLEdge);

                if(chosenSolucao.criterioAceitacao.aceitaSolucao(chosenSolucao,distanciaBLEdge, j))// distanciaBLEdge and j are useless
                {
                    gp.addElite(chosenSolucao);
                    cntBest = 0;
                }
                else{
                    cntBest++;
                    gp.addElite(chosenSolucao);
                }

                if(cntBest >= 5){
                    break;
                }

            }
        }

        // take all solucaos in elitePop out
        List<Solucao> allOutSols = new ArrayList<>();
        for (int i = 0; i < elitePopSize; i++) {
            for (int j = 0; j < elitePop[i].numIdv; j++) {
                allOutSols.add(elitePop[i].population[j]);
            }
        }

        return allOutSols;
    }

    public class IniSingleSol implements Callable<Solucao> {
        public Solucao solucao;

        public Solucao call() throws Exception{
            solucao = initializeSingleSol();
            return solucao;
        }
    }

    public class SubGroupProcessSol implements Callable<SolSetParams> {
        public SolSetParams inPG;
        public SolSetParams outPG;

        public SubGroupProcessSol(SolSetParams inPG){
            this.inPG = inPG;
        }

        public SolSetParams call() throws Exception{
            outPG = proceedSubGroupSingle(inPG);
            return outPG;
        }
    }

    public synchronized void addSrcCost(Solucao s, long sc){
        s.srcCost += sc;
    }

    public synchronized void addSingleSol(Solucao solucao){
        eliteSet.population[eliteSet.numIdv++] = solucao;
    }

    public Solucao initializeSingleSol(){

        Solucao newSolucao = new Solucao(instancia,config, randNum);

        do
        {
            newSolucao.construirSolucao(numRotasMin);
            factibilizou=newSolucao.factibilizador.factibilizar(newSolucao);
            addSrcCost(newSolucao, newSolucao.factibilizador.srcCost);
        }
        while (!factibilizou);

        newSolucao.buscaLocal.buscaLocal(newSolucao,true);
        addSrcCost(newSolucao, newSolucao.buscaLocal.srcCost);

        return newSolucao;
    }

    public SolSetParams proceedSubGroupSingle(SolSetParams oneElitePop){

        double sumF = 0;
        int chosenIndex = 0;
        int cntBest = 0;
        double randNum;
        double accumulativeP;
        long timeIni = System.currentTimeMillis();

        for (int j = 0; j < config.getGeneration(); j++) {
            // choose a solucao among gp according to the f value using roulette wheel, but the probability is accumulated through iterations
//            sumF = 0;
//            double[] coff = new double[oneElitePop.numIdv];
//            Solucao chosenSolucao = new Solucao(instancia, config);
//
//            for (int k = 0; k < oneElitePop.numIdv; k++) {
//                coff[k] = Math.pow(oneElitePop.population[k].f, Math.log(j + Math.E));
////                sumF += coff[k];
//                sumF += 1 / coff[k];
//            }
//            randNum = rand.nextDouble();
//            accumulativeP = 0;
//            for (int k = 0; k < oneElitePop.numIdv; k++) {
//                accumulativeP += coff[k] / sumF;
//                if (randNum <= accumulativeP) {
//                    chosenIndex = k;
//                    break;
//                }
//            }


            //choose a solucao among gp according to the f value using roulette wheel
            sumF = 0;

            Solucao chosenSolucao = new Solucao(instancia, config, Population.randNum);

            for (int k = 0; k < oneElitePop.numIdv; k++) {
                sumF += (1/oneElitePop.population[k].f);
            }

            randNum = rand.nextDouble();
            accumulativeP = 0;
            for (int k = 0; k < oneElitePop.numIdv; k++) {
                accumulativeP += (1/oneElitePop.population[k].f) / sumF;
                if (randNum <= accumulativeP) {
                    chosenIndex = k;
                    break;
                }
            }

            // choose the best solucao
//            Solucao chosenSolucao = new Solucao(instancia, config);
//
//            double best_f = Double.MAX_VALUE;
//            for (int k = 0; k < oneElitePop.numIdv; k++) {
//                if (oneElitePop.population[k].f < best_f) {
//                    best_f = oneElitePop.population[k].f;
//                    chosenIndex = k;
//                }
//            }

            // choose the worst solucao
//            Solucao chosenSolucao = new Solucao(instancia, config);
//
//            double best_f = 0;
//            for (int k = 0; k < oneElitePop.numIdv; k++) {
//                if (oneElitePop.population[k].f > best_f) {
//                    best_f = oneElitePop.population[k].f;
//                    chosenIndex = k;
//                }
//            }

            // choose random solucao
//            Solucao chosenSolucao = new Solucao(instancia, config);
//            chosenIndex = rand.nextInt(oneElitePop.numIdv);

            oneElitePop.population[chosenIndex].perturbacaoEscolhida = oneElitePop.population[chosenIndex].perturbadores[rand.nextInt(oneElitePop.population[chosenIndex].perturbadores.length)];

            //perturbation
            do {
                chosenSolucao.clone(oneElitePop.population[chosenIndex]);
                chosenSolucao.perturbacaoEscolhida = chosenSolucao.perturbadores[rand.nextInt(chosenSolucao.perturbadores.length)];  //randomly select a perturbation
                chosenSolucao.perturbacaoEscolhida.perturbar(chosenSolucao);
                oneElitePop.srcCost += chosenSolucao.perturbacaoEscolhida.srcCost;
                factibilizou = chosenSolucao.factibilizador.factibilizar(chosenSolucao);
                oneElitePop.srcCost += chosenSolucao.factibilizador.srcCost;
            }
            while (!factibilizou);

            //LS
            chosenSolucao.buscaLocal.buscaLocal(chosenSolucao, true);
            oneElitePop.srcCost += chosenSolucao.buscaLocal.srcCost;
            distanciaBLEdge = distEntreSolucoes.distanciaEdge(chosenSolucao, oneElitePop.population[chosenIndex]);
            chosenSolucao.perturbacaoEscolhida.getConfiguradorOmegaEscolhido().setDistancia(distanciaBLEdge);

            if (chosenSolucao.criterioAceitacao.aceitaSolucao(chosenSolucao, distanciaBLEdge, j))  // distanciaBLEdge and j are useless
            {
                cntBest = 0;
            } else {
                cntBest++;

            }

            oneElitePop.addElite(chosenSolucao);
            if (cntBest >= config.getConevergeTimes()) {
                break;
            }

            // test for single group
//            melhorSolucao.clone(oneElitePop.findBestSolucao());
//            if (melhorSolucao.f - melhorF < -epsilon) {
//                melhorF=melhorSolucao.f;
//                iteradorMF = iterador;
//                tempoMF = (double) (System.currentTimeMillis() - timeIni) / 1000;
//
//                if(print){
//                    System.out.println("melhorF: "+melhorF+" K: "+melhorSolucao.NumRotas
//                                    +" tempoMF: "+tempoMF
//                                    +" iteradorMF: "+iteradorMF
//                                    +" srcCost: "+srcCost
////                        +" gap: "+ 100 * (melhorF-cvrpOpt[ii])/optimals[ii] + "%"
////                        +" gap: "+ 100 * (melhorF-optimals[ii])/(optimals[ii] + 0.0001) + "%"
//                                    +" localNum: " + elitePopSize
//                    );
//                    bestSrcCost = srcCost;
//                    timeUntilBest = tempoMF;
//
//                    // using for plot
//                    timeEach.add(tempoMF);
//                    srcEachIter.add(srcCost);
//                    fEach.add(melhorF);
//                }
//            }
//            if ((double) (System.currentTimeMillis() - timeIni) / 1000 >= 60){
//                break;
//            }

        }

        // take all solucaos in elitePop out
        return oneElitePop;

    }

    public ExecutorService IniParallelProcess(int numOfTask) throws ExecutionException, InterruptedException {
        iterador=0;
        inicio=System.currentTimeMillis();
        eliteSet.numIdv=0;

        int numberOfThreads = Runtime.getRuntime().availableProcessors();
        System.out.println("numberOfThreads: " + numberOfThreads);
        double time1 = System.currentTimeMillis();
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        System.out.println("time to create executor: " + (System.currentTimeMillis() - time1) / 1000 + "s");
        for (int i = 0; i < numOfTask; i++) {
            Callable<Solucao> Task = new IniSingleSol();
            Future<Solucao> future = executor.submit(Task);

            // get the result
            addSingleSol(future.get());
            srcCost += future.get().srcCost;
            eliteSet.population[i].cleanSrcCost();
        }

//        executor.shutdown();

        return executor;
    }

    public List<Solucao> ProcessSubGroupParallel(ExecutorService executor) throws ExecutionException, InterruptedException {
        inicioLastBestSol=inicio;
        eliteSet.numIdv=0;

        int numOfTask = elitePopSize;
        List<Solucao> allOutSols = new ArrayList<>();

        for (int i = 0; i < numOfTask; i++) {
//            PopGroup inPG = elitePop[i];
            Callable<SolSetParams> Task = new SubGroupProcessSol(elitePop[i]);
            Future<SolSetParams> future = executor.submit(Task);

            // get the result
            SolSetParams outPG = future.get();
            srcCost += outPG.srcCost;

            for (int j = 0; j < outPG.numIdv; j++) {
                allOutSols.add(outPG.population[j]);
            }

        }

//        executor.shutdown();

        return allOutSols;
    }

//    // original
//    public void proceedOri(int ii, double[] optimals) throws ExecutionException, InterruptedException {
//        iterador=0;
//        inicio=System.currentTimeMillis();
//        inicioLastBestSol=inicio;
//
////        initilizePopulation();
//        ExecutorService executor = IniParallelProcess(config.getPopSize());
//
////        plot.getX(eliteSet, iterador);
//
//        while(!stoppingCriterion(ii, optimals))
//        {
//            iterador++;
//
//            if (true){
//                elitePopSize = 0;
//                for (int i = 0; i < eliteSet.numIdv; i++) {
//                    PopGroup newGroup = new PopGroup(config.getSubPopSize(), instancia, config);
//
//                    for(int j = 0; j < config.getSubPopSize(); j++){
//                        newGroup.population[j] = new Solucao(instancia, config);
//                        newGroup.population[j].f = Double.MAX_VALUE;
//                    }
//
//                    newGroup.population[newGroup.numIdv++] = eliteSet.population[i];
//                    elitePop[elitePopSize++] = newGroup;
//                }
//            }
//
//
//            List<Solucao> newSolucaos = ProcessSubGroupParallel(executor);
////            List<Solucao> newSolucaos = proceedSubGroup();
//
//            /*
//             * while set f to Double.MAX_VALUE in eliteSet, the f value in newSolucaos will be set to Double.MAX_VALUE,
//             * if the sol was not replaced in subProcess.
//             */
//
//            eliteSet.numIdv = 0;
//            for (int i = 0; i < newSolucaos.size(); i++) {
//                eliteSet.addElite(newSolucaos.get(i));
//            }
//
//            analisaSolucao(ii);
////            plot.getX(eliteSet, iterador);
//
//        }
//        executor.shutdown();
//        tempoFinal=(double)(System.currentTimeMillis()-inicio)/1000; //除1000得到的是秒
//    }
//
//    // keepMoreSub
//    public void proceedK(int ii, double[] optimals) throws ExecutionException, InterruptedException {
//        iterador=0;
//        inicio=System.currentTimeMillis();
//        inicioLastBestSol=inicio;
//
//        ExecutorService executor = IniParallelProcess(config.getPopSize());
//
//        plot.getX(eliteSet, iterador);
//
//        while(!stoppingCriterion(ii, optimals))
//        {
//            iterador++;
//
//            elitePopSize = 0;
//
//            // cluster
//            for (int i = 0; i < eliteSet.numIdv; i++) {
//                if (i == 0){
//                    PopGroup newGroup = new PopGroup(config.getSubPopSize(), instancia, config);
//
//                    for (int j = 0; j < config.getSubPopSize(); j++) {
//                        newGroup.population[j] = new Solucao(instancia, config);
//                        newGroup.population[j].f = Double.MAX_VALUE;
//                    }
//
//                    newGroup.population[newGroup.numIdv++] = eliteSet.population[i];
//                    elitePop[elitePopSize++] = newGroup;
//                }
//                else{
//                    for (int j = 0; j < elitePopSize; j++){
//                        boolean insert = false;
//                        for (int k = 0; k < elitePop[j].numIdv; k++){
//                            if (distEntreSolucoes.distanciaNew(eliteSet.population[i], elitePop[j].population[k]) <= config.getRefDist()){
//                                insert = true;
//                                break;
//                            }
//                        }
//
//                        if (insert){
//                            elitePop[j].addElite(eliteSet.population[i]);
//                            break;
//                        }
//                        else if (j == elitePopSize - 1){
//                            PopGroup newGroup = new PopGroup(config.getSubPopSize(), instancia, config);
//
//                            for (int k = 0; k < config.getSubPopSize(); k++) {
//                                newGroup.population[k] = new Solucao(instancia, config);
//                                newGroup.population[k].f = Double.MAX_VALUE;
//                            }
//
//                            newGroup.population[newGroup.numIdv++] = eliteSet.population[i];
//                            elitePop[elitePopSize++] = newGroup;
//                            break;
//                        }
//                    }
//                }
//
//            }
//
//            for (int i = 0; i < elitePopSize; i++){
//                plot.getX(elitePop[i], iterador);
//            }
//
//            List<Solucao> newSolucaos = ProcessSubGroupParallel(executor);
//
//            eliteSet.numIdv = 0;
//            for (int i = 0; i < newSolucaos.size(); i++) {
//                eliteSet.addElite(newSolucaos.get(i));
//            }
//
//            analisaSolucao(ii);
//            plot.getX(eliteSet, iterador);
//
//        }
//        executor.shutdown();
//        tempoFinal=(double)(System.currentTimeMillis()-inicio)/1000; //除1000得到的是秒
//    }

    // keepMoreSub & simplify
    public void proceed(int ii, double[] optimals, String problemType, double timeLimit, LeituraParametros leitor) throws ExecutionException, InterruptedException {
        iterador=0;
        inicio=System.currentTimeMillis();
        inicioLastBestSol=inicio;

        ExecutorService executor = IniParallelProcess(config.getPopSize());

        analisaSolucao(ii);

//        plot.getX(eliteSet, iterador);

        while(!stoppingCriterion(ii, optimals, problemType, timeLimit))
        {
            iterador++;

            elitePopSize = 0;

            // cluster
            for (int i = 0; i < eliteSet.numIdv; i++) {
                if (i == 0){
                    elitePop[elitePopSize].empty();
                    elitePop[elitePopSize].population[elitePop[elitePopSize].numIdv++] = eliteSet.population[i];
                    elitePopSize++;
                }
                else{
                    for (int j = 0; j < elitePopSize; j++){
                        boolean insert = false;
                        for (int k = 0; k < elitePop[j].numIdv; k++){
                            if (distEntreSolucoes.CalHammingDis(eliteSet.population[i], elitePop[j].population[k]) <= config.getRefDist()){
                                insert = true;
                                break;
                            }
                        }

                        if (insert){
                            elitePop[j].addElite(eliteSet.population[i]);
                            break;
                        }
                        else if (j == elitePopSize - 1){
                            elitePop[elitePopSize].empty();
                            elitePop[elitePopSize].population[elitePop[elitePopSize].numIdv++] = eliteSet.population[i];
                            elitePopSize++;
                            break;
                        }
                    }
                }

            }

//            for (int i = 0; i < elitePopSize; i++){
//                plot.getX(elitePop[i], iterador);
//            }

            // adjust parameters
            leitor.getConfig().setRefDist(ParamAdjust.adjustParamRefDist(instancia.getSize(), (double) elitePopSize/leitor.getConfig().getPopSize(), (double) (System.currentTimeMillis() - inicio) / instancia.getSize(), leitor.getConfig().getRefDist(), rand));

            List<Solucao> newSolucaos = ProcessSubGroupParallel(executor);

            eliteSet.numIdv = 0;
            for (int i = 0; i < newSolucaos.size(); i++) {
                eliteSet.addElite(newSolucaos.get(i));
//                eliteSet.addEliteTrail(newSolucaos.get(i));
            }

            analisaSolucao(ii);
//            plot.getX(eliteSet, iterador);

        }
        executor.shutdown();
        tempoFinal=(double)(System.currentTimeMillis()-inicio)/1000; //除1000得到的是秒
    }

//    //keep 50% of the best solutions
//    public void proceedKK(int ii, double[] optimals) throws ExecutionException, InterruptedException {
//        iterador=0;
//        inicio=System.currentTimeMillis();
//        inicioLastBestSol=inicio;
//
//        ExecutorService executor = IniParallelProcess(config.getPopSize());
//
//        analisaSolucao(ii);
//
////        plot.getX(eliteSet, iterador);
//
//        while(!stoppingCriterion(ii, optimals))
//        {
//            iterador++;
//
//            elitePopSize = 0;
//
//            // cluster
//            for (int i = 0; i < eliteSet.numIdv; i++) {
//                if (i == 0){
//                    elitePop[elitePopSize].empty();
//                    elitePop[elitePopSize].population[elitePop[elitePopSize].numIdv++] = eliteSet.population[i];
//                    elitePopSize++;
//                }
//                else{
//                    for (int j = 0; j < elitePopSize; j++){
//                        boolean insert = false;
//                        for (int k = 0; k < elitePop[j].numIdv; k++){
//                            if (distEntreSolucoes.distanciaNew(eliteSet.population[i], elitePop[j].population[k]) <= config.getRefDist()){
//                                insert = true;
//                                break;
//                            }
//                        }
//
//                        if (insert){
//                            elitePop[j].addElite(eliteSet.population[i]);
//                            break;
//                        }
//                        else if (j == elitePopSize - 1){
//                            elitePop[elitePopSize].empty();
//                            elitePop[elitePopSize].population[elitePop[elitePopSize].numIdv++] = eliteSet.population[i];
//                            elitePopSize++;
//                            break;
//                        }
//                    }
//                }
//
//            }
//
//            for (int i = 0; i < elitePopSize; i++){
//                plot.getX(elitePop[i], iterador);
//            }
//
//            List<Solucao> newSolucaos = ProcessSubGroupParallel(executor);
//
//            eliteSet.population = newSolucaos.toArray(new Solucao[0]);
//            Arrays.sort(eliteSet.population, 0, newSolucaos.size(), new PopGroup.SolucaoCostComparator());
//
//            eliteSet.population = Arrays.copyOfRange(eliteSet.population, 0, eliteSet.population.length / 2);
//            eliteSet.numIdv = eliteSet.population.length;
//            elitePop = new PopGroup[eliteSet.numIdv];
//            for (int e = 0; e < eliteSet.numIdv; e++)
//                elitePop[e] = new PopGroup(config.getSubPopSize(), instancia, config);
//
//            analisaSolucao(ii);
////            plot.getX(eliteSet, iterador);
//
//        }
//        executor.shutdown();
//        tempoFinal=(double)(System.currentTimeMillis()-inicio)/1000; //除1000得到的是秒
//    }
//
//    // test version: main group accepts all solutions from S
//    public void proceedM(int ii, double[] optimals) throws ExecutionException, InterruptedException {
//        iterador=0;
//        inicio=System.currentTimeMillis();
//        inicioLastBestSol=inicio;
//
//        ExecutorService executor = IniParallelProcess(config.getPopSize());
//
//        plot.getX(eliteSet, iterador);
//
//        while(!stoppingCriterion(ii, optimals))
//        {
//            iterador++;
//
//            elitePopSize = 0;
//            for (int i = 0; i < eliteSet.numIdv; i++) {
//                if (i == 0){
//                    PopGroup newGroup = new PopGroup(config.getSubPopSize(), instancia, config);
//
//                    for (int j = 0; j < config.getSubPopSize(); j++) {
//                        newGroup.population[j] = new Solucao(instancia, config);
//                        newGroup.population[j].f = Double.MAX_VALUE;
//                    }
//
//                    newGroup.population[newGroup.numIdv++] = eliteSet.population[i];
//                    elitePop[elitePopSize++] = newGroup;
//                }
//                else{
//                    for (int j = 0; j < elitePopSize; j++){
//                        boolean insert = false;
//                        for (int k = 0; k < elitePop[j].numIdv; k++){
//                            if (distEntreSolucoes.distanciaNew(eliteSet.population[i], elitePop[j].population[k]) <= config.getRefDist()){
//                                insert = true;
//                                break;
//                            }
//                        }
//
//                        if (insert){
//                            elitePop[j].addElite(eliteSet.population[i]);
//                            break;
//                        }
//                        else if (j == elitePopSize - 1){
//                            PopGroup newGroup = new PopGroup(config.getSubPopSize(), instancia, config);
//
//                            for (int k = 0; k < config.getSubPopSize(); k++) {
//                                newGroup.population[k] = new Solucao(instancia, config);
//                                newGroup.population[k].f = Double.MAX_VALUE;
//                            }
//
//                            newGroup.population[newGroup.numIdv++] = eliteSet.population[i];
//                            if (elitePopSize < config.getPopSize()) {
//                                elitePop[elitePopSize++] = newGroup;
//                            }
//                            break;
//                        }
//                    }
//                }
//
//            }
//
//            for (int i = 0; i < elitePopSize; i++){
//                plot.getX(elitePop[i], iterador);
//            }
//
//            List<Solucao> newSolucaos = ProcessSubGroupParallel(executor);
//
//            eliteSet.numIdv = newSolucaos.size();
//            eliteSet.population = newSolucaos.toArray(new Solucao[0]);
//
//            analisaSolucao(ii);
//            plot.getX(eliteSet, iterador);
//
//        }
//        executor.shutdown();
//        tempoFinal=(double)(System.currentTimeMillis()-inicio)/1000; //除1000得到的是秒
//    }
//
//    // cluster
//    public void proceedC(int ii, double[] optimals) throws ExecutionException, InterruptedException {
//        iterador=0;
//        inicio=System.currentTimeMillis();
//        inicioLastBestSol=inicio;
//
////        initilizePopulation();
//        ExecutorService executor = IniParallelProcess(config.getPopSize());  // 250
//
//        while(!stoppingCriterion(ii, optimals))
//        {
//            iterador++;
//
//            // cluster
//            elitePopSize = 0;
//            Boolean[] assignedSols = new Boolean[eliteSet.numIdv];
//            Arrays.fill(assignedSols, Boolean.FALSE);
//
//            Arrays.sort(eliteSet.population, 0, eliteSet.numIdv, new PopGroup.SolucaoCostComparator());  // assign groups starts with the best solucao
//            for (int i = 0; i < eliteSet.numIdv; i++) {
//                if (!assignedSols[i]){
//                    assignedSols[i] = true;
//                    PopGroup newGroup = new PopGroup(config.getSubPopSize(), instancia, config);
//                    newGroup.population[newGroup.numIdv++] = eliteSet.population[i];
//                    newGroup.melhorF = eliteSet.population[i].f;
//
//                    for (int j = i + 1; j < eliteSet.numIdv; j++) {
//                        if (assignedSols[j]){
//                            continue;
//                        }
//                        if (distEntreSolucoes.distanciaNew(eliteSet.population[i], eliteSet.population[j]) <= config.getRefDist()){
//                            newGroup.addElite(eliteSet.population[j]);
//                            assignedSols[j] = true;
//                        }
//                    }
//
//                    if (elitePopSize < config.getPopSize()) {
//                        elitePop[elitePopSize++] = newGroup;
//                    }
//                }
//            }
//            for (int i = 0; i < elitePopSize; i++){
//                plot.getX(elitePop[i], iterador);
//            }
//
//            List<Solucao> newSolucaos = ProcessSubGroupParallel(executor);
//
//            eliteSet.numIdv = newSolucaos.size();
//            eliteSet.population = newSolucaos.toArray(new Solucao[0]);
//
//            analisaSolucao(ii);
//
//
//        }
//        executor.shutdown();
//        tempoFinal=(double)(System.currentTimeMillis()-inicio)/1000; //除1000得到的是秒
//    }
//
//    // cluster_weights
//    public void proceedClusterWeights(int ii, double[] optimals) throws ExecutionException, InterruptedException {
//        iterador=0;
//        inicio=System.currentTimeMillis();
//        inicioLastBestSol=inicio;
//
//        ExecutorService executor = IniParallelProcess(config.getPopSize());
////        plot.getX(eliteSet, iterador);
//
//        while(!stoppingCriterion(ii, optimals))
//        {
//            iterador++;
//
//            // cluster
//            elitePopSize = 0;
//            Boolean[] assignedSols = new Boolean[eliteSet.numIdv];
//            Arrays.fill(assignedSols, Boolean.FALSE);
//
//            // assign groups starts with the roulette wheel
//            int numAssigned = 0;
//            while (elitePopSize < config.getSubPopSize() && numAssigned < eliteSet.numIdv){
//                int currentPopIndex = 0;
//                double sumF = 0;
//                for (int i = 0; i < eliteSet.numIdv; i++) {
//                    if (!assignedSols[i]){
//                        sumF += eliteSet.population[i].f;
//                    }
//                }
//
//                double randNum = rand.nextDouble();
//                double accumulativeP = 0;
//                for (int k = 0; k < eliteSet.numIdv; k++) {
//                    if (!assignedSols[k]){
//                        accumulativeP += eliteSet.population[k].f / sumF;
//                        if (randNum <= accumulativeP) {
//                            currentPopIndex = k;
//                            assignedSols[k] = true;
//                            numAssigned++;
//                            break;
//                        }
//                    }
//                }
//
//                PopGroup newGroup = new PopGroup(config.getSubPopSize(), instancia, config);
//                newGroup.population[newGroup.numIdv++] = eliteSet.population[currentPopIndex];
//                newGroup.melhorF = eliteSet.population[currentPopIndex].f;
//
//                for (int j = 0; j < eliteSet.numIdv; j++) {
//                    if (assignedSols[j]) {
//                        continue;
//                    }
//                    if (distEntreSolucoes.distanciaNew(eliteSet.population[currentPopIndex], eliteSet.population[j]) <= config.getRefDist()) {
//                        newGroup.addElite(eliteSet.population[j]);
//                        assignedSols[j] = true;
//                        numAssigned++;
//                    }
//                }
//
//                elitePop[elitePopSize++] = newGroup;
//            }
//
//
//            // proceed subgroup
//            List<Solucao> newSolucaos = ProcessSubGroupParallel(executor);
//
//            eliteSet.numIdv = newSolucaos.size();
//            eliteSet.population = newSolucaos.toArray(new Solucao[0]);
//
//            analisaSolucao(ii);
////            plot.getX(eliteSet, iterador);
//
//        }
//        executor.shutdown();
//        tempoFinal=(double)(System.currentTimeMillis()-inicio)/1000; //除1000得到的是秒
//    }

    public void analisaSolucao(int ii)
    {

        inicioLastBestSol = System.currentTimeMillis();

        melhorSolucao.clone(eliteSet.findBestSolucao());
        if (melhorSolucao.f - melhorF < -epsilon) {
            melhorF=melhorSolucao.f;
            iteradorMF = iterador;
            tempoMF = (double) (System.currentTimeMillis() - inicio) / 1000;

            if(print){
                System.out.println("melhorF: "+melhorF+" K: "+melhorSolucao.NumRotas
                        +" tempoMF: "+tempoMF
                        +" iteradorMF: "+iteradorMF
                        +" srcCost: "+srcCost
//                        +" gap: "+ 100 * (melhorF-cvrpOpt[ii])/optimals[ii] + "%"
//                        +" gap: "+ 100 * (melhorF-optimals[ii])/(optimals[ii] + 0.0001) + "%"
                        +" localNum: " + elitePopSize
                );
                bestSrcCost = srcCost;
                timeUntilBest = tempoMF;

                // using for plot
                timeEach.add(tempoMF);
                srcEachIter.add(srcCost);
                fEach.add(melhorF);
            }
        }
    }

    private boolean stoppingCriterion(int ii, double[] optimals, String problemType, double timeLimit)
    {
        switch(stoppingCriterion)
        {
            case Time: 	if(melhorF<=otimo||MAX<=(System.currentTimeMillis()-inicio)/1000)
                    return true;
                break;

//            case Iteration: if(melhorF - optimals[ii] <= 0.0001||srcCost >= 1000000000)
//            case Iteration: if(melhorF - cvrpOpt[ii] <= 0.0001||srcCost >= 1000000000)

            case Iteration: if (Objects.equals(problemType, "Classic")){
                if(melhorF - optimals[ii] <= 0.01||(System.currentTimeMillis()-inicio)/1000 >= timeLimit)
                    return true;
            } else {
                if((System.currentTimeMillis()-inicio)/1000 >= timeLimit)
                    return true;
            }
                break;

            case IterationWithoutImprovement: 	if (Objects.equals(problemType, "Classic")){
                if(melhorF - optimals[ii] <= 0.01||MAX<=(iterador-iteradorMF))
                    return true;
            } else {
                if(MAX<=(iterador-iteradorMF))
                    return true;
            }
                break;
        }
        return false;
    }

    // ii denotes the index of the instance, jj denotes the index of the run
//    public void storeValue(int ii, int jj, String Path) throws IOException {
//        File file1 = new File(Path + ii + "_src.txt");
//
//        if (!file1.getParentFile().exists()) {
//            file1.getParentFile().mkdirs(); // 创建文件夹
//        }
//
//        if (!file1.exists()) {
//            try {
//                file1.createNewFile();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file1, true));
//        bufferedWriter.write("" + jj);
//        bufferedWriter.newLine();
//        for(int k = 0; k < timeEach.size(); k++){
//            bufferedWriter.write(" " + timeEach.get(k));
//        }
//        bufferedWriter.newLine();
//        for (int k = 0; k < srcEachIter.size(); k++) {
//            bufferedWriter.write(" " + srcEachIter.get(k));
//        }
//        bufferedWriter.newLine();
//        for (int k = 0; k < srcEachIter.size(); k++) {
//            bufferedWriter.write(" " + fEach.get(k));
//        }
//        bufferedWriter.close();
//    }
//
//    public static void creatResultFile(String name){
//
//        File file = new File(name);
//        if (!file.getParentFile().exists()) {
//            file.getParentFile().mkdirs(); // 创建文件夹
//        }
//
//        // judge file exist continue
//        if (Files.exists(Paths.get(name))) {
//            return;
//        }
//        Workbook workbook = new XSSFWorkbook();
//        Sheet sheet = workbook.createSheet("Results");
//        Row header1 = sheet.createRow(0);
//        Row header2 = sheet.createRow(1);
//        header1.createCell(0).setCellValue("Instance");
//
//        header1.createCell(1).setCellValue("LLM-AILS");
//        header2.createCell(1).setCellValue("Best_Value");
//        header2.createCell(2).setCellValue("Avg_Value");
//        header2.createCell(3).setCellValue("Avg_Time");
//        header2.createCell(4).setCellValue("Avg_Eva");
//        header2.createCell(5).setCellValue("Variance");
//
//        header1.createCell(6).setCellValue("AILS");
//        header2.createCell(6).setCellValue("Best_Value");
//        header2.createCell(7).setCellValue("Avg_Value");
//        header2.createCell(8).setCellValue("Avg_Time");
//        header2.createCell(9).setCellValue("Avg_Eva");
//        header2.createCell(10).setCellValue("Variance");
//
//        try (FileOutputStream fileOut = new FileOutputStream(name)) {
//            workbook.write(fileOut);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        try {
//            workbook.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static void appendResultFile(String type, int index, String fileName, String instance, double[] all_time, double[] all_value, double a_eva, double vari, Solucao bestS, int repeatTimes){
//
//        // calculate values
//        double a_time = 0;
//        double a_value = 0;
//        double b_value;
//
//        for (int i = 0; i < repeatTimes; i++) {
//            a_time += all_time[i];
//            a_value += all_value[i];
//        }
//        a_time /= repeatTimes;
//        a_value /= repeatTimes;
//        b_value = a_value;
//        for (int y = 0; y < repeatTimes; y++) {
//            if (b_value > all_value[y]) {
//                b_value = all_value[y];
//            }
//        }
//
//        // write to excel
//        try {
//            FileInputStream fis = new FileInputStream(fileName);
//            Workbook workbook = new XSSFWorkbook(fis);
//            Sheet sheet = workbook.getSheetAt(0);
//
//            // 找到当前行，如果行不存在则创建新行
//            Row row = sheet.getRow(index + 2);
//            if (row == null) {
//                row = sheet.createRow(index + 2);
//            }
//
//            row.createCell(0).setCellValue(instance);
//
//            if (Objects.equals(type, "LLM-AILS")) {
//                row.createCell(1).setCellValue(b_value);
//                row.createCell(2).setCellValue(a_value);
//                row.createCell(3).setCellValue(a_time);
//                row.createCell(4).setCellValue(a_eva);
//                row.createCell(5).setCellValue(vari);
//
//            } else if (Objects.equals(type, "AILS")) {
//                row.createCell(6).setCellValue(b_value);
//                row.createCell(7).setCellValue(a_value);
//                row.createCell(8).setCellValue(a_time);
//                row.createCell(9).setCellValue(a_eva);
//                row.createCell(10).setCellValue(vari);
//
//            }
//
//            for (int i = 0; i < 13; i++){
//                sheet.autoSizeColumn(i);
//            }
//
//            // record results of every trail
//            for (int colIdx = 0; colIdx < repeatTimes; colIdx++) {
//                Cell cell = row.createCell(15 + colIdx);
//                cell.setCellValue(all_value[colIdx]);
//            }
//
//            for (int colIdx = 0; colIdx < repeatTimes; colIdx++) {
//                Cell cell = row.createCell(25 + colIdx + repeatTimes + 1);
//                cell.setCellValue(all_time[colIdx]);
//            }
//
//            for (int colIdx = 0; colIdx < 1; colIdx++) {
//                Cell cell = row.createCell(25 + colIdx + repeatTimes * 2 + 2);
//                cell.setCellValue(bestS.toString());
//            }
//
//            fis.close();
//
//            FileOutputStream fos = new FileOutputStream(fileName);
//            workbook.write(fos);
//            fos.close();
//
//            workbook.close();
//
//            System.out.println("Result appended!");
//
//        }catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        // print results
//        System.out.println("No. " + index + " Instance:");
//        System.out.println("Best value：" + b_value);
//        System.out.println("Avg value：" + a_value);
//        System.out.println("Avg time： " + a_time);
//        System.out.println("Avg evals： " + vari);
//        System.out.println(" ");
//
//    }
//
//    // baseline test
//    public static void main1(String[] args) throws IOException, ExecutionException, InterruptedException {
//
//        String[] problems = new String[]{"HVRPFD", "HVRPD", "FSMF", "FSMD", "FSMFD"};
//
//        for (int x = 0; x < 5; x++){
//            String problem = problems[x];
//            switch (problem) {
//                case "HVRPD" -> optimals = optimals_HVRPD;
//                case "HVRPFD" -> optimals = optimals_HVRPFD;
//                case "FSMD" -> optimals = optimals_FSMD;
//                case "FSMF" -> optimals = optimals_FSMF;
//                default -> optimals = optimals_FSMFD;
//            }
//
//            // settings
//            int size = 18;
//            int repeat = 30;
//            double timeLimit = 60;
//            String methodType = "LLM-AILS";
//            String problemType = "Classic";  // Classic, bigTSP, largeIns
//            String resultPath = "D:/XZL/AILS-HVRP-population/src/";
//            String resultFolder = "resultLLM_v0";
//            String resultFileName = timeLimit + "s_" + methodType + "_" + problemType;
//            String resultFilePath = resultPath + resultFolder + "/" + problem + "/" + problem + "_" + resultFileName + ".xlsx";
//
//            double[][] t = new double[size][repeat]; // all time result
//            double[][] vI = new double[size][repeat]; // all values
//            long[] srcCosts = new long[size];
//            long[] srcBestCosts = new long[size];
//            double[] timeBest = new double[size];
//
//            LeituraParametros leitor = new LeituraParametros();
//            leitor.lerParametros(args);
//
//            // adjust best & problem
//            leitor.setVariant(problem);
//            leitor.getConfig().setRefDist(0.1);  // 0.1,0.05
//            leitor.getConfig().setGeneration(100);  // 100
//            leitor.getConfig().setPopSize(20);  // 20
//            leitor.getConfig().setSubPopSize(5);  // 5,7
//            leitor.getConfig().setConevergeTimes(2);  // 2
//
//            // run instance
//            creatResultFile(resultFilePath);
//
//            for (int ii = 0; ii < size; ii++) {
//                String runFile1, runFile2, runFile3;
//                if (ii < 5) {
//                    runFile1 = resultPath + "Instances/Li_H" + (ii + 1) + ".txt";
//                    runFile2 = resultPath + "Instances/bigTSP/bigTSP100_" + (ii + 1) + ".txt";
//                    runFile3 = resultPath + "Instances/large_ins/large1000_" + (ii + 1) + ".txt";
//
//                } else if (ii < 10) {
//                    runFile1 = resultPath + "Instances/N" + (ii - 4) + ".txt";
//                    runFile2 = resultPath + "Instances/bigTSP/bigTSP300_" + (ii - 4) + ".txt";
//                    runFile3 = resultPath + "Instances/large_ins/large1000_" + (ii - 4) + ".txt";
//
//                } else {
//                    runFile1 = resultPath + "Instances/Taillard_" + (ii + 3) + ".txt";
//                    runFile2 = resultPath + "Instances/bigTSP/bigTSP500_" + (ii - 9) + ".txt";
//                    runFile3 = resultPath + "Instances/large_ins/large1000_" + (ii - 9) + ".txt";
//
//                }
//
//                if (Objects.equals(problemType, "Classic")) {
//                    leitor.setFile(runFile1);
//                } else if (Objects.equals(problemType, "bigTSP")) {
//                    leitor.setFile(runFile2);
//                } else if (Objects.equals(problemType, "largeIns")) {
//                    leitor.setFile(runFile3);
//                }
//
//                String insName = new File(leitor.getFile()).getName();
//                Instancia instancia = new Instancia(leitor.getFile(), leitor.getConfig(), leitor.isRounded(), leitor.getVariant());
//                Solucao b = new Solucao(instancia, leitor.getConfig());
//
//                // run instances repeatedly
//                for (int jj = 0; jj < repeat; jj++) {
//                    System.out.println("Run No." + ii + " " + insName + " instance, " + "On repeat No." + jj + " time：");
//                    Population population = new Population(instancia, leitor.getConfig(), leitor.getBest(), leitor.getTimeLimit());
//                    population.problemType = problemType;
//                    population.proceed(ii, optimals, population.problemType, timeLimit, leitor);
//
//                    double totalTime = System.currentTimeMillis() - population.inicio;
//                    System.out.println("Final result: "
//                            + "melhorF: "+population.melhorF
//                            +" K: "+population.melhorSolucao.NumRotas
//                            +" finalTime: "+ totalTime / 1000
//                            +" finalIteration: "+ population.iterador
//                            +" finalEvaTimes: " + population.srcCost
//                            +" bestEvaTimes: " + population.bestSrcCost);
//
////                    population.plot.plot2DPopulation(resultPath + resultFolder + "/" + problem + "/" + problemType + "/plotDataRecord/", "Population Distribution", "file " + ii, jj);
//                    population.storeValue(ii, jj, resultPath + resultFolder + "/" + problem + "/" + problemType + "/convergeDataRecord/");
//
//                    // record time and length
//                    t[ii][jj] = totalTime / 1000;
//                    vI[ii][jj] = population.melhorF;
//                    srcCosts[ii] += population.srcCost;
//                    srcBestCosts[ii] += population.bestSrcCost;
//                    timeBest[ii] += population.timeUntilBest;
//
//                    if (population.melhorF < b.f)
//                        b.clone(population.melhorSolucao);
//
//                }
//
//                appendResultFile(methodType, ii, resultFilePath, insName, t[ii], vI[ii],
//                        (double) srcBestCosts[ii] / repeat, StatUtils.variance(vI[ii]), b, repeat);
//
//            }
//        }
//
//        System.out.println("Finish all.");
//
//    }

    // llm
    public static void main(String[] args) throws ExecutionException, InterruptedException {

        LeituraParametros leitor=new LeituraParametros();
        leitor.lerParametros(args);

        int insNum = 0;
        String problemType = "Classic";
        String problem = "HVRPFD";
        double timeLimit = 60;

        for (int i = 0; i < args.length-1; i+=2)
        {
            switch(args[i])
            {
                case "-insNo": insNum = Integer.parseInt(args[i+1]); break;
                case "-problemType": problemType = args[i+1]; break;
                case "-timeLimit": timeLimit = Double.parseDouble(args[i+1]); break;
                case "-variant": problem = args[i+1]; break;
                case "-rand": randNum = Integer.parseInt(args[i+1]); break;
            }
        }
        leitor.getConfig().setRefDist(0.1);  // 0.1,0.05
        leitor.getConfig().setGeneration(100);  // 100
        leitor.getConfig().setPopSize(20);  // 20
        leitor.getConfig().setSubPopSize(5);  // 5,7
        leitor.getConfig().setConevergeTimes(2);  // 2
        leitor.setVariant(problem);

        switch (problem) {
            case "HVRPD" -> {
                optimals = optimals_HVRPD;
                variantType = 3;
            }
            case "HVRPFD" -> {
                optimals = optimals_HVRPFD;
                variantType = 4;
            }
            case "FSMD" -> {
                optimals = optimals_FSMD;
                variantType = 0;
            }
            case "FSMF" -> {
                optimals = optimals_FSMF;
                variantType = 1;
            }
            default -> {
                optimals = optimals_FSMFD;
                variantType = 2;
            }
        }

        Instancia instancia=new Instancia(leitor.getFile(),leitor.getConfig(),leitor.isRounded(),leitor.getVariant());
        timeLimit = instancia.getSize();
//        timeLimit = 0.5;
        Population population = new Population(instancia, leitor.getConfig(), leitor.getBest(), leitor.getTimeLimit(), randNum);
        population.problemType = problemType;
        population.proceed(insNum, optimals, population.problemType, timeLimit, leitor);

        System.out.println("Solution quality: ");
        System.out.println(population.melhorF);

    }

}
