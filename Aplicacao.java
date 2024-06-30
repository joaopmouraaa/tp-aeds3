import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Aplicacao {    
    public static void main(String[] args) throws IOException{
        // Input do usuário
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        // Nomes dos arquivos
        String binaryFilePath = "./binario.bin";
        String csvFilePath = "./TABELA-FINAL.csv";
        String encryptedFilePath = "./encrypted.bin";
        String decryptedFilePath = "./decrypted.bin";
        String keyPairPath = "./keyPair.bin";

        // Escolha do banco de dados
        System.out.println("O algoritmo RSA pode demorar de 2 a 3 minutos ao usar o banco completo.");
        System.out.println("1. Quero usar o banco completo (1007 registros).");
        System.out.println("2. Quero usar o banco pequeno (3 registros).");
        System.out.print("Digite sua escolha: ");
        char escolha = reader.readLine().charAt(0);
        if (escolha == '1') {
            csvFilePath = "./TABELA-FINAL.csv";
        } else if (escolha == '2') {
            csvFilePath = "./TABELA-PEQUENA.csv";
        } else {
            System.out.println("Opção inválida. Usando o banco completo.");
        }

        // Carrega a chave pública e privada
        KeyPair keyPair;
        File keyFile = new File(keyPairPath);
        if (keyFile.exists()) {
            keyPair = KeyPair.loadKeyPairFromFile();
            System.out.println("Par de chaves carregada do arquivo.");
        } else {
            keyPair = new KeyPair();
            System.out.println("Novo par de chaves criado e salvo no arquivo.");
        }

        // Verifica se o arquivo binário possui registros
        System.out.print("Verificando arquivo de registros \""+binaryFilePath+"\"... ");
        CRUD.inicializarContadorDeRegistros(binaryFilePath);
        if (CRUD.getNumeroRegistros(binaryFilePath) == 0) {
            // Caso o número de registros seja 0 no arquivo binário, cria registros a partir do arquivo CSV
            System.out.print("\nCriando registros a partir do diretório padrão: \""+csvFilePath+"\"... ");
            CRUD.createDatabaseFromCSV(csvFilePath, binaryFilePath, keyPair);
            System.out.println(CRUD.getNumeroRegistros(binaryFilePath)+" registros criados com sucesso.");
        } else {
            System.out.println("Total: " + CRUD.getNumeroRegistros(binaryFilePath) + " registros. ");
        }
        
        // Calcula o hash SHA-256 do arquivo binário
        String sha256 = sha256(CRUD.BinaryToString(binaryFilePath, keyPair));

        char choice = 'w';
        while (choice != '-') {
            System.out.println("----------------------------- MENU ------------------------------");
            System.out.println("Escolha uma opção:");
            System.out.println("1. Ler registro por ID");
            System.out.println("2. Ler todos os registros");
            System.out.println("3. Buscar padrão nos registros");
            System.out.println("4. Criptografar arquivo binario usando RSA");
            System.out.println("5. Descriptografar arquivo binario usando RSA");
            System.out.println("6. Criptografar registro usando RSA a partir do ID");
            System.out.println("7. Descriptografar registro usando RSA a partir do ID");
            System.out.println("8. Criptografar todos os registros usando RSA");
            System.out.println("9. Descriptografar todos os registros usando RSA");
            System.out.println("0. Consolidar registros em um novo arquivo binário");
            System.out.println("-. Salvar e sair");
            System.out.println("-----------------------------------------------------------------");
            System.out.print("Digite sua escolha: ");
            choice = reader.readLine().charAt(0);
            boolean cripto = false;

            switch (choice) {
                case '1':
                    // Lê um registro pelo ID
                    System.out.print("Digite o ID do registro que deseja ler: ");
                    int IntIdRead = Integer.parseInt(reader.readLine());
                    System.out.println("Buscando no arquivo de registros, na posição " + IntIdRead + ".");
                    CRUD.readById(binaryFilePath, IntIdRead, keyPair);
                    break;
                case '2':
                    // Lê todos os registros
                    System.out.println("Lendo todos os registros...");
                    CRUD.readAll(binaryFilePath, keyPair);
                    break;
                case '3':
                    // Busca um padrão nos registros usando Boyer-Moore
                    System.out.print("Digite o padrão que deseja buscar: ");
                    String padrao = reader.readLine();
                    String resposta = CRUD.readBoyerMoore(binaryFilePath, padrao, keyPair);
                    System.out.println(resposta);
                    if (resposta.length() == 0) {
                        System.out.println("Padrão não encontrado em nenhum registro.");
                    }
                    break;
                case '4':
                    // Criptografia do arquivo usando RSA
                    sha256 = sha256(CRUD.BinaryToString2(binaryFilePath));
                    System.out.println("SHA256 do arquivo binário: " + sha256);
                    System.out.print("Criptografando arquivo. "+ (escolha != 2 ? "Demora cerca de 2 minutos... " : ""));
                    long start = System.currentTimeMillis();
                    encryptFile(binaryFilePath, encryptedFilePath, keyPair);
                    long end = System.currentTimeMillis();
                    System.out.print("Concluído. ");
                    System.out.println("Tempo de execução: " + (end - start)/1000 + " segundos");
                    cripto = true;
                    break;
                case '5':
                    // Descriptografia do arquivo usando RSA
                    if (cripto == false) {
                        System.out.println("O arquivo ainda não foi criptografado. Criptografe o arquivo antes de descriptografá-lo.");
                        break;
                    }
                    System.out.print("Descriptografando arquivo. "+ (escolha != 2 ? "Demora cerca de 3 minutos... " : ""));
                    long start2 = System.currentTimeMillis();
                    decryptFile(encryptedFilePath, decryptedFilePath, keyPair);
                    long end2 = System.currentTimeMillis();
                    System.out.print("Concluído. ");
                    System.out.println("Tempo de execução: " + (end2 - start2)/1000 + " segundos");
                    String sha256Decrypted = sha256(CRUD.BinaryToString2(decryptedFilePath));
                    System.out.println("SHA256 do arquivo binário descriptografado: " + sha256Decrypted);
                    if (sha256.equals(sha256Decrypted)) {
                        System.out.println("Integridade do arquivo mantida.");
                    } else {
                        System.out.println("Integridade do arquivo comprometida.");
                    }
                    break;
                case '6':
                    // Criptografia de um registro específico
                    System.out.print("Digite o ID do registro que deseja criptografar: ");
                    int IntIdEncrypt = Integer.parseInt(reader.readLine());
                    System.out.println("Criptografando registro com ID " + IntIdEncrypt + "...");
                    CRUD.findAndEncrypt(IntIdEncrypt, binaryFilePath, keyPair);
                    break;
                case '7':
                    // Descriptografia de um registro específico
                    System.out.print("Digite o ID do registro que deseja descriptografar: ");
                    int IntIdDecrypt = Integer.parseInt(reader.readLine());
                    System.out.println("Descriptografando registro com ID " + IntIdDecrypt + "...");
                    CRUD.findAndDecrypt(IntIdDecrypt, binaryFilePath, keyPair);
                    break;
                case '8':
                    // Criptografia de todos os registros
                    System.out.print("Criptografando todos os registros. "+ (escolha != 2 ? "Demora cerca de 2 minutos... " : ""));
                    long start3 = System.currentTimeMillis();
                    CRUD.encryptAll(binaryFilePath, keyPair);
                    long end3 = System.currentTimeMillis();
                    System.out.print("Concluído. ");
                    System.out.println("Tempo de execução: " + (end3 - start3)/1000 + " segundos");
                    break;
                case '9':
                    // Descriptografia de todos os registros
                    System.out.print("Descriptografando todos os registros. "+ (escolha != 2 ? "Demora cerca de 3 minutos... " : ""));
                    long start4 = System.currentTimeMillis();
                    CRUD.decryptAll(binaryFilePath, keyPair);
                    long end4 = System.currentTimeMillis();
                    System.out.print("Concluído. ");
                    System.out.println("Tempo de execução: " + (end4 - start4)/1000 + " segundos");
                    break;
                case '0':
                    // Consolidação dos registros em um novo arquivo binário
                    System.out.println("Consolidando registros em um novo arquivo binário...");
                    CRUD.consolidate(binaryFilePath, keyPair);
                    break;
                case '-':
                    System.out.println("Salvando e saindo...");
                    CRUD.consolidate(binaryFilePath, keyPair);
                    break;
                default:
                    System.out.println("Opção inválida. Tente novamente.");
            }
        }
    }

    // Método para criptografar o arquivo
    public static void encryptFile(String inputFilePath, String encryptedFilePath, KeyPair keyPair) {
        try (RandomAccessFile inputFile = new RandomAccessFile(inputFilePath, "r"); RandomAccessFile encryptedFile = new RandomAccessFile(encryptedFilePath, "rw")) {

            byte[] buffer = new byte[1]; // Buffer para ler um byte de cada vez
            while (inputFile.read(buffer) != -1) {
                byte[] encryptedBytes = keyPair.encryptByte(buffer[0]);
                encryptedFile.writeInt(encryptedBytes.length); // Escreve o comprimento do array de bytes criptografados
                encryptedFile.write(encryptedBytes); // Escreve os bytes criptografados
            }

            System.out.println("File encrypted successfully.");
        } catch (IOException e) {
            System.err.println("Error during encryption: " + e.getMessage());
        }
    }

    // Método para descriptografar o arquivo
    public static void decryptFile(String encryptedFilePath, String decryptedFilePath, KeyPair keyPair) {
        try (RandomAccessFile encryptedFile = new RandomAccessFile(encryptedFilePath, "r"); RandomAccessFile decryptedFile = new RandomAccessFile(decryptedFilePath, "rw")) {
            boolean eof = false;
            while (!eof) {
                try {
                    int length = encryptedFile.readInt();
                    byte[] encryptedBytes = new byte[length];
                    encryptedFile.readFully(encryptedBytes);
                    byte decryptedByte = keyPair.decryptByte(encryptedBytes);
                    decryptedFile.writeByte(decryptedByte);
                } catch (IOException e) {
                    eof = true;
                }
            }
            System.out.println("File decrypted successfully.");
        } catch (IOException e) {
            System.err.println("Error during decryption: " + e.getMessage());
        }
    }

    // Converte uma data em string para milissegundos
    public static long convertDateToMillis(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        try {
            Date date = sdf.parse(dateString);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            return calendar.getTimeInMillis();
        } catch (ParseException e) {
            e.printStackTrace();
            return -1; // Retorna -1 em caso de erro de análise
        }
    }

    // Converte milissegundos para uma data em string
    public static String convertMillisToDate(long dateInMilliseconds) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date(dateInMilliseconds);
        return sdf.format(date);
    }

    // Calcula o hash SHA-256 de uma string
    public static String sha256(String entrada) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(entrada.getBytes(StandardCharsets.UTF_8));
            StringBuffer hexString = new StringBuffer();
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
        return null;
    }


}