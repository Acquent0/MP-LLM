package Metaheuristicas;


import AjusteOmega.AODist;
import AjusteOmega.AjusteOmega;
import BuscaLocal.BuscaLocal;
import BuscaLocalIntra.BuscaLocalIntra;
import CriterioAceitacao.CA;
import CriterioAceitacao.CALimiar;
import Dados.Instancia;
import Factibilizadores.Factibilizador;
import Pertubacao.HeuristicaAdicao;
import Pertubacao.Perturbacao;
import Pertubacao.PerturbacaoBuild;
//import org.apache.poi.hssf.model.WorkbookRecordList;
//import org.apache.poi.hssf.usermodel.HSSFWorkbook;
//import org.apache.poi.ss.usermodel.*;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Random;


public class AILS
{
	Solucao solucao,solucaoReferencia;
	Solucao melhorSolucao;

	Instancia instancia;
	Distancia distEntreSolucoes;
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
	
	DecimalFormat deci=new DecimalFormat("0.0000");
	Random rand=new Random(2025);
	

	HashMap<String,AjusteOmega>configuradoresOmega=new HashMap<String,AjusteOmega>();

	double distanciaBLEdge;
	
	PerturbacaoBuild perturbacaoBuild;
	Perturbacao[] perturbadores;
	Perturbacao perturbacaoEscolhida;
	
	Factibilizador factibilizador;
	
	BuscaLocal buscaLocal;
	int numRotasMin;
	int numRotasMax;

	//revise


	boolean aceitoCriterio,isMelhorSolucao;
	HeuristicaAdicao heuristicaAdicao;
	BuscaLocalIntra buscaLocalIntra;
	CA criterioAceitacao;
	
	boolean print=true;
	boolean factibilizou;

	double epsilon;
	private StoppingCriterion stoppingCriterion;
	public long srcCost = 0;
	public long bestSrcCost = 0;

	public AILS(Instancia instancia,Config config,double d,double MAX, int randNum)
	{
		this.instancia=instancia;
		srcCost = 0;

		this.buscaLocalIntra=new BuscaLocalIntra(instancia,config);
		this.epsilon=config.getEpsilon();
		
		this.solucao =new Solucao(instancia,config, randNum);
		this.solucaoReferencia =new Solucao(instancia,config, randNum);
		this.melhorSolucao =new Solucao(instancia,config, randNum);
		this.stoppingCriterion=config.getStoppingCriterion();
		this.numIterUpdate=config.getGamma();
		this.config=config;
		this.otimo=d;
		this.MAX=MAX;
		this.melhorF=Integer.MAX_VALUE;
		
		this.mFBL=new Media(numIterUpdate);

		this.perturbacaoBuild=new PerturbacaoBuild(); 
		this.numRotasMin=instancia.getNumRotasMin();
		this.numRotasMax=instancia.getNumRotasMax();
		this.factibilizador=new Factibilizador(instancia,config,buscaLocalIntra, randNum);
		this.buscaLocal=new BuscaLocal(instancia,config,buscaLocalIntra, randNum);
		
		this.criterioAceitacao=new CALimiar(config);
		
		this.distEntreSolucoes=new Distancia(instancia,config);
		
		this.perturbadores=new Perturbacao[config.getPerturbacao().length];

		AjusteOmega novo;
		for (int i = 0; i < config.getPerturbacao().length; i++)
		{
			for (int K = numRotasMin; K <= numRotasMax; K++)
			{
				novo=new AODist(config,instancia.getSize());
				configuradoresOmega.put(config.getPerturbacao()[i]+""+K, novo);
			}
		}

		for (int i = 0; i < perturbadores.length; i++)
		{
			this.perturbadores[i]=perturbacaoBuild.ConstruirPerturbacao(instancia, config,
			config.getPerturbacao()[i],configuradoresOmega, randNum);
		}
	}

	public void procurar()
	{
		iterador=0;
		inicio=System.currentTimeMillis();
		inicioLastBestSol=inicio;

		do 
		{
			//System.out.println("reference solution: " + this.solucaoReferencia.f + " solution: " + this.solucao.f);
			solucaoReferencia.construirSolucao(numRotasMin);

			factibilizou=factibilizador.factibilizar(solucaoReferencia);
			srcCost += factibilizador.srcCost;
			//System.out.println("reference solution: " + this.solucaoReferencia.f + " solution: " + this.solucao.f);
		}
		while (!factibilizou);

		buscaLocal.buscaLocal(solucaoReferencia,true);
		srcCost += buscaLocal.srcCost;
		
		melhorSolucao.clone(solucaoReferencia);
		
		while(!stoppingCriterion())
		{
			isMelhorSolucao=false;
			iterador++;

			//System.out.println("reference solution: " + this.solucaoReferencia.f + " solution: " + this.solucao.f);

			perturbacao();

			//System.out.println("reference solution: " + this.solucaoReferencia.f + " solution: " + this.solucao.f);

			buscaLocal();

			//System.out.println("reference solution: " + this.solucaoReferencia.f + " solution: " + this.solucao.f);

			update();
			
			criterioAceitacao(solucao);

			//System.out.println("reference solution: " + this.solucaoReferencia.f + " solution: " + this.solucao.f);

		}
		
		tempoFinal=(double)(System.currentTimeMillis()-inicio)/1000; //除1000得到的是秒
	}
	
	private void perturbacao()
	{
		do 
		{
			solucao.clone(solucaoReferencia);
			//perturbacaoEscolhida=perturbadores[rand.nextInt(perturbadores.length)];  //randomly select a perturbation
			perturbacaoEscolhida=perturbadores[rand.nextInt(3)];
			perturbacaoEscolhida.perturbar(solucao);
//			srcCost += perturbacaoEscolhida.srcCost;
			factibilizou=factibilizador.factibilizar(solucao);
			srcCost += factibilizador.srcCost;
		}
		while (!factibilizou);
	}
	
	private void buscaLocal()
	{
		buscaLocal.buscaLocal(solucao,true);
		srcCost += buscaLocal.srcCost;
		mFBL.setValor(solucao.f);
		distanciaBLEdge=distEntreSolucoes.distanciaEdge(solucao,solucaoReferencia);
	}
	
	private void update()
	{
		analisaSolucaoAlto(false,"",solucao);
		
		perturbacaoEscolhida.getConfiguradorOmegaEscolhido().setDistancia(distanciaBLEdge);
	}
	
	public void criterioAceitacao(Solucao solucao)
	{
		if(criterioAceitacao.aceitaSolucao(solucao,distanciaBLEdge, iterador))
		{
			aceitoCriterio=true;
			solucaoReferencia.clone(solucao);
		}
		else {
			aceitoCriterio = false;
		}
	}
	
	public void analisaSolucao(boolean PR,String metodo,Solucao solucao2)
	{
		if((solucao2.f-melhorF)<-epsilon)
		{		
			isMelhorSolucao=true;
			melhorF=solucao2.f;
			
			inicioLastBestSol=System.currentTimeMillis();
			tempoMF=(double)(System.currentTimeMillis()-inicio)/1000;
			iteradorMF=iterador;
			
			melhorSolucao.clone(solucao2);

			if(print){
				System.out.println("melhorF: "+melhorF+" K: "+solucao2.NumRotas
						+" tempoMF: "+tempoMF+" gap: "+deci.format(getGap())
						+" metodo: "+metodo
						+" iteradorMF: "+iteradorMF
						+" HeuAdd: "+perturbacaoEscolhida.heuristicaAdicaoEscolhida
						+" srcCost: " + srcCost
				);
				bestSrcCost = srcCost;
			}


		}
	}
	
	private boolean stoppingCriterion()
	{
		switch(stoppingCriterion)
		{
			case Time: 	if(melhorF<=otimo||MAX<=(System.currentTimeMillis()-inicio)/1000)
							return true;
						break;
						
			case Iteration: if(melhorF<=otimo||MAX<=iterador||srcCost >= 100000000)
//			case Iteration: if(melhorF<=otimo||MAX<=iterador)
								return true;
							break;
							
			case IterationWithoutImprovement: 	if(melhorF<=otimo||MAX<=(iterador-iteradorMF))
													return true;
												break;
		}
		return false;
	}

	public static void main(String[] args) throws IOException {
		String file_n = "";
		int size = 18;
		int[] num_veh = new int[]{3, 5};
		int[] num_nodes = new int[]{40, 60, 80, 100, 120, 140, 160};
		double startTime = System.currentTimeMillis();
		double endTime = System.currentTimeMillis();

		double t[][] = new double[size][10]; // 5组数据每组十个
		double a[] = new double[size]; //存储时间平均值
		double vI[][] = new double[size][10]; //存所有结果
		double v[][] = new double[size][2]; //存平均和最佳

		double time = 0; // 存时间
		double average = 0; //存储时间平均值
		double valueIni = 0; //存所有结果
		double value = 0; //存平均和最佳
		long[] srcCosts = new long[18];
		long[] srcBestCosts = new long[18];
		//初始化
		for(int i = 0; i < 18; i++){
			a[i] = 0;
			srcCosts[i] = 0;
			srcBestCosts[i] = Long.MAX_VALUE;
			for(int j = 0; j < 10; j++){
				t[i][j] = 0;
				vI[i][j] = 0;
			}
			v[i][1] = Double.POSITIVE_INFINITY; //附上最大值
			v[i][0] = 0;

		}
//		for(int i = 0; i < size; i++) {
//			t[i] = 0;
//			vI[i] = 0;
//			v[1] = Double.POSITIVE_INFINITY; //附上最大值
//			v[0] = 0;
//		}
		LeituraParametros leitor = new LeituraParametros();
		leitor.lerParametros(args);
		//对文件进行求解
		for(int ii = 0; ii < 18; ii++) {
//		for(int pro = 0; pro < 10; pro++){
//			int temp_veh = 0;
//			int temp_nodes = 0;
//			String file_n = "";
//			for(int ii = 0; ii < 10; ii++){
//				if(pro < 5){
//					temp_veh = num_veh[0];
//					temp_nodes = num_nodes[pro];
//					leitor.setFile("D:/Codes/AILS-HVRP/src/Instances/Random/data_gen_speed_txt/v"
//							+ num_veh[0] + "-c"+num_nodes[pro] + "/ins" + ii * 100 +".txt");
//					leitor.setFile("D:/Codes/AILS-HVRP/src/Instances/Taillard_20.txt");
//					System.out.println("计算v"+num_veh[0] + "-c"+num_nodes[pro]+",第"+ ii * 100 + "个文件");
//					file_n = "D:/Codes/AILS-HVRP/src/result_random/ins-v"+num_veh[0]+"-c"+num_nodes[pro]+"_"+ii*100+".txt";
//				}
//				else{
//					temp_veh = num_veh[1];
//					temp_nodes = num_nodes[pro-3];
//					leitor.setFile("D:/Codes/AILS-HVRP/src/Instances/Random/data_gen_txt/v"
//							+ num_veh[1] +"-c" + num_nodes[pro-3] + "/ins"+ ii * 100 +".txt");
//					System.out.println("计算v" + num_veh[1] + "-c" + num_nodes[pro-3] + ",第"+ ii * 100 + "个文件");
//					file_n = "D:/Codes/AILS-HVRP/src/result_random/ins-v"+num_veh[1]+"-c"+num_nodes[pro-3]+"_"+ii*100+".txt";
//				}
			String file_xlsx = "D:/Codes/AILS-HVRP/src/result_random/" + "test.xlsx";
			File file = new File(file_xlsx);
			if (ii < 8){
				leitor.setFile("D:/Codes/AILS-HVRP/src/Instances/Taillard_" + (13 + ii) + ".txt");
				file_n = "D:/Codes/AILS-HVRP/src/result_random/ins-Taillard_" + (13 + ii) + "ali_bc.txt";
			}
			else if(ii < 13){
				leitor.setFile("D:/Codes/AILS-HVRP/src/Instances/Li_H" + (ii - 7) + ".txt");
				file_n = "D:/Codes/AILS-HVRP/src/result_random/ins-Li_H" + (ii - 7) + "ali_bc.txt";
			}
			else{
				leitor.setFile("D:/Codes/AILS-HVRP/src/Instances/N" + (ii - 12) + ".txt");
				file_n = "D:/Codes/AILS-HVRP/src/result_random/ins-N" + (ii - 12) + "ali_bc.txt";
			}
//			if (ii < 5){
//				leitor.setFile("D:/Codes/AILS-HVRP/src/Instances/bigTSP/bigTSP100_" + (1 + ii) + ".txt");
//			}
//			else if(ii < 10){
//				leitor.setFile("D:/Codes/AILS-HVRP/src/Instances/bigTSP/bigTSP300_" + (ii - 4) + ".txt");
//			}
//			else{
//				leitor.setFile("D:/Codes/AILS-HVRP/src/Instances/bigTSP/bigTSP500_" + (ii - 9) + ".txt");
//			}



			Instancia instancia = new Instancia(leitor.getFile(), leitor.getConfig(), leitor.isRounded(), leitor.getVariant());
			//计算并记录所有的时间和长度
			for (int jj = 0; jj < 10; jj++) {
				System.out.println("计算第" + ii + "个文件, " + "第" + jj + "次：");
				startTime = System.currentTimeMillis();
				int randNum = 2025;
				AILS igas = new AILS(instancia, leitor.getConfig(), leitor.getBest(), leitor.getTimeLimit(), randNum);
				igas.bestSrcCost = 0;
				igas.procurar();
				endTime = System.currentTimeMillis();
				System.out.println("melhorF: "+igas.melhorF+" K: "+igas.melhorSolucao.NumRotas
						+" tempoMF: "+ (endTime - startTime) / 1000
						+" iteradorMF: "+ igas.iterador
						+" srcCost: " + igas.srcCost
						+" bestSrcCost: " + igas.bestSrcCost);
				t[ii][jj] = (endTime - startTime) / 1000;
				vI[ii][jj] = igas.melhorF;
				srcCosts[ii] += igas.srcCost;

				if (jj == 0){
					srcBestCosts[ii] = igas.bestSrcCost;
				}
				else if (vI[ii][jj] > vI[ii][jj-1]){
					srcBestCosts[ii] = igas.bestSrcCost;
				}

//			}
//		}

//				startTime = System.currentTimeMillis();
//				AILS igas = new AILS(instancia, leitor.getConfig(), leitor.getBest(), leitor.getTimeLimit());
//				igas.procurar();
//				System.out.println("reference solution: " + igas.solucaoReferencia.f + " solution: " + igas.solucao.f);
//
//				endTime = System.currentTimeMillis();
//				time = (endTime - startTime) / 1000;
//				valueIni = igas.melhorF;

				//写入文件
//				BufferedWriter recording = new BufferedWriter(new FileWriter(file_n));
//
//				System.out.println("v" + temp_veh + "-c" + temp_nodes + "_" + ii * 100);
//				//System.out.println("最佳长度：" + value[1]);
//				System.out.println("长度：" + valueIni);
//				System.out.println("时间： " + time);
//				System.out.println(" ");
//				recording.write("v" + temp_veh + "-c" + temp_nodes + "\r\n");
//				//recording.write("最佳长度：" + value[1] + "\r\n");
//				recording.write("长度：" + valueIni + " ");
//				recording.write("时间： " + time + "\r\n");
//				recording.write("\r\n");
//
//				recording.close();
//				System.out.println("文件写入成功！");
			}

//		}



			//计算平均和最佳
//		for(int t = 0; t < 10;t++){
//			for(int q = 0; q < size; q++){
//				average[t] += time[t][q];
//				value[t][0] += valueIni[t][q];
//			}
//			average[t] /= size;
//			value[t][0] /= size;
//			for(int y = 0; y < size; y++){
//				if(value[t][1] > valueIni[t][y]) {
//					value[t][1] = valueIni[t][y];
//				}
//			}
//		}

//			for(int o = 0; o < 10; o++){
//				a[ii] += t[ii][o];
//				v[ii][0] += vI[ii][o];
//			}
//			a[ii] /= 10;
//			v[ii][0] /= 10;
//			v[ii][1] = vI[ii][0];
//			for(int y = 0; y < 10; y++){
//				if(v[ii][1] > vI[ii][y]) {
//					v[ii][1] = vI[ii][y];
//				}
//			}
//
////			输出
//			System.out.println("第 "+ii+" 份文件:");
//			System.out.println("最佳长度：" + v[ii][1]);
//			System.out.println("平均长度：" + v[ii][0]);
//			System.out.println("平均时间： " + a[ii]);
//			System.out.println(" ");
//
//			Workbook workbook1 = null;
//			if (!file.exists()) {
//				try  {
//					file.createNewFile();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//
//			try (FileInputStream fileInputStream = new FileInputStream(file_xlsx);
//				 Workbook workbook = WorkbookFactory.create(fileInputStream)) {
//
//				// 打开现有的工作簿并进行操作
//				Sheet sheet = workbook.getSheet("Sheet1");
//				//write
//				for (int rowIdx = ii; rowIdx < ii + 1; rowIdx++) {
//					Row row = sheet.createRow(rowIdx);
//					Cell cell1 = row.createCell(0);
//					cell1.setCellValue("file: " + ii);
//					Cell cell2 = row.createCell(1);
//					cell2.setCellValue(v[ii][1]);
//					Cell cell3 = row.createCell(2);
//					cell3.setCellValue(v[ii][0]);
//					Cell cell4 = row.createCell(3);
//					cell4.setCellValue(a[ii]);
//					Cell cell5 = row.createCell(4);
//					cell5.setCellValue((double) srcCosts[ii] / 10);
//					Cell cell6 = row.createCell(5);
//					cell6.setCellValue((double) srcBestCosts[ii]);
//				}
//				// 保存修改后的工作簿到同一文件
//				try (FileOutputStream outputStream = new FileOutputStream(file_xlsx)) {
//					workbook.write(outputStream);
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//				System.out.println("XLSX 文件打开成功并修改保存！");
//			} catch (IOException e) {
//				e.printStackTrace();
//			}

//			BufferedWriter recording = new BufferedWriter(new FileWriter(file_n));
//			recording.write("第 "+ii+" 份文件:\r\n");
//			recording.write("最佳长度：" + v[ii][1] + "\r\n");
//			recording.write("平均长度：" + v[ii][0] + "\r\n");
//			recording.write("平均时间： " + a[ii] + "s"  + "\r\n");
//			recording.write( "\r\n");
//			recording.close();
//			System.out.println("文件写入成功！");
		}
//		BufferedWriter recording = new BufferedWriter(new FileWriter(file_n));
//		for(int i = 0; i < size; i++){
//			System.out.println("第 "+i+" 份文件:");
//			System.out.println("最佳长度：" + v[i][1]);
//			System.out.println("平均长度：" + v[i][0]);
//			System.out.println("平均时间： " + a[i] + "s");
//			System.out.println(" ");
//			recording.write("第 "+i+" 份文件:\r\n");
//			recording.write("最佳长度：" + v[i][1] + "\r\n");
//			recording.write("平均长度：" + v[i][0] + "\r\n");
//			recording.write("平均时间： " + a[i] + "s"  + "\r\n");
//			recording.write( "\r\n");
//		recording.close();
//		System.out.println("文件写入成功！");

//		}


	}
	
	public Solucao getMelhorSolucao() {
		return melhorSolucao;
	}

	public double getMelhorF() {
		return melhorF;
	}

	public double getTempoMF() {
		return tempoMF;
	}
	
	public double getTempoFinal() {
		return tempoFinal;
	}

	public int getIteradorMF() {
		return iteradorMF;
	}

	public Media getMediaBL() {
		return mFBL;
	}

	public double getGap()
	{
		return 100*(double)((double)melhorF-otimo)/otimo;
	}
	
	public double getGapMBLDinan()
	{
		return 100*(double)(mFBL.mediaDinam-(double)melhorF)/(double)melhorF;
	}
	
	public double getGapMBLGlobal()
	{
		return 100*(double)(mFBL.mediaGlobal-(double)melhorF)/(double)melhorF;
	}

	public boolean isPrint() {
		return print;
	}

	public void setPrint(boolean print)
	{
		this.print = print;
	}

	public Solucao getSolucao() {
		return solucao;
	}

	public int getIterador() {
		return iterador;
	}

	public void analisaSolucaoAlto(boolean PR,String sufixo,Solucao solucao2)
	{
		analisaSolucao(PR,perturbacaoEscolhida.getTipoPerturbacao()+sufixo,solucao2);
	}

	public CA getCriterioAceitacao() {
		return criterioAceitacao;
	}

	public Perturbacao[] getPerturbadores() {
		return perturbadores;
	}
}
