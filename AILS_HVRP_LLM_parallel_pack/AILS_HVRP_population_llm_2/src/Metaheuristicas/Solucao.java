package Metaheuristicas;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

import AjusteOmega.AODist;
import AjusteOmega.AjusteOmega;
import BuscaLocal.BuscaLocal;
import BuscaLocalIntra.BuscaLocalIntra;
import CriterioAceitacao.CA;
import CriterioAceitacao.CALimiar;
import Dados.Arquivo;
import Dados.Instancia;
import Dados.Instancias;
import Dados.Ponto;
import Dados.Veiculo;
import Factibilizadores.Factibilizador;
import Pertubacao.Perturbacao;
import Pertubacao.PerturbacaoBuild;
import Pertubacao.TipoGetVeiculo;

public class Solucao implements Comparable<Solucao>
{
	private Ponto[] pontos;
	Instancia instancia;
	Config config;
	protected int size;
	No[] solucao;
	protected No deposito;
	public Rota[] rotas;
	public int NumRotas;
	protected int NumRotasMin;
	protected int NumRotasMax;
	public double f=Double.MAX_VALUE;
	protected Random rand;
	public int distancia;
	double epsilon;
	private Veiculo veiculos[];
	private Veiculo listVeiculos[];
	Factibilizador factibilizador;
	BuscaLocalIntra buscaLocalIntra;
	BuscaLocal buscaLocal;
	public boolean factivel=false;
	int topVeiculos;
	TipoGetVeiculo tipoGetVeiculo;
	PerturbacaoBuild perturbacaoBuild;
	public Perturbacao[] perturbadores;
	public Perturbacao perturbacaoEscolhida;
	int numRotasMin;
	int numRotasMax;
	HashMap<String,AjusteOmega> configuradoresOmega=new HashMap<String,AjusteOmega>();
	CA criterioAceitacao;
	int numIterUpdate;


// -----------------population parameters-----------------

	double divValue=0;
	double fitness=0;
	long srcCost = 0;  // used for calculating the cost of the source solution inside the parallel process

	
//	-----------Comparadores-----------

	
	public Solucao(Instancia instancia,Config config, int randNum)
	{
		this.rand = new Random(randNum);
		this.instancia=instancia;
		this.config=config;
		this.pontos=instancia.getPontos();
		int deposito=instancia.getDeposito();
		this.size=instancia.getSize()-1;
		this.solucao=new No[size];
		this.NumRotasMin=instancia.getNumRotasMin();
		this.NumRotas=NumRotasMin;
		this.NumRotasMax=instancia.getNumRotasMax();
		this.deposito=new No(pontos[deposito],instancia);
		this.epsilon=config.getEpsilon();
		this.rotas=new Rota[NumRotasMax];
		int cont=0;
		this.numIterUpdate=config.getGamma();
		this.tipoGetVeiculo=config.getTipoGetVeiculo();
		this.criterioAceitacao=new CALimiar(config);

		this.numRotasMin=instancia.getNumRotasMin();
		this.numRotasMax=instancia.getNumRotasMax();

		this.criterioAceitacao=new CALimiar(config);

		this.perturbacaoBuild=new PerturbacaoBuild();
		this.perturbadores=new Perturbacao[config.getPerturbacao().length];

		this.buscaLocalIntra=new BuscaLocalIntra(instancia,config);
		this.factibilizador=new Factibilizador(instancia,config,buscaLocalIntra, randNum);
		this.buscaLocal=new BuscaLocal(instancia,config,buscaLocalIntra, randNum);

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
		
		if(instancia.getVariante()==Variante.FSMD||instancia.getVariante()==Variante.FSMF||instancia.getVariante()==Variante.FSMFD)
		{
			veiculos=new Veiculo[instancia.getNumTiposVeiculos()];
			listVeiculos=new Veiculo[instancia.getNumTiposVeiculos()+NumRotasMax];

			topVeiculos=veiculos.length;
			for (int i = 0; i < veiculos.length; i++) 
				veiculos[i]=new Veiculo(instancia.getTiposVeiculos()[i]);
		
			for (int i = 0; i < rotas.length; i++) 
			{
				rotas[i]=new Rota(instancia, this.deposito,i,new Veiculo(instancia.getTiposVeiculos()[rand.nextInt(instancia.getTiposVeiculos().length)]));
			}
		}
		else
		{
			veiculos=new Veiculo[NumRotasMax];
			topVeiculos=0;
			for (int i = 0; i < instancia.getTiposVeiculos().length; i++) 
			{
				for (int j = 0; j < instancia.getTiposVeiculos()[i].getM(); j++) 
					veiculos[topVeiculos++]=new Veiculo(instancia.getTiposVeiculos()[i]);
			}
			
			cont=0;
			for (int i = 0; i < rotas.length; i++) 
				rotas[i]=new Rota(instancia, this.deposito,i,veiculos[cont++]);
		}
		
		cont=0;
		for (int i = 0; i < (solucao.length+1); i++)
		{
			if(i!=deposito)
			{
				solucao[cont]=new No(pontos[i],instancia);
				cont++;
			}
		}
	}

	public void cleanSrcCost(){
		this.srcCost = 0;
	}
	
	public Veiculo getVeiculo()
	{
		switch(tipoGetVeiculo)
		{
			case Ale: 		return getVeiculoAle();
			case ProbMenor: return getVeiculoProbMenor();
			case ProbMaior: return getVeiculoProbMaior();
			case ProbProporcional: return getVeiculoProbProporcional();
		}
		return null;
	}
	
	public Veiculo getVeiculoAle()
	{
		return veiculos[rand.nextInt(veiculos.length)];
	}
	
	public Veiculo getVeiculoProbMenor()
	{
		int pos=rand.nextInt(veiculos.length*veiculos.length);
		int lin=pos/veiculos.length;
		int col=pos%veiculos.length;
		
		if(lin>col)
			pos=col;
		else
			pos=lin;
		
		return veiculos[veiculos.length-1-pos];
	}
	
	public Veiculo getVeiculoProbMaior()
	{
		int pos=rand.nextInt(veiculos.length*veiculos.length);
		int lin=pos/veiculos.length;
		int col=pos%veiculos.length;
		
		if(lin>col)
			pos=col;
		else
			pos=lin;
		
		return veiculos[pos];
	}
	
	public Veiculo getVeiculoProbProporcional()
	{
		int cont=0;
		for (int i = 0; i < NumRotas; i++) 
			listVeiculos[cont++]=rotas[i].getVeiculo();
		
		for (int i = 0; i < veiculos.length; i++)
			listVeiculos[cont++]=veiculos[i];
		
		
		return listVeiculos[rand.nextInt(cont)];
	}
	
	public Veiculo getVeiculoMaisCapacidade(Veiculo veiculo)
	{
		for (int i = veiculos.length-1; i >=0 ; i--) 
		{
			if(veiculos[i].getQ()>veiculo.getQ())
				return veiculos[i];
		}
		
		return veiculos[rand.nextInt(veiculos.length)];
	}

	public Veiculo getVeiculoFit(Rota rota)
	{
		for (int i = veiculos.length-1; i >=0 ; i--)
		{
			if(veiculos[i].getQ()>=rota.demandaTotal)
				return veiculos[i];
		}

		return veiculos[0];
	}
	
	public void construirSolucao(int numRotas)
	{
		this.NumRotas=numRotas;
		construirClienteRota();
	}
	
	public void construirClienteRota()
	{
		int index;
		No no,bestNo;
		f=0;
		
		No naoInseridos[]=new No[size];  // 未插入的点
		int contNaoInseridos=0;  // 未插入点的数量
		
		for (int i = 0; i < size; i++) 
		{
			solucao[i].limpar();
			naoInseridos[contNaoInseridos++]=solucao[i];
		}
		
		for (int i = 0; i < NumRotas; i++)
		{
			f+=rotas[i].limpar();
			
			index=rand.nextInt(contNaoInseridos);
			f+=rotas[i].addNoFinal(naoInseridos[index]);
			
			no=naoInseridos[index];
			naoInseridos[index]=naoInseridos[contNaoInseridos-1];
			naoInseridos[--contNaoInseridos]=no;
		}
		
		while(contNaoInseridos>0) 
		{
			index=rand.nextInt(contNaoInseridos);
			no=naoInseridos[index];
			bestNo=getBestKNNNo(no);
			f+=bestNo.rota.addDepois(no, bestNo);
			naoInseridos[index]=naoInseridos[contNaoInseridos-1];
			naoInseridos[--contNaoInseridos]=no;
		}
	}
	
	protected No getBestKNNNo(No no)
	{
		double bestCusto=Double.MAX_VALUE;
		No aux,bestNo=null;
		double custo,custoAnt;
		for (int i = 0; i < solucao.length; i++) 
		{
			aux=solucao[i];
			if(aux.jaInserido)
			{
				
				custo=instancia.dist(aux.nome,no.nome,aux.rota)+instancia.dist(no.nome,aux.prox.nome,aux.rota)-instancia.dist(aux.nome,aux.prox.nome,aux.rota);
				if(custo<bestCusto)
				{
					bestCusto=custo;
					bestNo=aux;
				}
			}
		}
		custo=instancia.dist(bestNo.nome,no.nome,bestNo.rota)+instancia.dist(no.nome,bestNo.prox.nome,bestNo.rota)-instancia.dist(bestNo.nome,bestNo.prox.nome,bestNo.rota);
		custoAnt=instancia.dist(bestNo.ant.nome,no.nome,bestNo.rota)+instancia.dist(no.nome,bestNo.nome,bestNo.rota)-instancia.dist(bestNo.ant.nome,bestNo.nome,bestNo.rota);
		if(custo<custoAnt)
			return bestNo;
		
		return bestNo.ant;
	}

	public void clone(Solucao referencia)
	{
		this.NumRotas=referencia.NumRotas;
		this.f=referencia.f;
		this.factivel=referencia.factivel;
		this.criterioAceitacao=referencia.criterioAceitacao;
		this.factibilizador=referencia.factibilizador;
		this.buscaLocal=referencia.buscaLocal;
		this.perturbacaoBuild=referencia.perturbacaoBuild;
		this.perturbadores=referencia.perturbadores;
		this.perturbacaoEscolhida=referencia.perturbacaoEscolhida;
		
		for (int i = 0; i < rotas.length; i++)
		{
			rotas[i].nomeRota=i;
			referencia.rotas[i].nomeRota=i;
		}
		
		for (int i = 0; i < rotas.length; i++)
		{
			rotas[i].demandaTotal=referencia.rotas[i].demandaTotal;
			rotas[i].fRota=referencia.rotas[i].fRota;
			rotas[i].numElements=referencia.rotas[i].numElements;
			rotas[i].alterada=referencia.rotas[i].alterada;
			
			rotas[i].veiculo.setF(referencia.rotas[i].getVeiculo().getF());
			rotas[i].veiculo.setQ(referencia.rotas[i].getVeiculo().getQ());
			rotas[i].veiculo.setR(referencia.rotas[i].getVeiculo().getR());
			
			if(referencia.rotas[i].inicio.ant==null)
				rotas[i].inicio.ant=null;
			else if(referencia.rotas[i].inicio.ant.nome==0)
				rotas[i].inicio.ant=rotas[i].inicio;
			else
				rotas[i].inicio.ant=solucao[referencia.rotas[i].inicio.ant.nome-1];
			
			if(referencia.rotas[i].inicio.prox==null)
				rotas[i].inicio.prox=null;
			else if(referencia.rotas[i].inicio.prox.nome==0)
				rotas[i].inicio.prox=rotas[i].inicio;
			else
				rotas[i].inicio.prox=solucao[referencia.rotas[i].inicio.prox.nome-1];
		}
		
		for (int i = 0; i < solucao.length; i++)
		{
			solucao[i].rota=rotas[referencia.solucao[i].rota.nomeRota];
			solucao[i].jaInserido=referencia.solucao[i].jaInserido;
			
			if(referencia.solucao[i].ant.nome==0)
				solucao[i].ant=rotas[referencia.solucao[i].ant.rota.nomeRota].inicio;
			else
				solucao[i].ant=solucao[referencia.solucao[i].ant.nome-1];
				
			if(referencia.solucao[i].prox.nome==0)
				solucao[i].prox=rotas[referencia.solucao[i].prox.rota.nomeRota].inicio;
			else
				solucao[i].prox=solucao[referencia.solucao[i].prox.nome-1];
		}

	}
	
	@Override
	public String toString() 
	{
		String str="";
		for (int i = 0; i < NumRotas; i++) 
		{
			str+=rotas[i].toString2()+"\n";
		}
		str+="Cost "+f+"\n";
		return str;
	}
	
	public void removeRotasVazias()
	{
		for (int i = 0; i < NumRotas; i++) 
		{
			if(rotas[i].inicio==rotas[i].inicio.prox)
			{
				removeRota(i);
				i--;
			}
		}
	}
	
	private void removeRota(int index)
	{
		Rota aux=rotas[index];
		
		if(instancia. getVariante()==Variante.HVRPFD||instancia. getVariante()==Variante.FSMFD||instancia. getVariante()==Variante.FSMF)
		{
			f-=rotas[index].getVeiculo().getF();
		}
		
		if(index!=NumRotas-1)
		{
			rotas[index]=rotas[NumRotas-1];
			rotas[NumRotas-1]=aux;
		}
		NumRotas--;
	}
	
	//------------------------get&set-------------------------

	@Override
	public int compareTo(Solucao x)
	{
		if(this.f<x.f)
			return -1;
		else if(this.f>x.f)
			return 1;
		return 0;
	}

	public double getDivValue() {
		return divValue;
	}

	public double getFitness() {
		return fitness;
	}

	public Rota[] getRotas() {
		return rotas;
	}

	public int getNumRotas() {
		return NumRotas;
	}

	public No[] getSolucao() {
		return solucao;
	}



}
