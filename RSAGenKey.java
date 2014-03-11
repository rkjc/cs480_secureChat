import java.io.*;
import java.math.BigInteger;
import java.util.Random;

/*
 * Delete pub_key.txt and pri_key
 * before generating new keys, if they already exist. 
 */

public class RSAGenKey {

	public static void main(String[] args) throws Exception {
		
		Random rand = new Random();
		int k = 0;
		
		if (args.length > 0) {
			try {
				k = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				System.err.println("Argument" + " must be an integer");
				System.exit(1);
			}
		}

		BigInteger p = new BigInteger(k, rand);
		BigInteger q = new BigInteger(k, rand);
		BigInteger n = p.multiply(q);
		BigInteger phi = (p.subtract(BigInteger.ONE)).multiply(q
				.subtract(BigInteger.ONE));

		int nlength; // using length of n, plug into random gen for e generation
		int res = 0; // int used as a bool to check BigInteger's compareTo
		BigInteger e = null;

		
		while (res != -1) // check e is less than phi
		{
			nlength = rand.nextInt(n.bitLength() - 10) + 10; // min of 10 bits
			e = new BigInteger(nlength, rand).nextProbablePrime();

			res = e.compareTo(phi); // 0 both equal, 1 first is greater, -1
									// second is greater
			if (e.gcd(phi).equals(BigInteger.ONE) != true)
				res = 0;
		}

		BigInteger d = e.modInverse(phi);

		// System.out.println("p " + p);
		// System.out.println("q " + q);
		// System.out.println("n " + n);
		// System.out.println("phi " + phi);
		// System.out.println("e " + e);
		// System.out.println("d " + d);

		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
					"pub_key.txt"), true));
			bw.write("e = " + e.toString());
			bw.newLine();
			bw.write("n = " + n.toString());
			bw.close();
		} catch (Exception ex) {
		}

		try {
			BufferedWriter bw2 = new BufferedWriter(new FileWriter(new File(
					"pri_key.txt"), true));
			bw2.write("d = " + d.toString());
			bw2.newLine();
			bw2.write("n = " + n.toString());
			bw2.close();
		} catch (Exception ex) {
		}

	}

}
