package Pertubacao;

import java.util.HashMap;

import AjusteOmega.AjusteOmega;
import Dados.Instancia;
import Metaheuristicas.Config;
import Metaheuristicas.No;
import Metaheuristicas.Solucao;

//Remocao concentrica não deixando no mesmo lugar
//表示同心圆移除，但不要让点保持在同一位置

public class Perturbacao23 extends Perturbacao
{
	public Perturbacao23(Instancia instancia, Config config, HashMap<String, AjusteOmega> configuradoresOmega)
	{
		super(instancia, config, configuradoresOmega);
		this.tipoPerturbacao=TipoPerturbacao.Pert23;
	}

	public void perturbar(Solucao s)
	{
		srcCost = 0;
		setSolucao(s);
//		System.out.println("inicio\n"+s.toStringMeu());
		VariandoNumRotas(s);  //变化解中的路径数量
		
//		---------------------------------------------------------------------
		//生成候选点
		contCandidatos=0;
		No referencia=solucao[rand.nextInt(solucao.length)];  // 从解中随机选取一个点
		for (int i = 0; i < omega&&i < referencia.knn.length; i++)  // omega是候选点的邻域，但是邻域的大小不能超过解的大小
		{
			if(referencia.knn[i]!=0) //因为knn大小是解的大小，所以这里的判断是为了防止knn中的0
			{
				no=solucao[referencia.knn[i]-1];
				candidatos[contCandidatos]=no;
				ordem[contCandidatos]=contCandidatos;  //ordem是候选点的顺序
				contCandidatos++;
				no.antOld=no.ant;
				no.proxOld=no.prox;
			}
		}

		mudaOrdem(contCandidatos);  //把候选解随机交换顺序，可能是为了防止每次都是从同一个点开始移除

		for (int i = 0; i < contCandidatos; i++)
		{
			no=candidatos[ordem[i]];
			f+=no.rota.remove(no);
			bestNo=getNo(no);  // 根据插入准则选点
			f+=bestNo.rota.addDepois(no, bestNo); //把no插入到bestNo后面
		}



		passaSolucao(s);
	}
}
