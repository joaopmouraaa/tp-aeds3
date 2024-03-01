import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.text.ParseException;

public class DateConverter {
    public static long convertDateToMillis(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        try {
            Date date = sdf.parse(dateString);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            return calendar.getTimeInMillis();
        } catch (ParseException e) {
            e.printStackTrace();
            return -1; // Retorna -1 em caso de erro de análise
        }
    }

    public static String convertMillisToDate(long dateInMilliseconds) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date(dateInMilliseconds);
        return sdf.format(date);
    }

    /*
     * public static void main(String[] args) {
     * String dateString = "01/01/2022"; // Exemplo de data
     * long millis = convertDateToMillis(dateString);
     * System.out.println("Data em milissegundos: " + millis);
     * }
     */
}
