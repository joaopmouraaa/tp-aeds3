import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.util.Random;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.nio.ByteBuffer;

class Key { // pública ou privada
    private BigInteger number;
    private BigInteger n;

    public Key(BigInteger number, BigInteger n) {
        this.number = number;
        this.n = n;
    }

    public BigInteger getNumber() {
        return number;
    }

    public BigInteger getN() {
        return n;
    }

}

class KeyPair {
    private Key pubKey;
    private Key privKey;
    private static final String MINPRIME = "10100";
    private static final String KEY_PAIR_FILE = "keyPair.bin";

    // Construtor do par de chaves que gera novas chaves e as salva em um arquivo
    public KeyPair() {
        Random rand = new Random();
        BigInteger[] pair = setPrimePair(rand);
        BigInteger n = pair[0].multiply(pair[1]);
        BigInteger z = pair[0].subtract(BigInteger.ONE).multiply(pair[1].subtract(BigInteger.ONE));
        setKeyPairs(n, z, rand);
        saveKeyPairToFile();
    }

    // Gera dois números primos grandes
    private BigInteger[] setPrimePair(Random rand) {
        BigInteger[] pair = new BigInteger[2];
        BigInteger prime1 = BigInteger.ZERO;
        BigInteger prime2 = BigInteger.ZERO;
        BigInteger minPrime = new BigInteger(MINPRIME);
        boolean isPrime = false;
        while (!isPrime) {
            prime1 = new BigInteger(1024, rand);
            if (prime1.compareTo(minPrime) < 0) {
                continue;
            }
            isPrime = prime1.isProbablePrime(100);
        }
        isPrime = false;
        while (!isPrime) {
            prime2 = new BigInteger(1024, rand);
            if (prime2.compareTo(minPrime) < 0 || prime2.equals(prime1)) {
                continue;
            }
            isPrime = prime2.isProbablePrime(100);
        }
        pair[0] = prime1;
        pair[1] = prime2;
        return pair;
    }

    // Define as chaves pública e privada
    private void setKeyPairs(BigInteger n, BigInteger z, Random rand) {
        boolean isCoprime = false;
        BigInteger privateKey;
        BigInteger publicKey = BigInteger.ZERO;
        while (!isCoprime) {
            publicKey = new BigInteger(1024, rand);
            publicKey = publicKey.add(BigInteger.ONE);
            if (publicKey.compareTo(z) < 0 && publicKey.gcd(z).equals(BigInteger.ONE)) {
                isCoprime = true;
            }
        }
        privateKey = publicKey.modInverse(z);
        pubKey = new Key(publicKey, n);
        privKey = new Key(privateKey, n);
    }

    // Descriptografa uma mensagem
    public String decrypt(String message) {
        String decryptedMessage = "";
        String[] parts = message.split("\\|");
        for (String part : parts) {
            if (part.equals("")) {
                continue;
            }
            BigInteger c = new BigInteger(part);
            decryptedMessage += (char) c.modPow(privKey.getNumber(), privKey.getN()).intValue();
        }
        return decryptedMessage;
    }

    // Assina uma mensagem com a chave privada
    public String sign(String message) {
        String hashed = sha256(message);
        BigInteger m = new BigInteger(hashed.getBytes());
        return m.modPow(privKey.getNumber(), privKey.getN()).toString();
    }

    // Verifica a assinatura de uma mensagem com a chave pública
    public boolean verify(String message, String signature, Key publicKey) {
        String hashed = sha256(message);
        BigInteger m = new BigInteger(hashed.getBytes());
        BigInteger s = new BigInteger(signature);
        return m.equals(s.modPow(publicKey.getNumber(), publicKey.getN()));
    }

    public Key getPublicKey() {
        return pubKey;
    }

    // Criptografa uma mensagem com a chave pública
    public String encryptWithPublicKey(String message, Key publicKey) {
        BigInteger m = new BigInteger(message.getBytes());
        return m.modPow(publicKey.getNumber(), publicKey.getN()).toString();
    }

    // Criptografa uma mensagem com a chave privada
    public String encryptWithPrivateKey(String message) {
        BigInteger m = new BigInteger(message.getBytes());
        return m.modPow(privKey.getNumber(), privKey.getN()).toString();
    }

    // Gera o hash SHA-256 de uma string
    public static String sha256(String entrada) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(entrada.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (int i = 0; i < encodedhash.length; i++) {
                String hex = Integer.toHexString(0xff & encodedhash[i]);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    // Criptografa um byte
    public byte[] encryptByte(byte b) {
        int unsignedByte = Byte.toUnsignedInt(b);
        BigInteger m = BigInteger.valueOf(unsignedByte);
        BigInteger encrypted = m.modPow(pubKey.getNumber(), pubKey.getN());
        // System.out.println("encryptByte - b value: "+b);
        // System.out.println("encryptByte - m value: "+m);
        // System.out.println("encryptByte - encrypted value: "+encrypted);
        // System.out.println("encryptByte - encrypted byte value: "+(byte) encrypted.intValue());
        // System.out.println("encryptByte - encrypted byte value unsigned: "+(byte) encrypted.intValue());
        // System.out.println("encryptByte - encrypted.toByteArray() value: "+encrypted.toByteArray());
        return encrypted.toByteArray();
    }

    // Descriptografa um byte
    public byte decryptByte(byte[] b) {
        BigInteger c = new BigInteger(b);
        BigInteger decrypted = c.modPow(privKey.getNumber(), privKey.getN());
        // System.out.println("decryptByte - b value: "+b);
        // System.out.println("decryptByte - c value: "+c);
        // System.out.println("decryptByte - decrypted value: "+decrypted);
        // System.out.println("decryptByte - decrypted byte value: "+(byte) decrypted.intValue());
        return (byte) decrypted.intValue();
    }

    // Salva o par de chaves em um arquivo
    private void saveKeyPairToFile() {
        try (RandomAccessFile file = new RandomAccessFile(KEY_PAIR_FILE, "rw")) {
            file.setLength(0);
            writeKeyToFile(file, pubKey);
            writeKeyToFile(file, privKey);
            System.out.println("Par de chaves salvas.");
        } catch (IOException e) {
            System.err.println("Erro ao salvar par de chaves: " + e.getMessage());
        }
    }

    // Escreve uma chave em um arquivo
    private void writeKeyToFile(RandomAccessFile file, Key key) throws IOException {
        byte[] numberBytes = key.getNumber().toByteArray();
        byte[] nBytes = key.getN().toByteArray();

        file.writeInt(numberBytes.length);
        file.write(numberBytes);

        file.writeInt(nBytes.length);
        file.write(nBytes);
    }

    // Carrega o par de chaves de um arquivo
    public static KeyPair loadKeyPairFromFile() {
        try (RandomAccessFile file = new RandomAccessFile(KEY_PAIR_FILE, "r")) {
            Key pubKey = readKeyFromFile(file);
            Key privKey = readKeyFromFile(file);
            return new KeyPair(pubKey, privKey);
        } catch (IOException e) {
            System.err.println("Erro ao carregar par de chaves do arquivo: " + e.getMessage());
            return null;
        }
    }

    // Lê uma chave de um arquivo
    private static Key readKeyFromFile(RandomAccessFile file) throws IOException {
        int numberLength = file.readInt();
        byte[] numberBytes = new byte[numberLength];
        file.readFully(numberBytes);
        BigInteger number = new BigInteger(numberBytes);

        int nLength = file.readInt();
        byte[] nBytes = new byte[nLength];
        file.readFully(nBytes);
        BigInteger n = new BigInteger(nBytes);

        return new Key(number, n);
    }

    // Construtor do par de chaves com chaves específicas
    public KeyPair(Key pubKey, Key privKey) {
        this.pubKey = pubKey;
        this.privKey = privKey;
    }

}