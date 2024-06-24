import java.io.*;

class myStruct {
    public boolean sucesso;
    public long posicao;
}

public class CRUD {

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
            System.out.print("Não há registros no arquivo ou o arquivo não existe. ");
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
    // Create All
    public static void createDatabaseFromCSV(String csvFilePath, String binaryFilePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            inicializarContadorDeRegistros(binaryFilePath);
            br.readLine(); // Pula a primeira linha (cabeçalho)
            while ((line = br.readLine()) != null) {
                String[] values = line.split(";");
                int id = Integer.parseInt(values[0]);
                String carMake = values[1];
                String carModel = values[2];
                String[] hpTorque = new String[2];
                hpTorque[0] = values[3].split(",")[0];
                hpTorque[1] = values[3].split(",")[1].substring(0, values[3].split(",")[1].length());
                String date = values[4];
                float zeroToSixty = Float.parseFloat(values[5]);
                String priceTemp = values[6];
                priceTemp = priceTemp.replace(",", "");
                float price = Float.parseFloat(priceTemp);
                Carro carro = new Carro(id, carMake, carModel, hpTorque, date, zeroToSixty, price);
                if (create(binaryFilePath, carro).sucesso) {
                    try (RandomAccessFile raf = new RandomAccessFile(binaryFilePath, "rw")) {
                        raf.seek(0);
                        int numberOfRecords = raf.readInt();
                        raf.seek(0);
                        raf.writeInt(numberOfRecords + 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Create
    public static myStruct create(String binaryFilePath, Carro registroCarro) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(binaryFilePath, "rw")) {
            boolean registroExiste = false;
            raf.seek(4); // Pula o contador de registros
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
                    Carro carro = deserializeCarro(recordBytes);
                    System.out.println(carro.toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readAlltoString(String binaryFilePath) {
        String retorno = "";
        try (RandomAccessFile raf = new RandomAccessFile(binaryFilePath, "r")) {
            raf.seek(4); // Pula o contador de registros
            while (raf.getFilePointer() < raf.length()) {
                byte lapide = raf.readByte();
                // System.out.print("Ponteiro: " + raf.getFilePointer() + " / Tamanho do arquivo: " + raf.length()
                        // + " / Lápide: " + lapide);
                if (lapide == 1) {
                    // System.out.println(" Registro excluído. Pulando...");
                    int recordSize = raf.readInt();
                    raf.skipBytes(recordSize);
                    continue;
                }
                int recordSize = raf.readInt();
                // System.out.println(" / Tamanho do registro: " + recordSize);
                byte[] recordBytes = new byte[recordSize];
                try {
                    raf.readFully(recordBytes);
                } catch (EOFException e) {
                }
                if (lapide == 0) {
                    Carro carro = deserializeCarro(recordBytes);
                    // System.out.println(carro.toString()); // comentar essa linha
                    retorno += carro.toString2() + "\n";
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // System.out.println("Retorno: " + retorno);
        // System.out.println("Retornando a função readAlltoString");
        return retorno;
    }

    // ReadById - lê um registro específico (recebe o ID do registro como parâmetro)
    public static boolean readById(String binaryFilePath, int carId) {
        boolean isFound = false;
        try (RandomAccessFile raf = new RandomAccessFile(binaryFilePath, "r")) {
            raf.seek(4); // Pula o contador de registros
            while (raf.getFilePointer() < raf.length()) {
                byte lapide = raf.readByte();
                // System.out.print("Ponteiro: " + raf.getFilePointer() + " / Tamanho do arquivo: " + raf.length()
                        // + " / Lápide: " + lapide);
                if (lapide == 1) {
                    // System.out.println(" Registro excluído. Pulando...");
                    int recordSize = raf.readInt();
                    raf.skipBytes(recordSize);
                    continue;
                }
                int recordSize = raf.readInt();
                // System.out.println(" / Tamanho do registro: " + recordSize);
                byte[] recordBytes = new byte[recordSize];
                try {
                    raf.readFully(recordBytes);
                } catch (EOFException e) {
                }
                if (lapide == 0) {
                    Carro carro = deserializeCarro(recordBytes);
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

    // readByPosicao - lê um registro específico (recebe a posição do registro como
    // parâmetro, além do id para conferir)
    public static boolean readByPosicao(String binaryFilePath, int posicao, int carId) {
        boolean isFound = false;
        try (RandomAccessFile raf = new RandomAccessFile(binaryFilePath, "r")) {
            raf.seek(4); // Pula o contador de registros
            raf.seek(posicao);
            byte lapide = raf.readByte();
            System.out.println("Ponteiro: " + raf.getFilePointer() + " / Tamanho do arquivo: " + raf.length()
                    + " / Lápide: " + lapide);
            if (lapide == 1) {
                System.out.println(" Registro excluído. Pulando...");
                int recordSize = raf.readInt();
                raf.skipBytes(recordSize);
            } else {
                int recordSize = raf.readInt();
                byte[] recordBytes = new byte[recordSize];
                raf.readFully(recordBytes);
                Carro carro = deserializeCarro(recordBytes);
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
    public static int update(int id, Carro carroAtualizado, String binaryFilePath) throws IOException {
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
                Carro carro = deserializeCarro(registroAtualBytes);
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
                    Carro carro = deserializeCarro(recordBytes); // Desserializa
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
    private static byte[] serializeCarro(Carro registroCarro) throws IOException {
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

    // Retornar um vetor de bytes a partir do arquivo binário
    public static byte[] getBytesFromFile(String binaryFilePath) {
        byte[] data = null;
        try (RandomAccessFile raf = new RandomAccessFile(binaryFilePath, "r")) {
            data = new byte[(int) raf.length()];
            raf.readFully(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    // Serializar um registro inteiro para um array de bytes
    public static String BinaryToString(String binaryFilePath) {
        // try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
        // DataInputStream dis = new DataInputStream(bais)) {
        StringBuilder retorno = new StringBuilder();
        try (RandomAccessFile raf = new RandomAccessFile(binaryFilePath, "r")) {
            raf.seek(0);
            int numberOfRecords = raf.readInt();
            retorno.append(numberOfRecords);
            retorno.append('|');
            // System.out.println("(BinaryToString) Número de registros: " + numberOfRecords);
            raf.seek(4); // Pula o contador de registros
            while (raf.getFilePointer() < raf.length()) {
                byte lapide = raf.readByte();
                retorno.append(lapide);
                retorno.append('|');
                // System.out.println("Ponteiro: " + raf.getFilePointer() + " / Tamanho do
                // arquivo: " + raf.length());
                int recordSize = raf.readInt();
                retorno.append(recordSize);
                retorno.append('|');
                byte[] recordBytes = new byte[recordSize];
                try {
                    raf.readFully(recordBytes);
                    // System.out.println("Lapide: " + lapide + " Tamanho: " + recordSize + "
                    // Registro: " + new String(recordBytes));
                    Carro carro = deserializeCarro(recordBytes);
                    retorno.append(carro.getId());
                    retorno.append('|');
                    retorno.append(carro.getCarMake());
                    retorno.append('|');
                    retorno.append(carro.getCarModel());
                    retorno.append('|');
                    for (String hp : carro.getHp_Torque()) {
                        retorno.append(hp);
                        retorno.append(',');
                    }
                    retorno.append('|');
                    retorno.append(carro.getDateInMilliseconds());
                    retorno.append('|');
                    retorno.append(carro.getZeroToSixty());
                    retorno.append('|');
                    retorno.append(carro.getPrice());
                    retorno.append(';');
                    // System.out.println(carro.toString()); // comentar essa linha
                } catch (EOFException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return retorno.toString();
    }

    public static void StringToBinary(String str, String outputFilePath) {
        try (RandomAccessFile raf = new RandomAccessFile(outputFilePath, "rw")) {
            raf.setLength(0); // Limpando arquivo
            String numeroRegistros = "";
            while (str.charAt(0) != '|') {
                numeroRegistros += str.charAt(0);
                str = str.substring(1);
            }
            int numberOfRecords = Integer.parseInt(numeroRegistros);
            // System.out.println(str.substring(0, 9) + " 10 primeiros caracteres");
            // avançar cursor uma posição
            // System.out.println("(StringToBinary) Número de registros: " + numberOfRecords);
            raf.seek(0);
            raf.writeInt(numberOfRecords);
            // reader.readLine(); // comentar
            str = str.substring(1);
            // System.out.println(str);
            raf.seek(4); // Pula o contador de registros
            String[] registros = str.split(";");
            for (String registro : registros) {
                try {
                    // System.out.println("Registro: " + registro);
                    byte lapide = (byte) Integer.parseInt(registro.charAt(0) + "");
                    raf.seek(raf.length());
                    raf.writeByte(lapide);
                    // reader.readLine(); // comentar
                    // System.out.println("Lapide: " + registro.charAt(0) + " / (" + lapide + ") ");
                    String[] campos = registro.substring(1).split("\\|");
                    int contadorDeCampos = 0;
                    for (String campo : campos) {
                        // System.out.print("Campo " + contadorDeCampos + ": " + campo + "  ");
                        contadorDeCampos++;
                    }
                    // System.out.println();
                    int recordSize = Integer.parseInt(campos[1]);
                    raf.writeInt(recordSize);
                    // reader.readLine(); // comentar
                    int id = Integer.parseInt(campos[2]);
                    // System.out.println("Tamanho: " + recordSize + " ID: " + id);
                    String carMake = campos[3];
                    String carModel = campos[4];
                    String[] hpTorque = new String[2];
                    hpTorque[0] = campos[5].split(",")[0];
                    hpTorque[1] = campos[5].split(",")[1].substring(0, campos[5].split(",")[1].length());
                    String stringDateInMilliseconds = campos[6];
                    // long dateInMilliseconds = Long.parseLong(stringDateInMilliseconds);
                    String stringZeroToSixty = campos[7];
                    float zeroToSixty = Float.parseFloat(stringZeroToSixty);
                    String stringPrice = campos[8];
                    float price = Float.parseFloat(stringPrice);
                    Carro carro = new Carro(id, carMake, carModel, hpTorque[0], hpTorque[1], stringDateInMilliseconds,
                            zeroToSixty, price);
                    // System.out.println(carro.toString2());
                    // System.out.println(carro.toString());
                    byte[] carroSerializado = serializeCarro(carro);
                    // raf.writeInt(carroSerializado.length);
                    raf.write(carroSerializado);
                } catch (ArrayIndexOutOfBoundsException e) {
                    // System.out.println("Fim do arquivo.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Desserializar um array de bytes para um objeto RegistroCarro
    private static Carro deserializeCarro(byte[] data) throws IOException {
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

            String dateString = Aplicacao.convertMillisToDate(dateInMilliseconds);

            return new Carro(id, carMake, carModel, hpTorque, dateString, zeroToSixty, price);
        }
    }

    // Update para lidar com atualizações parciais
    private static void updateCarFields(Carro carro, Carro carroAtualizado) {
        // if (carroAtualizado.getId() > -1) {
        // carro.setId(carroAtualizado.getId());
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