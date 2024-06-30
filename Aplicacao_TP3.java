import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
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
import java.util.HashMap;

public class Aplicacao {    
    public static void main(String[] args) throws IOException{
        // Input do usuário
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        // Nomes dos arquivos
        String binaryFilePath = "./binario.bin";
        String csvFilePath = "./TABELA-FINAL.csv";
        // String csvFilePath = "./TABELA-PEQUENA.csv"; // para debug e demonstração
        String arquivoVersao = "./versao.bin";
        String arquivoCodigos = "./codigosHuffman.bin";
        // Declarando variáveis que serão manipuladas posteriormente em escopos diferentes
        long tamanhoOriginal = 0;
        HashMap<Character, String> codigos = loadCodes(arquivoCodigos);
        // Verifica consistência de arquivos
        verificaArquivos(binaryFilePath, arquivoVersao);
        // Armazena variáveis da versão atual dos arquivos comprimidos e o contador de registros do arquivo binário
        int versao = getVersao(arquivoVersao);
        System.out.print("Verificando arquivo de registros \""+binaryFilePath+"\"... ");
        CRUD.inicializarContadorDeRegistros(binaryFilePath);
        if (CRUD.getNumeroRegistros(binaryFilePath) == 0) {
            // Caso o número de registros seja 0 no arquivo binário, cria registros a partir do arquivo CSV
            System.out.print("\nCriando registros a partir do diretório padrão: \""+csvFilePath+"\"... ");
            CRUD.createDatabaseFromCSV(csvFilePath, binaryFilePath);
            System.out.println(CRUD.getNumeroRegistros(binaryFilePath)+" registros criados com sucesso.");
        } else {
            System.out.println("Total: " + CRUD.getNumeroRegistros(binaryFilePath) + " registros. ");
        }

        char choice = '0';
        while (choice != '5') {
            System.out.println("----------------------------- MENU ------------------------------");
            System.out.println("Escolha uma opção:");
            System.out.println("1. Ler registro por ID");
            System.out.println("2. Ler todos os registros");
            System.out.println("3. Compactar Huffman e LZW");
            System.out.println("4. Descompactar Huffman e LZW");
            System.out.println("5. Salvar e sair");
            System.out.println("-----------------------------------------------------------------");
            System.out.print("Digite sua escolha: ");
            choice = reader.readLine().charAt(0);
            switch (choice) {
                case '1':
                    System.out.print("Digite o ID do registro que deseja ler: ");
                    int IntIdRead = Integer.parseInt(reader.readLine());
                    System.out.println("Buscando no arquivo de registros, na posição " + IntIdRead + ".");
                    CRUD.readById(binaryFilePath, IntIdRead);
                    break;
                case '2':
                    System.out.println("Lendo todos os registros...");
                    CRUD.readAll(binaryFilePath);
                    break;                    
                case '3':
                    // Salva o valor do tamanho do arquivo binario original e a versão dos arquivos compactados
                    tamanhoOriginal = new RandomAccessFile("binario.bin", "r").length();
                    versao = getVersao(arquivoVersao);
                    String nomeArquivoCompactado = "binarioHuffmanCompressao"+versao+".bin";
                    String nomeArquivoCompactadoLZW = "binarioLZWCompressao"+versao+".bin";
                    // Huffman
                    System.out.print("\nCompactando Huffman... ");
                    long huffman_tempo_compact = System.currentTimeMillis(); // tempo inicial
                    String mensagem = CRUD.BinaryToString(binaryFilePath);
                    // System.out.println("\nMensagem Huffman: \n"+mensagem+"\n"); // PARA DEBUG
                    codigos = Huffman.generateCodes(mensagem);
                    RandomAccessFile rafHuffman = new RandomAccessFile("codigosHuffman.bin", "rw");
                    for (char c : codigos.keySet()) {
                        rafHuffman.writeChar(c);
                        rafHuffman.writeUTF(codigos.get(c));
                    }
                    // System.out.println("Dicionário Huffman: "+codigos+"\n"); // PARA DEBUG
                    String mensagemCodificada = Huffman.codifica(mensagem, codigos);
                    int length = mensagemCodificada.length();
                    while (length % 8 != 0) { // completando com bits para formar o último byte
                        mensagemCodificada += "0";
                        length++;
                    }
                    // System.out.println("Mensagem codificada Huffman: \n"+mensagemCodificada+"\n"); // PARA DEBUG
                    byte[] bytes = new byte[length/8];
                    for (int i = 0; i < mensagemCodificada.length(); i+=8) { // alocando bytes no vetor a partir da string mensagemCodificada
                        String byteString = mensagemCodificada.substring(i, i+8);
                        bytes[i / 8] = (byte) Integer.parseInt(byteString, 2);
                    }
                    RandomAccessFile raf = new RandomAccessFile(nomeArquivoCompactado, "rw");
                    for (byte b : bytes) { // escrevendo os bytes no arquivo comprimido
                        raf.writeByte(b);
                    }
                    raf.close();
                    long huffman_tempo_compact_final = System.currentTimeMillis() - huffman_tempo_compact;;
                    System.out.println("Sucesso na compactação Huffman.");

                    // LZW
                    System.out.print("Compactando LZW... ");
                    long lzw_tempo_compact = System.currentTimeMillis(); // tempo inicial
                    byte[] binaryBytes = CRUD.getBytesFromFile(binaryFilePath);
                    byte[] msgBytes = binaryBytes;
                    // {
                    //     System.out.println("\nMensagem LZW (bytes): \n"+binaryBytes); // PARA DEBUG
                    //     StringBuilder sb = new StringBuilder(); // PARA DEBUG
                    //     for (byte b : msgBytes) { // PARA DEBUG
                    //         sb.append((char)b); // PARA DEBUG
                    //     } // PARA DEBUG
                    //     System.out.println("Mensagem LZW (formato String): \n"+sb); // PARA DEBUG
                    // }
                    byte[] mensagemCodificadaLZW = null;
                    try {
                        mensagemCodificadaLZW = LZW.codifica(msgBytes);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    RandomAccessFile rafLZW = new RandomAccessFile(nomeArquivoCompactadoLZW, "rw");
                    for (byte b : mensagemCodificadaLZW) { // escrevendo os bytes no arquivo comprimido
                        rafLZW.writeByte(b);
                    }
                    rafLZW.close();
                    long lzw_tempo_compact_final = System.currentTimeMillis() - lzw_tempo_compact;
                    System.out.println("Sucesso na compressão LZW.");

                    // Incrementa a versão os arquivos comprimidos
                    atualizaVersao(arquivoVersao);

                    // Análise
                    long tamanhoFinalHuffman = new RandomAccessFile("binarioHuffmanCompressao"+versao+".bin", "r").length();
                    double taxaCompressaoHuffman = (1 - (double) tamanhoFinalHuffman / tamanhoOriginal) * 100;
                    long tamanhoFinalLZW = new RandomAccessFile("binarioLZWCompressao"+versao+".bin", "r").length();
                    double taxaCompressaoLZW = (1 - (double) tamanhoFinalLZW / tamanhoOriginal) * 100;

                    // Resultados da Compressão
                    System.out.println("\n------------------- RESULTADOS DA COMPRESSAO --------------------");
                    System.out.println("Tamanho original: " + tamanhoOriginal + " bytes.");
                    System.out.println("--------------------------- HUFFFMAN ----------------------------");
                    System.out.println("Tamanho final Huffman: " + tamanhoFinalHuffman + " bytes.");
                    System.out.println("Tempo de compressão Huffman: " + huffman_tempo_compact_final + " ms.");
                    System.out.printf("Taxa de compressão Huffman: %.2f%%.%n", taxaCompressaoHuffman);
                    System.out.println("----------------------------- LZW -------------------------------");
                    System.out.println("Tamanho final LZW: " + tamanhoFinalLZW + " bytes.");
                    System.out.println("Tempo de compressão LZW: " + lzw_tempo_compact_final + " ms.");
                    System.out.printf("Taxa de compressão LZW: %.2f%%.%n", taxaCompressaoLZW);
                    System.out.println("-------------------------- COMPARAÇÃO ---------------------------");
                    if (tamanhoFinalHuffman < tamanhoFinalLZW) {
                        double percentualMaisEficiente = ((1 - (double) tamanhoFinalHuffman / tamanhoFinalLZW) * 100);
                        System.out.printf("A compressão Huffman foi %.2f%% mais eficiente.%n", percentualMaisEficiente);
                    } else {
                        double percentualMaisEficiente = ((1 - (double) tamanhoFinalLZW / tamanhoFinalHuffman) * 100);
                        System.out.printf("A compressão LZW foi %.2f%% mais eficiente.%n", percentualMaisEficiente);
                    }
                    if (huffman_tempo_compact_final < lzw_tempo_compact_final) {
                        double percentualMaisRapido = ((1 - (double) huffman_tempo_compact_final / lzw_tempo_compact_final) * 100);
                        System.out.printf("A compressão Huffman foi %.2f%% mais rápida.%n", percentualMaisRapido);
                    } else {
                        double percentualMaisRapido = ((1 - (double) lzw_tempo_compact_final / huffman_tempo_compact_final) * 100);
                        System.out.printf("A compressão LZW foi %.2f%% mais rápida.%n", percentualMaisRapido);
                    }
                    System.out.println("-----------------------------------------------------------------\n");
                    break;
                case '4':
                    // Carregando variáveis que serão usadas na análise pós-descompressão
                    tamanhoOriginal = new RandomAccessFile("binario.bin", "r").length();
                    String shabinaryOriginal = sha256(CRUD.BinaryToString(binaryFilePath));
                    // Verificando e listando os arquivos comprimidos
                    int numeroVersoes = getVersao(arquivoVersao);
                    boolean compactado = false;
                    for (int i = 1; i <= numeroVersoes; i++) {
                        if (new java.io.File("binarioHuffmanCompressao"+i+".bin").exists() && new java.io.File("binarioLZWCompressao"+i+".bin").exists()) {
                            compactado = true;
                            break;
                        }
                    }
                    if (compactado) {
                        for (int i = 1; i <= 15; i++) {
                            if (new java.io.File("binarioHuffmanCompressao"+i+".bin").exists() && new java.io.File("binarioLZWCompressao"+i+".bin").exists()) {
                                System.out.println("Versão "+i+" encontrada. Arquivos: ");
                                System.out.println("-binarioHuffmanCompressao"+i+".bin");
                                System.out.println("-binarioLZWCompressao"+i+".bin");
                            }
                        }
                    }
                    if (!compactado) {
                        System.out.println("Nenhum arquivo compactado encontrado.");
                        break;
                    }
                    // Escolha da versão
                    System.out.println("Escolha a versão a ser descompactada:");
                    System.out.print("Versão: ");
                    String escolhaVersao = reader.readLine();
                    int versaoEscolhida = 0;
                    try {
                        versaoEscolhida = Integer.parseInt(escolhaVersao);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    if (versaoEscolhida < 1 || versaoEscolhida >= numeroVersoes) {
                        System.out.println("Versão inválida. Tente novamente.");
                        break;
                    }
                    // Descompressão
                    String input = "binarioHuffmanCompressao"+versaoEscolhida+".bin";
                    String output = "binario.bin";
                    // String output = "binarioLZW.bin"; // PARA DEBUG
                    // Huffman
                    System.out.print("\nDescompactando Huffman... ");
                    long huffman_tempo_descompact = System.currentTimeMillis(); // tempo inicial
                    RandomAccessFile raf2 = new RandomAccessFile(input, "r");
                    byte[] conteudoArquivo = new byte[(int) raf2.length()]; // aloca um vetor de bytes do tamanho do arquivo
                    raf2.readFully(conteudoArquivo);
                    raf2.close();
                    StringBuilder codificada = new StringBuilder();
                    for (byte b : conteudoArquivo) { // Converte o vetor de bytes em uma string
                        codificada.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
                    }
                    // System.out.println("Mensagem codificada: \n"+codificada); // PARA DEBUG
                    String mensagemDescompactada = Huffman.decodifica(codificada.toString(), codigos);
                    // System.out.println("Mensagem descompactada: \n"+mensagemDescompactada); // PARA DEBUG
                    CRUD.StringToBinary(mensagemDescompactada, output);
                    long huffman_tempo_descompact_final = System.currentTimeMillis() - huffman_tempo_descompact;
                    System.out.println("Sucesso na descompactação Huffman.");
                    long tamanhoFinal1 = new RandomAccessFile("binario.bin", "r").length();
                    String shaBinaryHuffman = sha256(CRUD.BinaryToString("binario.bin"));

                    // LZW
                    String inputLZW = "binarioLZWCompressao"+versaoEscolhida+".bin";
                    // String outputLZW = "binarioLZW.bin"; // PARA DEBUG
                    String outputLZW = "binario.bin";
                    System.out.print("Descompactando LZW... ");
                    long lzw_tempo_descompact = System.currentTimeMillis(); // tempo inicial
                    RandomAccessFile raf2LZW = new RandomAccessFile(inputLZW, "r");
                    byte[] conteudoArquivoLZW = new byte[(int) raf2LZW.length()]; // aloca um vetor de bytes do tamanho do arquivo
                    raf2LZW.readFully(conteudoArquivoLZW);
                    raf2LZW.close();
                    byte[] mensagemDescompactadaLZW = null;
                    try {
                        mensagemDescompactadaLZW = LZW.decodifica(conteudoArquivoLZW);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // {
                    //     System.out.println("\n\nMensagem descompactada LZW (bytes): \n"+mensagemDescompactadaLZW); // PARA DEBUG
                    //     StringBuilder sb = new StringBuilder(); // PARA DEBUG
                    //     for (byte b : mensagemDescompactadaLZW) { // PARA DEBUG
                    //         sb.append((char)b); // PARA DEBUG
                    //     } // PARA DEBUG
                    //     System.out.println("Mensagem descompactada LZW (formato String): \n"+sb+"\n"); // PARA DEBUG
                    // }

                    RandomAccessFile rafLZW2 = new RandomAccessFile(outputLZW, "rw");
                    for (byte b : mensagemDescompactadaLZW) {
                        rafLZW2.writeByte(b);
                    }
                    long lzw_tempo_descompact_final = System.currentTimeMillis() - lzw_tempo_descompact;; // tempo inicial
                    System.out.println("Sucesso na descompactação LZW.");
                    long tamanhoFinal2 = new RandomAccessFile("binario.bin", "r").length();
                    String shaBinaryLZW = sha256(CRUD.BinaryToString("binario.bin"));

                    boolean integridade = ((shabinaryOriginal.equals(shaBinaryHuffman)) && (shabinaryOriginal.equals(shaBinaryLZW)));
                    
                    // Resultados da Descompressão
                    System.out.println("\n------------------ RESULTADOS DA DESCOMPRESSAO ------------------");
                    System.out.println("Tamanho do arquivo \"binario.bin\" após a descompactação:");
                    System.out.println("-> " + tamanhoFinal1 + " bytes após a manipulação com Huffman.");
                    System.out.println("-> " + tamanhoFinal2 + " bytes após a manipulação com LZW.");
                    System.out.println("-------------------------- INTEGRIDADE --------------------------");
                    System.out.println("SHA256 do binario.bin original: \n"+shabinaryOriginal);
                    System.out.println("SHA256 do binario.bin após descompressão Huffman: \n"+shaBinaryHuffman);
                    System.out.println("SHA256 do binario.bin após descompressão LZW: \n"+shaBinaryLZW);
                    System.out.println("Ao comparar entre si as strings SHA256 do arquivo \"binario.bin\"");
                    System.out.println("antes e após cada descompactação, podemos concluir que");
                    System.out.println((integridade ? "não " : "") + "houve perda de integridade do \"binario.bin\" original.");
                    System.out.println("---------------------------- HUFFMAN ----------------------------");
                    System.out.println("Tempo de descompactação Huffman: " + huffman_tempo_descompact_final + " ms.");
                    System.out.println("------------------------------ LZW ------------------------------");
                    System.out.println("Tempo de descompactação LZW: " + lzw_tempo_descompact_final + " ms.");
                    System.out.println("-------------------------- COMPARAÇÃO ---------------------------");
                    if (huffman_tempo_descompact_final < lzw_tempo_descompact_final) {
                        double percentualMaisRapido = ((1 - (double) huffman_tempo_descompact_final / lzw_tempo_descompact_final) * 100);
                        System.out.printf("A descompactação Huffman foi %.2f%% mais rápida.%n", percentualMaisRapido);
                    } else {
                        double percentualMaisRapido = ((1 - (double) lzw_tempo_descompact_final / huffman_tempo_descompact_final) * 100);
                        System.out.printf("A descompactação LZW foi %.2f%% mais rápida.%n", percentualMaisRapido);
                    }
                    System.out.println("-----------------------------------------------------------------\n");
                    break;
                case '5':
                    System.out.println("Salvando e saindo...");
                    break;
                default:
                    System.out.println("Opção inválida. Tente novamente.");
            }
        }
    }

    public static void atualizaVersao(String arquivoVersao) {
        try {
            RandomAccessFile raf = new RandomAccessFile(arquivoVersao, "rw");
            raf.seek(0);
            int versao = raf.readInt();
            if (versao > 10) {
                System.out.println("Erro: você alcançou o limite de arquivos comprimidos! Limpando arquivos compactados...");
                deleteCompressedFiles();
                versao = 0;
            }
            raf.seek(0);
            raf.writeInt(versao + 1);
            raf.close();
        } catch (IOException e) {
            try {
                RandomAccessFile raf = new RandomAccessFile(arquivoVersao, "rw");
                raf.writeInt(1);
                raf.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
    }

    public static int getVersao(String arquivoVersao) {
        try {
            RandomAccessFile raf = new RandomAccessFile(arquivoVersao, "r");
            if (raf.length() == 0) {
                atualizaVersao(arquivoVersao);
            }
            int versao = raf.readInt();
            raf.close();
            return versao;
        } catch (FileNotFoundException e) {
            atualizaVersao(arquivoVersao);
            return 1;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

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

    public static String convertMillisToDate(long dateInMilliseconds) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date(dateInMilliseconds);
        return sdf.format(date);
    }

    public static void verificaArquivos(String binaryFilePath, String versionPath) {
        boolean binaryFileExists = false;
        boolean versionFileExists = false;
        try (RandomAccessFile raf = new RandomAccessFile(binaryFilePath, "r")) {
            if (raf.length() != 0) binaryFileExists = true;
        } catch (IOException e) {}
        try (RandomAccessFile raf2 = new RandomAccessFile(versionPath, "rw")) {
            int i = 0;
            if (raf2.length() != 0) {
                versionFileExists = true;
                i = getVersao(versionPath);
                for (int j = (i-1); j > 0; j--) {
                    File huffmanCompressedFile = new File("binarioHuffmanCompressao"+j+".bin");
                    File lzwCompressedFile = new File("binarioLZWCompressao"+j+".bin");
                    if (huffmanCompressedFile.length() == 0 || lzwCompressedFile.length() == 0) {
                        System.out.println("Inconsistência entre arquivos compactados identificada. Limpando arquivos compactados...");
                        deleteCompressedFiles();
                        raf2.seek(0);
                        raf2.writeInt(1);
                        break;
                    }
                }
            }
            if (!binaryFileExists) {
                raf2.setLength(0);
                deleteCompressedFiles();
            }
        } catch (IOException e) {}
        if (binaryFileExists && !versionFileExists) {
            deleteCompressedFiles();
            atualizaVersao(versionPath);
        }
    }

    public static void deleteCompressedFiles() {
        for(int i = 15; i >= 0; i--) {
            File huffmanCompressedFile = new File("binarioHuffmanCompressao"+i+".bin");
            File lzwCompressedFile = new File("binarioLZWCompressao"+i+".bin");
            huffmanCompressedFile.delete();
            lzwCompressedFile.delete();
        }
    }

    public static HashMap<Character, String> loadCodes(String arquivoCodigos) {
        HashMap<Character, String> codigos = new HashMap<>();
        try (RandomAccessFile raf = new RandomAccessFile(arquivoCodigos, "r")) {
            while (raf.getFilePointer() < raf.length()){
                char c = raf.readChar();
                String s = raf.readUTF();
                codigos.put(c, s);
            }
            return codigos;
        } catch (FileNotFoundException e) {
            return new HashMap<>();
        } catch (IOException e) {
            return new HashMap<>();
        }
    }

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