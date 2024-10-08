import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class CurlApp {
    public static void main(String[] args) throws InterruptedException  {
        //それぞれのコマンドが入力されたかを判定する変数
        //この変数の値によりサーバーからレスポンスをもらった際の処理を変える
        boolean specify_req_method = false;
        boolean specify_data = false;
        boolean output_file = false;
        boolean verbose = false;

        //コマンドライン引数をチェック
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-X":
                    //リクエストメソッドを指定する
                    specify_req_method = true;
                    System.out.println("コマンドX");
                    break;
                case "-d":
                    specify_data = true;
                    System.out.println("コマンドd");
                    break;
                case "-o":
                    output_file = true;
                    System.out.println("コマンドo");
                    break;
                case "-v":
                    verbose = true;
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
