package com.example.mysignupapp.Utility;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.widget.AppCompatButton;

import com.example.mysignupapp.R;

public class NetworkChangeListener extends BroadcastReceiver
{

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if(!Common.IsConnected(context))
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            View layout_dialog = LayoutInflater.from(context).inflate(R.layout.internet_connection_check, null);
            builder.setView(layout_dialog);

            AppCompatButton retry = layout_dialog.findViewById(R.id.retry);

            AlertDialog dialog = builder.create();
            dialog.show();
            dialog.setCancelable(false);
            dialog.getWindow().setGravity(Gravity.CENTER);

            retry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    dialog.dismiss();
                    onReceive(context, intent);
                }
            });
        }
    }
}
