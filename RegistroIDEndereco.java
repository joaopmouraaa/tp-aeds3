import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class RegistroIDEndereco implements RegistroHashExtensivel<RegistroIDEndereco> {
    private static final int ID_SIZE = 4; // tamanho fixo para id
    public int id;
    public int endereco;

    public RegistroIDEndereco() {
    }

    public RegistroIDEndereco(int id, int endereco) {
        if (id < 0) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        this.id = id;
        this.endereco = endereco;
    }

    public int getId() {
        return id;
    }

    public int getEndereco() {
        return endereco;
    }

    // Método para desserializar um array de bytes em um objeto RegistroIDEndereco
    @Override
    public void fromByteArray(byte[] ba) throws IOException {
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(ba))) {
            this.id = dis.readInt();
            this.endereco = dis.readInt();
        }
    }

    // Método para serializar o objeto em um array de bytes
    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(id);
        dos.writeInt(endereco);
        // imprimir a sequênciad e bytes na tela
        System.out.println("Sequência de bytes do registro de id "+id+" e endereço "+endereco+" "+Arrays.toString(baos.toByteArray()));
        return baos.toByteArray();
    }

    // Método para calcular o hash code do objeto, tratando 'id' null
    @Override
    public int hashCode() {
        return id > -1 ? id : -1;
    }

    // Método para calcular o tamanho do objeto em bytes, tratando 'id' null
    @Override
    public short size() {
        return (short) (ID_SIZE + 4); // ID_SIZE bytes para o id + 4 bytes para o endereco
    }

    // toString
    @Override
    public String toString() {
        return "RegistroIDEndereco{" +
                "id=" + id +
                ", endereco=" + endereco +
                '}';
    }
}
