package com.andreassavva.expensemanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class IouEdit extends AppCompatActivity {

    // Aktiviteten som startas när man trycker på editknappen i IouInformation Tollbar.

    private static final String TAG = "AKS";

    private long contactId;
    private String moneyOwed;
    private EditText moneyOwedEditText;
    private TextView contactName;
    private Button showAllBtn;
    private RadioButton youOweThem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iou_edit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_close);

        // Får data från intent.
        Intent intent = getIntent();
        contactId = intent.getLongExtra("contactId", 0);
        moneyOwed = intent.getStringExtra("contactMoneyOwed");

        youOweThem = (RadioButton) findViewById(R.id.editYouOweThem);
        contactName = (TextView) findViewById(R.id.editContactName);
        moneyOwedEditText = (EditText) findViewById(R.id.editMoneyOwedText);

        moneyOwedEditText.setText(moneyOwed);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_iou_add, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.iou_add_menu_done:
                // När man väljer att man är klar med att redigera. Skickar tillbaka
                // kontakten med uppdaterade information.
                moneyOwed = moneyOwedEditText.getText().toString();
                if (!moneyOwed.equals("")) {
                    Intent returnIntent = new Intent(this, TabFragment1.class);
                    returnIntent.putExtra("contactId", contactId);
                    if (youOweThem.isChecked()) {
                        returnIntent.putExtra("moneyOwed", moneyOwed);
                    } else {
                        returnIntent.putExtra("moneyOwed", "-" + moneyOwed);
                    }
                    startActivity(returnIntent);
                } else {
                    Toast.makeText(getApplicationContext(), "Fill in the money owed", Toast.LENGTH_LONG).show();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}

