import java.io.*;

class myStruct {
    public boolean sucesso;
    public long posicao;
}

public class CRUD_TP2 {

    // Checa se o arquivo da árvore está vazio
    public static boolean isFileEmpty(String binaryFilePath) {
        File file = new File(binaryFilePath);
        return file.length() == 0;
    }

    // Inicializar o contador de registros
    public static void inicializarContadorDeRegistros(String binaryFilePath) {
        int numberOfRecords = 0;
        numberOfRecords = getNumeroRegistros(binaryFilePath);
        try (RandomAccessFile raf = new RandomAccessFile(binaryFilePath, "rw")) {
            raf.seek(0);
            raf.writeInt(numberOfRecords);
            raf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // retornar número de registros
    public static int getNumeroRegistros(String binaryFilePath) {
        int numberOfRecords = 0;
        try (RandomAccessFile raf = new RandomAccessFile(binaryFilePath, "r")) {
            raf.seek(0);
            numberOfRecords = raf.readInt();
            raf.close();
        } catch (IOException e) {
            System.out.println("Não há registros ou o arquivo não existe.");
        }
        return numberOfRecords;
    }

    // incrementar contador de registros
    public static int incrementarContadorDeRegistros(String binaryFilePath) {
        try (RandomAccessFile raf = new RandomAccessFile(binaryFilePath, "rw")) {
            int numberOfRecords = raf.readInt();
            raf.seek(0);
            raf.writeInt(numberOfRecords + 1);
            raf.close();
            return numberOfRecords + 1;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    // Create
    public static myStruct create(String binaryFilePath, Carro_TP2 registroCarro) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(binaryFilePath, "rw")) {
            boolean registroExiste = false;
            raf.seek(4); // Pula o contador de registros
            while (raf.getFilePointer() < raf.length()) {
                    byte lapide = raf.readByte();
                    int tamanhoRegistro = raf.readInt();
                    if (lapide != 1) {
                        byte[] registroAtualBytes = new byte[tamanhoRegistro];
                        raf.readFully(registroAtualBytes);
                        Carro_TP2 carroExistente = deserializeCarro(registroAtualBytes);
                        if (carroExistente.getId() == registroCarro.getId()) {
                            registroExiste = true;
                            break;
                        }
                    } else {
                        raf.skipBytes(tamanhoRegistro);
                    }
                }
            myStruct retorno = new myStruct();
            if (!registroExiste) {
                raf.seek(raf.length()); // Posiciona o ponteiro no final do arquivo
                retorno.posicao = raf.getFilePointer();
                byte[] registroCarroBytes = serializeCarro(registroCarro);
                raf.writeByte(0); // Escreve a lápide (0 para registro válido, 1 para registro excluído)
                raf.writeInt(registroCarroBytes.length); // Escreve o tamanho do registro
                raf.write(registroCarroBytes); // Escreve o registro em si
                retorno.sucesso = true;
                return retorno;
            } else {
                retorno.sucesso = false;
                retorno.posicao = raf.getFilePointer();
                return retorno;
            }
        }
    }

    // ReadAll - lê todos os registros
    public static void readAll(String binaryFilePath) {
        try (RandomAccessFile raf = new RandomAccessFile(binaryFilePath, "r")) {
            raf.seek(4); // Pula o contador de registros
            while (raf.getFilePointer() < raf.length()) {
                byte lapide = raf.readByte();
                System.out.print("Ponteiro: " + raf.getFilePointer() + " / Tamanho do arquivo: " + raf.length()
                        + " / Lápide: " + lapide);
                if (lapide == 1) {
                    System.out.println(" Registro excluído. Pulando...");
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
                    Carro_TP2 carro = deserializeCarro(recordBytes);
                    System.out.println(carro.toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ReadById - lê um registro específico (recebe o ID do registro como parâmetro)
    public static boolean readById(String binaryFilePath, int carId) {
        boolean isFound = false;
        try (RandomAccessFile raf = new RandomAccessFile(binaryFilePath, "r")) {
            raf.seek(4); // Pula o contador de registros
            while (raf.getFilePointer() < raf.length()) {
                byte lapide = raf.readByte();
                System.out.print("Ponteiro: " + raf.getFilePointer() + " / Tamanho do arquivo: " + raf.length()
                        + " / Lápide: " + lapide);
                if (lapide == 1) {
                    System.out.println(" Registro excluído. Pulando...");
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
                    Carro_TP2 carro = deserializeCarro(recordBytes);
                    if (carro.getId() == carId) {
                        System.out.println(" Registro encontrado: " + carro.toString());
                        isFound = true;
                        return isFound;
                    }
                }
            }
            if (!isFound) {
                System.out.println("Carro com ID " + carId + " não foi encontrado.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return isFound;
    }

    // readByPosicao - lê um registro específico (recebe a posição do registro como parâmetro, além do id para conferir)
    public static boolean readByPosicao(String binaryFilePath, int posicao, int carId) {
        boolean isFound = false;
        try (RandomAccessFile raf = new RandomAccessFile(binaryFilePath, "r")) {
            raf.seek(4); // Pula o contador de registros
            raf.seek(posicao);
            byte lapide = raf.readByte();
            System.out.println("Ponteiro: " + raf.getFilePointer() + " / Tamanho do arquivo: " + raf.length() + " / Lápide: " + lapide);
            if (lapide == 1) {
                System.out.println(" Registro excluído. Pulando...");
                int recordSize = raf.readInt();
                raf.skipBytes(recordSize);
            } else {
                int recordSize = raf.readInt();
                byte[] recordBytes = new byte[recordSize];
                raf.readFully(recordBytes);
                Carro_TP2 carro = deserializeCarro(recordBytes);
                if (carro.getId() == carId) {
                    System.out.println(" Registro encontrado: " + carro.toString());
                    isFound = true;
                    return isFound;
                }
            }
            if (!isFound) {
                System.out.println("Carro com ID " + carId + " não foi encontrado.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return isFound;
    }

    // Update
    public static int update(int id, Carro_TP2 carroAtualizado, String binaryFilePath) throws IOException {
        int novaPos = -1; // posicao atualizada
        RandomAccessFile file = new RandomAccessFile(binaryFilePath, "rw");
        file.seek(4); // Pula o contador de registros
        while (file.getFilePointer() < file.length()) {
            long posicaoAtual = file.getFilePointer();
            byte lapide = file.readByte();
            int tamanhoRegistro = file.readInt();
            if (lapide != 1) { // Checa se o registro não está marcado com lápide
                System.out.println("Lapide: " + lapide + " / Tamanho do registro: " + tamanhoRegistro);
                byte[] registroAtualBytes = new byte[tamanhoRegistro];
                file.readFully(registroAtualBytes);
                // Deserializa o registro para objeto Carro e verifica o ID
                Carro_TP2 carro = deserializeCarro(registroAtualBytes);
                if (carro.getId() == id) {
                    // Atualiza os campos do carro existente com os do carro atualizado
                    updateCarFields(carro, carroAtualizado);
                    byte[] registroAtualizadoBytes = serializeCarro(carro);
                    if (registroAtualizadoBytes.length <= tamanhoRegistro) {
                        // Substitui no local
                        file.seek(posicaoAtual);
                        novaPos = (int) posicaoAtual;
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
                        novaPos = (int) file.getFilePointer();
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
        return novaPos;
    }

    // Delete
    public static boolean deleteById(String binaryFilePath, int carId) {
        try (RandomAccessFile raf = new RandomAccessFile(binaryFilePath, "rw")) {
            raf.seek(4); // Pula o contador de registros
            boolean isFound = false;
            while (raf.getFilePointer() < raf.length()) {
                long recordStart = raf.getFilePointer();
                byte lapide = raf.readByte();
                int recordSize = raf.readInt();
                if (lapide == 0) {
                    byte[] recordBytes = new byte[recordSize];
                    raf.readFully(recordBytes); // Lê o registro
                    Carro_TP2 carro = deserializeCarro(recordBytes); // Desserializa
                    if (carro.getId() == carId) {
                        isFound = true;
                        raf.seek(recordStart); // Volta para o início do registro
                        raf.writeByte(1); // Atualiza a lápide para indicar que o registro foi excluído
                        System.out.println("Carro com ID " + carId + " foi deletado.");
                        return true;
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
        return false;
    }

    // Métodos auxiliares

    // getLastAddress
    public static long getLastAddress(String binaryFilePath) {
        long lastAddress = 0;
        try (RandomAccessFile raf = new RandomAccessFile(binaryFilePath, "r")) {
            raf.seek(4); // Pula o contador de registros
            while (raf.getFilePointer() < raf.length()) {
                byte lapide = raf.readByte();
                int recordSize = raf.readInt();
                if (lapide == 0) {
                    lastAddress = raf.getFilePointer();
                    raf.skipBytes(recordSize);
                } else {
                    raf.skipBytes(recordSize);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lastAddress;
    }

    // Serializar um objeto RegistroCarro para um array de bytes
    private static byte[] serializeCarro(Carro_TP2 registroCarro) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(registroCarro.getId());
        dos.writeUTF(registroCarro.getCarMake());
        dos.writeUTF(registroCarro.getCarModel());
        String[] hpTorque = registroCarro.getHp_Torque();
        dos.writeInt(hpTorque.length); // Escreve o tamanho do array
        for (String hp : hpTorque) {
            dos.writeUTF(hp); // Escreve cada elemento do array
        }
        dos.writeLong(registroCarro.getDateInMilliseconds());
        dos.writeFloat(registroCarro.getZeroToSixty());
        dos.writeFloat(registroCarro.getPrice());
        return baos.toByteArray();
    }

    // Desserializar um array de bytes para um objeto RegistroCarro
    private static Carro_TP2 deserializeCarro(byte[] data) throws IOException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
                DataInputStream dis = new DataInputStream(bais)) {
            int id = dis.readInt();
            String carMake = dis.readUTF();
            String carModel = dis.readUTF();
            // Deserialização de hp_Torque
            int hpTorqueSize = dis.readInt(); // Lê o tamanho do array
            String[] hpTorque = new String[hpTorqueSize];
            for (int i = 0; i < hpTorqueSize; i++) {
                hpTorque[i] = dis.readUTF(); // Lê cada elemento do array
            }
            long dateInMilliseconds = dis.readLong();
            float zeroToSixty = dis.readFloat();
            float price = dis.readFloat();

            String dateString = DateConverter_TP2.convertMillisToDate(dateInMilliseconds);

            return new Carro_TP2(id, carMake, carModel, hpTorque, dateString, zeroToSixty, price);
        }
    }

    // Update para lidar com atualizações parciais
    private static void updateCarFields(Carro_TP2 carro, Carro_TP2 carroAtualizado) {
        // if (carroAtualizado.getId() > -1) {
        //     carro.setId(carroAtualizado.getId());
        // }
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
}
