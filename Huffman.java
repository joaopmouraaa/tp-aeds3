import java.util.HashMap;
import java.util.PriorityQueue;

class HuffmanNode implements Comparable<HuffmanNode> {
    char caractere;
    int frequencia;
    HuffmanNode esquerdo;
    HuffmanNode direito;

    public HuffmanNode(char caractere, int frequencia) {
        this.caractere = caractere;
        this.frequencia = frequencia;
        esquerdo = direito = null;
    }

    @Override
    public int compareTo(HuffmanNode node) {
        return this.frequencia - node.frequencia;
    }

}

public class Huffman {

    Huffman() {
    }

    public static HashMap<Character, String> generateCodes(String mensagem) {
        HashMap<Character, Integer> frequencias = new HashMap<>();
        for(char c : mensagem.toCharArray()) {
            frequencias.put(c, frequencias.getOrDefault(c, 0)+1);
        }

        PriorityQueue<HuffmanNode> pq = new PriorityQueue<>();
        for(char c : frequencias.keySet()) {
            pq.add(new HuffmanNode(c, frequencias.get(c)));
        }

        while(pq.size()>1) {
            HuffmanNode esquerdo = pq.poll();
            HuffmanNode direito = pq.poll();
            HuffmanNode pai = new HuffmanNode('\0', esquerdo.frequencia + direito.frequencia);
            pai.esquerdo = esquerdo;
            pai.direito = direito;
            pq.add(pai);
        }

        HuffmanNode raiz = pq.poll();
        HashMap<Character, String> codigos = new HashMap<>();
        buildCodes(raiz, "", codigos);
        return codigos;
    }

    public static void buildCodes(HuffmanNode no, String codigo, HashMap<Character, String> codigos) {
        if(no.caractere != '\0') {
            codigos.put(no.caractere, codigo);
            return;
        }
        buildCodes(no.esquerdo, codigo+"0", codigos);
        buildCodes(no.direito, codigo+"1", codigos);
    }

    public static String codifica(String mensagem, HashMap<Character, String> codigos) {
        StringBuilder mensagemCodificada = new StringBuilder();
        for(char c : mensagem.toCharArray())
            mensagemCodificada.append(codigos.get(c));
        return mensagemCodificada.toString();
    }

    public static String decodifica(String mensagemCodificada, HashMap<Character, String> codigos) {
        StringBuilder mensagemDecodificada = new StringBuilder();
        HuffmanNode raiz = buildTree(codigos);

        HuffmanNode no = raiz;
        for(int i=0; i<mensagemCodificada.length(); i++) {
            if(mensagemCodificada.charAt(i)=='0')
                no = no.esquerdo;
            else  
                no = no.direito;
            if(no.direito == null && no.esquerdo == null) {
                mensagemDecodificada.append(no.caractere);
                no = raiz;
            }
        }
        return mensagemDecodificada.toString();
    }

    public static HuffmanNode buildTree(HashMap<Character, String> codigos) {
        HuffmanNode raiz = new HuffmanNode('\0', 0);
        for(char c : codigos.keySet()) {
            String codigo = codigos.get(c);
            HuffmanNode no = raiz;
            for(int i=0; i<codigo.length(); i++) {
                if(codigo.charAt(i)=='0') {
                    if(no.esquerdo == null)
                        no.esquerdo = new HuffmanNode('\0',0);
                    no = no.esquerdo;
                } else {
                    if(no.direito == null)
                        no.direito = new HuffmanNode('\0', 0);
                    no = no.direito;
                }
            }
            no.caractere = c;
        }
        return raiz;
    }

}