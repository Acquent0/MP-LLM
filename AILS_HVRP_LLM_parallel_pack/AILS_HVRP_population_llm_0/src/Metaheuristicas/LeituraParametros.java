package Metaheuristicas;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;

public class LeituraParametros 
{
	
	private String file="";
	private boolean rounded=false;
	private Variante variant=Variante.HVRPFD;
	private double limit=Double.MAX_VALUE; //无穷大的迭代次数或时间
	private double best=0;
	private Config config =new Config();
	
	public void lerParametros(String[] args)
	{
		try 
		{
			for (int i = 0; i < args.length-1; i+=2) 
			{
				switch(args[i])
				{
					case "-file": file=getEndereco(args[i+1]);break;
					case "-rounded": rounded=getRound(args[i+1]);break;
					case "-variant": variant=getVariant(args[i+1]);break;
					case "-limit": limit=getLimit(args[i+1]);break;
					case "-best": best=getBest(args[i+1]);break;
					case "-eta": config.setEta(getEta(args[i+1]));break;
					case "-alpha": config.setAlpha(getAlpha(args[i+1]));break;
					case "-varphi": config.setVarphi(getVarphi(args[i+1]));break;
					case "-gamma": config.setGamma(getVarphi(args[i+1]));break;
					case "-dBeta": config.setDBeta(getDBeta(args[i+1]));break;
//					case "-stoppingCriterion": config.setStoppingCriterion(getStoppingCriterion(args[i+1]));break;
					case "-stoppingCriterion": config.setStoppingCriterion(StoppingCriterion.Iteration);break;
				}
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		//config.setStoppingCriterion(StoppingCriterion.Time);
		
		System.out.println("File: "+file);
		System.out.println("Rounded: "+rounded);
		System.out.println("Variant: "+variant);
		System.out.println("limit: "+limit);
		System.out.println("Best: "+best);
		System.out.println("LimitTime: "+limit);
		System.out.println(config);
	}

	public void adjustArgs(HashMap<String, Double[]> arguments, Config c, int index){
		// get arguments
		int[] combs = new int[5];
		int ind = index + 1;
		if (ind <= 5){
			combs = new int[]{(int) Math.ceil((double) ind / 5), (int) (ind % 5), (int) (ind % 5), (int) (ind % 5), (int) (ind % 5)};
		}
		else if (ind <= 10){
			combs = new int[]{(int) Math.ceil((double) ind / 5), (int) (ind % 5), (int) ((ind + 1) % 5), (int) ((ind + 2) % 5), (int) ((ind + 3) % 5)};
		}
		else if (ind <= 15){
			combs = new int[]{(int) Math.ceil((double) ind / 5), (int) (ind % 5), (int) ((ind + 2) % 5), (int) ((ind + 4) % 5), (int) ((ind + 1) % 5)};
		}
		else if (ind <= 20){
			combs = new int[]{(int) Math.ceil((double) ind / 5), (int) (ind % 5), (int) ((ind + 3) % 5), (int) ((ind + 1) % 5), (int) ((ind + 4) % 5)};
		}
		else if (ind <= 25){
			combs = new int[]{(int) Math.ceil((double) ind / 5), (int) (ind % 5), (int) ((ind + 4) % 5), (int) ((ind + 3) % 5), (int) ((ind + 2) % 5)};
		}
		for (int i = 0; i < 5; i++){
			if (combs[i] == 0) combs[i] = 5;
			combs[i] -= 1;
		}
		System.out.println("Arguments: ");
		for (int i = 0; i < 5; i++){
			System.out.print(combs[i] + " ");
		}
		System.out.println();

		// set
		c.setRefDist(arguments.get("dis")[combs[0]]);

		int ne = arguments.get("iter")[combs[1]].intValue();
		c.setGeneration(ne);

		ne = arguments.get("mainP")[combs[2]].intValue();
		c.setGeneration(ne);

		ne = arguments.get("subP")[combs[3]].intValue();
		c.setGeneration(ne);

		ne = arguments.get("converge")[combs[4]].intValue();
		c.setGeneration(ne);

	}

	public static void main(String[] args)
	{
		for(int index = 0; index < 25; index++){
			int[] combs = new int[5];
			int ind = index + 1;
			if (ind <= 5){
				combs = new int[]{(int) Math.ceil((double) ind / 5), (int) (ind % 5), (int) (ind % 5), (int) (ind % 5), (int) (ind % 5)};
			}
			else if (ind <= 10){
				combs = new int[]{(int) Math.ceil((double) ind / 5), (int) (ind % 5), (int) ((ind + 1) % 5), (int) ((ind + 2) % 5), (int) ((ind + 3) % 5)};
			}
			else if (ind <= 15){
				combs = new int[]{(int) Math.ceil((double) ind / 5), (int) (ind % 5), (int) ((ind + 2) % 5), (int) ((ind + 4) % 5), (int) ((ind + 1) % 5)};
			}
			else if (ind <= 20){
				combs = new int[]{(int) Math.ceil((double) ind / 5), (int) (ind % 5), (int) ((ind + 3) % 5), (int) ((ind + 1) % 5), (int) ((ind + 4) % 5)};
			}
			else if (ind <= 25){
				combs = new int[]{(int) Math.ceil((double) ind / 5), (int) (ind % 5), (int) ((ind + 4) % 5), (int) ((ind + 3) % 5), (int) ((ind + 2) % 5)};
			}
			for (int i = 0; i < 5; i++){
				if (combs[i] == 0) combs[i] = 5;
			}
			for (int i = 0; i < 5; i++){
				System.out.print(combs[i] + " ");
			}
			System.out.println();
		}
		System.out.println();
	}
	
	public String getEndereco(String texto)
	{
		try 
		{
			File file=new File(texto);
			if(file.exists()&&!file.isDirectory())
				return texto;
			else
				System.err.println("The -file parameter must contain the address of a valid file.");
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		return "";	
	}
	
	public boolean getRound(String texto)
	{
		try 
		{
			if(texto.equals("false")||texto.equals("true"))
				rounded=Boolean.valueOf(texto);
			else
				System.err.println("The -rounded parameter must have the values false or true.");
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		return rounded;
	}
	
	public double getLimit(String texto)
	{
		try 
		{
			limit=Double.valueOf(texto);
		} 
		catch (java.lang.NumberFormatException e) {
			System.err.println("The -limit parameter must contain a valid real value.");
		}
		return limit;
	}
	
	public double getBest(String texto)
	{
		try 
		{
			best=Double.valueOf(texto);
		} 
		catch (java.lang.NumberFormatException e) {
			System.err.println("The -best parameter must contain a valid real value.");
		}
		return best;
	}
	
	public double getEta(String texto)
	{
		double eta=0.2;
		try 
		{
			if(Double.valueOf(texto)>=0&&Double.valueOf(texto)<=1)
				eta=Double.valueOf(texto);
			else
				throw new java.lang.NumberFormatException();
		} 
		catch (java.lang.NumberFormatException e) {
			System.err.println("The -eta parameter must contain a valid real value in the range [0,1].");
		}
		return eta;
	}
	
	public double getAlpha(String texto)
	{
		double alpha=0.4;
		try 
		{
			if(Double.valueOf(texto)>=0&&Double.valueOf(texto)<=1)
				alpha=Double.valueOf(texto);
			else
				throw new java.lang.NumberFormatException();
		} 
		catch (java.lang.NumberFormatException e) {
			System.err.println("The -alpha parameter must contain a valid real value in the range [0,1].");
		}
		return alpha;
	}
	
	public int getVarphi(String texto)
	{
		int varphi=20;
		try 
		{
			varphi=Integer.valueOf(texto);
		} 
		catch (java.lang.NumberFormatException e) {
			System.err.println("The -varphi parameter must contain a valid integer value.");
		}
		return varphi;
	}
	
	public int getGamma(String texto)
	{
		int gamma=20;
		try 
		{
			gamma=Integer.valueOf(texto);
		} 
		catch (java.lang.NumberFormatException e) {
			System.err.println("The -gamma parameter must contain a valid integer value.");
		}
		return gamma;
	}
	
	public int getDBeta(String texto)
	{
		int dBeta=15;
		try 
		{
			dBeta=Integer.valueOf(texto);
		} 
		catch (java.lang.NumberFormatException e) {
			System.err.println("The -dBeta parameter must contain a valid integer value.");
		}
		return dBeta;
	}
	
	public Variante getVariant(String texo)
	{
		try 
		{
			variant=Variante.valueOf(texo);
		} 
		catch (java.lang.IllegalArgumentException e) 
		{
			System.err.println("The -variant parameter must have the values "+Arrays.toString(Variante.values())+".");
		}
		return variant;
	}
	
	public StoppingCriterion getStoppingCriterion(String texo)
	{
		StoppingCriterion stoppingCriterion=StoppingCriterion.Time;//默认是time
		try 
		{
			stoppingCriterion=StoppingCriterion.valueOf(texo);
		} 
		catch (java.lang.IllegalArgumentException e) 
		{
			System.err.println("The -stoppingCriterion parameter must have the values "+Arrays.toString(StoppingCriterion.values())+".");
		}
		return stoppingCriterion;
	}

	public String getFile() {
		return file;
	}
	//增加了file更改
	public void setFile(String file){
		this.file = file;
	}

	public boolean isRounded() {
		return rounded;
	}

	public Variante getVariant() {
		return variant;
	}

	public void setVariant(String variant) {
		switch (variant){
			case "HVRPD": this.variant = Variante.HVRPD; break;
			case "FSMF": this.variant = Variante.FSMF; break;
			case "FSMFD": this.variant = Variante.FSMFD; break;
			case "FSMD": this.variant = Variante.FSMD; break;
		}
	}

	public double getTimeLimit() {
		return limit;
	}

	public double getBest() {
		return best;
	}


	public Config getConfig() {
		return config;
	}
	
}
