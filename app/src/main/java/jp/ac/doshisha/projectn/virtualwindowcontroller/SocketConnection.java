package jp.ac.doshisha.projectn.virtualwindowcontroller;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * UWP擬似窓システムとのソケット通信実装用クラス
 * @author Ryoto Tomioka
 * @version 1.0
 */
public class SocketConnection extends AsyncTask<String, Void, String>{

    static private String PORT;
    static private String IP_ADDRESS;

    private Socket socket;
    @SuppressLint("StaticFieldLeak")
    private Activity parentActivity;

    Handler handler = new Handler();

    static void setIpAddress(String ip) {
        IP_ADDRESS = ip;
    }

    static void setPORT(String PORT) {
        SocketConnection.PORT = PORT;
    }


    SocketConnection(Activity activity) {
        parentActivity = activity;
    }

    /**
     * ソケット通信の実行<br>
     * modeコマンドとdataコマンドを指定してSocketConnection.execute(mode, data)を実行することにより擬似窓の遠隔制御が実現できます。<br>
     * modeコマンドとdataコマンドの書式についてはUWP擬似窓システムの仕様書を参照してください。
     * @param str コマンドリスト：1つめのstrはmodeコマンドである'LIVE', 'IMAGE, 'VIDEO', 'BLANK'を指定します。2めのstrにはdataコマンドを指定します。
     */
    @Override
    protected String doInBackground(String... str) {
        InetSocketAddress endpoint = new InetSocketAddress(IP_ADDRESS, Integer.parseInt(PORT));
        try {
            Log.d("Debug", "IP:" + IP_ADDRESS + " PORT:" + PORT);
            socket = new Socket();
            socket.connect(endpoint, 2000);
        } catch (IOException e) {
            e.printStackTrace();
            handler.post(() -> Toast.makeText(StartActivity.getAppContext(), R.string.toast_connection_error, Toast.LENGTH_LONG).show());
            return null;
        }

        if (str.length == 1) {
            switch (str[0]) {
                case "IMAGE":
                case "VIDEO":
                    String res_i = sendCommand(str[0]);
                    if (res_i.equals("OK")) {
                        Intent intent_image = new Intent(parentActivity, ThumbsActivity.class);
                        intent_image.putExtra("MODE", str[0]);
                        parentActivity.startActivity(intent_image);
                    }
                    return "OK";
                case "GET_MODE":
                    updateModeText();
                    return "OK";
                case "GET_IMAGE_THUMBS":
                    return fetchImageThumbs("GET_IMAGE_THUMBS");
                case "GET_VIDEO_THUMBS":
                    return fetchImageThumbs("GET_VIDEO_THUMBS");
                default:
                    return sendCommand(str[0]);
            }
        }
        else if(str.length == 2) {
            return sendCommand(str[0] + "\n" + str[1]);
        }
        else {
            return null;
        }
    }

    /**
     * コマンド送出とチェック<br>
     * @param command 送出コマンド
     */
    private String sendCommand(String command) {
        try {
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));

            bw.write(command + "\n");
            bw.flush();

            // サーバからのOKを待機
            String resData = br.readLine();

            in.close();
            out.close();
            socket.close();

            return resData;
        }
        catch (Exception e) {
            e.printStackTrace();
            return "FAILED TO CONNECT.";
        }
    }

    /**
     * サムネイル画像の取得<br>
     * このメソッドはImageActivityからしか呼び出せません！
     * @return
     */
    private String fetchImageThumbs(String command) {
        try {
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));

            bw.write(command + "\n");
            bw.flush();

            ThumbsActivity act = (ThumbsActivity) parentActivity;

            // サーバからサムネイル数を待機
            int num = Integer.parseInt(br.readLine());

            // ToDo: Change to other good way
            // Layoutの整形のため微小時間待機
            Thread.sleep(300);

            String resData;
            for (int i=0; i<num; i++) {
                // Read Images as base64 string
                resData = br.readLine();
                String finalResData = resData;

                Thread thread = new Thread(() -> act.addThumbnailButton(decodeBase64(finalResData)));

                act.runOnUI(thread);

                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // 最終レスポンス
            String response = br.readLine();

            // 終了処理
            in.close();
            out.close();
            socket.close();

            return response;

        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 画面右上のステータスのアップデート
     */
    private void updateModeText() {
        try {
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));

            bw.write("GET_MODE" + "\n");
            bw.flush();

            String res = br.readLine();

            StartActivity act = (StartActivity)parentActivity;
            act.runOnUI(() -> {
                TextView view = act.findViewById(R.id.connectionText);
                String txt = "CONNECTED - " + res;
                view.setText(txt);
            });

            // 終了処理
            in.close();
            out.close();
            socket.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Bitmap decodeBase64(String input)
    {
        byte[] decodedBytes = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    @Override
    protected void onPostExecute(String s) {
        System.out.println(s);
    }
}


