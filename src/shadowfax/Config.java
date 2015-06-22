package shadowfax;
import java.io.FileInputStream;
import java.util.Properties;
public class Config {
	Properties prop;
	//To get data from perf or not
	public boolean debug;
	/* Settings controlling overall search space
	 * This should ideally be auto determined from the database size
	 * Should not be a configuration
	 */
	//Smallest index of table from database to search from 
	public int lb;	
	//Largest index of table from database to search from
	public int ub;
	
	
	/* Settings related to FFT analysis
	 * 1. Sampling rate: The sampling rate of the audio input
	 *			Wave files of lower sampling rate will not work?
	 *			Wave files of higher sampling rate will be downsampled to this rate
	 * 2. Window size for fft: The no of sample points to take for FFT
	 * 			Must be a power of 2
	 * 3. Window shift: The number of samples points to shift a subsequent window
	 * 			Should be less than the window size
	 * 4. Window function: The name of the window function to use
	 * 			Specified as a string parameter 
	 * 			Options: -
	 * 				Hamming 			
	 */
	public int nfft;
	public int shift;
	public int samplingRate;
	
	/* Settings related to maxima selection
	 * 1. Method to use: Maxima selection method to use
	 * 			Specified as a string
	 * 			Options: -
	 * 				Original
	 * 				SpacedCluster
	 * 2. Create discriminators: boolean value selecting whether or not to build discriminators
	 * 3. fanout: fan out factor for pairing
	 * 4. max_time_diff: How far to go in time to create a pair
	 * 5. min_freq_diff: Do not pair with frequencies closer than this limit 
	 * 
	 */
	//The fan out factor for pairing of maximas
	public int fanoutFactor;
	public int max_time_diff;
	public int min_freq_diff;
	
	/* Settings specific to a spaced cluster maxima selection method
	 * 1. Cluster size: the no of points to take as a cluster
	 * 2. Maxes per cluster: the no of maxes to select per cluster
	 */
	public int clusterSize;
	public int maximasPerClusters;
	public boolean takeTDAverage;
	
	
	/* Settings specific to the original maxes selection method
	 * 
	 */
	public int maxMaxes;
	public boolean subFromMean;
	public int nAmpsOfInitialFrames;
	public int nAmpsOfLastFrames;
	/* Setting specifc to searching in the database
	 * 1. rowsToSelect: The number of rows to select in stage 1 from discriminators table
	 * 2. threshold: The threshold below which no results are returned
	 */
	public int rowsToSelect;
	public int threshold;
	
	
	//Not used at the moment
	public int mNoOfParam;
	
	/**
	 * Creates a new Config by reading properties from a config file containing the following information:<br>
	 * <b>debug</b> 			turn debug mode on/off<br>
	 * <b>lb</b> 				smallest index of table from database to search from<br>
	 * <b>ub upper</b> 			largest index of table from database to search from<br>
	 * <b>rowsToSelect</b> 		The number of rows to select in stage 1 from discriminators table<br>
	 * <b>threshold</b> 		minimum hit count required to accept a match<br>
	 * <b>fanoutFactor</b> 		fan out factor for space clustered landmarks<br>
	 * <b>clusterSize</b> 		cluster size for space clustered landmarks<br>
	 * <b>maximasPerClusters</b> maximas to be found for each cluster<br>
	 * <b>takeTDAverage</b> 	take time domain average or not<br>
	 * <b>maxMaxes</b> 			maximum number of maxes allowed<br>
	 * <b>subFromMean</b> 		subtract from mean or not<br>
	 * <b>nAmpsOfInitialFrames</b> number of frames to pick initial threshold from<br>
	 * <b>nAmpsOfLastFrames</b> number of frames to pick reverse threshold from<br>
	 * <b>nfft</b> 				size of fft<br>
	 * <b>shift</b> 			sample shift in each fft<br>
	 * <b>samplingRate</b> 		sampling rate of the audio signal<br>
	 * <b>max_time_diff</b> 	maximum time difference in two landmarks<br>
	 * <b>min_freq_diff</b> 	minimum freq difference in two landmarks<br>
	 * <b>indexDuration</b> 	size of chunks of data processed every time, in seconds<br>
	 * @param sysconfig_file_name the file name to read the configuration from
	 * @throws Exception
	 */
	
	public Config(String sysconfig_file_name) throws Exception{
		prop = new Properties();
		
		FileInputStream sysConfig = new FileInputStream(sysconfig_file_name);
		prop.load(sysConfig);
		
		debug =Boolean.parseBoolean(prop.getProperty("debug"));
		
		lb = Integer.parseInt(prop.getProperty("lb"));
		ub=Integer.parseInt(prop.getProperty("ub"));
		rowsToSelect  = Integer.parseInt(prop.getProperty("rowsToSelect"));
		threshold= Integer.parseInt(prop.getProperty("Threshold"));

		fanoutFactor = Integer.parseInt(prop.getProperty("fanout"));
		clusterSize = Integer.parseInt(prop.getProperty("clusterSize"));
		maximasPerClusters = Integer.parseInt(prop.getProperty("maximasPerCluster"));
		takeTDAverage =  Boolean.parseBoolean(prop.getProperty("takeTDAverage"));
		
		maxMaxes = Integer.parseInt(prop.getProperty("maxMaxes"));
		subFromMean	= Boolean.parseBoolean(prop.getProperty("subFromMean"));
		nAmpsOfInitialFrames=Integer.parseInt(prop.getProperty("nAmpsOfInitialFrames"));
		nAmpsOfLastFrames=Integer.parseInt(prop.getProperty("nAmpsOfLastFrames"));
		
		nfft = Integer.parseInt(prop.getProperty("nfft"));
		shift =Integer.parseInt(prop.getProperty("shift"));
		samplingRate = Integer.parseInt(prop.getProperty("samplingRate"));
		
		max_time_diff = Integer.parseInt(prop.getProperty("max_time_diff"));
		min_freq_diff = Integer.parseInt(prop.getProperty("min_freq_diff"));
		sysConfig.close();
		
		
		
	}
}
