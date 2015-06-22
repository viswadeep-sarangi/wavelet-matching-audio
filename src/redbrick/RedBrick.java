/*
 * Krishna Kant Sijariya
 * IIT, Kharagpur
 * krishnakant.siz@gmail.com 
 */
package redbrick;
/*************************************************************************
 *  Limitations
 *  
 *    - Assumes the audio is 48KHz or 44.1kHz 16 bit single channel wav
 *
 *************************************************************************/

import java.applet.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;

import shadowfax.*;



/**
 *  This class provides a basic capability for
 *  creating and reading audio. 
 * 
 *  The audio format uses 16-bit, mono files.
 */
public final class RedBrick {

    public static final int SAMPLE_RATE = 8000;

    private static final int BYTES_PER_SAMPLE = 2;                // 16-bit audio
    private static final int BITS_PER_SAMPLE = 16;                // 16-bit audio
    private static final double MAX_16_BIT = Short.MAX_VALUE;// 32,767
    private static final int SAMPLE_BUFFER_SIZE = 4096; 
    public static final double BIN_SIZE = 86.1328125;
    public static final int SAMPLE_SIZE_11MS= 512;
    static PrintWriter writer=null;
    static PrintWriter writer2=null;
    static PrintWriter writer3=null;
    static PrintWriter writer4=null;
    
    static int onlyonce=1;
    static ArrayList<int[]> oneHashes=new ArrayList<int[]>();
    static ArrayList<int[]> firstOneIndexes=new ArrayList<int[]>();

    private static SourceDataLine line;   // to play the sound
    private static byte[] buffer;         // our internal buffer
    private static int bufferSize = 0;    // number of samples currently in internal buffer
    
    WaveletFP waveletfpobject=new WaveletFP();

    
//   static File file = new File("./output.txt");static FileWriter fw;static PrintWriter pw;

    // do not instantiate
    private RedBrick() {
//    {
//		try {
//			 
//			// if file doesnt exists, then create it
//			if (!file.exists()) {
//				file.createNewFile();
//			}
// 
////			fw = new FileWriter(file);
////			pw=new PrintWriter(fw);
////			BufferedWriter bw = new BufferedWriter(fw);
////			bw.write(content);
////			bw.close();
//// 
////			System.out.println("Done");
//// 
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
    }

   
    // static initializer
    static { init(); }

    // open up an audio stream
    private static void init() {
        try {
            // 44,100 samples per second, 16-bit audio, mono, signed PCM, little Endian
            AudioFormat format = new AudioFormat((float) SAMPLE_RATE, BITS_PER_SAMPLE, 1, true, false);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

            line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format, SAMPLE_BUFFER_SIZE * BYTES_PER_SAMPLE);
            buffer = new byte[SAMPLE_BUFFER_SIZE * BYTES_PER_SAMPLE/3];
            System.err.println("The frame rate is : "+format.getFrameRate());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }

        // no sound gets made before this call
        line.start();
    }


    /**
     * Close standard audio.
     */
    public static void close() {
        line.drain();
        line.stop();
    }
    
    /**
     * Write one sample (between -1.0 and +1.0) to standard audio. If the sample
     * is outside the range, it will be clipped.
     */
    public static void play(double in) {

        // clip if outside [-1, +1]
        if (in < -1.0) in = -1.0;
        if (in > +1.0) in = +1.0;

        // convert to bytes
        short s = (short) (MAX_16_BIT * in);
        buffer[bufferSize++] = (byte) s;
        buffer[bufferSize++] = (byte) (s >> 8);   // little Endian

        // send to sound card if buffer is full        
        if (bufferSize >= buffer.length) {
            line.write(buffer, 0, buffer.length);
            bufferSize = 0;
        }
    }

    /**
     * Write an array of samples (between -1.0 and +1.0) to standard audio. If a sample
     * is outside the range, it will be clipped.
     */
    public static void play(double[] input) {
        for (int i = 0; i < input.length; i++) {
            play(input[i]);
        }
    }

    /**
     * Read audio samples from a file (in .wav format) and return them as a double array
     * with values between -1.0 and +1.0.
     */
    public static double[] read(String filename) {
        byte[] data = readByte(filename);
        int N = data.length;
        double[] d = new double[N/2];
        for (int i = 0; i < N/2; i++) {
            d[i] = ((short) (((data[2*i+1] & 0xFF) << 8) + (data[2*i] & 0xFF))) / ((double) MAX_16_BIT);
        }
        return d;
    }




    /**
     * Play a sound file (in .wav format) in a background thread.
     */
    public static void play(String filename) {
        URL url = null;
        try {
            File file = new File(filename);
            if (file.canRead()) url = file.toURI().toURL();
        }
        catch (MalformedURLException e) { e.printStackTrace(); }
        // URL url = RedBrick.class.getResource(filename);
        if (url == null) throw new RuntimeException("audio " + filename + " not found");
        AudioClip clip = Applet.newAudioClip(url);
        clip.play();
    }


    // return data as a byte array
    private static byte[] readByte(String filename) {
        byte[] data = null;
        AudioInputStream ais = null;
        try {

            // try to read from file
            File file = new File(filename);
            if (file.exists()) {
                ais = AudioSystem.getAudioInputStream(file);
                data = new byte[ais.available()];
                ais.read(data);
            }

            // try to read from URL
            else {
                URL url = RedBrick.class.getResource(filename);
                ais = AudioSystem.getAudioInputStream(url);
                data = new byte[ais.available()];
                ais.read(data);
            }
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("Could not read " + filename);
        }

        return data;
    }




    @SuppressWarnings("unused")
	private static double[] note(double hz, double duration, double amplitude) {
        int N = (int) (RedBrick.SAMPLE_RATE * duration);
        double[] a = new double[N+1];
        for (int i = 0; i <= N; i++)
            a[i] = amplitude * Math.sin(2 * Math.PI * i * hz / RedBrick.SAMPLE_RATE);
        return a;
    }
    
     
    //@SuppressWarnings("unused")
	private static void drawSpec(double[][] regen, String iFile,int type) throws IOException{

		double min = Double.MAX_VALUE;
    	double max = Double.MIN_NORMAL;
    	for(int i=0; i<regen.length;i++){
    		for(int j=0;j<regen[0].length; j++){
    			if(regen[i][j]<min) min = regen[i][j];
    			if(regen[i][j]>max) max = regen[i][j];
    		}
    	}
    	
    	double range = max-min+1;
    	//System.out.println("Max:"+max+" Min:"+min+" Range"+range);
    	for(int i=0; i<regen.length;i++){
    		for(int j=0;j<regen[0].length; j++){
    			regen[i][j] -=min; 
    		}
    	}
    	
    	ColorMap.getJet((int)range);
    	if(type == BufferedImage.TYPE_BYTE_GRAY){
    		ColorMap.getJet(256);
    	}
    	BufferedImage img = new BufferedImage(regen.length, regen[0].length, type);  
		for(int x = 0; x < regen.length; x++){
		    for(int y = 0; y<regen[x].length; y++){
		    	//img.setRGB(x, y, 0);
		    	if(type == BufferedImage.TYPE_BYTE_GRAY){
		    		img.setRGB(x, regen[x].length-y-1, ColorMap.getColor((int)(256*(regen[x][y]-min))/(int)range));//ColorMap.getJet(256);
		    	}
		    	else
		        img.setRGB(x, regen[x].length-y-1, ColorMap.getColor((int)((regen[x][y]-min))));
		    }
		}
		File imageFile = new File(iFile);
		ImageIO.write(img, "png", imageFile);
    	    	
    }
    
    public static ArrayList<Integer> getOneBitErrorFP(ArrayList<Integer> OEFP){
		
		ArrayList<Integer> EFP = new ArrayList<Integer>();
		for(int i:OEFP){
			for(int j=0;j<32;j++){
				int q = i^(1<<j);
				EFP.add(q);
			}
		}
		return EFP;
	}
    
    public static void printArray(double[][] p){
    	for(int i=0;i<p.length;i++){
    		for(int j=0;j<p[i].length;j++)
    			System.out.print(p[i][j]+"\t");
    		System.out.print("\n");
    	}
    }

    public static void main(String[] args) throws Exception {
    	
        double data[] = RedBrick.read(args[0]);
        //System.out.println(args[0]);
        Config config;
		config = new Config("config.properties");
		Analysis an = new Analysis(config);
		System.err.println("Analysis Started..");
		an.indexAll(data, true);
		System.err.println("Analysis Complete");
		
		//WaveletFP.drawSpec(data, "./OriginalUntouched.png", BufferedImage.TYPE_INT_RGB);
		
		double[][] regen = an.spec;
		for (int i = 0; i < regen.length; i++) {
        	for (int j =0;j<regen[i].length;j++) {
        		if(regen[i][j]<-200){System.err.println("i:"+i+" j:"+j+" "+regen[i][j]);System.err.println("According to the energy values, it's not supposed to be here, will have to figure this out");}
        	}
        }
		
		/*double min = Double.MAX_VALUE;
    	double max = Double.MIN_NORMAL;
    	for(int i=0; i<regen.length;i++){
    		for(int j=0;j<regen[0].length; j++){
    			if(regen[i][j]<min) min = regen[i][j];
    			if(regen[i][j]>max) max = regen[i][j];
    		}
    	}
    	
    	double range = max-min+1;
    	System.out.println("Max:"+max+" Min:"+min+" Range"+range);
    	
    	for(int i=0; i<regen.length;i++){
    		for(int j=0;j<regen[0].length; j++){
    			regen[i][j] -=min; 
    		}
    	}*/
    	//Transform t = new Transform( new FastWaveletTransform( new Haar02() ),2 );
		//double[][] transformed = new double[128][32];/*regen;*//*new double[1024][regen[0].length];*/
		WaveletFP w = new WaveletFP();
		//drawSpec(regen, "Original Whole FFT.png", BufferedImage.TYPE_INT_RGB);
		
		double[][][] specs  = w.transform(regen);
		double[][][] top200=new double[specs.length][128][32];
		
		for(int i=0; i<specs.length;i++){
			//transformed = specs[i];
			String fname = "./Specs/Wavelets/Wavelets"+i+".png";
			//WaveletFP.drawSpec(transformed, fname, BufferedImage.TYPE_BYTE_GRAY);
			WaveletFP.drawSpec(specs[i], fname, BufferedImage.TYPE_INT_RGB);
			//int[] oneIndices1 = WaveletFP.getTop(specs[i]);
			//double[][] top200wmag=WaveletFP.getTop(specs[i]);
			//boolean[][][] topw=WaveletFP.getTop(specs[i]);
			
			oneHashes.add(WaveletFP.getTop(specs[i]));
			
			firstOneIndexes.add(AtProbabilityIndexes.returnLogicalIndexes(WaveletFP.getTop(specs[i])));
			
			
			//top200[i]=top200wmag;
			//oneHashes.add(oneIndices1);
			//boolean [] ffp = WaveletFP.flatten(topw);
//			boolean[][][] inflated=WaveletFP.inflate(ffp);
//			oneHashes.add( (new WaveletFP()).returnChunkOneIndices());
//			System.out.println("The length of the ffp array is "+ffp.length);
//			System.out.println(ffp.length);
			
			//int[] logicalHashes=AtProbabilityIndexes.
			
//			String topWaveletsPath = "./Specs/topWavelets"+i+".png";
//			String top200wmagn="./Specs/Top200withMagnitude/top200withMag"+i+".png";
//			String top200wmagnbw="./Specs/Top200withMagnitudeBW/top200withMagBW"+i+".png";
////			String inflatedName="./Specs/InflatedBack"+i+".png";
//			WaveletFP.drawSpec(top200wmag, top200wmagn,BufferedImage.TYPE_INT_RGB);
//			WaveletFP.drawSpec(top200wmag, top200wmagnbw,BufferedImage.TYPE_BYTE_GRAY);
//			WaveletFP.drawSpec(inflated, inflatedName);
//			
			
		}
		//System.out.println();
		//System.out.println();
		
/*		for (int j = 0; j < oneHashes.size(); j++) {
			int[] singleArray=oneHashes.get(j);
			

			
			for(int k=0;k<singleArray.length;k++)
        	{
        		
        		System.out.print("  "+singleArray[k]);
        		
        	}
        	System.out.println();
			//oneIndicesArray[i] = oneIndices.get(i);
        	//for (int j =0;j<spec[i].length;j++) System.out.println(spec[i][j]);
        }
*/		
		
		for(int j=0;j<firstOneIndexes.size();j++)
		{
			int[] singleArray=firstOneIndexes.get(j);
			
			for(int k=0;k<singleArray.length;k++)
			{
				System.out.print(singleArray[k]+" ");
			}
			System.out.println();
		}
		
		
//		System.err.println("J is "+j);
//		System.err.println("K is "+k);
		double[][][] specs2  = w.reversetransform(top200);
		
		for(int i=0; i<specs2.length;i++){
			//transformed = specs2[i];
			String fname3 = "./Specs/ReverseWaveletsFromTop200/ReverseWavelets"+i+".png";
			WaveletFP.drawSpec(specs2[i], fname3, BufferedImage.TYPE_INT_RGB);
		}
		System.err.println("Done");
		
		//System.out.println("************************************************************************");
		
		System.exit(0);
    }
}
