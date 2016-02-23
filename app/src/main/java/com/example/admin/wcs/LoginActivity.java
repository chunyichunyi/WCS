package com.example.admin.wcs;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends BaseActivity {

    private TextView textContent;
    private Button buttonLogin;
    private Button buttonDebug;
    private final String DEFAULT_IP = "192.168.8.1";
    private final String DEFAULT_PORT = "8088";
    private EditText editTextIp, editTextPort;
    private MyApplication mMyApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();

        textContent = (TextView)findViewById(R.id.text_content);
        textContent.setText("Wireless Control System");

        buttonLogin = (Button)findViewById(R.id.button_login);
        buttonDebug = (Button)findViewById(R.id.button_debug);

        editTextIp = (EditText) findViewById(R.id.editText_ip);
        editTextPort = (EditText) findViewById(R.id.editText_port);

        editTextIp.setText(DEFAULT_IP);
        editTextPort.setText(DEFAULT_PORT);

        mMyApplication = ((MyApplication)getApplicationContext());

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String serverIp = editTextIp.getText().toString();
                int serverPort = Integer.parseInt(editTextPort.getText().toString());
                boolean ret;

                ret = mMyApplication.UserLogin(serverIp, serverPort);

                if (ret) {
                    Toast.makeText(LoginActivity.this, "login operation successful", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }  else {
                    Toast.makeText(LoginActivity.this, "login operation failed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonDebug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMyApplication.SetDebugFlag(true);

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
