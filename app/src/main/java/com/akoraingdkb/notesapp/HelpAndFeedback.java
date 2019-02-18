package com.akoraingdkb.notesapp;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class HelpAndFeedback extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_and_feedback);

        // Prevent activity (dialog) from being destroyed when user clicks outside
        this.setFinishOnTouchOutside(false);

        ImageView whatsapp = (ImageView) findViewById(R.id.help_whatsapp);
        whatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent whatsappIntent  = new Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=233578337689"));
                if (whatsappIntent.resolveActivity(getPackageManager()) != null)
                    startActivity(whatsappIntent);
                else
                    Toast.makeText(HelpAndFeedback.this, "Sorry:) No supported app found for this link", Toast.LENGTH_LONG).show();
            }
        });

        ImageView email = (ImageView) findViewById(R.id.help_email);
        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:akoraingdkb@gmail.com"))
                        .putExtra(Intent.EXTRA_SUBJECT, "Notes App: Help and Feedback");

                if (emailIntent.resolveActivity(getPackageManager()) != null)
                    startActivity(emailIntent);
                else
                    Toast.makeText(HelpAndFeedback.this, "Sorry:) No supported app found for this link", Toast.LENGTH_LONG).show();
            }
        });

        ImageView telegram = (ImageView) findViewById(R.id.help_telegram);
        telegram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent whatsappIntent  = new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/akora_ing_dkb"));
                if (whatsappIntent.resolveActivity(getPackageManager()) != null)
                    startActivity(whatsappIntent);
                else
                    Toast.makeText(HelpAndFeedback.this, "Sorry:) No supported app found for this link", Toast.LENGTH_LONG).show();
            }
        });
    }

}
