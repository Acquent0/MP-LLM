package Metaheuristicas;

public enum StoppingCriterion 
{
	Time("time"),
	Iteration("iteration"),
	IterationWithoutImprovement("iterationWithoutImprovement");
	
	final String tipo;
	
	StoppingCriterion(String tipo)
	{
		this.tipo=tipo;
	}
}
