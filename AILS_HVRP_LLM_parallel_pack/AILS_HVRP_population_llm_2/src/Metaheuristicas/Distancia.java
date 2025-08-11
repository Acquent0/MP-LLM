package Metaheuristicas;

import Dados.Instancia;

public class Distancia 
{
	boolean jaNomeouA[];
	boolean jaNomeouB[];
	int topForaRota=0;
	int NumRotas;
	int qtnRotas[];
	No solucaoB[];
	No solucaoA[];
	private Rota rotasA[];
	int topParRotasVet=0;
	Instancia instancia;
	
	public Distancia(Instancia instancia,Config config)
	{
		this.instancia=instancia;
		this.qtnRotas=new int[instancia.getNumRotasMax()];
		this.jaNomeouA=new boolean[instancia.getNumRotasMax()];
		this.jaNomeouB=new boolean[instancia.getNumRotasMax()];
	}
	
	public int distanciaEdge(Solucao a, Solucao b)
	{
		this.solucaoB=b.getSolucao();
		this.rotasA=a.rotas;
		this.solucaoA=a.getSolucao();
		
		int viz;
		int dist=0;
		for (int i = 0; i < solucaoA.length; i++)
		{
			viz=solucaoA[i].prox.nome;
			if(solucaoB[i].prox.nome!=viz&&viz!=solucaoB[i].ant.nome)
				dist++;

			if(solucaoA[i].ant.nome==0)
			{
				if(solucaoB[i].prox.nome!=0&&0!=solucaoB[i].ant.nome)
					dist++;
			}
		}
		
		return dist;
	}

	public double CalHammingDis(Solucao a, Solucao b)
	{
		this.solucaoB=b.getSolucao();
		this.solucaoA=a.getSolucao();
		int EdgeA = a.size + a.getNumRotas();
		int EdgeB = b.size + b.getNumRotas();

		int viz;
		int sim=0;
		for (int i = 0; i < solucaoA.length; i++)
		{
			viz=solucaoA[i].prox.nome;
			if(solucaoB[i].prox.nome==viz||viz==solucaoB[i].ant.nome)
				sim++;

			if(solucaoA[i].ant.nome==0)
			{
				if(solucaoB[i].prox.nome==0||0==solucaoB[i].ant.nome)
					sim++;
			}
		}

        return 1 - ((double) (2 * sim) / (EdgeA + EdgeB));
	}

}
