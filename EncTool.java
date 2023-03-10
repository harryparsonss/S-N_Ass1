// An encryption tool
// Apart of Ex1 for Intro. to Comp. Sec.

import java.io.Console;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.Certificate;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

//The standard Java crypto libraries don't do CCM mode as default,
// so we will need another provider.
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class EncTool {

    static String inFile = "plainText.txt";
    static String outFile = "cipherText.enc";
    static String hexKey = "3eafda76cd8b015641cb946708675423";
    static String keyStore;
    static String keyName;

    public static void main(String[] args) {

        // Handle the command line arguments
        if (args.length == 4 && args[0].equals("-encAESCTR") && args[1].length() == 32) {
            hexKey = args[1];
            inFile = args[2];
            outFile = args[3];
            encryptAESCTR();
        } else if (args.length == 4 && args[0].equals("-decAESCTR") && args[1].length() == 32) {
            hexKey = args[1];
            inFile = args[2];
            outFile = args[3];
            decryptAESCTR();
        } else if (args.length == 4 && args[0].equals("-encAESCCM") && args[1].length() == 32) {
            hexKey = args[1];
            inFile = args[2];
            outFile = args[3];
            encryptAESCCM();
        } else if (args.length == 4 && args[0].equals("-decAESCCM") && args[1].length() == 32) {
            hexKey = args[1];
            inFile = args[2];
            outFile = args[3];
            decryptAESCCM();
        } else if (args.length == 5 && args[0].equals("-encRSA")) {
            keyStore = args[1];
            keyName = args[2];
            inFile = args[3];
            outFile = args[4];
            encryptRSA();
        } else if (args.length == 5 && args[0].equals("-decRSA")) {
            keyStore = args[1];
            keyName = args[2];
            inFile = args[3];
            outFile = args[4];
            decryptRSA();
        } else if (args.length == 1 && args[0].equals("-genAES")) {
            generateKey();
        } else {
            System.out.println("This is a simple program to encrypt and decrypt files");
            System.out.println("Usage: ");
            System.out
                    .println("    -encAESCTR <key:128 bits in as hex> <inputFile> <outputFile>  AES CTR mode encrypt");
            System.out
                    .println("    -decAESCTR <key:128 bits in as hex> <inputFile> <outputFile>  AES CTR mode decrypt");
            System.out
                    .println("    -encAESCCM <key:128 bits in as hex> <inputFile> <outputFile>  AES CCM mode encrypt");
            System.out.println("    -decAESCCM <key:128 bits in as hex> <inputFile> <outputFile>  AES CCM modedecrypt");
            System.out.println("    -encRSA <keyStore> <keyName> <inputFile> <outputFile>         RSA encrypt");
            System.out.println("    -decRSA <keyStore> <keyName> <inputFile> <outputFile>         RSA decrypt");
            System.out.println("    -genAES     generate an AES key");
        }
    }

    private static void encryptRSA() {
        try {
            // Get the public key from the keyStore and set up the Cipher object
            PublicKey publicKey = getPubKey(keyStore, keyName);
            Cipher rsaCipher = Cipher.getInstance("RSA");
            rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);

            // Read the plainText
            System.out.println("Loading plaintext file: " + inFile);
            RandomAccessFile rawDataFromFile = new RandomAccessFile(inFile, "r");
            byte[] plainText = new byte[(int) rawDataFromFile.length()];
            rawDataFromFile.read(plainText);

            // Generate a symmetric key to encrypt the data and initiate the AES Cipher
            // Object
            System.out.println("Generating AES key");
            KeyGenerator sKenGen = KeyGenerator.getInstance("AES");
            Key aesKey = sKenGen.generateKey();
            Cipher aesCipher = Cipher.getInstance("AES");
            aesCipher.init(Cipher.ENCRYPT_MODE, aesKey);

            // Encrypt the symmetric AES key with the public RSA key
            System.out.println("Encrypting Data");
            byte[] encodedKey = rsaCipher.doFinal(aesKey.getEncoded());
            // Encrypt the plaintext with the AES key
            byte[] cipherText = aesCipher.doFinal(plainText);

            // Write the encrypted AES key and Ciphertext to the file.
            System.out.println("Writting to file: " + outFile);
            FileOutputStream outToFile = new FileOutputStream(outFile);
            outToFile.write(encodedKey);
            outToFile.write(cipherText);

            System.out.println("Closing Files");
            rawDataFromFile.close();
            outToFile.close();
        } catch (Exception e) {
            System.out.println("Doh: " + e);
        }
    }

    private static PublicKey getPubKey(String keyStoreFile, String keyName) {
        PublicKey publicKey = null;
        try {
            // Load the keyStore
            KeyStore myKeyStore = KeyStore.getInstance("JKS");
            FileInputStream inStream = new FileInputStream(keyStoreFile);

            // Get the keyStore password, using Console lets us mask the password
            Console console = System.console();
            char[] password = console.readPassword("Enter your secret password: ");
            myKeyStore.load(inStream, password);
            Certificate cert = myKeyStore.getCertificate(keyName);
            publicKey = cert.getPublicKey();


        } catch (Exception e) {
            System.out.println("Doh: " + e);
        }
        return publicKey;
    }

    private static void decryptRSA() {
        System.out.println("RSA Decryption not yet supported");
        try{

            // getting private key from the keystore
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            FileInputStream inStream = new FileInputStream(keyStore);
            Console console = System.console();
            char[] password = console.readPassword("Enter your secret password: ");
            ks.load(inStream, password);
          //  System.out.println("Getting private key");
            // KeyStore.PrivateKeyEntry pKeyEntry = (KeyStore.PrivateKeyEntry)ks.getKey(keyName, password);
            PrivateKey privatekey = (PrivateKey)ks.getKey(keyName, password);

           //System.out.println("Private key: " + privatekey.getEncoded());


            // loading the cipher text
           System.out.println("Loading cipher text file: " + inFile);
           RandomAccessFile rawDataFromFile = new RandomAccessFile(inFile, "r");
           byte[] ciphertext = new byte[(int) rawDataFromFile.length()];
           rawDataFromFile.read(ciphertext);

        // getting the AES key and cipher text and splitting them
           byte AES[] = new byte[256];
           byte text[] = new byte[ciphertext.length - 256];
           System.arraycopy(ciphertext, 0, AES, 0, 256);
           System.arraycopy(ciphertext, 256, text, 0, ciphertext.length - 256);

            // decrpyt AES key

            Cipher AEScip = Cipher.getInstance("RSA");
           // SecretKeySpec secKey = new SecretKeySpec(privatekey.getEncoded(), "AES");
            AEScip.init(Cipher.DECRYPT_MODE, privatekey);
            byte[] plainAES = AEScip.doFinal(AES);


            // decrpt the cipherText using AES

            Cipher cipher = Cipher.getInstance("AES");
            SecretKeySpec secKey = new SecretKeySpec(plainAES, "AES");
            cipher.init(Cipher.DECRYPT_MODE, secKey);
            byte[] plaintxt = cipher.doFinal(text);


            String plaintextString = new String(plaintxt);
          //  System.out.println("Plaintext: " +  plaintextString);


            System.out.println("Openning file to write: " + outFile);

            FileOutputStream outToFile = new FileOutputStream(outFile);
            outToFile.write(plaintxt);
            outToFile.close();
            rawDataFromFile.close();
            System.out.println(inFile + " encrypted as " + outFile);



        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void decryptAESCTR() {

        // ex1
        // hexKey = args[1];
        // inFile = args[2];
        // outFile = args[3];

        try {
            // getting the data from the file
            RandomAccessFile rawDataFromFile = new RandomAccessFile(inFile, "r");
            byte[] cipherText = new byte[(int) rawDataFromFile.length()];
            rawDataFromFile.read(cipherText);
            rawDataFromFile.close();
            System.out.println(cipherText);
            Cipher cipher = Cipher.getInstance("AES/CTR/PKCS5Padding");
            SecretKeySpec secretKeySpec = new SecretKeySpec(hexStringToByteArray(hexKey), "AES");

            // seperating the data from the IV and copy to new byte arrays
            byte iv[] = new byte[16];
            byte text[] = new byte[cipherText.length - 16];
            System.arraycopy(cipherText, 0, iv, 0, 16);
            System.arraycopy(cipherText, 16, text, 0, cipherText.length - 16);

            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            // System.out.println("IV: " + iv );

            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec);
            byte[] plainByte = cipher.doFinal(text);

            // String plaintextString = new String(plainByte);
            // System.out.println("Plaintext: " + plaintextString);

            System.out.println("Openning file to write: " + outFile);

            FileOutputStream outToFile = new FileOutputStream(outFile);
            outToFile.write(plainByte);
            outToFile.close();
            System.out.println(inFile + " encrypted as " + outFile);

        } catch (Exception e) {
            System.out.println(e);
        }

    }

    private static void decryptAESCCM() {
        try {
            // getting the data from the file
            RandomAccessFile rawDataFromFile = new RandomAccessFile(inFile, "r");
            byte[] cipherText = new byte[(int) rawDataFromFile.length()];
            rawDataFromFile.read(cipherText);
            rawDataFromFile.close();
            System.out.println(cipherText);
            // Cipher cipher = Cipher.getInstance("AES/CCM/PKCS5Padding");
            SecretKeySpec secretKeySpec = new SecretKeySpec(hexStringToByteArray(hexKey), "AES");
            Security.insertProviderAt(new BouncyCastleProvider(), 1);
            Cipher cipher = Cipher.getInstance("AES/CCM/NoPadding", "BC");

            // seperating the data from the IV and copy to new byte arrays
            byte iv[] = new byte[10];
            byte text[] = new byte[cipherText.length - 10];
            System.arraycopy(cipherText, 0, iv, 0, 10);
            System.arraycopy(cipherText, 10, text, 0, cipherText.length - 10);

            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            // System.out.println("IV: " + iv );

            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec);
            byte[] plainByte = cipher.doFinal(text);

            // String plaintextString = new String(plainByte);
            // System.out.println("Plaintext: " + plaintextString);

            System.out.println("Openning file to write: " + outFile);

            FileOutputStream outToFile = new FileOutputStream(outFile);
            outToFile.write(plainByte);
            outToFile.close();
            System.out.println(inFile + " decrypted as " + outFile);

        } catch (Exception e) {
            System.out.println(e);
        }

    }

    private static void generateKey() {
        try {
            KeyGenerator sGen = KeyGenerator.getInstance("AES");
            Key aesKey = sGen.generateKey();
            System.out.println(
                    "Here are some bytes you can use as an AES key: " + byteArrayToHexString(aesKey.getEncoded()));
        } catch (Exception e) {
            System.out.println("doh " + e);
        }
    }

    private static void encryptAESCTR() {
        try {
            // Open and read the input file
            // N.B. this program reads the whole file into memory, not good for large
            // programs!
            RandomAccessFile rawDataFromFile = new RandomAccessFile(inFile, "r");
            byte[] plainText = new byte[(int) rawDataFromFile.length()];
            rawDataFromFile.read(plainText);
            rawDataFromFile.close();

            // Set up the AES key & cipher object in CTR mode
            SecretKeySpec secretKeySpec = new SecretKeySpec(hexStringToByteArray(hexKey), "AES");
            Cipher encAESCTRcipher = Cipher.getInstance("AES/CTR/PKCS5Padding");
            SecureRandom random = new SecureRandom();
            byte iv[] = new byte[16];
            random.nextBytes(iv);
            // System.out.println("IV: " + iv );
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            encAESCTRcipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec);

            // Encrypt the data
            byte[] cipherText = encAESCTRcipher.doFinal(plainText);

            // Write file to disk
            System.out.println("Openning file to write: " + outFile);
            FileOutputStream outToFile = new FileOutputStream(outFile);
            outToFile.write(iv);
            outToFile.write(cipherText);
            outToFile.close();
            System.out.println(inFile + " encrypted as " + outFile);
        } catch (Exception e) {
            System.out.println("doh " + e);
        }
    }

    private static void encryptAESCCM() {
        try {
            // Open and read the input file
            // N.B. this program reads the whole file into memory, not good for large
            // programs!
            RandomAccessFile rawDataFromFile = new RandomAccessFile(inFile, "r");
            byte[] plainText = new byte[(int) rawDataFromFile.length()];
            rawDataFromFile.read(plainText);
            rawDataFromFile.close();

            // Set up the AES key & cipher object in CCM mode
            SecretKeySpec secretKeySpec = new SecretKeySpec(hexStringToByteArray(hexKey), "AES");
            // Add a security provider that actually does provide CCM mode
            Security.insertProviderAt(new BouncyCastleProvider(), 1);
            Cipher encAESCCMcipher = Cipher.getInstance("AES/CCM/NoPadding", "BC");
            SecureRandom random = new SecureRandom();
            byte iv[] = new byte[10]; // BC needs us to leave room for the counter
            random.nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            encAESCCMcipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec);

            // Encrypt the data
            byte[] cipherText = encAESCCMcipher.doFinal(plainText);

            // Write file to disk
            System.out.println("Openning file to write: " + outFile);
            FileOutputStream outToFile = new FileOutputStream(outFile);
            outToFile.write(iv);
            outToFile.write(cipherText);
            outToFile.close();
            System.out.println(inFile + " encrypted as " + outFile);
        } catch (Exception e) {
            System.out.println("doh " + e);
        }
    }

    // Code from
    // http://www.anyexample.com/programming/java/java%5Fsimple%5Fclass%5Fto%5Fcompute%5Fmd5%5Fhash.xml
    private static String byteArrayToHexString(byte[] data) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                if ((0 <= halfbyte) && (halfbyte <= 9))
                    buf.append((char) ('0' + halfbyte));
                else
                    buf.append((char) ('a' + (halfbyte - 10)));
                halfbyte = data[i] & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

    // Code from http://javaconversions.blogspot.co.uk
    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}