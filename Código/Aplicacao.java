import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Aplicacao {

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String binaryFilePath = "./dataset/binario.bin";
        String csvFilePath = "./dataset/TABELA-FINAL.csv";

        while (true) {
            System.out.println("Escolha uma opção:");
            System.out.println("0. Criar Base de Dados por File Path");
            System.out.println("1. Ler todos os registros");
            System.out.println("2. Ler registro por ID");
            System.out.println("3. Atualizar registro por ID");
            System.out.println("4. Deletar registro por ID");
            System.out.println("5. Sair");

            System.out.print("Digite sua escolha: ");
            String choice = reader.readLine();

            switch (choice) {
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
                    if (!"-".equals(hpTorque)) {
                        carroAtualizado.setHp_Torque(hpTorque);
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
            // Supondo que a primeira linha seja o cabeçalho
            if (lines.indexOf(line) == 0)
                continue;

            String[] data = line.split(";");
            Carro carro = new Carro(Integer.parseInt(data[0]), data[1], data[2], data[3], data[4], Float.parseFloat(data[5]),
                    Float.parseFloat(data[6].replaceAll("\\,", "")));
            CsvToBinaryConverter.create(binaryFilePath, carro);

            contadorRegistros++;
        }
        System.out.println("Total de registros criados: " + contadorRegistros);

    }
}
