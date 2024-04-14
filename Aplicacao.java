import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.lang.reflect.Constructor;

public class Aplicacao {    
    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String binaryFilePath = "./binario.bin";
        String csvFilePath = "./TABELA-FINAL.csv";
        String bTreeFilePath = "./bTree.bin";
        String directoryFilePath = "./directory.bin";
        String bucketFilePath = "./bucket.bin";
        String dictionaryFilePath = "./dictionary.bin";
        String blocksFilePath = "./blocks.bin";
        CRUD.inicializarContadorDeRegistros(binaryFilePath);
        System.out.println("Contador de registros inicializado. Contando registros: " + CRUD.getNumeroRegistros(binaryFilePath) + " registros. ");
        System.out.println("Quer trabalhar com Árvore B+, Hash Extensível ou Lista Invertida?");
        System.out.println("1. Árvore B+");
        System.out.println("2. Hash Extensível");
        System.out.println("3. Lista Invertida");
        System.out.print("Digite sua escolha: ");
        String escolha = reader.readLine();
        BPTree bTree = null;
        HashExtensivel<RegistroIDEndereco> hash = null;
        try {
            Constructor<RegistroIDEndereco> constructor = RegistroIDEndereco.class.getConstructor();
            hash = new HashExtensivel<RegistroIDEndereco>(constructor, 20, directoryFilePath, bucketFilePath);
        } catch (Exception e) {
            System.out.println("Erro ao criar o Hash Extensível: " + e.getMessage());
            return;
        }
        ListaInvertida invertedList = null;
        if ("1".equals(escolha)) {
            System.out.println("Trabalhando com Árvore B+");
            bTree = new BPTree(8, bTreeFilePath);
        } else if ("2".equals(escolha)) {
            System.out.println("Trabalhando com Hash Extensível");
        } else if ("3".equals(escolha)) {
            System.out.println("Trabalhando com Lista Invertida");
            try {
                invertedList = new ListaInvertida(20, dictionaryFilePath, blocksFilePath);
            } catch (Exception e) {
                System.out.println("Erro ao criar a Lista Invertida: " + e.getMessage());
            }
        } else {
            System.out.println("Opção inválida. Fechando o programa...");
            return;
        }
        char choice = '0';
        while (choice != '6') {
            System.out.println("Escolha uma opção:");
            System.out.println("0. Criar um novo registro manualmente");
            System.out.println("1. Criar estrutura de dados escolhida");
            System.out.println("2. Ler registro por ID");
            System.out.println("3. Atualizar registro por ID");
            System.out.println("4. Deletar registro por ID");
            System.out.println("5. Ler todos os registros");
            System.out.println("6. Salvar e sair");
            System.out.print("Digite sua escolha: ");
            choice = reader.readLine().charAt(0);
            switch (choice) {
                case '0':
                    while (true) {
                        System.out.print("Digite a marca do carro: ");
                        String carMake = reader.readLine();
                        System.out.print("Digite o modelo do carro: ");
                        String carModel = reader.readLine();
                        System.out.print("Digite a potência e torque do carro separados por virgula: ");
                        String[] hpTorque = reader.readLine().split(",");
                        System.out.print("Digite a data de lançamento do carro: ");
                        String date = reader.readLine();
                        System.out.print("Digite o tempo de 0-60 MPH do carro: ");
                        float zeroToSixty = Float.parseFloat(reader.readLine());
                        System.out.print("Digite o preço do carro: ");
                        float price = Float.parseFloat(reader.readLine());
                        int id = CRUD.getNumeroRegistros(binaryFilePath); // ID
                        Carro carro = new Carro(id, carMake, carModel, hpTorque, date, zeroToSixty, price);
                        System.out.println(carro.toString());
                        myStruct resultado = CRUD.create(binaryFilePath, carro);
                        if (resultado.sucesso) {
                            // implementar inserção na árvore com ID e endereço do carro no arquivo binário
                            if ("1".equals(escolha)) {
                                insertIntoBTree(carro, resultado, bTree, binaryFilePath);
                            } else if ("2".equals(escolha)) {
                                insertIntoHash(carro, resultado, hash, binaryFilePath);
                            } else if ("3".equals(escolha)) {
                                insertIntoInvertedList(carro, resultado, invertedList, binaryFilePath);
                            }
                            CRUD.incrementarContadorDeRegistros(binaryFilePath);
                            System.out.println("Registro criado com o id " + carro.getId() + ".");
                        }
                        break;
                    }
                    break;
                case '1':
                    System.out.print("Criando a estrutura de dados: ");
                    if ("-".equals(csvFilePath)) {
                        System.out.println("Usando diretório padrão...");
                        csvFilePath = "./TABELA-FINAL.csv";
                    }
                    if ("1".equals(escolha)) {
                        System.out.println("Árvore B+...");
                        createDatabaseFromCSV(csvFilePath, binaryFilePath, bTree);
                        CRUD.readAll(binaryFilePath);
                    } else if ("2".equals(escolha)) {
                        System.out.println("Hash Extensível...");
                        createDatabaseFromCSV(csvFilePath, binaryFilePath, hash);
                        CRUD.readAll(binaryFilePath);
                    } else if ("3".equals(escolha)) {
                        System.out.println("Lista Invertida...");
                        createDatabaseFromCSV(csvFilePath, binaryFilePath, invertedList);
                        CRUD.readAll(binaryFilePath);
                    } else {
                        System.out.println("Opção inválida. Tente novamente.");
                    }
                    break;
                case '2':
                    System.out.print("Digite o ID do registro que deseja ler: ");
                    String idRead = reader.readLine();
                    int posicao = -1;
                    if ("1".equals(escolha)) {
                        posicao = bTree.read(idRead);
                    } else if ("2".equals(escolha)) {
                        int chave = Integer.parseInt(idRead);
                        try {
                            posicao = hash.read(chave).getEndereco();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if ("3".equals(escolha)) {
                        try {
                            System.out.println("Lendo da lista invertida...");
                            int[] array = invertedList.read(idRead);
                            System.out.println("Registro encontrado na lista invertida. ");
                            for (int i = 0; i < array.length; i++) {
                                System.out.println("Endereço do registro: " + array[i] + ".");
                            }
                            if (array.length > 0) {
                                posicao = array[0];
                            }
                        } catch (Exception e) {
                            System.out.println("Registro não encontrado.");
                        }
                    }
                    if (posicao == -1) {
                        System.out.println("Registro não encontrado.");
                    } else {
                        System.out.println("Buscando no arquivo de registros, na posição " + posicao + ".");
                        int IntIdRead = Integer.parseInt(idRead);
                        CRUD.readByPosicao(binaryFilePath, posicao, IntIdRead);
                    }
                    break;
                case '3':
                    System.out.print("Digite o ID do carro que deseja atualizar: ");
                    int idAtualiza = Integer.parseInt(reader.readLine());
                    boolean carroExiste = CRUD.readById(binaryFilePath, idAtualiza);
                    if (!carroExiste) {
                        break;
                    }
                    // Cria um objeto Carro para representar os dados atualizados
                    Carro carroAtualizado = new Carro();
                    // carroAtualizado.setId(idAtualiza); // ID não é alterado
                    int contadorCamposAtualizados = 0;
                    // Pergunta para cada campo se o usuário deseja atualizar
                    System.out.print("Atualizar 'Car Make' (Digite '-' para manter inalterado): ");
                    String carMake = reader.readLine();
                    if (!"-".equals(carMake)) {
                        carroAtualizado.setCarMake(carMake);
                        contadorCamposAtualizados++;
                    }
                    // Solicita a atualização do campo 'Car Model'
                    System.out.print("Atualizar 'Car Model' (Digite '-' para manter inalterado): ");
                    String carModel = reader.readLine();
                    if (!"-".equals(carModel)) {
                        carroAtualizado.setCarModel(carModel);
                        contadorCamposAtualizados++;
                    }
                    // Solicita a atualização do campo 'Horsepower_Torque'
                    System.out.print("Atualizar 'Horsepower_Torque' (Digite '-' para manter inalterado): ");
                    String hpTorque = reader.readLine();
                    String[] hpTorqueArray = hpTorque.split(",");
                    if (!"-".equals(hpTorque)) {
                        carroAtualizado.setHp_Torque(hpTorqueArray);
                        contadorCamposAtualizados++;
                    }
                    // Solicita a atualização da data
                    System.out.print("Atualizar 'Data' (Digite '-' para manter inalterado): ");
                    String date = reader.readLine();
                    if (!"-".equals(date)) {
                        carroAtualizado.setDate(date);
                        contadorCamposAtualizados++;
                    }
                    // Solicita a atualização do tempo '0-60 MPH'
                    System.out.print("Atualizar '0-60 MPH Time' (Digite '-' para manter inalterado): ");
                    String zeroToSixtyString = reader.readLine();
                    if (!"-".equals(zeroToSixtyString)) {
                        float zeroToSixty = Float.parseFloat(zeroToSixtyString);
                        carroAtualizado.setZeroToSixty(zeroToSixty);
                        contadorCamposAtualizados++;
                    }
                    // Solicita a atualização do preço
                    System.out.print("Atualizar 'Preço' (Digite '-' para manter inalterado): ");
                    String priceString = reader.readLine();
                    if (!"-".equals(priceString)) {
                        float price = Float.parseFloat(priceString);
                        carroAtualizado.setPrice(price);
                        contadorCamposAtualizados++;
                    }
                    // Chama o método de atualização
                    if (contadorCamposAtualizados == 0) {
                        System.out.println("Nenhum campo foi atualizado.");
                        break;
                    }
                    int novaPos = CRUD.update(idAtualiza, carroAtualizado, binaryFilePath);
                    if (novaPos == -1) {
                        System.out.println("Erro ao atualizar o registro.");
                    } else {
                        if ("1".equals(escolha)) {
                            bTree.update(Integer.toString(idAtualiza), novaPos);
                        } else if ("2".equals(escolha)) {
                            RegistroIDEndereco registro = new RegistroIDEndereco(idAtualiza, novaPos);
                            try {
                                hash.update(registro);
                            } catch (Exception e) {
                                System.out.println("Erro ao atualizar o registro: " + e.getMessage());
                            }
                        } else if ("3".equals(escolha)) {
                            System.out.println("Lista invertida não suporta atualização.");
                        }
                        if (!"3".equals(escolha)) {
                            System.out.println("Registro com id "+idAtualiza+" atualizado com sucesso, agora na posição " + novaPos + ".");
                        }
                    }
                    break;
                case '4':
                    System.out.print("Digite o ID do carro que deseja deletar: ");
                    int idDelete = Integer.parseInt(reader.readLine());
                    // Chama o método de deleção
                    boolean deletou = CRUD.deleteById(binaryFilePath, idDelete);
                    if (deletou) {
                        boolean deletouIndice = false;
                        if ("1".equals(escolha)) {
                            deletouIndice = bTree.delete(Integer.toString(idDelete));
                        } else if ("2".equals(escolha)) {
                            try {
                                deletouIndice = hash.delete(idDelete);
                            } catch (Exception e) {
                                System.out.println("Erro ao deletar o registro: " + e.getMessage());
                            }
                        } else if ("3".equals(escolha)) {
                            // System.out.println("Digite a palavra que deseja deletar: ");
                            // String palavra = reader.readLine();
                            String palavra = ""+idDelete;
                            try {
                                deletouIndice = invertedList.delete(palavra, idDelete);
                            } catch (Exception e) {
                                System.out.println("Erro ao deletar o registro: " + e.getMessage());
                            }
                        }
                        if (deletouIndice || "3".equals(escolha)) {
                            System.out.println("Registro deletado com sucesso.");
                        } else {
                            System.out.println("Registro deletado, porém houve erro para deletá-lo do índice.");
                        }
                    } else {
                        System.out.println("Registro não foi deletado pois não foi encontrado.");
                    }
                    break;
                case '5':
                    System.out.println("Lendo todos os registros...");
                    CRUD.readAll(binaryFilePath);
                    break;
                case '6':
                    System.out.println("Salvando e saindo...");
                    break;
                default:
                    System.out.println("Opção inválida. Tente novamente.");
            }
        }
    }

    private static void createDatabaseFromCSV(String csvFilePath, String binaryFilePath, BPTree bTree) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(csvFilePath));
        System.out.print("Criando registros com IDs: ");
        for (String line : lines) {
            if (lines.indexOf(line) == 0) continue; // Ignora o cabeçalho
            String[] data = line.split(";");
            String[] hpTorqueArray = data[3].split(","); // Divide a string hp_Torque em um array
            int carId = CRUD.getNumeroRegistros(binaryFilePath);
            System.out.print(carId + " ");
            Carro carro = new Carro(
                carId, // ID
                data[1], // Car Make
                data[2], // Car Model
                hpTorqueArray, // hpTorque como um array (dividido pela vírgula)
                data[4], // Data
                Float.parseFloat(data[5]), // 0-60 MPH Time
                Float.parseFloat(data[6].replaceAll("\\,", "")) // Preço
            );
            // System.out.println(carro.toString());
            myStruct resultado = CRUD.create(binaryFilePath, carro);
            if (resultado.sucesso) {
                // implementar inserção na árvore com ID e endereço do carro no arquivo binário
                // System.out.print("Inserindo na árvore o carro id " + carro.getId() + " com posição " + resultado.posicao);
                insertIntoBTree(carro, resultado, bTree, binaryFilePath);                
                CRUD.incrementarContadorDeRegistros(binaryFilePath);
                // System.out.println(". Registro criado com o id " + carro.getId() + ".");
            }
        }
        System.out.println("\nTotal de registros: " + CRUD.getNumeroRegistros(binaryFilePath) + ".");
    }

    private static void createDatabaseFromCSV(String csvFilePath, String binaryFilePath, HashExtensivel<RegistroIDEndereco> hash) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(csvFilePath));
        // int temp = 0;
        for (String line : lines) {
            if (lines.indexOf(line) == 0) continue; // Ignora o cabeçalho
            String[] data = line.split(";");
            String[] hpTorqueArray = data[3].split(","); // Divide a string hp_Torque em um array
            Carro carro = new Carro(
                CRUD.getNumeroRegistros(binaryFilePath), // ID
                data[1], // Car Make
                data[2], // Car Model
                hpTorqueArray, // hpTorque como um array (dividido pela vírgula)
                data[4], // Data
                Float.parseFloat(data[5]), // 0-60 MPH Time
                Float.parseFloat(data[6].replaceAll("\\,", "")) // Preço
            );
            // System.out.println(carro.toString());
            myStruct resultado = CRUD.create(binaryFilePath, carro);
            if (resultado.sucesso) {
                // implementar inserção na árvore com ID e endereço do carro no arquivo binário
                // System.out.print("Inserindo na árvore o carro id " + carro.getId() + " com posição " + resultado.posicao);
                insertIntoHash(carro, resultado, hash, binaryFilePath);
                CRUD.incrementarContadorDeRegistros(binaryFilePath);
                // temp++;
                // if (temp == 6) {
                //     break;
                // }
                // System.out.println(". Registro criado com o id " + carro.getId() + ".");
            }
        }
        // System.out.println("Total de registros criados: " + CRUD.getNumeroRegistros(binaryFilePath) + ".");
        hash.print();
    }

    private static void createDatabaseFromCSV(String csvFilePath, String binaryFilePath, ListaInvertida invertedList) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(csvFilePath));
        for (String line : lines) {
            if (lines.indexOf(line) == 0) continue; // Ignora o cabeçalho
            String[] data = line.split(";");
            String[] hpTorqueArray = data[3].split(","); // Divide a string hp_Torque em um array
            Carro carro = new Carro(
                CRUD.getNumeroRegistros(binaryFilePath), // ID
                data[1], // Car Make
                data[2], // Car Model
                hpTorqueArray, // hpTorque como um array (dividido pela vírgula)
                data[4], // Data
                Float.parseFloat(data[5]), // 0-60 MPH Time
                Float.parseFloat(data[6].replaceAll("\\,", "")) // Preço
            );
            // System.out.println(carro.toString());
            myStruct resultado = CRUD.create(binaryFilePath, carro);
            if (resultado.sucesso) {
                // implementar inserção na árvore com ID e endereço do carro no arquivo binário
                // System.out.print("Inserindo na árvore o carro id " + carro.getId() + " com posição " + resultado.posicao);
                insertIntoInvertedList(carro, resultado, invertedList, binaryFilePath);                
                CRUD.incrementarContadorDeRegistros(binaryFilePath);
                // System.out.println(". Registro criado com o id " + carro.getId() + ".");
            }
        }
        // System.out.println("Total de registros criados: " + CRUD.getNumeroRegistros(binaryFilePath) + ".");
    }

    private static void insertIntoBTree(Carro carro, myStruct resultado, BPTree bTree, String binaryFilePath) {
        // Converter o ID para String
        String idString = Integer.toString(carro.getId());
        // Converter a posição para int
        int posicao = (int) resultado.posicao;
        // Inserir na árvore
        try {
            bTree.create(idString, posicao);
        } catch (Exception e) {
            System.out.println("Erro ao inserir na árvore: " + e.getMessage());
        }
    }

    private static void insertIntoHash(Carro carro, myStruct resultado, HashExtensivel<RegistroIDEndereco> hash, String binaryFilePath) {
        if (hash == null) {
            System.out.println("Hash é nulo.");
            return;
        }
        // Converter o ID para String
        String idString = Integer.toString(carro.getId());
        // Converter a posição para int
        int posicao = (int) resultado.posicao;
        // Criando um novo registro
        RegistroIDEndereco registro = new RegistroIDEndereco(Integer.parseInt(idString), posicao);
        System.out.println("Inserindo registro: " + registro.getId() + ", " + registro.getEndereco()+" ");
        registro.toString();
        // Inserir na árvore
        try {
            hash.create(registro);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void insertIntoInvertedList(Carro carro, myStruct resultado, ListaInvertida invertedList, String binaryFilePath) {
        // Converter o ID para String
        String idString = Integer.toString(carro.getId());
        // Converter a posição para int
        int posicao = (int) resultado.posicao;
        // Inserir na árvore
        try {
            invertedList.create(idString, posicao);
            System.out.print("\nInserido na lista invertida: " + idString + ", " + posicao+" ");
        } catch (Exception e) {
            System.out.println("Erro ao inserir na lista invertida: " + e.getMessage());
        }
    }

}