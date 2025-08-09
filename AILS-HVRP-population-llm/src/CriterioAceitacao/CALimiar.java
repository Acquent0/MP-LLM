package CriterioAceitacao;

import Metaheuristicas.Config;
import Metaheuristicas.Solucao;

public class CALimiar extends CA
{
//	double[] kBest = new double[6];
	public CALimiar(Config config)
	{
		super(config);
		// give kbest big values
//		for (int i = 0; i < 6; i++)
//			kBest[i] = Double.MAX_VALUE;
	}
	
	public boolean aceitaSolucao(Solucao solucao, double distanciaBLEdge, int iterations)
	{
		update(solucao.f);
		limiarF=(int)(teto+(eta*(mFBL.getMediaDinam()-teto)));

//		if (iterations < 7)
//			kBest[iterations-1] = solucao.f;
//		else
//		{
//			for (int i = 0; i < 5; i++)
//				kBest[i] = kBest[i+1];
//			kBest[5] = solucao.f;
//		}
//
//		// find the smallest value in kBest
//		double min = kBest[0];
//		for (int i = 1; i < 6; i++)
//			if (kBest[i] < min)
//				min = kBest[i];

		if(solucao.f<=limiarF)
//		if(solucao.f<=min)
		{
			qtnPassouReal++;
			return true;
		}
		else
			return false;
	}
	
	@Override
	public double getEta() {
		return eta;
	}

	@Override
	public double getLimiarF() {
		return limiarF;
	}
	
	public void setEta(double eta) {
		this.eta = eta;
	}
	
	public void setFluxoIdeal(double fluxoIdeal) {}
}
