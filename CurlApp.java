import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class CurlApp {
    public static void main(String[] args) throws InterruptedException  {
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-X":
                    System.out.println("コマンドX");
                    break;
                case "-d":
                    System.out.println("コマンドd");
                    break;
                case "-o":
                    System.out.println("コマンドo");
                    break;
                case "-v":
                    System.out.println("コマンドv");
                    break;
                default:
                    
                    break;
            }
        }
        if (true) {
            // ①HttpClientを生成
            HttpClient cli = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();
            // ②HttpRequestを生成
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://abehiroshi.la.coocan.jp/"))
                .build();
            // ③リクエストを送信
            cli.sendAsync(req, HttpResponse.BodyHandlers.ofString())
            .thenAccept(res -> {
                //bodyの部分はheaders, statusCode, uriに変更できる
                System.out.println(res.body());
            });
            
            Thread.sleep(3000);
        }
    }
}
