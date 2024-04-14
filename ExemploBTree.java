// class BTreeNode {
//     int[] keys; // Array de chaves dentro do nó
//     int t; // Grau mínimo (define a faixa de chaves)
//     BTreeNode[] children; // Array de filhos
//     int n; // Número atual de chaves
//     boolean leaf; // É verdadeiro quando o nó é folha

//     public BTreeNode(int t, boolean leaf) {
//         this.t = t;
//         this.leaf = leaf;
//         keys = new int[2 * t - 1]; // Número máximo de chaves
//         children = new BTreeNode[2 * t]; // Número máximo de filhos
//         n = 0; // Inicialmente, não há chaves
//     }

//     // Método para buscar uma chave no subárvore enraizada com esse nó
//     public BTreeNode search(int k) {
//         // Encontra a primeira chave maior ou igual a k
//         int i = 0;
//         while (i < n && k > keys[i]) {
//             i++;
//         }
        
//         // Se a chave encontrada é igual a k, retorna este nó
//         if (i < n && keys[i] == k) {
//             return this;
//         }
        
//         // Se a chave não foi encontrada e este nó é uma folha
//         if (leaf) {
//             return null;
//         }
        
//         // Vai para o filho apropriado
//         return children[i].search(k);
//     }

// }

// public class ExemploBTree {
//     private BTreeNode root; // Raiz da árvore B
//     private int t; // Grau mínimo

//     public ExemploBTree(int t) {
//         this.root = null;
//         this.t = t;
//     }

//     // Método para inserir uma nova chave
//     public void insert(int key) {
//         // Se a árvore estiver vazia
//         if (root == null) {
//             root = new BTreeNode(t, true);
//             root.keys[0] = key; // Inserir chave
//             root.n = 1; // Atualizar número de chaves no nó
//         } else {
//             // Se a raiz está cheia, então a árvore cresce em altura
//             if (root.n == 2*t-1) {
//                 BTreeNode newRoot = new BTreeNode(t, false);
//                 newRoot.children[0] = root;
//                 // Dividir a raiz antiga e mover 1 chave para a nova raiz
//                 splitChild(newRoot, 0, root);
//                 // Nova raiz tem dois filhos agora. Decidir qual dos dois
//                 // vai ter a nova chave
//                 int i = 0;
//                 if (newRoot.keys[0] < key) {
//                     i++;
//                 }
//                 insertNonFull(newRoot.children[i], key);
//                 // Mudar raiz
//                 root = newRoot;
//             } else {
//                 // Se a raiz não está cheia, chamar insertNonFull para a raiz
//                 insertNonFull(root, key);
//             }
//         }
//     }

//     // Método para dividir o filho y do nó x. i é o índice de y em x
//     private void splitChild(BTreeNode x, int i, BTreeNode y) {
//         // Cria um novo nó que vai armazenar t-1 chaves de y
//         BTreeNode z = new BTreeNode(y.t, y.leaf);
//         z.n = t - 1;
        
//         // Copia as últimas (t-1) chaves de y para z
//         for (int j = 0; j < t - 1; j++) {
//             z.keys[j] = y.keys[j+t];
//         }
        
//         // Copia os últimos t filhos de y para z, se y não for uma folha
//         if (!y.leaf) {
//             for (int j = 0; j < t; j++) {
//                 z.children[j] = y.children[j+t];
//             }
//         }
        
//         // Reduz o número de chaves em y
//         y.n = t - 1;
        
//         // Como o nó x vai ter um novo filho, cria espaço para o novo filho
//         for (int j = x.n; j >= i+1; j--) {
//             x.children[j+1] = x.children[j];
//         }
        
//         // Linka o novo filho z a x
//         x.children[i+1] = z;
        
//         // Uma chave de y vai mover para o nó x. Move as chaves de x para criar espaço
//         for (int j = x.n-1; j >= i; j--) {
//             x.keys[j+1] = x.keys[j];
//         }
        
//         // Copia a chave do meio de y para x
//         x.keys[i] = y.keys[t-1];
//         x.n = x.n + 1;
//     }
    
//     // Método para inserir chave no nó que não está cheio
//     private void insertNonFull(BTreeNode x, int k) {
//         // Índice inicial no nó mais à direita
//         int i = x.n - 1;
        
//         // Se x é uma folha, encontra a posição da nova chave e move todas as chaves maiores um espaço para cima
//         if (x.leaf) {
//             while (i >= 0 && k < x.keys[i]) {
//                 x.keys[i+1] = x.keys[i];
//                 i--;
//             }
//             x.keys[i+1] = k;
//             x.n = x.n + 1;
//         } else { // Se x não é uma folha
//             // Encontra o filho que vai ter a nova chave
//             while (i >= 0 && k < x.keys[i]) {
//                 i--;
//             }
//             i++;
//             // Vê se o filho encontrado está cheio
//             if (x.children[i].n == 2*t-1) {
//                 // Se o filho está cheio, então divide
//                 splitChild(x, i, x.children[i]);
                
//                 // Após dividir, o filho do meio de x é o novo nó criado. Decide qual dos dois vai ter a nova chave
//                 if (k > x.keys[i]) {
//                     i++;
//                 }
//             }
//             insertNonFull(x.children[i], k);
//         }
//     }

//     // Método de busca na árvore B que chama o método search do nó
//     public BTreeNode search(int k) {
//         return root == null ? null : root.search(k);
//     }

//     // Método auxiliar para encontrar o índice da primeira chave maior ou igual a k
//     private int findKey(BTreeNode x, int k) {
//         int idx = 0;
//         while (idx < x.n && x.keys[idx] < k) {
//             ++idx;
//         }
//         return idx;
//     }

//     // Método para remover a chave k do subárvore enraizada com este nó
//     private void removeFromNode(BTreeNode node, int k) {
//         int idx = findKey(node, k);

//         // Caso: A chave a ser removida está presente neste nó
//         if (idx < node.n && node.keys[idx] == k) {
//             if (node.leaf) {
//                 // O nó é uma folha, remove a chave diretamente.
//                 for (int i = idx + 1; i < node.n; ++i) {
//                     node.keys[i-1] = node.keys[i];
//                 }
//                 node.n--;
//             } else {
//                 // A chave está em um nó interno. Este caso é mais complexo e requer
//                 // encontrar um sucessor ou predecessor ou mesclar nós. Por simplicidade,
//                 // esse caso não será coberto aqui em detalhes.
//             }
//         } else if (!node.leaf) {
//             // A chave não está neste nó e este nó não é uma folha.
//             boolean flag = (idx == node.n);
            
//             // Se o filho onde a chave poderia estar tem menos que t chaves,
//             // preenchemos esse filho
//             if (node.children[idx].n < t) {
//                 fill(node, idx);
//             }

//             // O filho pode ter sido mesclado, nesse caso devemos procurar no filho[idx-1]
//             if (flag && idx > node.n) {
//                 removeFromNode(node.children[idx-1], k);
//             } else {
//                 removeFromNode(node.children[idx], k);
//             }
//         } else {
//             // A chave não está na árvore.
//             return;
//         }
//     }

//     // Método para preencher o filho de x que tem menos que t-1 chaves
//     private void fill(BTreeNode x, int idx) {
//         // Se o filho anterior (idx-1) tem mais que t-1 chaves, pegue uma chave emprestada desse filho
//         if (idx != 0 && x.children[idx-1].n >= t) {
//             borrowFromPrev(x, idx);
//         }
//         // Se o próximo filho (idx+1) tem mais que t-1 chaves, pegue uma chave emprestada desse filho
//         else if (idx != x.n && x.children[idx+1].n >= t) {
//             borrowFromNext(x, idx);
//         }
//         // Se ambos os filhos adjacentes têm apenas t-1 chaves, mesclar idx com seu irmão
//         // Qualquer que seja idx tem menos que t-1 chaves agora
//         else {
//             if (idx != x.n) {
//                 merge(x, idx);
//             } else {
//                 merge(x, idx-1);
//             }
//         }
//     }

//     // Métodos para pegar emprestado do filho anterior, próximo e para mesclar serão implementados aqui...

//     public void remove(int k) {
//         if (root == null) {
//             System.out.println("A árvore está vazia.");
//             return;
//         }

//         // Chama a função removeFromNode para a raiz
//         removeFromNode(root, k);

//         // Se o nó raiz tem 0 chaves, torna seu primeiro filho como a nova raiz
//         // se tiver filho, caso contrário define a raiz como nula.
//         if (root.n == 0) {
//             if (root.leaf) {
//                 root = null;
//             } else {
//                 root = root.children[0];
//             }
//         }
//     }
    
//     private void borrowFromPrev(BTreeNode x, int idx) {
//         BTreeNode child = x.children[idx];
//         BTreeNode sibling = x.children[idx-1];
    
//         // Mover todas as chaves em child um passo à frente
//         for (int i = child.n-1; i >= 0; --i)
//             child.keys[i+1] = child.keys[i];
    
//         // Se child não for uma folha, mover todos os seus ponteiros de filhos um passo à frente
//         if (!child.leaf) {
//             for(int i = child.n; i >= 0; --i)
//                 child.children[i+1] = child.children[i];
//         }
    
//         // Definindo a primeira chave de child como a chave de x[idx-1]
//         child.keys[0] = x.keys[idx-1];
    
//         // Movendo a última chave do irmão para o pai
//         // Isso reduz o número de chaves em sibling
//         if(!child.leaf)
//             child.children[0] = sibling.children[sibling.n];
    
//         x.keys[idx-1] = sibling.keys[sibling.n-1];
//         child.n += 1;
//         sibling.n -= 1;
//     }

//     private void borrowFromNext(BTreeNode x, int idx) {
//         BTreeNode child = x.children[idx];
//         BTreeNode sibling = x.children[idx+1];
    
//         // A chave de x[idx] é inserida como a última chave em child
//         child.keys[(child.n)] = x.keys[idx];
    
//         // O primeiro filho de sibling é inserido como o último filho de child
//         if (!child.leaf)
//             child.children[(child.n)+1] = sibling.children[0];
    
//         // A primeira chave de sibling é movida para x[idx]
//         x.keys[idx] = sibling.keys[0];
    
//         // Mover todas as chaves em sibling um passo para trás
//         for (int i = 1; i < sibling.n; ++i)
//             sibling.keys[i-1] = sibling.keys[i];
    
//         // Mover os ponteiros de filhos um passo para trás
//         if (!sibling.leaf) {
//             for(int i = 1; i <= sibling.n; ++i)
//                 sibling.children[i-1] = sibling.children[i];
//         }
    
//         // Aumentar e diminuir o número de chaves em child e sibling respectivamente
//         child.n += 1;
//         sibling.n -= 1;
//     }

//     private void merge(BTreeNode x, int idx) {
//         BTreeNode child = x.children[idx];
//         BTreeNode sibling = x.children[idx+1];
    
//         // Puxando uma chave de x[idx] e inserindo-a em (t-1)ª posição de child
//         child.keys[t-1] = x.keys[idx];
    
//         // Copiando as chaves de sibling para child
//         for (int i=0; i<sibling.n; ++i)
//             child.keys[i+t] = sibling.keys[i];
    
//         // Copiando os ponteiros de filhos de sibling para child
//         if (!child.leaf) {
//             for(int i=0; i<=sibling.n; ++i)
//                 child.children[i+t] = sibling.children[i];
//         }
    
//         // Movendo as chaves em x um passo para trás após idx
//         for (int i=idx+1; i<x.n; ++i)
//             x.keys[i-1] = x.keys[i];
    
//         // Movendo os ponteiros de filhos um passo para trás após idx+1
//         for (int i=idx+2; i<=x.n; ++i)
//             x.children[i-1] = x.children[i];
    
//         // Atualizando o número de chaves em child e x
//         child.n += sibling.n + 1;
//         x.n--;
    
//         // Liberando o espaço ocupado por sibling
//         sibling = null;
//     }
    
// }
