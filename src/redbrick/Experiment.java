package redbrick;

import java.util.Random;

public class Experiment {
	int nextInt;
	int prob2=0;
	
	public void Runnit()
	{
	
		Random r=new Random(11);
		for(int i=0;i<35;i++)
		{
			nextInt=(int)r.nextInt(11);
			System.out.println(nextInt);
			if(nextInt==2)prob2++;
		}
		int bitmask=0x000F;
		System.out.println("probablity of 2 occuring : "+(double)prob2/35);
		System.out.println(bitmask & 63);
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Experiment expt=new Experiment();
		expt.Runnit();
		
	}

}
