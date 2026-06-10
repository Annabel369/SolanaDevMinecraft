import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class TestAPI {
    public static void main(String[] args) {
        String host = "localhost";
        String apiwebkey = "b493d48364afe44d";
        String solanaCmd = "solana"; // Using solana instead of heysolana for testing
        String walletAddress = "dadhcDXHiHDrWkT2Z4pSZyF6HWmHwQMG3HtGciwccVP";
        
        try {
            String comando = solanaCmd + " balance " + walletAddress;
            String urlString = String.format("http://%s/consulta.php?apikey=%s&comando=%s", 
                host, apiwebkey, URLEncoder.encode(comando, StandardCharsets.UTF_8));
            
            System.out.println("Testando URL: " + urlString);
            
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            
            System.out.println("Resposta da API: " + response.toString());
            
            if (response.toString().contains("\"status\":\"success\"")) {
                System.out.println("TESTE BEM SUCEDIDO!");
            } else {
                System.out.println("TESTE FALHOU!");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
