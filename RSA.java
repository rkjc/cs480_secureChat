package secureChat;
import java.math.BigInteger;
import java.util.Random;

public class RSA {

	static BigInteger p= null;
	static BigInteger q= null;
	static BigInteger e= null;
	static BigInteger d= null;
	static BigInteger n= null;
	static Random rand = new Random();

	static int k = 128;

	public static void main(String[] args) throws Exception {
		GenKey();
//		System.out.println("e lenght is " + e.bitLength());
//		System.out.println("d lenght is " + d.bitLength());
//		System.out.println("n lenght is " + n.bitLength());

		String s = "Hello world";

		byte[] p = new byte[16];
		byte[] c = new byte[16];

		p = s.getBytes();

		c = EncryptRSA(p);

		p = DecryptRSA(c);

		System.out.println(new String(p));
	}

	
	static void GenPandQ(){
			p = BigInteger.probablePrime(k, rand);
			q = BigInteger.probablePrime(k, rand);
	}
	static void GenKey() {
		
		GenPandQ();
		n = p.multiply(q);

		int x = n.bitLength();
		if (x == 255) {
			GenPandQ();
			n = p.multiply(q);
		}
		
		BigInteger phi = (p.subtract(BigInteger.ONE)).multiply(q
				.subtract(BigInteger.ONE));

		int nlength; // using length of n, plug into random gen for e generation
		int res = 0; // int used as a bool to check BigInteger's compareTo

		while (res != -1) // check e is less than phi
		{
			nlength = rand.nextInt(n.bitLength() - 10) + 10; // min of 10 bits
			e = new BigInteger(nlength, rand).nextProbablePrime();

			res = e.compareTo(phi); // 0 both equal, 1 first is greater, -1
									// second is greater
			if (e.gcd(phi).equals(BigInteger.ONE) != true)
				res = 0;
		}

		d = e.modInverse(phi);

	}

	private static byte[] EncryptRSA(byte[] p) throws Exception{
		BigInteger plain = new BigInteger(p);
		plain.modPow(e, n);
		return plain.toByteArray();
	}

	private static byte[] DecryptRSA(byte[] c) {
		BigInteger cipher = new BigInteger(c);
		cipher.modPow(d, n);
		return cipher.toByteArray();
	}

}
