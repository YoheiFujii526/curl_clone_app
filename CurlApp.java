import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean; // Iteratorのimportを忘れずに


public class CurlApp {
    public static void main(String[] args) throws InterruptedException  {

        //GETかPOST.デフォルトはGET
        String mode = "GET";
        String send_data = "key=value";//-dの時に送るデータ
        StringBuilder print_data = new StringBuilder ("");//コンソールに表示するデータを格納する変数
        //それぞれのコマンドが入力されたかを判定する変数
        //この変数の値によりサーバーからレスポンスをもらった際の処理を変える
        AtomicBoolean specify_req_method = new AtomicBoolean(false);//-Xのチェック
        AtomicBoolean specify_data = new AtomicBoolean(false);//-dのチェック
        //ラムダ式内で使えるようにする
        AtomicBoolean output_file = new AtomicBoolean(false);//-oのチェック
        AtomicBoolean verbose = new AtomicBoolean(false);//-vのチェック

        //URL
        String url = "";
        //HTTPバージョン
        Integer http_ver = 2;

        //コマンドライン引数をチェック
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-X":
                    //リクエストメソッドを指定する
                    specify_req_method.set(true);
                    //System.out.println("コマンドX");

                    //GETかPOSTかを決める
                    if(i < args.length - 1){
                        if (args[i+1].equals("GET") || args[i+1].equals("POST")) {
                            mode = args[i+1];
                        }
                    }
                    break;
                case "-d":
                    //-Xコマンドが使われたかどうかと、リクエスト方法がPOSTかどうか、
                    if(specify_req_method.get() && mode.equals("POST")) {
                        //-dコマンドの次の入力がデータかどうか
                        if (!args[i+1].startsWith("-") || !(args[i].startsWith("http://") || args[i].startsWith("https://"))) {
                            send_data = args[i+1];
                        }
                    }
                    specify_data.set(true);
                    //System.out.println("コマンドd");
                    break;
                case "-o":
                    output_file.set(true);
                    //System.out.println("コマンドo");
                    break;
                case "-v":
                    verbose.set(true);
                    //System.out.println("コマンドv");
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
            //データありでPOSTをする場合
            if (specify_data.get()) {
                req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .POST(HttpRequest.BodyPublishers.ofString(send_data))
                    .build();
            } else {
                req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .POST(HttpRequest.BodyPublishers.noBody())// データなしでPOSTリクエスト
                    .build();
            }
        }

        // Verboseモード: リクエストの詳細を出力
        if (verbose.get()) {

                // URIからホスト名を取得
                URI uri = URI.create(url);
                String host = uri.getHost();
            try {
            
    
                // ホスト名からIPアドレスを取得
                InetAddress ipAddress = InetAddress.getByName(host);
                String ip = ipAddress.getHostAddress();

                StringBuilder connect_message = new StringBuilder ("\n* Trying " + ip + "..." + "\n* Connected to " + host);
                print_data.append(connect_message);
    
            } catch (Exception e) {
                System.err.println("IPアドレスでエラー");
            }
            StringBuilder req_method = new StringBuilder ("\n> Request method: " + req.method() + " / HTTP/" + http_ver);
            StringBuilder req_host = new StringBuilder ("\n> Host: " + host);
            StringBuilder req_content_type = new StringBuilder ("\n> Accept: */*\n>");
            print_data.append(req_method);
            print_data.append(req_host);
            print_data.append(req_content_type);
        }

        
        // ③リクエストを送信
        //bodyの部分はheaders, statusCode, uriに変更できる
        cli.sendAsync(req, HttpResponse.BodyHandlers.ofString())
        .thenAccept(res -> {
            if (verbose.get()) {
                StringBuilder res_code = new StringBuilder ("\n< Response Code: " + res.statusCode());
                StringBuilder header_data = new StringBuilder ("");//header情報を格納する変数
                //mapデータ型のkeyとvalueをfor文で出力
                for (Iterator<String> itr = res.headers().map().keySet().iterator(); itr.hasNext();) {
                    String key = itr.next();
                    StringBuilder r_head = new StringBuilder ("\n< " + key + " : " + res.headers().map().get(key));
                    header_data.append(r_head);
                }
                StringBuilder res_Headers = new StringBuilder ("\n< Response Headers: " + header_data);
                print_data.append(res_code);
                print_data.append(res_Headers);
            }
            if (output_file.get()) {
                try {
                    // 現在日時を取得
                    LocalDateTime nowDate = LocalDateTime.now();
                    // 表示形式を指定
                    DateTimeFormatter dtf3 = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
                    String formatNowDate = dtf3.format(nowDate);
                    String file_name = "output_" + formatNowDate + ".txt";
                    System.out.println(file_name); 
                    File file = new File(file_name);
	
                    //createNewFileメソッドを使用してファイルを作成する
                    if (file.createNewFile()){
                        System.out.println("ファイル作成成功");
                        // FileWriterクラスを使用する
                        FileWriter writefile = new FileWriter(file);
                        // PrintWriterクラスを使用する
                        PrintWriter pw = new PrintWriter(new BufferedWriter(writefile));
                        
                        //ファイルに書き込む
                        pw.println(print_data + res.body());
                        //ファイルを閉じる
                        pw.close();
                    }else{
                        System.out.println("ファイル作成失敗");
                    }
                } catch (IOException e) {
                    System.err.println("エラー:" + e);
                }
            }
            StringBuilder res_body = new StringBuilder ("\n< Response body: \n" + res.body());
            print_data.append(res_body);
            System.out.println(print_data);
        });
        Thread.sleep(3000);
    }
}
