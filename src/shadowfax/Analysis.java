package shadowfax;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public class Analysis {

    /*Lmark_pairs[] unique_landmark_pairs;
    LMarks lm;
    maxes[] landmarks;
    EnergyBandFingerprint EFP = null;
    protected ArrayList<maxes> lmlist = new ArrayList<maxes>();
    protected ArrayList<Lmark_pairs> ulmlist = new ArrayList<Lmark_pairs>();*/
    protected double deltaF = 0.0D;
    protected double[] signal;
    protected boolean running;
    ByteArrayOutputStream out;
    protected List<double[]> spectra;
    protected int spectra_indexmax = 0;
    public double[][] spec;
    final int nfft;
    final int shift;
    static int f=1;
    static int t = 1;
    static double reSignal2[] = new double[2048];
    static double preEmphasis[] = new double[2048];
    int prevSpectraCnt = 0;
    int songID;    
    Config config;
	static long offset = 0;
	static long send_offset = 0;
	FastFourierTransform2 fft = null;
	Window wnd=null;
	
//	int onlyonce=1;
//	int count=0;
    
	/**
	 * Creates an Analysis object that is used to process audio
	 * double data and create Landmarks from it. The Config
	 * specifies the parameters used for processing the audio signal.
	 * 
	 * 
	 * @param my_config 		the Config object specifying parameters for Analysis
	 * @see Config
	 */
    public Analysis(Config my_config) throws Exception 
    {
     
        this.nfft=my_config.nfft;
        this.shift=my_config.shift;
        this.config=my_config;
        
        
        this.fft = new FastFourierTransform2(config.nfft);
        this.spectra = new ArrayList<double[]>();
        //this.EFP = new EnergyBandFingerprint(32);
        try{
            //Initialise the window function
            	wnd = new Window(config.nfft, Window.Function.HAMMING);
            }catch(Exception e){
            	e.printStackTrace();
            }
        for(int i=0;i<config.nfft;i++){
        	preEmphasis[i] = 6.0D / Math.log(2.0D) * Math.log((i + 1) * this.deltaF / 1000.0D);
        }
                
    }
    
    /**
     * Processes the double data for 8192 samples.
     * <p>
     * There is overlap between the chunks equal to <b>shift</b> in Config. If the size of
     * the double array is less than a multiple of 8192, the rest if padded with 0s.
     * 
     * @param Signal 		the double array of the amplitudes of audio signal to be processed
     * @param disc 			(deprecated)parameter specifying whether space-clustered landmarks should be created
     */
    public void index8k(double[] Signal, boolean disc) throws IOException, SQLException, InterruptedException{
    	double[] pSignal = new double[8192];
    	Arrays.fill(pSignal, 0);
    	System.arraycopy(Signal, 0, pSignal, 0, Math.min(8192, Signal.length));
    	indexSpecial2(pSignal, disc);
    }
    
    /**
     * Processes the entire double data provided in <b>Signal</b> in one go.
     * 
     * There is overlap between the chunks equal to <b>shift</b> in Config. 
     * If the size of the double array is less than a multiple of 8192,
     * the rest if padded with 0s.
     * 
     * @param Signal 		the double array of the amplitudes of audio signal to be processed
     * @param disc 			(deprecated) parameter specifying whether space-clustered landmarks should be created
     * @see Config
     */
    @SuppressWarnings("static-access")
	public void indexAll(double[] Signal,boolean disc) throws IOException, SQLException, InterruptedException{
    	this.t = 1;
    	this.f = 1;
    	int pad = (this.shift - (Signal.length - this.nfft)%this.shift);
    	if((Signal.length-this.nfft)%this.shift==0) pad = 0;
    	double[] pSignal = new double[Signal.length + pad];
    	Arrays.fill(pSignal, 0);
    	System.arraycopy(Signal, 0, pSignal, 0, Signal.length);
    	indexSpecial2(pSignal, disc);
    }
    
    /**
     * Processes the data in <b>reSignal</b> and stores <b>shift</b> amount of samples internally which are used
     * to overlap it with the new <b>reSignal</b> array when it is called again. Should not be called directly.
     * 
     * @param reSignal 		the double array of the amplitudes of audio signal to be processed
     * @param disc 			(deprecated) parameter specifying whether space-clustered landmarks should be created
     * @see Config
     */
    public void indexSpecial2( double[] reSignal, boolean disc) throws IOException, SQLException, InterruptedException
    {
    	//TODO:Fix the overlap for 64 shift
    	if(f==1){
    		System.arraycopy(reSignal, 8192-64, reSignal2, 0, 64);
    		offset += processData(reSignal, 8000,(int)offset, disc);  
    		f=0;
    		
    	}
    	
    	else {
    		if(t==1){
    			double[] reSignal3=new double[8192];
    			System.arraycopy(reSignal2, 0, reSignal3, 0, 64);
    			System.arraycopy(reSignal, 0, reSignal3, 64, 8192-64);
    			System.arraycopy(reSignal3, 8192-64, reSignal2, 0, 64);
    			System.arraycopy(reSignal, 8192-64, reSignal2, 64, 64);
    			
    			offset += processData(reSignal3, 8000,(int)offset, disc);  
    			t=0;
    		}
    		else
    		{
    			double [] reSignal3=new double[8192+2048];
    			System.arraycopy(reSignal2, 0, reSignal3, 0, 2048);
    			System.arraycopy(reSignal, 0, reSignal3, 2048, 8192);
    			System.arraycopy(reSignal3, 8192+64, reSignal2, 0, 64);
    			
    			offset += processData(reSignal3, 8000,(int)offset, disc);
    			t=1;
    		}
    		
    		
    	}   	
    }	
    	
   
    /**
     * Normalises the <b>reSignal</b> array w.r.t the maximum amplitude. Then,
     * computes the spectrum and save it in spec.
     * 
     * @param reSignal 			the double array of the amplitudes of audio signal to be processed
     * @param sampleRate 		sampling rate of the audio sample
     * @param send_offset 		time offset to be used during the landmark creation process
     * @param disc 				(deprecated) parameter specifying whether space-clustered landmarks should be created
     * @return time 			offset of the spectrum created in current run
     */
    public int processData(double[] reSignal, int sampleRate, int send_offset, boolean disc) throws IOException {
        int offset = 0;
        double absMax = CommonUtils.getAbsMax(reSignal);
        for (int i = 0; i < reSignal.length; i++) {
            reSignal[i] /= absMax;
        }
        getSpectra(reSignal, wnd, config.nfft, config.shift, sampleRate);
        offset = this.spectra.size();
        spec = new double[offset][nfft / 2];
        for (int i = 0; i < this.spectra.size(); i++) {
        	spec[i] = this.spectra.get(i);
        	//for (int j =0;j<spec[i].length;j++) System.out.println(spec[i][j]);
        }
        
        return offset;
    }
    
    /**
     * Generates the Spectra of the audio data in <b>signal</b> using Fast Fourier Transform.
     *  
     * @param signal 		the audio signal to be processed
     * @param window 		windowing to be applied to the signal
     * @param fftSize 		size of the Fast Fourier Transform
     * @param windowShift 	amount of shift during each iteration
     * @param samplingRate 	sampling rate of the audio signal
     */
    public void getSpectra(double[] signal, Window window, int fftSize, int windowShift, int samplingRate) {
    	
    	this.signal = signal;
        this.deltaF = samplingRate/fftSize;
        int niter = (signal.length-fftSize)/windowShift;
        double[] sample = new double[fftSize];
        int offset=0;
        for(int i=0; i<niter; i++){
        	System.arraycopy(signal, offset, sample, 0, config.nfft);
            window.transform(sample);
            double[] spectrum = new double[fftSize/2];
            spectrum = fft.fftMag(sample);
            //if(onlyonce==1){for(int s=0;s<spectrum.length;s++){System.out.println(spectrum[s]);onlyonce=0;count++;}}
            //System.out.println("Total number of fft values = "+count);
            this.spectra.add(spectrum);
        	offset+=windowShift;
        	//for(int j=0; j<spectra.get(i).length;j++) System.out.println(spectra.get(i)[j]);
        }
        
        
	}
    
    /*public ArrayList<maxes> getLandmarks(){
    	return this.lmlist;
    }
    
    public ArrayList<Lmark_pairs> getUniqueLandmarks(){
    	return this.ulmlist;
    }
    
    public ArrayList<Integer> getEFP(){
    	return EFP.getFprint();
    }
    
    public EnergyBandFingerprint getEFPObject(){
    	return EFP;
    }*/
    
    /**
     * Trims the audio signal to remove leading silences
     * @param sig 	audio signal to be trimmed
     * @return trimmed audio signals
     */
    double[] CropSignal(double[] sig) {
        int i;
        for (i = 1; i < sig.length; i++) 
        {
            if (sig[i] > 0) 
            {
                break;
            }
        }
        return Arrays.copyOfRange(sig, i, sig.length);
    }
    
    
}
