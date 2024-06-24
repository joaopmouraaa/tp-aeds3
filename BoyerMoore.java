import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class BoyerMoore {

    private BoyerMoore() { // <- construtor privado - sugestão do SonarLint
        throw new IllegalStateException("Uma classe utilitária não deve ser instanciada.");
    }

    public static List<Integer> boyerMoore(String mensagem, String padrao) {
        int tamanhoMensagem = mensagem.length(); // Comprimento do texto
        int tamanhoPadrao = padrao.length(); // Comprimento do padrão
        List<Integer> ocorrencias = new ArrayList<>(); // Lista para armazenar os índices das ocorrências do padrão no texto

        // Se o padrão é maior que o texto, não há como encontrar o padrão no texto
        if (tamanhoPadrao > tamanhoMensagem) {
            System.out.println("O padrão é maior que o texto.");
            return ocorrencias;
        }

        // Calcula as heurísticas de Caractere Ruim e Sufixo Bom
        int[] caractereRuim = HeuristicaCaractereRuim(padrao, tamanhoPadrao);
        int[] sufixoBom = HeuristicaSufixoBom(padrao, tamanhoPadrao);

        int indice = 0; // Deslocamento do padrão em relação ao texto

        // Loop para percorrer o texto
        while (indice <= (tamanhoMensagem - tamanhoPadrao)) { 
            int j = tamanhoPadrao - 1; // Começa a comparar do final do padrão

            // Continua reduzindo j enquanto os caracteres do padrão e do texto coincidirem
            while (j >= 0 && padrao.charAt(j) == mensagem.charAt(indice + j)) { 
                j--;
            }

            // Se o padrão foi encontrado (j < 0)
            if (j < 0) {
                ocorrencias.add(indice); // Adiciona o índice onde o padrão foi encontrado à lista de ocorrências
                indice++;
            } else { // Define o deslocamento com base nas heurísticas
                indice += Math.max(1, j - caractereRuim[mensagem.charAt(indice + j)]);
                indice = Math.max(indice, j - sufixoBom[j]);
            }
        }
        return ocorrencias; // Retorna a lista de ocorrências
    }

    private static int[] HeuristicaCaractereRuim(String padrao, int tamanho) {
        int[] caractereRuim = new int[256]; // ASCII
        Arrays.fill(caractereRuim, -1); // Inicializa com -1

        // Preenche o array com o índice do último caractere do padrão
        for (int i = 0; i < tamanho; i++) { 
            caractereRuim[padrao.charAt(i)] = i;
        }

        return caractereRuim;
    }

    private static int[] HeuristicaSufixoBom(String padrao, int tamanho) {
        int[] sufixoBom = new int[tamanho]; // Array para armazenar os deslocamentos de Sufixo Bom
        int[] sufixo = new int[tamanho]; // Array auxiliar para armazenar os sufixos do padrão

        Arrays.fill(sufixoBom, tamanho); // Inicializa todas as posições do array com o valor do tamanho do padrão
        Arrays.fill(sufixo, -1); // Inicializa todas as posições do array auxiliar com -1

        // Preenche o array de sufixos
        for (int i = tamanho - 1; i >= 0; i--) {
            int j = i;
            while (j >= 0 && padrao.charAt(j) == padrao.charAt(tamanho - 1 - i + j)) {
                sufixo[i] = j;
                j--;
            }
        }

        // Atualiza o array de Sufixo Bom baseado nos sufixos encontrados
        for (int i = 0; i < tamanho - 1; i++) {
            int j = sufixo[i];
            if (j != -1 && i - j < tamanho - 1) {
                sufixoBom[tamanho - 1 - (i - j)] = tamanho - 1 - i;
            }
        }

        return sufixoBom;
    }
}
