package Metaheuristicas;


import CriterioAceitacao.CA;
import CriterioAceitacao.CALimiar;
import Dados.Instancia;

import org.apache.poi.ss.usermodel.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Population {

    Instancia instancia;
    SolSetParams eliteSet;
    SolSetParams[] elitePop;
    int elitePopSize = 0;
    Solucao melhorSolucao;

    // operations
    CA criterioAceitacao;
    Distancia distEntreSolucoes;

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
    Random rand=new Random();
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

    static double[] optimals;
    public String problemType;
    static double[] optimals_HVRPFD = {13085.077097956533, 0, 0, 0, 0, 2233.90,
            2851.94, 2378.99082556146, 1839.21673823207, 2047.80590624202, 3185.08870574379, 10107.5290365555, 3065.29388423529, 3265.41226692788,
            2076.95695094159, 3743.58009841382, 10420.3438245153, 4760.68};

    static double[] optimals_HVRPD = {12050.08, 10130.30, 16192.26, 17273.75, 23024.58, 2233.90,
            2851.94, 2378.99082556146, 1839.21673823207, 2047.80590624202, 1517.84, 607.53, 1015.29, 1144.94, 1061.96, 1823.58, 1117.51, 1534.17};

    static double[] optimals_FSMFD = {11747.38747, 0, 15746.47736, 0, 0, 2211.63, 2811.37, 2234.57,
            1822.78, 2016.79, 2964.65, 9126.90, 2634.96, 3168.92, 2004.48, 3147.99, 8661.81, 4153.02};

    static double[] optimals_FSMF = {0, 0, 0, 0, 0, 0, 0, 1042.115027, 762.0455829, 1078.101175, 2406.36, 9119.03,
            2586.37, 2720.43, 1734.53, 2369.65, 8661.81, 4029.61};

    static double[] optimals_FSMD = {0, 0, 0, 0, 0, 2211.63, 2811.37, 2234.57,
            1822.78, 2016.79, 1491.86, 603.21, 999.82, 1131.00, 1038.60, 1800.80, 1105.44, 1530.43};

    static double[] optimals_bigTSP_HVRPFD = {30.11559911, 29.56669768, 29.9031677, 30.05428767, 29.74024918, 38.94142622,
            38.00031211, 39.12046912, 39.43980791, 38.22188082, 45.50334913, 45.55047084, 45.33010267, 45.44399839, 45.01842833};

    static double[] optimals_bigTSP_HVRPD = {20.11559911, 19.56669768, 19.9031677, 20.05428767, 19.74024918, 28.94354893,
            28.00114333, 29.12046912, 29.44003082, 28.22188082, 35.45433834, 35.65678594, 35.33373933, 35.45443571, 35.02854793};

    static double[] optimals_bigTSP_FSMFD = {28.47637965, 28.18554546, 28.34926202, 29.29266271, 28.93553614, 36.17161024,
            35.34361931, 36.52440786, 36.46956069, 35.57932834, 42.87079539, 42.90973815, 42.75787102, 42.73250968, 42.46320559};

    static double[] optimals_bigTSP_FSMF = {28.47637965, 28.18554546, 28.34926202, 29.25187423, 28.89897307, 36.16496388,
            35.34341094, 36.51797312, 36.47697471, 35.58024569, 42.84919132, 42.94384785, 42.72534581, 42.73729298, 42.4674225};

    static double[] optimals_bigTSP_FSMD = {19.0703563, 18.44268437, 18.75297677, 19.09754107, 18.91512685, 28.88371538,
            27.97247512, 29.12122477, 29.43928473, 28.17935832, 35.4571323, 35.5688901, 35.3477221, 35.41939752, 35.05783585};


    static double[] cvrpOpt = {27591, 26362, 14971, 12747, 13332, 55539, 28940, 10916, 13590, 15700, 43448, 21220, 16876, 14138, 20557, 45607, 47812, 25569, 24145, 16980,
            44225, 58578, 19565, 30656, 10856, 117595, 40437, 25742, 19230, 27042, 82751, 37274, 38684, 18839, 26558, 75478, 35291, 21245, 33503, 20215, 95151,
            47161, 34231, 21736, 25859, 94043, 78355, 29834, 27532, 31102, 139111, 42050, 25896, 51505, 22814, 147713, 65928,
            38260, 66154, 19712, 107798, 65449, 36391, 55233, 24139, 221824, 89449, 66483, 69226, 24201, 154593, 94846, 86700,
            42717, 50673, 190316, 108451, 59535, 62164, 63682, 106780, 146332, 68205, 81923, 43373, 136187, 77269, 114417, 72386,
            73305, 158121, 193737, 88965, 99299, 53860, 329179, 132715, 85465, 118976, 72355};

    static int variantType = 0;
    LeituraParametros leitor;

    List<Long> srcEachIter = new ArrayList<Long>();
    List<Double> timeEach = new ArrayList<Double>();
    List<Double> fEach = new ArrayList<Double>();
    List<Integer> popSize = new ArrayList<>();

    public Population(Instancia instancia, Config config, double d, double MAX){
        this.instancia=instancia;

        this.epsilon=config.getEpsilon();

        this.elitePop = new SolSetParams[config.getPopSize()];
        this.eliteSet = new SolSetParams(config.getPopSize(), instancia, config);
        for (int i = 0; i < config.getPopSize(); i++) {
            elitePop[i] = new SolSetParams(config.getSubPopSize(), instancia, config);
        }
        this.melhorSolucao =new Solucao(instancia,config);
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

        Solucao newSolucao = new Solucao(instancia,config);

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

            //choose a solucao among gp according to the f value using roulette wheel
            sumF = 0;

            Solucao chosenSolucao = new Solucao(instancia, config);

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
            leitor.getConfig().setRefDist(ParamAdjust.adjustParamRefDist(instancia.getSize(), (double) elitePopSize /leitor.getConfig().getPopSize(), (double) (System.currentTimeMillis() - inicio) /  instancia.getSize(), leitor.getConfig().getRefDist(), rand));

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
                        +" refDist: " + leitor.getConfig().getRefDist()
                );
                bestSrcCost = srcCost;
                timeUntilBest = tempoMF;

                // using for plot
                timeEach.add(tempoMF);
                srcEachIter.add(srcCost);
                fEach.add(melhorF);
                popSize.add(elitePopSize);
            }
        }
    }

    private boolean stoppingCriterion(int ii, double[] optimals, String problemType, double timeLimit)
    {
        switch(stoppingCriterion)
        {
            case Time:
                if (melhorF <= otimo || MAX <= (System.currentTimeMillis() - inicio) / 1000)
                    return true;
                break;

//            case Iteration: if(melhorF - optimals[ii] <= 0.0001||srcCost >= 1000000000)
//            case Iteration: if(melhorF - cvrpOpt[ii] <= 0.0001||srcCost >= 1000000000)

            case Iteration:
                if (Objects.equals(problemType, "Classic")) {
                    if (melhorF - optimals[ii] <= 0.01 || (System.currentTimeMillis() - inicio) / 1000 >= timeLimit)
                        return true;
                } else {
                    if ((System.currentTimeMillis() - inicio) / 1000 >= timeLimit)
                        return true;  //melhorF - optimals[ii] <= 0.0001 ||
                }
                break;

            case IterationWithoutImprovement:
                if (Objects.equals(problemType, "Classic")) {
                    if (melhorF - optimals[ii] <= 0.01 || MAX <= (iterador - iteradorMF))
                        return true;
                } else {
                    if (melhorF - optimals[ii] <= 0.0001 || MAX <= (iterador - iteradorMF))
                        return true;
                }
                break;
        }
        return false;
    }

    public static void creatResultFile(String name){

        File file = new File(name);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs(); // 创建文件夹
        }

        // judge file exist continue
        if (Files.exists(Paths.get(name))) {
            return;
        }
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Results");
        Row header1 = sheet.createRow(0);
        Row header2 = sheet.createRow(1);
        header1.createCell(0).setCellValue("Instance");

        header1.createCell(1).setCellValue("LLM-AILS");
        header2.createCell(1).setCellValue("Best_Value");
        header2.createCell(2).setCellValue("Avg_Value");
        header2.createCell(3).setCellValue("Avg_Time");
        header2.createCell(4).setCellValue("Avg_Eva");
        header2.createCell(5).setCellValue("Variance");

        header1.createCell(6).setCellValue("AILS");
        header2.createCell(6).setCellValue("Best_Value");
        header2.createCell(7).setCellValue("Avg_Value");
        header2.createCell(8).setCellValue("Avg_Time");
        header2.createCell(9).setCellValue("Avg_Eva");
        header2.createCell(10).setCellValue("Variance");

        try (FileOutputStream fileOut = new FileOutputStream(name)) {
            workbook.write(fileOut);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ii denotes the index of the instance, jj denotes the index of the run
    public void storeValue(int ii, int jj, String Path, Boolean needPopSize) throws IOException {
        File file1 = new File(Path + ii + "_src.txt");

        if (!file1.getParentFile().exists()) {
            file1.getParentFile().mkdirs(); // 创建文件夹
        }

        if (!file1.exists()) {
            try {
                file1.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file1, true));
//        bufferedWriter.newLine();
        bufferedWriter.write("" + jj);
        bufferedWriter.newLine();
        for (Double each : timeEach) {
            bufferedWriter.write(" " + each);
        }
        bufferedWriter.newLine();
        for (Long aLong : srcEachIter) {
            bufferedWriter.write(" " + aLong);
        }
        bufferedWriter.newLine();
        for (int k = 0; k < srcEachIter.size(); k++) {
            bufferedWriter.write(" " + fEach.get(k));
        }
        if (needPopSize){
            bufferedWriter.newLine();
            for (int k = 0; k < srcEachIter.size(); k++) {
                bufferedWriter.write(" " + popSize.get(k));
            }
        }

        bufferedWriter.close();
    }

    public static void appendResultFile(String type, int index, String fileName, String instance, double[] all_time, double[] all_value, double a_eva, double vari, Solucao bestS, int repeatTimes){

        // calculate values
        double a_time = 0;
        double a_value = 0;
        double b_value;

        for (int i = 0; i < repeatTimes; i++) {
            a_time += all_time[i];
            a_value += all_value[i];
        }
        a_time /= repeatTimes;
        a_value /= repeatTimes;
        b_value = a_value;
        for (int y = 0; y < repeatTimes; y++) {
            if (b_value > all_value[y]) {
                b_value = all_value[y];
            }
        }

        // write to excel
        try {
            FileInputStream fis = new FileInputStream(fileName);
            Workbook workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheetAt(0);

            // 找到当前行，如果行不存在则创建新行
            Row row = sheet.getRow(index + 2);
            if (row == null) {
                row = sheet.createRow(index + 2);
            }

            row.createCell(0).setCellValue(instance);

            if (Objects.equals(type, "LLM-AILS")) {
                row.createCell(1).setCellValue(b_value);
                row.createCell(2).setCellValue(a_value);
                row.createCell(3).setCellValue(a_time);
                row.createCell(4).setCellValue(a_eva);
                row.createCell(5).setCellValue(vari);

            } else if (Objects.equals(type, "AILS")) {
                row.createCell(6).setCellValue(b_value);
                row.createCell(7).setCellValue(a_value);
                row.createCell(8).setCellValue(a_time);
                row.createCell(9).setCellValue(a_eva);
                row.createCell(10).setCellValue(vari);

            }

            for (int i = 0; i < 13; i++){
                sheet.autoSizeColumn(i);
            }

            // record results of every trail
            for (int colIdx = 0; colIdx < repeatTimes; colIdx++) {
                Cell cell = row.createCell(15 + colIdx);
                cell.setCellValue(all_value[colIdx]);
            }

            for (int colIdx = 0; colIdx < repeatTimes; colIdx++) {
                Cell cell = row.createCell(25 + colIdx + repeatTimes + 1);
                cell.setCellValue(all_time[colIdx]);
            }

            for (int colIdx = 0; colIdx < 1; colIdx++) {
                Cell cell = row.createCell(25 + colIdx + repeatTimes * 2 + 2);
                cell.setCellValue(bestS.toString());
            }

            fis.close();

            FileOutputStream fos = new FileOutputStream(fileName);
            workbook.write(fos);
            fos.close();

            workbook.close();

            System.out.println("Result appended!");

        }catch (IOException e) {
            e.printStackTrace();
        }

        // print results
        System.out.println("No. " + index + " Instance:");
        System.out.println("Best value：" + b_value);
        System.out.println("Avg value：" + a_value);
        System.out.println("Avg time： " + a_time);
        System.out.println("Avg evals： " + vari);
        System.out.println(" ");

    }


    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {

        String[] problems = new String[]{"HVRPFD", "HVRPD", "FSMF", "FSMD", "FSMFD"};

        for (int x = 0; x < 5; x++){
            String problem = problems[x];

            String chooseOptimalType = "Classic";  // Classic, bigTSP, largeIns
            if (chooseOptimalType.equals("Classic")) {
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

            } else {
                switch (problem) {
                    case "HVRPD" -> {
                        optimals = optimals_bigTSP_HVRPD;
                        variantType = 3;
                    }
                    case "HVRPFD" -> {
                        optimals = optimals_bigTSP_HVRPFD;
                        variantType = 4;
                    }
                    case "FSMD" -> {
                        optimals = optimals_bigTSP_FSMD;
                        variantType = 0;
                    }
                    case "FSMF" -> {
                        optimals = optimals_bigTSP_FSMF;
                        variantType = 1;
                    }
                    default -> {
                        optimals = optimals_bigTSP_FSMFD;
                        variantType = 2;
                    }
                }
            }

            // settings
            int size = 15;
            int repeat = 5;
            double timeLimit = 60;
            String methodType = "LLM-AILS";
            String problemType = chooseOptimalType;  // Classic, bigTSP, largeIns
            String resultPath = "src/";
            String resultFolder = "resultLLM_v0";
            String convergeFolder = "convergeDataRecord_scale_LLM_adjustParam_v2";
            String resultFileName = "scale_" + methodType + "_" + problemType;
            String resultFilePath = resultPath + resultFolder + "/" + problem + "/" + problem + "_" + resultFileName + "_LLM_adjustParam_v2.xlsx";

            double[][] t = new double[size][repeat]; // all time result
            double[][] vI = new double[size][repeat]; // all values
            long[] srcCosts = new long[size];
            long[] srcBestCosts = new long[size];
            double[] timeBest = new double[size];

            // adjust best & problem
            LeituraParametros leitor = new LeituraParametros();
            leitor.lerParametros(args);
            leitor.setVariant(problem);
            // adjust best & problem
            leitor.setVariant(problem);
            leitor.getConfig().setRefDist(0.1);  // 0.1,0.05
            leitor.getConfig().setGeneration(100);  // 100
            leitor.getConfig().setPopSize(20);  // 20, 50
            leitor.getConfig().setSubPopSize(5);  // 5,7
            leitor.getConfig().setConevergeTimes(2);  // 2

            // run instance
            creatResultFile(resultFilePath);

            for (int ii = 0; ii < 18; ii++) { // 18 for classic and 15 for largeins anf bigTSP
                String runFile1, runFile2, runFile3;
                if (ii < 5) {
                    runFile1 = resultPath + "Instances/Li_H" + (ii + 1) + ".txt";
                    runFile2 = resultPath + "Instances/bigTSP/bigTSP100_" + (ii + 1) + ".txt";
                    runFile3 = resultPath + "Instances/large_ins/large1000_" + (ii + 1) + ".txt";

                } else if (ii < 10) {
                    runFile1 = resultPath + "Instances/N" + (ii - 4) + ".txt";
                    runFile2 = resultPath + "Instances/bigTSP/bigTSP300_" + (ii - 4) + ".txt";
                    runFile3 = resultPath + "Instances/large_ins/large3000_" + (ii - 4) + ".txt";

                } else {
                    runFile1 = resultPath + "Instances/Taillard_" + (ii + 3) + ".txt";
                    runFile2 = resultPath + "Instances/bigTSP/bigTSP500_" + (ii - 9) + ".txt";
                    runFile3 = resultPath + "Instances/large_ins/large5000_" + (ii - 9) + ".txt";
                }

                if (Objects.equals(problemType, "Classic")) {
                    leitor.setFile(runFile1);
                } else if (Objects.equals(problemType, "bigTSP")) {
                    leitor.setFile(runFile2);
                } else if (Objects.equals(problemType, "largeIns")) {
                    leitor.setFile(runFile3);
                }

                String insName = new File(leitor.getFile()).getName();
                Instancia instancia = new Instancia(leitor.getFile(), leitor.getConfig(), leitor.isRounded(), leitor.getVariant());
                timeLimit = instancia.getSize();
                Solucao b = new Solucao(instancia, leitor.getConfig());

                // run instances repeatedly
                for (int jj = 0; jj < repeat; jj++) {
                    leitor.getConfig().setRefDist(0.1);
                    System.out.println("Run No." + ii + " " + insName + " instance, " + "On repeat No." + jj + " time：");
                    Population igas = new Population(instancia, leitor.getConfig(), leitor.getBest(), leitor.getTimeLimit());
                    igas.problemType = problemType;
                    igas.leitor = leitor;
                    igas.proceed(ii, optimals, problemType, timeLimit, leitor);

                    double totalTime = System.currentTimeMillis() - igas.inicio;
                    System.out.println("Final result: "
                            + "melhorF: "+igas.melhorF
                            +" K: "+igas.melhorSolucao.NumRotas
                            +" finalTime: "+ totalTime / 1000
                            +" finalIteration: "+ igas.iterador
                            +" finalEvaTimes: " + igas.srcCost
                            +" bestEvaTimes: " + igas.bestSrcCost);

//					igas.plot.plot2DPopulation(resultPath + resultFolder + "/" + problem + "/" + problemType + "/plotDataRecord/", "Population Distribution", "file " + ii, jj);
                    igas.storeValue(ii, jj, resultPath + resultFolder + "/" + problem + "/" + problemType + "/" + convergeFolder + "/", true);

                    // record time and length
                    t[ii][jj] = totalTime / 1000;
                    vI[ii][jj] = igas.melhorF;
                    srcCosts[ii] += igas.srcCost;
                    srcBestCosts[ii] += igas.bestSrcCost;
                    timeBest[ii] += igas.timeUntilBest;

                    if (igas.melhorF < b.f)
                        b.clone(igas.melhorSolucao);

                }

                appendResultFile(methodType, ii, resultFilePath, insName, t[ii], vI[ii],
                        (double) srcBestCosts[ii] / repeat, StatUtils.variance(vI[ii]), b, repeat);

            }
        }

        System.out.println("Finish all.");

    }

}
