package shadowfax;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
///import java.util.List;

import javax.imageio.ImageIO;

import math.jwave.Transform;
import math.jwave.transforms.FastWaveletTransform;
import math.jwave.transforms.wavelets.Haar02;
/**
 * Class required to perform all processing related to wavelets
 * @author kai
 *
 */
public class WaveletFP {
	
	public static final int NO_OF_ITERATIONS=2; //Not implemented in jwave.jar
	private static final double LOG10 = Math.log(10);
	Transform t;
	double[][] transformed;
	static double threshold = 0;
	
	
	/**
	 * Creates a new object and intialises a new transform object
	 */
	public WaveletFP(){
		this.t = new Transform( new FastWaveletTransform( new Haar02() ), NO_OF_ITERATIONS);
	}
	
	/**
	 * Collapses the audio spectrum data and breaks it into spectral images of
	 * size 32*128 each with an offset of 180. Then, performs a non standard 
	 * Haar wavelet transform on images.
	 * @param spec		The spectral data for the audio sample
	 * @return			Wavelet transform of each spectral image extracted from the audio data
	 * @throws IOException
	 */
	public double[][][] transform(double[][] spec) throws IOException{
		//collapse the spectrum
		this.transformed = collapse(spec);
		//draw the complete original image
		//drawSpec(transformed, "./Specs/Original.png", BufferedImage.TYPE_INT_RGB);
		double[][][] spectrals = new double[((transformed.length)/80)+1][128][32];
		double[][] sub = new double[128][32];
		//run it for all sub images of size 128*32, with offset 80 in between
		for(int i=0;i<transformed.length-128;i+=80){
			//extract the sub image at index i
			sub = subfp(transformed, i);
			//printing the sub files into .png images just to see if it is working
			drawSpec(sub, "./Specs/SubImageFFT/FFTChunk"+(int)(i/80)+".png", BufferedImage.TYPE_INT_RGB);
			//perform forward transform horizontally
			for(int j=0;j<sub.length;j++)
			{
				sub[j] = t.forward(sub[j]);
				
			}
			String firstWavelet="./Specs/firstWaveletTransform/firstWavelet"+i+".png";
			drawSpec(sub, firstWavelet,BufferedImage.TYPE_INT_RGB);
			//transpose to carry out vertical transform
			sub = transpose(sub);
			//perform forward transform vertically (horizontally on transposed data)
			for(int j=0;j<sub.length;j++){
				sub[j] = t.forward(sub[j]);
			}
			//transpose back to get the wavelet image and save it to spectrals
			sub = transpose(sub);
			String secondWavelet="./Specs/secondWaveletTransform/secondWavelet"+i+".png";
			drawSpec(sub, secondWavelet,BufferedImage.TYPE_INT_RGB);
			spectrals[i/80] = sub; 
		}
		return spectrals;
	}
	
	/**
	 * Performs a reverse transform on array of Wavelet images
	 * @param spec	The array of wavelet images
	 * @return		Array of the reconstructed images
	 */
	public double[][][] reversetransform(double[][][] spec){
		//See transform(double[][] spec) for explanation
		double[][][] spectrals = new double[spec.length][128][32];
		for(int i=0;i<spec.length;i++){
			double[][] sub = new double[128][32];
			System.arraycopy(spec[i], 0, sub, 0, spec[i].length);
			sub = transpose(sub); 
			for(int j=0;j<sub.length;j++){
				sub[j] = t.reverse(sub[j]);
			}
			sub = transpose(sub);
			for(int j=0;j<sub.length;j++){
				sub[j] = t.reverse(sub[j]);
			}
			spectrals[i] = sub; 
		}
		return spectrals;
	}
	
	/**
	 * Performs a matrix transpose
	 * @param a 	original 2D matrix of size MxN
	 * @return		Transposed matrix of size NxM
	 */
	public static double[][] transpose(double[][] a){
		int r = a.length;
		int c = a[0].length;
		double [][] t = new double[c][r];
		for(int i = 0; i < r; ++i) {
			for(int j = 0; j < c; ++j) {
				t[j][i] = a[i][j];
			}
		}
		return t;
	}
	
	/**
	 * Get the sub-2D-array beginning at a certain offset in a large 2D array
	 * @param spec		The original array
	 * @param offset	Offset for the returned array
	 * @return			The subarray of size 128*32 beginning at the given offset in the original array
	 */
	private double[][] subfp(double[][] spec, int offset){
		double[][] split = new double[128][32];
		System.arraycopy(spec, offset, split, 0, split.length);
		return split;
	}
	
	/**
	 * Collapse the energyband into 32 bands
	 * @param spec
	 * @return
	 */
	private double[][] collapse(double[][] spec){
		double[][] collapsed = new double[spec.length][32];
		for(int i=0;i<spec.length;i++){
			collapsed[i] = collapseLog(spec[i]);
		}
		return collapsed;
	}
	
	/**
	 * Collapse a 1024 bin energy band into 32 logarithmically spaced bins
	 * with frequency bins ranging from 100Hz to 3kHz.  
	 * @param spec
	 * @return
	 */
	private double[] collapseLog(double[] spec){
		
		int intoff = 25; //Frequency bin at which 100Hz is located
		double freq = 100, ofreq = 100, diff = 0; //Initial frequency
		double[] tempSpec = new double[32];
		for(int i=0;i<32;i++){
			freq *= 1.11214148025; /*3kHz cutoff. Use 1.10582301703024 for cutoff 2.5kHz*/
			//find the difference between the next and current frequency
			diff = freq - ofreq;
			//find the number of bins between the next and current frequency
			int count = (int)(diff/3.90625);
			//add all bins that lie between the next and current frequency
			for(int j=0;j<count; j++){
				tempSpec[i]+=spec[intoff+j];
			}
			intoff+=count;
			ofreq = freq;
			//convert to log scale
			if (tempSpec[i] <= 1e-2) tempSpec[i] = -20.;
            else tempSpec[i] = 10 * (Math.log(tempSpec[i])/LOG10);
		}
		return tempSpec;
	}
	
	/**
	 * Flattens a 3D boolean array to a 1D boolean array
	 * @param br  The boolean array to be flattened
	 * @return	  The flattened array
	 */
	public static boolean[] flatten(boolean[][][] br){
		System.err.println("br.length="+br.length+" br[0].length="+br[0].length+" br[0][0].length="+br[0][0].length);
		boolean[] fbr = new boolean[br.length*br[0].length*br[0][0].length];
		for(int i=0;i<br.length;i++){
			for(int j=0;j<br[0].length;j++){
//				//TODO : i*2j+1 and i*2j - fix this up
//				fbr[i*j + j] = br[i][j][0];
//				fbr[i*j + j+1] = br[i][j][1];
				fbr[ 2*( (32*i)+j ) ]=br[i][j][0];
				fbr[ 2*( (32*i)+j ) + 1 ]=br[i][j][1];
				
			}
		}
		return fbr;
	}
	
	public static boolean[][][] inflate(boolean[] oneD)
	{
		boolean[][][]threeD=new boolean[128][32][2];
		for(int x=0;x<oneD.length;x+=2)
		{
			threeD[(int)x/64][(int)(x/2)%32][0]=oneD[x];
			threeD[(int)x/64][(int)(x/2)%32][1]=oneD[x+1];
		}
		
		return threeD;
		
	}
	
	/**
	 * Draw an image from double data using a jet ColorMap
	 * @param regen		The array to be converted to an image
	 * @param iFile		Filename for the image
	 * @param type		Type of Image
	 * @throws IOException
	 */
	public static void drawSpec(double[][] regen, String iFile,int type) throws IOException{
		//This drawSpec is for the 2D array
		
		double min = Double.MAX_VALUE;
    	double max = Double.MIN_NORMAL;
    	//find the largest element and the smallest element greater that -70
    	for(int i=0; i<regen.length;i++){
    		for(int j=0;j<regen[0].length; j++){
    			if(regen[i][j]<min&& regen[i][j]>-70) min = regen[i][j];
    			if(regen[i][j]>max) max = regen[i][j];
    		}
    	}
    	
    	double range = max-min+1;
    	//System.out.println("Max:"+max+" Min:"+min+" Range"+range);
    	//Initialize the color map
    	ColorMap.getJet((int)range);
    	
    	//color map for grayscale image
    	if(type == BufferedImage.TYPE_BYTE_GRAY){
    		ColorMap.getJet(256);
    	}
    	BufferedImage img = new BufferedImage(regen.length, regen[0].length, type);
    	
    	//Set the pixel RGB values for each point
		for(int x = 0; x < regen.length; x++){
		    for(int y = 0; y<regen[x].length; y++){
		    	if(regen[x][y]<-70){
		    		img.setRGB(x, regen[x].length-y-1, ColorMap.getColor(0));
		    	}
		    	else{
			    	if(type == BufferedImage.TYPE_BYTE_GRAY){
			    		img.setRGB(x, regen[x].length-y-1, ColorMap.getColor((int)(256*(regen[x][y]-min))/(int)range));
			    	}
			    	else
			        img.setRGB(x, regen[x].length-y-1, ColorMap.getColor((int)((regen[x][y]-min))));
		    	}
		    }
		}
		//write to file
		File imageFile = new File(iFile);
		ImageIO.write(img, "png", imageFile);
    	    	
    }
	
	/**
	 * Draw an image from boolean data using a jet ColorMap
	 * @param regen		The array to be converted to an image
	 * @param iFile		Filename for the image
	 * @param type		Type of Image
	 * @throws IOException
	 */
	public static void drawSpec(boolean[][][] regen, String iFile) throws IOException{
		//This drawSpec is for a 3D array
		int type = BufferedImage.TYPE_INT_RGB;
		ColorMap.getJet(256);
		
    	BufferedImage img = new BufferedImage(regen.length, regen[0].length, type);
    	
    	//Set the pixel RGB values for each point
		for(int x = 0; x < regen.length; x++){
		    for(int y = 0; y<regen[x].length; y++){
		    	//if part of top 200 wavelets - either positive or negative
		    	if(regen[x][y][0] == true||regen[x][y][1] == true){
		    		img.setRGB(x, regen[x].length-y-1, ColorMap.getColor(255));
		    	}
		    	//else if not among top 200
		    	else{
		    		img.setRGB(x, regen[x].length-y-1, ColorMap.getColor(0));
		    	}
		    }
		}
		//write to file
		File imageFile = new File(iFile);
		ImageIO.write(img, "png", imageFile);
    	    	
    }
	
//	public int[] returnChunkOneIndices()
//	{
//		int[] oneIndicesArray=new int[oneIndices.size()];
//		 for (int i = 0; i < oneIndices.size(); i++) {
//	        	oneIndicesArray[i] = (int)oneIndices.get(i);
//	        	//for (int j =0;j<spec[i].length;j++) System.out.println(spec[i][j]);
//	        }
//		 oneIndices=null;
//		return oneIndicesArray;
//		
//	}
	
	/**
	 * Find the indices of the top 200 wavelets, according to magnitude.
	 * The indices amongst top 200 with positive values are set to (1,0).
	 * The indices amongst top 200 with negative values are set to (0,1).
	 * @param wavelets	Array of Wavelets
	 * @return	Boolean array set to either (1,0) or (0,1) for top 200 wavelets, (0,0) otherwise
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
//	public static boolean[][][] getTop(double[][] wavelets){
//	public static int[] getTop(double[][] wavelets){
		public static int[] getTop(double[][] wavelets){
		
		int[] oneIndicesArray=new int[200];
		
		double[][] top200withmag=new double[wavelets.length][wavelets[0].length];
		for(int i=0;i<top200withmag.length;i++)
		{
			Arrays.fill(top200withmag[i], 0.0);
		}
		
		boolean[][][] top = new boolean[wavelets.length][wavelets[0].length][2];
		
		//Arrays.fill(top, false);
		
		ArrayList<WaveletPair> toplist = new ArrayList<WaveletPair>();
		//add all elements that are above threshold to toplist
		for(int i=0; i<wavelets.length;i++){
			for(int j=0; j<wavelets[0].length; j++){
				if(threshold<Math.abs(wavelets[i][j])) toplist.add(new WaveletPair(wavelets[i][j],i,j)); 
			}
		}
		
		//if more than 200 elements qualify, sort them
		if(toplist.size()>200){
			Collections.sort(toplist, new Comparator() {
		        public int compare(Object o1, Object o2) {
		               return ((Double) Math.abs(((WaveletPair) (o2)).mag))
		              .compareTo(Math.abs(((WaveletPair) (o1)).mag));
		          }
		     });
			//for top 200 elements set corresponding indices in top[][][] to appropriate values
			for(int i=0;i<200;i++){
				
				WaveletPair t = toplist.get(i);
				if(t.mag>0){
					top[t.i][t.j][0] = true;
					top[t.i][t.j][1] = false;
					oneIndicesArray[i]=(2*((32*(t.i)) + t.j ));
					top200withmag[t.i][t.j]=t.mag;
//					System.out.print("("+t.i+","+t.j+") ");
					
				}
				else{
					top[t.i][t.j][0] = false;
					top[t.i][t.j][1] = true;
					//Integer x=((2*(32*(t.i)) + t.j) + 1);
					oneIndicesArray[i]=(2*((32*(t.i)) + t.j )+1);
					top200withmag[t.i][t.j]=t.mag;
//					System.out.print("("+t.i+","+t.j+") ");
					//oneIndices.add( x);
				}
					
			}
			//update the threshold for the next run
			threshold = toplist.get(Math.min(300,toplist.size()-1)).mag;
			//threshold=0;
//			System.out.println();
			
			return oneIndicesArray;
			//return top;
			
		}
		//if less than 200 elements qualify, decrease the threshold and try again
		else{
			//sort !!!!
			threshold = threshold - 50;
			return getTop(wavelets);
		}
	}

}
