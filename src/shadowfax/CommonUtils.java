package shadowfax;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Stack;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
//import marytts.util.Pair;
/**
 *
 * @author SOM
 */
public class CommonUtils {
    
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void quicksort(double A[], int[] indexes, int p, int r){
		int l=p;
		int u=r;
		Stack<Pair> bag = new Stack<Pair>();
		
		if(l<u){
			bag.push(new Pair<Integer, Integer>(l, u));
		}
		while(!bag.isEmpty()){
			Pair temp = bag.pop();
			int x=(Integer)temp.getFirst();
			int y=(Integer)temp.getSecond();
			
			int t = partition(A, indexes, x, y);
			if(x < t-1)bag.push(new Pair(x,t-1));

			if(y > t+1)bag.push(new Pair(t+1,y));

		}
	}

    public static int partition(double A[], int[] indexes, int p, int r) 
    {
        double x = A[r];
        int i = p - 1;
        for (int j = p; j < r; j++) {
            if (A[j] >= x) {
                i++;
                double temp = A[i];
                A[i] = A[j];
                A[j] = temp;
                int tmpid = indexes[i];
                indexes[i] = indexes[j];
                indexes[j] = tmpid;
            }
        }
        double temp = A[i + 1];
        A[i + 1] = A[r];
        A[r] = temp;
        int tmpid = indexes[i + 1];
        indexes[i + 1] = indexes[r];
        indexes[r] = tmpid;
        return i + 1;
    }
    
    public static double[] locmax1D(double[] X1) {

        double[] Y = new double[X1.length];
        int i;
        double[] Xend = new double[X1.length + 1];
        double[] Xst = new double[X1.length + 1];
        int[] nbr = new int[Xst.length];
        Xend[X1.length] = X1[X1.length - 1];
        Xst[0] = X1[0];

        for (i = 0; i < X1.length - 1; i++) {
            Xend[i] = X1[i];
            Xst[i + 1] = X1[i];
            nbr[i] = Xend[i] >= Xst[i] ? 1 : 0;
            if (i != 0) {
                Y[i - 1] = X1[i - 1] * nbr[i - 1] * (1 - nbr[i]);
            }
        }
        Y[i] = 0;
        return Y;
    }

    public static double[] locmax(double[][] X) 
    {
        double[] X1 = convertToRow(X);
        double[] Y = new double[X1.length];
        int i;
        double[] Xend = new double[X1.length + 1];
        double[] Xst = new double[X1.length + 1];
        int[] nbr = new int[Xst.length];
        Xend[X1.length] = X1[X1.length - 1];
        Xst[0] = X1[0];

        for (i = 0; i < X1.length - 1; i++) {
            Xend[i] = X1[i];
            Xst[i + 1] = X1[i];
            nbr[i] = Xend[i] >= Xst[i] ? 1 : 0;
            if (i != 0) {
                Y[i - 1] = X1[i - 1] * nbr[i - 1] * (1 - nbr[i]);
            }
        }
        Y[i] = 0;
        return Y;
    }

    public static double[] convertToRow(double[][] source) 
    {
        int numElements = source.length * source[0].length;
        double[] row = new double[numElements];
        int i, j;
        long width = source[0].length;
        long height = source.length;
        int count = 0;
        for (j = 0; j < width; j++) {
            for (i = 0; i < height; i++) {
                row[count++] = source[i][j];
            }
        }
        return row;
    }

    @SuppressWarnings("null")
	public static double[] spread(double[][] X, double[] E) {
        if (E == null) {
            E[0] = 4;
        }
        if (E.length == 1) {
            int W = (int) (4 * E[0]);
            double[] W1 = new double[2 * W + 1];
            long assign = -W, i;
            for (i = 0; i < 2 * W + 1; i++) {
                W1[(int) i] = (assign++) / E[0];
                W1[(int) i] = (-0.5) * (W1[(int) i] * W1[(int) i]);
            }
            E = new double[2 * W + 1];
            for (i = 0; i < 2 * W + 1; i++) {
                E[(int) i] = Math.exp(W1[(int) i]);
            }
        }
        long i;
        double[] X1 = locmax(X);
        double[] Y = new double[X1.length];
        for (i = 0; i < Y.length; i++) {
            Y[(int) i] = 0;
        }
        long lenx = X1.length;
        long maxi = X1.length + E.length;
        long spos = (E.length % 2 == 0) ? (1 + (int) (E.length / 2))
                : (1 + (int) ((E.length - 1) / 2));
        double[] EE = new double[(int) maxi];
        double[] EE1 = new double[X1.length];
        for (i = 0; i < maxi; i++) {
            EE[(int) i] = 0;
        }
        for (i = 0; i < lenx; i++) {
            if (X1[(int) i] == 0) {
                continue;
            } else {
                long count = 0;
                long j;

                spos = (E.length % 2 == 0) ? (1 + (int) (E.length / 2))
                        : (1 + (int) ((E.length - 1) / 2));
                for (j = 0; j < i; j++) {
                    EE[(int) j] = 0;
                }

                for (j = i; j < i + E.length; j++) {
                    EE[(int) j] = E[(int) count++];
                }
                for (j = i + E.length; j < maxi; j++) {
                    EE[(int) j] = 0;
                }
                count = 0;
                for (j = spos; j < spos + lenx; j++) {
                    EE1[(int) count++] = EE[(int) j];
                }
                for (j = 0; j < X1.length; j++) {
                    Y[(int) j] = Y[(int) j] > (X1[(int) i]) * EE1[(int) j] ? Y[(int) j]
                            : (X1[(int) i] * EE1[(int) j]);
                }
            }
        }
        return Y;
    }

    public static double mean2D(double[][] spec) {
        long NumSamples = 0;
        double mean = 0;
        int i, j;
        for (i = 0; i < spec.length; i++) {
            for (j = 0; j < spec[i].length; j++) {
                mean = mean + spec[i][j];
                NumSamples++;
            }
        }
        return (mean / NumSamples);
    }

    public static double max2Nos(double no1, double no2) 
    {
        return (no1 > no2 ? no1 : no2);
    }

    public static double max1D(double[] spec) {
        long i;
        double max = Double.MIN_VALUE;
        for (i = 0; i < spec.length; i++) {
            max = max > spec[(int) i] ? max : spec[(int) i];
        }
        return max;
    }

    public static double max2D(double[][] spec) {
        long i, j;
        double max = Double.MIN_VALUE;
        int height = spec.length;
        for (i = 0; i < height; i++) {
            int width = spec[(int) i].length;
            for (j = 0; j < width; j++) {
                if (spec[(int) i][(int) j] > max) {
                    max = spec[(int) i][(int) j];
                }
            }
        }
        return max;
    }
    
    public static double getAbsMax (double [] signal)
    {
        double max = Double.MIN_VALUE;
        for(int i=0;i<signal.length;i++)
        {
            double absVal = signal[i] >= 0?signal[i]:(-signal[i]) ;
            max = max < absVal ? absVal:max;
        }
        return max;
    }
    
    public static void playAudioStream(AudioInputStream audioInputStream) {        
        AudioFormat audioFormat = audioInputStream.getFormat();
        System.out.println("Play input audio format=" + audioFormat);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        if (!AudioSystem.isLineSupported(info)) {
            System.out.println("Play.playAudioStream does not handle this type of audio on this system.");
            return;
        }

        try {        
            SourceDataLine dataLine = (SourceDataLine) AudioSystem.getLine(info);
            dataLine.open(audioFormat);
        
            if (dataLine.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl volume = (FloatControl) dataLine.getControl(FloatControl.Type.MASTER_GAIN);
                volume.setValue(6.0F);
            }        
            dataLine.start();        
            int bufferSize = (int) audioFormat.getSampleRate() * audioFormat.getFrameSize();
            byte[] buffer = new byte[bufferSize];        
            try {
                int bytesRead = 0;
                while (bytesRead >= 0) {
                    bytesRead = audioInputStream.read(buffer, 0, buffer.length);
                    if (bytesRead >= 0) {
                        dataLine.write(buffer, 0, bytesRead);
                    }
                } // while
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Play.playAudioStream draining line.");            
            dataLine.drain();
            System.out.println("Play.playAudioStream closing line.");            
            dataLine.close();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }
    
    public static BufferedWriter WriteToFile(String filename,String data) throws IOException
    {
        BufferedWriter b = new BufferedWriter(new FileWriter(filename));
        b.write(data);
        return b;                
    }
}
