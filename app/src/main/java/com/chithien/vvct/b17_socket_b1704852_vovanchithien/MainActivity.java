package com.chithien.vvct.b17_socket_b1704852_vovanchithien;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity {

    // Khai báo các View
    private Button mButtonLogin;
    private Button mButtonChat;
    private EditText edtContent;
    private ListView lvUser, lvChat;
    private ImageButton btnAdd, btnSend;
    private TextView mTVRequest;
    ArrayList<String> arrayUser, arrayChat;
    ArrayAdapter adapterUser, adapterChat;
    // Khai báo ip và port của Server
    // Gõ lệnh ipconfig trên Command Promp hoặc mở Tark Manager để tìm ip.
    private final String URL_SERVER = "http://192.168.1.8:3000";
    private Socket mSocket; // Chọn Socket (IO.socket.client)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Ánh xạ các view từ layout
        btnAdd = findViewById(R.id.btn_login);
        btnSend = findViewById(R.id.btnchat);
        edtContent = findViewById(R.id.editTextContent);
        lvChat = findViewById(R.id.list_chat);
        lvUser = findViewById(R.id.list_user);
        // Khởi tạo arrayUser, adapterUser và đưa arrayUser vào ListView
        arrayUser = new ArrayList<>();
        adapterUser = new ArrayAdapter(this, android.R.layout.simple_list_item_1,
                arrayUser);
        lvUser.setAdapter(adapterUser);
        // Khởi tạo arrayChat, adapterChat và đưa arrayChat vào ListView
        arrayChat = new ArrayList<>();
        adapterChat = new ArrayAdapter(this, android.R.layout.simple_list_item_1,
                arrayChat);
        lvChat.setAdapter(adapterChat);
        // Gởi yêu cầu kết nối
        try {
            mSocket = IO.socket(URL_SERVER);
            Log.e("TAG", "onCreate: " );
        } catch (URISyntaxException e) {
            Log.e("TAG", "onCreate: " + e.getMessage());
        }

        mSocket.connect();
        mSocket.on("server-send-data", onRetrieveResult);
        mSocket.on("server-send-user", onListUser);
        mSocket.on("server-send-chat", onListChat);

        // Xử lý tương tác cho nút đăng ký user.
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Nếu có nhập tên vào EditText thì gởi đăng ký User.
                if(edtContent.getText().toString().trim().length()>0){
                    mSocket.emit("client-register-user", edtContent.getText().toString());
                    // Nhớ sửa lại sự kiện bên Server
                }
            }
        });
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Nếu có nhập message vào EditText thì gởi.
                if (edtContent.getText().toString().trim().length() > 0) {
                    mSocket.emit("client-send-chat", edtContent.getText().toString());
                    Log.e("TAG2", edtContent.getText().toString());
                    edtContent.setText("");
                    // Nhớ sửa lại sự kiện bên Server
                }
            }
        });
    }
    private Emitter.Listener onListChat = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject object = (JSONObject) args[0];
                    try {
                        String noiDung = object.getString("chatComent");
                        arrayChat.add(noiDung);
                        adapterChat.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };
    private Emitter.Listener onListUser = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject object = (JSONObject) args[0];
                    try {
                        JSONArray array = object.getJSONArray("danhsach");
                        adapterUser.clear();
                        for (int i = 0; i < array.length(); i++) {
                            String username = array.getString(i);
                            adapterUser.add(username);
                        }
                        adapterUser.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };
    private Emitter.Listener onRetrieveResult = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject object = (JSONObject) args[0];
                    try {
                        //String ten = object.getString("noidung");
                        boolean exits = object.getBoolean("ketqua");
                        if(exits) {
                            Toast.makeText(MainActivity.this, "Tài khoản này đã tôn tại!", Toast.LENGTH_LONG).show();
                        }else {
                            Toast.makeText(MainActivity.this, "Đã đăng ký thành công", Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };
}