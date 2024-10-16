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
import java.util.Iterator;//コレクション内の各要素に順番にアクセスするためのオブジェクトです
import java.util.concurrent.atomic.AtomicBoolean; // Iteratorのimportを忘れずに


public class CurlApp {
    public static void main(String[] args) throws InterruptedException  {

        
        String mode = "GET";//GETかPOST.デフォルトはGET
        String send_data = "key=value";//-dの時に送るデータ
        StringBuilder print_data = new StringBuilder ("");//コンソールに表示するデータを格納する変数
        //それぞれのコマンドが入力されたかを判定する変数
        //この変数の値によりサーバーからレスポンスをもらった際の処理を変える
        AtomicBoolean specify_req_method = new AtomicBoolean(false);//-Xのチェック
        AtomicBoolean specify_data = new AtomicBoolean(false);//-dのチェック
        //ラムダ式内で使えるようにする
        AtomicBoolean output_file = new AtomicBoolean(false);//-oのチェック
        AtomicBoolean verbose = new AtomicBoolean(false);//-vのチェック

       
        String url = ""; //URL
        Integer http_ver = 2;//HTTPバージョン


        //コマンドライン引数をチェック
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                //リクエストメソッドを指定する
                case "-X":
                    specify_req_method.set(true);
                    //GETかPOSTかを決める
                    //-Xの後ろの引数を確認して、GETかPOSTか決める
                    if(i < args.length - 1){
                        if (args[i+1].equals("GET") || args[i+1].equals("POST")) {
                            mode = args[i+1];// 指定されたメソッドを設定
                        }
                    }
                    break;
                //POSTデータを指定する
                case "-d":
                    //-Xコマンドが使われたかどうかと、リクエスト方法がPOSTかどうか、
                    if(specify_req_method.get() && mode.equals("POST")) {
                        //-dコマンドの次の入力がデータかどうか
                        if (!args[i+1].startsWith("-") || !(args[i].startsWith("http://") || args[i].startsWith("https://"))) {
                            send_data = args[i+1];// 送信するデータを設定
                        }
                    }
                    specify_data.set(true);
                    break;
                //出力をファイルに書き込み、出力する
                case "-o":
                    output_file.set(true);// outputモードを有効にする
                    break;
                //詳細な出力をする
                case "-v":
                    verbose.set(true);// verboseモードを有効にする
                    break;
                default:
                    //コマンドに"-"が付いてない
                    if(!args[i].startsWith("-")) {
                        //URLかどうかを判定
                        //引数の最初の数文字で判定
                        if(args[i].startsWith("http://") || args[i].startsWith("https://")) {
                            url = args[i];//urlを設定
                        }
                    }
                    break;
            }
        }
        // HttpRequest.Builderの作成
        HttpRequest req  = HttpRequest.newBuilder().uri(URI.create(url)).build();
        // ①HttpClientインスタンスの生成
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
                    .POST(HttpRequest.BodyPublishers.ofString(send_data))// POSTリクエスト用にBodyPublishers.ofStringでデータを設定
                    .build();
            } else {
                req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .POST(HttpRequest.BodyPublishers.noBody())// データなしでPOSTリクエスト
                    .build();
            }
        }

        // verboseモード: リクエストの詳細を出力
        if (verbose.get()) {
                // URIからホスト名を取得
                URI uri = URI.create(url);
                String host = uri.getHost();
            try {
                // ホスト名からIPアドレスを取得
                InetAddress ipAddress = InetAddress.getByName(host);
                String ip = ipAddress.getHostAddress();

                //コネクトメッセージを作成・print_dataに追加
                StringBuilder connect_message = new StringBuilder ("\n* Trying " + ip + "..." + "\n* Connected to " + host);
                print_data.append(connect_message);
    
            } catch (Exception e) {
                ///エラーのとき
                System.err.println("IPアドレスでエラー");
            }

            //method,host,content-typeのメッセージを作成・print_dataに追加
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

            //verboseモードの時
            if (verbose.get()) {
                //responsecodeのメッセージをを作成・print_dataに追加
                StringBuilder res_code = new StringBuilder ("\n< Response Code: " + res.statusCode());

                //header情報を格納する変数
                StringBuilder header_data = new StringBuilder ("");

                //headerの中身が1行で出力されるので、一つづつ改行して表示されるようにする
                //mapデータ型のkeyとvalueをfor文で出力
                //res.headers().map(): これは、HTTPレスポンスからヘッダーを取得するメソッド
                //itr.hasNext() : Iteratorが次の要素が存在するかどうかを確認するメソッド,存在する限りループする
                for (Iterator<String> itr = res.headers().map().keySet().iterator(); itr.hasNext();) {
                    String key = itr.next();
                    //ヘッダー情報を1つの文字列に連結
                    StringBuilder r_head = new StringBuilder ("\n< " + key + " : " + res.headers().map().get(key));
                    header_data.append(r_head);
                }
                StringBuilder res_Headers = new StringBuilder ("\n< Response Headers: " + header_data);
                print_data.append(res_code);
                print_data.append(res_Headers);
            }

            //output_fileモードの時
            if (output_file.get()) {
                try {
                    // 現在日時を取得
                    LocalDateTime nowDate = LocalDateTime.now();
                    // 日時の表示形式を指定
                    DateTimeFormatter dtf3 = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
                    //フォーマットに従って文字列に変換
                    String formatNowDate = dtf3.format(nowDate);
                    String file_name = "output_" + formatNowDate + ".txt";
                    System.out.println(file_name); 
                    //ファイル作成
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

            //アクセスしたページのbodyをprint_dataに追加
            StringBuilder res_body = new StringBuilder ("\n< Response body: \n" + res.body());
            print_data.append(res_body);
            //コンソールにprint_dataを出力
            System.out.println(print_data);
        });
        Thread.sleep(3000);
    }
}
