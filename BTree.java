// import java.io.RandomAccessFile;
// import java.io.EOFException;
// import java.io.IOException;

// public class BTree {
//     // Declaração de atributos
//     private RandomAccessFile arq;
//     private Node raiz;
//     private int ordem;
//     private int quant;

//     // Construtor
//     public BTree(int ordem, String nomeArquivo) {
//         this.ordem = ordem;
//         this.raiz = null;
//         try{
//             arq = new RandomAccessFile(nomeArquivo, "rw");
//             arq.seek(0);
//             System.out.println("Árvore B criada com ordem: "+ordem);
//         }catch(Exception e){
//             System.out.println("Erro ao criar árvore B");
//         }
//         quant=0;
//     }

//     public void deserializar(String nomeArquivo) throws IOException {
//         try {
//             RandomAccessFile arquivo = new RandomAccessFile(nomeArquivo, "r");
//             raiz = deserializarNo(arquivo);
//             arquivo.close();
//         } catch (EOFException e) {
//             System.out.println("Árvore vazia.");
//         }
//     }
    
//     private Node deserializarNo(RandomAccessFile arquivo) throws IOException {
//         System.out.println("Deserializando nó");
//         int numChaves = arquivo.readInt();
//         System.out.println("Número de chaves: "+numChaves);
//         if (numChaves == -1) { // Checa se o nó é nulo
//             System.out.println("Nó nulo");
//             return null;
//         }
//         Node no = new Node(ordem);
//         no.setNumChaves(numChaves);
//         for (int i = 0; i < numChaves; i++) {
//             int temp = arquivo.readInt();
//             System.out.println("Chave: "+temp);
//             no.setChave(i, temp); // Lê e atribui as chaves
//         }
        
//         // Se o nó não for folha, deserializar seus filhos
//         if (!no.isFolha()) {
//             for (int i = 0; i <= numChaves; i++) {
//                 no.setFilho(i, deserializarNo(arquivo));
//             }
//         }
//         return no;
//     }    

//     // Método de inserção
//     public void insere(int chave, long pos) {
//         // Atualizar arquivo
//         quant++;
//         try{
//             arq.seek(0);
//             arq.writeInt(quant);
//             arq.seek(arq.length());
//             arq.writeInt(chave);
//             arq.writeLong(pos);
//         } catch(Exception e){}
//         // Checa se árvore está vazia
//         if (raiz == null) {
//             // Cria nova a raiz e prepara a árvore
//             System.out.println("Criando nova raiz");
//             raiz = new Node(ordem);
//             raiz.setChave(0, chave);
//             raiz.setPos(0, pos);
//             raiz.setNumChaves(1);
//         } else {
//             // Checa se nó está cheio
//             if (raiz.getNumChaves() == 2 * ordem - 1) {
//                 // Cria novo nó e balanceia árvore
//                 Node novoNo = new Node(ordem);
//                 novoNo.setFilho(0, raiz);
//                 divideNo(novoNo, 0, raiz);
//                 int i = 0;
//                 if (novoNo.getChave(0) < chave) {
//                     i++;
//                 }
//                 insereNoNaoCheio(novoNo.getFilho(i), chave, pos);
//                 raiz = novoNo;
//             } else {
//                 // Insere no nó designado caso não esteja cheio
//                 insereNoNaoCheio(raiz, chave, pos);
//             }
//         }
//     }

//     // Método para balancear árvore
//     private void divideNo(Node noPai, int index, Node noFilho) {
//         // Cria novo nó
//         Node novoNoFilho = new Node(ordem);
//         novoNoFilho.setNumChaves(ordem - 1);
//         // Preenche novo nó
//         for (int j = 0; j < ordem - 1; j++) {
//             novoNoFilho.setChave(j, noFilho.getChave(j + ordem));
//             novoNoFilho.setPos(j, noFilho.getPos(j+ordem));
//         }
//         // Checa se nó filho não é folha
//         if (!noFilho.isFolha()) {
//             for (int j = 0; j < ordem; j++) {
//                 novoNoFilho.setFilho(j, noFilho.getFilho(j + ordem));
//             }
//         }

//         // Prepara nós filhos
//         noFilho.setNumChaves(ordem - 1);
//         for (int j = noPai.getNumChaves(); j >= index + 1; j--) {
//             noPai.setFilho(j + 1, noPai.getFilho(j));
//         }
//         noPai.setFilho(index + 1, novoNoFilho);
//         for (int j = noPai.getNumChaves() - 1; j >= index; j--) {
//             noPai.setChave(j + 1, noPai.getChave(j));
//             noPai.setPos(j+1, noPai.getPos(j));
//         }

//         // Termina balanceamento da árvore
//         noPai.setChave(index, noFilho.getChave(ordem - 1));
//         noPai.setPos(index, noFilho.getPos(ordem-1));
//         noPai.incrementaChaves();
//     }

//     // Método de inserção
//     private void insereNoNaoCheio(Node no, int chave, long pos) {
//         int i = no.getNumChaves() - 1;
//         // Checa se o nó é folha
//         if (no.isFolha()) {
//             // Redefine o jeito que o nó está organizado
//             while (i >= 0 && chave < no.getChave(i)) {
//                 no.setChave(i + 1, no.getChave(i));
//                 no.setPos(i + 1, no.getPos(i));
//                 i--;
//             }
//             // Insere novos valores
//             // System.out.println("Inserindo o par id/chave na folha: "+chave+" "+pos);
//             no.setChave(i + 1, chave);
//             no.setPos(i+1, pos);
//             no.incrementaChaves();
//         } else {
//             // Vai para posição onde o ID deve ser inserido
//             while (i >= 0 && chave < no.getChave(i)) {
//                 i--;
//             }
//             i++;
//             // Checa se nó está cheio
//             if (no.getFilho(i).getNumChaves() == 2 * ordem - 1) {
//                 // Gera novos nós
//                 divideNo(no, i, no.getFilho(i));
//                 if (chave > no.getChave(i)) {
//                     i++;
//                 }
//             }
//             // Insere ID no nó
//             insereNoNaoCheio(no.getFilho(i), chave, pos);
//         }
//     }
//     // Método de busca que retorna o nó
//     public Node busca(int chave) {
//         return busca(raiz, chave);
//     }
//     // Chama o método com a raiz
//     private Node busca(Node no, int chave) {
//         int i = 0;
//         // Procura a posição dentro do nó
//         while (i < no.getNumChaves() && chave > no.getChave(i)) {
//             i++;
//         }
//         // Checa se posição corresponde ao ID procurado
//         if (i < no.getNumChaves() && chave == no.getChave(i)) {
//             // Se sim, retorna o nó desejado
//             return no;
//         }
//         // Se não achar, retorna null
//         if (no.isFolha()) {
//             return null;
//         }
//         // Busca no próximo nó caso o atual não seja folha
//         return busca(no.getFilho(i), chave);
//     }
//     // Método de busca que retorna a posição do objeto no arquivo binário (retorna o ponteiro)
//     public long buscaPos(int chave) {
//         System.out.println("Buscando chave na árvore: "+chave);
//         return buscaPos(raiz, chave);
//     }
//     // Chama método passando a raiz
//     private long buscaPos(Node no, int chave) {
//         int i = 0;
//         System.out.print(no.getChave(i)+" ");
//         // Procura a posição no nó
//         while (i < no.getNumChaves() && chave > no.getChave(i)) {
//             i++;
//         }
//         // Checa se o elemento da posição é o nó
//         if (i < no.getNumChaves() && chave == no.getChave(i)) {
//             System.out.println("Chave encontrada na árvore: "+chave+" na posição: "+no.getPos(i));
//             return no.getPos(i);
//         }
//         // Se for folha, retorna 0
//         if (no.isFolha()) {
//             return 0;
//         }
//         // Refaz a busca no próximo nó caso esse não seja folha
//         return buscaPos(no.getFilho(i), chave);
//     }
//     // Método de busca que retorna o índice do ID desejado dentro do nó
//     public int buscaI(int chave) {
//         return buscaI(raiz, chave);
//     }
//     // Chama método passando a raiz
//     private int buscaI(Node no, int chave) {
//         int i = 0;
//         // Procura a posição do ID no nó
//         while (i < no.getNumChaves() && chave > no.getChave(i)) {
//             i++;
//         }
//         // Checa se posição corresponde ao ID
//         if (i < no.getNumChaves() && chave == no.getChave(i)) {
//             // Retorna índice
//             return i;
//         }
//         // Checa se é folha
//         if (no.isFolha()) {
//             return 9; // Retornar erro
//         }
//         // Continua busca no próximo nó
//         return buscaI(no.getFilho(i), chave);
//     }
//     // Método de remover a partir do ID
//     public void remove(int chave) {
//         if (raiz == null) {
//             return;
//         }
//         remove(raiz, chave);
//         // Se árvore estiver vazia
//         if (raiz.getNumChaves() == 0) {
//             if (raiz.isFolha()) {
//                 raiz = null;
//             } else {
//                 raiz = raiz.getFilho(0);
//             }
//         }
//     }
//     // Chama método passando raiz
//     private void remove(Node no, int chave) {
//         int i = 0;
//         // Acha posição
//         while (i < no.getNumChaves() && chave > no.getChave(i)) {
//             i++;
//         }
//         // Checa se a posição corresponde ao ID
//         if (i < no.getNumChaves() && chave == no.getChave(i)) {
//             if (no.isFolha()) {
//                 removeChaveFolha(no, i);
//             } else {
//                 removeChaveNaoFolha(no, i);
//             }
//         } else {
//             if (no.isFolha()) {
//                 return;
//             }
//             // Checa tamanho do nó
//             boolean flag = (i == no.getNumChaves());
    
//             if (no.getFilho(i).getNumChaves() < ordem) {
//                 preenche(no, i);
//             }
            
//             if (flag && i > no.getNumChaves()) {
//                 remove(no.getFilho(i - 1), chave);
//             } else {
//                 remove(no.getFilho(i), chave);
//             }
//         }
//     }
//     // Método para remoção de folha
//     private void removeChaveFolha(Node no, int index) {
//         // Remove a chave desejada da folha
//         for (int i = index + 1; i < no.getNumChaves(); ++i) {
//             no.setPos(i-1, no.getPos(i));
//             no.setChave(i - 1, no.getChave(i));
//         }
//         // Decrementa chave do nó
//         no.decrementaChaves();
//     }
//     // Método para remoção de nó que não é folha
//     private void removeChaveNaoFolha(Node no, int index) {
//         int chave = no.getChave(index);
//         // Checa se nó está sobrecarregado
//         if (no.getFilho(index).getNumChaves() >= ordem) {
//             // Balanceia o nó
//             int predecessor = getPredecessor(no, index);
//             no.setChave(index, predecessor);
//             remove(no.getFilho(index), predecessor);
//         } else if (no.getFilho(index + 1).getNumChaves() >= ordem) {
//             // Checa se nó vai ficar acima do tamanho
//             int sucessor = getSucessor(no, index);
//             no.setChave(index, sucessor);
//             remove(no.getFilho(index + 1), sucessor);
//         } else {
//             // Balanceia árvore fazendo a remoção
//             merge(no, index);
//             remove(no.getFilho(index), chave);
//         }
//     }
//     // Método para pegar predecessor
//     private int getPredecessor(Node no, int index) {
//         // Vai para o nó filho
//         Node cur = no.getFilho(index);
//         while (!cur.isFolha()) {
//             cur = cur.getFilho(cur.getNumChaves());
//         }
//         // Retorna quantidade de chaves do nó filho
//         return cur.getChave(cur.getNumChaves() - 1);
//     }
//     // Método para pegar sucessor
//     private int getSucessor(Node no, int index) {
//         // Vai para nó filho
//         Node cur = no.getFilho(index + 1);
//         while (!cur.isFolha()) {
//             cur = cur.getFilho(0);
//         }
//         // Retorna primeiro elemento do filho
//         return cur.getChave(0);
//     }
//     // Método para preeencher nó
//     private void preenche(Node no, int index) {
//         // Preenche nó
//         if (index != 0 && no.getFilho(index - 1).getNumChaves() >= ordem) {
//             moveChaveParaDireita(no, index);
//         } else if (index != no.getNumChaves() && no.getFilho(index + 1).getNumChaves() >= ordem) {
//             moveChaveParaEsquerda(no, index);
//         } else {
//             // Realiza balanceamento
//             if (index != no.getNumChaves()) {
//                 merge(no, index);
//             } else {
//                 merge(no, index - 1);
//             }
//         }
//     }
//     // Método para mover a chave para a direita
//     private void moveChaveParaDireita(Node no, int index) {
//         // Remaneja nó para esquerda
//         Node filho = no.getFilho(index);
//         Node irmao = no.getFilho(index - 1);
//         // Adiciona nó à direita
//         for (int i = filho.getNumChaves() - 1; i >= 0; --i) {
//             filho.setChave(i + 1, filho.getChave(i));
//         }
    
//         if (!filho.isFolha()) {
//             for (int i = filho.getNumChaves(); i >= 0; --i) {
//                 filho.setFilho(i + 1, filho.getFilho(i));
//             }
//         }
//         filho.setChave(0, no.getChave(index - 1));
//         if (!no.isFolha()) {
//             filho.setFilho(0, irmao.getFilho(irmao.getNumChaves()));
//         }
    
//         no.setChave(index - 1, irmao.getChave(irmao.getNumChaves() - 1));
//         // Muda o balanceamento da árvore
//         filho.incrementaChaves();
//         irmao.decrementaChaves();
//     }
//     // Método para mover a chave para a esquerda
//     private void moveChaveParaEsquerda(Node no, int index) {
//         // Remaneja nó para direita
//         Node filho = no.getFilho(index);
//         Node irmao = no.getFilho(index + 1);
//         // Prepara inserção do nó
//         filho.setChave(filho.getNumChaves(), no.getChave(index));
//         if (!filho.isFolha()) {
//             filho.setFilho(filho.getNumChaves() + 1, irmao.getFilho(0));
//         }
//         no.setChave(index, irmao.getChave(0));
//         for (int i = 1; i < irmao.getNumChaves(); ++i) {
//             irmao.setChave(i - 1, irmao.getChave(i));
//         }
//         if (!irmao.isFolha()) {
//             for (int i = 1; i <= irmao.getNumChaves(); ++i) {
//                 irmao.setFilho(i - 1, irmao.getFilho(i));
//             }
//         }
//         // Realiza balanceamento da árvore
//         filho.incrementaChaves();
//         irmao.decrementaChaves();
//     }
//     // Método para utilização do merge
//     private void merge(Node no, int index) {
//         // Preparação utilizando atributos
//         Node filho = no.getFilho(index);
//         Node irmao = no.getFilho(index + 1);
//         filho.setChave(ordem - 1, no.getChave(index));
//         filho.setPos(ordem-1, no.getPos(index));
//         // Reorganizando nó
//         for (int i = 1; i < irmao.getNumChaves(); ++i) {
//             filho.setChave(i + ordem - 1, irmao.getChave(i));
//             filho.setPos(i+ ordem -1, irmao.getPos(i));
//         }
//         // Checa se não é folha
//         if (!filho.isFolha()) {
//             for (int i = 1; i <= irmao.getNumChaves(); ++i) {
//                 filho.setFilho(i + ordem - 1, irmao.getFilho(i));
//             }
//         }
//         for (int i = index + 1; i < no.getNumChaves(); ++i) {
//             no.setChave(i - 1, no.getChave(i));
//             no.setPos(i-1,no.getPos(i));
//         }
//         for (int i = index + 2; i <= no.getNumChaves(); ++i) {
//             no.setFilho(i - 1, no.getFilho(i));
//         }
//         // Termina balanceamento
//         filho.setNumChaves(filho.getNumChaves() + irmao.getNumChaves() + 1);
//         no.decrementaChaves();
//     }
// }    