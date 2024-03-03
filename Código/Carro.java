import java.io.Serializable;
//import java.util.Date;
import java.util.Arrays;

public class Carro implements Serializable {
    // Atributos da classe
    private int id; // String de tamanho fixo
    private String carMake; // String de tamanho variável (Marca)
    private String carModel; // String de tamanho variável (Modelo)
    private String[] hp_Torque; // Lista de valores com separador (Horsepower_Torque)
    private String date; // Data
    private long dateInMilliseconds;
    private float zeroToSixty; // Float (0-60 MPH Time (seconds))
    private float price; // Float (Preço)

    // Construtores

    public Carro() {
        //
    }

    public Carro(int id, String carMake, String carModel, String[] hpTorque, String dateString, float zeroToSixty,
            float price) {
        this.id = id;
        this.carMake = carMake;
        this.carModel = carModel;
        this.hp_Torque = hpTorque;
        this.date = dateString;
        this.dateInMilliseconds = DateConverter.convertDateToMillis(dateString);
        this.zeroToSixty = zeroToSixty;
        this.price = price;
    }

    // Getters e setters para cada atributo
    public int getId() {
        return id;
    }

    public String getCarMake() {
        return carMake;
    }

    public String getCarModel() {
        return carModel;
    }

    public String[] getHp_Torque() {
        return hp_Torque;
    }

    public String getDate() {
        return date;
    }

    public long getDateInMilliseconds() {
        return dateInMilliseconds;
    }

    public float getZeroToSixty() {
        return zeroToSixty;
    }

    public float getPrice() {
        return price;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCarMake(String carMake) {
        this.carMake = carMake;
    }

    public void setCarModel(String carModel) {
        this.carModel = carModel;
    }

    public void setHp_Torque(String[] hp_Torque) {
        this.hp_Torque = hp_Torque;
    }

    public void setDate(String date) {
        this.date = date;
        this.dateInMilliseconds = DateConverter.convertDateToMillis(date);
    }

    public void setDateInMilliseconds(long dateInMilliseconds) {
        this.dateInMilliseconds = dateInMilliseconds;
    }

    public void setZeroToSixty(float zeroToSixty) {
        this.zeroToSixty = zeroToSixty;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    // Método para converter o objeto em uma string (para fins de visualização)
    @Override
    public String toString() {
        return "{" +
                "id='" + id + '\'' +
                ", carMake='" + carMake + '\'' +
                ", carModel='" + carModel + '\'' +
                ", hp_Torque='" + Arrays.toString(hp_Torque) + '\'' + // Alterado para usar Arrays.toString()
                ", date='" + date + '\'' +
                ", dateInMilliseconds='" + dateInMilliseconds + '\'' +
                ", zeroToSixty=" + zeroToSixty +
                ", price=" + price +
                '}';
    }

    // Métodos adicionais para serialização e desserialização podem ser adicionados
    // ...
}
