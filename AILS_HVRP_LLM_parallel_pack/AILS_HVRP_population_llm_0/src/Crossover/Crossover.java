package Crossover;

import Metaheuristicas.Config;
import Metaheuristicas.No;
import Metaheuristicas.Rota;
import Metaheuristicas.Solucao;
import Dados.Instancia;

import java.util.Random;

public class Crossover {
    public Solucao solucaoA;
    public Solucao solucaoB;
    public Solucao son;
    public No[] solucaoSon;
    public Rota[] rotasSon;

    Instancia instancia;
    Config config;

    protected Random rand=new Random();


    public Crossover(Instancia instancia, Config config, int randNum)
    {
        son = new Solucao(instancia,config, randNum);
        this.instancia = instancia;
    }

    public void setCrossover(Solucao solucaoA, Solucao solucaoB){
        this.solucaoA=solucaoA;
        this.solucaoB=solucaoB;
        son.clone(solucaoA);

        solucaoSon = son.getSolucao();
        rotasSon = son.getRotas();
    }

    public Solucao remainCross()
    {
        int index;
        int size = instancia.getSize() - 1;
        No no,bestNo;

        No naoInseridos[]=new No[size];  // 未插入的点
        int contNaoInseridos=0;  // 未插入点的数量

        for (int i = 0; i < size; i++)
        {
            int typeA = son.getSolucao()[i].rota.getVeiculo().getType();
            int typeB = solucaoB.getSolucao()[i].rota.getVeiculo().getType();
            if (typeA != typeB) {
                int rotaNome = solucaoSon[i].rota.nomeRota;
                son.f += rotasSon[rotaNome].remove(solucaoSon[i]);

                solucaoSon[i].limpar();
                naoInseridos[contNaoInseridos++] = solucaoSon[i];
        }

        while(contNaoInseridos>0)
        {
            index=rand.nextInt(contNaoInseridos);
            no=naoInseridos[index];
            bestNo=getBestKNNNo(no);
            son.f+=bestNo.rota.addDepois(no, bestNo);
            naoInseridos[index]=naoInseridos[contNaoInseridos-1];
            naoInseridos[--contNaoInseridos]=no;
        }
    }

        return son;
    }

    protected No getBestKNNNo(No no)
    {
        double bestCusto=Double.MAX_VALUE;
        No aux,bestNo=null;
        double custo,custoAnt;
        for (int i = 0; i < solucaoSon.length; i++)
        {
            aux=solucaoSon[i];
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
}
