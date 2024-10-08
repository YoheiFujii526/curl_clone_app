import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class CurlApp {
    public static void main(String[] args) throws InterruptedException  {

        //GETかPOST.デフォルトはGET
        String mode = "GET";
        //それぞれのコマンドが入力されたかを判定する変数
        //この変数の値によりサーバーからレスポンスをもらった際の処理を変える
        boolean specify_req_method = false;
        boolean specify_data = false;
        boolean output_file = false;
        boolean verbose = false;

        //URL
        String url = "";

        //コマンドライン引数をチェック
        for (int i = 0; i < args.length; i++) {
            System.out.println(i + "番目の引数" + args[i]);
            switch (args[i]) {
                case "-X":
                    //リクエストメソッドを指定する
                    specify_req_method = true;
                    System.out.println("コマンドX");

                    //GETかPOSTかを決める
                    if(i < args.length - 1){
                        if (args[i+1].equals("GET") || args[i+1].equals("POST")) {
                            mode = args[i];
                            System.out.println("モードを決定");
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
                    verbose = true;
                    System.out.println("コマンドv");
                    break;
                default:
                    //コマンドに"-"が付いてない
                    if(!args[i].startsWith("-")) {
                        if(args[i].startsWith("http://") || args[i].startsWith("https://")) {
                            url = args[i];
                            System.out.println("URLを取得:" + url);
                        }
                    }
                    break;
            }
        }
        if (true) {
            // ①HttpClientを生成
            HttpClient cli = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();
            

            //-Xコマンドが使われていた場合,モードをGETからPOSTに切り替える
            if (specify_req_method) {
                mode = "POST";
            }


            //GETの場合
            if (mode.equals("GET")) {
                // ②HttpRequestを生成
                HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    //.POST(HttpRequest.BodyPublishers.ofString("some body text"))
                    .build();

                // ③リクエストを送信
                cli.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenAccept(res -> {
                    //bodyの部分はheaders, statusCode, uriに変更できる
                    System.out.println(res.request());
                });

                
            //POSTの場合
            } else if(mode.equals("POST")) {
                // ②HttpRequestを生成
                HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .POST(HttpRequest.BodyPublishers.ofString("some body text"))
                    .build();

                // ③リクエストを送信
                cli.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenAccept(res -> {
                    //bodyの部分はheaders, statusCode, uriに変更できる
                    System.out.println(res.request());
                });
            }

            
            
            Thread.sleep(3000);
        }
    }
}
