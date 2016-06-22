package com.sg.secram.encryption;

import htsjdk.samtools.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Hashtable;

import org.bouncycastle.crypto.Mac;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;

import com.sg.secram.SECRAMEncryptionMethod;

/**
 * 
 * @author zhicong
 *
 */

/*
 * The notation and algorithms here are from the paper "Order-preserving
 * symmetric encryption" by Boldyreva et Al., 2009
 */

public class OPE implements SECRAMEncryptionMethod<Long>{
	private static final int BitsForRCoins = 64;
	private static final int BitsForHGDCoins = 128;
	
	//secretkey used for OPE algorithm
	private byte[] key = null;
	
	//We constrain the plaintextspace to be 40 bits
	private static final int plainTextSpace = 40;
	public static final long MIN_PLAINTEXT = 0;
	public static final long MAX_PLAINTEXT = (1l << plainTextSpace) - 1;
	
	//ciphertextspace is 48 bits
	private static final int cipherTextSpace = 48;
	public static final long MIN_CIPHERTEXT = 0;
	public static final long MAX_CIPHERTEXT = (1l << cipherTextSpace) - 10;
	
	private Mac VIL_PRF;
	
	public Hashtable<Long, Long> cache;
	private static final int maxCacheSize = 4000000;
	
	private Log log = Log.getInstance(OPE.class);
	private boolean DEBUG = false;
	
	/**
	 * This OPE implementation works for 64-bit integer
	 */
	
	public OPE(){
		this("SECRET_1SECRET_2SECRET_3".getBytes());
	}
	
	public OPE(byte[] keyBytes){
		//get the key
        key = new byte[keyBytes.length];
        System.arraycopy(keyBytes, 0 , key, 0, keyBytes.length);
		
		VIL_PRF = new HMac(new SHA1Digest());
		VIL_PRF.init(new KeyParameter(key));
		
		this.cache = new Hashtable<Long, Long>(maxCacheSize);
	}
	
	public int getUsedCache(){
		return cache.size();
	}
	
	public void fillCache(){
		for(int i = 0; i < 10000000; i++){
			cache.put((long)i, (long)i+1);
		}
	}
	
	/*
	 * When you want to encrypt a number, call this function
	 */
	public long encrypt(long plainNum) throws IOException, HGDException, NoSuchAlgorithmException{
		if(plainNum < MIN_PLAINTEXT || plainNum > MAX_PLAINTEXT) {
			if(plainNum < MIN_CIPHERTEXT || plainNum > MAX_CIPHERTEXT){ //try to skip the exception if the number is also out of the ciphertext range, so that no one would misuse it for decryption
				if(DEBUG)
					log.debug("OPE cannot encrypt number " + plainNum 
							+ " because it is out of the plaintext range. It has been returned without any change.");
				return plainNum;
			}
			throw new IllegalArgumentException("OPE encryption failed: the given plaintext number " + plainNum
			+ " is out of plaintet range [" + MIN_PLAINTEXT + ", " + MAX_PLAINTEXT + "]");
		}
		long result = EncK(MIN_PLAINTEXT, MAX_PLAINTEXT, MIN_CIPHERTEXT, MAX_CIPHERTEXT, plainNum);
		
		return result;
	}
	
	/**
	 * main encryption function in the paper
	 * @throws HGDException 
	 * @throws NoSuchAlgorithmException 
	 */
	private long EncK(long lowD, long highD, long lowR, long highR, long m) throws IOException, HGDException, NoSuchAlgorithmException{
		long M = highD - lowD + 1;
		long N = highR - lowR + 1;
		long d = lowD - 1;
		long r = lowR - 1;
		long y = r + (N+1)/2;
		
		byte[] coins;
		if(M == 1){
			//generate coins for random selection in the range[lowR, highR]
			coins = TapeGen(lowD, highD, lowR, highR, m, BitsForRCoins);
			SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
			sr.setSeed(coins);
			long result = lowR + ((sr.nextLong() & 0x7fffffffffffffffl)%N);
			return result;
		}
		Long x = cache.get(y);
		if(x == null){
			coins = TapeGen(lowD, highD, lowR, highR, y, BitsForHGDCoins);
			x = lowD + HGD(y-lowR, M, N - M, coins);
			if(cache.size() > maxCacheSize)
				cache.clear();
			cache.put(y, x);
		}
		if(m <= x){
			lowD = d+1;
            highD = x;
            lowR = r+1;
            highR = y;
        } else {
            lowD = x+1;
            highD = d + M;
            lowR = y+1;
            highR = r+N;
		}
		return EncK(lowD, highD, lowR, highR, m);
	}
	
	/*
	 * When you want to decrypt a number, call this function
	 */
	public long decrypt(long cipherNum) throws IOException, HGDException, NoSuchAlgorithmException{
		if (cipherNum < MIN_CIPHERTEXT || cipherNum > MAX_CIPHERTEXT) {
			if(DEBUG)
				log.debug("OPE cannot decrypt number " + cipherNum 
						+ " because it is out of the ciphertext range. It has been returned without any change.");
			return cipherNum;
		}
		long result = DecK(MIN_PLAINTEXT, MAX_PLAINTEXT, MIN_CIPHERTEXT, MAX_CIPHERTEXT, cipherNum);
		
		return result;
	}
	
	/*
	 * almost symmetric with EncK
	 */
	private long DecK(long lowD, long highD, long lowR, long highR, long c) throws IOException, HGDException, NoSuchAlgorithmException{
		long M = highD - lowD + 1;
		long N = highR - lowR + 1;
		long d = lowD - 1;
		long r = lowR - 1;
		long y = r + (N+1)/2;
		
		long m;
		byte[] coins;
		if(M == 1){
			m = lowD;
			coins = TapeGen(lowD, highD, lowR, highR, m, BitsForRCoins);
			SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
			sr.setSeed(coins);
			long w = lowR + ((sr.nextLong() & 0x7fffffffffffffffl)%N);
			if(w == c)
				return m;
			else{
				log.error(String.format("This value %d was not encrypted correctly", c));
				throw new IllegalArgumentException();
			}
		}
		
		Long x = cache.get(y);
		if(x == null){
			coins = TapeGen(lowD, highD, lowR, highR, y, BitsForHGDCoins);
			x = lowD + HGD(y-lowR, M, N - M, coins);
			if(cache.size() > maxCacheSize)
				cache.clear();
			cache.put(y, x);
		}
		if (c <= y) {
            lowD = d+1;
            highD = x;
            lowR = r+1;
            highR = y;
        } else {
            lowD = x+1;
            highD = d + M;
            lowR = y+1;
            highR = r+N;
        }
		return DecK(lowD, highD, lowR, highR, c);
	}
	
	/**
	 * generate a specific num of peudorandom bits
	 */
	public byte[] TapeGen(long lowD, long highD, long lowR, long highR, long m, int numOfBits) throws IOException, NoSuchAlgorithmException{
		int numOfBytes = (numOfBits+7) / 8;
		
		byte[] input = longsToBytes(new long[]{lowD, highD, lowR, highR, m});
		byte[] seed = new byte[VIL_PRF.getMacSize()];
		//Javax.crypto.mac is not thread-safe. Synchronized it if multiple threads will access the mac instance, 
		//otherwise Exception will be thrown (you can try to remove synchronized and see what happens)
		synchronized(VIL_PRF){
			VIL_PRF.update(input, 0, input.length);
			VIL_PRF.doFinal(seed, 0);
		}
		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
		sr.setSeed(seed);
		byte[] coins = new byte[numOfBytes];
		sr.nextBytes(coins);
		if(numOfBytes % 8 != 0)
			coins[0] &= ~(0xff00 >> (8 - numOfBytes % 8));
		return coins;
	}
	
	/**
	 * generation of HyperGeometric random variate
	 * An implementation of:
	 * Computer Generation of Hypergeometric Random Variates. J.Stat.Comput.Simul.22(1985), 127-145
	 * @throws HGDException 
	 * @throws NoSuchAlgorithmException 
	 * 
	 * Yeah, this algorithm is complicated. Don't waste your time on this.
	 */
	public long HGD(long KK, long NN1, long NN2, byte[] coins) throws HGDException, NoSuchAlgorithmException{
		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
		sr.setSeed(coins);
		
		double JX;
		double TN, N1, N2, K;
		double P, U, V, A, IX = 0, XL, XR, M;
		double KL, KR, LAMDL, LAMDR, NK, NM, P1, P2, P3;
		
		boolean REJECT;
		double MINJX, MAXJX;
		
		double CON = 57.56462733;
		double DELTAL = 0.0078;
		double DELTAU = 0.0034;
		double SCALE = 1.0e25;
		
		
		
		//check for validity
		
		if(NN1 < 0 || NN2 < 0 || KK < 0|| KK > ((NN1)+(NN2))){
			log.error("Invalid parameters for HGD NN1: " + NN1 + ", NN2: "+NN2 + ", KK: "+KK);
			throw new IllegalArgumentException();
		}
		
		//Initialize. Set-up constants.
		REJECT = true;
		if(NN1 >= NN2){
			N1 = NN2;
			N2 = NN1;
		}else{
			N1 = NN1;
			N2 = NN2;
		}
		TN = N1 + N2;
		if(KK + KK >= TN){
			K = TN - KK;
		}else{
			K = KK;
		}
		
		M = (K+1) * (N1 + 1) / (TN+2);  //the mode
		
		if(K-N2 < 0){
			MINJX = 0;
		}else{
			MINJX = K-N2;
		}
		
		if(N1 < K){
			MAXJX = N1;
		}else{
			MAXJX = K;
		}
		
		//Generate random variate
		if(MINJX == MAXJX){
			IX = MINJX;
		}
		else if(M-MINJX < 10){
			//Inverse Transformation
			double W;
			if(K < N2){
				W = Math.exp(CON + AFC(N2) + AFC(N1+N2-K) - AFC(N2-K) - AFC(N1+N2)); 
			}else{
				W = Math.exp(CON + AFC(N1) + AFC(K) - AFC(K-N2) - AFC(N1 + N2));
			}
			
			boolean flagTen = true;
			boolean flagTwenty = true;
			int countFlagTen = 0, countFlagTwenty = 0;
			//10
			while(flagTen){
				countFlagTen++;
				if(countFlagTen % 500 == 0){
					if(DEBUG){
						log.debug("passed through label ten "+countFlagTen+" times");
					}
				}
				flagTen = false;
				P = W;
				IX = MINJX;
				U = sr.nextDouble()*SCALE;
				countFlagTwenty = 0;
				while(flagTwenty && !flagTen){
					countFlagTwenty ++;
					if(countFlagTwenty > 1000){
						log.error("Time out in Inverse Transfromation");
						throw new HGDException();
					}
					flagTwenty = false;
					if(U > P){
						U = U - P;
						P = P * (N1-IX) * (K - IX);
						IX = IX + 1;
						P = P / IX / (N2 - K + IX);
						if(IX > MAXJX){
							flagTen = true;
						}
						flagTwenty = true;
					}
				}
			}
			if(DEBUG)
				log.debug("Inverse Transfromation: MINJX: "+ MINJX+", MAXJX: "+ MAXJX+", IX: " + IX);
		}
		else{
			// H2PE
			double S = Math.sqrt((TN - K) * K * N1 * N2 / (TN - 1) / TN / TN);
			double D = Math.floor(1.5*S) + 0.5;
			XL = Math.floor(M - D + 0.5);
			XR = Math.floor(M + D + 0.5);
			A = AFC(M) + AFC(N1-M) + AFC(K-M) + AFC(N2-K+M);
			KL = Math.exp(A - AFC(XL) - AFC(N1-XL) - AFC(K - XL) - AFC(N2 - K + XL));
			KR = Math.exp(A - AFC(XR - 1) - AFC(N1 - XR + 1) - AFC(K - XR + 1) - AFC(N2 - K + XR - 1));
			LAMDL = -Math.log(XL * (N2-K+XL) / (N1-XL+1) / (K-XL+1));
	        LAMDR = -Math.log((N1-XR+1) * (K-XR+1) / XR / (N2-K+XR));
	        P1 = 2*D;
	        P2 = P1 + KL / LAMDL;
	        P3 = P2 + KR / LAMDR;
	        
	        int countThirtyB = 0;
	        //30
	        while(REJECT){
	        	countThirtyB++;
	        	if(countThirtyB % 500 == 0){
	        		if(DEBUG){
	        			log.debug("In H2PE, count is " + countThirtyB);
	        		}
	        	}
	        	U = sr.nextDouble() * P3;
	        	V = sr.nextDouble();
	        	if(U < P1){
	        		//Rectangular region
	        		IX = XL + U;
	        	}
	        	else if(U <= P2){
	        		//left tail
	        		IX = XL + Math.log(V) / LAMDL;
	        		if(IX < MINJX){
	        			if(DEBUG)
	        				log.debug("left. \n");
	        			continue;
	        		}
	        		V = V * (U - P1) * LAMDL;
	        	}
	        	else{
	        		//right tail
	        		IX = XR - Math.log(V) / LAMDR;
	        		if(IX > MAXJX){
	        			if(DEBUG)
	        				log.debug("right. \n");
	        			continue;
	        		}
	        		V = V * (U - P2) * LAMDR;
	        	}
	        	
	        	//acceptance/rejection test
	        	double F;
	        	if(M < 100 || IX <= 50){
	        		//explicit evaluation
	        		F = 1.0;
	        		if(M < IX){
	        			for(double I = M+1; I < IX; I++){
	        				F = F * (N1-I+1) * (K-I+1) / (N2-K+I) / I;
	        			}
	        		}else if(M > IX){
	        			for (double I = IX+1; I < M; I++) {
	                        F = F * I * (N2-K+I) / (N1-I) / (K-I);
	                    }
	        		}
	        		if(V <= F){
	        			REJECT = false;
	        		}
	        	}
	        	else{
	        		//SQUEEZE USING UPPER AND LOWER BOUNDS...
		            double Y   = IX;
		            double Y1  = Y + 1;
		            double YM  = Y - M;
		            double YN  = N1 - Y + 1;
		            double YK  = K - Y + 1;
		            NK  = N2 - K + Y1;
		            double R   = -YM / Y1;
		            double S2  = YM / YN;
		            double T   = YM / YK;
		            double E   = -YM / NK;
		            double G   = YN * YK / (Y1*NK) - 1;
		            double DG  = 1.0;
		            if (G < 0)  { DG = 1.0 +G; }
		            double GU  = G * (1+G*(-0.5+G/3.0));
		            double GL  = GU - 0.25 * Math.pow(G, 4) / DG;
		            double XM  = M + 0.5;
		            double XN  = N1 - M + 0.5;
		            double XK  = K - M + 0.5;
		            NM  = N2 - K + XM;
		            double UB  = Y * GU - M * GL + DELTAU  + XM * R *
		                     (1.+R*
		                      (-.5+R/
		                       3.))  + XN * S2 *
		                     (1.+S2*
		                      (-0.5+S2/
		                       3.))  + XK * T *
		                     (1.+T*(-.5+T/3.))   + NM * E * (1.+E*(-.5+E/3.));
		            
		            //TEST AGAINST UPPER BOUND
		            double ALV = Math.log(V);
		            if (ALV > UB) {
		                REJECT = true;
		            }
		            else {
		            	//TEST AGAINST LOWER BOUND

		                double DR = XM * Math.pow(R, 4);
		                if (R < 0)  {
		                    DR = DR / (1.+R);
		                }
		                double DS = XN * Math.pow(S2, 4);
		                if (S2 < 0) {
		                    DS = DS / (1+S2);
		                }
		                double DT = XK * Math.pow(T, 4);
		                if (T < 0)  { DT = DT / (1+T); }
		                double DE = NM * Math.pow(E, 4);
		                if (E < 0)  {
		                    DE = DE / (1+E);
		                }
		                if (ALV < UB-0.25*(DR+DS+DT+DE)  +(Y+M)*(GL-GU)-DELTAL) {
		                    REJECT = false;
		                } else {
		                	//STIRLING'S FORMULA TO MACHINE ACCURACY

		                    if (ALV <=
		                        (A - AFC(IX) -
		                         AFC(N1-IX)  - AFC(K-IX) - AFC(N2-K+IX)) ) {
		                        REJECT = false;
		                    } else {
		                        REJECT = true;
		                    }
		                }
		            }
	        	}
	        }
	        
		}
		//return appropriate variate
		if (KK + KK >= TN) {
	        if (NN1 > NN2) {
	            IX = KK - NN2 + IX;
	        } else {
	            IX =  NN1 - IX;
	        }
	    } else {
	        if (NN1 > NN2)  { IX = KK - IX; }
	    }
	    JX = IX;
	    if (DEBUG) 
	    	log.debug("KK: " + KK + ", NN1: " + NN1 + ", NN2: " + NN2 + "HGD Sample value: " + JX);
	    return (long)(JX+0.5);
	}
	
	/**
	 * Function to evaluate logarithm of the factorial I
	 * if(I > 7) use stirling's approximation
	 * otherwise, use table lookup
	 * @param n
	 * @return
	 */
	public double AFC(double I){
		double[] AL = new double[]
	    {0.0, 0.0, 0.6931471806, 1.791759469, 3.178053830, 4.787491743,
	     6.579251212, 8.525161361};

	    if (I <= 7) {
	        return AL[(int) Math.round(I)];
	    } else {
	        double LL = Math.log(I);
	        return (I+0.5) * LL - I + 0.399089934;
	    }
	}
	
	/*
     * transform a long array to a byte array
     */
    public byte[] longsToBytes(long[] values) throws IOException{
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        for(int i=0; i < values.length; ++i)
        {
             dos.writeLong(values[i]);
        }

        return baos.toByteArray();
    }
	
	class HGDException extends Exception{
		
	}

	@Override
	public Long encrypt(Long objectToEncrypt, String key) {
		
		try {
			return encrypt(objectToEncrypt);
		} catch (NoSuchAlgorithmException | IOException | HGDException e) {
			log.error("Order preserving encryption failed");
			e.printStackTrace();
			System.exit(1);
		}
		return -1L;
	}

	@Override
	public Long decrypt(Long objectToDecrypt, String key) {
		
		try {
			return decrypt(objectToDecrypt);
		} catch (NoSuchAlgorithmException | IOException | HGDException e) {
			log.error("Order preserving decrption failed");
			e.printStackTrace();
			System.exit(1);
		}
		return -1L;
	}
}
