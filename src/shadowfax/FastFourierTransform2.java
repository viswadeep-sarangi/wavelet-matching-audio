package shadowfax;

public final class FastFourierTransform2 {

    @SuppressWarnings("unused")
	private static final double LOG10 = Math.log(10);
	private int n, nu;
    double[] sn;
    double[] cs;
    /**
     * Creates a fft object which canbe used to compute fft of an array of real values size n
     * @param n 	the size of the fft
     */
    public FastFourierTransform2(int n){
    	sn = new double[n];
        cs = new double[n];
        this.n = n;
    	this.nu = (int)(Math.log(n)/Math.log(2));
    	for(int i=0; i<n/2; i++) {
    	       cs[i] = Math.cos(-2*Math.PI*i/n);
    	       sn[i] = Math.sin(-2*Math.PI*i/n);
    	     }
    	
    }
    
    

    private int bitrev(int j) {

        int j2;
        int j1 = j;
        int k = 0;
        for (int i = 1; i <= nu; i++) {
            j2 = j1/2;
            k  = 2*k + j1 - 2*j2;
            j1 = j2;
        }
        return k;
    }
    /**
     * computes the fft for the data in the double array x
     * @param x 	the input data for the fft
     * @return 		double array containing the energies of each fft bin
     */
    public final double[] fftMag(double[] x) {
        // assume n is a power of 2
        if(this.n != x.length){
        	throw new RuntimeException("FFT input length does not match the FFT Size");
        }
        
        int n2 = this.n/2;
        int nu1 = this.nu - 1;
        double[] xre = new double[n];
        double[] xim = new double[n];
        double[] mag = new double[n2];
        double tr, ti;
        int p;
        for (int i = 0; i < n; i++) {
            xre[i] = (double) x[i];
            xim[i] = 0.0f;
        }
        int k = 0;

        for (int l = 1; l <= nu; l++) {
            while (k < n) {
                for (int i = 1; i <= n2; i++) {
                	p = bitrev (k >> nu1);
                    tr = xre[k+n2]*cs[p] + xim[k+n2]*sn[p];
                    ti = xim[k+n2]*cs[p] - xre[k+n2]*sn[p];
                    xre[k+n2] = xre[k] - tr;
                    xim[k+n2] = xim[k] - ti;
                    xre[k] += tr;
                    xim[k] += ti;
                    k++;
                }
                k += n2;
            }
            k = 0;
            nu1--;
            n2 = n2/2;
        }
        k = 0;
        int r;
        while (k < n) {
            r = bitrev (k);
            if (r > k) {
                tr = xre[k];
                ti = xim[k];
                xre[k] = xre[r];
                xim[k] = xim[r];
                xre[r] = tr;
                xim[r] = ti;
            }
            k++;
        }

        mag[0] = (double) (Math.sqrt(xre[0]*xre[0] + xim[0]*xim[0]));
        for (int i = 1; i < n/2; i++){
            mag[i]=  (double) Math.sqrt(xre[i]*xre[i] + xim[i]*xim[i]);
        }
        return mag;
    }
}
