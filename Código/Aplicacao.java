import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Aplicacao {
    private static int idCounter = 0;
    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String binaryFilePath = "./dataset/binario.bin";
        String csvFilePath = "./dataset/TABELA-FINAL.csv";

        while (true) {
            System.out.println("Escolha uma opção:");
            System.out.println("0. Criar novo registro");
            System.out.println("1. Ler todos os registros");
            System.out.println("2. Ler registro por ID");
            System.out.println("3. Atualizar registro por ID");
            System.out.println("4. Deletar registro por ID");
            System.out.println("5. Sair");

            System.out.print("Digite sua escolha: ");
            String choice = reader.readLine();

            switch (choice) {
                case "0":
                    while (true) {
                        System.out.println("Escolha uma opção:");
                        System.out.println("0. Criar registros a partir de um arquivo CSV");
                        System.out.println("1. Criar um registro manualmente");
                        System.out.println("2. Voltar");
                        System.out.print("Digite sua escolha: ");
                        String subChoice = reader.readLine();

                        switch (subChoice) {
                            case "0":
                                System.out.print("Digite o caminho do arquivo CSV (ou digite \"-\" para usar o diretório padrão): ");
                                csvFilePath = reader.readLine();
                                if ("-".equals(csvFilePath)) {
                                    System.out.println("Usando diretório padrão...");
                                    csvFilePath = "./dataset/TABELA-FINAL.csv";
                                }
                                createDatabaseFromCSV(csvFilePath, binaryFilePath);
                                break;
                            case "1":
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
                                int id = idCounter++;
                                Carro carro = new Carro(id, carMake, carModel, hpTorque, date, zeroToSixty, price);
                                CsvToBinaryConverter.create(binaryFilePath, carro);
                                System.out.println("Registro criado com sucesso! O id do registro é " + id);
                                break;
                            case "2":
                                break;
                            default:
                                System.out.println("Opção inválida. Tente novamente.");
                        }
                        if ("2".equals(subChoice)) {
                            break;
                        }
                        break;
                    }
                    break;
                case "1":
                    System.out.print("Lendo todos os registros... ");

                    CsvToBinaryConverter.readAll(binaryFilePath);
                    break;
                case "2":
                    System.out.print("Digite o ID do registro que deseja ler: ");
                    int idRead = Integer.parseInt(reader.readLine());
                    CsvToBinaryConverter.readById(binaryFilePath, idRead);
                    break;
                case "3":
                    System.out.print("Digite o ID do carro que deseja atualizar: ");
                    int idAtualiza = Integer.parseInt(reader.readLine());
                    boolean carroExiste = CsvToBinaryConverter.readById(binaryFilePath, idAtualiza);
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
                    CsvToBinaryConverter.update(idAtualiza, carroAtualizado, binaryFilePath);
                    break;
                case "4":
                    System.out.print("Digite o ID do carro que deseja deletar: ");
                    int idDelete = Integer.parseInt(reader.readLine());

                    // Chama o método de deleção
                    CsvToBinaryConverter.deleteById(binaryFilePath, idDelete);
                    break;

                case "5":
                    System.out.println("Saindo...");
                    return;
                default:
                    System.out.println("Opção inválida. Tente novamente.");
            }
        }
    }

    private static void createDatabaseFromCSV(String csvFilePath, String binaryFilePath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(csvFilePath));
        int contadorRegistros = 0;
    
        for (String line : lines) {
            if (lines.indexOf(line) == 0) continue; // Ignora o cabeçalho
        
            String[] data = line.split(";");
            String[] hpTorqueArray = data[3].split(","); // Divide a string hp_Torque em um array
            
            Carro carro = new Carro(
                Integer.parseInt(data[0]), // ID
                data[1], // Car Make
                data[2], // Car Model
                hpTorqueArray, // hpTorque como um array (dividido pela vírgula)
                data[4], // Data
                Float.parseFloat(data[5]), // 0-60 MPH Time
                Float.parseFloat(data[6].replaceAll("\\,", "")) // Preço
            );
        
            CsvToBinaryConverter.create(binaryFilePath, carro);
            contadorRegistros++;
        }
        
    
        System.out.println("Total de registros criados: " + contadorRegistros);
    }
    
}