import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class CurlApp {
    public static void main(String[] args) throws InterruptedException  {
        // ①HttpClientを生成
        HttpClient cli = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();
        // ②HttpRequestを生成
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create("https://java-code.jp/"))
            .build();
        // ③リクエストを送信
        cli.sendAsync(req, HttpResponse.BodyHandlers.ofString())
          .thenAccept(res -> {
            System.out.println(res.body());
          });
         
        Thread.sleep(3000);
      }
}
