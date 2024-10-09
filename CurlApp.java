import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.atomic.AtomicBoolean;

public class CurlApp {
    public static void main(String[] args) throws InterruptedException  {

        //GETかPOST.デフォルトはGET
        String mode = "GET";
        //それぞれのコマンドが入力されたかを判定する変数
        //この変数の値によりサーバーからレスポンスをもらった際の処理を変える
        boolean specify_req_method = false;
        boolean specify_data = false;
        boolean output_file = false;
        AtomicBoolean verbose = new AtomicBoolean(false);//ラムダ式内で使えるようにする

        //URL
        String url = "";

        //コマンドライン引数をチェック
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-X":
                    //リクエストメソッドを指定する
                    specify_req_method = true;
                    System.out.println("コマンドX");

                    //GETかPOSTかを決める
                    if(i < args.length - 1){
                        if (args[i+1].equals("GET") || args[i+1].equals("POST")) {
                            mode = args[i+1];
                        }
                    }
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
                    verbose.set(true);
                    System.out.println("コマンドv");
                    break;
                default:
                    //コマンドに"-"が付いてない
                    if(!args[i].startsWith("-")) {
                        //URLかどうかを判定
                        if(args[i].startsWith("http://") || args[i].startsWith("https://")) {
                            url = args[i];
                        }
                    }
                    break;
            }
        }

        HttpRequest req  = HttpRequest.newBuilder().uri(URI.create(url)).build();
        if (true) {
            // ①HttpClientを生成
            HttpClient cli = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();

            //GETの場合
            if (mode.equals("GET")) {
                // ②HttpRequestを生成
                req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .build();
            //POSTの場合
            } else if(mode.equals("POST")) {
                // ②HttpRequestを生成
                req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    //.POST(HttpRequest.BodyPublishers.ofString("some body text"))
                    .POST(HttpRequest.BodyPublishers.noBody())// データなしでPOSTリクエスト
                    .build();
            }

            
            // ③リクエストを送信
            //bodyの部分はheaders, statusCode, uriに変更できる
            cli.sendAsync(req, HttpResponse.BodyHandlers.ofString())
            .thenAccept(res -> {
                if (verbose.get()) {
                    System.out.println("Response Code: " + res.statusCode());
                    System.out.println("Response Headers: " + res.headers());
                }
                //System.out.println(res.statusCode());
                System.out.println(res.body());
            });
            
            // verboseモードでレスポンス情報を表示
            /*if (verbose) {
                System.out.println("Response Code: " + response.statusCode());
                System.out.println("Response Headers: " + response.headers());
            }*/

            
            
            Thread.sleep(3000);
        }
    }
}
