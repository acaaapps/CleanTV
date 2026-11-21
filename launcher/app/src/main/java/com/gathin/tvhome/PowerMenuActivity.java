package com.gathin.tvhome;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

public class PowerMenuActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_power_menu);

        View btnPowerOff = findViewById(R.id.btnPowerOff);
        View btnReboot = findViewById(R.id.btnReboot);
        View btnScreensaver = findViewById(R.id.btnScreensaver);

        View.OnFocusChangeListener focusAnim = (v, hasFocus) -> {
            v.setScaleX(hasFocus ? 1.08f : 1f);
            v.setScaleY(hasFocus ? 1.08f : 1f);
        };
        btnPowerOff.setOnFocusChangeListener(focusAnim);
        btnReboot.setOnFocusChangeListener(focusAnim);
        btnScreensaver.setOnFocusChangeListener(focusAnim);

        btnPowerOff.setNextFocusRightId(R.id.btnReboot);
        btnReboot.setNextFocusLeftId(R.id.btnPowerOff);
        btnReboot.setNextFocusRightId(R.id.btnScreensaver);
        btnScreensaver.setNextFocusLeftId(R.id.btnReboot);

        btnPowerOff.setOnClickListener(v -> {
            finish();
            HomeKeyService.executePowerAction(HomeKeyService.POWER_ACTION_SHUTDOWN);
        });

        btnReboot.setOnClickListener(v -> {
            finish();
            HomeKeyService.executePowerAction(HomeKeyService.POWER_ACTION_REBOOT);
        });

        btnScreensaver.setOnClickListener(v -> {
            finish();
            startActivity(new Intent(this, ClockActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        });

        findViewById(android.R.id.content).setOnClickListener(v -> finish());

        // Default focus on Apagar
        btnPowerOff.requestFocus();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            finish();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
