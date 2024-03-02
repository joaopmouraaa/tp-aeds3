import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime; // nao usado
import java.util.List;
import java.util.Base64; // nao usado

public class CsvToBinaryConverter {

    // CRUD

    // Create
    public static void create(String binaryFilePath, Carro registroCarro) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(binaryFilePath, "rw")) {
            boolean registroExiste = false;

            while (raf.getFilePointer() < raf.length()) {
                byte lapide = raf.readByte();
                int tamanhoRegistro = raf.readInt();

                if (lapide != 1) {
                    byte[] registroAtualBytes = new byte[tamanhoRegistro];
                    raf.readFully(registroAtualBytes);
                    Carro carroExistente = deserializeCarro(registroAtualBytes);

                    if (carroExistente.getId() == registroCarro.getId()) {
                        registroExiste = true;
                        break;
                    }
                } else {
                    raf.skipBytes(tamanhoRegistro);
                }
            }

            if (!registroExiste) {
                raf.seek(raf.length()); // Posiciona o ponteiro no final do arquivo
                byte[] registroCarroBytes = serializeCarro(registroCarro);
                raf.writeByte(0); // Escreve a lápide (0 para registro válido, 1 para registro excluído)
                raf.writeInt(registroCarroBytes.length); // Escreve o tamanho do registro
                raf.write(registroCarroBytes); // Escreve o registro em si
            } else {
                // System.out.println("Registro com ID " + registroCarro.getId() + " já
                // existe.");
            }
        }
    }

    // ReadAll - lê todos os registros
    public static void readAll(String binaryFilePath) {
        try (RandomAccessFile raf = new RandomAccessFile(binaryFilePath, "r")) {
            while (raf.getFilePointer() < raf.length()) { // Enquanto não terminar o arquivo
                byte lapide = raf.readByte();
                int recordSize = raf.readInt();
                if (lapide == 0) {
                    byte[] recordBytes = new byte[recordSize];
                    raf.readFully(recordBytes); // Lê o registro
                    Carro carro = deserializeCarro(recordBytes); // Desserializa
                    System.out.println("Sport Car: " + carro.toString());
                } else {
                    raf.skipBytes(recordSize);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ReadById - lê um registro específico (recebe o ID do registro como parâmetro)
    public static void readById(String binaryFilePath, int carId) {
        try (RandomAccessFile raf = new RandomAccessFile(binaryFilePath, "r")) {
            boolean isFound = false;
            while (raf.getFilePointer() < raf.length()) {
                byte lapide = raf.readByte();
                System.out.print("Ponteiro: " + raf.getFilePointer() + " / Tamanho do arquivo: " + raf.length()
                        + " / Lápide: " + lapide);
                if (lapide == 1) {
                    System.out.println("Registro excluído. Pulando...");
                    int recordSize = raf.readInt();
                    raf.skipBytes(recordSize);
                    continue;
                }
                int recordSize = raf.readInt();
                System.out.println(" / Tamanho do registro: " + recordSize);
                byte[] recordBytes = new byte[recordSize];
                try {
                    raf.readFully(recordBytes);
                } catch (EOFException e) {
                }
                if (lapide == 0) {
                    Carro carro = deserializeCarro(recordBytes);
                    if (carro.getId() == carId) {
                        System.out.println("Registro encontrado: " + carro.toString());
                        isFound = true;
                        break;
                    }
                }
            }
            if (!isFound) {
                System.out.println("Carro com ID " + carId + " não foi encontrado.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Update
    public static void update(int id, Carro carroAtualizado, String binaryFilePath) throws IOException {
        RandomAccessFile file = new RandomAccessFile(binaryFilePath, "rw");

        while (file.getFilePointer() < file.length()) {
            long posicaoAtual = file.getFilePointer();
            byte lapide = file.readByte();
            int tamanhoRegistro = file.readInt();

            if (lapide != 1) { // Checa se o registro não está marcado com lápide
                byte[] registroAtualBytes = new byte[tamanhoRegistro];
                file.readFully(registroAtualBytes);

                // Deserializa o registro para objeto Carro e verifica o ID
                Carro carro = deserializeCarro(registroAtualBytes);
                if (carro.getId() == id) {
                    // Atualiza os campos do carro existente com os do carro atualizado
                    updateCarFields(carro, carroAtualizado);

                    byte[] registroAtualizadoBytes = serializeCarro(carro);

                    if (registroAtualizadoBytes.length <= tamanhoRegistro) {
                        // Substitui no local
                        file.seek(posicaoAtual);
                        file.writeByte(0); // Escreve lápide como válida
                        file.writeInt(tamanhoRegistro);
                        // file.writeInt(registroAtualizadoBytes.length); // não pode atualizar o
                        // tamanho do registro se ele for menor
                        file.write(registroAtualizadoBytes);
                    } else {
                        // Marca com lápide e adiciona no final
                        file.seek(posicaoAtual);
                        file.writeByte(1); // Marca com lápide
                        file.seek(file.length());
                        file.writeByte(0); // Nova lápide válida
                        file.writeInt(registroAtualizadoBytes.length); // atualiza o registro, já que é maior
                        file.write(registroAtualizadoBytes);
                    }
                    break;
                }
            }
            // Avança para o próximo registro
            file.seek(posicaoAtual + 5 + tamanhoRegistro);
        }
        file.close();
    }

    // Delete
    public static void deleteById(String binaryFilePath, int carId) {
        try (RandomAccessFile raf = new RandomAccessFile(binaryFilePath, "rw")) {
            boolean isFound = false;
            while (raf.getFilePointer() < raf.length()) {
                long recordStart = raf.getFilePointer();
                byte lapide = raf.readByte();
                int recordSize = raf.readInt();
                if (lapide == 0) {
                    byte[] recordBytes = new byte[recordSize];
                    raf.readFully(recordBytes); // Lê o registro
                    Carro carro = deserializeCarro(recordBytes); // Desserializa
                    if (carro.getId() == carId) {
                        isFound = true;
                        raf.seek(recordStart); // Volta para o início do registro
                        raf.writeByte(1); // Atualiza a lápide para indicar que o registro foi excluído
                        System.out.println("Carro com ID " + carId + " foi deletado.");
                        break;
                    }
                } else {
                    // Pula o registro se ele já está excluído
                    raf.skipBytes(recordSize);
                }
            }
            if (!isFound) {
                System.out.println("Carro com ID " + carId + " não foi encontrado.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Métodos auxiliares

    // Serializar um objeto RegistroCarro para um array de bytes
    private static byte[] serializeCarro(Carro registroCarro) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(registroCarro.getId());
        dos.writeUTF(registroCarro.getCarMake());
        dos.writeUTF(registroCarro.getCarModel());
        dos.writeUTF(registroCarro.getHp_Torque());
        dos.writeLong(registroCarro.getDateInMilliseconds());
        dos.writeFloat(registroCarro.getZeroToSixty());
        dos.writeFloat(registroCarro.getPrice());
        return baos.toByteArray();
    }

    // Desserializar um array de bytes para um objeto RegistroCarro
    private static Carro deserializeCarro(byte[] data) throws IOException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
                DataInputStream dis = new DataInputStream(bais)) {
            int id = dis.readInt();
            String carMake = dis.readUTF();
            String carModel = dis.readUTF();
            String hpTorque = dis.readUTF();
            long dateInMilliseconds = dis.readLong();
            float zeroToSixty = dis.readFloat();
            float price = dis.readFloat();

            String dateString = DateConverter.convertMillisToDate(dateInMilliseconds);

            return new Carro(id, carMake, carModel, hpTorque, dateString, zeroToSixty, price);
        }
    }

    // Update para lidar com atualizações parciais
    private static void updateCarFields(Carro carro, Carro carroAtualizado) {
        if (carroAtualizado.getId() > -1) {
            carro.setId(carroAtualizado.getId());
        }
        if (carroAtualizado.getCarMake() != null) {
            carro.setCarMake(carroAtualizado.getCarMake());
        }
        if (carroAtualizado.getCarModel() != null) {
            carro.setCarModel(carroAtualizado.getCarModel());
        }
        if (carroAtualizado.getHp_Torque() != null) {
            carro.setHp_Torque(carroAtualizado.getHp_Torque());
        }
        if (carroAtualizado.getDateInMilliseconds() != 0) {
            carro.setDateInMilliseconds(carroAtualizado.getDateInMilliseconds());
        }
        if (carroAtualizado.getZeroToSixty() != 0.0f) {
            carro.setZeroToSixty(carroAtualizado.getZeroToSixty());
        }
        if (carroAtualizado.getPrice() != 0.0f) {
            carro.setPrice(carroAtualizado.getPrice());
        }
    }

    /*
     * Main
     * 
     * public static void main(String[] args) throws IOException {
     * String csvFilePath = "./dataset/TABELA-FINAL.csv";
     * String binaryFilePath = "./dataset/binario.bin";
     * 
     * // Create - a partir do CSV, salvando no DB
     * try {
     * List<String> lines = Files.readAllLines(Paths.get(csvFilePath));
     * for (int i = 1; i < lines.size(); i++) {
     * //for (int i = 1; i < 2; i++) { // aqui eu tava testando só com um registro
     * String line = lines.get(i);
     * String[] data = line.split(";");
     * Carro registroCarro = new Carro(
     * data[0], // id
     * data[1], // carMake
     * data[2], // carModel
     * data[3], // hp_Torque
     * data[4], // dateString
     * Float.parseFloat(data[5]), // zeroToSixty
     * Float.parseFloat(data[6].replaceAll("\\,", "")) // price
     * );
     * create(binaryFilePath, registroCarro);
     * }
     * } catch (IOException e) {
     * e.printStackTrace();
     * }
     * 
     * // Teste de operações por id
     * String testCarId = "1"; // No caso é o Porsche 911
     * Carro carroUpdate = new Carro();
     * carroUpdate.setId("0");
     * carroUpdate.setCarMake("Audi");
     * carroUpdate.setCarModel("tt");
     * //carroUpdate.setHp_Torque("120, 70");
     * //carroUpdate.setDateInMilliseconds(1641006000000l);
     * //carroUpdate.setZeroToSixty(16f);
     * //carroUpdate.setPrice(40000.0f);
     * 
     * update("0", carroUpdate, binaryFilePath);
     * System.out.println();
     * 
     * 
     * 
     * // Teste do ReadAll - val mostrar todos os registros
     * //readAll(binaryFilePath);
     * 
     * // Teste do ReadById - vai falar que foi encontrado e mostrar o registro
     * readById(binaryFilePath, "0");
     * 
     * // Teste DeleteById - vai falar que foi deletado
     * //deleteById(binaryFilePath, testCarId);
     * 
     * // Teste ReadById após Delete - vai falar que não foi encontrado
     * //readById(binaryFilePath, testCarId);
     * 
     * // Observações:
     * // Criar o Update, que é o mais difícil
     * // Sempre que a gente roda este arquivo, ele adiciona mais registros ao
     * arquivo binário. Não recria o arquivo. Se rodar mais de uma vez, vai ter
     * registros repetidos.
     * // A gente precisa ver se precisamos guardar a data tanto em milissegundos
     * quanto em string no objeto.
     * // Ver se precisamos tratar ou separar os dados de hp e torque. Talvez criar
     * outro objeto ou armazenar em um array de inteiros ou de floats.
     * 
     * }
     */
}
