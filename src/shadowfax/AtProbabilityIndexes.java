package shadowfax;

import java.util.Arrays;

public class AtProbabilityIndexes {
	
	
	public AtProbabilityIndexes(int[] inputArray)
	{
		returnLogicalIndexes(inputArray);
	}

	public static int[] returnLogicalIndexes(int[] inputArrayInsideClass) 
	{
		// TODO Auto-generated method stub
		boolean foundOne=false; 
		
		boolean firstTime=true;int firstValue=0;
		
		int[] firstOneIndexes=new int[40];
		boolean[] oneFound=new boolean[40];
		Arrays.fill(oneFound, false);
		
		int count=0,startingIndex=34;
		
		Arrays.sort(inputArrayInsideClass);
		
		for(int i=0;i<inputArrayInsideClass.length;i++)
		{
			int currentChunk=(int)inputArrayInsideClass[i]/205;
			//if(inputArrayInsideClass[i]>startingIndex && inputArrayInsideClass[i]<(startingIndex+203))
			if(currentChunk==count && inputArrayInsideClass[i]>34)
			{
				if(oneFound[currentChunk]==false)
				{
					if(firstTime==true){firstValue=inputArrayInsideClass[i];firstTime=false;}
					firstOneIndexes[count]=inputArrayInsideClass[i];
					oneFound[count]=true;
					count++;
				}
			}
			else if(currentChunk>count  && inputArrayInsideClass[i]>34)
			{
				if(oneFound[count]==false)
				{
					if(firstTime==true){firstValue=inputArrayInsideClass[i];firstTime=false;}
					firstOneIndexes[count]=inputArrayInsideClass[i];
					oneFound[count]=true;
					count++;
				}
			}
			
			for(int j=0;j<firstOneIndexes.length;j++)
			{
				if(oneFound[j]==false){firstOneIndexes[j]=firstValue;}
			}
		}
		
/*		for(int i=0;i<inputArrayInsideClass.length;i++)
		{
			if(inputArrayInsideClass[i]>index && inputArrayInsideClass[i]<(index+203) && foundOne==false)
			{
				firstOneIndexes[count]=inputArrayInsideClass[i];
				foundOne=true;
				count++;
			}
			else if (inputArrayInsideClass[i]>(index+203)   &&  foundOne==true  )
			{
				index+=203;i--;foundOne=false;
			}
			else if (inputArrayInsideClass[i]>(index+203) && foundOne==false)
			{
				index=index-50;
				i--;
			}
			
			if(count==40) {break;}
			
		}
*/		
		
		
//		for(int i=71;i<inputArrayInsideClass.length;i+=203)
//			/*beginning index is 71 (ignoring all the previous indexes) and finding 40 indexes, i.e. in steps
//			 * of (8192-72)/40 = 203, but in java the first index is 0 thus, we begin considering from 71 instead of 72 
//			 * and go up till 71 + (40*203) = 8191 (last index of the boolean array). Thus we have chunks of 203 numbers from which
//			 * we find the occurence of the first 1
//			 */
//		{
//			for(int j=i;j<(i+203);j++)
//			{
//				if(inputArrayInsideClass[j]==true) { 
//			}
//		}
		
		return firstOneIndexes;
	}
	


}
