package com.example.vpn.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import com.example.vpn.vpn.MyVpnService;

/**
 * Минимальный UI: кнопки Connect / Disconnect + статус.
 */
public class MainActivity extends Activity {

    private static final int VPN_REQUEST_CODE = 1;

    private Button btnConnect;
    private Button btnDisconnect;
    private TextView tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnConnect = findViewById(R.id.btnConnect);
        btnDisconnect = findViewById(R.id.btnDisconnect);
        tvStatus = findViewById(R.id.tvStatus);

        btnConnect.setOnClickListener(v -> requestVpnPermission());
        btnDisconnect.setOnClickListener(v -> stopVpn());

        setStatus("Не подключено");
    }

    /**
     * Запрашивает разрешение VPN у системы
     */
    private void requestVpnPermission() {
        Intent intent = VpnService.prepare(this);
        if (intent != null) {
            startActivityForResult(intent, VPN_REQUEST_CODE);
        } else {
            onActivityResult(VPN_REQUEST_CODE, RESULT_OK, null);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VPN_REQUEST_CODE && resultCode == RESULT_OK) {
            startVpn();
        }
    }

    private void startVpn() {
        Intent intent = new Intent(this, MyVpnService.class);
        intent.setAction(MyVpnService.ACTION_START);
        startService(intent);
        setStatus("Подключено");
    }

    private void stopVpn() {
        Intent intent = new Intent(this, MyVpnService.class);
        intent.setAction(MyVpnService.ACTION_STOP);
        startService(intent);
        setStatus("Не подключено");
    }

    private void setStatus(String msg) {
        tvStatus.setText("Статус: " + msg);
    }
}