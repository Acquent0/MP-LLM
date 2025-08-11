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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Random;


public class AILS_FSMF
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
	Random rand=new Random();


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

	public AILS_FSMF(Instancia instancia, Config config, double d, double MAX, int randNum)
	{
		this.instancia=instancia;

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
			solucaoReferencia.construirSolucao(numRotasMin);
			factibilizou=factibilizador.factibilizar(solucaoReferencia);
		}
		while (!factibilizou);
		
		buscaLocal.buscaLocal(solucaoReferencia,true);
		
		melhorSolucao.clone(solucaoReferencia);
		
		while(!stoppingCriterion())
		{
			isMelhorSolucao=false;
			iterador++;
			perturbacao();

			buscaLocal();
			
			update();
			
			criterioAceitacao(solucao);
		}
		
		tempoFinal=(double)(System.currentTimeMillis()-inicio)/1000; //除1000得到的是秒
	}
	
	private void perturbacao()
	{
		do 
		{
			solucao.clone(solucaoReferencia);
			perturbacaoEscolhida=perturbadores[rand.nextInt(perturbadores.length)];
			perturbacaoEscolhida.perturbar(solucao);
			factibilizou=factibilizador.factibilizar(solucao);
		}
		while (!factibilizou);
	}
	
	private void buscaLocal()
	{
		buscaLocal.buscaLocal(solucao,true);
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
		if(criterioAceitacao.aceitaSolucao(solucao,distanciaBLEdge,iterador))
		{
			aceitoCriterio=true;
			solucaoReferencia.clone(solucao);
		}
		else
			aceitoCriterio=false;
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

			if(print)
				System.out.println("melhorF: "+melhorF+" K: "+solucao2.NumRotas
				+" tempoMF: "+tempoMF+" gap: "+deci.format(getGap())
				+" metodo: "+metodo
				+" iteradorMF: "+iteradorMF
				+" HeuAdd: "+perturbacaoEscolhida.heuristicaAdicaoEscolhida
				);
		}
	}
	
	private boolean stoppingCriterion()
	{
		switch(stoppingCriterion)
		{
			case Time: 	if(melhorF<=otimo||MAX<=(System.currentTimeMillis()-inicio)/1000)
							return true;
						break;
						
			case Iteration: if(melhorF<=otimo||MAX<=iterador)
								return true;
							break;
							
			case IterationWithoutImprovement: 	if(melhorF<=otimo||MAX<=(iterador-iteradorMF))
													return true;
												break;
		}
		return false;
	}
	
	public static void main(String[] args) throws IOException {
		int size = 17;
		double startTime = System.currentTimeMillis();
		double endTime = System.currentTimeMillis();
		//写入文件
		BufferedWriter recording = new BufferedWriter(new FileWriter("FSMF.txt"));
		double time[][] = new double[size][10]; // 5组数据每组十个
		double average[] = new double[size]; //存储时间平均值
		double valueIni[][] = new double[size][10]; //存所有结果
		double value[][] = new double[size][2]; //存平均和最佳
		//初始化
		for(int i = 0; i < size; i++){
			average[i] = 0;
			for(int j = 0; j < 10; j++){
				time[i][j] = 0;
				valueIni[i][j] = 0;
			}
			value[i][1] = Double.POSITIVE_INFINITY; //附上最大值
			value[i][0] = 0;

		}
		LeituraParametros leitor=new LeituraParametros();
		leitor.lerParametros(args);
		//对5个文件进行求解
		for(int ii = 0; ii < size; ii++){
			switch (ii){
				case 0: leitor.setFile("D:/Codes/AILS_HVRP_real/src/Instances/N1.txt");break;
				case 1: leitor.setFile("D:/Codes/AILS_HVRP_real/src/Instances/N2.txt");break;
				case 2: leitor.setFile("D:/Codes/AILS_HVRP_real/src/Instances/N3.txt");break;
				case 3: leitor.setFile("D:/Codes/AILS_HVRP_real/src/Instances/N4.txt");break;
				case 4: leitor.setFile("D:/Codes/AILS_HVRP_real/src/Instances/N5.txt");break;
				case 5: leitor.setFile("D:/Codes/AILS_HVRP_real/src/Instances/Taillard_03.txt");break;
				case 6: leitor.setFile("D:/Codes/AILS_HVRP_real/src/Instances/Taillard_04.txt");break;
				case 7: leitor.setFile("D:/Codes/AILS_HVRP_real/src/Instances/Taillard_05.txt");break;
				case 8: leitor.setFile("D:/Codes/AILS_HVRP_real/src/Instances/Taillard_06.txt");break;
				case 9: leitor.setFile("D:/Codes/AILS_HVRP_real/src/Instances/Taillard_13.txt");break;
				case 10: leitor.setFile("D:/Codes/AILS_HVRP_real/src/Instances/Taillard_14.txt");break;
				case 11: leitor.setFile("D:/Codes/AILS_HVRP_real/src/Instances/Taillard_15.txt");break;
				case 12: leitor.setFile("D:/Codes/AILS_HVRP_real/src/Instances/Taillard_16.txt");break;
				case 13: leitor.setFile("D:/Codes/AILS_HVRP_real/src/Instances/Taillard_17.txt");break;
				case 14: leitor.setFile("D:/Codes/AILS_HVRP_real/src/Instances/Taillard_18.txt");break;
				case 15: leitor.setFile("D:/Codes/AILS_HVRP_real/src/Instances/Taillard_19.txt");break;
				case 16: leitor.setFile("D:/Codes/AILS_HVRP_real/src/Instances/Taillard_20.txt");break;
			}
			Instancia instancia=new Instancia(leitor.getFile(),leitor.getConfig(),leitor.isRounded(),leitor.getVariant());
			//计算并记录所有的时间和长度
			for(int jj = 0; jj < 10; jj++){
				System.out.println("计算第"+ ii + "个文件, " + "第" + jj +"次：");
				startTime = System.currentTimeMillis();
				int randNum = 2025;
				AILS_FSMF igas=new AILS_FSMF(instancia,leitor.getConfig(),leitor.getBest(),leitor.getTimeLimit(), randNum);
				igas.procurar();
				endTime = System.currentTimeMillis();
				time[ii][jj] = (endTime - startTime)/1000;
				valueIni[ii][jj] = igas.melhorF;
			}
		}
		//计算平均和最佳
		for(int t = 0; t < size;t++){
			for(int q = 0; q < 10; q++){
				average[t] += time[t][q];
				value[t][0] += valueIni[t][q];
			}
			average[t] /= 10;
			value[t][0] /= 10;
			for(int y = 0; y < 10; y++){
				if(value[t][1] > valueIni[t][y]) {
					value[t][1] = valueIni[t][y];
				}
			}
		}
		//输出
		for(int i = 0; i < size; i++){
			System.out.println("第 "+i+" 份文件:");
			System.out.println("最佳长度：" + value[i][1]);
			System.out.println("平均长度：" + value[i][0]);
			System.out.println("平均时间： " + average[i] + "s");
			System.out.println(" ");
			recording.write("第 "+i+" 份文件:\r\n");
			recording.write("最佳长度：" + value[i][1] + "\r\n");
			recording.write("平均长度：" + value[i][0] + "\r\n");
			recording.write("平均时间： " + average[i] + "s"  + "\r\n");
			recording.write( "\r\n");
		}
		recording.close();
		System.out.println("文件写入成功！");



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
