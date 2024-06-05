import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;

import java.util.BitSet;

// Armazena uma sequência de números em um vetor de bits
// Cada número tem "bitsPorNumero" bits
class BitSequence {
    int bitsPorNumero; // quantidade de bits por número
    private int ultimoBit; // próximo bit a ser usado;
    private BitSet bs;  // vetor de bits

    public BitSequence(int bs) {
        bitsPorNumero = bs;
        ultimoBit=0;
        this.bs = new BitSet();
    }

    // Adiciona um número de "bitsPorNumero" bits
    public void add(int n) {
        int i = bitsPorNumero;
        while(i>0) {
            if(n%2==0)
                bs.clear(ultimoBit++);
            else
                bs.set(ultimoBit++);
            n = n >> 1;
            i--;
        }
    }

    // Recupera um número de "bitsPorNumero" bits na i-ésima posição
    public int get(int i) { // retorna o i-ésimo número
        int pos = i*bitsPorNumero;
        int n = 0;
        for(int j=0; j<bitsPorNumero; j++) {
            if(bs.get(pos+j))
                n += (int)Math.pow(2,j);
        }
        return n;
    }

    // Retorna a quantidade de números armazenada no BitSet
    public int size() {
        return ultimoBit/bitsPorNumero;
    }

    public byte[] getBytes() {
        return bs.toByteArray();
    }

    public void setBytes(int n, byte[] bytes) {
        ultimoBit = n*bitsPorNumero;
        bs = BitSet.valueOf(bytes);
    }
}


public class LZW {

    public static final int BITS_POR_INDICE = 12;

    public static byte[] codifica(byte[] msgBytes) throws Exception {

        ArrayList<ArrayList<Byte>> dicionario = new ArrayList<>(); // dicionario
        ArrayList<Byte> vetorBytes;  // auxiliar para cada elemento do dicionario
        ArrayList<Integer> saida = new ArrayList<>();

        // inicializa o dicionário
        byte b;
        for(int j=-128; j<128; j++) {
            b = (byte)j;
            vetorBytes = new ArrayList<>();
            vetorBytes.add(b);
            dicionario.add(vetorBytes);
        }

        int i=0;
        int indice=-1;
        int ultimoIndice;
        while(indice==-1 && i<msgBytes.length) { // testa se o último vetor de bytes não parou no meio caminho por falta de bytes
            vetorBytes = new ArrayList<>();
            b = msgBytes[i];
            vetorBytes.add(b);
            indice = dicionario.indexOf(vetorBytes);
            ultimoIndice = indice;

            while(indice!=-1 && i<msgBytes.length-1) {
                i++;
                b = msgBytes[i];
                vetorBytes.add(b);
                ultimoIndice = indice;
                indice = dicionario.indexOf(vetorBytes);

            }

            // acrescenta o último índice à saída
            saida.add(indice!=-1 ? indice : ultimoIndice);

            // acrescenta o novo vetor de bytes ao dicionário
            if(dicionario.size() < (Math.pow(2, BITS_POR_INDICE))) {
                dicionario.add(vetorBytes);
            }

        }

        // System.out.println("Indices");
        // System.out.println(saida);
        // System.out.println("Dicionário tem "+dicionario.size()+" elementos");
        
        BitSequence bs = new BitSequence(BITS_POR_INDICE);
        for(i=0; i<saida.size(); i++) {
            bs.add(saida.get(i));
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(bs.size());
        dos.write(bs.getBytes());
        return baos.toByteArray();
    }

    @SuppressWarnings("unchecked")
    public static byte[] decodifica(byte[] msgCodificada) throws Exception {

        ByteArrayInputStream bais = new ByteArrayInputStream(msgCodificada);
        DataInputStream dis = new DataInputStream(bais);
        int n = dis.readInt();
        byte[] bytes = new byte[msgCodificada.length-4];
        dis.read(bytes);
        BitSequence bs = new BitSequence(BITS_POR_INDICE);
        bs.setBytes(n, bytes);

        // Recupera os números do bitset
        ArrayList<Integer> entrada = new ArrayList<>();
        int i, j;
        for(i=0; i<bs.size(); i++) {
            j = bs.get(i);
            entrada.add(j);
        }

        // inicializa o dicionário
        ArrayList<ArrayList<Byte>> dicionario = new ArrayList<>(); // dicionario
        ArrayList<Byte> vetorBytes;  // auxiliar para cada elemento do dicionario
        byte b;
        for(j=-128; j<128; j++) {
            b = (byte)j;
            vetorBytes = new ArrayList<>();
            vetorBytes.add(b);
            dicionario.add(vetorBytes);
        }

        // Decodifica os números
        ArrayList<Byte> proximoVetorBytes;
        ArrayList<Byte> msgDecodificada = new ArrayList<>();
        i = 0;
        while( i< entrada.size() ) {

            // decodifica o número
            vetorBytes = (ArrayList<Byte>)(dicionario.get(entrada.get(i)).clone());
            msgDecodificada.addAll(vetorBytes);

            // decodifica o próximo número
            i++;
            if(i<entrada.size()) {
                // adiciona o vetor de bytes (+1 byte do próximo vetor) ao fim do dicionário
                if(dicionario.size()<Math.pow(2,BITS_POR_INDICE))
                    dicionario.add(vetorBytes);
                    
                proximoVetorBytes = dicionario.get(entrada.get(i));
                vetorBytes.add(proximoVetorBytes.get(0));
            }

        }

        byte[] msgDecodificadaBytes = new byte[msgDecodificada.size()];
        for(i=0; i<msgDecodificada.size(); i++)
            msgDecodificadaBytes[i] = msgDecodificada.get(i);
        return msgDecodificadaBytes;

    }
}

